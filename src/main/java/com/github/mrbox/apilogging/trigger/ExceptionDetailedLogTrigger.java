
package com.github.mrbox.apilogging.trigger;


import com.github.mrbox.apilogging.ApiLoggingProperties;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于异常的详细日志触发器实现，当请求处理过程中抛出异常时触发详细日志。
 *
 * @author Zwk
 */
public class ExceptionDetailedLogTrigger implements DetailedLogTrigger {

    /**
     * @return 触发器名称
     */
    @Override
    public String name() {
        return "exception";
    }

    @Override
    public boolean shouldLogDetailed(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Throwable exception,
                                     ApiLoggingDataContext loggingDataContext,
                                     ApiLoggingProperties properties) {
        // 如果存在任何异常，则触发详细日志记录。
        return exception != null;
    }

}
