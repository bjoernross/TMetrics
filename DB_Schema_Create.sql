SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `datamining` DEFAULT CHARACTER SET utf8mb4 ;
USE `datamining` ;

-- -----------------------------------------------------
-- Table `datamining`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`users` (
  `id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(140) NULL,
  `screen_name` VARCHAR(45) NOT NULL,
  `profile_image_url` TINYTEXT NULL,
  `created_at` DATETIME NOT NULL,
  `location` VARCHAR(45) NULL,
  `url` TINYTEXT NULL,
  `lang` VARCHAR(10) NULL,
  `followers_count` INT UNSIGNED NOT NULL,
  `verified` TINYINT(1) NOT NULL,
  `time_zone` VARCHAR(45) NULL,
  `description` VARCHAR(160) NULL,
  `statuses_count` INT UNSIGNED NOT NULL,
  `friends_count` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `datamining`.`tweets`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`tweets` (
  `id` BIGINT UNSIGNED NOT NULL,
  `coordinates_longitude` FLOAT NULL,
  `coordinates_latitude` FLOAT NULL,
  `users_id` BIGINT UNSIGNED NOT NULL,
  `is_retweet_of_id` BIGINT UNSIGNED NULL,
  `created_at` DATETIME NOT NULL,
  `source` TINYTEXT NULL,
  `text` VARCHAR(280) NOT NULL,
  `iso_language_code` VARCHAR(3) NULL,
  `retweet_count` INT NOT NULL,
  `sentiment` FLOAT NULL,
  `sentiment_human_label` FLOAT NULL,
  `is_reply_to_status_id` BIGINT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_tweets_users1_idx` (`users_id` ASC),
  INDEX `fk_tweets_tweets2_idx` (`is_retweet_of_id` ASC),
  INDEX `fk_tweets_tweets3_idx` (`iso_language_code` ASC),
  INDEX `fk_tweets_tweets4_idx` (`sentiment_human_label` ASC),
  INDEX `fk_tweets_tweets5_idx` (`retweet_count` ASC),
  INDEX `fk_tweets_tweets6_idx` (`created_at` ASC),
  CONSTRAINT `fk_tweets_users1`
    FOREIGN KEY (`users_id`)
    REFERENCES `datamining`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweets_tweets2`
    FOREIGN KEY (`is_retweet_of_id`)
    REFERENCES `datamining`.`tweets` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `datamining`.`hashtags`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`hashtags` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `text` VARCHAR(140) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `text_UNIQUE` (`text` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `datamining`.`search_terms`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`search_terms` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `term` VARCHAR(140) NOT NULL,
  `active` TINYINT(1) NOT NULL,
  `current_start` DATETIME NOT NULL,
  `old_start` DATETIME NULL,
  `interval_length` TIME NULL DEFAULT "00:15:00",
  `priority` INT NULL DEFAULT 0,
  `time_last_fetched` DATETIME NULL,
  `last_fetched_tweet_id` BIGINT UNSIGNED NULL,
  `last_fetched_tweet_count` INT NULL,
  `when_created` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `term_UNIQUE` (`term` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `datamining`.`mentions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`mentions` (
  `tweets_id` BIGINT UNSIGNED NOT NULL,
  `users_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`tweets_id`, `users_id`),
  INDEX `fk_tweets_has_users_tweets_idx` (`tweets_id` ASC),
  CONSTRAINT `fk_tweets_has_users_tweets`
    FOREIGN KEY (`tweets_id`)
    REFERENCES `datamining`.`tweets` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `datamining`.`tweets_has_hashtags`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`tweets_has_hashtags` (
  `tweets_id` BIGINT UNSIGNED NOT NULL,
  `hashtags_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`tweets_id`, `hashtags_id`),
  INDEX `fk_tweets_has_hashtags_hashtags1_idx` (`hashtags_id` ASC),
  INDEX `fk_tweets_has_hashtags_tweets1_idx` (`tweets_id` ASC),
  CONSTRAINT `fk_tweets_has_hashtags_tweets1`
    FOREIGN KEY (`tweets_id`)
    REFERENCES `datamining`.`tweets` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweets_has_hashtags_hashtags1`
    FOREIGN KEY (`hashtags_id`)
    REFERENCES `datamining`.`hashtags` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `datamining`.`tweets_has_search_terms`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `datamining`.`tweets_has_search_terms` (
  `tweets_id` BIGINT UNSIGNED NOT NULL,
  `search_terms_id` INT UNSIGNED NOT NULL,
  `iso_language_code` VARCHAR(3) NULL,
  `sentiment` FLOAT NULL,
  `created_at` DATETIME NULL,
  `is_retweet_of_id` BIGINT(20) NULL,
  `retweet_count` INT(11) NULL,
  PRIMARY KEY (`tweets_id`, `search_terms_id`),
  INDEX `fk_tweets_has_search_terms_search_terms1_idx` (`search_terms_id` ASC),
  INDEX `fk_tweets_has_search_terms_tweets1_idx` (`tweets_id` ASC),
  INDEX `fk_tweets_has_search_terms_iso_language_code_idx` (`iso_language_code` ASC),
  INDEX `fk_tweets_has_search_terms_sentiment` (`sentiment` ASC),
  INDEX `fk_tweets_has_search_terms_created_at` (`created_at` ASC),
  INDEX `fk_tweets_has_search_terms_is_retweet_of_id` (`is_retweet_of_id` ASC),
  INDEX `fk_tweets_has_search_terms_retweet_count` (`retweet_count` ASC),
  CONSTRAINT `fk_tweets_has_search_terms_tweets1`
    FOREIGN KEY (`tweets_id`)
    REFERENCES `datamining`.`tweets` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweets_has_search_terms_search_terms1`
    FOREIGN KEY (`search_terms_id`)
    REFERENCES `datamining`.`search_terms` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
