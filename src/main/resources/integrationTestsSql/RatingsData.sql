-- --------------------------------------------------------
-- Host:                         192.168.0.106
-- Server version:               10.3.8-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
SET FOREIGN_KEY_CHECKS = 0;

-- Dumping database structure for extensions-market
CREATE DATABASE IF NOT EXISTS `extensions-market-test` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `extensions-market-test`;

-- Dumping structure for table extensions-market.ratings
DELETE FROM `ratings`;
ALTER TABLE `ratings` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `ratings` (
  `user` bigint(20) NOT NULL,
  `rating` tinyint(4) NOT NULL DEFAULT 0,
  `extension` bigint(20) NOT NULL,
  PRIMARY KEY (`user`,`extension`),
  KEY `extension` (`extension`),
  CONSTRAINT `FK1_extensions_extension_id` FOREIGN KEY (`extension`) REFERENCES `extensions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK2_users_user_id` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market.ratings: ~44 rows (approximately)
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
INSERT INTO `ratings` (`user`, `rating`, `extension`) VALUES
	(2, 5, 1),
	(1, 5, 2),
	(1, 5, 3),
	(3, 5, 4),
	(1, 4, 4),
	(1, 4, 5),
	(1, 4, 6),
	(2, 5, 6),
	(1, 3, 7),
	(3, 5, 7),
	(1, 4, 8),
	(2, 5, 8),
	(1, 5, 9),
	(1, 5, 10),
	(3, 5, 10);
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
