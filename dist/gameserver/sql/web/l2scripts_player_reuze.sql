DROP TABLE IF EXISTS `l2scripts_player_reuze`;
CREATE TABLE `l2scripts_player_reuze` (
  `type` varchar(45) NOT NULL,
  `player_id` varchar(20) NOT NULL,
  `time` varchar(255) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=cp1251 ROW_FORMAT=DYNAMIC;