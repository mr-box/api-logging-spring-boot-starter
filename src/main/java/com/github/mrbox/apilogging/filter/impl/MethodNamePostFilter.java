package com.github.mrbox.apilogging.filter.impl;

import com.github.mrbox.apilogging.filter.PostFilter;
import com.github.mrbox.apilogging.model.ApiLoggingDataContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Set;

/**
 * 基于方法名的后置过滤器实现。
 *
 * <p>支持基于Controller类名或方法名来决定是否跳过日志记录。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>排除特定Controller类的所有方法</li>
 *   <li>排除特定方法名（如健康检查方法）</li>
 *   <li>排除工具类方法</li>
 * </ul>
 *
 * @author Zwk
 */
public class MethodNamePostFilter implements PostFilter {

    private final Set<String> excludeClassNames;
    private final Set<String> excludeMethodNames;

    /**
     * 构造函数
     *
     * @param excludeClassNames 需要排除的类名集合（简单类名）
     * @param excludeMethodNames 需要排除的方法名集合
     */
    public MethodNamePostFilter(Set<String> excludeClassNames, Set<String> excludeMethodNames) {
        this.excludeClassNames = excludeClassNames != null ? excludeClassNames : Collections.emptySet();
        this.excludeMethodNames = excludeMethodNames != null ? excludeMethodNames : Collections.emptySet();
    }

    @Override
    public boolean shouldSkipLogging(ProceedingJoinPoint joinPoint,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object result,
                                     Throwable exception,
                                     ApiLoggingDataContext loggingDataContext,
                                     long processingTimeMs) {
        if (CollectionUtils.isEmpty(excludeClassNames) && CollectionUtils.isEmpty(excludeMethodNames)) {
            return false;
        }

        // 检查类名
        if (!CollectionUtils.isEmpty(excludeClassNames)) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            if (excludeClassNames.contains(className)) {
                return true;
            }
        }

        // 检查方法名
        if (!CollectionUtils.isEmpty(excludeMethodNames)) {
            String methodName = joinPoint.getSignature().getName();
            if (excludeMethodNames.contains(methodName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return -80; // 高优先级
    }
}
