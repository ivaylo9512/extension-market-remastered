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


-- Dumping database structure for tick42-quicksilver4
CREATE DATABASE IF NOT EXISTS `tick42-quicksilver4` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `tick42-quicksilver4`;

-- Dumping structure for table tick42-quicksilver4.extensions
CREATE TABLE IF NOT EXISTS `extensions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `times_rated` int(11) NOT NULL DEFAULT 0,
  `rating` double NOT NULL DEFAULT 0,
  `name` char(255) NOT NULL,
  `version` varchar(50) NOT NULL,
  `times_downloaded` int(11) DEFAULT 0,
  `owner` int(11) NOT NULL,
  `github_id` int(11) DEFAULT NULL,
  `file_id` int(11) DEFAULT NULL,
  `image_id` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `pending` tinyint(4) NOT NULL DEFAULT 1,
  `featured` tinyint(4) NOT NULL DEFAULT 0,
  `upload_date` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `FK_extensions_users` (`owner`),
  KEY `FK_extensions_files` (`file_id`),
  KEY `FK_extensions_files_2` (`image_id`),
  KEY `FK_extensions_github` (`github_id`),
  CONSTRAINT `FK_extensions_files` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_extensions_files_2` FOREIGN KEY (`image_id`) REFERENCES `files` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_extensions_github` FOREIGN KEY (`github_id`) REFERENCES `github` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_extensions_users` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=464 DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.extensions: ~25 rows (approximately)
/*!40000 ALTER TABLE `extensions` DISABLE KEYS */;
INSERT INTO `extensions` (`id`, `times_rated`, `rating`, `name`, `version`, `times_downloaded`, `owner`, `github_id`, `file_id`, `image_id`, `description`, `pending`, `featured`, `upload_date`) VALUES
	(381, 1, 5, 'Sonar Cloud', '1', 4, 19, 151, 63, 64, 'SonarCloud is the leading product for Continuous Code Quality online, totally free for open-source projects. It supports all major programming languages, including C#, VB .Net, JavaScript, TypeScript, C/C++ and many more. If your code is closed source, SonarCloud also offers a paid plan to run private analyses.<br /><br />This VSTS extension provides build tasks that you can add in your build definition. All branches and pull-requests are automatically analyzed, allowing you to discover early any bug or vulnerability in the code. A widget is also available to track the health of the overall application.<br /><br />To get started in a few minutes, you can follow this dedicated Microsoft Lab.', 0, 0, '2018-09-09 22:32:46'),
	(382, 1, 5, 'Package Management', '3.32', 2, 1, 152, 65, 66, 'Move your packages to the cloud<br />You no longer need to manage legacy on-prem file shares or host private package servers. We’ll host, index, and manage your packages seamlessly in VSTS/TFS right alongside your source code, builds, and releases.<br />Best-in-class NuGet server<br />Create scalable .NET apps, frameworks, and microservices using NuGet packages. Stay on the forefront of .NET and .NET Core development with support for the latest NuGet updates. Publish symbols for your packages to the Package Management symbol server to enable easy debugging.<br />Private npm registry<br />Create and share private npm packages with a hosted npm registry. Proxy and cache packages from npmjs.com so you can make private packages without using scopes.<br />Host your Maven artifacts<br />Create and share Maven artifacts with the rest of your team. Use Maven or Gradle to build your code and let us manage your artifacts.<br />Deliver packages in every build for package continuous integration<br />Pack and publish packages to Package Management and to NuGet.org and npmjs.com. Automatically version your packages alongside your builds. Use release views to communicate package quality.<br />Easily debug with symbols<br />Publish the symbols created by your build to the Symbol Server in Package Management so your team can easily debug as they develop.', 0, 0, '2018-09-09 22:40:34'),
	(383, 2, 4, 'Octopus Deploy In', '2.4', 4, 1, 153, 67, 68, 'Tasks and Widgets<br />This extension adds the following tasks:<br />Use a specific octo version<br />Package Application<br />Push Packages to Octopus<br />Create Octopus Release<br />Deploy Octopus Release<br />Promote Octopus Release<br />And the following widget:', 0, 1, '2018-09-09 22:51:50'),
	(384, 1, 4, 'Build with devenv edit', '0.0.001', 0, 20, 154, 69, 70, 'The different parameters of the task are explained below:<br />Project: Solution or project that you intend to build.<br />Configuration: Build configuration. Eg. \'Release\'<br />Platform: Build platform; only applied when a build configuration is specified. Leave blank for solution/project default.<br />Visual Studio Version": A specific version of Visual Studio that should be used for this build (in case of multiple versions present on the build server).<br />Deploy: Builds the solution, along with files necessary for deployment, according to the solutions configuration. The specified project must be a deployment project. If the specified project is not a deployment project, when the project that has been built is passed to be deployed, it fails with an error.<br />Clean: Deletes any files created by the build command, without affecting source files.', 0, 0, '2018-09-09 22:56:27'),
	(385, 2, 4.5, 'JFrog Artifactory', '1.2a', 1, 21, 155, 71, 72, 'Download generic build dependencies from Artifactory<br />The Artifactory Generic Download task supports downloading your build dependencies from Artifactory to the build agent. The task triggers the JFrog CLI to perform the download. The downloaded dependencies are defined using File Specs and can be also configured to capture the build-info. It will store the downloaded files as dependencies in the build-info which can later be published to Artifactory using the Artifactory Publish Build-Info task.', 0, 1, '2018-09-09 22:59:06'),
	(386, 2, 4.5, 'Ensure Tests', '2.3aa1', 0, 22, 156, 73, 74, 'This extension provides a build task that allows you to quickly fail a build if it didn\'t execute any tests. This task uses "Server Phase". Be sure to set the dependency of this phase to the phases that run the tests.', 0, 0, '2018-09-09 23:02:57'),
	(387, 1, 4, 'Linked Wiki pages', '1', 1, 23, 157, 75, 76, 'Link Work Items to Wiki Pages<br />This extension will add a custom UI control for all Work Item forms. With this UI control you will be able to see all the Wiki Pages that were attached to current Work Item.<br />How to install<br />After you install this extension from Marketplace it will automatically add custom UI control to all Work Item forms, no further configuration required.<br />How to use?', 0, 0, '2018-09-09 23:04:26'),
	(388, 2, 4.5, 'HockeyApp', '0.000001', 0, 24, 158, 77, 78, 'Accelerate your Apps with Mobile DevOps<br /><br />Take advantage of HockeyApp and bring a full Mobile DevOps flow to your mobile app development. Through this extension, HockeyApp integrates with Visual Studio Team Services or Team Foundation Server to streamline development, management, monitoring, and delivery of your mobile apps.<br /><br />Distribution – Upload your apps for beta testing and seamless in-app updates<br /><br />Crash Reports – Get symbolicated crash reports while testing and in production<br /><br />Feedback – Allow your users to create a dialog with you directly from within the app<br /><br />User Metrics – See how users interact with your app and plan your investments based on real data<br /><br />Managing Apps – Create teams to manage access, integrate with Azure Active Directory, and quickly on-board your team<br /><br />Getting Started with HockeyApp<br /><br />Don\'t have a HockeyApp account? It\'s easy to get started. You can use your Microsoft Account, social media account or just an email address to setup your HockeyApp account. The free plan comes with two apps, one owner, and no data restrictions. If you need more apps or owners you can upgrade to one of our business plans to take advantage of more apps and more owners. Each app allows you to target a specific platform. You can keep an unlimited number of versions of each app in HockeyApp with no data limits on crashes, feedback or user metrics. Only owners can create apps, but once an app is created developers or your continuous integration / continuous deployment process can upload new versions of the app, making updates simple for your organization. Get started with HockeyApp.', 0, 0, '2018-09-09 23:07:45'),
	(389, 1, 5, 'IIS Web App Deployment Using WinRM', '213a', 1, 24, 159, 79, 80, 'IIS Web App Deployment Using WinRM<br /><br />Using Windows Remote Management (WinRM), connect to the host machine(s) where IIS or SQL Server is installed, and manage the Web application or deploy the SQL Server Database as described below:<br /><br />Create a new website or update an existing website using AppCmd.exe.<br /><br />Create a new application pool or update an existing application pool using AppCmd.exe.<br /><br />Deploy a Web Application to the IIS Server using Web Deploy.<br /><br />Deploy a SQL Server Database using DACPAC and SqlPackage.exe.<br /><br />To easily setup WinRM on the host machines, follow the directions for the domain-joined machines or the workgroup machines.<br /><br />The Visual Studio Team Services accounts that are using the preview tasks wiz. IIS Web Application Deployment or SQL Server Database Deployment, should move to this extension. All future enhancements to the IIS Web App Deployment task or to the SQL Server Database Deployment task will be provided in this extension.', 0, 0, '2018-09-09 23:09:01'),
	(390, 1, 4, 'VSTS Open in Excel', '1', 4, 25, 160, 81, 82, 'Open in Excel<br /><br />This extension requires Microsoft Excel and one of the following clients to be installed:<br /><br />Visual Studio 2017 or later<br /><br />Team Foundation Server Office® Integration 2017 or later<br /><br />Use this extension for bulk editing work items, or to leverage Excel tools to analyze and visualize a large number of work items. Work items that are opened in Excel can be edited and published back to Visual Studio Team Services with a single click. Once you are ready to publish your changes, simply hit "Publish" from Excel to sync your changes back to VSTS. Learn more about Office integration', 0, 0, '2018-09-09 23:19:13'),
	(391, 1, 4, 'Agile Poker - estimation toolkit for VSTS', '1', 0, 25, 161, 83, 84, 'Agile Poker is a versatile toolkit for estimating your product backlog to get it ready for grooming and planning. It is heavily inspired by Planning Poker® - but not limited to - and derives the best practices from industry standard estimation techniques.<br /><br />Getting started<br /><br />Start estimation by clicking "cards" extension icon in the top-right menu above the Board', 0, 0, '2018-09-09 23:30:42'),
	(392, 2, 4.5, 'Slack Integration', 'a', 0, 27, 162, 85, 86, 'Slack is a popular team collaboration service that helps teams be more productive by keeping all communications in one place and easily searchable from virtually anywhere. All your messages, your files, and everything from Twitter, Dropbox, Google Docs, Visual Studio Team Services, and more all together. Slack also has fully native apps for iOS and Android to give you the full functionality of Slack wherever you go.', 0, 0, '2018-09-09 23:33:50'),
	(393, 2, 4, 'wxml', 'as', 0, 29, 163, 87, 88, 'Visual Studio Live Share enables you to collaboratively edit and debug with others in real time, regardless what programming languages you\'re using or app types you\'re building. It allows you to instantly (and securely) share your current project, and then as needed, share debugging sessions, terminal instances, localhost web apps, voice calls, and more! Developers that join your sessions recieve all of their editor context from your environment (e.g. language services, debugging), which ensures they can start productively collaborating immediately, without needing to clone any repos or install any SDKs.', 0, 0, '2018-09-09 23:39:43'),
	(394, 2, 4.5, 'Python Extension Pack', '213', 0, 29, 164, 89, 90, 'Python - Linting, Debugging (multi-threaded, remote), Intellisense, code formatting, refactoring, unit tests, snippets, Data Science (with Jupyter), PySpark and more.<br /><br />Jupyter - Data Science with Jupyter on Visual Studio Code.<br /><br />MagicPython - Syntax highlighter for cutting edge Python.<br /><br />Jinja - Jinja template language support for Visual Studio Code.<br /><br />Django - Beautiful syntax and scoped snippets for perfectionists with deadlines.<br /><br />Visual Studio IntelliCode - Provides AI-assisted productivity features for Python developers in Visual Studio Code with insights based on understanding your code combined with machine learning..', 0, 0, '2018-09-09 23:40:33'),
	(395, 2, 4.5, 'Debugger for Chrome', '1.992.2', 0, 29, 165, 91, 131, 'A VS Code extension to debug your JavaScript code in the Google Chrome browser, or other targets that support the Chrome DevTools Protocol.<br /><br />', 0, 0, '2018-09-09 23:41:43'),
	(396, 2, 4.5, 'C/C++', '2.3++', 0, 29, 166, 92, 93, 'You can find more detailed information about C/C++ support for Visual Studio Code at our GitHub page and our VS Code documentation page.', 0, 0, '2018-09-09 23:42:44'),
	(397, 2, 4, 'reason-vscode', 'ver1', 0, 29, 167, 94, 95, 'Show a file\'s dependencies at the top<br /><br />Show what values are used from an open<br /><br />Per-value type codelens (off by default)', 0, 0, '2018-09-09 23:44:02'),
	(398, 2, 4.5, 'Kirikiri Adventure Game (KAG) Script', '12.2.334', 0, 30, 168, 96, 97, 'This extension provides rich KAG3 Script language support for Visual Studio Code. It\'s still in progress ( GitHub ), please expect frequent updates with breaking changes before 1.0. If you are interested in this project, feel free to<br /><br />File GitHub issues anytime you ran into unexpected situations/bugs.<br /><br />Fork our project, hack it around and send us PRs!', 0, 0, '2018-09-09 23:46:22'),
	(399, 2, 4, 'GML Support', '0.01', 4, 30, 169, 98, 99, 'I am going to prepare for the college entrance examination, I will not actively update the extension. if you find a bug, please report an issue, I will fix (If I have free time).', 0, 0, '2018-09-09 23:47:08'),
	(400, 2, 4, 'Daedalus', '12.444', 0, 30, 170, 100, 101, 'This is Visual Studio Code extension providing support of Daedalus language used for gameplay scripting in games on ZenGin engine (Gothic I, II & II:NoTR). The project was developed primarily for SoulFire.', 0, 0, '2018-09-09 23:48:09'),
	(401, 2, 4, 'Paradox Syntax', '1', 7, 30, 171, 102, 103, 'Paradox Syntax<br /><br />This extension is intended to help modders creating mods for Paradox Interactive games by providing basic yet necessary conveniences.<br /><br />Current Features<br /><br />Syntax support<br /><br />Custom theme<br /><br />Snippets', 0, 0, '2018-09-09 23:49:00'),
	(402, 2, 4.5, 'Pong', '23', 1, 31, 172, 104, 105, 'Pong<br /><br />Features<br /><br />Play Pong-like Game', 0, 0, '2018-09-09 23:52:40'),
	(403, 2, 3.5, 'Try\'s Icon Pack', '1', 2, 31, 173, 106, 107, 'The Material Icon Theme provides lots of icons based on Material Design for Visual Studio Code.<br /><br />', 0, 0, '2018-09-09 23:53:20'),
	(404, 2, 4, 'ScalaSnippets', '1.0a', 3, 31, 174, 108, 109, 'ScalaSnippets README<br /><br />Scala & SBT snippets for the Visual Studio Code', 0, 1, '2018-09-09 23:54:22'),
	(460, 2, 5, 'Restaurant App Vision', '2.0b', 0, 36, 231, NULL, 130, 'Restaurant app is our new way to', 0, 1, '2019-04-02 04:20:39');
/*!40000 ALTER TABLE `extensions` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.extension_tags
CREATE TABLE IF NOT EXISTS `extension_tags` (
  `tag` char(50) NOT NULL,
  `extension_id` int(11) NOT NULL,
  UNIQUE KEY `tag_id_extension_id` (`tag`,`extension_id`),
  KEY `FK_extension_tags_extensions` (`extension_id`),
  CONSTRAINT `FK_extension_tags_extensions` FOREIGN KEY (`extension_id`) REFERENCES `extensions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_extension_tags_tags` FOREIGN KEY (`tag`) REFERENCES `tags` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.extension_tags: ~8 rows (approximately)
/*!40000 ALTER TABLE `extension_tags` DISABLE KEYS */;
INSERT INTO `extension_tags` (`tag`, `extension_id`) VALUES
	('2.0', 460),
	('app', 460),
	('auto', 384),
	('auto', 399),
	('build', 396),
	('c', 384),
	('restaurant', 460),
	('vision', 460);
/*!40000 ALTER TABLE `extension_tags` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.files
CREATE TABLE IF NOT EXISTS `files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `location` varchar(512) NOT NULL,
  `type` varchar(512) NOT NULL,
  `size` double NOT NULL,
  `name` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=132 DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.files: ~69 rows (approximately)
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
INSERT INTO `files` (`id`, `location`, `type`, `size`, `name`) VALUES
	(63, 'http://localhost:8090/api/download/381_file.txt', 'file/plain', 19270, '381_file.txt'),
	(64, 'http://localhost:8090/api/download/381_image.png', 'image/png', 11451, '381_image.png'),
	(65, 'http://localhost:8090/api/download/382_file.txt', 'file/plain', 4945, '382_file.txt'),
	(66, 'http://localhost:8090/api/download/382_image.png', 'image/png', 5409, '382_image.png'),
	(67, 'http://localhost:8090/api/download/383_file.txt', 'text/plain', 15, '383_file.txt'),
	(68, 'http://localhost:8090/api/download/383_image.png', 'image/png', 10920, '383_image.png'),
	(69, 'http://localhost:8090/api/download/384_file.txt', 'text/plain', 15, '384_file.txt'),
	(70, 'http://localhost:8090/api/download/384_image.png', 'image/png', 7300, '384_image.png'),
	(71, 'http://localhost:8090/api/download/385_file.txt', 'text/plain', 15, '385_file.txt'),
	(72, 'http://localhost:8090/api/download/385_image.png', 'image/png', 5519, '385_image.png'),
	(73, 'http://localhost:8090/api/download/386_file.txt', 'text/plain', 15, '386_file.txt'),
	(74, 'http://localhost:8090/api/download/386_image.png', 'image/png', 4830, '386_image.png'),
	(75, 'http://localhost:8090/api/download/387_file.txt', 'text/plain', 15, '387_file.txt'),
	(76, 'http://localhost:8090/api/download/387_image.png', 'image/png', 7264, '387_image.png'),
	(77, 'http://localhost:8090/api/download/388_file.txt', 'text/plain', 15, '388_file.txt'),
	(78, 'http://localhost:8090/api/download/388_image.png', 'image/png', 10749, '388_image.png'),
	(79, 'http://localhost:8090/api/download/389_file.txt', 'text/plain', 15, '389_file.txt'),
	(80, 'http://localhost:8090/api/download/389_image.png', 'image/png', 12328, '389_image.png'),
	(81, 'http://localhost:8090/api/download/390_file.txt', 'text/plain', 15, '390_file.txt'),
	(82, 'http://localhost:8090/api/download/390_image.png', 'image/png', 4945, '390_image.png'),
	(83, 'http://localhost:8090/api/download/391_file.txt', 'text/plain', 15, '391_file.txt'),
	(84, 'http://localhost:8090/api/download/391_image.png', 'image/png', 15412, '391_image.png'),
	(85, 'http://localhost:8090/api/download/392_file.txt', 'text/plain', 15, '392_file.txt'),
	(86, 'http://localhost:8090/api/download/392_image.png', 'image/png', 5581, '392_image.png'),
	(87, 'http://localhost:8090/api/download/393_file.txt', 'text/plain', 15, '393_file.txt'),
	(88, 'http://localhost:8090/api/download/393_image.png', 'image/png', 5310, '393_image.png'),
	(89, 'http://localhost:8090/api/download/394_file.txt', 'text/plain', 15, '394_file.txt'),
	(90, 'http://localhost:8090/api/download/394_image.png', 'image/png', 11936, '394_image.png'),
	(91, 'http://localhost:8090/api/download/395_file.txt', 'text/plain', 15, '395_file.txt'),
	(92, 'http://localhost:8090/api/download/396_file.txt', 'text/plain', 15, '396_file.txt'),
	(93, 'http://localhost:8090/api/download/396_image.png', 'image/png', 6996, '396_image.png'),
	(94, 'http://localhost:8090/api/download/397_file.txt', 'text/plain', 15, '397_file.txt'),
	(95, 'http://localhost:8090/api/download/397_image.png', 'image/png', 16195, '397_image.png'),
	(96, 'http://localhost:8090/api/download/398_file.txt', 'text/plain', 15, '398_file.txt'),
	(97, 'http://localhost:8090/api/download/398_image.png', 'image/png', 13211, '398_image.png'),
	(98, 'http://localhost:8090/api/download/399_file.txt', 'text/plain', 15, '399_file.txt'),
	(99, 'http://localhost:8090/api/download/399_image.png', 'image/png', 2080, '399_image.png'),
	(100, 'http://localhost:8090/api/download/400_file.txt', 'text/plain', 15, '400_file.txt'),
	(101, 'http://localhost:8090/api/download/400_image.png', 'image/png', 5992, '400_image.png'),
	(102, 'http://localhost:8090/api/download/401_file.txt', 'text/plain', 15, '401_file.txt'),
	(103, 'http://localhost:8090/api/download/401_image.png', 'image/png', 9194, '401_image.png'),
	(104, 'http://localhost:8090/api/download/402_file.txt', 'text/plain', 15, '402_file.txt'),
	(105, 'http://localhost:8090/api/download/402_image.png', 'image/png', 31137, '402_image.png'),
	(106, 'http://localhost:8090/api/download/403_file.txt', 'text/plain', 15, '403_file.txt'),
	(107, 'http://localhost:8090/api/download/403_image.png', 'image/png', 23578, '403_image.png'),
	(108, 'http://localhost:8090/api/download/404_file.txt', 'text/plain', 15, '404_file.txt'),
	(109, 'http://localhost:8090/api/download/404_image.png', 'image/png', 38137, '404_image.png'),
	(110, 'http://localhost:8090/api/download/429_image.png', 'image/png', 4868, '429_image.png'),
	(111, 'http://localhost:8090/api/download/430_image.png', 'image/png', 4868, '430_image.png'),
	(112, 'http://localhost:8090/api/download/431_image.jpg', 'image/jpeg', 826754, '431_image.jpg'),
	(113, 'http://localhost:8090/api/download/432_image.png', 'image/png', 4868, '432_image.png'),
	(114, 'http://localhost:8090/api/download/441_image.jpg', 'image/jpeg', 826754, '441_image.jpg'),
	(115, 'http://localhost:8090/api/download/443_image.jpg', 'image/jpeg', 275128, '443_image.jpg'),
	(116, 'http://localhost:8090/api/download/444_image.jpg', 'image/jpeg', 826754, '444_image.jpg'),
	(117, 'http://localhost:8090/api/download/446_image.jpg', 'image/jpeg', 826754, '446_image.jpg'),
	(118, 'http://localhost:8090/api/download/448_image.jpg', 'image/jpeg', 826754, '448_image.jpg'),
	(119, 'http://localhost:8090/api/download/451_image.jpg', 'image/jpeg', 826754, '451_image.jpg'),
	(120, 'http://localhost:8090/api/download/452_image.png', 'image/png', 4868, '452_image.png'),
	(121, 'http://localhost:8090/api/download/453_image.jpg', 'image/jpeg', 826754, '453_image.jpg'),
	(122, 'http://localhost:8090/api/download/454_image.jpg', 'image/jpeg', 275128, '454_image.jpg'),
	(123, 'http://localhost:8090/api/download/455_image.jpg', 'image/jpeg', 275128, '455_image.jpg'),
	(124, 'http://localhost:8090/api/download/456_image.jpg', 'image/jpeg', 275128, '456_image.jpg'),
	(125, 'http://localhost:8090/api/download/457_image.jpg', 'image/jpeg', 826754, '457_image.jpg'),
	(126, 'http://localhost:8090/api/download/457_file.txt', 'text/plain', 15, '457_file.txt'),
	(127, 'http://localhost:8090/api/download/457_image.png', 'image/png', 786, '457_image.png'),
	(128, 'http://localhost:8090/api/download/457_file.txt', 'text/plain', 16, '457_file.txt'),
	(129, 'http://localhost:8090/api/download/458_image.png', 'image/png', 4868, '458_image.png'),
	(130, 'http://localhost:8090/api/download/460_image.png', 'image/png', 22453, '460_image.png'),
	(131, 'http://localhost:8090/api/download/395_image.png', 'image/png', 6389, '395_image.png');
/*!40000 ALTER TABLE `files` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.github
CREATE TABLE IF NOT EXISTS `github` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `link` varchar(512) DEFAULT NULL,
  `user` varchar(512) DEFAULT NULL,
  `repo` varchar(512) DEFAULT NULL,
  `last_commit` datetime DEFAULT NULL,
  `open_issues` int(11) DEFAULT NULL,
  `pull_requests` int(11) DEFAULT NULL,
  `last_success` datetime DEFAULT NULL,
  `last_fail` datetime DEFAULT NULL,
  `fail_msg` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=236 DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.github: ~28 rows (approximately)
/*!40000 ALTER TABLE `github` DISABLE KEYS */;
INSERT INTO `github` (`id`, `link`, `user`, `repo`, `last_commit`, `open_issues`, `pull_requests`, `last_success`, `last_fail`, `fail_msg`) VALUES
	(150, 'https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended', 'ivaylo9512', 'Tick42-ExtensionRepository-Extended', '2019-04-06 23:19:06', 0, 0, '2019-04-07 23:22:23', '2019-04-06 23:26:23', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended. Check URL.'),
	(151, 'https://github.com/SonarSource/sonar-scanner-vsts', 'SonarSource', 'sonar-scanner-vsts', '2019-02-26 18:38:26', 0, 5, '2019-04-07 23:22:26', '2019-04-06 23:26:23', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/SonarSource/sonar-scanner-vsts. Check URL.'),
	(152, 'https://github.com/uzh-rpg/rpg_dvs_ros', 'uzh-rpg', 'rpg_dvs_ros', '2019-03-20 09:53:41', 19, 3, '2019-04-07 23:22:30', '2019-04-06 23:26:24', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/uzh-rpg/rpg_dvs_ros. Check URL.'),
	(153, 'https://github.com/IvanGrigorov/RPG-Game-Challenge', 'IvanGrigorov', 'RPG-Game-Challenge', '2015-05-27 18:04:40', 0, 0, '2019-04-07 23:22:31', '2019-04-06 23:26:24', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/IvanGrigorov/RPG-Game-Challenge. Check URL.'),
	(154, 'https://github.com/mmajcica/DevEnvBuild', 'mmajcica', 'DevEnvBuild', '2018-05-14 17:05:56', 2, 0, '2019-04-07 23:22:32', '2019-04-06 23:26:24', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/mmajcica/DevEnvBuild. Check URL.'),
	(155, 'https://github.com/GameGrind/Simple-RPG-in-Unity', 'GameGrind', 'Simple-RPG-in-Unity', '2018-03-25 06:12:06', 0, 0, '2019-04-07 23:22:33', '2019-04-06 23:26:24', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/GameGrind/Simple-RPG-in-Unity. Check URL.'),
	(156, 'https://github.com/jessehouwing/vsts-ensure-tests', 'jessehouwing', 'vsts-ensure-tests', '2018-12-03 17:13:43', 3, 0, '2019-04-07 23:22:34', '2019-04-06 23:26:25', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/jessehouwing/vsts-ensure-tests. Check URL.'),
	(157, 'https://github.com/TFSExt/Forms', 'TFSExt', 'Forms', '2018-07-19 16:59:18', 8, 0, '2019-04-07 23:22:35', '2019-04-06 23:26:25', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/TFSExt/Forms. Check URL.'),
	(158, 'https://github.com/swordmaster2k/rpgwizard', 'swordmaster2k', 'rpgwizard', '2019-03-29 23:28:25', 13, 0, '2019-04-07 23:22:37', '2019-04-06 23:26:25', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/swordmaster2k/rpgwizard. Check URL.'),
	(159, 'https://github.com/topics/roleplaying-game', 'topics', 'roleplaying-game', NULL, 0, 0, NULL, '2019-04-07 23:22:37', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/topics/roleplaying-game. Check URL.'),
	(160, 'https://github.com/juliangarnier/3D-Hartwig-chess-set', 'juliangarnier', '3D-Hartwig-chess-set', '2012-12-13 23:25:54', 4, 1, '2019-04-07 23:22:38', '2019-04-06 23:26:26', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/juliangarnier/3D-Hartwig-chess-set. Check URL.'),
	(161, 'https://github.com/OCA/vertical-medical', 'OCA', 'vertical-medical', '2019-04-04 03:48:43', 14, 0, '2019-04-07 23:22:47', '2019-04-06 23:26:26', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/OCA/vertical-medical. Check URL.'),
	(162, 'https://github.com/alexander-rakhlin/ICIAR2018', 'alexander-rakhlin', 'ICIAR2018', '2019-01-20 14:56:21', 1, 0, '2019-04-07 23:22:48', '2019-04-06 23:26:26', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/alexander-rakhlin/ICIAR2018. Check URL.'),
	(163, 'https://github.com/MicrosoftDocs/live-share', 'MicrosoftDocs', 'live-share', '2019-04-05 02:21:46', 245, 0, '2019-04-07 23:23:06', '2019-04-06 23:26:26', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/MicrosoftDocs/live-share. Check URL.'),
	(164, 'https://github.com/DonJayamanne/python-extension-pack', 'DonJayamanne', 'python-extension-pack', '2019-02-22 18:56:23', 6, 1, '2019-04-07 23:23:07', '2019-04-06 23:26:26', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/DonJayamanne/python-extension-pack. Check URL.'),
	(165, 'https://github.com/Microsoft/vscode-chrome-debug', 'Microsoft', 'vscode-chrome-debug', '2019-04-05 18:18:59', 50, 3, '2019-04-07 23:23:19', '2019-04-06 23:26:26', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/Microsoft/vscode-chrome-debug. Check URL.'),
	(166, 'https://github.com/Microsoft/vscode-cpptools', 'Microsoft', 'vscode-cpptools', '2019-04-05 00:11:08', 920, 5, '2019-04-07 23:23:48', '2019-04-06 23:26:27', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/Microsoft/vscode-cpptools. Check URL.'),
	(167, 'https://github.com/jaredly/reason-language-server', 'jaredly', 'reason-language-server', '2019-04-07 07:48:21', 66, 3, '2019-04-07 23:23:56', '2019-04-06 23:26:27', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/jaredly/reason-language-server. Check URL.'),
	(168, 'https://github.com/happiness9721/Kirikiri-Adventure-Game-KAG-Script', 'happiness9721', 'Kirikiri-Adventure-Game-KAG-Script', '2017-05-21 08:55:39', 0, 0, '2019-04-07 23:23:57', '2019-04-06 23:26:27', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/happiness9721/Kirikiri-Adventure-Game-KAG-Script. Check URL.'),
	(169, 'https://github.com/gml-support/gml-support', 'gml-support', 'gml-support', '2018-09-01 15:56:50', 1, 0, '2019-04-07 23:23:59', '2019-04-06 23:26:27', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/gml-support/gml-support. Check URL.'),
	(170, 'https://github.com/Szmyk/daedalus-vscode', 'Szmyk', 'daedalus-vscode', '2018-07-29 00:09:29', 0, 0, '2019-04-07 23:24:00', '2019-04-06 23:26:27', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/Szmyk/daedalus-vscode. Check URL.'),
	(171, 'https://github.com/gml-support/gml-tools-langserver', 'gml-support', 'gml-tools-langserver', '2018-08-30 06:27:31', 0, 0, '2019-04-07 23:24:02', '2019-04-06 23:26:28', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/gml-support/gml-tools-langserver. Check URL.'),
	(172, 'https://github.com/happiness9721/line-bot-sdk-swift', 'happiness9721', 'line-bot-sdk-swift', '2018-05-03 10:34:13', 3, 0, '2019-04-07 23:24:04', '2019-04-06 23:26:28', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/happiness9721/line-bot-sdk-swift. Check URL.'),
	(173, 'https://github.com/rafapaulin/vscode-material-icon-theme', 'rafapaulin', 'vscode-material-icon-theme', '2018-09-01 19:09:27', 0, 0, '2019-04-07 23:24:12', '2019-04-06 23:26:28', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/rafapaulin/vscode-material-icon-theme. Check URL.'),
	(174, 'https://github.com/jaredly/vim-debug', 'jaredly', 'vim-debug', '2016-08-13 06:56:30', 15, 1, '2019-04-07 23:24:13', '2019-04-06 23:26:28', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/jaredly/vim-debug. Check URL.'),
	(229, 'https://github.com/octocat/Hello-World', 'octocat', 'Hello-World', '2012-03-07 01:06:50', 152, 191, '2019-04-07 23:24:27', '2019-04-06 23:26:29', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/octocat/Hello-World. Check URL.'),
	(231, 'https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended', 'ivaylo9512', 'Tick42-ExtensionRepository-Extended', '2019-04-06 23:19:06', 0, 0, '2019-04-07 23:24:31', '2019-04-06 23:26:29', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended. Check URL.'),
	(235, 'https://github.com/Smytt/Tick42-ExtensionRepository', 'Smytt', 'Tick42-ExtensionRepository', '2018-09-10 12:42:40', 1, 0, '2019-04-07 23:24:34', '2019-04-06 23:26:29', 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/Smytt/Tick42-ExtensionRepository. Check URL.');
/*!40000 ALTER TABLE `github` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.ratings
CREATE TABLE IF NOT EXISTS `ratings` (
  `user` int(11) NOT NULL,
  `rating` tinyint(4) NOT NULL DEFAULT 0,
  `extension` int(11) NOT NULL,
  PRIMARY KEY (`user`,`extension`),
  KEY `extension` (`extension`),
  CONSTRAINT `FK1_extensions_extension_id` FOREIGN KEY (`extension`) REFERENCES `extensions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK2_users_user_id` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.ratings: ~43 rows (approximately)
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
INSERT INTO `ratings` (`user`, `rating`, `extension`) VALUES
	(1, 4, 383),
	(1, 4, 385),
	(1, 4, 386),
	(1, 5, 388),
	(1, 5, 392),
	(1, 3, 393),
	(1, 4, 394),
	(1, 5, 395),
	(1, 4, 396),
	(1, 5, 397),
	(1, 4, 398),
	(1, 5, 399),
	(1, 4, 400),
	(1, 3, 401),
	(1, 5, 402),
	(1, 4, 403),
	(1, 4, 404),
	(1, 5, 460),
	(36, 5, 381),
	(36, 5, 382),
	(36, 4, 383),
	(36, 4, 384),
	(36, 5, 385),
	(36, 5, 386),
	(36, 4, 387),
	(36, 4, 388),
	(36, 5, 389),
	(36, 4, 390),
	(36, 4, 391),
	(36, 4, 392),
	(36, 5, 393),
	(36, 5, 394),
	(36, 4, 395),
	(36, 5, 396),
	(36, 3, 397),
	(36, 5, 398),
	(36, 3, 399),
	(36, 4, 400),
	(36, 5, 401),
	(36, 4, 402),
	(36, 3, 403),
	(36, 4, 404),
	(36, 5, 460);
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.settings
CREATE TABLE IF NOT EXISTS `settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rate` int(11) DEFAULT NULL,
  `wait` int(11) DEFAULT NULL,
  `git_token` varchar(512) DEFAULT NULL,
  `git_username` varchar(512) DEFAULT NULL,
  `user` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_settings_users` (`user`),
  CONSTRAINT `FK_settings_users` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.settings: ~1 rows (approximately)
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` (`id`, `rate`, `wait`, `git_token`, `git_username`, `user`) VALUES
	(1, 500000, 5000, 'null', 'null', 1);
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.tags
CREATE TABLE IF NOT EXISTS `tags` (
  `name` char(50) NOT NULL,
  PRIMARY KEY (`name`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.tags: ~72 rows (approximately)
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` (`name`) VALUES
	('2.0'),
	('app'),
	('auto'),
	('binary'),
	('bot'),
	('build'),
	('c'),
	('c++'),
	('chat'),
	('chess'),
	('chrome'),
	('code'),
	('das'),
	('dasda'),
	('dasdsasddadsa'),
	('devenv'),
	('dsada'),
	('dsadsa'),
	('fish'),
	('free'),
	('game'),
	('generic'),
	('github'),
	('gml'),
	('google'),
	('hockey'),
	('icons'),
	('iis'),
	('jaredly'),
	('julian'),
	('kag'),
	('keybindings'),
	('kirikiri'),
	('language'),
	('medical'),
	('microsoft'),
	('ms'),
	('note'),
	('nuget'),
	('octopus'),
	('pack'),
	('package'),
	('pages'),
	('paradox'),
	('ping'),
	('pong'),
	('python'),
	('reason'),
	('restaurant'),
	('rpg'),
	('scala'),
	('server'),
	('slack'),
	('sonar cloud'),
	('support'),
	('syntax'),
	('task'),
	('test'),
	('test task'),
	('tests'),
	('typescript'),
	('update'),
	('vi'),
	('vim'),
	('vision'),
	('vs code'),
	('vscode'),
	('vscodevim'),
	('vsiualstudio'),
	('web'),
	('wiki'),
	('xpirit');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;

-- Dumping structure for table tick42-quicksilver4.users
CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(68) NOT NULL,
  `enabled` tinyint(4) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role` varchar(50) DEFAULT 'user',
  `extensions_rated` double NOT NULL DEFAULT 0,
  `rating` double NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- Dumping data for table tick42-quicksilver4.users: ~15 rows (approximately)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`username`, `password`, `enabled`, `id`, `role`, `extensions_rated`, `rating`) VALUES
	('admin', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 1, 1, 'ROLE_ADMIN', 2, 4.5),
	('kiril1987', '$2a$04$ZoKhfJblgh0a1NfVtRdkvu0h.SIhdKnPq2iyhtxXwDQKe0bQeK/uu', 1, 19, 'ROLE_USER', 1, 5),
	('Robocop', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 1, 20, 'ROLE_USER', 1, 4),
	('Batman_batman', '$2a$04$ZNXjn5UZuDrGClbeAZaJ/.c37udduJ1ciVrZih6LeWwSQ/Qi2UdDS', 1, 21, 'ROLE_USER', 1, 4.5),
	('IvayloAleksandrov', '$2a$04$EWABZM1fVroR5pxbtyDaReopB8Lxa7qg4ZLP5blxmrY9N1oMssQai', 1, 22, 'ROLE_USER', 1, 4.5),
	('VeselinGeorgiev', '$2a$04$jIr.bgNqa5pUIiNr1M9PV.Ek02O/Qba781xrnGpPxjG3IXKG2mEaW', 1, 23, 'ROLE_USER', 1, 4),
	('KrasimirZahariev', '$2a$04$m2ApL7GXTAeZ1sRsBp1I6uuEVs.ukjmjRSQg2vFzJ3jWcNiRpqSRG', 1, 24, 'ROLE_USER', 2, 4.75),
	('MartinStoyanoff', '$2a$04$rU6oNY216IDjw3.kIy.AL.YRSPCl8JV3ovI7/v0Gd4xcxG1mTXS1a', 1, 25, 'ROLE_USER', 2, 4),
	('RosiBusarova', '$2a$04$Ow7zt6ArZM0didTXuZSyCe8t1v2pHDBnsD7WDkExwBieoVHCfOtBO', 1, 27, 'ROLE_USER', 1, 4.5),
	('LuchiaSavova', '$2a$04$6N7btYP9Y483qlVdz.ldQuWM.GTo6sSkb7WP5.NjhLUEiy4wD9Z6S', 1, 29, 'ROLE_USER', 5, 4.3),
	('MariaIskrova', '$2a$04$YibgvWtHYtX0FFa2HjILQODpP9crvDthnk3Ve88SFksBFvVnriDdi', 1, 30, 'ROLE_USER', 4, 4.125),
	('MariaGrigorova', '$2a$04$nl4ngkcqWvq2KKfB2fuCa.ZoGMcc1U3oSt3YLJTOB4P2KM/QHWy/S', 1, 31, 'ROLE_USER', 3, 4),
	('ivailo', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 1, 36, 'ROLE_ADMIN', 1, 5),
	('ivailo9512', '$2a$04$g81cEI380e1Qsk1Arr2OiuU6NCABymsnbXzP5WCkmXwk14OUaZ3pi', 1, 37, 'ROLE_USER', 0, 0),
	('ivailo95123', '$2a$04$6w6FIjuQUBeCyejL/3uOjeDuLehjai0Uez2jeUNBEOxB2rB/5fipa', 1, 38, 'ROLE_USER', 0, 0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
