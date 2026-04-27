CREATE TABLE IF NOT EXISTS ext_country_stats (
    country_id BIGINT NOT NULL PRIMARY KEY,
    scam_cnt   BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ext_state_stats (
    state_id BIGINT NOT NULL PRIMARY KEY,
    scam_cnt BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ext_city_stats (
    city_id  BIGINT NOT NULL PRIMARY KEY,
    scam_cnt BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ext_scam_action_stats (
    country_id     BIGINT NOT NULL,
    scam_action_id BIGINT NOT NULL,
    scam_cnt       BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (country_id, scam_action_id)
);

CREATE TABLE IF NOT EXISTS ext_scam_context_stats (
    country_id      BIGINT NOT NULL,
    scam_context_id BIGINT NOT NULL,
    scam_cnt        BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (country_id, scam_context_id)
);
