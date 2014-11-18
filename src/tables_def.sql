DROP TABLE IF EXISTS `vk_accounts`;
CREATE TABLE `vk_accounts` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uid` bigint unsigned NOT NULL,
  `nickname` varchar (10) NOT NULL,
  `password` varchar (10) NOT NULL,
  `account_type` varchar(10) NOT NULL,
  `stamp` bigint unsigned NOT NULL,
  `balance` bigint NOT NULL,
  PRIMARY KEY `id` (`id`),
  UNIQUE KEY `account_id` (`uid`, `nickname`),
  KEY `password` (`password`),
  KEY `account_type` (`account_type`),
  KEY `stamp` (`stamp`),
  KEY `balance` (`balance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `vk_albums`;
CREATE TABLE `vk_albums` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uid` bigint unsigned NOT NULL,
  `gid` bigint unsigned NOT NULL,
  `aid` bigint unsigned NOT NULL,
  `in_process` boolean NOT NULL,
  `stamp` bigint unsigned NOT NULL,
  PRIMARY KEY `id` (`id`),
  UNIQUE KEY `unkey` (`uid`, `gid`, `aid`),
  KEY `in_process` (`in_process`),
  KEY `stamp` (`stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `vk_album_types`;
CREATE TABLE `vk_album_types` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_album_id` bigint unsigned NOT NULL, # target id from vk_albums
  `type` varchar(10) NOT NULL,
  `access` boolean NOT NULL, # true - use items with this type, false - not
  PRIMARY KEY `id` (`id`),
  UNIQUE KEY `unkey` (`user_album_id`, `type`),
  KEY `access` (`access`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `vk_album_subtypes`;
CREATE TABLE `vk_album_subtypes` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_album_id` bigint unsigned NOT NULL, # target id from vk_albums
  `subtype` varchar(10) NOT NULL,
  `access` boolean NOT NULL, # true - use items with this type, false - not
  PRIMARY KEY `id` (`id`),
  UNIQUE KEY `unkey` (`user_album_id`, `subtype`),
  KEY `access` (`access`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `vk_items`;
CREATE TABLE `vk_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint unsigned NOT NULL, # target id from vk_accounts
  `type` varchar(10) NOT NULL,
  `subtype` varchar(10) NOT NULL,
  `description` varchar(100) NOT NULL,
  `stamp` bigint unsigned NOT NULL,
  PRIMARY KEY `id` (`id`),
  KEY `account_id` (`account_id`),
  KEY `item_type` (`item_type`),
  KEY `item_subtype` (`item_subtype`),
  KEY `description` (`description`),
  KEY `stamp` (`stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;