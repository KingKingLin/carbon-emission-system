package cn.cetasas.message.utils;

import cn.cetasas.message.exception.BusinessException;
import cn.cetasas.message.exception.BusinessExceptionCode;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class NacosUtils {

    @Resource
    private NacosServiceManager nacosServiceManager;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    public String getGatewayAddress(String servername) {
        String res = null;
        try {
            NamingService namingService = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties());
            Instance instance = namingService.selectOneHealthyInstance(servername);

            res = instance.getIp() + ":" + instance.getPort();
            return res;
        } catch (NacosException e) {
            throw new BusinessException(BusinessExceptionCode.NOT_SUCH_SERVER);
        }
    }

}
