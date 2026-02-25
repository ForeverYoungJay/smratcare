<template>
  <PageContainer title="量表模板" subTitle="评估量表与评分规则配置">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="模板名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="评估类型">
          <a-select v-model:value="query.assessmentType" allow-clear style="width: 180px">
            <a-select-option value="ADMISSION">入住评估</a-select-option>
            <a-select-option value="REGISTRATION">评估登记</a-select-option>
            <a-select-option value="CONTINUOUS">持续评估</a-select-option>
            <a-select-option value="ARCHIVE">评估档案</a-select-option>
            <a-select-option value="OTHER_SCALE">其他量表</a-select-option>
            <a-select-option value="COGNITIVE">认知能力</a-select-option>
            <a-select-option value="SELF_CARE">自理能力</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-button type="primary" @click="openForm()">新增模板</a-button>
      </div>
      <a-table :data-source="rows" :columns="columns" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" danger @click="remove(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="open" title="量表模板" width="760px" :confirm-loading="saving" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="模板编码" name="templateCode">
              <a-input v-model:value="form.templateCode" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模板名称" name="templateName">
              <a-input v-model:value="form.templateName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估类型" name="assessmentType">
              <a-select v-model:value="form.assessmentType">
                <a-select-option value="ADMISSION">入住评估</a-select-option>
                <a-select-option value="REGISTRATION">评估登记</a-select-option>
                <a-select-option value="CONTINUOUS">持续评估</a-select-option>
                <a-select-option value="ARCHIVE">评估档案</a-select-option>
                <a-select-option value="OTHER_SCALE">其他量表</a-select-option>
                <a-select-option value="COGNITIVE">认知能力</a-select-option>
                <a-select-option value="SELF_CARE">自理能力</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status">
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="模板描述">
          <a-textarea v-model:value="form.description" :rows="2" />
        </a-form-item>
        <a-form-item label="评分规则JSON" name="scoreRulesJson">
          <a-textarea v-model:value="form.scoreRulesJson" :rows="4" placeholder='[{"key":"q1","weight":1}]' />
        </a-form-item>
        <a-form-item label="等级规则JSON">
          <a-textarea v-model:value="form.levelRulesJson" :rows="4" placeholder='[{"min":0,"max":10,"level":"低"}]' />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getAssessmentTemplatePage,
  createAssessmentTemplate,
  updateAssessmentTemplate,
  deleteAssessmentTemplate
} from '../../api/assessment'
import type { AssessmentScaleTemplate, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<AssessmentScaleTemplate[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  assessmentType: undefined as string | undefined
})

const open = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<AssessmentScaleTemplate>>({
  templateCode: '',
  templateName: '',
  assessmentType: 'OTHER_SCALE',
  description: '',
  scoreRulesJson: '[]',
  levelRulesJson: '[]',
  status: 1
})

const rules: FormRules = {
  templateCode: [{ required: true, message: '请输入模板编码' }],
  templateName: [{ required: true, message: '请输入模板名称' }],
  assessmentType: [{ required: true, message: '请选择评估类型' }],
  scoreRulesJson: [{ required: true, message: '请输入评分规则JSON' }]
}

const columns = [
  { title: '编码', dataIndex: 'templateCode', key: 'templateCode', width: 140 },
  { title: '名称', dataIndex: 'templateName', key: 'templateName', width: 180 },
  { title: '评估类型', dataIndex: 'assessmentType', key: 'assessmentType', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '操作', key: 'action', width: 140 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<AssessmentScaleTemplate> = await getAssessmentTemplatePage(query)
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.pageNo = 1
  query.keyword = ''
  query.assessmentType = undefined
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function openForm(row?: AssessmentScaleTemplate) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: undefined,
      templateCode: '',
      templateName: '',
      assessmentType: 'OTHER_SCALE',
      description: '',
      scoreRulesJson: '[]',
      levelRulesJson: '[]',
      status: 1
    })
  }
  open.value = true
}

async function submit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateAssessmentTemplate(form.id, form)
      message.success('更新成功')
    } else {
      await createAssessmentTemplate(form)
      message.success('创建成功')
    }
    open.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function remove(id: number) {
  Modal.confirm({
    title: '确认删除该模板？',
    onOk: async () => {
      await deleteAssessmentTemplate(id)
      message.success('删除成功')
      fetchData()
    }
  })
}

onMounted(fetchData)
</script>
