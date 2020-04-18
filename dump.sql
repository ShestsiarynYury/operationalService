-- MySQL dump 10.13  Distrib 8.0.18, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: operationalservice
-- ------------------------------------------------------
-- Server version	8.0.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `absences`
--

DROP TABLE IF EXISTS `absences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `absences` (
  `id_absence` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_absence`),
  UNIQUE KEY `a_1` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `absences`
--

LOCK TABLES `absences` WRITE;
/*!40000 ALTER TABLE `absences` DISABLE KEYS */;
INSERT INTO `absences` VALUES (4,'болен'),(2,'дежурство'),(3,'командировка');
/*!40000 ALTER TABLE `absences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id_category` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `fk_id_title` int(11) NOT NULL,
  PRIMARY KEY (`id_category`),
  UNIQUE KEY `c_1` (`name`),
  KEY `c_2` (`fk_id_title`),
  CONSTRAINT `c_2` FOREIGN KEY (`fk_id_title`) REFERENCES `titles` (`id_title`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (5,'офицеры',1),(6,'прапорщики',1),(7,'сержанты и солдаты',1),(8,'лгп',1),(9,'легковые',2),(10,'грузовые',2),(11,'специальная',2);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories_absences`
--

DROP TABLE IF EXISTS `categories_absences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories_absences` (
  `id_category_absence` int(11) NOT NULL AUTO_INCREMENT,
  `fk_id_absence` int(11) DEFAULT NULL,
  `fk_id_category` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_category_absence`),
  KEY `ca_2` (`fk_id_absence`),
  KEY `ca_1` (`fk_id_category`),
  CONSTRAINT `ca_1` FOREIGN KEY (`fk_id_category`) REFERENCES `categories` (`id_category`),
  CONSTRAINT `ca_2` FOREIGN KEY (`fk_id_absence`) REFERENCES `absences` (`id_absence`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories_absences`
--

LOCK TABLES `categories_absences` WRITE;
/*!40000 ALTER TABLE `categories_absences` DISABLE KEYS */;
INSERT INTO `categories_absences` VALUES (4,2,5),(5,2,6),(6,2,7),(7,2,8),(8,2,9),(9,2,10),(10,2,11),(11,3,5),(12,3,6),(13,3,7),(14,3,8),(15,4,5),(16,4,6),(17,4,7),(18,4,8);
/*!40000 ALTER TABLE `categories_absences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories_absences_counts`
--

DROP TABLE IF EXISTS `categories_absences_counts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories_absences_counts` (
  `id_category_absence_count` int(11) NOT NULL AUTO_INCREMENT,
  `count_absence` int(11) DEFAULT NULL,
  `fk_id_category_absence` int(11) DEFAULT NULL,
  `fk_id_expenditure_category_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_category_absence_count`),
  KEY `cac_1` (`fk_id_category_absence`),
  KEY `cac_2` (`fk_id_expenditure_category_count`),
  CONSTRAINT `cac_1` FOREIGN KEY (`fk_id_category_absence`) REFERENCES `categories_absences` (`id_category_absence`),
  CONSTRAINT `cac_2` FOREIGN KEY (`fk_id_expenditure_category_count`) REFERENCES `expenditure_category_counts` (`id_expenditure_category_count`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories_absences_counts`
--

LOCK TABLES `categories_absences_counts` WRITE;
/*!40000 ALTER TABLE `categories_absences_counts` DISABLE KEYS */;
INSERT INTO `categories_absences_counts` VALUES (1,0,5,51),(2,0,16,51),(3,0,12,51),(4,0,7,52),(5,0,18,52),(6,0,14,52),(7,0,4,53),(8,0,15,53),(9,0,11,53),(10,0,6,54),(11,0,17,54),(12,0,13,54),(13,0,8,55),(14,0,9,56),(15,0,10,57),(46,1,7,72),(47,0,18,72),(48,0,14,72),(49,1,5,73),(50,0,16,73),(51,1,12,73),(52,0,6,74),(53,0,17,74),(54,0,13,74),(55,0,4,75),(56,2,15,75),(57,0,11,75),(58,0,10,76),(59,0,8,77),(60,0,9,78);
/*!40000 ALTER TABLE `categories_absences_counts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expenditure_category_counts`
--

DROP TABLE IF EXISTS `expenditure_category_counts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenditure_category_counts` (
  `id_expenditure_category_count` int(11) NOT NULL AUTO_INCREMENT,
  `count_category` int(11) DEFAULT NULL,
  `id_expenditure` int(11) NOT NULL,
  `fk_id_category` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_expenditure_category_count`),
  KEY `ecc_1` (`fk_id_category`),
  CONSTRAINT `ecc_1` FOREIGN KEY (`fk_id_category`) REFERENCES `categories` (`id_category`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expenditure_category_counts`
--

LOCK TABLES `expenditure_category_counts` WRITE;
/*!40000 ALTER TABLE `expenditure_category_counts` DISABLE KEYS */;
INSERT INTO `expenditure_category_counts` VALUES (9,5,3,8),(10,25,3,6),(11,30,3,5),(12,100,3,7),(13,5,3,11),(14,10,3,9),(15,5,3,10),(16,29,4,5),(17,4,4,8),(18,24,4,6),(19,99,4,7),(20,9,4,9),(21,4,4,11),(22,4,4,10),(23,30,5,6),(24,25,5,5),(25,5,5,8),(26,110,5,7),(27,15,5,10),(28,10,5,11),(29,25,5,9),(30,24,6,5),(31,4,6,8),(32,29,6,6),(33,109,6,7),(34,14,6,10),(35,9,6,11),(36,24,6,9),(37,5,7,8),(38,25,7,5),(39,35,7,6),(40,120,7,7),(41,10,7,10),(42,15,7,9),(43,10,7,11),(44,24,8,5),(45,34,8,6),(46,4,8,8),(47,119,8,7),(48,9,8,11),(49,14,8,9),(50,9,8,10),(51,24,9,6),(52,4,9,8),(53,29,9,5),(54,99,9,7),(55,9,9,9),(56,4,9,10),(57,4,9,11),(72,3,12,8),(73,27,12,6),(74,109,12,7),(75,22,12,5),(76,9,12,11),(77,24,12,9),(78,14,12,10);
/*!40000 ALTER TABLE `expenditure_category_counts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expenditures`
--

DROP TABLE IF EXISTS `expenditures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenditures` (
  `id_expenditure` int(11) NOT NULL AUTO_INCREMENT,
  `date_and_time` datetime(6) DEFAULT NULL,
  `name_subdivision` varchar(255) DEFAULT NULL,
  `type_expenditure` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_expenditure`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expenditures`
--

LOCK TABLES `expenditures` WRITE;
/*!40000 ALTER TABLE `expenditures` DISABLE KEYS */;
INSERT INTO `expenditures` VALUES (3,NULL,'Подразделение1','state'),(4,NULL,'Подразделение1','list'),(5,NULL,'Подразделение2','state'),(6,NULL,'Подразделение2','list'),(7,NULL,'Подразделение11','state'),(8,NULL,'Подразделение11','list'),(9,'2020-03-27 11:32:21.259000','Подразделение1','current'),(12,'2020-03-27 11:34:17.572000','Подразделение2','current');
/*!40000 ALTER TABLE `expenditures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subdivisions`
--

DROP TABLE IF EXISTS `subdivisions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subdivisions` (
  `id_subdivision` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `fk_id_subdivision` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_subdivision`),
  UNIQUE KEY `s_1` (`name`),
  KEY `s_2` (`fk_id_subdivision`),
  CONSTRAINT `s_2` FOREIGN KEY (`fk_id_subdivision`) REFERENCES `subdivisions` (`id_subdivision`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subdivisions`
--

LOCK TABLES `subdivisions` WRITE;
/*!40000 ALTER TABLE `subdivisions` DISABLE KEYS */;
INSERT INTO `subdivisions` VALUES (1,'Главное',NULL),(2,'Подразделение1',1),(3,'Подразделение2',1),(4,'Подразделение11',2);
/*!40000 ALTER TABLE `subdivisions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `titles`
--

DROP TABLE IF EXISTS `titles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `titles` (
  `id_title` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_title`),
  UNIQUE KEY `t_1` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `titles`
--

LOCK TABLES `titles` WRITE;
/*!40000 ALTER TABLE `titles` DISABLE KEYS */;
INSERT INTO `titles` VALUES (1,'личный состав'),(2,'техника');
/*!40000 ALTER TABLE `titles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role_system` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `u_1` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'od','123','admin'),(2,'p1_dej','123','user'),(3,'p1_kom','123','user'),(4,'p1_nsh','123','user'),(5,'p2_dej','123','user'),(6,'p11_dej','123','user');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_subdivisions`
--

DROP TABLE IF EXISTS `users_subdivisions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_subdivisions` (
  `id_user_subdivision` int(11) NOT NULL AUTO_INCREMENT,
  `is_edit` bit(1) DEFAULT NULL,
  `fk_id_subdivision` int(11) DEFAULT NULL,
  `fk_id_user` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_user_subdivision`),
  UNIQUE KEY `us_1` (`fk_id_user`,`fk_id_subdivision`),
  KEY `us_3` (`fk_id_subdivision`),
  CONSTRAINT `us_2` FOREIGN KEY (`fk_id_user`) REFERENCES `users` (`id_user`),
  CONSTRAINT `us_3` FOREIGN KEY (`fk_id_subdivision`) REFERENCES `subdivisions` (`id_subdivision`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_subdivisions`
--

LOCK TABLES `users_subdivisions` WRITE;
/*!40000 ALTER TABLE `users_subdivisions` DISABLE KEYS */;
INSERT INTO `users_subdivisions` VALUES (6,_binary '',1,1),(7,_binary '',2,1),(8,_binary '',3,1),(9,_binary '',4,1),(10,_binary '',2,2),(11,_binary '\0',2,3),(12,_binary '\0',2,4),(13,_binary '',3,5),(14,_binary '',4,6);
/*!40000 ALTER TABLE `users_subdivisions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'operationalservice'
--

--
-- Dumping routines for database 'operationalservice'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-03-27 14:39:59
