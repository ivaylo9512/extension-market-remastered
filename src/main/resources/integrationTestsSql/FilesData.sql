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

-- Dumping database structure for chat-app-test
CREATE DATABASE IF NOT EXISTS `chat-app-test` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `chat-app-test`;

-- Dumping structure for table chat-app-test.files
DELETE FROM `files`;
ALTER TABLE `files` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(50) NOT NULL DEFAULT '0',
  `owner` bigint(20) NOT NULL,
  `extension` tinytext NOT NULL,
  `type` varchar(50) NOT NULL DEFAULT '0',
  `size` double NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FK_files_users` (`owner`),
  CONSTRAINT `FK_files_users` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

-- Dumping data for table chat-app-test.files: ~8 rows (approximately)
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
INSERT INTO `files` (`id`, `resource_type`, `owner`, `extension`, `type`, `size`) VALUES
	(1, 'profileImage', 1, 'png', 'image/png', 43250),
	(2, 'profileImage', 2, 'png', 'image/png', 46000),
	(3, 'profileImage', 3, 'png', 'image/png', 32000),
	(4, 'profileImage', 4, 'png', 'image/png', 37000),
	(5, 'profileImage', 5, 'png', 'image/png', 50000),
	(6, 'profileImage', 6, 'png', 'image/png', 20000),
	(7, 'profileImage', 7, 'png', 'image/png', 31500),
	(8, 'profileImage', 8, 'png', 'image/png', 31200),
	(9, 'test', 3, 'png', 'image/png', 66800);
/*!40000 ALTER TABLE `files` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
