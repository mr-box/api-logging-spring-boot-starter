package com.github.mrbox.apilogging.trigger;

import com.github.mrbox.apilogging.ApiLoggingProperties;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于HTTP响应状态码的详细日志触发器实现，当返回特定错误状态码时触发详细日志。
 *
 * @author Zwk
 */
public class HttpStatusCodeDetailedLogTrigger implements DetailedLogTrigger {
    private static final Logger logger = LoggerFactory.getLogger(HttpStatusCodeDetailedLogTrigger.class);

    /**
     * @return 触发器名称
     */
    @Override
    public String name() {
        return "statusCode";
    }

    @Override
    public boolean shouldLogDetailed(HttpServletRequest request,
            HttpServletResponse response,
            Throwable exception,
            ApiLoggingDataContext loggingDataContext,
            ApiLoggingProperties properties) {

        if (properties.getDetailedLogOnStatusCodes() == null || properties.getDetailedLogOnStatusCodes().isEmpty()) {
            return false;
        }

        Integer status = null;

        // 首先检查ResponseEntity，因为它更准确地反映了当前响应的实际状态码
        if (loggingDataContext != null && loggingDataContext.getProceedResult() instanceof ResponseEntity) {
            try {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) loggingDataContext.getProceedResult();
                status = responseEntity.getStatusCodeValue();
                logger.debug("从ResponseEntity获取状态码: {}", status);
            } catch (Exception e) {
                logger.warn("从ResponseEntity获取状态码失败: {}", e.getMessage());
            }
        }

        // 如果没有从ResponseEntity获取到状态码，尝试从response对象获取
        if (status == null && response != null) {
            status = response.getStatus();
            logger.debug("从HttpServletResponse获取状态码: {}", status);
        }

        return status != null && properties.getDetailedLogOnStatusCodes().contains(status);
    }
}
