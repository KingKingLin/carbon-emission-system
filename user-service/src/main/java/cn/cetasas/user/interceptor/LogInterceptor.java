package cn.cetasas.user.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器：Spring框架持有的，常用于登录校验，权限校验，请求日志打印
 */
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LogInterceptor.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印请求信息
        LOG.info("----------------LogInterceptor 开始---------------");
        // LOG.info("请求地址: {} {}", request.getRequestURL(), request.getMethod());
        // LOG.info("远程地址: {}", request.getRemoteAddr());

        long startTime = System.currentTimeMillis();
        request.setAttribute("requestStartTime", startTime);

        // OPTIONS请求不做校验,
        // 前后端分离的架构, 前端会发一个OPTIONS请求先做预检, 对预检请求不做校验
        if(request.getMethod().equalsIgnoreCase("OPTIONS")){
            return true;
        }

        String path = request.getRequestURL().toString();
        LOG.info("接口登录拦截：path：{}", path);

        // 获取 header 的 token 参数
        String token = request.getHeader("token");
        LOG.info("登录校验开始，token：{}", token);
        if (token == null || token.isEmpty()) {
            LOG.info( "token为空，请求被拦截" );
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
            return false;
        }
        // redis 对 token 这个参数的要求是有要求的
        String s = stringRedisTemplate.opsForValue().get(token);
        if (s == null) {
            LOG.warn( "token无效，请求被拦截" );
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
            return false;
        } else {
            LOG.info("已登录：{}", s);
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("requestStartTime");
        LOG.info("----------LogInterceptor 结束 耗时: {} ms----------", System.currentTimeMillis() - startTime);
    }
}
