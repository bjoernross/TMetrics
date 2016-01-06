DROP DATABASE IF EXISTS `resttest`;
CREATE DATABASE  IF NOT EXISTS `resttest` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `resttest`;
-- MySQL dump 10.13  Distrib 5.5.34, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: tmetrics
-- ------------------------------------------------------
-- Server version	5.6.14

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
-- Table structure for table `hashtags`
--

DROP TABLE IF EXISTS `hashtags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hashtags` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `text` varchar(140) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `text_UNIQUE` (`text`)
) ENGINE=InnoDB AUTO_INCREMENT=867 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hashtags`
--

LOCK TABLES `hashtags` WRITE;
/*!40000 ALTER TABLE `hashtags` DISABLE KEYS */;
INSERT INTO `hashtags` VALUES (5,'Datum'),(6,'Dummy'),(1,'hashtag1'),(2,'hashtag2'),(3,'hashtag3'),(4,'hashtag4');
/*!40000 ALTER TABLE `hashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mentions`
--

DROP TABLE IF EXISTS `mentions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mentions` (
  `tweets_id` varchar(140) NOT NULL,
  `users_id` varchar(45) NOT NULL,
  PRIMARY KEY (`tweets_id`,`users_id`),
  KEY `fk_tweets_has_users_tweets_idx` (`tweets_id`),
  CONSTRAINT `fk_tweets_has_users_tweets` FOREIGN KEY (`tweets_id`) REFERENCES `tweets` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mentions`
--

LOCK TABLES `mentions` WRITE;
/*!40000 ALTER TABLE `mentions` DISABLE KEYS */;
/*!40000 ALTER TABLE `mentions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_terms`
--

DROP TABLE IF EXISTS `search_terms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `search_terms` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `term` varchar(140) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `current_start` datetime NOT NULL,
  `old_start` datetime DEFAULT NULL,
  `interval_length` time DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `time_last_fetched` datetime DEFAULT NULL,
  `last_fetched_tweet_id` bigint(20) unsigned DEFAULT NULL,
  `last_fetched_tweet_count` tinyint(3) unsigned DEFAULT NULL,
  `when_created` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `term_UNIQUE` (`term`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_terms`
--

LOCK TABLES `search_terms` WRITE;
/*!40000 ALTER TABLE `search_terms` DISABLE KEYS */;
INSERT INTO `search_terms` VALUES (1,'Merkel',1,'2013-12-02 15:38:55',NULL,'00:30:00',1,'2013-12-04 16:15:35',3,NULL,'2013-10-01 13:37:42'),(2,'Meer',0,'0000-00-00 00:00:00',NULL,'00:15:00',-2,NULL,NULL,NULL,'2013-10-02 13:37:42'),(3,'Datum',1,'2013-12-02 16:40:50',NULL,'00:15:00',NULL,NULL,NULL,NULL,'2013-10-03 13:37:42'),(4,'Mehrwertsteuer',0,'2013-12-02 18:43:08',NULL,'00:15:00',NULL,NULL,NULL,NULL,'2013-10-04 13:37:42'),(5,'Count',1,'2013-12-03 15:46:25',NULL,'00:15:00',NULL,NULL,NULL,NULL,'2013-10-05 13:37:42'),(6,'CountZero',1,'2013-12-03 16:21:17',NULL,'00:15:00',NULL,NULL,NULL,NULL,'2013-10-06 13:37:42');
/*!40000 ALTER TABLE `search_terms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tweets`
--

DROP TABLE IF EXISTS `tweets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweets` (
  `id` varchar(140) NOT NULL,
  `coordinates_longitude` float DEFAULT NULL,
  `coordinates_latitude` float DEFAULT NULL,
  `users_id` varchar(140) NOT NULL,
  `is_retweet_of_id` varchar(140) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `source` tinytext,
  `text` varchar(280) NOT NULL,
  `iso_language_code` varchar(3) DEFAULT NULL,
  `retweet_count` int(11) NOT NULL,
  `sentiment` float DEFAULT NULL,
  `sentiment_human_label` float DEFAULT NULL,
  `is_reply_to_status_id` varchar(140) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tweets_users1_idx` (`users_id`),
  KEY `fk_tweets_tweets2_idx` (`is_retweet_of_id`),
  CONSTRAINT `fk_tweets_users1` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweets_tweets2` FOREIGN KEY (`is_retweet_of_id`) REFERENCES `tweets` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tweets`
--

LOCK TABLES `tweets` WRITE;
/*!40000 ALTER TABLE `tweets` DISABLE KEYS */;
INSERT INTO `tweets` VALUES ('1',69.641,18.9413,'1',NULL,'2013-12-02 16:04:29','www.url.com','Merkel','de',2,0,0,NULL),('10',0,0,'1',NULL,'2013-01-01 11:00:00','www.url.com','Counts','en',0,1,0,NULL),('11',0,0,'1',NULL,'2013-01-01 12:00:00','www.url.com','Counts','en',0,1,0,NULL),('12',0,0,'1',NULL,'2013-01-01 12:00:00','www.url.com','Counts','de',0,1,0,NULL),('13',0,0,'1',NULL,'2013-01-01 12:00:00','www.url.com','CountsZero','de',0,1,0,NULL),('14',0,0,'1',NULL,'2013-01-01 15:00:00','www.url.com','CountsZero','de',0,1,0,NULL),('15',0,0,'1',NULL,'2014-01-01 12:00:00','www.url.com','I SUPPOSE I SHOULD TELL YOU WHAT THIS BITCH IS THINKING mein name ist angela merkel & ich halte nun meine neujahrsansprache. yo, bitch.','en',0,1,0,NULL),('16',0,0,'1',NULL,'2014-01-01 12:00:00','www.url.com','Eine Frage betreffend der Urheberrechte an der Neujahrsansprache Herr @RegSprecher Wer hat die Ansprache verfasst. Frau Merkel selbst?','de',0,1,0,NULL),('17',0,0,'1',NULL,'2014-01-01 12:00:00','www.url.com','Grad Merkel?s Neujahrsansprache nachgeholt: Boa, immer diese abwartende Haltung! Wie in der Sauna, blo? kein Schwei? aufs Holz!','de',0,1,0,NULL),('2',0,0,'1',NULL,'2013-12-02 16:07:04','www.url.com','Merkel','de',0,1,0,NULL),('3',0,0,'1',NULL,'2013-12-02 16:07:11','www.url.com','Merkel','de',1,1,0,NULL),('4',0,0,'1',NULL,'2013-01-01 00:00:00','www.url.com','Datum','de',0,1,0,NULL),('5',0,0,'1',NULL,'2013-01-02 00:00:00','www.url.com','Datum','de',0,1,0,NULL),('6',0,0,'1',NULL,'2013-12-02 18:44:12','www.url.com','Mehrwertsteuer','de',0,1,0,NULL),('7',0,0,'1',NULL,'2013-01-01 12:00:00','www.url.com','Counts1','de',1,1,0,NULL),('8',0,0,'1',NULL,'2013-01-01 12:00:00','www.url.com','Counts','de',0,1,0,NULL),('9',0,0,'1',NULL,'2013-01-01 10:00:00','www.url.com','Counts','de',0,1,0,NULL);
/*!40000 ALTER TABLE `tweets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tweets_has_hashtags`
--

DROP TABLE IF EXISTS `tweets_has_hashtags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweets_has_hashtags` (
  `tweets_id` varchar(140) NOT NULL,
  `hashtags_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`tweets_id`,`hashtags_id`),
  KEY `fk_tweets_has_hashtags_hashtags1_idx` (`hashtags_id`),
  KEY `fk_tweets_has_hashtags_tweets1_idx` (`tweets_id`),
  CONSTRAINT `fk_tweets_has_hashtags_tweets1` FOREIGN KEY (`tweets_id`) REFERENCES `tweets` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweets_has_hashtags_hashtags1` FOREIGN KEY (`hashtags_id`) REFERENCES `hashtags` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tweets_has_hashtags`
--

LOCK TABLES `tweets_has_hashtags` WRITE;
/*!40000 ALTER TABLE `tweets_has_hashtags` DISABLE KEYS */;
INSERT INTO `tweets_has_hashtags` VALUES ('1',1),('2',1),('3',1),('1',2),('3',2),('1',3),('4',5),('5',5),('4',6);
/*!40000 ALTER TABLE `tweets_has_hashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tweets_has_search_terms`
--

DROP TABLE IF EXISTS `tweets_has_search_terms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweets_has_search_terms` (
  `tweets_id` varchar(140) NOT NULL,
  `search_terms_id` int(10) unsigned NOT NULL,
  `iso_language_code` varchar(3) DEFAULT NULL,
  `sentiment` float DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_retweet_of_id` bigint(20) unsigned DEFAULT NULL,
  `retweet_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`tweets_id`,`search_terms_id`),
  KEY `fk_tweets_has_search_terms_search_terms1_idx` (`search_terms_id`),
  KEY `fk_tweets_has_search_terms_tweets1_idx` (`tweets_id`),
  CONSTRAINT `fk_tweets_has_search_terms_search_terms1` FOREIGN KEY (`search_terms_id`) REFERENCES `search_terms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweets_has_search_terms_tweets1` FOREIGN KEY (`tweets_id`) REFERENCES `tweets` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tweets_has_search_terms`
--

LOCK TABLES `tweets_has_search_terms` WRITE;
/*!40000 ALTER TABLE `tweets_has_search_terms` DISABLE KEYS */;
INSERT INTO `tweets_has_search_terms` VALUES ('1',1,'de',0,'2013-12-02 16:04:29',NULL,1),('10',5,'en',1,'2013-01-01 11:00:00',NULL,0),('11',5,'en',1,'2013-01-01 12:00:00',NULL,0),('12',5,'de',1,'2013-01-01 12:00:00',NULL,0),('13',6,'de',1,'2013-01-01 12:00:00',NULL,0),('14',6,'de',1,'2013-01-01 15:00:00',NULL,0),('15',1,'en',1,'2014-01-01 12:00:00',NULL,0),('16',1,'de',1,'2014-01-01 12:00:00',NULL,0),('17',1,'de',1,'2014-01-01 12:00:00',NULL,0),('2',1,'de',1,'2013-12-02 16:07:04',NULL,0),('3',1,'de',1,'2013-12-02 16:07:11',1,0),('4',3,'de',1,'2013-01-01 00:00:00',NULL,0),('5',3,'de',1,'2013-01-02 00:00:00',NULL,0),('6',4,'de',1,'2013-12-02 18:44:12',NULL,0),('7',5,'de',1,'2013-01-01 12:00:00',NULL,1),('8',5,'de',1,'2013-01-01 12:00:00',NULL,0),('9',5,'de',1,'2013-01-01 10:00:00',NULL,0);
/*!40000 ALTER TABLE `tweets_has_search_terms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` varchar(140) NOT NULL,
  `name` varchar(140) DEFAULT NULL,
  `screen_name` varchar(45) NOT NULL,
  `profile_image_url` tinytext,
  `created_at` datetime NOT NULL,
  `location` varchar(45) DEFAULT NULL,
  `url` tinytext,
  `lang` varchar(5) DEFAULT NULL,
  `followers_count` int(10) unsigned NOT NULL,
  `verified` tinyint(1) NOT NULL,
  `time_zone` varchar(45) DEFAULT NULL,
  `description` varchar(160) DEFAULT NULL,
  `statuses_count` int(10) unsigned NOT NULL,
  `friends_count` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('1','name','screen_name','image url','2013-12-02 15:42:24','de','url','de',1,1,'UTC','description',1,1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-03-04 19:13:28
