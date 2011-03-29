-- MySQL dump 10.11
--
-- Host: localhost    Database: jbpmtest
-- ------------------------------------------------------
-- Server version	5.0.67

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `JBPM_ID_USER`
--

DROP TABLE IF EXISTS `JBPM_ID_USER`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `JBPM_ID_USER` (
  `ID_` bigint(20) NOT NULL auto_increment,
  `CLASS_` char(1) NOT NULL,
  `NAME_` varchar(255) default NULL,
  `EMAIL_` varchar(255) default NULL,
  `PASSWORD_` varchar(255) default NULL,
  PRIMARY KEY  (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `JBPM_ID_USER`
--

LOCK TABLES `JBPM_ID_USER` WRITE;
/*!40000 ALTER TABLE `JBPM_ID_USER` DISABLE KEYS */;
INSERT INTO `JBPM_ID_USER` VALUES (3,'U','admin','','admin'),(4,'U','manager','','manager'),(5,'U','fred','','fred'),(6,'U','mary','','mary');
/*!40000 ALTER TABLE `JBPM_ID_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `JBPM_ID_GROUP`
--

DROP TABLE IF EXISTS `JBPM_ID_GROUP`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `JBPM_ID_GROUP` (
  `ID_` bigint(20) NOT NULL auto_increment,
  `CLASS_` char(1) NOT NULL,
  `NAME_` varchar(255) default NULL,
  `TYPE_` varchar(255) default NULL,
  `PARENT_` bigint(20) default NULL,
  PRIMARY KEY  (`ID_`),
  KEY `FK_ID_GRP_PARENT` (`PARENT_`),
  CONSTRAINT `FK_ID_GRP_PARENT` FOREIGN KEY (`PARENT_`) REFERENCES `jbpm_id_group` (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `JBPM_ID_GROUP`
--

LOCK TABLES `JBPM_ID_GROUP` WRITE;
/*!40000 ALTER TABLE `JBPM_ID_GROUP` DISABLE KEYS */;
INSERT INTO `JBPM_ID_GROUP` VALUES (7,'','admin','security-role',NULL),(8,'','user','security-role',NULL),(9,'','manager','security-role',NULL),(10,'','manager','organisation',NULL),(11,'','user','organisation',NULL),(12,'G','sales','organisation',NULL),(13,'G','hr','organisation',NULL);
/*!40000 ALTER TABLE `JBPM_ID_GROUP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `JBPM_ID_MEMBERSHIP`
--

DROP TABLE IF EXISTS `JBPM_ID_MEMBERSHIP`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `JBPM_ID_MEMBERSHIP` (
  `ID_` bigint(20) NOT NULL auto_increment,
  `CLASS_` char(1) NOT NULL,
  `NAME_` varchar(255) default NULL,
  `ROLE_` varchar(255) default NULL,
  `USER_` bigint(20) default NULL,
  `GROUP_` bigint(20) default NULL,
  PRIMARY KEY  (`ID_`),
  KEY `FK_ID_MEMSHIP_GRP` (`GROUP_`),
  KEY `FK_ID_MEMSHIP_USR` (`USER_`),
  CONSTRAINT `FK_ID_MEMSHIP_USR` FOREIGN KEY (`USER_`) REFERENCES `jbpm_id_user` (`ID_`),
  CONSTRAINT `FK_ID_MEMSHIP_GRP` FOREIGN KEY (`GROUP_`) REFERENCES `jbpm_id_group` (`ID_`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `JBPM_ID_MEMBERSHIP`
--

LOCK TABLES `JBPM_ID_MEMBERSHIP` WRITE;
/*!40000 ALTER TABLE `JBPM_ID_MEMBERSHIP` DISABLE KEYS */;
INSERT INTO `JBPM_ID_MEMBERSHIP` VALUES (3,'M',NULL,NULL,3,7),(4,'M',NULL,NULL,3,9),(5,'M',NULL,NULL,3,8),(6,'M',NULL,NULL,3,10),(7,'M',NULL,NULL,4,9),(9,'M',NULL,NULL,4,8),(10,'M',NULL,NULL,4,13),(11,'M',NULL,NULL,4,11),(12,'M',NULL,NULL,5,11),(13,'M',NULL,NULL,5,8),(14,'',NULL,NULL,6,8),(15,'M',NULL,NULL,6,11);
/*!40000 ALTER TABLE `JBPM_ID_MEMBERSHIP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2008-11-20 10:39:38
