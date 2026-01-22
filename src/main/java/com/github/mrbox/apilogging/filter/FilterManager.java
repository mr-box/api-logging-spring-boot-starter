package com.github.mrbox.apilogging.filter;

import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *     过滤器管理器，负责管理和执行前置、后置过滤器链。
 *
 *     该管理器采用责任链模式，按照过滤器的优先级顺序执行，
 *     支持短路操作（任一过滤器返回true即跳过日志记录）。
 * </pre>
 *
 * @author Zwk
 */
public class FilterManager {

    private static final Logger logger = LoggerFactory.getLogger(FilterManager.class);

    private final List<PreFilter> preFilters;
    private final List<PostFilter> postFilters;

    public FilterManager(List<PreFilter> preFilters, List<PostFilter> postFilters) {
        // 按优先级排序过滤器
        this.preFilters = sortFilters(preFilters);
        this.postFilters = sortFilters(postFilters);

        if (logger.isDebugEnabled()) {
            logger.debug("初始化FilterManager - 前置过滤器: {}, 后置过滤器: {}",
                    this.preFilters.size(), this.postFilters.size());

            this.preFilters.forEach(filter ->
                logger.debug("前置过滤器: {} (order: {})", filter.getClass().getSimpleName(), filter.getOrder()));
            this.postFilters.forEach(filter ->
                logger.debug("后置过滤器: {} (order: {})", filter.name(), filter.getOrder()));
        }
    }

    /**
     * 执行前置过滤器链
     *
     * @param joinPoint 切点信息
     * @param request HTTP请求对象
     * @return true表示应该跳过日志记录，false表示继续处理
     */
    public boolean shouldSkipLoggingByPreFilters(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(preFilters)) {
            return false;
        }

        for (PreFilter filter : preFilters) {
            try {
                if (filter.shouldSkipLogging(joinPoint, request)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("前置过滤器 {} 跳过日志记录", filter.name());
                    }
                    return true;
                }
            } catch (Exception e) {
                logger.warn("前置过滤器 {} 执行异常: {}", filter.name(), e.getMessage(), e);
                // 继续执行下一个过滤器
            }
        }

        return false;
    }

    /**
     * 执行后置过滤器链
     *
     * @param joinPoint 切点信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param result 方法执行结果
     * @param exception 方法执行异常
     * @param loggingDataContext 日志上下文
     * @param processingTimeMs 处理耗时
     * @return true表示应该跳过日志记录，false表示继续处理
     */
    public boolean shouldSkipLoggingByPostFilters(ProceedingJoinPoint joinPoint,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object result,
                                                 Throwable exception,
                                                 ApiLoggingDataContext loggingDataContext,
                                                 long processingTimeMs) {
        if (CollectionUtils.isEmpty(postFilters)) {
            return false;
        }

        for (PostFilter filter : postFilters) {
            try {
                if (filter.shouldSkipLogging(joinPoint, request, response, result,
                        exception, loggingDataContext, processingTimeMs)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("后置过滤器 {} 跳过日志记录", filter.name());
                    }
                    return true;
                }
            } catch (Exception e) {
                logger.warn("后置过滤器 {} 执行异常: {}", filter.name(), e.getMessage(), e);
                // 继续执行下一个过滤器
            }
        }

        return false;
    }


    /**
     * 按优先级排序过滤器
     */
    private <T extends Filter> List<T> sortFilters(List<T> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Collections.emptyList();
        }

        return filters.stream()
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .collect(Collectors.toList());
    }

}
