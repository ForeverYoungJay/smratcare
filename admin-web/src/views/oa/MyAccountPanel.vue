<template>
  <div class="ma-grid">
    <section class="ma-card">
      <div class="ma-head">
        <b>我的档案</b>
        <span class="ma-muted">正式档案由行政人事部维护，如需更正请联系人事；身份证、银行卡不在此显示</span>
      </div>
      <a-spin :spinning="loading">
        <a-descriptions v-if="profile" size="small" bordered :column="{ xs: 1, md: 2 }">
          <a-descriptions-item label="姓名">{{ profile.realName }} <a-tag v-if="profile.onJob" color="green">在职</a-tag></a-descriptions-item>
          <a-descriptions-item label="工号">{{ profile.staffNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="部门">{{ profile.departmentName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="岗位">{{ profile.jobTitle || '-' }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ profile.phone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="入职日期">{{ profile.hireDate || '-' }}</a-descriptions-item>
          <a-descriptions-item label="用工类型">{{ profile.employmentType || '-' }}</a-descriptions-item>
          <a-descriptions-item label="资质等级">{{ profile.qualificationLevel || '-' }}</a-descriptions-item>
          <a-descriptions-item label="劳动合同" :span="2">
            {{ profile.contractType || '-' }}{{ profile.contractStartDate ? ` · ${profile.contractStartDate} 至 ${profile.contractEndDate || '长期'}` : '' }}
          </a-descriptions-item>
          <a-descriptions-item label="紧急联系人" :span="2">{{ profile.emergencyContactName || '-' }} {{ profile.emergencyContactPhone || '' }}</a-descriptions-item>
          <a-descriptions-item label="证书" :span="2">
            <span v-if="!profile.certificates.length">暂无登记</span>
            <div v-for="(c, i) in profile.certificates" :key="i">{{ c.name }} {{ c.certificateNo || '' }}{{ c.validUntil ? ` · 有效至 ${c.validUntil}` : '' }}</div>
          </a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </section>

    <section class="ma-card">
      <div class="ma-head"><b>账号安全</b></div>
      <button type="button" class="ma-action" @click="pwdOpen = true">
        <LockOutlined /><div><b>修改密码</b><span>至少 8 位，包含字母和数字</span></div>
      </button>
      <div class="ma-head ma-head--sub"><b>最近登录记录</b><span class="ma-muted">发现不是本人登录请立即修改密码</span></div>
      <a-empty v-if="!logins.length" description="暂无记录" />
      <div v-for="(l, i) in logins" :key="i" class="ma-login">
        <div>
          <b>{{ l.time ? dayjs(l.time).format('YYYY-MM-DD HH:mm') : '-' }}</b>
          <span class="ma-muted">{{ l.ip || '' }} {{ browser(l.userAgent) }}</span>
        </div>
        <a-tag :color="l.result === 'SUCCESS' ? 'green' : 'red'">{{ l.result === 'SUCCESS' ? '成功' : '失败' }}</a-tag>
      </div>
      <p class="ma-muted">「登录设备管理（远程退出）」暂未开通。</p>
    </section>

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
import { LockOutlined } from '@ant-design/icons-vue'
import { changeMyPassword, getMyLogins, getMyProfile, type MyLogin, type MyProfile } from '../../api/me'
import { passwordProblem } from './myOffice'

const loading = ref(false)
const saving = ref(false)
const profile = ref<MyProfile>()
const logins = ref<MyLogin[]>([])
const pwdOpen = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

function browser(ua?: string) {
  if (!ua) return ''
  if (/Edg\//.test(ua)) return 'Edge'
  if (/Chrome\//.test(ua)) return 'Chrome'
  if (/Safari\//.test(ua)) return 'Safari'
  if (/MicroMessenger/.test(ua)) return '微信'
  return '其他浏览器'
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
.ma-grid { display: grid; grid-template-columns: minmax(0, 1.7fr) minmax(280px, 1fr); gap: 16px; margin-bottom: 16px; }
.ma-card { padding: 16px; border-radius: 14px; background: #fff; border: 1px solid #e6ece8; }
.ma-head { display: flex; flex-wrap: wrap; align-items: baseline; gap: 10px; margin-bottom: 12px; }
.ma-head--sub { margin-top: 16px; }
.ma-muted { color: #7b877e; font-size: 12px; }
.ma-action { display: flex; align-items: center; gap: 12px; width: 100%; padding: 12px; border: 1px solid #e3ebe6; border-radius: 10px; background: #f6faf7; text-align: left; cursor: pointer; font-size: 20px; color: #1f6f55; }
.ma-action div { display: grid; font-size: 13px; color: #33433a; }
.ma-action span { color: #7b877e; font-size: 12px; }
.ma-login { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #eef2ef; }
.ma-login div { display: grid; font-size: 13px; }
@media (max-width: 1000px) { .ma-grid { grid-template-columns: 1fr; } }
</style>
