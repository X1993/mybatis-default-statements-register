DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id int(11) PRIMARY KEY AUTO_INCREMENT,
  name varchar(6) DEFAULT NULL COMMENT '姓名',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  address varchar(50) DEFAULT NULL COMMENT '地址',
  address2 varchar(50) DEFAULT NULL COMMENT '地址2',
  note varchar(100) DEFAULT NULL COMMENT '备注',
  version int(255) DEFAULT NULL COMMENT '版本号',
  removed bit(1) DEFAULT 0 COMMENT '是否已删除，1：已删除'
) DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity6`;
CREATE TABLE `entity6` (
  `id` varchar(50) ,
  `id2` varchar(50) ,
  `or` varchar(30) DEFAULT NULL,
  `and` varchar(30) DEFAULT NULL,
  `like` varchar(30) DEFAULT NULL,
  `by` varchar(30) DEFAULT NULL,
  `by_and_like` varchar(30) DEFAULT NULL,
  `index` varchar(30) DEFAULT NULL,
  `lo_code` varchar(30) DEFAULT NULL,
  `removed` char(1) ,
  CONSTRAINT table_entity6_pk PRIMARY KEY (id, id2)
) DEFAULT CHARSET=utf8;