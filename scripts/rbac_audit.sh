#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "== RBAC 审计开始 =="
echo

echo "[1/5] 检查后端遗留 hasRole('ADMIN')"
if rg -nF "@PreAuthorize(\"hasRole('ADMIN')\")" src/main/java; then
  echo "发现遗留 hasRole('ADMIN')，请修复。"
  exit 1
else
  echo "通过：未发现 hasRole('ADMIN')。"
fi
echo

echo "[2/5] 检查后端泛权限 hasAnyRole('ADMIN','STAFF')"
if rg -nF "@PreAuthorize(\"hasAnyRole('ADMIN','STAFF')\")" src/main/java; then
  echo "发现泛权限 hasAnyRole('ADMIN','STAFF')，请收敛到显式部门角色。"
  exit 1
else
  echo "通过：未发现 hasAnyRole('ADMIN','STAFF')。"
fi
echo

echo "[3/5] 检查后端旧营销角色 MANAGER/OPERATOR（注解层）"
if rg -n "@PreAuthorize\\(\"hasAnyRole\\([^\\)]*(MANAGER|OPERATOR)" src/main/java/com/zhiyangyun/care/crm -S; then
  echo "发现旧营销角色 MANAGER/OPERATOR 的鉴权表达式，建议迁移到 MARKETING_*。"
  exit 1
else
  echo "通过：CRM 控制器未发现旧营销角色表达式。"
fi
echo

echo "[4/5] 检查前端角色归一化是否包含旧营销角色映射"
if ! rg -n "OPERATOR|MANAGER|MARKETING_EMPLOYEE|MARKETING_MINISTER" admin-web/src/utils/auth.ts admin-web/src/utils/auth.js -S; then
  echo "未发现前端旧营销角色映射，请补充 normalizeRoles 兼容。"
  exit 1
else
  echo "通过：前端包含旧营销角色映射。"
fi
echo

echo "[5/5] 检查路由是否接入统一权限判断"
if ! rg -n "hasRouteAccess" admin-web/src/router/permission.ts admin-web/src/layouts/menu.ts admin-web/src/router/permission.js admin-web/src/layouts/menu.js -S; then
  echo "路由/菜单未统一接入 hasRouteAccess。"
  exit 1
else
  echo "通过：路由与菜单已统一接入 hasRouteAccess。"
fi
echo

echo "== RBAC 审计通过 =="
