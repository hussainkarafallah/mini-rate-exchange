CREATE TABLE orders (
    id UUID PRIMARY KEY,
    instrument VARCHAR(32) NOT NULL,
    order_state VARCHAR(32) NOT NULL,
    order_type VARCHAR(32) NOT NULL,
    trader_id BIGINT NOT NULL,
    fulfillments BYTEA,
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP NOT NULL,
    version INT DEFAULT 0
);
