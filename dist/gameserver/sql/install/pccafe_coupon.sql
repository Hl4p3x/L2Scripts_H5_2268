DROP TABLE IF EXISTS `pccafe_coupons`;
CREATE TABLE `pccafe_coupons` (
	`serial_code` VARCHAR(20) NOT NULL,
	`type` TINYINT(1) NOT NULL,
	`value` VARCHAR(300) NOT NULL DEFAULT '',
	`used_by` INT NOT NULL DEFAULT '0',
	PRIMARY KEY (serial_code)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;