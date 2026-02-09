# Git Commit Message

以下是对你项目提交类型的简化说明，并结合提交记录提供了每种类型的示例，便于团队理解和统一使用格式：

feat: 新功能
> feat: add click support for teaching aids
> feat: add whiteboard camera apis

fix: 修复问题
> fix: resolve page blocking issue when opening multiple files quickly
> fix: window tab error on first entry

BREAKING CHANGE: 破坏性变更
> refactor!: change RoomOptions API

refactor: 重构（非功能性更改，无行为变化）
> refactor: extract Application interface and refactor AbstractApplication

chore: 杂项（工具/依赖/配置变更）
> chore: move whiteboard, windowmanager, imagerydoc to forge-room

docs: 文档, 改动说明文档内容，如 README、注释等。
> docs: update README to add usage example

style: 样式, 不影响代码执行的更改，如格式、空格、标点等。
> style: format WhiteboardActivity file

perf: 性能优化,提高运行效率或减少资源消耗。
> perf: optimize application startup time

test: 测试: 添加、更新或修复测试代码。
> test: add whiteboard region merge test case

build: 构建系统相关
> build: update jsbridge to es5
> build: upgrade quickjs to 0.1.2

ci: 持续集成相关: 修改 CI 流程或配置文件。
> ci: update github actions for Android SDK
