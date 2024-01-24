package cn.cetasas.db.config;

import cn.cetasas.db.mapper.*;
import cn.cetasas.db.util.CarbonEmissionStatistics;
import cn.cetasas.db.util.InsertBatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig{
    @Bean
    public CarbonEmissionStatistics carbonEmissionStatistics(RestTemplate restTemplate) {
        return new CarbonEmissionStatistics(restTemplate);
    }

    @Bean
    public InsertBatch insertBatch(CESMapper cesMapper, EESMapper eesMapper, TotalMapper totalMapper, RCodeMapper rCodeMapper, ECodeMapper eCodeMapper) {
        return new InsertBatch(cesMapper, eesMapper, totalMapper, rCodeMapper, eCodeMapper);
    }
}
