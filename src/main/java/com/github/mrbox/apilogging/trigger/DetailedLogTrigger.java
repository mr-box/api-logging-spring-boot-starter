package com.github.mrbox.apilogging.trigger;


import com.github.mrbox.apilogging.ApiLoggingProperties;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 用于判断是否应为当前请求激活详细日志记录的组件接口
 * @author Zwk
 */
public interface DetailedLogTrigger {
    /**
     * @return 触发器名称
     */
    String name();

    /**
     * 判断是否应为当前请求/响应上下文触发详细日志记录。
     *
     * @param request        当前的 HttpServletRequest
     * @param response       当前的 HttpServletResponse (在finally块中，通常可用)
     * @param exception      处理过程中发生的任何异常 (可能为 null)
     * @param loggingDataContext 共享上下文
     * @param properties     日志配置属性
     * @return 如果应激活详细日志记录，则返回 true，否则返回 false
     */
    boolean shouldLogDetailed(HttpServletRequest request,
                              HttpServletResponse response,
                              Throwable exception,
                              ApiLoggingDataContext loggingDataContext,
                              ApiLoggingProperties properties);
}
