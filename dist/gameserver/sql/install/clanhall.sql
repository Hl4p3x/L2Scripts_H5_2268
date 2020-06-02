DROP TABLE IF EXISTS `fortress`;
CREATE TABLE `clanhall` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(40) NOT NULL DEFAULT '',
  `last_siege_date` bigint(20) NOT NULL,
  `own_date` bigint(20) NOT NULL,
  `siege_date` bigint(20) NOT NULL,
  `auction_min_bid` bigint(20) NOT NULL,
  `auction_length` int(11) NOT NULL,
  `auction_desc` text,
  `cycle` int(11) NOT NULL,
  `paid_cycle` int(11) NOT NULL,
  PRIMARY KEY (`id`,`name`)
);