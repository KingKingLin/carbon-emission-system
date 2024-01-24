# ces 省际能源排放数据表
CREATE TABLE IF NOT EXISTS `${tableName}` (
  `r_code` CHAR(6) NOT NULL COMMENT '省份代码',
  `value` DOUBLE DEFAULT 0 COMMENT '数据',
  FOREIGN KEY (`r_code`) REFERENCES `r_code`(`r_code`),
  PRIMARY KEY (`r_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = #{tableName};