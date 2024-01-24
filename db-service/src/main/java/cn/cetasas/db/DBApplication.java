package cn.cetasas.db;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.session.FlushMode;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 项目初始化注意事项：
 * 1、执行 init.sql 中创建 e_code 和 r_code 表的相关 sql
 * 2、启动项目，访问 http://ip:port/db/ees/2019 初始化 r_code 和 e_code 表
 * 3、执行 init.sql 剩余 cef 的相关 sql（即，e_code 表的扩展表 => 碳排放系数）
 */
@EnableAsync                        // 启动异步任务
@EnableTransactionManagement        // 启动事务
@MapperScan("cn.cetasas.db.mapper") // 扫描mybatis的mapper映射文件
//@EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE)
@SpringBootApplication
public class DBApplication {
    private final static Logger LOG = LoggerFactory.getLogger(DBApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DBApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("ヾ(◍°∇°◍)ﾉﾞ\t启动成功!!");
        LOG.info("启动地址: http://127.0.0.1:{}", env.getProperty("server.port"));
    }
}
