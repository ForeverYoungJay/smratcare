/* 家属小程序自动化走查：连接微信开发者工具 → 本地后端 → 登录 → 核心页面遍历 */
const automator = require('miniprogram-automator')
const fs = require('fs')

const SHOT_DIR = __dirname + '/shots'
if (!fs.existsSync(SHOT_DIR)) fs.mkdirSync(SHOT_DIR)

const results = []
const log = (name, ok, detail = '') => {
  results.push({ name, ok, detail })
  console.log(`${ok ? 'PASS' : 'FAIL'} | ${name}${detail ? ' | ' + detail : ''}`)
}


async function pageText(page) {
  const root = await page.$('view')
  if (root) return (await root.text()) || ''
  const texts = await page.$$('text')
  const parts = []
  for (const t of texts.slice(0, 80)) parts.push((await t.text()) || '')
  return parts.join(' ')
}

async function main() {
  const mini = await automator.connect({ wsEndpoint: 'ws://localhost:9420' })
  console.log('connected')

  // 1) 指到本地后端 + 重启到登录页
  await mini.callWxMethod('setStorageSync', 'smartcare_family_runtime_flags', {
    enableMockFallback: false,
    enableLocalDevBypassLogin: false,
    allowManualRechargeFallback: false,
    baseUrlOverride: 'http://127.0.0.1:8080'
  })
  await mini.callWxMethod('removeStorageSync', 'smartcare_family_token')
  await mini.callWxMethod('removeStorageSync', 'smartcare_family_user')
  // App 实例内存态也要清，否则登录页 onShow 检测到旧会话会自动跳回首页
  await mini.evaluate(() => {
    const app = getApp()
    app.globalData.token = ''
    app.globalData.userType = ''
    app.globalData.familyUser = null
    app.globalData.staffUser = null
    app.globalData.selectedElderId = null
  })
  let page = await mini.reLaunch('/pages/login/index')
  await page.waitFor(3000)
  page = await mini.currentPage()
  if (page.path !== 'pages/login/index') {
    // IDE 重载后首次 reLaunch 可能被旧会话重定向，清理后再试一次
    await mini.callWxMethod('removeStorageSync', 'smartcare_family_token')
    await mini.callWxMethod('removeStorageSync', 'smartcare_family_user')
    await mini.evaluate(() => { const app = getApp(); app.globalData.token=''; app.globalData.userType=''; app.globalData.familyUser=null })
    page = await mini.reLaunch('/pages/login/index')
    await page.waitFor(2500)
    page = await mini.currentPage()
  }
  log('打开登录页', page.path === 'pages/login/index', page.path)
  await mini.screenshot({ path: `${SHOT_DIR}/01-login.png` })

  // 2) 登录（手机号+密码）
  const inputs = await page.$$('input')
  log('登录页输入框', inputs.length >= 2, `count=${inputs.length}`)
  await inputs[0].input('13900001234')
  await inputs[1].input('123456')
  const btns = await page.$$('button')
  let loginBtn = null
  for (const b of btns) {
    const t = ((await b.text()) || '').replace(/\s/g, '')
    if (t.includes('登录') && !t.includes('微信')) { loginBtn = b; break }
  }
  log('找到登录按钮', !!loginBtn)
  await loginBtn.tap()
  await page.waitFor(2500)
  page = await mini.currentPage()
  log('登录跳转首页', page.path === 'pages/home/index', page.path)
  await page.waitFor(2000)
  await mini.screenshot({ path: `${SHOT_DIR}/02-home.png` })
  const homeText = await pageText(page)
  log('首页出现长者信息', /王老太/.test(homeText), (homeText.match(/王老太[^\n]{0,12}/) || [''])[0])
  log('首页无原始枚举泄漏', !/[A-Z]{4,}_[A-Z]+/.test(homeText), (homeText.match(/[A-Z]{4,}_[A-Z]+/) || [''])[0])

  // 3) 健康页（敏感数据，先过安全验证：未设独立密码时用登录密码）
  try {
    let hp = await mini.reLaunch('/pages/health/index')
    await hp.waitFor(2500)
    hp = await mini.currentPage()
    if (hp.path === 'pages/security-verify/index') {
      const pwd = await hp.$('input')
      await pwd.input('123456')
      const vbtns = await hp.$$('button')
      let vbtn = null
      for (const b of vbtns) { const t = ((await b.text())||'').replace(/\s/g,''); if (/验证|确认|提交/.test(t)) { vbtn = b; break } }
      if (!vbtn) vbtn = vbtns[0]
      await vbtn.tap()
      await hp.waitFor(2500)
      hp = await mini.currentPage()
    }
    log('健康页(安全验证后)', hp.path === 'pages/health/index', hp.path)
    const htext = await pageText(hp)
    log('健康页内容', htext.replace(/\s/g,'').length > 8, `len=${htext.length}`)
    await mini.screenshot({ path: `${SHOT_DIR}/03-health.png` })
  } catch (e) { log('健康页(安全验证后)', false, e.message.slice(0,80)) }

  // 4) 核心页面遍历
  const pages = [
    ['/pages/payment/index', '缴费'],
    ['/pages/dynamic/index', '动态'],
    ['/pages/messages/index', '消息'],
    ['/pages/services/index', '服务预约'],
    ['/pages/schedule/index', '探视/日程'],
    ['/pages/meal/index', '膳食'],
    ['/pages/care-log/index', '护理日志'],
    ['/pages/weekly-brief/index', '每周简报'],
    ['/pages/survey-fill/index', '问卷填写'],
    ['/pages/profile/index', '我的']
  ]
  let idx = 3
  for (const [route, name] of pages) {
    try {
      page = await mini.reLaunch(route)
      await page.waitFor(2200)
      const body = await pageText(page)
      const blank = !body || body.replace(/\s/g, '').length < 8
      const hasEnumLeak = /[A-Z]{4,}_[A-Z]+/.test(body)
      const isoLeak = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(body)
      log(`页面:${name}`, !blank, blank ? '疑似白屏' : `len=${body.length}${hasEnumLeak ? ' 枚举泄漏:' + body.match(/[A-Z]{4,}_[A-Z]+/)[0] : ''}${isoLeak ? ' ISO时间泄漏' : ''}`)
      await mini.screenshot({ path: `${SHOT_DIR}/${String(idx).padStart(2, '0')}-${route.split('/')[2]}.png` })
      idx++
    } catch (e) {
      log(`页面:${name}`, false, e.message.slice(0, 80))
    }
  }

  console.log('\nSUMMARY:', results.filter(r => r.ok).length + '/' + results.length, 'passed')
  await mini.disconnect()
}

main().catch(e => { console.error('FATAL:', e.message); process.exit(1) })
