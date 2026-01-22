package com.github.mrbox.apilogging;

import com.github.mrbox.apilogging.filter.FilterManager;
import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.filter.PreFilter;
import com.github.mrbox.apilogging.filter.impl.MethodNamePostFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 测试配置类，用于提供必要的Bean
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true) // 使用CGLIB代理，确保AOP正常工作
@EnableConfigurationProperties(ApiLoggingProperties.class)
public class FilterTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(FilterTestConfig.class);



    @Bean
    public MethodNamePostFilter methodNamePostFilter() {
        Set<String> excludeMethods = new HashSet<String>(){{
            add("health");
            add("testMethodFilter");
        }};
        return new MethodNamePostFilter(new HashSet<>(), excludeMethods);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterManager filterManager(@Autowired(required = false) List<PreFilter> preFilters,
                                      @Autowired(required = false) List<PostFilter> postFilters) {
        logger.info("创建FilterManager，前置过滤器数量: {}, 后置过滤器数量: {}",
                preFilters != null ? preFilters.size() : 0,
                postFilters != null ? postFilters.size() : 0);
        return new FilterManager(preFilters, postFilters);
    }

}
