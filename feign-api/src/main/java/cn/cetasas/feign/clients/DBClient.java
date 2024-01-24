package cn.cetasas.feign.clients;

import cn.cetasas.feign.pojo.CESResp;
import cn.cetasas.feign.pojo.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("dbservice") // 服务名称
public interface DBClient {
    @GetMapping("/ces/{year}")
    CommonResp<List<CESResp>> getAllProvince(@PathVariable("year") String year);
}
