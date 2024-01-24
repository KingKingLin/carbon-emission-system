SELECT `message`.`id`, `title`, `createtime`, `modifytime`, IF(`message`.`id` = `tmp`.`id`, 1, 0) AS `isread`
FROM `message`, (-- 用户表阅读表
                 SELECT `message`.`id` AS `id`
                 FROM `record`, `message`
                 WHERE `record`.`id` = `message`.`id` AND
                 `record`.`userid` = 123) AS `tmp`
WHERE `message`.`deleted` = FALSE;

SELECT `message`.`id`, `title`, `createtime`, `modifytime`, `message`.`id` = `tmp`.`id` AS `isread`
FROM `message`, (-- 用户表阅读表
                 SELECT `message`.`id` AS `id`
                 FROM `record`, `message`
                 WHERE `record`.`id` = `message`.`id` AND
                       `record`.`userid` = 123) AS `tmp`
WHERE `message`.`deleted` = FALSE;

SELECT `message`.`id`, `title`, `createtime`, `modifytime`, SELECT EXISTS ( SELECT 1
                                                                            FROM `message`, (-- 用户表阅读表
                                                                            SELECT `message`.`id` AS `id`
                                                                            FROM `record`, `message`
                                                                            WHERE `record`.`id` = `message`.`id` AND
                                                                            `record`.`userid` = 123) AS `tmp`
                                                                            WHERE `message`.`id` = `tmp`.id) AS `isread`
WHERE `message`.`deleted` = FALSE;

DECLARE userid 123
IF (-- 用户表阅读表
    SELECT EXISTS
    FROM `record`, `message`
    WHERE `record`.`id` = `message`.`id` AND
    `record`.`userid` = userid) THEN
SELECT `message`.`id`, `title`, `createtime`, `modifytime`, `message`.`id` = `tmp`.`id` AS `isread`
FROM `message`, (-- 用户表阅读表
                SELECT `message`.`id` AS `id`
                FROM `record`, `message`
                WHERE `record`.`id` = `message`.`id` AND
                      `record`.`userid` = userid) AS `tmp`
WHERE `message`.`deleted` = FALSE;
ELSE SELECT `message`.`id`, `title`, `createtime`, `modifytime`, `message`.`id` = `tmp`.`id` AS `isread`
     FROM `message`, (-- 用户表阅读表
                      SELECT `message`.`id` AS `id`
                      FROM `record`, `message`
                      WHERE `record`.`id` = `message`.`id` AND
                            `record`.`userid` = userid) AS `tmp`
     WHERE `message`.`deleted` = FALSE;

-- 用户表阅读表
SELECT `message`.`id` AS `id`
FROM `record`, `message`
WHERE `record`.`id` = `message`.`id` AND
        `record`.`userid` = 24

SELECT IFNULL((-- 用户表阅读表
                  SELECT `message`.`id` AS `id`
                  FROM `record`, `message`
                  WHERE `record`.`id` = `message`.`id` AND
                        `record`.`userid` = 123), NULL) AS `id`;

-- yes => 有 bug ifnull 超过两行就出问题
SELECT `message`.`id`, `title`, `createtime`, `modifytime`, `message`.`id` = `tmp`.`id` AS `isread`
FROM `message`, (SELECT IFNULL((-- 用户表阅读表
                                   SELECT `message`.`id` AS `id`
                                   FROM `record`, `message`
                                   WHERE `record`.`id` = `message`.`id` AND
                                         `record`.`userid` = 24), 0) AS `id`) AS `tmp`
WHERE `message`.`deleted` = FALSE;

-- 采取拆开来做的方法
-- 用户表阅读表
SELECT `message`.`id` AS `id`
FROM `record`, `message`
WHERE `record`.`id` = `message`.`id` AND
      `record`.`userid` = 8