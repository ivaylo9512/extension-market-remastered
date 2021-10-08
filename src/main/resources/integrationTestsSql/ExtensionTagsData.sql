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

-- Dumping structure for table extensions-market-test.extension_tags
DELETE FROM `extension_tags`;
ALTER TABLE `extension_tags` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `extension_tags` (
  `tag` char(50) NOT NULL,
  `extension` bigint(20) NOT NULL,
  UNIQUE KEY `tag_id_extension` (`tag`,`extension`),
  KEY `FK_extension_tags_extensions` (`extension`),
  CONSTRAINT `FK_extension_tags_extensions` FOREIGN KEY (`extension`) REFERENCES `extensions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_extension_tags_tags` FOREIGN KEY (`tag`) REFERENCES `tags` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market-test.extension_tags: ~8 rows (approximately)
/*!40000 ALTER TABLE `extension_tags` DISABLE KEYS */;
INSERT INTO `extension_tags` (`tag`, `extension`) VALUES
	('auto', 1),
	('repo', 1),
	('c', 1),
	('app', 1),
    ('c', 2),
    ('repo', 2),
    ('auto', 3),
    ('repo', 3),
    ('app', 3),
    ('repo', 4),
    ('repo', 5),
    ('repo', 6),
    ('repo', 7),
    ('repo', 8),
    ('repo', 9),
	('repo', 10);
/*!40000 ALTER TABLE `extension_tags` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
