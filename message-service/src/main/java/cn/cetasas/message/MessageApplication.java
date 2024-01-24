package cn.cetasas.message;

//import cn.cetasas.feign.clients.DBClient;
//import cn.cetasas.feign.config.DefaultFeignConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
//import org.springframework.session.FlushMode;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@MapperScan("cn.cetasas.message.mapper") // 扫描mybatis的mapper映射文件
//@EnableFeignClients(clients = DBClient.class, defaultConfiguration = DefaultFeignConfiguration.class)
//@EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE)
@SpringBootApplication
public class MessageApplication {
    private final static Logger LOG = LoggerFactory.getLogger(MessageApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MessageApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("ヾ(◍°∇°◍)ﾉﾞ\t启动成功!!");
        LOG.info("启动地址: http://127.0.0.1:{}", env.getProperty("server.port"));
    }
}