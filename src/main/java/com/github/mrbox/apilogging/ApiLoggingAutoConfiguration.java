package com.github.mrbox.apilogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrbox.apilogging.filter.FilterManager;
import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.filter.PreFilter;
import com.github.mrbox.apilogging.formatter.DefaultLogContentFormatter;
import com.github.mrbox.apilogging.formatter.LogContentFormatter;
import com.github.mrbox.apilogging.logger.ApiLoggingLogger;
import com.github.mrbox.apilogging.logger.DefaultJsonLogger;
import com.github.mrbox.apilogging.trigger.DetailedLogTrigger;
import com.github.mrbox.apilogging.trigger.ExceptionDetailedLogTrigger;
import com.github.mrbox.apilogging.trigger.HeaderDetailedLogTrigger;
import com.github.mrbox.apilogging.trigger.HttpStatusCodeDetailedLogTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * 日志组件的Spring Boot自动配置类。
 *
 * @author Zwk
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ApiLoggingAspect.class)
@EnableConfigurationProperties(ApiLoggingProperties.class)
@ConditionalOnProperty(prefix = ApiLoggingAutoConfiguration.PREFIX, name = "enabled", havingValue = "true")
public class ApiLoggingAutoConfiguration {
    public static final String PREFIX = "mr-box.api-logging";

    private final ApiLoggingProperties properties;

    public ApiLoggingAutoConfiguration(ApiLoggingProperties properties) {
        this.properties = properties;

        String ver = "";
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/META-INF/maven/com.github.mr-box/api-logging-spring-boot-starter/pom.properties")) {
            props.load(in);
            ver = props.getProperty("version");
        } catch (Exception e) {
            ver = "Unknown";
        }

        String text = "   >>>  API Logging is ENABLED  <<<   ";
        int width = text.length();

        String finalVer = "v" + ver;
        String vLeft = strRepeat("=", (width - finalVer.length()) / 2);
        String vRight = strRepeat("=", width - finalVer.length() - vLeft.length());
        Banner banner = (environment, sourceClass, out) -> out.println(
                "\n"+ strRepeat("=", width)+"\n" +
                        text + "\n" +
                        vLeft  + finalVer + vRight + "\n"
        );

        banner.printBanner(null, null, System.out);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiLoggingLogger apiLoggingLogger(@Autowired(required = false) ObjectMapper objectMapper) {
        return new DefaultJsonLogger(objectMapper);
    }

    // --- 格式化器 Bean ---
    @Bean
    @ConditionalOnMissingBean
    public LogContentFormatter logContentFormatter(@Autowired(required = false) ObjectMapper objectMapper) {
        return new DefaultLogContentFormatter(objectMapper);
    }

    // --- 触发器 Beans ---
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

    // --- 过滤器管理器 Bean ---

    @Bean
    @ConditionalOnMissingBean
    public FilterManager filterManager(@Autowired(required = false) List<PreFilter> preFilters,
                                      @Autowired(required = false) List<PostFilter> postFilters) {
        return new FilterManager(preFilters, postFilters);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiLoggingAspect apiLoggingAspect(
            List<DetailedLogTrigger> detailedLogTriggers,
            ApiLoggingLogger apiLoggingLogger,
            LogContentFormatter logContentFormatter,
            FilterManager filterManager) {

        return new ApiLoggingAspect(properties, detailedLogTriggers, apiLoggingLogger, logContentFormatter, filterManager);
    }

    /**
     * 重复字符串
     *
     * @param str
     * @param count
     * @return
     */
    private static String strRepeat(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
