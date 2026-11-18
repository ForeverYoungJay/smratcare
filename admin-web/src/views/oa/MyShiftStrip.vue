<template>
  <section class="mss">
    <div class="mss-head">
      <b>本月我的排班</b>
      <span class="mss-muted">来自排班系统，只读；调班请在「考勤与请假」发起调班申请，不需要在日程里重复录入</span>
    </div>
    <a-empty v-if="!shifts.length" :description="loaded ? '本月没有排班' : '加载中'" :image-style="{ height: '32px' }" />
    <div v-else class="mss-list">
      <div v-for="s in shifts" :key="String(s.id)" class="mss-item" :class="{ 'mss-item--today': s.dutyDate === today }">
        <b>{{ dayjs(s.dutyDate).format('MM-DD') }} {{ WEEK[dayjs(s.dutyDate).day()] }}</b>
        <span>{{ s.shiftCode || s.sourceTemplateName || '排班' }}</span>
        <small>{{ s.startTime ? `${hm(s.startTime)}–${hm(s.endTime)}` : '' }}</small>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { getSchedulePage } from '../../api/schedule'
import type { ScheduleItem } from '../../types'
import { useUserStore } from '../../stores/user'

const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const today = dayjs().format('YYYY-MM-DD')
const shifts = ref<ScheduleItem[]>([])
const loaded = ref(false)
// 时间可能是 HH:mm:ss 或完整日期时间
const hm = (value?: string) => (value ? value.slice(-8, -3) || value : '')

onMounted(async () => {
  const staffId = useUserStore().staffInfo?.id
  try {
    // 服务端对普通员工强制只返回本人；管理员显式传本人 ID，避免看到全院排班
    const page = await getSchedulePage({ pageNo: 1, pageSize: 62, staffId, dateFrom: dayjs().startOf('month').format('YYYY-MM-DD'), dateTo: dayjs().endOf('month').format('YYYY-MM-DD'), sortBy: 'dutyDate', sortOrder: 'asc' })
    shifts.value = page.list || []
  } catch {
    shifts.value = []
  } finally {
    loaded.value = true
  }
})
</script>

<style scoped>
.mss { margin-bottom: 16px; padding: 12px 14px; border-radius: 12px; background: #fff; border: 1px solid #e6ece8; }
.mss-head { display: flex; flex-wrap: wrap; gap: 10px; align-items: baseline; margin-bottom: 8px; }
.mss-muted { color: #8a968f; font-size: 12px; }
.mss-list { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; }
.mss-item { flex: none; display: grid; gap: 2px; min-width: 96px; padding: 8px 10px; border-radius: 10px; background: #f3faf6; font-size: 12px; }
.mss-item b { color: #1f6f55; }
.mss-item small { color: #7b877e; }
.mss-item--today { outline: 2px solid #1f9d74; }
</style>
