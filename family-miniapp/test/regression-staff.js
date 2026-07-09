/* 员工端小程序走查：员工身份登录 → staff 分包全页面断言 + 截图 */
const automator = require('miniprogram-automator')
const fs = require('fs')

const SHOT_DIR = __dirname + '/shots-staff'
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

  await mini.callWxMethod('setStorageSync', 'smartcare_family_runtime_flags', {
    enableMockFallback: false,
    enableLocalDevBypassLogin: false,
    allowManualRechargeFallback: false,
    baseUrlOverride: 'http://127.0.0.1:8080'
  })
  await mini.callWxMethod('removeStorageSync', 'smartcare_family_token')
  await mini.callWxMethod('removeStorageSync', 'smartcare_family_user')
  await mini.evaluate(() => {
    const app = getApp()
    app.globalData.token = ''
    app.globalData.userType = ''
    app.globalData.familyUser = null
    app.globalData.staffUser = null
  })
  let page = await mini.reLaunch('/pages/login/index')
  await page.waitFor(2500)
  page = await mini.currentPage()
  if (page.path !== 'pages/login/index') {
    page = await mini.reLaunch('/pages/login/index')
    await page.waitFor(2500)
    page = await mini.currentPage()
  }
  log('打开登录页', page.path === 'pages/login/index', page.path)

  // 切换员工身份
  const tabs = await page.$$('.identity')
  let staffTab = null
  for (const t of tabs) { if (((await t.text()) || '').includes('员工')) { staffTab = t; break } }
  log('找到员工身份切换', !!staffTab)
  await staffTab.tap()
  await page.waitFor(800)

  const inputs = await page.$$('input')
  log('员工登录输入框', inputs.length >= 2, `count=${inputs.length}`)
  await inputs[0].input('qa_nursing_emp')
  await inputs[1].input('123456')
  const btns = await page.$$('button')
  let loginBtn = null
  for (const b of btns) {
    const t = ((await b.text()) || '').replace(/\s/g, '')
    if (t.includes('登录') && !t.includes('微信')) { loginBtn = b; break }
  }
  await loginBtn.tap()
  await page.waitFor(3000)
  page = await mini.currentPage()
  log('登录进入员工工作台', page.path === 'packageStaff/pages/staff-home/index', page.path)
  await page.waitFor(1500)
  await mini.screenshot({ path: `${SHOT_DIR}/01-staff-home.png` })
  const homeText = await pageText(page)
  log('工作台内容', homeText.replace(/\s/g, '').length > 20, `len=${homeText.length}`)
  log('工作台无枚举泄漏', !/[A-Z]{4,}_[A-Z]+/.test(homeText), (homeText.match(/[A-Z]{4,}_[A-Z]+/) || [''])[0])

  const pages = [
    ['/packageStaff/pages/staff-tasks/index', '护理任务'],
    ['/packageStaff/pages/staff-vitals/index', '体征录入'],
    ['/packageStaff/pages/staff-schedule/index', '我的排班'],
    ['/packageStaff/pages/staff-attendance/index', '考勤打卡'],
    ['/packageStaff/pages/staff-handover/index', '交接班'],
    ['/packageStaff/pages/staff-patrol/index', '巡查'],
    ['/packageStaff/pages/staff-incident/index', '异常上报'],
    ['/packageStaff/pages/staff-repairs/index', '报修'],
    ['/packageStaff/pages/staff-meals/index', '送餐'],
    ['/packageStaff/pages/staff-material/index', '物资'],
    ['/packageStaff/pages/staff-approval/index', '审批'],
    ['/packageStaff/pages/staff-notices/index', '公告'],
    ['/packageStaff/pages/staff-todo/index', '待办'],
    ['/packageStaff/pages/staff-suggestions/index', '建议'],
    ['/packageStaff/pages/staff-elder-card/index', '床边档案'],
    ['/packageStaff/pages/staff-profile/index', '我的']
  ]
  let idx = 2
  for (const [route, name] of pages) {
    try {
      page = await mini.reLaunch(route)
      await page.waitFor(2200)
      const actual = (await mini.currentPage()).path
      const body = await pageText(await mini.currentPage())
      const blank = !body || body.replace(/\s/g, '').length < 8
      const enumLeak = (body.match(/[A-Z]{4,}_[A-Z]+/) || [''])[0]
      const isoLeak = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(body)
      log(`页面:${name}`, !blank && actual.startsWith('packageStaff'),
        `${actual}${blank ? ' 疑似白屏' : ' len=' + body.length}${enumLeak ? ' 枚举:' + enumLeak : ''}${isoLeak ? ' ISO泄漏' : ''}`)
      await mini.screenshot({ path: `${SHOT_DIR}/${String(idx).padStart(2, '0')}-${route.split('/')[3]}.png` })
      idx++
    } catch (e) {
      log(`页面:${name}`, false, e.message.slice(0, 80))
    }
  }

  console.log('\nSUMMARY:', results.filter(r => r.ok).length + '/' + results.length, 'passed')
  const failed = results.filter(r => !r.ok).length
  // disconnect() 偶发挂起：3 秒兜底强制退出
  setTimeout(() => process.exit(failed ? 1 : 0), 3000).unref?.()
  try { await mini.disconnect() } catch (e) {}
  process.exit(failed ? 1 : 0)
}

main().catch(e => { console.error('FATAL:', e.message); process.exit(1) })
