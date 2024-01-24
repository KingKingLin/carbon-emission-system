-- 创建用户表 => 储存用户的用户名、密码和邮箱 => 邮箱为 id
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT AUTO_INCREMENT COMMENT '主键',
  `mail` VARCHAR(50) NOT NULL UNIQUE COMMENT '邮箱',
  `name` VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(32) NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '用户表';

-- INSERT INTO `user`(`mail`, `name`, `password`)
-- VALUES('827543964@qq.com', 'admin', 'admin');

INSERT INTO `user`(`mail`, `name`, `password`)
VALUES('827543964@qq.com', 'admin', '1f15d405949c4ed3c2279aa6d48b1e9a');

-- 创建用户附加信息表 => 储存用户的性别、身份[学生、单身等等]、居住地、联系地址
DROP TABLE IF EXISTS `info`;
CREATE TABLE IF NOT EXISTS `info` (
  `id` BIGINT COMMENT '主键', -- 用雪花算法生成
  `sex` ENUM('男', '女', '未知') DEFAULT '未知' COMMENT '性别',
  `userid` INT UNIQUE COMMENT '用户ID',
  `city` VARCHAR(20) COMMENT '居住地',
  `info` VARCHAR(100) COMMENT '备注', -- 存放的是列表的 json 字符串
  `address` VARCHAR(50) COMMENT '联系地址',
  `createtime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`userid`) REFERENCES `user`(`id`),
  PRIMARY KEY (`id`)
) ENGINE = INNODB
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '用户附加信息';

-- 用户表 => 这里就专指管理员
--  本系统不会提供有关该表的相关操作 => 只有一个管理员 admin, admin
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `username` VARCHAR(10) NOT NULL COMMENT '用户名',
  `password` VARCHAR(20) NOT NULL COMMENT '密码',
  PRIMARY KEY (`username`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '管理员表';

INSERT INTO `user`(`username`, `password`)
VALUES ('admin', 'admin');


