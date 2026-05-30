-- ============================================================
-- V3__add_missing_tables.sql
-- Thêm các bảng còn thiếu cho entity models
-- ============================================================

-- Bảng affiliate_clicks
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

CREATE INDEX idx_affiliate_clicks_clicked_at ON affiliate_clicks(clicked_at DESC);
CREATE INDEX idx_affiliate_clicks_user_id ON affiliate_clicks(user_id);
CREATE INDEX idx_affiliate_clicks_product_id ON affiliate_clicks(product_id);

-- Bảng crawl_errors
CREATE TABLE crawl_errors (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    platform            VARCHAR(100) NOT NULL,
    product_listing_id  UUID,
    product_name        VARCHAR(500),
    url                 VARCHAR(1000),
    error_type          VARCHAR(50),
    error_message       TEXT,
    crawled_at          TIMESTAMP   DEFAULT NOW()
);

CREATE INDEX idx_crawl_errors_platform ON crawl_errors(platform);
CREATE INDEX idx_crawl_errors_crawled_at ON crawl_errors(crawled_at DESC);

-- Bảng notifications
CREATE TABLE notifications (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    alert_id    UUID,
    product_id  UUID,
    title       VARCHAR(255) NOT NULL,
    message     TEXT        NOT NULL,
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Bảng payment_orders
CREATE TABLE payment_orders (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users(id),
    plan            VARCHAR(50) NOT NULL,
    method          VARCHAR(50) NOT NULL,
    amount          INT         NOT NULL,
    transfer_code   VARCHAR(100) NOT NULL UNIQUE,
    status          VARCHAR(50) NOT NULL,
    proof_image     TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    confirmed_at    TIMESTAMP,
    submitted_at    TIMESTAMP
);

CREATE INDEX idx_payment_orders_user_id ON payment_orders(user_id);
CREATE INDEX idx_payment_orders_transfer_code ON payment_orders(transfer_code);
CREATE INDEX idx_payment_orders_status ON payment_orders(status);
