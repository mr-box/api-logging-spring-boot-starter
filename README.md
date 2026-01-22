# api-logging 组件使用文档

![Build Status](https://github.com/mr-box/api-logging/actions/workflows/maven.yml/badge.svg)
[![GitHub release](https://img.shields.io/github/release/mr-box/api-logging.svg)](https://github.com/mr-box/api-logging/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![JitPack](https://jitpack.io/v/mr-box/api-logging.svg)](https://jitpack.io/#mr-box/api-logging)


## 简介

api-logging 是一个基于 Spring AOP 的 API 日志记录组件，用于自动记录 Spring Web 应用中的 HTTP 请求和响应信息。该组件通过切面技术拦截 Controller 方法的调用，记录请求参数、响应结果、处理时间等信息，默认记录精简的日志信息，并支持多种触发条件来决定何时记录详细日志。

## 核心依赖

该模块依赖以下组件：

- `spring-boot-starter-web`: Spring Web MVC 基础。
- `spring-boot-starter-aop`: Spring AOP，用于实现切面功能。

## 功能特性

- 支持启用、禁用组件
- 自动记录请求和响应信息
- 支持精简和详细两种日志模式
- 多种触发器决定何时记录详细日志
- 支持自定义记录详细日志的触发器
- 支持指定URI强制记录详细日志
- 敏感参数脱敏
- 异常信息和堆栈跟踪记录
- 默认基于 SLF4J 的 JSON 格式日志输出
- 支持自定义日志格式
- 支持过滤器配置，自定义不打印日志的场景

## 快速开始

组件所有配置均有默认值，添加依赖后启用该组件即可使用。

### 1. 添加依赖

本组件通过 JitPack 发布。

**第一步**：在项目根目录的 `pom.xml` 中添加 JitPack 仓库源：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**第二步**：添加组件依赖：

```xml
<dependency>
    <groupId>com.github.mr-box</groupId>
    <artifactId>api-logging-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用组件

在 `application.yml` 或 `application.properties` 中启用组件：

```yaml
mr-box:
  api-logging:
    enabled: true
```

## 配置说明

下面列举的配置项，除特殊说明外，示例值均为组件默认值。

### 基础配置

```yaml
mr-box:
  api-logging:
    # 是否启用日志记录
    enabled: false
    
    # 日志模式：SIMPLE（精简）或 DETAILED（详细）
    logMode: SIMPLE
    
    # 参数或返回值序列化的最大长度
    maxPayloadLength: 1024
```

### 敏感信息配置

```yaml
mr-box:
  api-logging:
    sensitive:
      # 需要脱敏的参数名称列表，区分大小写
      argNames:
        - password
      
      # 需要脱敏的请求头名称列表，不区分大小写
      requestHeaders:
        - Authorization
        - Token
      
      # 脱敏替换字符
      mask: "****"
```

### 异常堆栈配置

```yaml
mr-box:
  api-logging:
    exceptionStack:
      # 是否启用异常堆栈记录
      enabled: false
      
      # 堆栈跟踪最大行数
      maxLines: 20

      # 项目包名前缀
      # 如果不为空，只打印到含有该前缀的异常堆栈信息以及后面2行
      # 默认为空字符串
      packagePrefix: "" # 示例: com.example
```

### 详细日志触发配置

```yaml
mr-box:
  api-logging:
     # 启用的详细日志触发器
     # - header: 通过请求头触发详细日志
     # - exception: 通过异常触发详细日志
     # - statusCode: 通过HTTP状态码触发详细日志
     triggers:
       # 默认启用以下两个
       - exception
       - statusCode
    
    # 请求头触发配置
    headerTrigger:
      # 触发详细日志的请求头名称
      headerName: X-Log-Mode
      # 触发详细日志的请求头值
      detailedValue: DETAILED
    
    # 触发详细日志的 HTTP 状态码
    detailedLogOnStatusCodes: 400,401,403,404,405,500,501,502,503,504
    
    # 强制使用详细日志的 URI 模式（默认为空）
    forceDetailedLogPatterns:
      - "/api/admin/**"
      - "/debug/**"
```

### 特殊Content-Type配置

```yaml
mr-box:
  api-logging:
    # 不记录日志的请求Content-Type集合。 不区分大小写、前缀匹配。
    excluded-argument-on-content-types: 
      - application/octet-stream
      - multipart/form-data
```

## 使用示例

### 1. 精简模式日志输出示例

```json
{
  "logMode": "SIMPLE",
  "requestTimestamp": 1747707931829,
  "uri": "/api/users/list",
  "controllerHandler": "UserController#listUsers",
  "processingTimeMs": 286
}
```

### 2. 详细模式日志输出示例

```json
{
  "logMode": "DETAILED",
  "requestTimestamp": 1747708132583,
  "uri": "/api/users/list",
  "controllerHandler": "UserController#listUsers",
  "processingTimeMs": 239,
  "statusCode": 200,
  "requestHeader": {
    "content-length": "276",
    "host": "localhost:8080",
    "content-type": "application/json",
    "user-agent": "PostmanRuntime/7.43.0"
  },
  "requestParams": "{\"username\":\"guest\",\"page\":1}",
  "responseData": "{\"code\":0,\"data\":[...]}"
}
```

### 3. 异常情况日志输出示例

```json
{
  "logMode": "DETAILED",
  "requestTimestamp": 1747708262354,
  "uri": "/api/users/list",
  "controllerHandler": "UserController#listUsers",
  "processingTimeMs": 3,
  "statusCode": 500,
  "errorIndicator": "ERROR:NullPointerException",
  "requestHeader": {
    "content-length": "276",
    "host": "localhost:8080",
    "content-type": "application/json"
  },
  "requestParams": "{\"username\":\"guest\"}",
  "exceptionStacktrace": "java.lang.NullPointerException: null\n\tat com.example.demo.controller.UserController.listUsers(UserController.java:25)\n..."
}
```

## 功能扩展

api-logging 组件提供了多个扩展点，允许自定义日志记录行为。

### 过滤器功能

组件支持过滤器功能，允许依赖方自定义哪些接口需要记录日志。过滤器分为两类：

#### 前置过滤器（PreFilter）
在Controller方法执行前进行快速失败判断，可以基于请求信息（URI、参数、请求头等）决定是否跳过日志记录。

#### 后置过滤器（PostFilter）
在Controller方法执行后进行判断，可以基于响应结果、异常信息、执行时间等决定是否跳过日志记录。

#### 过滤器配置

如果内置过滤器满足要求，只需要将过滤器实现注册为Spring Bean即可自动生效。

使用内置过滤器，只需要注册Bean，无需额外配置
```java
@Bean
public UriPatternPreFilter uriPatternPreFilter() {
    Set<String> excludePatterns = Sets.newHashSet("/health", "/actuator/**");
    return new UriPatternPreFilter(excludePatterns);
}

```

可条件化启用过滤器或制定环境启用

```java
@Bean
@ConditionalOnProperty(name = "app.logging.xxxx.enabled", havingValue = "true")
@Profile("prod")
public UriPatternPreFilter uriPatternPreFilter() {
    return new UriPatternPreFilter(Sets.newHashSet("/health", "/actuator/**"));
}
```

#### 自定义前置过滤器示例

实现 `PreFilter` 接口，并将其注册为 Spring Bean：

```java
@Component
public class CustomPreFilter implements PreFilter {

    @Override
    public boolean shouldSkipLogging(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        // 自定义逻辑，返回true表示跳过日志记录
        if (request != null && "/health".equals(request.getRequestURI())) {
            return true; // 跳过健康检查接口的日志
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100; // 执行顺序，数值越小优先级越高
    }
}
```

#### 内置过滤器示例

**UriPatternPreFilter** - 基于URI模式过滤：
```java
@Bean
public UriPatternPreFilter uriPatternPreFilter() {
    Set<String> excludePatterns = Sets.newHashSet(
        "/health",
        "/actuator/**",
        "/static/**"
    );
    return new UriPatternPreFilter(excludePatterns);
}
```

**RequestHeaderPreFilter** - 基于请求头过滤：
```java
@Bean
public RequestHeaderPreFilter requestHeaderPreFilter() {
    Map<String, String> excludeHeaders = new HashMap<>();
    excludeHeaders.put("X-Client", "IOS");
    excludeHeaders.put("X-Monitor", "true");
    return new RequestHeaderPreFilter(excludeHeaders);
}
```

**MethodNamePreFilter** - 基于控制器或方法名过滤：
```java
@Bean
public MethodNamePreFilter methodNamePreFilter() {
    Set<String> excludeClasses = Sets.newHashSet("HealthController", "MonitorController");
    Set<String> excludeMethods = Sets.newHashSet("health", "ping", "status");
    return new MethodNamePreFilter(excludeClasses, excludeMethods);
}
```

**ProcessingTimePostFilter** - 基于处理时间过滤：
```java
@Bean
public ProcessingTimePostFilter processingTimePostFilter() {
    // 只记录超过500ms的慢请求
    return new ProcessingTimePostFilter(500);
}
```

### 自定义详细日志触发器

实现 `DetailedLogTrigger` 接口，并将其注册为 Spring Bean：
```java
@Component
public class CustomDetailedLogTrigger implements DetailedLogTrigger {

    @Override
    public String name() {
        return "custom";
    }

    @Override
    public boolean shouldLogDetailed(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Throwable exception,
                                    ApiLoggingDataContext logDataContext,
                                    ApiLoggingProperties properties) {
        // 自定义逻辑，决定是否触发详细日志
        return true;
    }
}
```

然后在配置中启用该触发器：

```yaml
mr-box:
  api-logging:
    triggers: custom,...
```
> 触发器名称可以重名，例如自定义的触发器也叫`response`，那么默认触发器和自定义触发器都会生效

### 自定义日志内容格式化器

实现 `LogContentFormatter` 接口，并将其注册为 Spring Bean

```java
@Component
@Primary
public class CustomLogContentFormatter implements LogContentFormatter {
    // 实现接口中的方法
}
```

## 注意事项

1. 默认情况下组件是禁用的，需要显式配置 `mr-box.api-logging.enabled=true` 来启用
2. 组件禁用状态下启动应用，不支持运行中启用组件。启用状态下启动应用，支持运行中实时禁用或启用组件。
3. 对于文件上传等特殊请求，会自动排除参数记录
4. 敏感信息会自动进行脱敏处理
5. 可以通过请求头 `X-Log-Mode: DETAILED` 临时启用详细日志
   > enabled 需要为启用状态 <br>
   > triggers 配置列表需要包含`header`
6. 异常情况下会自动记录堆栈信息
   > 需开启异常堆栈记录 <br>
   > 默认关闭时只记录一行异常类型+异常信息
7. 通常情况细触发器的优先级大于过滤器，但下面情况除外：
   > 触发器依赖接口执行结果，但使用了前置过滤器，且符合过滤条件
   > 这种情况应该使用后置过滤器，后置过滤器的优先级永远低于触发器

## 最佳实践

1. 建议在开发环境使用详细模式，生产环境使用精简模式
2. 合理配置 `maxPayloadLength` 避免日志过大
3. 及时更新敏感信息配置，确保信息安全
4. 使用 `forceDetailedLogPatterns` 为重要接口配置详细日志

## 常见问题

1. Q: 为什么某些请求没有记录参数？

   A: 
   - 检查请求的 Content-Type 是否在 `excludedArgumentOnContentTypes` 中
   - 确认 Controller 类上是否有 @Controller 或 @RestController 注解
   - 检查是否启用了 Spring AOP

2. Q：日志内容过长

   A：调整 max-payload-length 参数限制日志内容长度

   3. Q：日志中的异常堆栈信息不足
   
      A：启用异常堆栈记录，并设置合适的记录行数
      ```java
      mr-box:
         api-logging:
            enabled: on
            exception-stack:
               enabled: true                 # 默认false，只打印一行异常类型+异常信息
               package-prefix: ""            # 默认空，可配置项目包名前缀，如com.example
               max-lines: 20                 # 默认20
      ```   

4. Q: 如何临时查看某个请求的详细日志？

   A: 方法一：在请求头中添加 `X-Log-Mode: DETAILED`

      方法二：将接口地址配置`forceDetailedLogPatterns`中

5. Q: 如何自定义日志格式？

   A: 实现 `LogContentFormatter` 接口并注册为 Spring Bean

6. Q: 如何添加自定义的详细日志触发条件？

   A: 实现 `DetailedLogTrigger` 接口并注册为 Spring Bean

7. Q: 触发器没生效？

   A: 检查是否被前置过滤器过滤掉了，依赖接口响应的触发器，需要实现`PostFilter`，不能使用`PreFilter`

8. Q: 如何自定义过滤器来控制哪些接口记录日志？

   A: 实现 `PreFilter`（前置过滤）或 `PostFilter`（后置过滤）接口并注册为 Spring Bean即可自动生效

9. Q: 过滤器不生效怎么办？

   A: 检查以下几点：
   - 确认过滤器已正确注册为 Spring Bean
   - 确认过滤器的 `shouldSkipLogging` 方法逻辑正确
   - 检查是否有其他过滤器先返回了跳过日志的结果

10. Q: 如何调试过滤器执行情况？

   A: 启用DEBUG日志级别：`logging.level.com.github.mrbox.apilogging=DEBUG`

