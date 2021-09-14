-- --------------------------------------------------------
-- Host:                         127.0.0.1
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

-- Dumping database structure for extensions-market-test
CREATE DATABASE IF NOT EXISTS `extensions-market-test` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `extensions-market-test`;

-- Dumping structure for table extensions-market-test.users
DELETE FROM `users`;
ALTER TABLE `users` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `country` varchar(255) DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `profile_image` bigint(20) DEFAULT NULL,
  `is_enabled` bigint(20) DEFAULT 1,
  `is_active` bigint(20) DEFAULT 0,
  `rating` DOUBLE DEFAULT 0,
  `extensions_rated` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `FKc7w5h6b6d74bu9o1yulvrbh6c` (`profile_image`),
  CONSTRAINT `FKc7w5h6b6d74bu9o1yulvrbh6c` FOREIGN KEY (`profile_image`) REFERENCES `files` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

-- Dumping data for table extensions-market-test.users: ~3 rows (approximately)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `country`, `info`, `password`, `role`, `username`, `email`, `profile_image`, `is_enabled`, `is_active`, `rating`, `extensions_rated`) VALUES
	(1, 'Bulgaria', 'info', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 'ROLE_ADMIN', 'adminUser', 'adminUser@gmail.com', NULL, true, true, 4.166666666666667, 3),
	(2, 'Bulgaria', 'info', '$2a$04$MRJSj9OWmmaWeDLPIHxU6.en5D8n10XTpFvgPQY5g.r61z0SFRkJW', 'ROLE_ADMIN', 'testUser', 'testUser@gmail.com', NULL, true, true, 4, 1),
	(3, 'Bulgaria', 'info', '$2a$04$JrrsioMxE3HE7bJ/sXLWD.0Ty8iJB1W4zgxumoFmn9rWl5a0vATa6', 'ROLE_USER', 'testUser1', 'testUser1@gmail.com', NULL, true, true, 4.5, 1),
	(4, 'Spain', 'info', '$2a$04$CMuyMiF6Wo5a4lbSdA68X.pj7jzYD6OPtv2KMLm.jl.B61waR/e9W', 'ROLE_USER', 'user1', 'user1@gmail.com', NULL, true, true, 4, 1),
	(5, 'Italy', 'info', '$2a$04$CMuyMiF6Wo5a4lbSdA68X.pj7jzYD6OPtv2KMLm.jl.B61waR/e9W', 'ROLE_USER', 'firstTest', 'firstTest@gmail.com', NULL, true, true, 4.75, 2),
	(6, 'Spain', 'info', '$2a$04$CMuyMiF6Wo5a4lbSdA68X.pj7jzYD6OPtv2KMLm.jl.B61waR/e9W', 'ROLE_USER', 'secondTest', 'secondTest@gmail.com', NULL, true, true, 0, 0),
	(7, 'Italy', 'info', '$2a$04$CMuyMiF6Wo5a4lbSdA68X.pj7jzYD6OPtv2KMLm.jl.B61waR/e9W', 'ROLE_USER', 'testThird', 'testThird@gmail.com', NULL, true, true, 0, 0),
	(8, 'Italy', 'info', '$2a$04$CMuyMiF6Wo5a4lbSdA68X.pj7jzYD6OPtv2KMLm.jl.B61waR/e9W', 'ROLE_USER', 'testForth', 'testForth@gmail.com', NULL, true, false, 0, 0),
	(9, 'Spain', 'info', '$2a$04$CMuyMiF6Wo5a4lbSdA68X.pj7jzYD6OPtv2KMLm.jl.B61waR/e9W', 'ROLE_USER', 'testFifth', 'testFifth@gmail.com', NULL, true, true, 0, 0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
