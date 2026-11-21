<template>
  <PageContainer class="rm-page" title="消防系统配置" sub-title="仅消防主管与管理层可见：楼栋巡查点位、检查模板；修改检查项会升版本，不改动历史记录">
    <section class="rm-card">
      <a-tabs v-model:active-key="tab">
        <a-tab-pane key="locations" tab="楼栋与巡查点位">
          <div class="fs-bar"><a-button type="primary" size="small" @click="editLocation()">新增点位</a-button></div>
          <a-table size="small" row-key="id" :data-source="locations" :columns="locationColumns" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'risk'"><span class="rm-pill" :class="RISK[record.riskLevel].tone">{{ RISK[record.riskLevel].label }}</span></template>
              <template v-else-if="column.key === 'key'">{{ record.keyPart ? '重点部位' : '' }}</template>
              <template v-else-if="column.key === 'enabled'">{{ record.enabled === 0 ? '停用' : '启用' }}</template>
              <template v-else-if="column.key === 'action'">
                <a-space><a class="rm-link" @click="editLocation(record)">编辑</a><a-popconfirm title="删除该点位？" @confirm="removeLocation(record)"><a class="rm-link">删除</a></a-popconfirm></a-space>
              </template>
            </template>
          </a-table>
          <p class="fs-muted">楼栋名称用于工作台「楼栋风险概览」：隐患记录的位置文字包含楼栋名即归入该楼栋。</p>
        </a-tab-pane>
        <a-tab-pane key="templates" tab="检查模板">
          <div class="fs-bar"><a-button type="primary" size="small" @click="editTemplate()">新增模板</a-button></div>
          <a-table size="small" row-key="id" :data-source="templates" :columns="templateColumns" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">{{ RECORD_TYPE_LABEL[record.recordType as FireSafetyRecordType] }}</template>
              <template v-else-if="column.key === 'items'">{{ splitTemplateItems(record.items).length }} 项</template>
              <template v-else-if="column.key === 'version'">v{{ record.version }}{{ record.enabled === 0 ? '（停用）' : '' }}</template>
              <template v-else-if="column.key === 'action'"><a class="rm-link" @click="editTemplate(record)">编辑</a></template>
            </template>
          </a-table>
          <p class="fs-muted">新增日巡、夜巡、月检记录时按启用的模板逐项勾选，未通过的项自动写入问题描述并进入隐患整改。</p>
        </a-tab-pane>
      </a-tabs>
    </section>

    <a-modal v-model:open="locationOpen" title="楼栋与巡查点位" @ok="saveLocation">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="楼栋" required><a-input v-model:value="location.building" placeholder="如：A栋" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="楼层"><a-input v-model:value="location.floorName" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="区域/部位"><a-input v-model:value="location.area" placeholder="如：一层大厅、厨房" /></a-form-item>
        <a-form-item label="点位编号（现场二维码）"><a-input v-model:value="location.pointCode" /></a-form-item>
        <a-form-item label="风险等级"><a-radio-group v-model:value="location.riskLevel"><a-radio value="HIGH">高</a-radio><a-radio value="MEDIUM">中</a-radio><a-radio value="LOW">低</a-radio></a-radio-group></a-form-item>
        <a-space>
          <a-checkbox :checked="location.keyPart === 1" @change="(e: { target: { checked: boolean } }) => (location.keyPart = e.target.checked ? 1 : 0)">重点部位（白天至少巡查 2 次）</a-checkbox>
          <a-checkbox :checked="location.enabled !== 0" @change="(e: { target: { checked: boolean } }) => (location.enabled = e.target.checked ? 1 : 0)">启用</a-checkbox>
        </a-space>
      </a-form>
    </a-modal>
    <a-modal v-model:open="templateOpen" title="检查模板" @ok="saveTemplate">
      <a-form layout="vertical">
        <a-form-item label="适用于" required>
          <a-radio-group v-model:value="template.recordType"><a-radio value="DAY_PATROL">日间巡查</a-radio><a-radio value="NIGHT_PATROL">夜间巡查</a-radio><a-radio value="MONTHLY_CHECK">月度检查</a-radio></a-radio-group>
        </a-form-item>
        <a-form-item label="模板名称" required><a-input v-model:value="template.templateName" /></a-form-item>
        <a-form-item label="检查项（一行一项）" required><a-textarea v-model:value="template.items" :rows="8" /></a-form-item>
        <a-checkbox :checked="template.enabled !== 0" @change="(e: { target: { checked: boolean } }) => (template.enabled = e.target.checked ? 1 : 0)">启用</a-checkbox>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import { deleteFireLocation, getFireLocations, getFireTemplates, saveFireLocation, saveFireTemplate, type FireCheckTemplate, type FireLocation } from '../../../api/fireOps'
import type { FireSafetyRecordType } from '../../../types'
import { RECORD_TYPE_LABEL, splitTemplateItems } from './fireModel'
import '../../elder/residentUi.css'

const RISK: Record<string, { label: string; tone: string }> = {
  HIGH: { label: '高风险', tone: 'rm-pill--danger' }, MEDIUM: { label: '中风险', tone: 'rm-pill--warning' }, LOW: { label: '低风险', tone: 'rm-pill--info' }
}
const locationColumns = [
  { title: '楼栋', dataIndex: 'building', width: 100 }, { title: '楼层', dataIndex: 'floorName', width: 80 },
  { title: '区域/部位', dataIndex: 'area' }, { title: '点位编号', dataIndex: 'pointCode', width: 120 },
  { title: '风险', key: 'risk', width: 90 }, { title: '', key: 'key', width: 90 }, { title: '状态', key: 'enabled', width: 70 },
  { title: '', key: 'action', width: 110 }
]
const templateColumns = [
  { title: '适用于', key: 'type', width: 110 }, { title: '模板名称', dataIndex: 'templateName' },
  { title: '检查项', key: 'items', width: 80 }, { title: '版本', key: 'version', width: 110 },
  { title: '最后修改', dataIndex: 'updatedByName', width: 110 }, { title: '', key: 'action', width: 70 }
]

const tab = ref('locations')
const locations = ref<FireLocation[]>([])
const templates = ref<FireCheckTemplate[]>([])
const locationOpen = ref(false)
const templateOpen = ref(false)
const location = ref<FireLocation>({ building: '', riskLevel: 'MEDIUM', enabled: 1, keyPart: 0 })
const template = ref<FireCheckTemplate>({ recordType: 'DAY_PATROL', templateName: '', items: '', enabled: 1 })

async function load() {
  ;[locations.value, templates.value] = await Promise.all([getFireLocations(), getFireTemplates()])
}

function editLocation(row?: FireLocation) {
  location.value = row ? { ...row } : { building: '', riskLevel: 'MEDIUM', enabled: 1, keyPart: 0 }
  locationOpen.value = true
}
async function saveLocation() {
  await saveFireLocation(location.value)
  locationOpen.value = false
  message.success('已保存')
  await load()
}
async function removeLocation(row: FireLocation) {
  await deleteFireLocation(row.id!)
  await load()
}
function editTemplate(row?: FireCheckTemplate) {
  template.value = row ? { ...row } : { recordType: 'DAY_PATROL', templateName: '', items: '', enabled: 1 }
  templateOpen.value = true
}
async function saveTemplate() {
  await saveFireTemplate(template.value)
  templateOpen.value = false
  message.success('已保存')
  await load()
}

onMounted(load)
</script>

<style scoped>
.fs-bar { display: flex; justify-content: flex-end; margin-bottom: 10px; }
.fs-muted { margin: 10px 0 0; color: #8a968f; font-size: 12px; }
</style>
