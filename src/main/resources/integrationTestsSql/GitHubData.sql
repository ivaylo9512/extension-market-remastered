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
CREATE DATABASE IF NOT EXISTS `extensions-market-test` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `extensions-market-test`;

-- Dumping structure for table extensions-market-test.github
DELETE FROM `github`;
ALTER TABLE `github` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `github` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(512) DEFAULT NULL,
  `repo` varchar(512) DEFAULT NULL,
  `last_commit` datetime DEFAULT NULL,
  `open_issues` int DEFAULT NULL,
  `pull_requests` int DEFAULT NULL,
  `last_success` datetime DEFAULT NULL,
  `last_fail` datetime DEFAULT NULL,
  `fail_message` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=398 DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market-test.github: ~25 rows (approximately)
/*!40000 ALTER TABLE `github` DISABLE KEYS */;
INSERT INTO `github` (`id`, `user`, `repo`, `last_commit`, `open_issues`, `pull_requests`, `last_success`, `last_fail`, `fail_message`) VALUES
	(1, 'ivaylo9512', 'extension-market-remastered', '2020-09-07 05:23:44', 0, 0, '2020-09-10 05:37:17', '2020-09-10 02:48:43', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended. Check URL.'),
	(2, 'SonarSource', 'sonar-scanner-vsts', '2021-09-06 12:55:20', 0, 2, '2021-09-10 05:37:20', '2021-09-10 02:48:43', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/SonarSource/sonar-scanner-vsts. Check URL.'),
	(3, 'IvanGrigorov', 'RPG-Game-Challenge', '2015-05-27 15:04:40', 0, 0, '2021-09-10 05:37:21', '2021-09-10 02:48:43', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/IvanGrigorov/RPG-Game-Challenge. Check URL.'),
	(4, 'mmajcica', 'DevEnvBuild', '2019-05-30 08:34:40', 7, 4, '2021-09-10 05:37:22', '2021-09-10 02:48:43', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/mmajcica/DevEnvBuild. Check URL.'),
	(5, 'GameGrind', 'Simple-RPG-in-Unity', '2019-05-26 04:56:08', 0, 0, '2021-09-10 05:37:23', '2021-09-10 02:48:43', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/GameGrind/Simple-RPG-in-Unity. Check URL.'),
	(6, 'TFSExt', 'Forms', '2019-07-20 19:54:22', 15, 1, '2021-09-10 05:37:24', '2021-09-10 02:48:43', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/TFSExt/Forms. Check URL.'),
	(7, 'topics', 'roleplaying-game', NULL, 0, 0, NULL, '2021-09-10 05:37:24', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/topics/roleplaying-game. Check URL.');
/*!40000 ALTER TABLE `github` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
