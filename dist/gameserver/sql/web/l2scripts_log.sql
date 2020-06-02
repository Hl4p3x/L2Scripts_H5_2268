DROP TABLE IF EXISTS `l2scripts_log`;
CREATE TABLE `l2scripts_log` (
  `ip` varchar(255) NOT NULL,
  `date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `nick` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `param` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=cp1251;