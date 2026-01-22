
package com.github.mrbox.apilogging.trigger;


import com.github.mrbox.apilogging.ApiLoggingProperties;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 基于HTTP请求头的详细日志触发器实现，当请求头中包含特定值时触发详细日志。
 *
 * @author Zwk
 */
public class HeaderDetailedLogTrigger implements DetailedLogTrigger {

    /**
     * @return 触发器名称
     */
    @Override
    public String name() {
        return "header";
    }

    @Override
    public boolean shouldLogDetailed(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Throwable exception,
                                     ApiLoggingDataContext loggingDataContext,
                                     ApiLoggingProperties properties) {
        if (request == null) return false;
        ApiLoggingProperties.HeaderTriggerProperties headerProps = properties.getHeaderTrigger();
        if (StringUtils.hasText(headerProps.getHeaderName())) {
            String headerValue = request.getHeader(headerProps.getHeaderName());
            if (headerValue != null) {
                return headerValue.equalsIgnoreCase(headerProps.getDetailedValue());
            }
        }
        return false;
    }
}
