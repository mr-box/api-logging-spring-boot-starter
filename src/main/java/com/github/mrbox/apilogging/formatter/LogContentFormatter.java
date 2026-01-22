
package com.github.mrbox.apilogging.formatter;

import com.github.mrbox.apilogging.ApiLoggingProperties;
import com.github.mrbox.apilogging.model.LogMode;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
/**
 * 日志内容格式化器接口。
 * 允许自定义如何将请求参数、返回值和异常转换为字符串表示形式，以及提取通用请求信息。
 *
 * @author Zwk
 */
public interface LogContentFormatter {

    /**
     * 截断字符串，如果超过最大长度，则截断。
     *
     * @param str 需要截断的字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    String truncate(String str, int maxLength);

    /**
     * 格式化方法参数。
     *
     * @param joinPoint AOP连接点，可以访问方法签名和参数
     * @param request 当前的HttpServletRequest
     * @param properties 日志配置属性
     * @return 格式化后的参数字符串
     */
    String formatArguments(ProceedingJoinPoint joinPoint, HttpServletRequest request, ApiLoggingProperties properties);

    /**
     * 格式化请求参数 (查询字符串)。
     *
     * @param request 当前的HttpServletRequest
     * @param properties 日志配置属性
     * @return 请求参数字符串，如果适用；否则返回null
     */
    String formatRequestQueries(HttpServletRequest request, ApiLoggingProperties properties);

    /**
     * 格式化方法返回值。
     *
     * @param returnValue 方法的原始返回值
     * @param mode 当前的日志模式 (DETAILED 或 SIMPLE)
     * @param properties 日志配置属性
     * @return 格式化后的返回值字符串
     */
    String formatReturnValue(Object returnValue, LogMode mode, ApiLoggingProperties properties);

    /**
     * 格式化异常信息。
     *
     * @param throwable 捕获到的异常
     * @param mode 当前的日志模式 (DETAILED 或 SIMPLE)
     * @param properties 日志配置属性
     * @return 格式化后的异常信息字符串
     */
    String formatException(Throwable throwable, LogMode mode, ApiLoggingProperties properties);

    /**
     * 格式化请求头。
     *
     * @param request 当前的HttpServletRequest
     * @param properties 日志配置属性
     * @return 包含请求头的Map，敏感信息已处理
     */
    Map<String, String> formatRequestHeaders(HttpServletRequest request, ApiLoggingProperties properties);

    /**
     * 格式化客户端IP地址。
     *
     * @param request 当前的HttpServletRequest
     * @return 客户端IP地址字符串
     */
    String formatClientIp(HttpServletRequest request);

}
