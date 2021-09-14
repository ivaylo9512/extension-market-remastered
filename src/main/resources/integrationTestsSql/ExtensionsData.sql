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

-- Dumping structure for table extensions-market-test.extensions
DELETE FROM `extensions`;
ALTER TABLE `extensions` AUTO_INCREMENT = 1;
CREATE TABLE IF NOT EXISTS `extensions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `times_rated` int NOT NULL DEFAULT '0',
  `rating` double NOT NULL DEFAULT '0',
  `name` char(255) NOT NULL,
  `version` varchar(50) NOT NULL,
  `owner` int NOT NULL,
  `github_id` int DEFAULT NULL,
  `file_id` int DEFAULT NULL,
  `image_id` int DEFAULT NULL,
  `description` longtext NOT NULL,
  `pending` tinyint NOT NULL DEFAULT '1',
  `featured` tinyint NOT NULL DEFAULT '0',
  `upload_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cover_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_extensions_users` (`owner`),
  KEY `FK_extensions_files` (`file_id`),
  KEY `FK_extensions_files_2` (`image_id`),
  KEY `FK_extensions_github` (`github_id`),
  KEY `FK_cover_files` (`cover_id`),
  CONSTRAINT `FK_cover_files` FOREIGN KEY (`cover_id`) REFERENCES `files` (`id`),
  CONSTRAINT `FK_extensions_files` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`),
  CONSTRAINT `FK_extensions_files_2` FOREIGN KEY (`image_id`) REFERENCES `files` (`id`),
  CONSTRAINT `FK_extensions_github` FOREIGN KEY (`github_id`) REFERENCES `github` (`id`),
  CONSTRAINT `FK_extensions_users` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- Dumping data for table extensions-market-test.extensions: ~24 rows (approximately)
/*!40000 ALTER TABLE `extensions` DISABLE KEYS */;
INSERT INTO `extensions` (`id`, `times_rated`, `rating`, `name`, `version`, `owner`, `github_id`, `file_id`, `image_id`, `description`, `pending`, `featured`, `upload_date`, `cover_id`) VALUES
    (1, 1, 5, 'Extension Market', '1', 1, 1, 10, 1, 'Extension market application.', 0, 0, '2019-09-09 22:32:46', NULL),
    (2, 1, 5, 'Sonar Cloud', '1', 2, 2, 11, 2, 'SonarCloud is the leading product for Continuous Code Quality online, totally free for open-source projects. It supports all major programming languages, including C#, VB .Net, JavaScript, TypeScript, C/C++ and many more. If your code is closed source, SonarCloud also offers a paid plan to run private analyses.', 0, 0, '2018-09-09 22:32:46', NULL),
	(3, 1, 5, 'Package Management', '3.32', 3, 3, 12, 3, 'Move your packages to the cloud<br />You no longer need to manage legacy on-prem file shares or host private package servers. We’ll host, index, and manage your packages seamlessly in VSTS/TFS right alongside your source code, builds, and releases.<br />Best-in-class NuGet server<br />Create scalable .NET apps, frameworks, and microservices using NuGet packages. Stay on the forefront of .NET and .NET Core development with support for the latest NuGet updates. Publish symbols for your packages to the Package Management symbol server to enable easy debugging.<br />Private npm registry<br />Create and share private npm packages with a hosted npm registry. Proxy and cache packages from npmjs.com so you can make private packages without using scopes.<br />Host your Maven artifacts<br />Create and share Maven artifacts with the rest of your team. Use Maven or Gradle to build your code and let us manage your artifacts.<br />Deliver packages in every build for package continuous integration<br />Pack and publish packages to Package Management and to NuGet.org and npmjs.com. Automatically version your packages alongside your builds. Use release views to communicate package quality.<br />Easily debug with symbols<br />Publish the symbols created by your build to the Symbol Server in Package Management so your team can easily debug as they develop.', 0, 0, '2018-09-09 22:40:34', NULL),
	(4, 2, 4.5, 'Octopus Deploy In', '2.4', 4, 4, 13, 4, 'This extension adds the following tasks: Package Application, Push Packages to Octopus, Create Octopus Release, Deploy Octopus Release', 0, 1, '2018-09-09 22:51:50', NULL),
	(5, 1, 4, 'Build with devenv edit', '0.0.001', 1, 5, 14, 6, 'The different parameters of the task are explained below:<br />Project: Solution or project that you intend to build.<br />Configuration: Build configuration. Eg. \'Release\'<br />Platform: Build platform; only applied when a build configuration is specified. Leave blank for solution/project default.<br />Visual Studio Version": A specific version of Visual Studio that should be used for this build (in case of multiple versions present on the build server).<br />Deploy: Builds the solution, along with files necessary for deployment, according to the solutions configuration. The specified project must be a deployment project. If the specified project is not a deployment project, when the project that has been built is passed to be deployed, it fails with an error.<br />Clean: Deletes any files created by the build command, without affecting source files.', 0, 0, '2018-09-09 22:56:27', NULL),
	(6, 2, 4.5, 'JFrog Artifactory', '1.2a', 5, 6, 15, 7, 'Download generic build dependencies from Artifactory<br />The Artifactory Generic Download task supports downloading your build dependencies from Artifactory to the build agent. The task triggers the JFrog CLI to perform the download. The downloaded dependencies are defined using File Specs and can be also configured to capture the build-info. It will store the downloaded files as dependencies in the build-info which can later be published to Artifactory using the Artifactory Publish Build-Info task.', 0, 1, '2018-09-09 22:59:06', NULL),
	(7, 2, 4, 'Linked Wiki pages', '1', 4, 7, 16, 8, 'Link Work Items to Wiki Pages<br />This extension will add a custom UI control for all Work Item forms. With this UI control you will be able to see all the Wiki Pages that were attached to current Work Item.<br />How to install<br />After you install this extension from Marketplace it will automatically add custom UI control to all Work Item forms, no further configuration required.<br />How to use?', 1, 0, '2018-09-09 23:04:26', NULL),
	(8, 2, 4.5, 'HockeyApp', '0.000001', 6, 3, 17, 1, 'Accelerate your Apps with Mobile DevOps<br /><br />Take advantage of HockeyApp and bring a full Mobile DevOps flow to your mobile app development. Through this extension, HockeyApp integrates with Visual Studio Team Services or Team Foundation Server to streamline development, management, monitoring, and delivery of your mobile apps.<br /><br />Distribution – Upload your apps for beta testing and seamless in-app updates<br /><br />Crash Reports – Get symbolicated crash reports while testing and in production<br /><br />Feedback – Allow your users to create a dialog with you directly from within the app<br /><br />User Metrics – See how users interact with your app and plan your investments based on real data<br /><br />Managing Apps – Create teams to manage access, integrate with Azure Active Directory, and quickly on-board your team<br /><br />Getting Started with HockeyApp<br /><br />Don\'t have a HockeyApp account? It\'s easy to get started. You can use your Microsoft Account, social media account or just an email address to setup your HockeyApp account. The free plan comes with two apps, one owner, and no data restrictions. If you need more apps or owners you can upgrade to one of our business plans to take advantage of more apps and more owners. Each app allows you to target a specific platform. You can keep an unlimited number of versions of each app in HockeyApp with no data limits on crashes, feedback or user metrics. Only owners can create apps, but once an app is created developers or your continuous integration / continuous deployment process can upload new versions of the app, making updates simple for your organization. Get started with HockeyApp.', 0, 0, '2018-09-09 23:07:45', NULL),
	(9, 1, 5, 'IIS Web App Deployment Using WinRM', '213a', 3, 4, 18, 2, 'IIS Web App Deployment Using WinRM<br /><br />Using Windows Remote Management (WinRM), connect to the host machine(s) where IIS or SQL Server is installed, and manage the Web application or deploy the SQL Server Database as described below:<br /><br />Create a new website or update an existing website using AppCmd.exe.<br /><br />Create a new application pool or update an existing application pool using AppCmd.exe.<br /><br />Deploy a Web Application to the IIS Server using Web Deploy.<br /><br />Deploy a SQL Server Database using DACPAC and SqlPackage.exe.<br /><br />To easily setup WinRM on the host machines, follow the directions for the domain-joined machines or the workgroup machines.<br /><br />The Visual Studio Team Services accounts that are using the preview tasks wiz. IIS Web Application Deployment or SQL Server Database Deployment, should move to this extension. All future enhancements to the IIS Web App Deployment task or to the SQL Server Database Deployment task will be provided in this extension.', 1, 0, '2018-09-09 23:09:01', NULL),
	(10, 2, 5, 'Restaurant App', '2.0b', 8, 5, 19, 3, 'Restaurant app is a modern way to create orders. It has different type of roles that can create orders and mark them as ready. Chat feature is added where every employe can write to each other.', 0, 1, '2019-04-02 04:20:39', NULL);
/*!40000 ALTER TABLE `extensions` ENABLE KEYS */;

SET FOREIGN_KEY_CHECKS = 1;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
