-- --------------------------------------------------------
-- Host:                         database-2.cdad4jowljyd.eu-central-1.rds.amazonaws.com
-- Server version:               8.0.23 - Source distribution
-- Server OS:                    Linux
-- HeidiSQL Version:             11.3.0.6295
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET FOREIGN_KEY_CHECKS = 0;

-- Dumping database structure for extensions-market-test
CREATE DATABASE IF NOT EXISTS `extensions-market-test` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `extensions-market-test`;

-- Dumping structure for table extensions-market-test.email_tokens
DELETE FROM `email_tokens`;
ALTER TABLE `email_tokens` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `email_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` text NOT NULL,
  `user_id` bigint(20) NOT NULL DEFAULT '0',
  `expiry_date` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_email_tokens_users` (`user_id`) USING BTREE,
  CONSTRAINT `FK_email_tokens_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market-test.email_tokens: ~0 rows (approximately)
/*!40000 ALTER TABLE `email_tokens` DISABLE KEYS */;
INSERT INTO `email_tokens` (`id`, `token`, `user_id`, `expiry_date`) VALUES
	(1, 'token', 3, '2021-10-06 01:58:54.000000'),
	(2, 'token1', 7, '9999-10-07 01:58:54.000000');
/*!40000 ALTER TABLE `email_tokens` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
