SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;


INSERT INTO `hashtags` (`id`, `text`) VALUES
(1, '#Gangsta');

INSERT INTO `search_terms` (`id`, `term`, `active`, `when_started`, `stepwidth`, `forward`, `priority`, `time_last_fetched`, `last_fetched_tweet_date`, `last_fetched_tweet_count`) VALUES
(1, 'Merkel', 1, '2013-11-18 20:39:43', '20:39:43', 1, 100, '2013-11-18 20:39:43', '2013-11-18 20:39:43', 100),
(2, 'Gabriel', 2, '2013-11-18 20:39:56', '20:39:56', 1, 100, '2013-11-18 20:39:56', '2013-11-18 20:39:56', 100),
(3, 'Gysi', 1, '2013-11-18 22:53:05', '01:00:00', 0, NULL, NULL, NULL, NULL);

INSERT INTO `tweets` (`id`, `coordinates_longitude`, `coordinates_latitude`, `users_id`, `is_retweet_of_id`, `created_at`, `source`, `text`, `iso_language_code`, `retweet_count`, `sentiment`, `is_reply_to_status_id`) VALUES
('57109', 0, 0, '1', NULL, '2013-11-18 20:37:50', NULL, 'Angela Merkel und Sigmar Gabriel erzielen Einigung in Koalitionsverhandlungen', 'de', 0, 0, NULL),
('57110', 0, 0, '1', NULL, '2013-11-18 20:37:50', NULL, 'Gabriel und Gysi sind voll die G''s! #Gangsta', 'de', 0, 0, NULL);

INSERT INTO `tweets_has_hashtags` (`tweets_id`, `hashtags_id`) VALUES
('57110', 1);

INSERT INTO `tweets_has_search_terms` (`tweets_id`, `search_terms_id`) VALUES
('57109', 1),
('57109', 2),
('57110', 2),
('57110', 3);

INSERT INTO `users` (`id`, `name`, `screen_name`, `profile_image_url`, `created_at`, `location`, `url`, `lang`, `followers_count`, `verified`, `time_zone`, `description`, `statuses_count`, `friends_count`) VALUES
('1', 'name', 'screen_name', 'www.url.com', '2013-11-18 20:36:02', 'Germany', 'www.url.com', 'de', 0, 1, 'GMT', 'descr', 0, 0);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
