DROP TABLE IF EXISTS `castle`;
CREATE TABLE `castle` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `tax_percent` int(11) NOT NULL,
  `treasury` bigint(20) unsigned NOT NULL DEFAULT '0',
  `last_siege_date` bigint(20) NOT NULL,
  `own_date` bigint(20) NOT NULL,
  `siege_date` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
);