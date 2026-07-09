# 家属小程序自动化回归

基于 [miniprogram-automator](https://developers.weixin.qq.com/miniprogram/dev/devtools/auto/) 驱动微信开发者工具，对家属端做登录 + 10 个核心页面的走查断言（非白屏 / 无原始枚举泄漏 / 无 ISO 时间泄漏），并逐页截图。

## 前置条件

1. 本地后端已启动（`docker compose up -d`，端口 8080）；
2. 微信开发者工具已安装，并在「设置 → 安全设置」中开启**服务端口**；
3. 家属测试账号：`13900001234 / 123456`（若密码失效，用 mock 短信重置：
   `POST /api/auth/family/sms-code/send` 场景 `RESET_PASSWORD` 会回显 `debugCode`，
   再 `POST /api/auth/family/password/reset`）。

## 运行

```bash
npm i miniprogram-automator          # 任意目录安装一次
# 启动带自动化端口的工具窗口（首次会提示确认）
"/Applications/wechatwebdevtools.app/Contents/MacOS/cli" auto \
  --project <repo>/family-miniapp --auto-port 9420
node regression.js                    # 输出 PASS/FAIL 清单与 SUMMARY，截图在 ./shots
```

说明：脚本会通过 `baseUrlOverride` 运行时开关把小程序指向 `http://127.0.0.1:8080`，
不修改任何业务代码；健康页会自动完成“敏感访问安全验证”（使用登录密码）。

注意：`miniprogram-automator` 的 `disconnect()` 会结束工具的自动化会话，
重跑前需重新执行上面的 `cli auto` 命令。
