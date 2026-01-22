package com.github.mrbox.apilogging.model;

/**
 * 定义日志记录的模式
 * @author Zwk
 */
public enum LogMode {
    /**
     * 精简模式: 仅记录基础的请求信息
     * @see SimpleLogRecord
     */
    SIMPLE,
    /**
     * 详细模式: 在精简模式基础上，额外记录请求头、请求参数、返回参数、异常等信息。
     * @see DetailedLogRecord
     */
    DETAILED
}
