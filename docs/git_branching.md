# Git 分支管理规范（适用于小型项目）

> 本文档定义了本项目使用的 Git 分支策略及命名规范，用于提升代码管理的可维护性、可协作性和发布效率。

## 分支模型概览

```bash
main               # 主分支，稳定版本（生产）
│
├── dev            # 开发分支（可选）
│
├── feature/*      # 功能开发分支
│
├── fix/*          # Bug 修复分支
│
└── release/*      # 发布准备分支
```

## 主分支说明

| 分支     | 说明                       |
|--------|--------------------------|
| `main` | 生产主分支，始终保持稳定状态，只允许合并稳定功能 |
| `dev`  | （可选）主开发分支，用于多人协作或统一功能集成  |

## 功能开发流程

1. 从 `main` 或 `dev` 创建功能分支：
   ```bash
   git checkout main
   git pull
   git checkout -b feature/<name>
   ```

2. 在 `feature/<name>` 中进行开发。

3. 合并功能分支时使用 `--no-ff` 保留合并记录：
   ```bash
   git checkout main
   git merge feature/<name> --no-ff
   ```

4. 合并成功后删除功能分支（可选）：
   ```bash
   git branch -d feature/<name>
   ```

## 分支命名规范

| 类型     | 命名示例                        | 说明         |
|--------|-----------------------------|------------|
| 功能开发   | `feature/mediaplayer`       | 新功能模块开发    |
| Bug 修复 | `fix/playback-crash`        | Bug 修复或小调整 |
| 发布准备   | `release/v1.0.0`            | 准备发布的新版本   |
| 紧急修复   | `hotfix/fix-login-bug`      | 生产环境紧急修复   |
| 实验分支   | `experiment/idea-player-ui` | 临时试验性分支    |

## 合并策略说明

- 所有合并到 `main` 的操作，必须使用以下命令：
  ```bash
  git merge <branch> --no-ff
  ```
  保留合并提交，便于历史追踪。

- 使用 Pull Request（PR）或 Merge Request（MR）进行合并审核（如果使用 GitHub / GitLab）。

## 示例：添加 `mediaplayer` 应用

```bash
git checkout main
git checkout -b feature/mediaplayer
# ...开发中...
git add .
git commit -m "Add basic mediaplayer app"
git checkout main
git merge feature/mediaplayer --no-ff
git branch -d feature/mediaplayer
```
