CREATE TABLE IF NOT EXISTS `mercenaries_orders` (
  `target` int(11) NOT NULL,
  `targetName` varchar(20) DEFAULT NULL,
  `client` int(11) NOT NULL,
  `clientName` varchar(20) DEFAULT NULL,
  `countToKill` int(11) NOT NULL,
  PRIMARY KEY (`target`,`client`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

