DROP TABLE IF EXISTS `record`;
-- 消息表
--  添加消息时，给所有在线的用户发送消息
DROP TABLE IF EXISTS `message`;
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL COMMENT '消息ID',
  `title` VARCHAR(50) NOT NULL COMMENT '标题',
  `content` MEDIUMTEXT NOT NULL COMMENT '内容', -- 富文本 -> html, 媒体文本
  `createtime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifytime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最近修改时间',
  `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '消息表';

-- 是否已读 => 记录表 ==> 存在 用户ID 则代表已读取，没有则代表该用户未读
DROP TABLE IF EXISTS `record`;
CREATE TABLE IF NOT EXISTS `record` (
  `id` BIGINT NOT NULL COMMENT '消息ID',
  `userid` INT NOT NULL COMMENT '用户ID', -- 来自 bysj_user 库的 user 表，由于两张表不在同一个库中，所以这里不能用外键索引
  FOREIGN KEY (`id`) REFERENCES `message`(`id`),
  PRIMARY KEY (`id`, `userid`) -- 联合主键索引
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '记录表';

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

-- 图片
DROP TABLE IF EXISTS `m_image`;
CREATE TABLE `m_image` (
  `id` BIGINT COMMENT '图片ID',
  `filename` VARCHAR(50) NOT NULL UNIQUE COMMENT '图片名',
  `content` MEDIUMBLOB NOT NULL COMMENT '图片二进制数据',
  `createtime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `suffix` VARCHAR(10) NOT NULL COMMENT '后缀名',
  PRIMARY KEY (`id`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '图片';

-- 视频
DROP TABLE IF EXISTS `m_video`;
CREATE TABLE `m_video` (
  `id` BIGINT COMMENT '视频ID',
  `filename` VARCHAR(50) NOT NULL UNIQUE COMMENT '视频名',
  `poster` VARCHAR(50) COMMENT '封面地址',
  `content` MEDIUMBLOB NOT NULL COMMENT '视频二进制数据',
  `createtime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `suffix` VARCHAR(10) NOT NULL COMMENT '后缀名',
  PRIMARY KEY (`id`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '视频';