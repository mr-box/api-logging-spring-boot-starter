package com.github.mrbox.apilogging;

import com.github.mrbox.apilogging.filter.FilterManager;
import com.github.mrbox.apilogging.formatter.LogContentFormatter;
import com.github.mrbox.apilogging.logger.ApiLoggingLogger;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import com.github.mrbox.apilogging.model.DetailedLogRecord;
import com.github.mrbox.apilogging.model.LogMode;
import com.github.mrbox.apilogging.model.SimpleLogRecord;
import com.github.mrbox.apilogging.trigger.DetailedLogTrigger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 日志组件的核心AOP切面，用于拦截Controller记录请求和响应信息。
 *
 * @author Zwk
 */
@Aspect
public class ApiLoggingAspect implements Ordered {

    private final ApiLoggingProperties properties;
    private final ThreadLocal<ApiLoggingDataContext> loggingDataContextThreadLocal = new ThreadLocal<>();
    private final List<DetailedLogTrigger> detailedLogTriggers;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ApiLoggingLogger logger;
    private final LogContentFormatter logContentFormatter;
    private final FilterManager filterManager;

    public ApiLoggingAspect(ApiLoggingProperties properties,
                            List<DetailedLogTrigger> detailedLogTriggers,
                            ApiLoggingLogger logger,
                            LogContentFormatter logContentFormatter,
                            FilterManager filterManager) {
        this.properties = properties;
        this.detailedLogTriggers = Optional.ofNullable(detailedLogTriggers).orElse(Collections.emptyList());
        this.logger = logger;
        this.logContentFormatter = logContentFormatter;
        this.filterManager = filterManager;
    }

    @Around("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public Object logRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (sra != null) ? sra.getRequest() : null;
        HttpServletResponse response = (sra != null) ? sra.getResponse() : null;


        ApiLoggingDataContext loggingDataContext = new ApiLoggingDataContext(properties.getLogMode());
        loggingDataContextThreadLocal.set(loggingDataContext);

        // 是否触发了详细模式的触发条件
        boolean detailModeTriggerEffected = determineEffectiveLogModeByUriPattern(request) || determineEffectiveLogModeByTriggers(request, response, null);

        // 如果没有触发器被触发，执行前置过滤器检查，如果被过滤，直接执行业务方法
        if (!detailModeTriggerEffected && filterManager.shouldSkipLoggingByPreFilters(joinPoint, request)) {
            return joinPoint.proceed();
        }

        DetailedLogRecord logRecord = new DetailedLogRecord();

        logRecord.setClientIp(logContentFormatter.formatClientIp(request));

        String controllerMethod = joinPoint.getTarget().getClass().getSimpleName() + "#"
                + joinPoint.getSignature().getName();
        String requestUri = (request != null) ? request.getRequestURI() : "UnknownURI";

        Object result = null;
        Throwable exception = null;

        try {
            LogMode currentMode = getEffectiveLogMode();
            logRecord.setLogMode(currentMode.name());

            logRecord.setControllerHandler(controllerMethod);
            String queryString = null;
            if (request != null) {
                queryString = logContentFormatter.formatRequestQueries(request, properties);
                logRecord.setRequestHeader(logContentFormatter.formatRequestHeaders(request, properties));
                logRecord.setRequestParams(logContentFormatter.formatArguments(joinPoint, request, properties));
            }
            // uri可以使用URLDecoder.decode()方法处理一下以解析requestUri里的中文字符，暂不处理
            logRecord.setUri(queryString == null || queryString.isEmpty() ? requestUri : requestUri + "?" + queryString);

            logRecord.setRequestTimestamp(System.currentTimeMillis());
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).warn("logRecord请求信息获取失败: {}", e.getMessage(), e);
        }

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            try {
                loggingDataContext.setProceedResult(result);

                long processingTimeMs = System.currentTimeMillis() - logRecord.getRequestTimestamp();

                if (!detailModeTriggerEffected) {
                    // 确定最终的日志模式
                    detailModeTriggerEffected = determineEffectiveLogModeByTriggers(request, response, exception);
                }
                // 执行后置过滤器检查
                boolean shouldSkipByPostFilter = false;
                // 如果没有触发器被触发，执行后置过滤器检查
                if (!detailModeTriggerEffected) {
                    shouldSkipByPostFilter = filterManager.shouldSkipLoggingByPostFilters(
                            joinPoint, request, response, result, exception, loggingDataContext, processingTimeMs);
                }

                if (!shouldSkipByPostFilter) {
                    logRecord.setProcessingTimeMs(processingTimeMs);

                    // 获取状态码
                    Integer statusCode = null;
                    if (result instanceof ResponseEntity) {
                        statusCode = ((ResponseEntity<?>) result).getStatusCodeValue();
                    } else if (result instanceof HttpServletResponse) {
                        statusCode = ((HttpServletResponse) result).getStatus();
                    } else if (response != null) {
                        statusCode = response.getStatus();
                    }

                    LogMode finalMode = getEffectiveLogMode();
                    logRecord.setLogMode(finalMode.name());

                    // 设置状态码和相关数据
                    logRecord.setStatusCode(statusCode);
                    if (exception != null) {
                        logRecord.setExceptionStacktrace(
                                logContentFormatter.formatException(exception, finalMode, properties));
                        String errorType = exception.getClass().getSimpleName();
                        logRecord.setErrorIndicator(
                                statusCode != null && statusCode >= 500 ? "ERROR:" + errorType : "WARN:" + errorType);
                    } else {
                        logRecord.setResponseData(logContentFormatter.formatReturnValue(result, finalMode, properties));
                        if (statusCode != null && statusCode >= 400) {
                            logRecord.setErrorIndicator(statusCode >= 500 ? "ERROR_HTTP_STATUS_" + statusCode
                                    : "WARN_HTTP_STATUS_" + statusCode);
                        }
                    }

                    if (!LogMode.DETAILED.name().equals(logRecord.getLogMode())) {
                        logger.publish(convertToSimpleLog(logRecord));
                    } else {
                        logger.publish(logRecord);
                    }
                }

            } catch (Throwable e) {
                LoggerFactory.getLogger(getClass()).error("请求日志记录失败，异常信息:{}", e.getMessage(), e);
            } finally {
                loggingDataContextThreadLocal.remove();
            }
        }
    }

    private SimpleLogRecord convertToSimpleLog(DetailedLogRecord logRecord) {
        SimpleLogRecord record = new SimpleLogRecord();
        record.setLogMode(logRecord.getLogMode());
        record.setClientIp(logRecord.getClientIp());
        record.setRequestTimestamp(logRecord.getRequestTimestamp());
        record.setUri(logRecord.getUri());
        record.setControllerHandler(logRecord.getControllerHandler());
        record.setProcessingTimeMs(logRecord.getProcessingTimeMs());
        record.setStatusCode(logRecord.getStatusCode());
        record.setErrorIndicator(logRecord.getErrorIndicator());
        record.setExceptionStacktrace(logRecord.getExceptionStacktrace());

        return record;
    }

    private boolean determineEffectiveLogModeByUriPattern(HttpServletRequest request) {
        ApiLoggingDataContext context = loggingDataContextThreadLocal.get();
        if (context == null) {
            return false;
        }
        try {
            // 根据uri匹配强制详细模式
            if (request != null && !CollectionUtils.isEmpty(properties.getForceDetailedLogPatterns())) {
                for (String pattern : properties.getForceDetailedLogPatterns()) {
                    if (pathMatcher.match(pattern, request.getRequestURI())) {
                        context.setEffectiveLogMode(LogMode.DETAILED);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).warn("自动切换日志模式异常:{}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * 通过触发器切换日志模式
     *
     * @param request
     * @param response
     * @param exception
     * @return 是否触发了详细模式的触发条件
     */
    private boolean determineEffectiveLogModeByTriggers(HttpServletRequest request, HttpServletResponse response,
                                                        Throwable exception) {
        ApiLoggingDataContext context = loggingDataContextThreadLocal.get();
        if (context == null) {
            return false;
        }

        try {
            // 通过详细模式触发器触发
            if (!CollectionUtils.isEmpty(properties.getTriggers())) {
                for (DetailedLogTrigger trigger : detailedLogTriggers) {
                    if (properties.getTriggers().contains(trigger.name()) && trigger.shouldLogDetailed(request, response, exception, context, properties)) {
                        context.setEffectiveLogMode(LogMode.DETAILED);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).warn("自动切换日志模式异常:{}", e.getMessage(), e);
        }

        return false;
    }

    private LogMode getEffectiveLogMode() {
        ApiLoggingDataContext context = loggingDataContextThreadLocal.get();
        return (context != null) ? context.getEffectiveLogMode() : properties.getLogMode();
    }

    @Override
    public int getOrder() {
        return 0;
        //return properties.getAop().getOrder();
    }
}
