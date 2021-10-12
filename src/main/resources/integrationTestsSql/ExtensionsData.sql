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

-- Dumping database structure for extensions-market-test
CREATE DATABASE IF NOT EXISTS `extensions-market-test` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `extensions-market-test`;

-- Dumping structure for table extensions-market-test.extensions
DELETE FROM `extensions`;
ALTER TABLE `extensions` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `extensions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `times_rated` int NOT NULL DEFAULT '0',
  `rating` double NOT NULL DEFAULT '0',
  `name` char(255) NOT NULL,
  `version` varchar(50) NOT NULL,
  `owner` bigint(20) NOT NULL,
  `github_id` bigint(20) DEFAULT NULL,
  `file_id` bigint(20) DEFAULT NULL,
  `image_id` bigint(20) DEFAULT NULL,
  `description` longtext NOT NULL,
  `pending` tinyint NOT NULL DEFAULT '1',
  `featured` tinyint NOT NULL DEFAULT '0',
  `upload_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cover_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_extensions_users` (`owner`),
  KEY `FK_extensions_files` (`file_id`),
  KEY `FK_extensions_files_2` (`image_id`),
  KEY `FK_extensions_github` (`github_id`),
  KEY `FK_cover_files` (`cover_id`),
  CONSTRAINT `FK_cover_files` FOREIGN KEY (`cover_id`) REFERENCES `files` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_extensions_files` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_extensions_files_2` FOREIGN KEY (`image_id`) REFERENCES `files` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_extensions_github` FOREIGN KEY (`github_id`) REFERENCES `github` (`id`),
  CONSTRAINT `FK_extensions_users` FOREIGN KEY (`owner`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market-test.extensions: ~24 rows (approximately)
/*!40000 ALTER TABLE `extensions` DISABLE KEYS */;
INSERT INTO `extensions` (`id`, `times_rated`, `rating`, `name`, `version`, `owner`, `github_id`, `file_id`, `image_id`, `description`, `pending`, `featured`, `upload_date`, `cover_id`) VALUES
    (1, 1, 5, 'Extension Market', '1', 1, 1, 10, 1, 'Extension market application.', 0, 1, '2021-02-01 22:32:46', 6),
    (2, 1, 5, 'Sonar Cloud', '1', 2, 2, 11, 2, 'SonarCloud is the leading product for Continuous Code Quality online, totally free for open-source projects.', 0, 0, '2021-01-01 22:32:46', NULL),
	(3, 1, 5, 'Package Management', '3.32', 3, 3, 12, 3, 'Move your packages to the cloud<br />You no longer need to manage legacy on-prem file shares or host private package servers.', 1, 0, '2021-03-01 22:40:34', 3),
	(4, 2, 4.5, 'Octopus Deploy In', '2.4', 2, 4, 13, 4, 'This extension adds the following tasks: Package Application, Push Packages to Octopus, Create Octopus Release, Deploy Octopus Release', 1, 0, '2020-10-02 22:51:50', NULL),
	(5, 1, 4, 'Build with devenv edit', '0.0.001', 1, 5, 14, 5, 'Build with devenv edit specific version of Visual Studio.', 0, 0, '2020-09-02 22:56:27', NULL),
	(6, 2, 4.5, 'JFrog Artifactory', '1.2a', 1, 6, 15, 6, 'Download generic build dependencies from Artifactory.', 0, 1, '2020-08-02 22:59:06', NULL),
	(7, 2, 4, 'Linked Wiki pages', '1', 4, 7, 16, 7, 'Link Work Items to Wiki.', 1, 0, '2019-07-02 23:04:26', NULL),
	(8, 2, 4.5, 'HockeyApp', '0.000001', 1, 3, 17, 8, 'Accelerate your Apps with Mobile DevOps Take advantage of HockeyApp and bring a full Mobile DevOps flow.', 0, 1, '2020-06-02 23:07:45', NULL),
	(9, 1, 5, 'IIS Web App Deployment Using WinRM', '213a', 3, 4, 18, NULL, 'IIS Web App Deployment Using WinRM.', 1, 0, '2020-05-02 23:09:01', NULL),
	(10, 2, 5, 'Restaurant App', '2.0b', 8, 5, 19, 10, 'Restaurant app is a modern way to create orders. ', 0, 1, '2020-04-02 04:20:39', NULL);
/*!40000 ALTER TABLE `extensions` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
