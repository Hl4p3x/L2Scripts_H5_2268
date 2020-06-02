DELETE FROM gameservers;
ALTER TABLE gameservers ADD COLUMN `ip` varchar(32) NOT NULL AFTER `id`;
ALTER TABLE gameservers ADD COLUMN `port` smallint(6) NOT NULL AFTER `ip`;
ALTER TABLE gameservers ADD COLUMN `age_limit` tinyint(3) NOT NULL AFTER `port`;
ALTER TABLE gameservers ADD COLUMN `pvp` tinyint(3) NOT NULL AFTER `age_limit`;
ALTER TABLE gameservers ADD COLUMN `max_players` smallint(6) NOT NULL AFTER `pvp`;
ALTER TABLE gameservers ADD COLUMN `type` int(11) NOT NULL AFTER `max_players`;
ALTER TABLE gameservers ADD COLUMN `brackets` tinyint(3) NOT NULL AFTER `type`;