CREATE TABLE game
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           TEXT NOT NULL,
    scoring_system TEXT NOT NULL,
    CONSTRAINT uq_game_name UNIQUE (name)
);

CREATE TABLE player
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    CONSTRAINT uq_player_name UNIQUE (name)
);

CREATE TABLE daily_entry
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_id    BIGINT  NOT NULL REFERENCES game (id),
    player_id  BIGINT  NOT NULL REFERENCES player (id),
    entry_date DATE    NOT NULL,
    points     INTEGER NOT NULL,
    CONSTRAINT uq_daily_entry_game_player_date UNIQUE (game_id, player_id, entry_date)
);

CREATE INDEX idx_daily_entry_game_date ON daily_entry (game_id, entry_date);
