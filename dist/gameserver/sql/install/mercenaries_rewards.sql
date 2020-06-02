CREATE TABLE IF NOT EXISTS `mercenaries_rewards` (
  `target` int(11) NOT NULL,
  `id` bigint(25) NOT NULL,
  `count` bigint(25) NOT NULL,
  PRIMARY KEY (`target`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
