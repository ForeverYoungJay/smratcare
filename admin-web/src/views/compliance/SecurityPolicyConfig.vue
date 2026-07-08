<template>
  <PageContainer title="安全策略配置" subTitle="等保2.0 · 双因子登录 / 密码策略 / 会话超时 / 登录锁定 / 脱敏展示 / 日志留存">
    <template #extra>
      <a-button type="primary" :loading="saving" @click="save">保存策略</a-button>
    </template>

    <a-spin :spinning="loading">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card class="card-elevated" :bordered="false" title="登录安全" style="margin-bottom: 16px">
            <a-form layout="vertical">
              <a-form-item label="管理端双因子登录（2FA）">
                <a-space direction="vertical" style="width: 100%">
                  <a-switch v-model:checked="form.twoFactorEnabled" checked-children="开" un-checked-children="关" />
                  <span class="field-tip">开启后，用户名密码校验通过还需短信验证码（发送到员工手机号）才能登录</span>
                </a-space>
              </a-form-item>
              <a-form-item v-if="form.twoFactorEnabled" label="仅对以下角色启用 2FA（不选 = 全部角色）">
                <a-select
                  v-model:value="form.twoFactorRoles"
                  mode="multiple"
                  allow-clear
                  placeholder="全部角色"
                  :options="roleOptions"
                />
              </a-form-item>
              <a-form-item label="密码有效期（天，0 = 不启用）">
                <a-input-number v-model:value="form.passwordMaxDays" :min="0" :max="3650" style="width: 200px" />
                <div class="field-tip">超过有效期后禁止登录，需管理员重置密码</div>
              </a-form-item>
              <a-form-item label="登录失败锁定次数（0 = 不启用）">
                <a-input-number v-model:value="form.loginFailLockCount" :min="0" :max="20" style="width: 200px" />
              </a-form-item>
              <a-form-item label="锁定时长（分钟）">
                <a-input-number v-model:value="form.loginFailLockMinutes" :min="1" :max="1440" style="width: 200px" />
              </a-form-item>
              <a-form-item label="会话超时（分钟，0 = 使用系统默认）">
                <a-input-number v-model:value="form.sessionTimeoutMinutes" :min="0" :max="1440" style="width: 200px" />
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card class="card-elevated" :bordered="false" title="数据安全" style="margin-bottom: 16px">
            <a-form layout="vertical">
              <a-form-item label="敏感字段脱敏展示">
                <a-space direction="vertical" style="width: 100%">
                  <a-switch v-model:checked="form.maskingEnabled" checked-children="开" un-checked-children="关" />
                  <span class="field-tip">开启后，长者/家属的身份证、手机号等在接口返回时自动打码（豁免角色可看全文）</span>
                </a-space>
              </a-form-item>
              <a-form-item label="脱敏豁免角色（可查看全文）">
                <a-select
                  v-model:value="form.maskingExemptRoles"
                  mode="multiple"
                  placeholder="SYS_ADMIN / ADMIN / DIRECTOR"
                  :options="roleOptions"
                />
              </a-form-item>
              <a-form-item label="日志留存天数">
                <a-input-number v-model:value="form.logRetentionDays" :min="180" :max="3650" style="width: 200px" />
                <div class="field-tip">
                  覆盖审计日志 / 敏感访问日志 / 登录日志 / 导出留痕。等保2.0 要求不少于 180 天（6 个月），系统硬性下限 180 天。
                </div>
              </a-form-item>
            </a-form>
          </a-card>
          <a-alert
            type="info"
            show-icon
            message="策略即时生效"
            description="策略按机构生效，保存后约 1 分钟内在登录与接口脱敏处生效。开启 2FA 前请确认相关员工已在员工档案中配置手机号，且短信通道可用。"
          />
        </a-col>
      </a-row>
    </a-spin>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getSecurityPolicy, updateSecurityPolicy } from '../../api/complianceSecurity'

const loading = ref(false)
const saving = ref(false)

const form = reactive({
  twoFactorEnabled: false,
  twoFactorRoles: [] as string[],
  passwordMaxDays: 0,
  loginFailLockCount: 5,
  loginFailLockMinutes: 30,
  sessionTimeoutMinutes: 0,
  maskingEnabled: false,
  maskingExemptRoles: ['SYS_ADMIN', 'ADMIN', 'DIRECTOR'] as string[],
  logRetentionDays: 400
})

const roleOptions = [
  { label: '系统管理员 SYS_ADMIN', value: 'SYS_ADMIN' },
  { label: '管理员 ADMIN', value: 'ADMIN' },
  { label: '院长/管理层 DIRECTOR', value: 'DIRECTOR' },
  { label: '医务部长 MEDICAL_MINISTER', value: 'MEDICAL_MINISTER' },
  { label: '护理部长 NURSING_MINISTER', value: 'NURSING_MINISTER' },
  { label: '财务部长 FINANCE_MINISTER', value: 'FINANCE_MINISTER' },
  { label: '后勤部长 LOGISTICS_MINISTER', value: 'LOGISTICS_MINISTER' },
  { label: '市场部长 MARKETING_MINISTER', value: 'MARKETING_MINISTER' },
  { label: '人事部长 HR_MINISTER', value: 'HR_MINISTER' },
  { label: '医务员工 MEDICAL_EMPLOYEE', value: 'MEDICAL_EMPLOYEE' },
  { label: '护理员工 NURSING_EMPLOYEE', value: 'NURSING_EMPLOYEE' },
  { label: '财务员工 FINANCE_EMPLOYEE', value: 'FINANCE_EMPLOYEE' },
  { label: '后勤员工 LOGISTICS_EMPLOYEE', value: 'LOGISTICS_EMPLOYEE' },
  { label: '市场员工 MARKETING_EMPLOYEE', value: 'MARKETING_EMPLOYEE' },
  { label: '人事员工 HR_EMPLOYEE', value: 'HR_EMPLOYEE' }
]

async function load() {
  loading.value = true
  try {
    const data = await getSecurityPolicy()
    form.twoFactorEnabled = !!data.twoFactorEnabled
    form.twoFactorRoles = data.twoFactorRoles || []
    form.passwordMaxDays = data.passwordMaxDays ?? 0
    form.loginFailLockCount = data.loginFailLockCount ?? 5
    form.loginFailLockMinutes = data.loginFailLockMinutes ?? 30
    form.sessionTimeoutMinutes = data.sessionTimeoutMinutes ?? 0
    form.maskingEnabled = !!data.maskingEnabled
    form.maskingExemptRoles = data.maskingExemptRoles?.length
      ? data.maskingExemptRoles
      : ['SYS_ADMIN', 'ADMIN', 'DIRECTOR']
    form.logRetentionDays = data.logRetentionDays ?? 400
  } catch (e) {
    message.error('加载安全策略失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  if (form.logRetentionDays < 180) {
    message.error('日志留存天数不得低于 180 天（等保2.0要求）')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    await updateSecurityPolicy({ ...form })
    message.success('安全策略已保存')
  } catch (e) {
    // 错误提示由请求拦截器统一处理
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.field-tip {
  font-size: 12px;
  color: var(--muted, #5c6f69);
}
</style>
