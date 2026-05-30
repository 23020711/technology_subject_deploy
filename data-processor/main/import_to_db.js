const { Client } = require('pg');
const fs   = require('fs');
const path = require('path');
const dns  = require('dns');

// Load .env.db file
const envPath = path.join(__dirname, '..\\..\\data-processor\\.env.db');
if (fs.existsSync(envPath)) {
  const envContent = fs.readFileSync(envPath, 'utf8');
  envContent.split('\n').forEach(line => {
    const [key, ...vals] = line.split('=');
    if (key && vals.length) {
      if (!process.env[key.trim()]) {
        process.env[key.trim()] = vals.join('=').trim();
      }
    }
  });
}

dns.setDefaultResultOrder('ipv4first');

const OUTPUT_PATH = path.join(__dirname, '..\\..\\crawl-data\\pricehawk_output.json');

console.log('Script bat dau...');
console.log('OUTPUT_PATH:', OUTPUT_PATH);
console.log('');

async function importToDb() {
  let client = null;

  try {
    // Hardcoded for Supabase pooler
    const dbHost = 'aws-1-ap-northeast-2.pooler.supabase.com';
    const dbPort = 5432;
    const dbUser = 'postgres.astkanfsacxriwprspqr';
    const dbPassword = 'PriceHawl123@';
    const dbName = 'postgres';
    
    console.log('Doc cau hinh...');
    console.log(`Ket noi toi: ${dbHost}:${dbPort}`);
    console.log('Doc config xong');

    // Ket noi DB
    client = new Client({
      host:     dbHost,
      port:     dbPort,
      database: dbName,
      user:     dbUser,
      password: dbPassword,
      ssl:      { rejectUnauthorized: false },
    });
    console.log('Dang ket noi Supabase...');
    await client.connect();
    console.log('Ket noi thanh cong!\n');

    // Doc file output
    console.log('Doc file output...');
    if (!fs.existsSync(OUTPUT_PATH)) {
      throw new Error(`Khong tim thay file: ${OUTPUT_PATH}`);
    }
    const json     = JSON.parse(fs.readFileSync(OUTPUT_PATH, 'utf8'));
    const products = json.products;
    console.log(`Bat dau import ${products.length} san pham...\n`);

    let successCount = 0;
    let errorCount   = 0;

    for (let i = 0; i < products.length; i++) {
      const entry       = products[i];
      const productName = entry.product?.name || 'Khong ten';

      try {
        await client.query('BEGIN');

        // A. BRAND
        const brandRes = await client.query(
          `INSERT INTO brand (name, slug)
           VALUES ($1, $2)
           ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name
           RETURNING id`,
          [entry.brand.name, entry.brand.slug]
        );
        const brandId = brandRes.rows[0].id;

        // B. CATEGORY
        const catName = entry.product.category_name || 'Cham soc da';
        const catSlug = catName
          .toLowerCase()
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .trim()
          .replace(/\s+/g, '-');

        let categoryId;
        const existCat = await client.query(
          `SELECT id FROM category WHERE slug = $1 LIMIT 1`,
          [catSlug]
        );
        if (existCat.rows.length > 0) {
          categoryId = existCat.rows[0].id;
        } else {
          const newCat = await client.query(
            `INSERT INTO category (name, slug) VALUES ($1, $2) RETURNING id`,
            [catName, catSlug]
          );
          categoryId = newCat.rows[0].id;
        }

        // C. PRODUCT
        const prod    = entry.product;
        const prodRes = await client.query(
          `INSERT INTO product (
             name, brand_id, category_id, barcode,
             description, image_url, skin_type,
             volume_ml, attributes, popularity_score
           )
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
           ON CONFLICT (name) DO UPDATE SET
             updated_at  = NOW(),
             description = COALESCE(EXCLUDED.description, product.description),
             image_url   = COALESCE(EXCLUDED.image_url,   product.image_url),
             barcode     = COALESCE(EXCLUDED.barcode,     product.barcode)
           RETURNING id`,
          [
            prod.name,
            brandId,
            categoryId,
            prod.barcode          || null,
            prod.description      || null,
            prod.image_url        || null,
            prod.skin_type        || null,
            prod.volume_ml        || null,
            JSON.stringify(prod.attributes || {}),
            prod.popularity_score ?? 0,
          ]
        );
        const productId = prodRes.rows[0].id;

        // D. LISTINGS + PRICE_RECORD
        const listings = entry.listings || [];
        for (const listing of listings) {
          const listRes = await client.query(
            `INSERT INTO product_listing (
               product_id, platform_id, platform_name, url,
               platform_image_url, crawl_time,
               status, trust_score, is_fake_promo, is_pinned
             )
             VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
             ON CONFLICT (url) DO UPDATE SET
               updated_at         = NOW(),
               platform_id        = EXCLUDED.platform_id,
               platform_name      = EXCLUDED.platform_name,
               platform_image_url = COALESCE(EXCLUDED.platform_image_url, product_listing.platform_image_url),
               status             = EXCLUDED.status,
               trust_score        = EXCLUDED.trust_score
             RETURNING id`,
            [
              productId,
              listing.platform_id,
              listing.platform_name,
              listing.url,
              listing.platform_image_url || null,
              listing.crawl_time         || null,
              listing.status             || 'active',
              listing.trust_score        ?? 1.0,
              listing.is_fake_promo      ?? false,
              listing.is_pinned          ?? false,
            ]
          );
          const listingId = listRes.rows[0].id;

          // price_record
          const pr = listing.price_record;
          if (pr && pr.price && pr.price > 0) {
            await client.query(
              `INSERT INTO price_record (
                 product_listing_id, price, original_price,
                 discount_pct, in_stock, is_flash_sale, promotion_label
               )
               VALUES ($1,$2,$3,$4,$5,$6,$7)`,
              [
                listingId,
                Math.round(pr.price),
                pr.original_price  ? Math.round(pr.original_price) : null,
                pr.discount_pct    ?? null,
                pr.in_stock        ?? true,
                pr.is_flash_sale   ?? false,
                pr.promotion_label || null,
              ]
            );
          }
        }

        await client.query('COMMIT');
        successCount++;

        if (i % 20 === 0 || i === products.length - 1) {
          console.log(`[${i + 1}/${products.length}] OK: ${productName}`);
        }

      } catch (itemErr) {
        await client.query('ROLLBACK');
        errorCount++;
        console.error(`Loi [${i + 1}] "${productName}": ${itemErr.message}`);
      }
    }

    console.log(`\n${'─'.repeat(40)}`);
    console.log(`Thanh cong: ${successCount}`);
    console.log(`That bai:   ${errorCount}`);
    console.log(`Import hoan tat!`);

  } catch (err) {
    console.error('LOI HE THONG:', err.message);
  } finally {
    if (client) {
      await client.end();
      console.log('Da dong ket noi.');
    }
  }
}

importToDb().then(() => {
  console.log('Script ket thuc.');
}).catch((err) => {
  console.error('Loi ngoai cung:', err);
});
