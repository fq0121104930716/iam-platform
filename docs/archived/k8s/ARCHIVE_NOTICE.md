# K8s 部署配置归档说明

## 归档日期
2026-05-09

## 归档原因
项目已从 Kubernetes 部署方式迁移至纯 Docker 部署方式，为简化项目结构和降低运维复杂度，原 K8s 相关配置和文档已归档至此目录。

## 归档内容
- `k8s/` - 原 Kubernetes 部署配置文件目录
  - `base/` - Kustomize 基础配置
  - `overlays/` - 各环境 Kustomize 覆盖配置

## 当前部署方式
项目现在使用以下方式进行部署：
- **Docker 直接部署** - 使用 `docker run` 命令
- **Docker Compose** - 使用 `docker-compose.yml` 配置文件
- **CI/CD 脚本** - `ci-build.ps1` 和 `uninstall-env.ps1` 已更新为 Docker 部署模式

## 恢复说明
如未来需要恢复 K8s 部署支持，可从此归档目录恢复相关文件，并相应更新：
- `ci-build.ps1` - 恢复 K8s 部署逻辑
- `uninstall-env.ps1` - 恢复 K8s 卸载逻辑
- 相关部署文档 - 恢复 K8s 部署说明

## 注意事项
- 归档文件仅作为历史参考，不再维护
- 新功能和修复仅针对当前 Docker 部署方式
- 如需 K8s 支持，建议基于当前代码库重新实现
