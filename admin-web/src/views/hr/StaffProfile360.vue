<template>
  <a-drawer :open="open" :title="`员工 360° 档案 · ${profile?.realName || name || ''}`" width="720" @close="emit('close')">
    <a-spin :spinning="loading">
      <a-descriptions size="small" bordered :column="2">
        <a-descriptions-item label="工号">{{ profile?.staffNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="岗位">{{ profile?.jobTitle || '-' }}</a-descriptions-item>
        <a-descriptions-item label="入职日期">{{ profile?.hireDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用工类型">{{ profile?.employmentType || '-' }}</a-descriptions-item>
        <a-descriptions-item label="合同">{{ profile?.contractType || '-' }} · 至 {{ profile?.contractEndDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="资质等级">{{ profile?.qualificationLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="紧急联系人" :span="2">{{ profile?.emergencyContactName || '-' }}</a-descriptions-item>
      </a-descriptions>
      <p class="s360-muted">身份证、银行卡等敏感信息不在此处展示；工资明细只在工资批次中按权限查看。</p>

      <a-tabs>
        <a-tab-pane key="cert" :tab="`证书资质 ${certificates.length}`">
          <a-empty v-if="!certificates.length" description="暂无证书" />
          <div v-for="c in certificates" :key="String(c.id)" class="s360-row">
            <b>{{ c.certificateName }}</b>
            <span :class="{ 's360-danger': Number(c.remainingDays) < 0 }">到期 {{ c.expiryDate || '长期' }}</span>
          </div>
        </a-tab-pane>
        <a-tab-pane key="training" :tab="`培训履历 ${trainings.length}`">
          <a-empty v-if="!trainings.length" description="暂无培训记录" />
          <div v-for="t in trainings" :key="String(t.id)" class="s360-row">
            <b>{{ t.trainingName }}</b>
            <span>{{ t.startDate || '' }}{{ t.score != null ? ` · ${t.score} 分` : '' }}{{ t.testResult ? ` · ${t.testResult}` : '' }}</span>
          </div>
        </a-tab-pane>
        <a-tab-pane key="points" :tab="`积分流水 ${points.length}`">
          <a-empty v-if="!points.length" description="暂无积分记录" />
          <div v-for="p in points" :key="String(p.id)" class="s360-row">
            <b :class="p.changePoints < 0 ? 's360-danger' : 's360-ok'">{{ p.changePoints > 0 ? '+' : '' }}{{ p.changePoints }}</b>
            <span>{{ p.remark || p.sourceType }} · {{ p.createTime ? dayjs(p.createTime).format('YYYY-MM-DD') : '' }}</span>
          </div>
        </a-tab-pane>
        <a-tab-pane key="attendance" :tab="`近期考勤 ${attendance.length}`">
          <a-empty v-if="!attendance.length" description="暂无考勤记录" />
          <div v-for="a in attendance" :key="String(a.id)" class="s360-row">
            <b>{{ a.checkInTime ? dayjs(a.checkInTime).format('MM-DD HH:mm') : '-' }} ~ {{ a.checkOutTime ? dayjs(a.checkOutTime).format('MM-DD HH:mm') : '未签退' }}</b>
            <span :class="{ 's360-danger': a.abnormal }">{{ a.status }}</span>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import dayjs from 'dayjs'
import { getHrAttendanceRecordPage, getHrProfile, getHrProfileCertificatePage, getHrTrainingPage, getStaffPointsLog } from '../../api/hr'
import type { HrAttendanceRecordItem, HrStaffCertificateItem, HrStaffProfile, StaffPointsLog, StaffTrainingRecord } from '../../types'

const props = defineProps<{ open: boolean; staffId?: string | number; name?: string }>()
const emit = defineEmits<{ close: [] }>()
const loading = ref(false)
const profile = ref<HrStaffProfile>()
const certificates = ref<HrStaffCertificateItem[]>([])
const trainings = ref<StaffTrainingRecord[]>([])
const points = ref<StaffPointsLog[]>([])
const attendance = ref<HrAttendanceRecordItem[]>([])

/** 各块独立加载：某一块无权限或失败不影响其他块 */
async function load(staffId: string | number) {
  loading.value = true
  const page = { pageNo: 1, pageSize: 10, staffId }
  const [p, c, t, pt, a] = await Promise.allSettled([
    getHrProfile(staffId),
    getHrProfileCertificatePage(page),
    getHrTrainingPage(page),
    getStaffPointsLog(page),
    getHrAttendanceRecordPage({ pageNo: 1, pageSize: 10, keyword: props.name })
  ])
  profile.value = p.status === 'fulfilled' ? p.value : undefined
  certificates.value = c.status === 'fulfilled' ? c.value.list || [] : []
  trainings.value = t.status === 'fulfilled' ? t.value.list || [] : []
  points.value = pt.status === 'fulfilled' ? pt.value.list || [] : []
  attendance.value = a.status === 'fulfilled' ? (a.value.list || []).filter((row) => String(row.staffId) === String(staffId)) : []
  loading.value = false
}

watch(() => [props.open, props.staffId] as const, ([open, staffId]) => { if (open && staffId) load(staffId) }, { immediate: true })
</script>

<style scoped>
.s360-muted { margin: 8px 0 0; color: #8a968f; font-size: 12px; }
.s360-row { display: flex; justify-content: space-between; gap: 12px; padding: 8px 0; border-bottom: 1px solid #eef2ef; font-size: 13px; }
.s360-row span { color: #6b7a74; }
.s360-danger { color: #c0392b !important; }
.s360-ok { color: #1f6f55; }
</style>
