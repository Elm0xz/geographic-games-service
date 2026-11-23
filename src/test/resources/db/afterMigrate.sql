TRUNCATE table player, game, daily_entry, weekly_position, weekly_ranking RESTART IDENTITY;

INSERT INTO game (name, scoring_system)
values ('WhenTaken', 'STANDARD'), ('Mapster', 'STANDARD'), ('Reborder', 'STANDARD');

INSERT INTO player (name)
values ('Andrzej'), ('Ferdynand');

INSERT INTO daily_entry (game_id, player_id, entry_date, points, submitted_at)
VALUES (1, 1, '2026-09-01', 933, '2026-09-01 00:00:00+00'),
       (1, 1, '2026-09-02', 856, '2026-09-02 00:00:00+00'),
       (1, 1, '2026-09-03', 777, '2026-09-03 00:00:00+00'),
       (1, 2, '2026-09-01', 928, '2026-09-01 00:00:00+00'),
       (1, 2, '2026-09-02', 856, '2026-09-02 00:00:00+00'),
       (1, 2, '2026-09-03', 903, '2026-09-03 00:00:00+00'),
       (2, 1, '2026-09-01', 620, '2026-09-01 00:00:00+00'),
       (2, 2, '2026-09-02', 640, '2026-09-02 00:00:00+00');