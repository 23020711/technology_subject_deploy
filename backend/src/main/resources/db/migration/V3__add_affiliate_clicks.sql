-- ============================================================
-- V3__add_affiliate_clicks.sql
-- Thêm bảng affiliate_clicks cho AffiliateService
-- ============================================================

CREATE TABLE affiliate_clicks (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID,
    product_id  UUID,
    platform    VARCHAR(100) NOT NULL,
    click_id    VARCHAR(255) UNIQUE,
    ip          VARCHAR(100),
    user_agent  TEXT,
    clicked_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_affiliate_clicks_clicked_at
    ON affiliate_clicks(clicked_at DESC);

CREATE INDEX idx_affiliate_clicks_user_id
    ON affiliate_clicks(user_id);

CREATE INDEX idx_affiliate_clicks_product_id
    ON affiliate_clicks(product_id);
