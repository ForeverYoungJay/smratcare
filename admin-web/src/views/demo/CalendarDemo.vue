<template>
  <PageContainer title="排班日历" subTitle="FullCalendar 示例">
    <a-card class="card-elevated" :bordered="false">
      <FullCalendar :options="options" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '../../components/PageContainer.vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'

const options = {
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  events: [
    { title: '护理早班', date: '2026-02-10' },
    { title: '医生巡诊', date: '2026-02-12' }
  ]
}
</script>
