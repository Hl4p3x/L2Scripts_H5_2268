DROP TABLE IF EXISTS `installed_updates`;
CREATE TABLE `installed_updates` (
	`file_name` VARCHAR(300) CHARACTER SET UTF8 NOT NULL DEFAULT '0',
	PRIMARY KEY  (`file_name`)
) ENGINE=MyISAM;

INSERT INTO installed_updates (`file_name`) VALUES
("2013_04_08 01-41"),
("2013_07_23 22-56"),
("2014_06_25 19_54"),
("2014_07_06 20-08"),
("2015_04_07 23-54"),
("2015_04_10 19-42"),
("2015_05_30 22-28"),
("2015_09_22 13-15"),
("2016_03_10 01-35"),
("2016_04_23 15-13"),
("2016_12_07 14-34"),
("2017_01_07 01-50"),
("2017_01_25 13-53"),
("2017_01_31 13-45"),
("2017_09_20 00-54"),
("2018_03_07 02-24"),
("2018_03_14 21-52"),
("2018_05_04 00-08"),
("2018_08_14 12-37"),
("2018_10_26 22-51"),
("2018_10_26 23-16"),
("2018_11_29 01-11"),
("2019_01_05 05-25"),
("2019_01_07 06-40"),
("2019_02_14 02-34"),
("2019_02_14 15-50");