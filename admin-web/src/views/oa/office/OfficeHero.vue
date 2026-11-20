<template>
  <div class="of-hero-row">
    <section class="of-hero" :style="{ '--of-hero-img': `url(${visualAssets.scenicMountain.url})` }">
      <div class="of-hero__avatar" aria-hidden="true">{{ initial }}</div>
      <div class="of-hero__copy">
        <h2>{{ greeting }}，{{ name }} <span aria-hidden="true">👋</span></h2>
        <p class="of-hero__meta">
          <span>{{ [profile?.departmentName, profile?.jobTitle].filter(Boolean).join(' · ') || '我的办公' }}</span>
          <span v-if="profile?.staffNo">工号：{{ profile.staffNo }}</span>
        </p>
        <p class="of-hero__tagline"><slot>用专业的照护和温暖的陪伴，守护长者的幸福晚年！</slot></p>
      </div>
    </section>
    <section class="of-date">
      <CalendarOutlined class="of-date__icon" />
      <div>
        <small>今天是</small>
        <b>{{ today }}</b>
        <span v-if="shift">今日班次 {{ shift }}</span>
        <span v-else>本年第 {{ week }} 周</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import { CalendarOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '../../../stores/user'
import { visualAssets } from '../../../config/visualAssets'
import { useMyProfile } from './useMyProfile'

defineProps<{ shift?: string }>()
const WEEK = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const user = useUserStore()
const profile = useMyProfile()
const name = computed(() => profile.value?.realName || user.staffInfo?.realName || user.staffInfo?.username || '同事')
const initial = computed(() => name.value.slice(0, 1))
const greeting = computed(() => { const h = dayjs().hour(); return h < 11 ? '早上好' : h < 13 ? '中午好' : h < 18 ? '下午好' : '晚上好' })
const today = `${dayjs().format('YYYY年M月D日')}（${WEEK[dayjs().day()]}）`
// ISO 周数，避免引入 dayjs 插件
const week = (() => { const d = new Date(); d.setHours(0, 0, 0, 0); d.setDate(d.getDate() + 3 - ((d.getDay() + 6) % 7)); const w1 = new Date(d.getFullYear(), 0, 4); return 1 + Math.round(((d.getTime() - w1.getTime()) / 86400000 - 3 + ((w1.getDay() + 6) % 7)) / 7) })()
</script>
