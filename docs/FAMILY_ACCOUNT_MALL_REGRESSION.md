# 家属账号 + 商城回归脚本

脚本路径：`scripts/family_account_mall_regression.sh`

## 作用范围

- 家属引导信息（自动机构）
- 注册验证码发送 + 首次注册
- 账号密码登录
- 敏感操作密码二次验证
- 绑定查询（可选身份证绑定）
- 商城商品查询 + 预检（可选提交下单）
- 商城订单查询 + 订单详情（含关键字段断言）
- 可选：订单取消 / 订单退款
- 可选：取消/退款后状态断言（`orderStatus=4/5`）
- 可选：找回密码（验证码重置 + 新密码登录）

## 前置条件

1. 后端服务已启动（默认：`http://localhost:8080`）。
2. 已安装 `jq` 与 `curl`。

## 快速执行

```bash
./scripts/family_account_mall_regression.sh
```

脚本会自动生成一个测试手机号（`139xxxxxxxx`）并执行注册登录链路。

## 常用参数

```bash
BASE_URL=http://localhost:8080 \
FAMILY_PHONE=13912345678 \
FAMILY_PASSWORD='Family@123' \
BIND_ID_CARD=31010119491231002X \
RUN_SUBMIT=true \
RUN_CANCEL=true \
RUN_REFUND=true \
RUN_RESET=true \
./scripts/family_account_mall_regression.sh
```

## 参数说明

- `BASE_URL`：后端地址，默认 `http://localhost:8080`
- `FAMILY_PHONE`：家属手机号；不传则自动生成
- `FAMILY_PASSWORD`：注册及登录密码
- `FAMILY_NEW_PASSWORD`：找回密码后的新密码（`RUN_RESET=true` 时使用）
- `REGISTER_CODE`：注册验证码（当未开启 debugCode 回传时必须手动传入）
- `RESET_CODE`：重置验证码（当未开启 debugCode 回传时必须手动传入）
- `BIND_ID_CARD`：若当前账号无绑定老人，可传身份证自动绑定
- `MALL_PRODUCT_ID`：指定商城商品ID（不传默认取首个）
- `MALL_QTY`：购买数量，默认 `1`
- `RUN_SUBMIT`：是否真实提交下单，默认 `false`
- `RUN_CANCEL`：是否执行订单取消回归（需要可取消订单），默认 `false`
- `RUN_REFUND`：是否执行订单退款回归（需要可退款订单），默认 `false`
- `RUN_RESET`：是否执行找回密码链路，默认 `false`

## 行为说明

- 当 `RUN_CANCEL=true` 且订单可取消时，脚本会执行取消并断言订单状态更新为 `4`。
- 当 `RUN_REFUND=true` 且订单可退款时，脚本会执行退款并断言订单状态更新为 `5`。
- 若订单当前不满足取消/退款条件，脚本会输出接口返回的 `cancelHint/refundHint` 并跳过该步骤，不影响其余链路回归。
