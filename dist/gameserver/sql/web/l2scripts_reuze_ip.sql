DROP TABLE IF EXISTS `l2scripts_reuze_ip`;
CREATE TABLE `l2scripts_reuze_ip` (
  `type` text NOT NULL,
  `ip` text NOT NULL,
  `time` int(11) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=cp1251;