package com.github.mrbox.apilogging.filter;

import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 后置过滤器接口，用于基于响应内容和异常进行过滤判断。
 *
 * <p>后置过滤器在Controller方法执行后被调用，可以基于响应结果、异常信息、
 * 执行时间等因素决定是否需要记录日志。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>基于响应状态码过滤（如只记录错误响应）</li>
 *   <li>基于响应内容过滤（如排除空响应或特定格式响应）</li>
 *   <li>基于异常类型过滤（如只记录业务异常）</li>
 *   <li>基于执行时间过滤（如只记录慢请求）</li>
 *   <li>基于响应大小过滤（如排除大文件下载）</li>
 * </ul>
 *
 * @author Zwk
 */
@FunctionalInterface
public interface PostFilter extends Filter {



    /**
     * 判断是否应该跳过日志记录
     *
     * @param joinPoint 切点信息，包含目标方法和参数
     * @param request HTTP请求对象，可能为null
     * @param response HTTP响应对象，可能为null
     * @param result 方法执行结果，可能为null
     * @param exception 方法执行过程中的异常，可能为null
     * @param loggingDataContext 日志上下文数据
     * @param processingTimeMs 方法执行耗时（毫秒）
     * @return true表示跳过日志记录，false表示继续处理
     */
    boolean shouldSkipLogging(ProceedingJoinPoint joinPoint,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             Object result,
                             Throwable exception,
                             ApiLoggingDataContext loggingDataContext,
                             long processingTimeMs);


}
