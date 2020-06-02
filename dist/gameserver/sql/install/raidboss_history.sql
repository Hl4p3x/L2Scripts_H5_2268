DROP TABLE IF EXISTS `raidboss_history`;
CREATE TABLE `raidboss_history` (
	`killer_object_id` INT NOT NULL,
	`killer_name` varchar(35) NOT NULL DEFAULT '',
	`raid_id` INT NOT NULL,
	`raid_name` varchar(35) NOT NULL DEFAULT '',
	`kill_time` INT NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;