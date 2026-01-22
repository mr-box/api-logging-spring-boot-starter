package com.github.mrbox.apilogging.filter.impl;

import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于处理时间的后置过滤器实现。
 *
 * <p>支持基于请求处理时间来决定是否跳过日志记录。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>只记录慢请求（超过指定阈值）</li>
 *   <li>排除快速请求（低于指定阈值）</li>
 * </ul>
 *
 * @author Zwk
 */
public class ProcessingTimePostFilter implements PostFilter {

    private final long minProcessingTimeMs;
    private final long maxProcessingTimeMs;

    /**
     * 构造函数 - 设置处理时间范围
     *
     * @param minProcessingTimeMs 最小处理时间（毫秒），小于此值的请求将被跳过
     * @param maxProcessingTimeMs 最大处理时间（毫秒），大于此值的请求将被跳过，-1表示不限制
     */
    public ProcessingTimePostFilter(long minProcessingTimeMs, long maxProcessingTimeMs) {
        this.minProcessingTimeMs = minProcessingTimeMs;
        this.maxProcessingTimeMs = maxProcessingTimeMs;
    }

    /**
     * 构造函数 - 只记录慢请求
     *
     * @param slowRequestThresholdMs 慢请求阈值（毫秒），超过此值才记录日志
     */
    public ProcessingTimePostFilter(long slowRequestThresholdMs) {
        this.minProcessingTimeMs = slowRequestThresholdMs;
        this.maxProcessingTimeMs = -1;
    }

    @Override
    public boolean shouldSkipLogging(ProceedingJoinPoint joinPoint,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object result,
                                   Throwable exception,
                                   ApiLoggingDataContext loggingDataContext,
                                   long processingTimeMs) {

        return processingTimeMs < minProcessingTimeMs
                || (maxProcessingTimeMs > 0 && processingTimeMs > maxProcessingTimeMs);

    }

    @Override
    public int getOrder() {
        return 10; // 较低优先级，让其他过滤器先执行
    }
}
