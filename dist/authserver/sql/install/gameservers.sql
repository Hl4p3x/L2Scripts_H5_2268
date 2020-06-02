CREATE TABLE IF NOT EXISTS `gameservers` (
  `id` int(11) NOT NULL,
  `ip` varchar(32) NOT NULL,
  `port` smallint(6) NOT NULL,
  `age_limit` tinyint(3) NOT NULL,
  `pvp` tinyint(3) NOT NULL,
  `max_players` smallint(6) NOT NULL,
  `type` int(11) NOT NULL,
  `brackets` tinyint(3) NOT NULL,
  `key` varchar(255),
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;