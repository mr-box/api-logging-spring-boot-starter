package com.github.mrbox.apilogging.formatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrbox.apilogging.ApiLoggingProperties;
import com.github.mrbox.apilogging.model.LogMode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认的日志内容格式化器实现。
 *
 * @author Zwk
 */
public class DefaultLogContentFormatter implements LogContentFormatter {

    private final ObjectMapper objectMapper;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private static final String UNKNOWN_IP = "unknown";

    public DefaultLogContentFormatter(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
    }

    /**
     * 截断字符串，如果超过最大长度，则截断。
     *
     * @param str       需要截断的字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    @Override
    public String truncate(String str, int maxLength) {
        if (maxLength < 0 || str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...[已截断]";
    }

    /**
     * 格式化方法参数。
     *
     * @param joinPoint  AOP连接点，可以访问方法签名和参数
     * @param request    当前的HttpServletRequest
     * @param properties 日志配置属性
     * @return 格式化后的参数字符串
     */
    @Override
    public String formatArguments(ProceedingJoinPoint joinPoint, HttpServletRequest request,
            ApiLoggingProperties properties) {
        if (properties.getMaxPayloadLength() == 0)
            return "[已忽略]";

        // 检查内容类型是否在排除列表中
        if (request != null && properties.getExcludedArgumentOnContentTypes() != null
                && !properties.getExcludedArgumentOnContentTypes().isEmpty()) {
            String contentType = request.getContentType();
            if (contentType != null) {
                String finalContentType = contentType.toLowerCase();
                for (String excluded : properties.getExcludedArgumentOnContentTypes()) {
                    if (finalContentType.startsWith(excluded.toLowerCase())) {
                        return "[忽略Content-Type: " + contentType + "]";
                    }
                }
            }
        }

        // 检查是否是安全的内容类型
        boolean isSafeContentType = isSafeContentType(request, properties);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());

        Map<String, Object> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String paramName = (paramNames != null && paramNames.length > i) ? paramNames[i] : "arg" + i;

            // 下面if判断逻辑的顺序不能改变
            // 优先处理敏感参数
            if (properties.getSensitive().getArgNames().contains(paramName)) {
                // 敏感参数总是掩码处理，无论内容类型
                argsMap.put(paramName, properties.getSensitive().getMask());
            } else if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse ||
                    args[i] instanceof InputStream || args[i] instanceof OutputStream ||
                    args[i] instanceof org.springframework.ui.Model
                    || args[i] instanceof org.springframework.validation.Errors) {
                // 这些类型总是排除
                argsMap.put(paramName, "[忽略类型: " + args[i].getClass().getSimpleName() + "]");
            } else if (args[i] instanceof MultipartFile) {
                // 文件类型特殊处理
                try {
                    MultipartFile multipartFile = (MultipartFile) args[i];
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", multipartFile.getOriginalFilename());
                    fileInfo.put("contentType", multipartFile.getContentType());
                    fileInfo.put("size", multipartFile.getSize());
                    argsMap.put(paramName, fileInfo);
                } catch (Exception e) {
                    argsMap.put(paramName, "[文件信息提取失败: " + e.getMessage() + "]");
                }
            } else if (isSafeContentType || isSimpleValueType(args[i])) {
                // 安全内容类型的请求或简单值类型记录完整内容
                argsMap.put(paramName, args[i]);
            } else {
                // 其他类型只记录类型信息
                String typeName = args[i] != null ? args[i].getClass().getName() : "null";
                argsMap.put(paramName, "[忽略复杂类型: " + typeName + "]");
            }
        }

        try {
            String jsonArgs = objectMapper.writeValueAsString(argsMap);
            return truncate(jsonArgs, properties.getMaxPayloadLength());
        } catch (Exception e) {
            return "[参数序列化错误: " + e.getMessage() + "]";
        }
    }


    /**
     * 格式化请求参数 (查询字符串)。
     *
     * @param request 当前的HttpServletRequest
     * @param properties 日志配置属性
     * @return 请求参数字符串，如果适用；否则返回null
     */
    @Override
    public String formatRequestQueries(HttpServletRequest request, ApiLoggingProperties properties) {
        if (request == null)
            return null;
        // 用于查询参数
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }
        try {
            return truncate(URLDecoder.decode(queryString, StandardCharsets.UTF_8.name()), properties.getMaxPayloadLength());
        } catch (UnsupportedEncodingException e) {
            return queryString + " [URLDecoder Decode Error: " + e.getMessage() + "]";
        }
    }

    /**
     * 格式化方法返回值。
     *
     * @param returnValue 方法的原始返回值
     * @param mode        当前的日志模式 (DETAILED 或 SIMPLE)
     * @param properties  日志配置属性
     * @return 格式化后的返回值字符串
     */
    @Override
    public String formatReturnValue(Object returnValue, LogMode mode, ApiLoggingProperties properties) {
        if (!LogMode.DETAILED.equals(mode) || returnValue == null) {
            return null;
        }
        if (properties.getMaxPayloadLength() == 0)
            return "[已忽略]";

        Object valueToLog = returnValue;
        if (returnValue instanceof ResponseEntity) {
            valueToLog = ((ResponseEntity<?>) returnValue).getBody();
            if (valueToLog == null) {
                return "[ResponseEntity的body为空]";
            }

            if (valueToLog instanceof InputStreamSource) {
                return "[ResponseEntity包含InputStreamSource类型的body]";
            }
        } else if (returnValue instanceof ModelAndView) {
            return "[忽略类型:ModelAndView]";
        }

        try {
            String jsonReturn = objectMapper.writeValueAsString(valueToLog);
            return truncate(jsonReturn, properties.getMaxPayloadLength());
        } catch (Exception e) {
            return "[返回值序列化错误: " + e.getMessage() + "]";
        }
    }

    /**
     * 格式化异常信息。
     *
     * @param throwable  捕获到的异常
     * @param mode       当前的日志模式 (DETAILED 或 SIMPLE)
     * @param properties 日志配置属性
     * @return 格式化后的异常信息字符串
     */
    @Override
    public String formatException(Throwable throwable, LogMode mode, ApiLoggingProperties properties) {
        String message = throwable.getClass().getName() + ": " + throwable.getMessage();
        if (mode == LogMode.DETAILED || properties.getExceptionStack().isEnabled()) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            String[] lines = sw.toString().split(System.lineSeparator());
            StringBuilder stackTraceBuilder = new StringBuilder("\n");

            String packagePrefix = properties.getExceptionStack().getPackagePrefix();
            int maxLines = properties.getExceptionStack().getMaxLines();
            boolean foundPackagePrefix = false;

            int maxLoop = Math.min(lines.length, maxLines);

            for (int i = 1; i < maxLoop; i++) {
                String line = lines[i];
                stackTraceBuilder.append(line).append("\n");

                // 如果找到了包名前缀，开始计数后续行数
                if (!foundPackagePrefix && packagePrefix != null && !packagePrefix.isEmpty()
                    && line.contains("at " + packagePrefix)) {

                    foundPackagePrefix = true;
                    // 找到包名，最多再打印2行
                    maxLoop = Math.min(maxLoop, i + 1 + 2);
                }
            }

            message += stackTraceBuilder.toString();
        }
        return message;
    }

    /**
     * 格式化请求头。
     *
     * @param request    当前的HttpServletRequest
     * @param properties 日志配置属性
     * @return 包含请求头的Map，敏感信息已处理
     */
    @Override
    public Map<String, String> formatRequestHeaders(HttpServletRequest request, ApiLoggingProperties properties) {
        if (request == null)
            return Collections.emptyMap();
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        Set<String> sensitiveHeadersLower = properties.getSensitive().getRequestHeaders().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (sensitiveHeadersLower.contains(headerName.toLowerCase())) {
                headers.put(headerName, properties.getSensitive().getMask());
            } else {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    /**
     * 格式化客户端IP地址。
     *
     * @param request 当前的HttpServletRequest
     * @return 客户端IP地址字符串
     */
    @Override
    public String formatClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多个代理，第一个IP是客户端的IP
        // if (ip != null && ip.contains(",")) {
        // ip = ip.split(",")[0].trim();
        // }
        return ip;
    }

    /**
     * 判断对象是否为简单值类型（可安全序列化）
     *
     * @param value value
     * @return 是否为可安全序列化的值类型
     */
    protected boolean isSimpleValueType(Object value) {
        if (value == null) {
            return true;
        }

        Class<?> clazz = value.getClass();
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                Number.class.isAssignableFrom(clazz) ||
                clazz == Date.class ||
                clazz.isEnum();
    }


    /**
     * 判断是否安全的请求类型
     *
     * @param request HttpServletRequest
     * @param properties ApiLoggingProperties
     * @return 是否可安全处理的请求类型
     */
    protected static boolean isSafeContentType(HttpServletRequest request, ApiLoggingProperties properties) {

        if (request == null) {
            return false;
        }

        HashSet<String> safeTypes = new HashSet<>(Arrays.asList(
                "application/json",
                "application/x-www-form-urlencoded",
                "text/plain",
                "multipart/form-data"));

        String contentType = request.getContentType();
        return contentType != null && safeTypes.stream().anyMatch(s -> contentType.toLowerCase().startsWith(s));
    }
}
