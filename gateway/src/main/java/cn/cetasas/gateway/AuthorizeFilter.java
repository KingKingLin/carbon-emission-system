//package cn.cetasas.gateway;
//
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//// 全局过滤器
////@Order(-1) // 权重，值越小权重越大
//@Component
//public class AuthorizeFilter implements GlobalFilter, Ordered {
//    // Mono 是 WebFlux 的 api
//    /**
//     * 需求：定义全局过滤器，拦截请求，判断请求的参数是否满足下面条件：
//     *
//     * - 参数中是否有authorization，
//     *
//     * - authorization参数值是否为admin
//     *
//     * 如果同时满足则放行，否则拦截
//     */
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        // 1. 获取请求参数
//        ServerHttpRequest request = exchange.getRequest();
//        MultiValueMap<String, String> params = request.getQueryParams();
//        // 2. 获取参数中的 authorization 参数
//        String auth = params.getFirst("authorization");
//        // 3. 判断参数值是否等于 admin
//        if ("admin".equals(auth)) {
//            // 4. 是，放行
//            return chain.filter(exchange);
//        }
//        // 5. 否，拦截
//        // 5.1 设置状态码
//        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); // 401 是未登录
//        // 5.2 拦截请求
//        return exchange.getResponse().setComplete(); // 拦截
//    }
//
//    @Override
//    public int getOrder() {
//        return -1;
//    }
//}
///**
// * 请求进入网关会碰到三类过滤器：当前路由的过滤器、DefaultFilter、GlobalFilter
// * 请求路由后，会将当前路由过滤器和 DefaultFilter、GlobalFilter，合并到一个过滤器链（集合）中，“排序” 后依次执行每个过滤器
// * GlobalFilter 通过实现 Ordered 接口，或者添加 @Order 注解来指定 order 值，由我们自己指定
// * 路由过滤器和 DefaultFilter 的 order 由 Spring 指定，默认是按照声明顺序从 1 递增
// * -----------------------------
// * 当过滤器的 order 值一样时，会按照 DefaultFilter -> 路由过滤器 -> GlobalFilter 的顺序执行
// */
