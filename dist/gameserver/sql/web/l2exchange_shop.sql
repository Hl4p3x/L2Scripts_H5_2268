DROP TABLE IF EXISTS `l2exchange_shop`;
CREATE TABLE `l2exchange_shop` (
  `account` varchar(255) NOT NULL,
  `count` int(255) DEFAULT NULL,
  PRIMARY KEY (`account`)
) ENGINE=MyISAM DEFAULT CHARSET=cp1251;