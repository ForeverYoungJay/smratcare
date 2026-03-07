#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

INIT_SQL="docker/mysql/init/2_data.sql"

if [[ ! -f "$INIT_SQL" ]]; then
  echo "未找到初始化数据文件：$INIT_SQL"
  exit 1
fi

echo "== RBAC 初始化数据静态审计开始 =="
echo

echo "[1/6] 检查 6 部门编码是否完整"
dept_codes=(CARE MED FIN LOGI HR MKT)
for code in "${dept_codes[@]}"; do
  if ! rg -nF "'$code'" "$INIT_SQL" >/dev/null; then
    echo "缺少部门编码：$code"
    exit 1
  fi
done
echo "通过：6 部门编码完整。"
echo

echo "[2/6] 检查角色编码是否完整"
role_codes=(
  ADMIN SYS_ADMIN DIRECTOR
  NURSING_EMPLOYEE NURSING_MINISTER
  MEDICAL_EMPLOYEE MEDICAL_MINISTER
  FINANCE_EMPLOYEE FINANCE_MINISTER
  LOGISTICS_EMPLOYEE LOGISTICS_MINISTER
  HR_EMPLOYEE HR_MINISTER
  MARKETING_EMPLOYEE MARKETING_MINISTER
)
for code in "${role_codes[@]}"; do
  if ! rg -nF "'$code'" "$INIT_SQL" >/dev/null; then
    echo "缺少角色编码：$code"
    exit 1
  fi
done
echo "通过：角色编码完整。"
echo

echo "[3/6] 检查关键账号是否完整"
users=(
  admin
  nursing_emp nursing_minister nursing_director
  medical_emp medical_minister medical_director
  finance_emp finance_minister finance_director
  logistics_emp logistics_minister logistics_director
  hr_emp hr_minister hr_director
  marketing_emp marketing_minister marketing_director
)
for username in "${users[@]}"; do
  if ! rg -n "'.*', '$username'," "$INIT_SQL" >/dev/null; then
    echo "缺少账号：$username"
    exit 1
  fi
done
echo "通过：账号矩阵完整。"
echo

echo "[4/6] 检查 admin 是否归属 HR 部门"
if ! rg -n "\(500, 1, 14, 'S0001', 'admin'," "$INIT_SQL" >/dev/null; then
  echo "admin 未归属 HR（department_id=14）。"
  exit 1
fi
echo "通过：admin 已归属 HR。"
echo

echo "[5/6] 检查迁移补丁是否存在"
if [[ ! -f "src/main/resources/db/migration/V146__admin_department_to_hr.sql" ]]; then
  echo "缺少迁移：V146__admin_department_to_hr.sql"
  exit 1
fi
if [[ ! -f "src/main/resources/db/migration/V147__seed_demo_staff_for_6_departments_3_roles.sql" ]]; then
  echo "缺少迁移：V147__seed_demo_staff_for_6_departments_3_roles.sql"
  exit 1
fi
echo "通过：V146/V147 迁移文件存在。"
echo

echo "[6/6] 检查 staff_role 是否包含 admin 双角色绑定"
if ! rg -n "\(600, 1, 500, 100," "$INIT_SQL" >/dev/null; then
  echo "缺少 admin -> ADMIN 绑定。"
  exit 1
fi
if ! rg -n "\(601, 1, 500, 101," "$INIT_SQL" >/dev/null; then
  echo "缺少 admin -> SYS_ADMIN 绑定。"
  exit 1
fi
echo "通过：admin 双角色绑定完整。"
echo

echo "== RBAC 初始化数据静态审计通过 =="
