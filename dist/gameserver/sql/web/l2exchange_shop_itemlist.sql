DROP TABLE IF EXISTS `l2exchange_shop_itemlist`;
CREATE TABLE `l2exchange_shop_itemlist` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `itemid` int(255) NOT NULL DEFAULT '0',
  `count` int(255) DEFAULT NULL,
  `cost` int(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=8 DEFAULT CHARSET=cp1251;