# 贡献指南 (Contributing Guide)

感谢你对 api-logging-spring-boot-starter 项目的关注！我们欢迎任何形式的贡献，包括但不限于：

- 报告 Bug
- 提交功能建议
- 改进文档
- 提交代码补丁
- 优化性能

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发环境设置](#开发环境设置)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [Pull Request 流程](#pull-request-流程)
- [测试要求](#测试要求)
- [文档规范](#文档规范)

---

## 行为准则

### 我们的承诺

为了营造一个开放和友好的环境，我们承诺：

- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

### 不可接受的行为

- 使用性暗示的语言或图像
- 人身攻击或侮辱性评论
- 公开或私下骚扰
- 未经许可发布他人的私人信息
- 其他不道德或不专业的行为

---

## 如何贡献

### 报告 Bug

在提交 Bug 报告之前，请：

1. **检查现有 Issues**：确保该问题尚未被报告
2. **使用最新版本**：确认问题在最新版本中仍然存在
3. **提供详细信息**：使用 Bug 报告模板

**Bug 报告应包含：**

```markdown
### 环境信息
- api-logging 版本：1.0.0
- Spring Boot 版本：2.7.18
- JDK 版本：1.8
- 操作系统：macOS 13.0

### 问题描述
简要描述遇到的问题

### 复现步骤
1. 配置 xxx
2. 调用接口 xxx
3. 观察到 xxx

### 期望行为
描述你期望发生什么

### 实际行为
描述实际发生了什么

### 相关日志
```
粘贴相关日志或错误堆栈
```

### 补充信息
其他可能有用的信息
```

### 提交功能建议

我们欢迎新功能建议！请：

1. **检查现有 Issues**：避免重复建议
2. **详细描述用例**：说明为什么需要这个功能
3. **考虑向后兼容性**：新功能不应破坏现有 API

**功能建议应包含：**

```markdown
### 功能描述
简要描述建议的功能

### 使用场景
描述这个功能解决什么问题

### 建议的实现方式
如果有想法，可以描述实现思路

### 替代方案
是否考虑过其他解决方案？

### 影响范围
这个功能是否会影响现有功能？
```

---

## 开发环境设置

### 前置要求

- **JDK**：1.8 
- **Maven**：3.6.0 或更高版本
- **Git**：2.0 或更高版本
- **IDE**：推荐使用 IntelliJ IDEA 

### 克隆项目

```bash
git clone https://github.com/mr-box/api-logging-spring-boot-starter.git
cd api-logging-spring-boot-starter
```

### 构建项目

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ApiLoggingAspectTest

# 运行单个测试方法
mvn test -Dtest=ApiLoggingAspectTest#testGetRequestAbstractLog
```

---

## 代码规范

### Java 代码规范

我们遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)，主要规则：

#### 1. 命名规范

```java
// 类名：大驼峰（PascalCase）
public class ApiLoggingAspect { }

// 方法名：小驼峰（camelCase）
public void formatArguments() { }

// 常量：全大写，下划线分隔
private static final int MAX_PAYLOAD_LENGTH = 1024;

// 变量：小驼峰
private String requestUri;

// 包名：全小写
package com.github.mrbox.apilogging;
```

#### 2. 代码格式

```java
// 缩进：4个空格（不使用 Tab）
public void example() {
    if (condition) {
        doSomething();
    }
}

// 大括号：K&R 风格
if (condition) {
    // code
} else {
    // code
}

// 行长度：不超过 120 字符
```

#### 3. 注释规范

```java
/**
 * 类级别 JavaDoc：描述类的职责和用途
 * 
 * @author 作者名
 * @since 版本号
 */
public class Example {
    
    /**
     * 方法级别 JavaDoc：描述方法功能、参数、返回值
     * 
     * @param request HTTP 请求对象
     * @param properties 配置属性
     * @return 格式化后的字符串
     */
    public String formatArguments(HttpServletRequest request, 
                                  ApiLoggingProperties properties) {
        // 单行注释：解释复杂逻辑
        String result = process(request);
        return result;
    }
}
```

#### 4. 异常处理

```java
// ❌ 不要捕获过于宽泛的异常
try {
    doSomething();
} catch (Exception e) {  // 不推荐
    // ...
}

// ✅ 捕获具体的异常类型
try {
    doSomething();
} catch (IOException e) {
    logger.error("IO 操作失败", e);
} catch (JsonProcessingException e) {
    logger.error("JSON 序列化失败", e);
}

// ✅ 不要吞掉异常
try {
    doSomething();
} catch (Exception e) {
    logger.error("操作失败: {}", e.getMessage(), e);  // 记录完整堆栈
}
```

#### 5. 日志规范

```java
// ✅ 使用 SLF4J
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

// ✅ 使用占位符，避免字符串拼接
logger.info("处理请求: {}, 耗时: {}ms", uri, time);

// ❌ 不要使用字符串拼接
logger.info("处理请求: " + uri + ", 耗时: " + time + "ms");

// ✅ 根据日志级别合理使用
logger.debug("调试信息: {}", detail);    // 详细调试信息
logger.info("业务信息: {}", info);       // 重要业务信息
logger.warn("警告信息: {}", warning);    // 潜在问题
logger.error("错误信息: {}", error, e);  // 错误和异常
```

### 配置规范

```yaml
# 配置项命名：使用 kebab-case
mr-box:
  api-logging:
    enabled: true
    log-mode: SIMPLE
    max-payload-length: 1024
```

---

## 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

### 提交消息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

- **feat**: 新功能
- **fix**: Bug 修复
- **docs**: 文档更新
- **style**: 代码格式调整（不影响功能）
- **refactor**: 代码重构（不是新功能也不是 Bug 修复）
- **perf**: 性能优化
- **test**: 测试相关
- **chore**: 构建工具或辅助工具的变动

### Scope 范围（可选）

- **aspect**: AOP 切面相关
- **filter**: 过滤器相关
- **trigger**: 触发器相关
- **formatter**: 格式化器相关
- **config**: 配置相关
- **test**: 测试相关

### 示例

```bash
# 新功能
feat(filter): 添加基于响应时间的过滤器

支持根据接口响应时间决定是否记录日志，
可用于只记录慢请求的场景。

Closes #123

# Bug 修复
fix(aspect): 修复 ThreadLocal 内存泄漏问题

在异常情况下 ThreadLocal 可能未被清理，
导致内存泄漏。现在确保在所有情况下都会清理。

Fixes #456

# 文档更新
docs(readme): 更新配置示例

添加更多配置场景的示例代码

# 性能优化
perf(formatter): 优化 JSON 序列化性能

使用对象池复用 ObjectMapper，减少对象创建开销
```

---

## Pull Request 流程

### 1. Fork 项目

点击 GitHub 页面右上角的 "Fork" 按钮

### 2. 创建分支

```bash
# 克隆你的 Fork
git clone https://github.com/YOUR_USERNAME/api-logging-spring-boot-starter.git
cd api-logging-spring-boot-starter

# 添加上游仓库
git remote add upstream https://github.com/mr-box/api-logging-spring-boot-starter.git

# 创建功能分支
git checkout -b feature/your-feature-name
```

### 3. 开发和提交

```bash
# 进行开发
# ...

# 添加变更
git add .

# 提交（遵循提交规范）
git commit -m "feat(scope): 简短描述"

# 推送到你的 Fork
git push origin feature/your-feature-name
```

### 4. 保持同步

```bash
# 获取上游更新
git fetch upstream

# 合并到你的分支
git rebase upstream/main
```

### 5. 创建 Pull Request

1. 访问你的 Fork 页面
2. 点击 "New Pull Request"
3. 填写 PR 描述（使用模板）
4. 等待 Review

### PR 描述模板

```markdown
## 变更类型
- [ ] Bug 修复
- [ ] 新功能
- [ ] 性能优化
- [ ] 代码重构
- [ ] 文档更新
- [ ] 测试相关

## 变更说明
简要描述这个 PR 做了什么

## 相关 Issue
Closes #123
Fixes #456

## 测试
- [ ] 添加了单元测试
- [ ] 添加了集成测试
- [ ] 所有测试通过
- [ ] 手动测试通过

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 提交消息符合规范
- [ ] 更新了相关文档
- [ ] 添加了必要的注释
- [ ] 没有引入新的警告
- [ ] 向后兼容（如果不兼容，请说明）

## 截图（如果适用）
粘贴截图或日志输出

## 补充说明
其他需要说明的内容
```

### 6. Code Review

- 响应 Review 意见
- 根据反馈修改代码
- 保持讨论友好和专业

### 7. 合并

PR 被批准后，维护者会合并你的代码

---

## 测试要求

### 测试覆盖率

- **新功能**：必须包含单元测试，覆盖率 ≥ 80%
- **Bug 修复**：必须包含回归测试
- **重构**：确保现有测试通过

### 测试类型

#### 1. 单元测试

```java
@Test
void testFormatArguments() {
    // Given
    ApiLoggingProperties properties = new ApiLoggingProperties();
    properties.setMaxPayloadLength(100);
    
    // When
    String result = formatter.formatArguments(joinPoint, request, properties);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.length()).isLessThanOrEqualTo(100);
}
```

#### 2. 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ApiLoggingIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testEndToEndLogging() throws Exception {
        mockMvc.perform(get("/api/test"))
               .andExpect(status().isOk());
        
        // 验证日志输出
    }
}
```

#### 3. 性能测试

```java
@Test
void testPerformance() {
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < 10000; i++) {
        formatter.formatArguments(joinPoint, request, properties);
    }
    
    long duration = System.currentTimeMillis() - startTime;
    assertThat(duration).isLessThan(1000); // 10000次调用应在1秒内完成
}
```

### 运行测试

```bash
# 运行所有测试
mvn clean test

# 生成测试覆盖率报告
mvn clean test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

---

## 文档规范

### 代码文档

- **所有公共类和方法**必须有 JavaDoc
- **复杂逻辑**必须有行内注释
- **配置项**必须有说明

### README 更新

如果你的变更影响用户使用，请更新 README.md：

- 新功能：添加使用示例
- 配置变更：更新配置说明
- API 变更：更新 API 文档

### CHANGELOG 更新

在 CHANGELOG.md 中记录你的变更：

```markdown
## [Unreleased]

### Added
- 新增基于响应时间的过滤器 (#123)

### Fixed
- 修复 ThreadLocal 内存泄漏问题 (#456)

### Changed
- 优化 JSON 序列化性能 (#789)
```

---

## 发布流程

（仅限维护者）

### 版本号规范

遵循 [语义化版本](https://semver.org/lang/zh-CN/)：

- **主版本号**：不兼容的 API 变更
- **次版本号**：向下兼容的功能新增
- **修订号**：向下兼容的问题修正

### 发布步骤

1. 更新 CHANGELOG.md
2. 更新 pom.xml 中的版本号
3. 创建 Git Tag
4. 推送到 GitHub
5. 发布到 JitPack

---

## 常见问题

### Q: 我不熟悉 Java，可以贡献文档吗？

A: 当然可以！文档改进同样重要。

### Q: 我的 PR 多久会被 Review？

A: 通常在 3-5 个工作日内。如果超过一周没有响应，可以在 PR 中 @ 维护者。

### Q: 我可以同时提交多个功能吗？

A: 建议每个 PR 只包含一个功能或修复，这样更容易 Review 和合并。

### Q: 测试失败了怎么办？

A: 检查错误信息，修复问题后重新提交。如果需要帮助，可以在 PR 中说明。

---

## 联系方式

- **GitHub Issues**: https://github.com/mr-box/api-logging-spring-boot-starter/issues
- **Email**: [wel.come@qq.com]

---

## 致谢

感谢所有为这个项目做出贡献的开发者！

你的贡献将被记录在 [Contributors](https://github.com/mr-box/api-logging-spring-boot-starter/graphs/contributors) 页面。

---

**再次感谢你的贡献！** 🎉
