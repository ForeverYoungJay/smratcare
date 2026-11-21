<template>
  <div class="of-page">
    <section class="mi-banner" :style="{ '--of-hero-img': `url(${visualAssets.scenicMountain.url})` }">
      <h2>个人信息中心</h2>
      <p>查看个人档案与岗位信息，保障账号安全；正式档案由行政人事部维护，更正请走「资料更正」申请</p>
    </section>

    <div class="mi-grid">
      <aside class="of-card mi-profile">
        <div class="mi-avatar">{{ (profile?.realName || '我').slice(0, 1) }}</div>
        <h3>{{ profile?.realName || '--' }} <span v-if="profile?.jobTitle" class="of-pill tone-green">{{ profile.jobTitle }}</span></h3>
        <div class="mi-facts">
          <div><BankOutlined /><span>所属部门</span><b>{{ profile?.departmentName || '-' }}</b></div>
          <div><IdcardOutlined /><span>工号</span><b>{{ profile?.staffNo || '-' }}</b></div>
          <div><PhoneOutlined /><span>联系电话</span><b>{{ profile?.phone || '-' }}</b></div>
          <div><CheckCircleOutlined /><span>在职状态</span><b><span class="of-pill" :class="profile?.onJob ? 'tone-green' : 'tone-orange'">{{ profile?.onJob ? '在职' : '非在职' }}</span></b></div>
        </div>
        <a-button type="primary" block @click="router.push('/oa/approval?type=PROFILE_CHANGE&quick=1')"><EditOutlined /> 申请更正资料</a-button>
        <a-button block @click="pwdOpen = true"><LockOutlined /> 修改密码</a-button>
        <p class="of-muted">员工不能直接修改正式档案；提交更正申请后由人事核对并更新。</p>
      </aside>

      <section class="of-card">
        <a-tabs v-model:active-key="tab">
          <a-tab-pane key="basic" tab="基本信息" />
          <a-tab-pane key="post" tab="岗位信息" />
          <a-tab-pane key="contract" tab="合同与证书" />
          <a-tab-pane key="contact" tab="紧急联系人" />
        </a-tabs>
        <a-spin :spinning="loading">
          <div v-show="tab === 'basic'" class="mi-section">
            <h4><UserOutlined />基本信息</h4>
            <dl class="mi-dl">
              <dt>姓名</dt><dd>{{ profile?.realName || '-' }}</dd>
              <dt>账号</dt><dd>{{ profile?.username || '-' }}</dd>
              <dt>联系电话</dt><dd>{{ profile?.phone || '-' }}</dd>
              <dt>电子邮箱</dt><dd>{{ profile?.email || '-' }}</dd>
              <dt>身份证、银行卡</dt><dd class="of-muted">敏感信息不在此显示，如需核对请联系人事</dd>
            </dl>
          </div>
          <div v-show="tab === 'post'" class="mi-section">
            <h4><SolutionOutlined />岗位信息</h4>
            <dl class="mi-dl">
              <dt>所属部门</dt><dd>{{ profile?.departmentName || '-' }}</dd>
              <dt>岗位</dt><dd>{{ profile?.jobTitle || '-' }}</dd>
              <dt>用工类型</dt><dd>{{ profile?.employmentType || '-' }}</dd>
              <dt>入职日期</dt><dd>{{ profile?.hireDate || '-' }}</dd>
              <dt>资质等级</dt><dd>{{ profile?.qualificationLevel || '-' }}</dd>
            </dl>
          </div>
          <div v-show="tab === 'contract'" class="mi-section">
            <h4><FileProtectOutlined />劳动合同</h4>
            <dl class="mi-dl">
              <dt>合同类型</dt><dd>{{ profile?.contractType || '-' }}</dd>
              <dt>合同期限</dt><dd>{{ profile?.contractStartDate ? `${profile.contractStartDate} 至 ${profile.contractEndDate || '长期'}` : '-' }}</dd>
              <dt>合同状态</dt><dd>{{ profile?.contractStatus || '-' }}</dd>
            </dl>
            <h4><SafetyCertificateOutlined />执业证书</h4>
            <div v-if="!profile?.certificates?.length" class="of-empty">暂无登记的证书</div>
            <div v-for="(c, i) in profile?.certificates || []" :key="i" class="of-row">
              <div class="of-row__main"><b>{{ c.name }}</b><small>证书编号 {{ c.certificateNo || '-' }}</small></div>
              <span class="of-pill" :class="certTone(c.validUntil)">{{ c.validUntil ? `有效至 ${c.validUntil}` : '长期' }}</span>
            </div>
          </div>
          <div v-show="tab === 'contact'" class="mi-section">
            <h4><TeamOutlined />紧急联系人</h4>
            <div v-if="!profile?.emergencyContactName" class="of-empty">未登记紧急联系人，请提交资料更正申请补充</div>
            <div v-else class="mi-contact">
              <span class="of-kpi__icon tone-green"><UserOutlined /></span>
              <div class="of-row__main"><b>{{ profile.emergencyContactName }}</b><small>联系电话 {{ profile.emergencyContactPhone || '-' }}</small></div>
              <span class="of-pill tone-green">主要联系人</span>
            </div>
          </div>
        </a-spin>
      </section>

      <aside class="of-page">
        <section class="of-card">
          <div class="of-card__head"><h3><SafetyOutlined />账号安全</h3></div>
          <button type="button" class="mi-action" @click="pwdOpen = true">
            <span class="of-kpi__icon tone-green"><LockOutlined /></span>
            <div class="of-row__main"><b>修改密码</b><small>至少 8 位，包含字母和数字</small></div><RightOutlined />
          </button>
          <div class="mi-action">
            <span class="of-kpi__icon tone-blue"><MobileOutlined /></span>
            <div class="of-row__main"><b>绑定手机</b><small>{{ profile?.phone || '未登记' }}，用于登录验证和重要通知</small></div>
            <span class="of-pill" :class="profile?.phone ? 'tone-green' : 'tone-orange'">{{ profile?.phone ? '已绑定' : '未绑定' }}</span>
          </div>
          <div class="mi-action">
            <span class="of-kpi__icon tone-purple"><DesktopOutlined /></span>
            <div class="of-row__main"><b>登录设备</b><small>远程退出其他设备暂未开通</small></div>
          </div>
        </section>
        <section class="of-card">
          <div class="of-card__head"><h3><HistoryOutlined />最近登录记录</h3><span class="of-muted">不是本人登录请立即改密码</span></div>
          <div v-if="!logins.length" class="of-empty">暂无记录</div>
          <div v-for="(l, i) in logins.slice(0, 5)" :key="i" class="of-row">
            <span class="of-kpi__icon" :class="l.result === 'SUCCESS' ? 'tone-green' : 'tone-red'"><DesktopOutlined /></span>
            <div class="of-row__main"><b>{{ browser(l.userAgent) }}</b><small>{{ l.time ? dayjs(l.time).format('YYYY-MM-DD HH:mm') : '-' }} · {{ l.ip || '-' }}</small></div>
            <span class="of-pill" :class="l.result === 'SUCCESS' ? 'tone-green' : 'tone-red'">{{ l.result === 'SUCCESS' ? '成功' : '失败' }}</span>
          </div>
        </section>
      </aside>
    </div>

    <a-modal v-model:open="pwdOpen" title="修改密码" :confirm-loading="saving" @ok="savePassword">
      <a-form layout="vertical">
        <a-form-item label="原密码" required><a-input-password v-model:value="pwd.oldPassword" autocomplete="current-password" /></a-form-item>
        <a-form-item label="新密码" required :extra="passwordProblem(pwd.newPassword) || '符合要求'">
          <a-input-password v-model:value="pwd.newPassword" autocomplete="new-password" />
        </a-form-item>
        <a-form-item label="确认新密码" required><a-input-password v-model:value="pwd.confirm" autocomplete="new-password" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  BankOutlined, CheckCircleOutlined, DesktopOutlined, EditOutlined, FileProtectOutlined, HistoryOutlined, IdcardOutlined, LockOutlined,
  MobileOutlined, PhoneOutlined, RightOutlined, SafetyCertificateOutlined, SafetyOutlined, SolutionOutlined, TeamOutlined, UserOutlined
} from '@ant-design/icons-vue'
import { changeMyPassword, getMyLogins, getMyProfile, type MyLogin, type MyProfile } from '../../api/me'
import { visualAssets } from '../../config/visualAssets'
import { passwordProblem } from './myOffice'
import './office/office.css'

const router = useRouter()
const tab = ref('basic')
const loading = ref(false)
const saving = ref(false)
const profile = ref<MyProfile>()
const logins = ref<MyLogin[]>([])
const pwdOpen = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

function browser(ua?: string) {
  if (!ua) return '未知设备'
  const os = /iPhone|iPad/.test(ua) ? 'iOS' : /Android/.test(ua) ? 'Android' : /Mac OS/.test(ua) ? 'macOS' : /Windows/.test(ua) ? 'Windows' : ''
  const app = /MicroMessenger/.test(ua) ? '微信' : /Edg\//.test(ua) ? 'Edge' : /Chrome\//.test(ua) ? 'Chrome' : /Safari\//.test(ua) ? 'Safari' : '浏览器'
  return [os, app].filter(Boolean).join(' · ')
}
function certTone(validUntil?: string) {
  if (!validUntil) return 'tone-green'
  const days = dayjs(validUntil).diff(dayjs(), 'day')
  return days < 0 ? 'tone-red' : days < 60 ? 'tone-orange' : 'tone-green'
}

async function load() {
  loading.value = true
  const [p, l] = await Promise.allSettled([getMyProfile(), getMyLogins()])
  profile.value = p.status === 'fulfilled' ? p.value : undefined
  logins.value = l.status === 'fulfilled' ? l.value : []
  loading.value = false
}

async function savePassword() {
  const problem = passwordProblem(pwd.newPassword)
  if (!pwd.oldPassword) return message.warning('请输入原密码')
  if (problem) return message.warning(problem)
  if (pwd.newPassword !== pwd.confirm) return message.warning('两次输入的新密码不一致')
  saving.value = true
  try {
    await changeMyPassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    message.success('密码已修改，下次登录请使用新密码')
    Object.assign(pwd, { oldPassword: '', newPassword: '', confirm: '' })
    pwdOpen.value = false
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mi-banner { position: relative; padding: 26px 32px; overflow: hidden; border-radius: 18px; background: linear-gradient(100deg, #fff7ec, #fdf1e3 55%, #f3f8f2); border: 1px solid #f1e5d3; }
.mi-banner::after { content: ''; position: absolute; inset: 0 0 0 55%; opacity: 0.5; background: var(--of-hero-img) center / cover;
  -webkit-mask-image: linear-gradient(90deg, transparent, #000 50%); mask-image: linear-gradient(90deg, transparent, #000 50%); }
.mi-banner h2 { position: relative; z-index: 1; margin: 0 0 6px; font-size: 28px; font-weight: 800; color: #1d3a2f; }
.mi-banner p { position: relative; z-index: 1; margin: 0; color: #4f6258; }
.mi-grid { display: grid; grid-template-columns: minmax(240px, 0.8fr) minmax(0, 2fr) minmax(280px, 1fr); gap: 16px; align-items: start; }
.mi-profile { display: grid; gap: 10px; justify-items: center; text-align: center; }
.mi-avatar { display: grid; place-items: center; width: 110px; height: 110px; border-radius: 50%; background: linear-gradient(135deg, #1f8a63, #126b4a); color: #fff; font-size: 44px; font-weight: 700; box-shadow: 0 0 0 6px #eef8f2; }
.mi-profile h3 { display: flex; align-items: center; gap: 8px; margin: 4px 0 0; font-size: 22px; color: #1d3a2f; }
.mi-facts { display: grid; width: 100%; gap: 2px; text-align: left; }
.mi-facts div { display: grid; grid-template-columns: 18px 72px 1fr; align-items: center; gap: 6px; padding: 8px 0; border-bottom: 1px solid #f0f3f1; color: #17734f; }
.mi-facts span { color: #6b7a74; font-size: 13px; }
.mi-facts b { color: #22332e; font-weight: 600; }
.mi-section h4 { display: flex; align-items: center; gap: 8px; margin: 4px 0 10px; font-size: 16px; color: #1d3a2f; }
.mi-section h4 .anticon { color: #17734f; }
.mi-dl { display: grid; grid-template-columns: 120px 1fr; gap: 0; margin: 0 0 16px; border: 1px solid #eef2ef; border-radius: 12px; overflow: hidden; }
.mi-dl dt, .mi-dl dd { margin: 0; padding: 10px 14px; border-bottom: 1px solid #eef2ef; }
.mi-dl dt { background: #f6faf7; color: #6b7a74; }
.mi-dl dd { color: #22332e; }
.mi-contact { display: flex; align-items: center; gap: 12px; padding: 14px; border-radius: 12px; background: #f6faf7; }
.mi-action { display: flex; align-items: center; gap: 12px; width: 100%; padding: 12px; margin-bottom: 10px; border: 1px solid #e8eee9; border-radius: 12px; background: #fff; text-align: left; }
button.mi-action { cursor: pointer; }
button.mi-action:hover { border-color: #1f9d74; }
@media (max-width: 1200px) { .mi-grid { grid-template-columns: minmax(240px, 0.8fr) minmax(0, 2fr); } .mi-grid > :last-child { grid-column: 1 / -1; } }
@media (max-width: 860px) { .mi-grid { grid-template-columns: 1fr; } }
</style>
