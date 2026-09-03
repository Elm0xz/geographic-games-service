CREATE TABLE weekly_ranking
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_id BIGINT  NOT NULL REFERENCES game (id),
    year INTEGER NOT NULL,
    week INTEGER NOT NULL,
    CONSTRAINT uq_weekly_ranking_game_year_week UNIQUE (game_id, year, week)
);

CREATE TABLE weekly_position
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    weekly_ranking_id BIGINT  NOT NULL REFERENCES weekly_ranking (id),
    player_id BIGINT  NOT NULL REFERENCES player (id),
    wins INTEGER NOT NULL,
    points FLOAT NOT NULL,
    CONSTRAINT uq_weekly_position_weekly_ranking_player UNIQUE (weekly_ranking_id, player_id)
);