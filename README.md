# Tick42-ExtensionRepository

[![Build Status](https://travis-ci.org/Smytt/Tick42-ExtensionRepository.svg?branch=master)](https://travis-ci.org/Smytt/Tick42-ExtensionRepository)

IMPORTANT: If you want to test the github api you must generate your own github token from https://github.com/settings/tokens and set it from the admin section or directly in your database in the settings table.

Tick42-ExtensionRepository a.k.a Ext42 is a web platform which provides extension developers with the ability to share their work along with a github repository link. Every userModel can receive ratings for his extensions and also keep count on how many times they were downloaded. The app provides 3 different levels of accessibility.

The extensions with linked GitHubs are refreshed on interval set by the admins or every github can be manually refreshed also by the admins.

## How to run

### From the EXE file

You can run the application from the executable file it has all dependencies, the application is written in Java 8 - jre 1.8.

If you don't have the required jre the exe file will send you to the page where you can download it.

You should download the server-side-Jar folder, because it uses the upload folder in it for the images and the files for the uploaded extensions.

If Upload folder is not present it will be created, but all the images and the files for the pre-created extensions in the database won't be present.

It runs the application on 8090, you should check your ports first and kill any tasks on the port.

The EXE uses inner database so you don't need to execute the sql. It has pre-added data in it you can log with username: admin password: password.

You can also register, but the created user won't be admin. You can create new admins only with user that is already one. The option is available from the admin section.

### Without the EXE file

You need jre 1.8.0 editor and a database server:

Execute the sql:
`$ mysql -u root -p < database.sql`
the sql is set to user: root password: 1234. You can change that from the application.properties

Run the app:
`$ ./gradlew bootRun`

Again runs on :8090 port you can change that from application.properties

## Guest

A Guest userModel can browse through the extensions, search and download them.

## Registered userModel

A registered userModel can do everything the guest can, plus the following:
* upload, edit and delete their own extensions - including image and file upload and linking to a github repository
* rate others' extensions and receive ratings for own extensions

## Administrator

An administrator can do everything a registered userModel can, plus the following:
* enable / disable userModel profiles along with all their extensions
* enable / disable extensions
* feature / unfeature extensions
* force github data refresh - fetch latest data for last commit date, pull requests and open issues
* set refresh time for fetching all extensions' github data(all GitHubs are refreshed then application waits the time set and refreshes them again)
* register additional admin accounts

### How is it made?

The back end is a RestAPI created in Java and SpringMVC. Hibernate is used for ORM and the DB is MariaDB.
The front end is html / css + mustache.js for rendering the html templates.

There is one admin userModel preset in the database:
`username: admin`, `password: password`
