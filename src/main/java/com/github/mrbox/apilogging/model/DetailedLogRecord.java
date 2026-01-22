
package com.github.mrbox.apilogging.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * 日志详细模式的记录对象，继承自精简记录类，含更详细的请求信息。
 *
 * @author Zwk
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedLogRecord extends SimpleLogRecord {
    private Map<String, String> requestHeader; // 请求头
    private String requestParams; // 请求参数
    private String responseData; // 返回参数
}
