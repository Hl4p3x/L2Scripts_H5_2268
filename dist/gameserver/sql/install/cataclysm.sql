DROP TABLE IF EXISTS `cataclysm`;
CREATE TABLE `cataclysm` (
  `town` varchar(255) NOT NULL DEFAULT '',
  `player_name` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`town`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO cataclysm VALUES 
('Aden', ""),
('Dion', ""),
('Giran', ""),
('Gludio', ""),
('Goddard', ""),
('Heine', ""),
('Oren', ""),
('Rune', ""),
('Schuttgart', "");