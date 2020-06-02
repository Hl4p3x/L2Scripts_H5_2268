CREATE TABLE IF NOT EXISTS `event_tvt_arena` (
	`obj_Id` int(11) NOT NULL DEFAULT '0',
	`char_name` varchar(35) NOT NULL DEFAULT '',
	`char_level` int(11) NOT NULL DEFAULT '0',
	`char_points` int(11) NOT NULL DEFAULT '0',
	`arena_id` int(11) NOT NULL DEFAULT '0',
	`event_time` INT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY (`obj_Id`),
	UNIQUE KEY `char_name` (`char_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;