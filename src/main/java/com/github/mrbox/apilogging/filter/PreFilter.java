package com.github.mrbox.apilogging.filter;

import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;

/**
 * 前置过滤器接口，用于在方法执行前进行快速失败判断。
 *
 * <p>前置过滤器在Controller方法执行前被调用，可以基于请求信息（如URI、参数、请求头等）
 * 快速决定是否需要记录日志，避免不必要的性能开销。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>基于URI路径过滤（如排除健康检查接口）</li>
 *   <li>基于请求参数过滤（如排除包含敏感信息的请求）</li>
 *   <li>基于请求头过滤（如排除指定客户端调用）</li>
 *   <li>基于Controller类或方法过滤</li>
 * </ul>
 *
 * @author Zwk
 */
@FunctionalInterface
public interface PreFilter extends Filter {


    /**
     * 判断是否应该跳过日志记录
     *
     * @param joinPoint 切点信息，包含目标方法和参数
     * @param request HTTP请求对象，可能为null
     * @return true表示跳过日志记录，false表示继续处理
     */
    boolean shouldSkipLogging(ProceedingJoinPoint joinPoint, HttpServletRequest request);


}
