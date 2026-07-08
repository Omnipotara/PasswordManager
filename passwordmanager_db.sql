/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.0.18 : Database - passwordmanager_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`passwordmanager_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `passwordmanager_db`;

/*Table structure for table `password_entries` */

DROP TABLE IF EXISTS `password_entries`;

CREATE TABLE `password_entries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `service` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `password_entries_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `password_entries` */

insert  into `password_entries`(`id`,`user_id`,`service`,`username`,`password`,`description`) values 
(3,4,'Facebook','MyAccount','FwHOmzh56bBroHPlpTuoNuBwX7RWcK7Qn9vceAMvv+Jv33px','This is my account. I love it and I want to keep it safe. I am a good person.'),
(7,3,'Instagram','Omnix32','YWyEE129swknU9NU6bdtchTq0RHSgfgzwCMSAouzfmWp2xFtDEH9Nro=','This is my ultra safe account! With a changed DESCRIPTION!'),
(9,3,'Facebook','Ognjen2003','jpSXgWRtAipQ8H5Lmp4MfUGVul5A41SO6azoctULWMU1gpTiEA==','This is my facebook account!'),
(10,3,'instagram','test','kDnPs4aoVM8WQcvJlvMyRp5DBOjfqyGfIoJwoYl+8PS6ffk=','test123'),
(11,3,'faCeboOk','ognjen','cq3EnIYnLjaEfULDzISrGGY27LoGzDv0FjZvEudXJZ1aeA==','aa'),
(12,3,'World of Warcraft - Old Version','Gamer','Lc/CicjP9/K4uY2HRKIVqj8IZmEe7MDDTdLLhubotDl8AHK/','This is my WoW account. It is an older expansion.');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(254) NOT NULL,
  `password_hash` text NOT NULL,
  `salt` text NOT NULL,
  `hashing_algorithm` varchar(30) NOT NULL DEFAULT 'BCRYPT',
  `hashing_parameters` text DEFAULT NULL,
  `mfa_enabled` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `users` */

insert  into `users`(`id`,`email`,`password_hash`,`salt`,`hashing_algorithm`,`hashing_parameters`,`mfa_enabled`) values 
(3,'omnix@example.local','$2a$10$uzzwLGzzfQ5urGSXwoF0UO3XsBvtLM.UJD2OP.LOVpo06uab1cSB.','vyWwjzd+3aq7C96I5Mg81g==','BCRYPT',NULL,0),
(4,'testuser@example.local','$2a$10$ZF3EDVGlYiWU0H3P/HD8uuSijETWd3NreMW4XzUIAo78IqOfa.CqC','PPllYLi2D5XyTh8rZIX48A==','BCRYPT',NULL,0),
(5,'testaccount@example.local','$2a$10$9wusFsUHtsYb9BPRHP1GMeIeLjt.As0jUdFpQRPDVknKrDKVNMwa.','7/yZL/fRUwSVbozv8lQ8Rw==','BCRYPT',NULL,0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
