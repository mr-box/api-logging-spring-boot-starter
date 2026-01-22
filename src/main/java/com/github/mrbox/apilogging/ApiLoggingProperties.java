package com.github.mrbox.apilogging;

import lombok.Data;
import com.github.mrbox.apilogging.model.LogMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * 日志组件的配置属性类。
 *
 * @author Zwk
 */
@Data
@ConfigurationProperties(prefix = "mr-box.api-logging")
public class ApiLoggingProperties {

    /**
     * 启用或禁用API日志记录。默认为 false
     */
    private boolean enabled = false;

    /**
     * 默认日志模式。可以是 SIMPLE (精简) 或 DETAILED (详细)。
     */
    private LogMode logMode = LogMode.SIMPLE;

    /**
     * 记录日志时，参数或返回值序列化为字符串的最大长度。
     * 超出部分将被截断。设置为 -1 表示不限制，0 表示不记录参数/返回值。
     * // 默认1KB
     */
    private int maxPayloadLength = 1024;

    /**
     * 机密信息处理配置
     */
    private Sensitive sensitive = new Sensitive();

    /**
     * 异常堆栈打印配置
     */
    private ExceptionStack exceptionStack = new ExceptionStack();


    /**
     * <pre>
     * 内置的详细日志触发器
     * - header 通过请求头触发详细日志，默认不启用
     * - exception 通过异常触发详细日志
     * - statusCode 通过HTTP状态码触发详细日志
     * </pre>
     */
    private LinkedHashSet<String> triggers = new LinkedHashSet<>(Arrays.asList("exception","statusCode"));

    /**
     * 通过请求头触发详细日志的配置。
     */
    private HeaderTriggerProperties headerTrigger = new HeaderTriggerProperties();

    /**
     * 应触发响应详细日志记录的 HTTP 状态码集合。
     */
    private Set<Integer> detailedLogOnStatusCodes = new HashSet<>(
            Arrays.asList(400, 401, 403, 404, 405, 500, 501, 502, 503, 504));

    /**
     * <pre>
     * 应始终启用详细日志的 URI 模式。
     * 使用 Ant 风格的路径模式。
     * 示例: "/api/admin/**", "/debug/**"。
     * </pre>
     */
    private List<String> forceDetailedLogPatterns = new ArrayList<>();


    /**
     * 不记录日志的请求Content-Type集合。
     * 不区分大小写、前缀匹配。
     */
    private Set<String> excludedArgumentOnContentTypes = new HashSet<>(
            Arrays.asList("application/octet-stream", "multipart/form-data"));


    @Data
    public static class HeaderTriggerProperties {
        /**
         * 可触发详细日志记录的请求头名称。
         * 默认: "X-Log-Mode"。
         */
        private String headerName = "X-Log-Mode";
        /**
         * 表示应启用详细日志记录的请求头值。
         * 不区分大小写匹配。
         * 默认: "DETAILED"。
         */
        private String detailedValue = "DETAILED";
    }

    @Data
    public static class Sensitive {
        /**
         * 不应记录其值的参数名称列表 (完全匹配，区分大小写)。
         * 用于屏蔽密码、密钥等敏感信息。
         */
        private Set<String> argNames = new HashSet<>(Collections.singletonList("password"));

        /**
         * 其值应被掩码的请求头名称列表。
         * 不区分大小写匹配。
         * 示例: "Authorization", "Cookie"。
         */
        private Set<String> requestHeaders = new HashSet<>(Arrays.asList("Authorization", "Token"));

        /**
         * 用于替换敏感请求头值的掩码字符串。
         */
        private String mask = "****";
    }

    @Data
    public static class ExceptionStack {
        /**
         * 是否启用异常堆栈记录,false时只保留一行异常类型+异常信息
         */
        private boolean enabled = false;

        /**
         * 项目包名前缀（如com.example)，打印异常堆栈时，支持匹配到指定包名的时候再输出2行即停止打印剩余堆栈信息
         * 但最大行数仍受maxLines控制
         */
        private String packagePrefix = "";

        /**
         * 如果 enabled 为 true，堆栈跟踪的最大行数。
         */
        private int maxLines = 20;
    }

}
