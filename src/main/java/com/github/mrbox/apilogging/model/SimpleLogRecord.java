
package com.github.mrbox.apilogging.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志精简模式的记录对象，含基本的请求信息。
 *
 * @author Zwk
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleLogRecord {
    private String logMode;         // 日志模式 (SIMPLE 或 DETAILED)
    private String clientIp;        // 客户端ip
    private long requestTimestamp;  // 请求时间戳 (毫秒数)
    private String uri;             // 请求URI
    private String controllerHandler; // Controller类名#方法名
    private Long processingTimeMs;  // 请求处理耗时 (毫秒)
    private Integer statusCode;     // HTTP响应状态码
    private String errorIndicator;  // 错误指示 (例如: "WARN:BusinessRuleException", "ERROR:NullPointerException", 或 null)
    private String exceptionStacktrace; // 异常堆栈信息


    public SimpleLogRecord(long requestTimestamp, String uri, String controllerHandler, String logMode) {
        this.requestTimestamp = requestTimestamp;
        this.uri = uri;
        this.controllerHandler = controllerHandler;
        this.logMode = logMode;
    }
}
