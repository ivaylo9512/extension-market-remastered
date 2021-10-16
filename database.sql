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


-- Dumping database structure for extensions-market
CREATE DATABASE IF NOT EXISTS `extensions-market` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `extensions-market`;

-- Dumping structure for table extensions-market.email_tokens
CREATE TABLE IF NOT EXISTS `email_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9j2pq9xa8fh246k3uruj9feec` (`user_id`),
  CONSTRAINT `FK9j2pq9xa8fh246k3uruj9feec` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.email_tokens: ~2 rows (approximately)
/*!40000 ALTER TABLE `email_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `email_tokens` ENABLE KEYS */;

-- Dumping structure for table extensions-market.extensions
CREATE TABLE IF NOT EXISTS `extensions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text,
  `featured` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pending` bit(1) NOT NULL,
  `rating` double NOT NULL,
  `times_rated` int DEFAULT NULL,
  `upload_date` datetime(6) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `cover_id` bigint DEFAULT NULL,
  `file_id` bigint DEFAULT NULL,
  `github_id` bigint DEFAULT NULL,
  `image_id` bigint DEFAULT NULL,
  `owner` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `FKrafamub89ipy7y51n29xdfppq` (`cover_id`),
  KEY `FKqf05w7npupggan4ms8j6apvd2` (`file_id`),
  KEY `FKb6cnbhlpsh7dkjlloa0b4i141` (`github_id`),
  KEY `FKl8w2rm3jxnnavyb13ok0qjybe` (`image_id`),
  KEY `FKmhdsoju7hguagwng33faqsf5f` (`owner`),
  CONSTRAINT `FKb6cnbhlpsh7dkjlloa0b4i141` FOREIGN KEY (`github_id`) REFERENCES `github` (`id`),
  CONSTRAINT `FKl8w2rm3jxnnavyb13ok0qjybe` FOREIGN KEY (`image_id`) REFERENCES `files` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKmhdsoju7hguagwng33faqsf5f` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKqf05w7npupggan4ms8j6apvd2` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKrafamub89ipy7y51n29xdfppq` FOREIGN KEY (`cover_id`) REFERENCES `files` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=461 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.extensions: ~24 rows (approximately)
/*!40000 ALTER TABLE `extensions` DISABLE KEYS */;
INSERT INTO `extensions` (`id`, `description`, `featured`, `name`, `pending`, `rating`, `times_rated`, `upload_date`, `version`, `cover_id`, `file_id`, `github_id`, `image_id`, `owner`) VALUES
	(381, 'SonarCloud is the leading product for Continuous Code Quality online, totally free for open-source projects. It supports all major programming languages, including C#, VB .Net, JavaScript, TypeScript, C/C++ and many more. If your code is closed source, SonarCloud also offers a paid plan to run private analyses.', b'0', 'Sonar Cloud', b'0', 5, 1, '2018-09-09 22:32:46.000000', '1', 133, 63, 151, 64, 36),
	(382, 'Move your packages to the cloud<br />You no longer need to manage legacy on-prem file shares or host private package servers. We’ll host, index, and manage your packages seamlessly in VSTS/TFS right alongside your source code, builds, and releases.<br />Best-in-class NuGet server<br />Create scalable .NET apps, frameworks, and microservices using NuGet packages. Stay on the forefront of .NET and .NET Core development with support for the latest NuGet updates. Publish symbols for your packages to the Package Management symbol server to enable easy debugging.<br />Private npm registry<br />Create and share private npm packages with a hosted npm registry. Proxy and cache packages from npmjs.com so you can make private packages without using scopes.<br />Host your Maven artifacts<br />Create and share Maven artifacts with the rest of your team. Use Maven or Gradle to build your code and let us manage your artifacts.<br />Deliver packages in every build for package continuous integration<br />Pack and publish packages to Package Management and to NuGet.org and npmjs.com. Automatically version your packages alongside your builds. Use release views to communicate package quality.<br />Easily debug with symbols<br />Publish the symbols created by your build to the Symbol Server in Package Management so your team can easily debug as they develop.', b'0', 'Package Management', b'0', 5, 1, '2018-09-09 22:40:34.000000', '3.32', 133, 65, 309, 66, 1),
	(383, 'This extension adds the following tasks: Package Application, Push Packages to Octopus, Create Octopus Release, Deploy Octopus Release', b'1', 'Octopus Deploy In', b'0', 4.5, 2, '2018-09-09 22:51:50.000000', '2.4', 133, 67, 153, 68, 1),
	(384, 'The different parameters of the task are explained below:<br />Project: Solution or project that you intend to build.<br />Configuration: Build configuration. Eg. \'Release\'<br />Platform: Build platform; only applied when a build configuration is specified. Leave blank for solution/project default.<br />Visual Studio Version": A specific version of Visual Studio that should be used for this build (in case of multiple versions present on the build server).<br />Deploy: Builds the solution, along with files necessary for deployment, according to the solutions configuration. The specified project must be a deployment project. If the specified project is not a deployment project, when the project that has been built is passed to be deployed, it fails with an error.<br />Clean: Deletes any files created by the build command, without affecting source files.', b'0', 'Build with devenv edit', b'0', 4, 1, '2018-09-09 22:56:27.000000', '0.0.001', 133, 69, 154, 70, 20),
	(385, 'Download generic build dependencies from Artifactory<br />The Artifactory Generic Download task supports downloading your build dependencies from Artifactory to the build agent. The task triggers the JFrog CLI to perform the download. The downloaded dependencies are defined using File Specs and can be also configured to capture the build-info. It will store the downloaded files as dependencies in the build-info which can later be published to Artifactory using the Artifactory Publish Build-Info task.', b'1', 'JFrog Artifactory', b'0', 4.5, 2, '2018-09-09 22:59:06.000000', '1.2a', 133, 71, 155, 72, 21),
	(387, 'Link Work Items to Wiki Pages<br />This extension will add a custom UI control for all Work Item forms. With this UI control you will be able to see all the Wiki Pages that were attached to current Work Item.<br />How to install<br />After you install this extension from Marketplace it will automatically add custom UI control to all Work Item forms, no further configuration required.<br />How to use?', b'1', 'Linked Wiki pages', b'0', 4, 2, '2018-09-09 23:04:26.000000', '1', 133, 75, 157, 76, 23),
	(388, 'Accelerate your Apps with Mobile DevOps<br /><br />Take advantage of HockeyApp and bring a full Mobile DevOps flow to your mobile app development. Through this extension, HockeyApp integrates with Visual Studio Team Services or Team Foundation Server to streamline development, management, monitoring, and delivery of your mobile apps.<br /><br />Distribution – Upload your apps for beta testing and seamless in-app updates<br /><br />Crash Reports – Get symbolicated crash reports while testing and in production<br /><br />Feedback – Allow your users to create a dialog with you directly from within the app<br /><br />User Metrics – See how users interact with your app and plan your investments based on real data<br /><br />Managing Apps – Create teams to manage access, integrate with Azure Active Directory, and quickly on-board your team<br /><br />Getting Started with HockeyApp<br /><br />Don\'t have a HockeyApp account? It\'s easy to get started. You can use your Microsoft Account, social media account or just an email address to setup your HockeyApp account. The free plan comes with two apps, one owner, and no data restrictions. If you need more apps or owners you can upgrade to one of our business plans to take advantage of more apps and more owners. Each app allows you to target a specific platform. You can keep an unlimited number of versions of each app in HockeyApp with no data limits on crashes, feedback or user metrics. Only owners can create apps, but once an app is created developers or your continuous integration / continuous deployment process can upload new versions of the app, making updates simple for your organization. Get started with HockeyApp.', b'0', 'HockeyApp', b'0', 4.5, 2, '2018-09-09 23:07:45.000000', '0.000001', 133, 77, 378, 78, 24),
	(389, 'IIS Web App Deployment Using WinRM<br /><br />Using Windows Remote Management (WinRM), connect to the host machine(s) where IIS or SQL Server is installed, and manage the Web application or deploy the SQL Server Database as described below:<br /><br />Create a new website or update an existing website using AppCmd.exe.<br /><br />Create a new application pool or update an existing application pool using AppCmd.exe.<br /><br />Deploy a Web Application to the IIS Server using Web Deploy.<br /><br />Deploy a SQL Server Database using DACPAC and SqlPackage.exe.<br /><br />To easily setup WinRM on the host machines, follow the directions for the domain-joined machines or the workgroup machines.<br /><br />The Visual Studio Team Services accounts that are using the preview tasks wiz. IIS Web Application Deployment or SQL Server Database Deployment, should move to this extension. All future enhancements to the IIS Web App Deployment task or to the SQL Server Database Deployment task will be provided in this extension.', b'0', 'IIS Web App Deployment Using WinRM', b'1', 5, 1, '2018-09-09 23:09:01.000000', '213a', 133, 79, 159, 80, 24),
	(390, 'Open in Excel<br /><br />This extension requires Microsoft Excel and one of the following clients to be installed:<br /><br />Visual Studio 2017 or later<br /><br />Team Foundation Server Office® Integration 2017 or later<br /><br />Use this extension for bulk editing work items, or to leverage Excel tools to analyze and visualize a large number of work items. Work items that are opened in Excel can be edited and published back to Visual Studio Team Services with a single click. Once you are ready to publish your changes, simply hit "Publish" from Excel to sync your changes back to VSTS. Learn more about Office integration', b'0', 'VSTS Open in Excel', b'0', 4, 1, '2018-09-09 23:19:13.000000', '1', 133, 81, 160, 82, 25),
	(391, 'Agile Poker is a versatile toolkit for estimating your product backlog to get it ready for grooming and planning. It is heavily inspired by Planning Poker® - but not limited to - and derives the best practices from industry standard estimation techniques.<br /><br />Getting started<br /><br />Start estimation by clicking "cards" extension icon in the top-right menu above the Board', b'0', 'Agile Poker - estimation toolkit for VSTS', b'0', 4, 1, '2018-09-09 23:30:42.000000', '1', 133, 83, 330, 84, 25),
	(392, 'Slack is a popular team collaboration service that helps teams be more productive by keeping all communications in one place and easily searchable from virtually anywhere. All your messages, your files, and everything from Twitter, Dropbox, Google Docs, Visual Studio Team Services, and more all together. Slack also has fully native apps for iOS and Android to give you the full functionality of Slack wherever you go.', b'0', 'Slack Integration', b'0', 4.5, 2, '2018-09-09 23:33:50.000000', 'a', 133, 85, 162, 86, 27),
	(393, 'Visual Studio Live Share enables you to collaboratively edit and debug with others in real time, regardless what programming languages you\'re using or app types you\'re building. It allows you to instantly (and securely) share your current project, and then as needed, share debugging sessions, terminal instances, localhost web apps, voice calls, and more! Developers that join your sessions recieve all of their editor context from your environment (e.g. language services, debugging), which ensures they can start productively collaborating immediately, without needing to clone any repos or install any SDKs.', b'0', 'wxml', b'0', 4, 2, '2018-09-09 23:39:43.000000', 'as', 133, 87, 163, 88, 36),
	(394, 'Python - Linting, Debugging (multi-threaded, remote), Intellisense, code formatting, refactoring, unit tests, snippets, Data Science (with Jupyter), PySpark and more.<br /><br />Jupyter - Data Science with Jupyter on Visual Studio Code.<br /><br />MagicPython - Syntax highlighter for cutting edge Python.<br /><br />Jinja - Jinja template language support for Visual Studio Code.<br /><br />Django - Beautiful syntax and scoped snippets for perfectionists with deadlines.<br /><br />Visual Studio IntelliCode - Provides AI-assisted productivity features for Python developers in Visual Studio Code with insights based on understanding your code combined with machine learning..', b'0', 'Python Extension Pack', b'0', 4.5, 2, '2018-09-09 23:40:33.000000', '213', 133, 89, 164, 90, 36),
	(395, 'A VS Code extension to debug your JavaScript code in the Google Chrome browser, or other targets that support the Chrome DevTools Protocol.<br /><br />', b'0', 'Debugger for Chrome', b'1', 4.5, 2, '2018-09-09 23:41:43.000000', '1.992.2', 133, 91, 165, 131, 36),
	(396, 'You can find more detailed information about C/C++ support for Visual Studio Code at our GitHub page and our VS Code documentation page.', b'0', 'C/C++', b'0', 4.5, 2, '2018-09-09 23:42:44.000000', '2.3++', 133, 92, 166, 93, 36),
	(397, 'Show a file\'s dependencies at the top. Show what values are used from an open Per-value type codelens (off by default)', b'0', 'reason-vscode', b'0', 4, 2, '2018-09-09 23:44:02.000000', 'ver1', 133, 94, 167, 95, 36),
	(398, 'This extension provides rich KAG3 Script language support for Visual Studio Code. It\'s still in progress ( GitHub ), please expect frequent updates with breaking changes before 1.0. If you are interested in this project, feel free to<br /><br />File GitHub issues anytime you ran into unexpected situations/bugs.<br /><br />Fork our project, hack it around and send us PRs!', b'0', 'Kirikiri Adventure Game (KAG) Script', b'1', 4.5, 2, '2018-09-09 23:46:22.000000', '12.2.334', 133, 96, 168, 97, 30),
	(399, 'I am going to prepare for the college entrance examination, I will not actively update the extension. if you find a bug, please report an issue, I will fix (If I have free time).', b'0', 'GML Support', b'1', 4, 2, '2018-09-09 23:47:08.000000', '0.01', 133, 98, 169, 99, 30),
	(400, 'This is Visual Studio Code extension providing support of Daedalus language used for gameplay scripting in games on ZenGin engine (Gothic I, II & II:NoTR). The project was developed primarily for SoulFire.', b'0', 'Daedalus', b'1', 4, 2, '2018-09-09 23:48:09.000000', '12.444', 133, 100, 170, 101, 30),
	(401, 'Paradox Syntax<br /><br />This extension is intended to help modders creating mods for Paradox Interactive games by providing basic yet necessary conveniences.<br /><br />Current Features<br /><br />Syntax support<br /><br />Custom theme<br /><br />Snippets', b'0', 'Paradox Syntax', b'1', 4, 2, '2018-09-09 23:49:00.000000', '1', 133, 102, 171, 103, 30),
	(402, 'Pong<br /><br />Features<br /><br />Play Pong-like Game', b'0', 'Pong', b'1', 4.5, 2, '2018-09-09 23:52:40.000000', '23', 133, 104, 172, 105, 31),
	(403, 'The Material Icon Theme provides lots of icons based on Material Design for Visual Studio Code.<br /><br />', b'0', 'Try\'s Icon Pack', b'0', 3.5, 2, '2018-09-09 23:53:20.000000', '1', 133, 106, 173, 107, 31),
	(404, 'ScalaSnippets README<br /><br />Scala & SBT snippets for the Visual Studio Code', b'1', 'ScalaSnippets', b'0', 4, 2, '2018-09-09 23:54:22.000000', '1.0a', 133, 108, 174, 109, 31),
	(460, 'Restaurant app is a modern way to create orders. It has different type of roles that can create orders and mark them as ready. Chat feature is added where every employe can write to each other.', b'1', 'Restaurant App', b'0', 4.5, 2, '2019-04-02 04:20:39.000000', '2.0b', 133, 132, 294, 130, 36);
/*!40000 ALTER TABLE `extensions` ENABLE KEYS */;

-- Dumping structure for table extensions-market.extension_tags
CREATE TABLE IF NOT EXISTS `extension_tags` (
  `extension` bigint NOT NULL,
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY (`extension`,`tag`),
  KEY `FK1wfbcj98jdmcnncap5gbnawm2` (`tag`),
  CONSTRAINT `FK1wfbcj98jdmcnncap5gbnawm2` FOREIGN KEY (`tag`) REFERENCES `tags` (`name`),
  CONSTRAINT `FKkofqklosvfuersha5vhnuo87u` FOREIGN KEY (`extension`) REFERENCES `extensions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.extension_tags: ~8 rows (approximately)
/*!40000 ALTER TABLE `extension_tags` DISABLE KEYS */;
INSERT INTO `extension_tags` (`extension`, `tag`) VALUES
	(3, '2.0'),
	(4, 'app'),
	(1, 'auto'),
	(3, 'auto'),
	(2, 'build'),
	(2, 'c'),
	(3, 'restaurant'),
	(5, 'vision');
/*!40000 ALTER TABLE `extension_tags` ENABLE KEYS */;

-- Dumping structure for table extensions-market.files
CREATE TABLE IF NOT EXISTS `files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `download_count` int DEFAULT NULL,
  `extension_type` varchar(255) DEFAULT NULL,
  `resource_type` varchar(255) DEFAULT NULL,
  `size` double NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `extension` bigint DEFAULT NULL,
  `owner` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk991n31ns2si7oy9qbsx9s22b` (`extension`),
  KEY `FK7smd1t0j0srej7e4e1wnfmrhc` (`owner`),
  CONSTRAINT `FK7smd1t0j0srej7e4e1wnfmrhc` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKk991n31ns2si7oy9qbsx9s22b` FOREIGN KEY (`extension`) REFERENCES `extensions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.files: ~52 rows (approximately)
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
INSERT INTO `files` (`id`, `download_count`, `extension_type`, `resource_type`, `size`, `type`, `extension`, `owner`) VALUES
	(63, 0, 'txt', 'file', 19270, 'text/plain', 381, 1),
	(64, 0, 'png', 'logo', 11451, 'image/png', 381, 36),
	(65, 30, 'txt', 'file', 4945, 'text/plain', 382, 36),
	(66, 0, 'png', 'logo', 5409, 'image/png', 382, 23),
	(67, 0, 'txt', 'file', 15, 'text/plain', 383, 23),
	(68, 0, 'png', 'logo', 10920, 'image/png', 383, 36),
	(69, 0, 'txt', 'file', 15, 'text/plain', 384, 1),
	(70, 0, 'png', 'logo', 7300, 'image/png', 384, 23),
	(71, 2, 'txt', 'file', 15, 'text/plain', 385, 25),
	(72, 0, 'png', 'logo', 5519, 'image/png', 385, 1),
	(75, 0, 'txt', 'file', 15, 'text/plain', 387, 23),
	(76, 0, 'png', 'logo', 7264, 'image/png', 387, 36),
	(77, 0, 'txt', 'file', 15, 'text/plain', 388, 25),
	(78, 0, 'png', 'logo', 10749, 'image/png', 388, 1),
	(79, 0, 'txt', 'file', 15, 'text/plain', 389, 1),
	(80, 0, 'png', 'logo', 12328, 'image/png', 389, 36),
	(81, 0, 'txt', 'file', 15, 'text/plain', 390, 37),
	(82, 0, 'png', 'logo', 4945, 'image/png', 390, 23),
	(83, 4, 'txt', 'file', 15, 'text/plain', 391, 37),
	(84, 0, 'png', 'logo', 15412, 'image/png', 391, 25),
	(85, 0, 'txt', 'file', 15, 'text/plain', 392, 1),
	(86, 0, 'png', 'logo', 5581, 'image/png', 392, 37),
	(87, 0, 'txt', 'file', 15, 'text/plain', 393, 23),
	(88, 0, 'png', 'logo', 5310, 'image/png', 393, 37),
	(89, 0, 'txt', 'file', 15, 'text/plain', 394, 23),
	(90, 0, 'png', 'logo', 11936, 'image/png', 394, 25),
	(91, 0, 'txt', 'file', 15, 'text/plain', 395, 23),
	(92, 0, 'txt', 'file', 15, 'text/plain', 396, 25),
	(93, 0, 'png', 'logo', 6996, 'image/png', 396, 23),
	(94, 0, 'txt', 'file', 15, 'text/plain', 397, 38),
	(95, 0, 'png', 'logo', 16195, 'image/png', 397, 37),
	(96, 0, 'txt', 'file', 15, 'text/plain', 398, 27),
	(97, 0, 'png', 'logo', 13211, 'image/png', 398, 21),
	(98, 0, 'txt', 'file', 15, 'text/plain', 399, 25),
	(99, 0, 'png', 'logo', 2080, 'image/png', 399, 24),
	(100, 0, 'txt', 'file', 15, 'text/plain', 400, 23),
	(101, 0, 'png', 'logo', 5992, 'image/png', 400, 22),
	(102, 0, 'txt', 'file', 15, 'text/plain', 401, 38),
	(103, 0, 'png', 'logo', 9194, 'image/png', 401, 1),
	(104, 0, 'txt', 'file', 15, 'text/plain', 402, 37),
	(105, 0, 'png', 'logo', 31137, 'image/png', 402, 21),
	(106, 0, 'txt', 'file', 15, 'text/plain', 403, 36),
	(107, 0, 'png', 'logo', 23578, 'image/png', 403, 19),
	(108, 0, 'txt', 'file', 15, 'text/plain', 404, 1),
	(109, 0, 'png', 'logo', 38137, 'image/png', 404, 21),
	(110, 0, 'png', 'logo', 4868, 'image/png', 400, 1),
	(130, 0, 'png', 'logo', 22453, 'image/png', 460, 36),
	(131, 0, 'png', 'logo', 6389, 'image/png', 395, 1),
	(132, 5, 'jar', 'file', 16258, 'file/plain', 460, 38),
	(133, 0, 'jpg', 'cover', 5100, 'image/jpg', 383, 21),
	(134, 0, 'png', 'profileImage', 46000, 'image/png', NULL, 36),
	(135, 0, 'png', 'logo', 15200, 'image/png', 389, 1);
/*!40000 ALTER TABLE `files` ENABLE KEYS */;

-- Dumping structure for table extensions-market.github
CREATE TABLE IF NOT EXISTS `github` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fail_message` varchar(255) DEFAULT NULL,
  `last_commit` datetime(6) DEFAULT NULL,
  `last_fail` datetime DEFAULT NULL,
  `last_success` datetime DEFAULT NULL,
  `open_issues` int DEFAULT NULL,
  `pull_requests` int DEFAULT NULL,
  `repo` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=385 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.github: ~36 rows (approximately)
/*!40000 ALTER TABLE `github` DISABLE KEYS */;
INSERT INTO `github` (`id`, `fail_message`, `last_commit`, `last_fail`, `last_success`, `open_issues`, `pull_requests`, `repo`, `user`) VALUES
	(1, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended. Check URL.', '2021-10-10 22:33:13.000000', '2020-09-10 02:48:43', '2021-10-11 06:00:24', 0, 0, 'extension-market-remastered', 'ivaylo9512'),
	(2, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/SonarSource/sonar-scanner-vsts. Check URL.', '2021-10-07 06:50:22.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:28', 0, 2, 'sonar-scanner-vsts', 'SonarSource'),
	(3, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/IvanGrigorov/RPG-Game-Challenge. Check URL.', '2015-05-27 15:04:40.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:30', 0, 0, 'RPG-Game-Challenge', 'IvanGrigorov'),
	(4, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/mmajcica/DevEnvBuild. Check URL.', '2019-05-30 08:34:40.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:31', 7, 4, 'DevEnvBuild', 'mmajcica'),
	(5, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/GameGrind/Simple-RPG-in-Unity. Check URL.', '2019-05-26 04:56:08.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:33', 0, 0, 'Simple-RPG-in-Unity', 'GameGrind'),
	(6, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/TFSExt/Forms. Check URL.', '2019-07-20 19:54:22.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:35', 15, 1, 'Forms', 'TFSExt'),
	(7, 'org.kohsuke.github.GHException: Couldn\'t connect to repo: \'roleplaying-game\'. Check details.', NULL, '2021-10-11 06:00:36', NULL, 0, 0, 'roleplaying-game', 'topics'),
	(150, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/ivaylo9512/Tick42-ExtensionRepository-Extended. Check URL.', '2021-10-10 22:33:13.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:41', 0, 0, 'Tick42-ExtensionRepository-Extended', 'ivaylo9512'),
	(151, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/SonarSource/sonar-scanner-vsts. Check URL.', '2021-10-07 06:50:22.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:46', 0, 2, 'sonar-scanner-vsts', 'SonarSource'),
	(153, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/IvanGrigorov/RPG-Game-Challenge. Check URL.', '2015-05-27 15:04:40.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:47', 0, 0, 'RPG-Game-Challenge', 'IvanGrigorov'),
	(154, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/mmajcica/DevEnvBuild. Check URL.', '2019-05-30 08:34:40.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:49', 7, 4, 'DevEnvBuild', 'mmajcica'),
	(155, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/GameGrind/Simple-RPG-in-Unity. Check URL.', '2019-05-26 04:56:08.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:50', 0, 0, 'Simple-RPG-in-Unity', 'GameGrind'),
	(157, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/TFSExt/Forms. Check URL.', '2019-07-20 19:54:22.000000', '2021-09-10 02:48:43', '2021-10-11 06:00:52', 15, 1, 'Forms', 'TFSExt'),
	(159, 'org.kohsuke.github.GHException: Couldn\'t connect to repo: \'roleplaying-game\'. Check details.', NULL, '2021-10-11 06:00:53', NULL, 0, 0, 'roleplaying-game', 'topics'),
	(160, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/juliangarnier/3D-Hartwig-chess-set. Check URL.', '2012-12-13 21:25:54.000000', '2021-09-10 02:48:44', '2021-10-11 06:00:55', 7, 8, '3D-Hartwig-chess-set', 'juliangarnier'),
	(162, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/alexander-rakhlin/ICIAR2018. Check URL.', '2020-12-16 21:31:52.000000', '2021-09-10 02:48:44', '2021-10-11 06:00:57', 0, 0, 'ICIAR2018', 'alexander-rakhlin'),
	(163, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/MicrosoftDocs/live-share. Check URL.', '2021-09-28 11:01:32.000000', '2021-09-10 02:48:44', '2021-10-11 06:01:21', 428, 0, 'live-share', 'MicrosoftDocs'),
	(164, 'org.kohsuke.github.GHException: Couldn\'t connect to repo: \'python-extension-pack\'. Check details.', '2020-08-31 21:49:23.000000', '2021-10-04 05:43:39', '2021-10-11 06:01:24', 8, 2, 'python-extension-pack', 'DonJayamanne'),
	(165, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/Microsoft/vscode-chrome-debug. Check URL.', '2021-08-03 22:04:45.000000', '2021-09-10 02:48:44', '2021-10-11 06:01:37', 0, 0, 'vscode-chrome-debug', 'Microsoft'),
	(166, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Connected to https://github.com/Microsoft/vscode-cpptools but couldn\'t fetch data.', '2021-10-05 00:28:41.000000', '2021-09-10 03:34:12', '2021-10-11 06:02:16', 918, 5, 'vscode-cpptools', 'Microsoft'),
	(167, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/jaredly/reason-language-server. Check URL.', '2021-02-01 22:01:34.000000', '2021-09-10 02:48:45', '2021-10-11 06:02:26', 138, 6, 'reason-language-server', 'jaredly'),
	(168, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/happiness9721/Kirikiri-Adventure-Game-KAG-Script. Check URL.', '2017-05-21 05:55:39.000000', '2021-09-10 02:48:45', '2021-10-11 06:02:27', 0, 0, 'Kirikiri-Adventure-Game-KAG-Script', 'happiness9721'),
	(169, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/gml-support/gml-support. Check URL.', '2021-03-13 09:05:34.000000', '2021-09-10 02:48:45', '2021-10-11 06:02:30', 3, 1, 'gml-support', 'gml-support'),
	(170, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/Szmyk/daedalus-vscode. Check URL.', '2019-12-03 18:54:22.000000', '2021-09-10 02:48:45', '2021-10-11 06:02:32', 0, 0, 'daedalus-vscode', 'Szmyk'),
	(171, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/gml-support/gml-tools-langserver. Check URL.', '2018-08-30 03:27:31.000000', '2021-09-10 02:48:45', '2021-10-11 06:02:34', 0, 0, 'gml-tools-langserver', 'gml-support'),
	(172, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/happiness9721/line-bot-sdk-swift. Check URL.', '2018-05-03 07:34:13.000000', '2021-09-10 02:48:46', '2021-10-11 06:02:36', 3, 0, 'line-bot-sdk-swift', 'happiness9721'),
	(173, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Connected to https://github.com/rafapaulin/vscode-material-icon-theme but couldn\'t fetch data.', '2018-09-01 16:09:27.000000', '2021-09-10 04:15:39', '2021-10-11 06:02:43', 0, 0, 'vscode-material-icon-theme', 'rafapaulin'),
	(174, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/jaredly/vim-debug. Check URL.', '2016-08-13 03:56:30.000000', '2021-09-10 02:48:46', '2021-10-11 06:02:46', 15, 2, 'vim-debug', 'jaredly'),
	(294, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/ivaylo9512/Restaurant-app-with-chat-client-JavaFx-desktopApp-3-designs. Check URL.', '2021-09-15 20:27:44.000000', '2021-09-10 02:48:46', '2021-10-11 06:02:51', 1, 0, 'Restaurant-app-with-chat-client-JavaFx-desktopApp-3-designs', 'ivaylo9512'),
	(309, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/uzh-rpg/rpg_dvs_ros. Check URL.', '2020-12-19 13:50:21.000000', '2021-09-10 02:48:46', '2021-10-11 06:02:55', 17, 1, 'rpg_dvs_ros', 'uzh-rpg'),
	(330, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Couldn\'t connect to https://github.com/OCA/vertical-medical. Check URL.', '2021-08-14 05:13:25.000000', '2021-09-10 02:48:46', '2021-10-11 06:03:04', 17, 0, 'vertical-medical', 'OCA'),
	(378, 'com.tick42.quicksilver.exceptions.GitHubRepositoryException: Connected to https://github.com/swordmaster2k/rpgwizard but couldn\'t fetch data.', '2021-10-01 16:37:29.000000', '2021-09-10 04:16:06', '2021-10-11 06:03:09', 32, 19, 'rpgwizard', 'swordmaster2k'),
	(381, 'org.kohsuke.github.GHException: Failed to retrieve https://api.github.com/repositories/170855134/commits?page=13', '2021-10-10 22:33:13.000000', '2021-10-09 16:40:24', '2021-10-11 06:03:14', 0, 0, 'extension-market-remastered', 'ivaylo9512'),
	(382, NULL, '2021-10-10 22:33:13.000000', NULL, '2021-10-11 06:03:20', 0, 0, 'extension-market-remastered', 'ivaylo9512'),
	(383, NULL, '2021-10-10 22:33:13.000000', NULL, '2021-10-11 06:03:24', 0, 0, 'extension-market-remastered', 'ivaylo9512'),
	(384, NULL, '2021-10-10 22:33:13.000000', NULL, '2021-10-11 06:03:29', 0, 0, 'extension-market-remastered', 'ivaylo9512');
/*!40000 ALTER TABLE `github` ENABLE KEYS */;

-- Dumping structure for table extensions-market.ratings
CREATE TABLE IF NOT EXISTS `ratings` (
  `extension` bigint NOT NULL,
  `user` bigint NOT NULL,
  `rating` int NOT NULL,
  PRIMARY KEY (`extension`,`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.ratings: ~42 rows (approximately)
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
INSERT INTO `ratings` (`extension`, `user`, `rating`) VALUES
	(381, 36, 5),
	(382, 36, 5),
	(383, 1, 4),
	(383, 36, 5),
	(384, 36, 4),
	(385, 1, 4),
	(385, 36, 5),
	(387, 1, 4),
	(387, 36, 4),
	(388, 1, 5),
	(388, 36, 4),
	(389, 36, 5),
	(390, 36, 4),
	(391, 36, 4),
	(392, 1, 5),
	(392, 36, 4),
	(393, 1, 3),
	(393, 36, 5),
	(394, 1, 4),
	(394, 36, 5),
	(395, 1, 5),
	(395, 36, 4),
	(396, 1, 4),
	(396, 36, 5),
	(397, 1, 5),
	(397, 36, 3),
	(398, 1, 4),
	(398, 36, 5),
	(399, 1, 5),
	(399, 36, 3),
	(400, 1, 4),
	(400, 36, 4),
	(401, 1, 3),
	(401, 36, 5),
	(402, 1, 5),
	(402, 36, 4),
	(403, 1, 4),
	(403, 36, 3),
	(404, 1, 4),
	(404, 36, 4),
	(460, 1, 5),
	(460, 36, 4);
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;

-- Dumping structure for table extensions-market.settings
CREATE TABLE IF NOT EXISTS `settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rate` int NOT NULL,
  `git_token` varchar(255) DEFAULT NULL,
  `git_username` varchar(255) DEFAULT NULL,
  `wait` int NOT NULL,
  `user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo4ke5o52eci58foyh2jws0bi2` (`user`),
  CONSTRAINT `FKo4ke5o52eci58foyh2jws0bi2` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.settings: ~6 rows (approximately)
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` (`id`, `rate`, `git_token`, `git_username`, `wait`, `user`) VALUES
	(1, 5000, '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', 'ivaylo9512', 5000, 1);
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;

-- Dumping structure for table extensions-market.tags
CREATE TABLE IF NOT EXISTS `tags` (
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.tags: ~69 rows (approximately)
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
	('false'),
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
	('tag'),
	('tag1'),
	('tag2'),
	('task'),
	('test'),
	('test task'),
	('tests'),
	('typescript'),
	('update'),
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

-- Dumping structure for table extensions-market.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `country` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `extensions_rated` int DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `is_active` int NOT NULL,
  `is_enabled` bit(1) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `rating` double NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `profile_image` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`),
  KEY `FKc7w5h6b6d74bu9o1yulvrbh6c` (`profile_image`),
  CONSTRAINT `FKc7w5h6b6d74bu9o1yulvrbh6c` FOREIGN KEY (`profile_image`) REFERENCES `files` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table extensions-market.users: ~19 rows (approximately)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `country`, `email`, `extensions_rated`, `info`, `is_active`, `is_enabled`, `password`, `rating`, `role`, `username`, `profile_image`) VALUES
	(1, 'Bulgaria', 'email1@gmail.com', 3, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 4.166666666666667, 'ROLE_ADMIN', 'admin', 103),
	(19, 'Spain', 'email2@gmail.com', 0, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$ZoKhfJblgh0a1NfVtRdkvu0h.SIhdKnPq2iyhtxXwDQKe0bQeK/uu', 0, 'ROLE_USER', 'kiril1987', 135),
	(20, 'Italy', 'email3@gmail.com', 1, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 4, 'ROLE_USER', 'Robocop', 72),
	(21, 'Bulgaria', 'email4@gmail.com', 1, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$ZNXjn5UZuDrGClbeAZaJ/.c37udduJ1ciVrZih6LeWwSQ/Qi2UdDS', 4.5, 'ROLE_USER', 'Batman_batman', 68),
	(22, 'Spain', 'email5@gmail.com', 0, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$EWABZM1fVroR5pxbtyDaReopB8Lxa7qg4ZLP5blxmrY9N1oMssQai', 0, 'ROLE_USER', 'IvayloAleksandrov', 64),
	(23, 'Bulgaria', 'email6@gmail.com', 1, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$jIr.bgNqa5pUIiNr1M9PV.Ek02O/Qba781xrnGpPxjG3IXKG2mEaW', 4, 'ROLE_USER', 'VeselinGeorgiev', 82),
	(24, 'Bulgaria', 'email7@gmail.com', 2, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$m2ApL7GXTAeZ1sRsBp1I6uuEVs.ukjmjRSQg2vFzJ3jWcNiRpqSRG', 4.75, 'ROLE_USER', 'KrasimirZahariev', 95),
	(25, 'Bulgaria', 'email8@gmail.com', 2, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$rU6oNY216IDjw3.kIy.AL.YRSPCl8JV3ovI7/v0Gd4xcxG1mTXS1a', 4, 'ROLE_USER', 'MartinStoyanoff', 76),
	(27, 'Spain', 'email9@gmail.com', 1, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$Ow7zt6ArZM0didTXuZSyCe8t1v2pHDBnsD7WDkExwBieoVHCfOtBO', 4.5, 'ROLE_USER', 'RosiBusarova', 72),
	(29, 'Bulgaria', 'email10@gmail.com', 5, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$6N7btYP9Y483qlVdz.ldQuWM.GTo6sSkb7WP5.NjhLUEiy4wD9Z6S', 4.3, 'ROLE_USER', 'LuchiaSavova', 84),
	(30, 'Spain', 'emai11l@gmail.com', 4, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$YibgvWtHYtX0FFa2HjILQODpP9crvDthnk3Ve88SFksBFvVnriDdi', 4.125, 'ROLE_USER', 'MariaIskrova', 86),
	(31, 'Italy', 'email12@gmail.com', 3, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$nl4ngkcqWvq2KKfB2fuCa.ZoGMcc1U3oSt3YLJTOB4P2KM/QHWy/S', 4, 'ROLE_USER', 'MariaGrigorova', 88),
	(36, 'Bulgaria', 'email13@gmail.com', 1, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$PFraPHMqOOa7qiBJX5Mmq.STptiykt4m1H.p7rfpzzg/x1mQ9Ega6', 4.5, 'ROLE_ADMIN', 'Adobe Inc.', 134),
	(37, 'Spain', 'email14@gmail.com', 0, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$g81cEI380e1Qsk1Arr2OiuU6NCABymsnbXzP5WCkmXwk14OUaZ3pi', 0, 'ROLE_USER', 'ivailo9512', 68),
	(38, 'Germany', 'email15@gmail.com', 0, 'Adobe Inc. is an American multinational computer software company headquartered in San Jose, California.Adobe Inc. is an American multinational computer software company headquartered in San Jose, California. Adobe Inc. is an American multinational comput', 1, b'1', '$2a$04$6w6FIjuQUBeCyejL/3uOjeDuLehjai0Uez2jeUNBEOxB2rB/5fipa', 0, 'ROLE_USER', 'ivailo95123', 64);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
