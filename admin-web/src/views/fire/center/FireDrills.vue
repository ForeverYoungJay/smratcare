<template>
  <section class="rm-card">
    <div class="rm-card__head">
      <span class="rm-card__title">消防培训与应急演练台账</span>
      <a-space>
        <a-radio-group v-model:value="drillType" size="small" @change="load">
          <a-radio-button value="">全部</a-radio-button><a-radio-button value="DRILL">演练</a-radio-button><a-radio-button value="TRAINING">培训</a-radio-button>
        </a-radio-group>
        <a-button v-if="manager" type="primary" size="small" @click="edit()">新增计划</a-button>
      </a-space>
    </div>
    <a-table size="small" row-key="id" :data-source="rows" :columns="columns" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">{{ record.drillType === 'DRILL' ? '消防演练' : '员工培训' }}</template>
        <template v-else-if="column.key === 'status'"><span class="rm-pill" :class="record.status === 'DONE' ? 'rm-pill--info' : 'rm-pill--warning'">{{ record.status === 'DONE' ? '已完成' : '计划中' }}</span></template>
        <template v-else-if="column.key === 'action'"><a v-if="manager" class="rm-link" @click="edit(record)">{{ record.status === 'DONE' ? '查看/修改' : '登记执行' }}</a></template>
      </template>
    </a-table>
    <p class="fo-muted">演练与培训保留签到人员、总结复盘和下次安排；标记完成前须填写实际日期和总结。</p>

    <a-modal v-model:open="open" :title="form.id ? '登记执行情况' : '新增培训/演练计划'" :confirm-loading="saving" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="类型"><a-radio-group v-model:value="form.drillType"><a-radio value="DRILL">消防演练</a-radio><a-radio value="TRAINING">员工培训</a-radio></a-radio-group></a-form-item>
        <a-form-item label="主题" required><a-input v-model:value="form.topic" /></a-form-item>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="计划日期"><a-date-picker v-model:value="form.plannedDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="实际日期"><a-date-picker v-model:value="form.heldDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="组织人"><a-input v-model:value="form.organizerName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="参加人数"><a-input-number v-model:value="form.participantCount" :min="0" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="签到人员"><a-textarea v-model:value="form.attendees" :rows="2" /></a-form-item>
        <a-form-item label="总结与复盘"><a-textarea v-model:value="form.summary" :rows="3" /></a-form-item>
        <a-form-item label="下次安排"><a-input v-model:value="form.nextPlan" /></a-form-item>
        <a-checkbox :checked="form.status === 'DONE'" @change="(e: { target: { checked: boolean } }) => (form.status = e.target.checked ? 'DONE' : 'PLANNED')">已完成</a-checkbox>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../../../stores/user'
import { getFireDrills, saveFireDrill, type FireTrainingDrill } from '../../../api/fireOps'
import { canCloseHazard } from './fireModel'
import '../../elder/residentUi.css'

const columns = [
  { title: '类型', key: 'type', width: 100 },
  { title: '主题', dataIndex: 'topic' },
  { title: '计划日期', dataIndex: 'plannedDate', width: 110 },
  { title: '最近执行', dataIndex: 'heldDate', width: 110 },
  { title: '参加人数', dataIndex: 'participantCount', width: 90 },
  { title: '下次安排', dataIndex: 'nextPlan', width: 140 },
  { title: '状态', key: 'status', width: 90 },
  { title: '', key: 'action', width: 100 }
]
const user = useUserStore()
const manager = computed(() => canCloseHazard(user.roles || []))
const rows = ref<FireTrainingDrill[]>([])
const loading = ref(false)
const saving = ref(false)
const open = ref(false)
const drillType = ref('')
const blank = (): FireTrainingDrill => ({ drillType: 'DRILL', topic: '', status: 'PLANNED' })
const form = ref<FireTrainingDrill>(blank())

async function load() {
  loading.value = true
  try {
    rows.value = (await getFireDrills({ pageNo: 1, pageSize: 50, drillType: drillType.value || undefined })).list || []
  } finally {
    loading.value = false
  }
}

function edit(row?: FireTrainingDrill) {
  form.value = row ? { ...row } : blank()
  open.value = true
}

async function save() {
  saving.value = true
  try {
    await saveFireDrill(form.value)
    open.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.fo-muted { margin: 10px 0 0; color: #8a968f; font-size: 12px; }
</style>
