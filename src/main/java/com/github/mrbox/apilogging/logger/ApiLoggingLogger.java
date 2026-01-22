package com.github.mrbox.apilogging.logger;


import com.github.mrbox.apilogging.model.SimpleLogRecord;

/**
 * 用于发布日志记录的接口
 * @author Zwk
 */
public interface ApiLoggingLogger {

    /**
     * 发布给定的日志记录。
     *
     * @param logRecord 要发布的日志记录
     */
    void publish(SimpleLogRecord logRecord);

}
