# 测试
select count(*) from information_schema.TABLES
where TABLE_SCHEMA = 'bysj' AND
      TABLE_NAME = '2019_CES';

-- drop table if exists `2019_CES`;

CREATE TABLE IF NOT EXISTS `2019_CES` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) COMMENT '名称',
primary key (`id`)
) ENGINE = Innodb
  default
  CHARSET = utf8mb4 COMMENT = '2019_CES';