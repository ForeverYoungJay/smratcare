<template>
  <div class="dashboard">
    <a-page-header title="智慧运营看板" sub-title="Ant Design Vue Demo Dashboard" />

    <div class="hero">
      <div class="hero-left">
        <div class="hero-title">本日运营概览</div>
        <div class="hero-desc">实时掌握护理任务、入住与库存健康状况</div>
        <div class="hero-kpis">
          <div class="kpi">
            <div class="kpi-label">护理任务</div>
            <div class="kpi-value">268</div>
          </div>
          <div class="kpi">
            <div class="kpi-label">异常提醒</div>
            <div class="kpi-value">7</div>
          </div>
          <div class="kpi">
            <div class="kpi-label">库存预警</div>
            <div class="kpi-value">12</div>
          </div>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-ring">
          <a-progress type="circle" :percent="78" :size="160" />
          <div class="ring-label">运营健康度</div>
        </div>
      </div>
    </div>

    <a-row :gutter="16" class="section">
      <a-col :xs="24" :sm="12" :lg="6" v-for="stat in stats" :key="stat.title">
        <a-card class="stat-card" :bordered="false">
          <a-statistic :title="stat.title" :value="stat.value" />
          <div class="stat-meta">
            <a-tag :color="stat.tagColor">{{ stat.tag }}</a-tag>
            <span class="stat-trend">{{ stat.trend }}</span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="section">
      <a-col :xs="24" :lg="14">
        <a-card title="护理任务完成趋势" :bordered="false">
          <div class="trend-list">
            <div v-for="item in trend" :key="item.day" class="trend-item">
              <div class="trend-day">{{ item.day }}</div>
              <a-progress :percent="item.value" />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="10">
        <a-card title="实时告警" :bordered="false">
          <a-timeline>
            <a-timeline-item v-for="item in alerts" :key="item.text" :color="item.color">
              {{ item.text }}
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="section">
      <a-col :xs="24" :lg="12">
        <a-card title="今日值班" :bordered="false">
          <div class="staff-list">
            <div v-for="staff in staffList" :key="staff.name" class="staff-item">
              <a-avatar :style="{ backgroundColor: staff.color }">{{ staff.name.slice(0,1) }}</a-avatar>
              <div class="staff-info">
                <div class="staff-name">{{ staff.name }}</div>
                <div class="staff-role">{{ staff.role }}</div>
              </div>
              <a-tag color="blue">在线</a-tag>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="床位状态" :bordered="false">
          <a-table :columns="bedColumns" :data-source="bedData" :pagination="false" size="small" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="section">
      <a-col :xs="24" :lg="24">
        <a-card title="智能助手" :bordered="false">
          <div class="assistant">
            <Bubble
              v-for="msg in assistantMessages"
              :key="msg.id"
              :content="msg.content"
              :placement="msg.role === 'user' ? 'end' : 'start'"
              :variant="msg.role === 'user' ? 'filled' : 'outlined'"
              :type="msg.role === 'user' ? 'primary' : 'default'"
              class="assistant-bubble"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { Bubble } from 'ant-design-x-vue';

const stats = [
  { title: '入住老人', value: 126, tag: '入住率 86%', tagColor: 'green', trend: '+3.2% 本周' },
  { title: '待执行任务', value: 42, tag: '高优先级', tagColor: 'red', trend: '-1.1% 本周' },
  { title: '今日访客', value: 18, tag: '正常', tagColor: 'blue', trend: '+0.8% 本周' },
  { title: '月度账单', value: 62, tag: '回款中', tagColor: 'gold', trend: '+6.4% 本周' }
];

const trend = [
  { day: '周一', value: 70 },
  { day: '周二', value: 82 },
  { day: '周三', value: 76 },
  { day: '周四', value: 88 },
  { day: '周五', value: 80 },
  { day: '周六', value: 92 },
  { day: '周日', value: 78 }
];

const alerts = [
  { text: 'A区 3F 血压异常提醒', color: 'red' },
  { text: 'B区 护理任务超时 2 项', color: 'orange' },
  { text: '库存不足：营养奶粉', color: 'red' },
  { text: 'C区 床位更换待确认', color: 'blue' }
];

const staffList = [
  { name: '王婷', role: '护理主管', color: '#5B8FF9' },
  { name: '李晨', role: '医生', color: '#5AD8A6' },
  { name: '周敏', role: '行政', color: '#F6BD16' },
  { name: '陈峰', role: '护工', color: '#E8684A' }
];

const bedColumns = [
  { title: '房间', dataIndex: 'room', key: 'room' },
  { title: '床位', dataIndex: 'bed', key: 'bed' },
  { title: '状态', dataIndex: 'status', key: 'status' }
];

const bedData = [
  { key: '1', room: 'A101', bed: '01', status: '已入住' },
  { key: '2', room: 'A101', bed: '02', status: '已入住' },
  { key: '3', room: 'B202', bed: '01', status: '空闲' },
  { key: '4', room: 'C303', bed: '02', status: '清洁中' }
];

const assistantMessages = [
  { id: 1, role: 'assistant', content: '今天共有 268 项护理任务，完成率 78%。' },
  { id: 2, role: 'assistant', content: '库存预警商品 12 项，建议优先补货 3 项。' },
  { id: 3, role: 'user', content: '请给出护理异常清单摘要。' },
  { id: 4, role: 'assistant', content: 'A区 3F 与 B区 2F 共有 7 条异常提醒。' }
];
</script>
