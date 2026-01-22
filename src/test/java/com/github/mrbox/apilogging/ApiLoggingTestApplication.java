package com.github.mrbox.apilogging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * API日志测试应用程序
 */
@SpringBootApplication
@EnableConfigurationProperties(ApiLoggingProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true) // 启用CGLIB代理
public class ApiLoggingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiLoggingTestApplication.class, args);
    }
}
