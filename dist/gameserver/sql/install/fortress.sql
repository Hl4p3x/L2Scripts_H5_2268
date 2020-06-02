DROP TABLE IF EXISTS `fortress`;
CREATE TABLE `fortress` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(45) NOT NULL,
  `state` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `castle_id` int(11) NOT NULL,
  `last_siege_date` bigint(20) NOT NULL,
  `own_date` bigint(20) NOT NULL,
  `siege_date` bigint(20) NOT NULL,
  `supply_count` bigint(20) NOT NULL,
  `facility_0` int(20) NOT NULL,
  `facility_1` int(11) NOT NULL,
  `facility_2` int(11) NOT NULL,
  `facility_3` int(11) NOT NULL,
  `facility_4` int(11) NOT NULL,
  `cycle` int(11) NOT NULL,
  `reward_count` int(11) NOT NULL,
  `paid_cycle` int(11) NOT NULL,
  `supply_spawn` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);