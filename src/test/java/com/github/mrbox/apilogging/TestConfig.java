package com.github.mrbox.apilogging;

import com.github.mrbox.apilogging.filter.FilterManager;
import com.github.mrbox.apilogging.formatter.DefaultLogContentFormatter;
import com.github.mrbox.apilogging.formatter.LogContentFormatter;
import com.github.mrbox.apilogging.logger.ApiLoggingLogger;
import com.github.mrbox.apilogging.trigger.DetailedLogTrigger;
import com.github.mrbox.apilogging.trigger.ExceptionDetailedLogTrigger;
import com.github.mrbox.apilogging.trigger.HeaderDetailedLogTrigger;
import com.github.mrbox.apilogging.trigger.HttpStatusCodeDetailedLogTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 测试配置类，用于提供必要的Bean
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true) // 使用CGLIB代理，确保AOP正常工作
@EnableConfigurationProperties(ApiLoggingProperties.class)
public class TestConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    public LogContentFormatter logContentFormatter() {
        return new DefaultLogContentFormatter(null);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderDetailedLogTrigger headerDetailedLogTrigger() {
        return new HeaderDetailedLogTrigger();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpStatusCodeDetailedLogTrigger errorStatusCodeDetailedLogTrigger() {
        return new HttpStatusCodeDetailedLogTrigger();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExceptionDetailedLogTrigger exceptionDetailedLogTrigger() {
        return new ExceptionDetailedLogTrigger();
    }

    @Bean
    @Primary
    public ApiLoggingAspect apiLoggingAspect(
            ApiLoggingProperties properties,
            List<DetailedLogTrigger> detailedLogTriggers,
            ApiLoggingLogger apiLoggingLogger,
            LogContentFormatter logContentFormatter,
            FilterManager filterManager) {

        logger.info("创建ApiLoggingAspect，apiLoggingLogger={}, 是Mock对象: {}",
                apiLoggingLogger,
                apiLoggingLogger.getClass().getName().contains("MockitoMock"));

        return new ApiLoggingAspect(properties, detailedLogTriggers,
                apiLoggingLogger, logContentFormatter, filterManager);
    }
}
