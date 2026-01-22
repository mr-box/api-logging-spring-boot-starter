
package com.github.mrbox.apilogging.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Zwk
 */
@Data
@NoArgsConstructor
public class ApiLoggingDataContext {
    /**
     * 生效的日志模式
     */
    private LogMode effectiveLogMode;

    /**
     * joinPoint.proceed()结果
     */
    private Object proceedResult;

    public ApiLoggingDataContext(LogMode defaultMode) {
        this.effectiveLogMode = defaultMode;
    }
}
