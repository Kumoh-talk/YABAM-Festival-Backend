-- ./docker/mysql/initdb/01-create-authdb.sql
CREATE DATABASE IF NOT EXISTS `local_authdb`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
