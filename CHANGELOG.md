# 更新日志 (Changelog)

本项目的所有重要更改都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本 (Semantic Versioning)](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-01-22

### 新增 (Added)
- api-logging SDK 初始版本发布。
- 支持精简 (SIMPLE) 和详细 (DETAILED) 两种日志模式。
- 支持可配置的详细日志触发器 (异常、状态码、请求头、自定义)。
- 支持请求参数和请求头的敏感数据脱敏。
- 支持过滤器扩展 (PreFilter/PostFilter)。
- 集成 Spring Boot 自动配置。
