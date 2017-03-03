--
-- Copyright 2017 ZTE Corporation.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
/******************drop old database and user***************************/
use mysql;
drop database IF  EXISTS holmes;
delete from user where User='holmes';
FLUSH PRIVILEGES;

/******************CREATE NEW DATABASE AND USER***************************/
create database holmes CHARACTER SET utf8;

GRANT ALL PRIVILEGES ON holmes.* TO 'holmes'@'%' IDENTIFIED BY 'holmes' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'holmes'@'%' IDENTIFIED BY 'holmes' WITH GRANT OPTION;

GRANT ALL PRIVILEGES ON holmes.* TO 'holmes'@'localhost' IDENTIFIED BY 'holmes' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'holmes'@'localhost' IDENTIFIED BY 'holmes' WITH GRANT OPTION;
FLUSH PRIVILEGES;

use holmes;
set Names 'utf8';
/******************DELETE OLD TABLE AND CREATE NEW***************************/
use holmes;
DROP TABLE IF EXISTS APLUS_RULE;
CREATE TABLE APLUS_RULE (
  RID VARCHAR(30) NOT NULL,
  NAME VARCHAR(150) NOT NULL,
  DESCRIPTION VARCHAR(4000) NULL,
  ENABLE INT(1) NOT NULL,
  TEMPLATEID INT(10) NOT NULL,
  ENGINEID VARCHAR(20)  NOT NULL,
  ENGINETYPE VARCHAR(20)  NOT NULL,
  CREATOR VARCHAR(20)  NOT NULL,
  CREATETIME DATETIME NOT NULL,
  UPDATOR VARCHAR(20)  NULL,
  UPDATETIME DATETIME NULL,
  PARAMS VARCHAR(4000) NULL,
  CONTENT VARCHAR(4000) NOT NULL,
  VENDOR VARCHAR(100)  NOT NULL,
  PACKAGE VARCHAR(255) NULL,
  PRIMARY KEY (RID),
  UNIQUE KEY NAME (NAME),
  KEY IDX_APLUS_RULE_ENABLE (ENABLE),
  KEY IDX_APLUS_RULE_TEMPLATEID (TEMPLATEID),
  KEY IDX_APLUS_RULE_ENGINEID (ENGINEID),
  KEY IDX_APLUS_RULE_ENGINETYPE (ENGINETYPE)
);

