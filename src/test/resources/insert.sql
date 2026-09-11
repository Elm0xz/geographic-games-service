INSERT INTO game (name, scoring_system)
values ('WhenTaken', 'STANDARD'), ('Mapster', 'STANDARD'), ('Reborder', 'STANDARD');

INSERT INTO player (name)
values ('Andrzej'), ('Ferdynand');

INSERT INTO daily_entry (game_id, player_id, entry_date, points)
values ( 1, 1,'2026-09-01', 933),
       ( 1, 1,'2026-09-02', 856),
       ( 1, 1,'2026-09-03', 777),
       ( 1, 2,'2026-09-01', 928),
       ( 1, 2,'2026-09-02', 856),
       ( 1, 2,'2026-09-03', 903),
       ( 2, 1,'2026-09-01', 620),
       ( 2, 2,'2026-09-02', 640);
