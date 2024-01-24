# 初始化数据库
# 准备 reg code 表和 energy code 表
# ces 原碳排放数据表
CREATE TABLE IF NOT EXISTS `r_code` (
  `r_code` CHAR(6) NOT NULL COMMENT '主键',
  `r_name` VARCHAR(20) COMMENT '名称',
  PRIMARY KEY (`r_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '省份代码';

CREATE TABLE IF NOT EXISTS `e_code` (
   `e_code` CHAR(7) NOT NULL COMMENT '主键',
   `e_name` VARCHAR(20) COMMENT '名称',
  PRIMARY KEY (`e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '能源代码';

# 判断 e_code 和 r_code 表是否初始化
DROP TABLE IF EXISTS `init`;
CREATE TABLE IF NOT EXISTS  `init` (
  `table_name` VARCHAR(15) NOT NULL COMMENT '主键',
  `isInit` TINYINT DEFAULT 0 COMMENT '是否初始化',
  PRIMARY KEY (`table_name`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '表是否初始化';

INSERT INTO `init` (`table_name`) VALUES ('r_code');

INSERT INTO `init` (`table_name`) VALUES ('e_code_ees');

INSERT INTO `init` (`table_name`) VALUES ('e_code_total');

# 需要等待 e_code 表初始化完毕后
# 准备碳排放因子表 Carbon Emission Factor => 扩展表
CREATE TABLE IF NOT EXISTS `cef` (
  `e_code` CHAR(7) NOT NULL COMMENT '主键',
  `cef` DOUBLE DEFAULT 0 COMMENT '碳排放因子',
  PRIMARY KEY (`e_code`),
  FOREIGN KEY (`e_code`) REFERENCES `e_code`(`e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '碳排放因子';

# 煤炭
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070601', 1.8605);
# 焦炭
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070602', 2.9898);
# 原油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070603', 3.0995);
# 汽油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070604', 3.0065);
# 煤油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070605', 3.0989);
# 柴油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070606', 3.1275);
# 燃料油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070607', 3.2769);
# 天然气
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070608', 2.1633);
# 电力
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070609', 5.8100);

# 创建 geo 表, 配置 China 的地图信息
CREATE TABLE IF NOT EXISTS  `geo` (
    `nation` VARCHAR(20) NOT NULL COMMENT '主键',
    `geo` LONGTEXT COMMENT '地图信息',
    PRIMARY KEY (`nation`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '地图信息';