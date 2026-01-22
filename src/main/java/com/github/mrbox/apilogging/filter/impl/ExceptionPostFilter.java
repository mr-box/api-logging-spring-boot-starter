package com.github.mrbox.apilogging.filter.impl;

import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于异常的后置过滤器实现。
 *
 * @author Zwk
 */
public class ExceptionPostFilter implements PostFilter {

    @Override
    public boolean shouldSkipLogging(ProceedingJoinPoint joinPoint,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object result,
                                   Throwable exception,
                                   ApiLoggingDataContext loggingDataContext,
                                   long processingTimeMs) {

        // 存在异常时不记录日志
        return exception == null;
    }

}
