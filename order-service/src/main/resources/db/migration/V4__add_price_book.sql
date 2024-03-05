CREATE TABLE IF NOT EXISTS price_book (
    instrument VARCHAR(255) NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    PRIMARY KEY (instrument)
);
