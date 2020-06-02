CREATE TABLE IF NOT EXISTS `fences` (
	`object_id` INT NOT NULL,
	`name` VARCHAR(200) NOT NULL DEFAULT '',
	`x` INT NOT NULL,
	`y` INT NOT NULL,
	`z` INT NOT NULL,
	`width` INT NOT NULL,
	`length` INT NOT NULL,
	`height` INT NOT NULL,
	`state` TINYINT NOT NULL,
	PRIMARY KEY (`object_id`)
) ENGINE=MyISAM;