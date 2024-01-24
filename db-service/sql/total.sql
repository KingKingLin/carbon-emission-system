# 国家总能源排放数据表
CREATE TABLE IF NOT EXISTS `${tableName}` (
    `e_code` CHAR(7) NOT NULL COMMENT '能源代码',
    `value` DOUBLE DEFAULT 0 COMMENT '数据',
    FOREIGN KEY (`e_code`) REFERENCES `e_code`(`e_code`),
    PRIMARY KEY (`e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = #{tableName};