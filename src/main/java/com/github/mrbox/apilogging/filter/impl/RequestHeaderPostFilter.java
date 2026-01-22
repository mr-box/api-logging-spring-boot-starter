package com.github.mrbox.apilogging.filter.impl;

import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 基于请求头的后置过滤器实现。
 *
 * <p>支持基于请求头的值来决定是否跳过日志记录。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>排除指定客户端调用：X-Client-Type: IOS</li>
 *   <li>排除监控调用：X-Monitor: true</li>
 *   <li>排除特定客户端：User-Agent匹配特定值</li>
 *   <li>基于自定义标识排除</li>
 * </ul>
 *
 * @author Zwk
 */
public class RequestHeaderPostFilter implements PostFilter {

    private final Map<String, String> excludeHeaders;

    /**
     * 构造函数
     *
     * @param excludeHeaders 需要排除的请求头映射，key为请求头名称，value为匹配值（不区分大小写）
     */
    public RequestHeaderPostFilter(Map<String, String> excludeHeaders) {
        this.excludeHeaders = excludeHeaders;
    }

    @Override
    public boolean shouldSkipLogging(ProceedingJoinPoint joinPoint,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object result,
                                     Throwable exception,
                                     ApiLoggingDataContext loggingDataContext,
                                     long processingTimeMs) {
        if (request == null || excludeHeaders == null || excludeHeaders.isEmpty()) {
            return false;
        }

        // 检查是否匹配任何排除的请求头
        for (Map.Entry<String, String> entry : excludeHeaders.entrySet()) {
            String headerName = entry.getKey();
            String expectedValue = entry.getValue();

            if (!StringUtils.hasText(headerName) || !StringUtils.hasText(expectedValue)) {
                continue;
            }

            String actualValue = request.getHeader(headerName);
            if (actualValue != null && expectedValue.equalsIgnoreCase(actualValue.trim())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return -90; // 高优先级
    }
}
