CREATE TABLE IF NOT EXISTS outgoing_message_0_0 (
  id BIGSERIAL PRIMARY KEY,
  message BYTEA NOT NULL
) WITH (autovacuum_analyze_threshold=2000000000, toast_tuple_target=8160);

CREATE TABLE IF NOT EXISTS outgoing_message_1_0 (
  id BIGSERIAL PRIMARY KEY,
  message BYTEA NOT NULL
) WITH (autovacuum_analyze_threshold=2000000000, toast_tuple_target=8160);