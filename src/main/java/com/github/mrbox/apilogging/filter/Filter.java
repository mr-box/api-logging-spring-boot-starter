package com.github.mrbox.apilogging.filter;

/**
 *
 * @author Zwk
 */
public interface Filter {
    /**
     * 过滤器名称
     *
     * @return 过滤器名称
     */
    default String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * <pre>
     * 过滤器的执行顺序，数值越小优先级越高
     *
     *  过滤器优先级建议：
     *   URI模式过滤器：-100 ~ -90（最高优先级）
     *   请求头过滤器：-90 ~ -80
     *   方法名过滤器：-80 ~ -70
     *   自定义业务过滤器：-70 ~ 0
     *   响应状态过滤器：0 ~ 10
     *   处理时间过滤器：10 ~ 20（最低优先级）
     * </pre>
     * @return 执行顺序，默认为0
     */
    default int getOrder() {
        return 0;
    }
}
