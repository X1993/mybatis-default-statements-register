DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id int(11) PRIMARY KEY AUTO_INCREMENT,
  name varchar(6) DEFAULT NULL ,
  create_time datetime DEFAULT NULL ,
  update_time datetime DEFAULT NULL ,
  address varchar(50) DEFAULT NULL ,
  address2 varchar(50) DEFAULT NULL ,
  note varchar(100) DEFAULT NULL ,
  version int(255) DEFAULT NULL ,
  removed bit(1) DEFAULT 0 COMMENT '是否已删除，1：已删除'
);