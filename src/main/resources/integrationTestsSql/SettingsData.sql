-- --------------------------------------------------------
-- Host:                         database-2.cd3qhxwxyvzj.eu-west-2.rds.amazonaws.com
-- Server version:               8.0.23 - Source distribution
-- Server OS:                    Linux
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

-- Dumping structure for table extensions-market-test.settings
DELETE FROM `settings`;
ALTER TABLE `settings` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `settings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rate` int DEFAULT NULL,
  `wait` int DEFAULT NULL,
  `git_token` varchar(512) DEFAULT NULL,
  `user` int DEFAULT NULL,
  `git_username` tinytext,
  PRIMARY KEY (`id`),
  KEY `FK_settings_users` (`user`),
  CONSTRAINT `FK_settings_users` FOREIGN KEY (`user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market-test.settings: ~2 rows (approximately)
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` (`id`, `rate`, `wait`, `git_token`, `user`, `git_username`) VALUES
	(1, 500000, 9000, 'ghp_aiw47lLie3m9VnlQRWI2', 1, 'ivaylo9512'),
	(3, 8000000, 6000, 'ghp_aiw47lLie3m9VnlQRWIPyOB', 2, 'ia9512'),
	(7, 9000000, 6000, 'ghp_aiw47lLie3m9VnlQRWIPyOB', 3, 'im98122'),
	(5, 10000000, 7000, 'ghp_aiw47lLie3m9VnlQRWIPyOB', 4, 'ib9212');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
