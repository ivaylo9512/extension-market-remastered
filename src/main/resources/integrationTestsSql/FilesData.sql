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
CREATE DATABASE IF NOT EXISTS `extensions-market-test` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `extensions-market-test`;

-- Dumping structure for table extensions-market-test.files
DELETE FROM `files`;
ALTER TABLE `files` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(50) NOT NULL DEFAULT '0',
  `owner` bigint(20) NOT NULL,
  `extension` bigint(20) NOT NULL,
  `extension_type` tinytext NOT NULL,
  `type` varchar(50) NOT NULL DEFAULT '0',
  `extension_id` int DEFAULT NULL,
  `download_count` int NOT NULL DEFAULT 0,
  `size` double NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_files_extensions` (`extension_id`),
  KEY `FK_files_users` (`owner`),
  CONSTRAINT `FK_files_extensions` FOREIGN KEY (`extension_id`) REFERENCES `extensions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_files_users` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;

-- Dumping data for table extensions-market-test.files: ~8 rows (approximately)
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
INSERT INTO `files` (`id`, `resource_type`, `owner`, `extension`, `extension_type`, `type`, `download_count`, `size`) VALUES
	(1, 'profileImage', 1, NULL, 'png', 'image/png', 0, 43250),
	(2, 'profileImage', 2, NULL, 'png', 'image/png', 0, 46000),
	(3, 'profileImage', 3, NULL, 'png', 'image/png', 0, 32000),
	(4, 'profileImage', 4, NULL, 'png', 'image/png', 0, 37000),
	(5, 'profileImage', 5, NULL, 'png', 'image/png', 0, 50000),
	(6, 'profileImage', 6, NULL, 'png', 'image/png', 0, 20000),
	(7, 'profileImage', 7, NULL, 'png', 'image/png', 0, 31500),
	(8, 'profileImage', 8, NULL, 'png', 'image/png', 0, 31200),
	(9, 'test', 3, 1, 'png', 'image/png', 0, 66800),
	(10, 'file', 8, 2, 'text', 'plain/text', 2, 31200),
	(11, 'file', 8, 3, 'text', 'plain/text', 3, 31200),
	(12, 'file', 8, 4, 'text', 'plain/text', 5, 31200),
	(13, 'file', 8, 5, 'text', 'plain/text', 3, 31200),
	(14, 'file', 8, 6, 'text', 'plain/text', 6, 31200),
	(15, 'file', 8, 7, 'text', 'plain/text', 1, 31200),
	(16, 'file', 8, 8, 'text', 'plain/text', 3, 31200),
	(17, 'file', 8, 9, 'text', 'plain/text', 7, 31200),
	(18, 'file', 8, 10, 'text', 'plain/text', 2, 31200),
	(19, 'file', 8, 11, 'text', 'plain/text', 7, 31200);
/*!40000 ALTER TABLE `files` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
