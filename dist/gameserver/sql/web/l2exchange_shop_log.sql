DROP TABLE IF EXISTS `l2exchange_shop_log`;
CREATE TABLE `l2exchange_shop_log` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `account` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `comment` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2767 DEFAULT CHARSET=cp1251;