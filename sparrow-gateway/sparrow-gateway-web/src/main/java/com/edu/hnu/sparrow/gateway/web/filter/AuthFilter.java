package com.edu.hnu.sparrow.gateway.web.filter;



import com.edu.hnu.sparrow.gateway.web.exception.NoLogException;
import com.edu.hnu.sparrow.gateway.web.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_URL="http://localhost:8001/api/oauth/toLogin";

    @Autowired
    private AuthService authService;

    /**
     * 实现第一个方法 拦截
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
//        HttpServletResponse response = (HttpServletResponse)exchange.getResponse();

        //课程中这里获取令牌尝试从多个地方获取了，当然你就从cookie获取也行
        //从多个地方获取只不过是多加了if/else
        //1.判断当前请求路径是否为登录请求,如果是,则直接放行
        String path = request.getURI().getPath();
        if ("/api/oauth/login".equals(path) || !UrlFilter.hasAuthorize(path)){
            //直接放行
            return chain.filter(exchange);
        }

        //2.从cookie中获取jti的值,如果该值不存在,拒绝本次访问
        String jti = authService.getJtiFromCookie(request);
        if (StringUtils.isEmpty(jti)){
            throw new NoLogException();
        }

        //3.从redis中获取jwt的值,如果该值不存在,拒绝本次访问
        String jwt = authService.getJwtFromRedis(jti);
        //这里为啥不能直接为null呢？
        if (StringUtils.isEmpty(jwt)){
            throw new NoLogException();
        }

        //4.对当前的请求对象进行增强,让它会携带令牌的信息
        request.mutate().header("Authorization",jwt);
        return chain.filter(exchange);
    }


    /**
     * 实现第二个方法排序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
