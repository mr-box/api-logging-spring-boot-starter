package com.github.mrbox.apilogging.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrbox.apilogging.ApiLoggingAspect;
import com.github.mrbox.apilogging.model.DetailedLogRecord;
import com.github.mrbox.apilogging.model.SimpleLogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 使用 SLF4J 将日志对象以 JSON 格式发布的实现
 * @author Zwk
 */
public class DefaultJsonLogger implements ApiLoggingLogger {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAspect.class);

    private final ObjectMapper objectMapper;

    public DefaultJsonLogger(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
    }

    /**
     * 发布日志记录。
     *
     * @param record 要发布的日志记录对象
     */
    @Override
    public void publish(SimpleLogRecord record) {
        if (record == null) {
            return;
        }

        try {
            String jsonLog = objectMapper.writeValueAsString(record);
            if (record.getErrorIndicator() != null && !record.getErrorIndicator().isEmpty()) {
                // 如果有错误指示，则使用错误日志记录器
                logger.error("{}", jsonLog);
            } else if (record instanceof DetailedLogRecord) {
                logger.info("{}", jsonLog);
            } else {
                logger.info("{}", jsonLog);
            }
        } catch (Throwable e) {
            // 序列化失败时，记录一个简化的错误日志，避免日志系统本身抛出异常导致主流程问题
            logger.error("LogRecord 序列话异常. record: {}, error: {}", record, e.getMessage());
        }
    }
}
