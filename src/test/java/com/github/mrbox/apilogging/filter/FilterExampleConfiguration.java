package com.github.mrbox.apilogging.filter;

import com.github.mrbox.apilogging.filter.impl.MethodNamePreFilter;
import com.github.mrbox.apilogging.filter.impl.ProcessingTimePostFilter;
import com.github.mrbox.apilogging.filter.impl.RequestHeaderPreFilter;
import com.github.mrbox.apilogging.filter.impl.UriPatternPreFilter;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 过滤器使用示例配置类
 *
 * <p>演示如何配置和使用各种过滤器来控制日志记录行为</p>
 *
 * @author Zwk
 */
//影响单元测试，实际需要添加 @Configuration
public class FilterExampleConfiguration {

    /**
     * URI模式过滤器示例
     * 排除健康检查、监控和静态资源接口
     */
    @Bean
    public UriPatternPreFilter uriPatternPreFilter() {
        Set<String> excludePatterns = new HashSet<String>(){{
            add("/health");
            add("/actuator/**");
            add("/metrics/**");
            add("/prometheus/**");
            add("/static/**");
            add("/assets/**");
            add("/favicon.ico");
        }};
        return new UriPatternPreFilter(excludePatterns);
    }

    /**
     * 请求头过滤器示例
     * 排除指定客户端调用和监控调用
     */
    @Bean
    public RequestHeaderPreFilter requestHeaderPreFilter() {
        Map<String, String> excludeHeaders = new HashMap<String, String>(){{
            put("X-Client-Type", "IOS");    // IOS客户端
            put("X-Monitor", "true");       // 监控调用
            put("X-Health-Check", "true");  // 健康检查
            put("User-Agent", "kube-probe");    // K8s探针
        }};
        return new RequestHeaderPreFilter(excludeHeaders);
    }

    /**
     * 方法名过滤器示例
     * 排除特定Controller和方法
     */
    @Bean
    public MethodNamePreFilter methodNamePreFilter() {
        Set<String> excludeClasses = new HashSet<String>(){{
            add("HealthController"); // 健康检查控制器
            add("MonitorController"); // 监控控制器
            add("MetricsController"); // 指标控制器
        }};
        Set<String> excludeMethods  = new HashSet<String>(){{
            add("health");  // 健康检查方法
            add("ping");    // ping方法
            add("status");  // 状态检查方法
            add("heartbeat"); // 心跳方法
        }};

        return new MethodNamePreFilter(excludeClasses, excludeMethods);
    }


    /**
     * 处理时间过滤器示例1：只记录慢请求
     */
    @Bean
    public ProcessingTimePostFilter slowRequestProcessingTimePostFilter() {
        // 只记录超过1秒的慢请求
        return new ProcessingTimePostFilter(1000);
    }

    /**
     * 处理时间过滤器示例2：记录特定时间范围的请求
     */
    @Bean
    public ProcessingTimePostFilter rangeProcessingTimePostFilter() {
        // 只记录100ms到5秒之间的请求
        return new ProcessingTimePostFilter(100, 5000);
    }

    /**
     * 自定义前置过滤器示例
     * 基于业务逻辑的复杂过滤
     */
    @Bean
    public PreFilter customBusinessPreFilter() {
        return new PreFilter() {
            @Override
            public String name() {
                return "customBusiness";
            }

            @Override
            public boolean shouldSkipLogging(org.aspectj.lang.ProceedingJoinPoint joinPoint,
                                           javax.servlet.http.HttpServletRequest request) {
                if (request == null) {
                    return false;
                }

                // 示例：排除测试环境的特定用户请求
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.contains("TestClient")) {
                    return true;
                }

                // 示例：排除特定参数的请求
                String skipLog = request.getParameter("skipLog");
                if ("true".equalsIgnoreCase(skipLog)) {
                    return true;
                }

                return false;
            }

            @Override
            public int getOrder() {
                return -50; // 中等优先级
            }
        };
    }

    /**
     * 自定义后置过滤器示例
     * 基于响应内容的复杂过滤
     */
    @Bean
    public PostFilter customBusinessPostFilter() {
        return new PostFilter() {
            @Override
            public String name() {
                return "customBusinessPost";
            }

            @Override
            public boolean shouldSkipLogging(org.aspectj.lang.ProceedingJoinPoint joinPoint,
                                           javax.servlet.http.HttpServletRequest request,
                                           javax.servlet.http.HttpServletResponse response,
                                           Object result,
                                           Throwable exception,
                                           ApiLoggingDataContext loggingDataContext,
                                           long processingTimeMs) {

                // 示例：排除空响应
                if (result == null) {
                    return true;
                }

                // 示例：排除特定类型的响应
                if (result instanceof org.springframework.http.ResponseEntity) {
                    org.springframework.http.ResponseEntity<?> responseEntity =
                        (org.springframework.http.ResponseEntity<?>) result;
                    Object body = responseEntity.getBody();

                    // 排除空body的响应
                    if (body == null) {
                        return true;
                    }

                    // 排除特定类型的响应体
                    if (body instanceof String && ((String) body).isEmpty()) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public int getOrder() {
                return 5; // 较低优先级
            }
        };
    }
}
