DROP TABLE IF EXISTS `l2scripts_ban_ip`;
CREATE TABLE `l2scripts_ban_ip` (
  `type` varchar(255) NOT NULL,
  `ip` varchar(20) NOT NULL,
  `time` varchar(255) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=cp1251 ROW_FORMAT=DYNAMIC;