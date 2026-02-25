<template>
  <PageContainer title="合同签约" sub-title="合同新增、审批与签约状态跟踪">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="合同编号">
          <a-input v-model:value="query.contractNo" placeholder="请输入 合同编号" allow-clear />
        </a-form-item>
        <a-form-item label="签约房号">
          <a-input v-model:value="query.reservationRoomNo" placeholder="请输入 签约房号" allow-clear />
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="query.elderName" placeholder="请输入 姓名" allow-clear />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
        </a-form-item>
        <a-form-item label="营销人员">
          <a-input v-model:value="query.marketerName" placeholder="请输入 营销人员" allow-clear />
        </a-form-item>
        <a-form-item label="合同状态">
          <a-input v-model:value="query.contractStatus" placeholder="请选择 合同状态" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜 索</a-button>
            <a-button @click="reset">清 空</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openForm()">新增合同</a-button>
          <a-button @click="batchApprove">审批</a-button>
          <a-button danger @click="batchDelete">删除</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ x: 1800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operation'">
            <a-space wrap>
              <a-button type="link" @click="view(record)">查看</a-button>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" @click="openAttachment(record)">附件</a-button>
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

    <a-modal v-model:open="open" :title="form.id ? '编辑合同' : '新增合同'" width="760px" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="合同编号" name="contractNo"><a-input v-model:value="form.contractNo" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="签约房号"><a-input v-model:value="form.reservationRoomNo" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="姓名" name="elderName"><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="联系电话"><a-input v-model:value="form.elderPhone" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="性别"><a-select v-model:value="form.gender" allow-clear><a-select-option :value="1">男</a-select-option><a-select-option :value="2">女</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="年龄"><a-input-number v-model:value="form.age" :min="0" :max="120" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="签约日期"><a-date-picker v-model:value="form.contractSignedAt" value-format="YYYY-MM-DD HH:mm:ss" show-time style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="合同有效期止"><a-date-picker v-model:value="form.contractExpiryDate" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="合同状态"><a-input v-model:value="form.contractStatus" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="营销人员"><a-input v-model:value="form.marketerName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="所属机构"><a-input v-model:value="form.orgName" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="attachmentOpen" title="合同附件" width="820px" :footer="null">
      <a-space style="margin-bottom: 12px;">
        <a-upload
          :show-upload-list="false"
          :custom-request="handleUpload"
          :before-upload="beforeUpload"
          :accept="uploadAccept"
        >
          <a-button type="primary" :loading="attachmentSubmitting">上传附件</a-button>
        </a-upload>
      </a-space>
      <a-table :data-source="attachments" :columns="attachmentColumns" :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preview'">
            <a-image
              v-if="isImageAttachment(record)"
              :src="record.fileUrl"
              :width="40"
              :height="40"
              style="object-fit: cover; border-radius: 4px;"
            />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'fileUrl'">
            <a :href="record.fileUrl" target="_blank">{{ record.fileName }}</a>
          </template>
          <template v-else-if="column.key === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" danger @click="removeAttachment(record.id)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import {
  batchDeleteLeads,
  createCrmLead,
  createLeadAttachment,
  deleteLeadAttachment,
  getLeadAttachments,
  getLeadPage,
  updateCrmLead,
  uploadMarketingFile
} from '../../api/marketing'
import type { ContractAttachmentItem, CrmLeadItem, PageResult } from '../../types'

const loading = ref(false)
const submitting = ref(false)
const open = ref(false)
const formRef = ref<FormInstance>()
const rows = ref<CrmLeadItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])

const query = reactive({
  contractNo: '',
  reservationRoomNo: '',
  elderName: '',
  elderPhone: '',
  marketerName: '',
  contractStatus: '',
  pageNo: 1,
  pageSize: 10
})

const form = reactive<Partial<CrmLeadItem>>({})
const rules: FormRules = {
  contractNo: [{ required: true, message: '请输入合同编号' }],
  elderName: [{ required: true, message: '请输入姓名' }]
}

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (number | string)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '签约房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 140 },
  { title: '姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
  { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
  { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
  { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
  { title: '签约日期', dataIndex: 'contractSignedAt', key: 'contractSignedAt', width: 170 },
  { title: '合同有效期止', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate', width: 140 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 140 },
  { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
  { title: '操作', key: 'operation', fixed: 'right', width: 180 }
]

const attachmentOpen = ref(false)
const attachmentSubmitting = ref(false)
const currentAttachmentLead = ref<CrmLeadItem>()
const attachments = ref<ContractAttachmentItem[]>([])
const uploadAccept = '.jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip'
const attachmentColumns = [
  { title: '预览', dataIndex: 'preview', key: 'preview', width: 80 },
  { title: '文件名', dataIndex: 'fileName', key: 'fileName', width: 220 },
  { title: '文件链接', dataIndex: 'fileUrl', key: 'fileUrl', width: 320 },
  { title: '文件类型', dataIndex: 'fileType', key: 'fileType', width: 120 },
  { title: '文件大小', dataIndex: 'fileSize', key: 'fileSize', width: 110 },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'operation', width: 80 }
]

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<CrmLeadItem> = await getLeadPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: 2,
      contractNo: query.contractNo || undefined,
      elderName: query.elderName || undefined,
      elderPhone: query.elderPhone || undefined,
      marketerName: query.marketerName || undefined,
      contractStatus: query.contractStatus || undefined
    })
    rows.value = page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.contractNo = ''
  query.reservationRoomNo = ''
  query.elderName = ''
  query.elderPhone = ''
  query.marketerName = ''
  query.contractStatus = ''
  query.pageNo = 1
  selectedRowKeys.value = []
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function openForm(record?: CrmLeadItem) {
  if (record) {
    Object.assign(form, record)
  } else {
    Object.assign(form, {
      id: undefined,
      status: 2,
      contractSignedFlag: 1,
      contractNo: `HT${dayjs().format('YYYYMMDDHHmmss')}`,
      contractSignedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      contractStatus: '未提交',
      orgName: '德合养老院'
    } as Partial<CrmLeadItem>)
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload: Partial<CrmLeadItem> = {
      ...form,
      name: form.elderName || form.name || '签约客户',
      phone: form.elderPhone || form.phone,
      status: 2,
      contractSignedFlag: 1
    }
    if (form.id) {
      await updateCrmLead(form.id, payload)
    } else {
      await createCrmLead(payload)
    }
    message.success('合同保存成功')
    open.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function batchApprove() {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    message.info('请先勾选合同')
    return
  }
  await Promise.all(ids.map((id) => {
    const row = rows.value.find((item) => item.id === id)
    if (!row) return Promise.resolve()
    return updateCrmLead(row.id, { ...row, contractStatus: '已审批,已通过' })
  }))
  message.success(`已审批 ${ids.length} 条合同`)
  fetchData()
}

function batchDelete() {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    message.info('请先勾选要删除的合同')
    return
  }
  Modal.confirm({
    title: `确认删除 ${ids.length} 条合同记录吗？`,
    onOk: async () => {
      await batchDeleteLeads({ ids })
      selectedRowKeys.value = []
      message.success('删除成功')
      fetchData()
    }
  })
}

function view(record: CrmLeadItem) {
  Modal.info({
    title: '合同详情',
    content: `${record.contractNo || '-'} / ${record.elderName || '-'} / ${record.contractStatus || '-'}`
  })
}

async function openAttachment(record: CrmLeadItem) {
  currentAttachmentLead.value = record
  attachments.value = await getLeadAttachments(record.id)
  attachmentOpen.value = true
}

function beforeUpload(file: File) {
  const allowExt = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'zip']
  const ext = file.name?.split('.').pop()?.toLowerCase() || ''
  if (!allowExt.includes(ext)) {
    message.error(`文件类型不支持，仅允许: ${allowExt.join(', ')}`)
    return false
  }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('文件大小超限，最大支持 10MB')
    return false
  }
  return true
}

async function handleUpload(option: any) {
  const file = option?.file as File
  if (!file || !currentAttachmentLead.value) {
    option?.onError?.(new Error('未选择合同或文件'))
    return
  }
  attachmentSubmitting.value = true
  try {
    const uploaded = await uploadMarketingFile(file, 'marketing-contract')
    await createLeadAttachment(currentAttachmentLead.value.id, {
      contractNo: currentAttachmentLead.value.contractNo,
      fileName: uploaded.originalFileName || uploaded.fileName,
      fileUrl: uploaded.fileUrl,
      fileType: uploaded.fileType,
      fileSize: uploaded.fileSize
    })
    attachments.value = await getLeadAttachments(currentAttachmentLead.value.id)
    message.success('附件上传成功')
    option?.onSuccess?.(uploaded)
  } catch (error) {
    option?.onError?.(error)
  } finally {
    attachmentSubmitting.value = false
  }
}

function isImageAttachment(record: ContractAttachmentItem) {
  const type = (record.fileType || '').toLowerCase()
  if (type.startsWith('image/')) return true
  const url = (record.fileUrl || '').toLowerCase()
  return ['.jpg', '.jpeg', '.png', '.gif', '.webp'].some((suffix) => url.endsWith(suffix))
}

function formatFileSize(size?: number) {
  if (size == null || Number.isNaN(size)) return '-'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(2)}MB`
}

async function removeAttachment(attachmentId: number) {
  await deleteLeadAttachment(attachmentId)
  if (currentAttachmentLead.value) {
    attachments.value = await getLeadAttachments(currentAttachmentLead.value.id)
  }
  message.success('附件已删除')
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.table-actions {
  margin-bottom: 12px;
}
</style>
