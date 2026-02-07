<template>
  <PageContainer title="运营看板" subTitle="实时掌握机构运行情况">
    <a-skeleton active :loading="loading">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :lg="6" v-for="card in cards" :key="card.title">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic :title="card.title" :value="card.value" />
            <div class="card-meta">
              <a-tag :color="card.color">{{ card.tag }}</a-tag>
              <span>{{ card.trend }}</span>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :lg="16">
          <a-card class="card-elevated" title="任务趋势" :bordered="false">
            <v-chart class="chart" :option="trendOption" autoresize />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-card class="card-elevated" title="今日异常" :bordered="false">
            <a-list :data-source="alerts" item-layout="horizontal">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta
                    :title="item.title"
                    :description="item.desc"
                  />
                  <a-tag :color="item.level === '高' ? 'red' : 'orange'">{{ item.level }}</a-tag>
                </a-list-item>
              </template>
            </a-list>
            <div v-if="alerts.length === 0" class="empty-wrap">
              <a-empty description="暂无异常" />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :lg="12">
          <a-card class="card-elevated" title="库存风险" :bordered="false">
            <a-table
              :columns="inventoryColumns"
              :data-source="inventoryRows"
              :pagination="false"
              size="small"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card class="card-elevated" title="账单概览" :bordered="false">
            <v-chart class="chart" :option="billOption" autoresize />
          </a-card>
        </a-col>
      </a-row>
    </a-skeleton>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'

const loading = ref(true)

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})

const cards = [
  { title: '护理任务', value: 268, tag: '完成率 78%', color: 'blue', trend: '+3.2% 周同比' },
  { title: '异常提醒', value: 7, tag: '今日新增 2', color: 'red', trend: '-1.1% 周同比' },
  { title: '库存预警', value: 12, tag: '需补货 3', color: 'orange', trend: '+0.8% 周同比' },
  { title: '未支付账单', value: 18, tag: '回款中', color: 'gold', trend: '+6.4% 周同比' }
]

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 20, right: 16, top: 20, bottom: 20, containLabel: true },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: { type: 'value' },
  series: [
    {
      data: [120, 132, 101, 134, 90, 230, 210],
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.2 },
      color: '#1677ff'
    }
  ]
}))

const alerts = [
  { title: 'A区 3F 血压异常', desc: '张** 202', level: '高' },
  { title: 'B区 2F 任务超时', desc: '待处理 2 项', level: '中' },
  { title: '库存不足：营养奶粉', desc: '库存 8 / 安全 15', level: '高' }
]

const inventoryColumns = [
  { title: '商品', dataIndex: 'product', key: 'product' },
  { title: '库存', dataIndex: 'stock', key: 'stock', width: 80 },
  { title: '风险', dataIndex: 'risk', key: 'risk', width: 80 }
]

const inventoryRows = [
  { key: 1, product: '营养奶粉', stock: '8', risk: '低库存' },
  { key: 2, product: '护理手套', stock: '30', risk: '低库存' },
  { key: 3, product: '营养液', stock: '20', risk: '临期' }
]

const billOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 52, name: '已结清' },
        { value: 18, name: '待支付' },
        { value: 6, name: '异常' }
      ],
      label: { formatter: '{b}: {d}%'}
    }
  ]
}))
</script>

<style scoped>
.chart {
  height: 260px;
}

.card-meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
}
</style>
