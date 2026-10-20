package com.littlecase.myblog.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * <p>
 * 重要：sa-token-spring-boot3-starter 只注册上下文，**不会**自动开启注解鉴权。
 * 必须手动注册 {@link SaInterceptor}，@SaCheckLogin / @SaCheckRole 才会生效。
 * 这里只注册无参构造（仅做注解校验），不做全局路由拦截，
 * 因此前台公开接口（/api/article/page、/api/category/list 等）无需登录即可访问。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/api/**");
    }
}
