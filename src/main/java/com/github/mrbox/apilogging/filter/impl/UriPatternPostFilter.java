package com.github.mrbox.apilogging.filter.impl;

import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Set;

/**
 * 基于URI模式的后置过滤器实现。
 *
 * <p>支持使用Ant风格的路径模式来匹配需要跳过日志记录的URI。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>排除特定业务接口</li>
 * </ul>
 *
 * @author Zwk
 */
public class UriPatternPostFilter implements PostFilter {

    private final Set<String> excludePatterns;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 构造函数
     *
     * @param excludePatterns 需要排除的URI模式列表
     */
    public UriPatternPostFilter(Set<String> excludePatterns) {
        this.excludePatterns = excludePatterns != null ? excludePatterns : Collections.emptySet();
    }

    @Override
    public boolean shouldSkipLogging(ProceedingJoinPoint joinPoint, HttpServletRequest request, HttpServletResponse response, Object result, Throwable exception, ApiLoggingDataContext loggingDataContext, long processingTimeMs) {
        if (request == null || CollectionUtils.isEmpty(excludePatterns)) {
            return false;
        }

        String requestUri = request.getRequestURI();
        if (requestUri == null) {
            return false;
        }

        // 检查是否匹配任何排除模式
        for (String pattern : excludePatterns) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，尽早过滤
    }

}
