<template>
  <PageContainer title="合同模板库" subTitle="员工档案中心 / 合同模板库">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="模板名/上传人/备注" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button type="primary" @click="openDrawer">新增模板</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'url'">
          <a v-if="record.url" :href="record.url" target="_blank">查看</a>
          <span v-else>-</span>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" title="新增合同模板" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="模板名称" required>
          <a-input v-model:value="form.name" placeholder="例如：劳动合同模板（全职）" />
        </a-form-item>
        <a-form-item label="上传模板" required>
          <a-upload :show-upload-list="false" :before-upload="beforeUploadTemplate">
            <a-button :loading="uploading">上传合同模板</a-button>
          </a-upload>
          <div class="upload-hint">{{ uploadHint }}</div>
        </a-form-item>
        <a-form-item label="文件链接">
          <a-input :value="form.url || ''" disabled placeholder="上传后自动生成，不再手填 URL" />
        </a-form-item>
        <a-form-item label="文件大小(B)">
          <a-input-number v-model:value="form.sizeBytes" :min="0" style="width: 100%" disabled />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createHrProfileTemplate, getHrProfileTemplatePage } from '../../api/hr'
import { uploadOaFile } from '../../api/oa'
import type { HrProfileDocumentItem, PageResult } from '../../types'
import { resolveHrError } from './hrError'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '模板名称', dataIndex: 'name', key: 'name', width: 260 },
  { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName', width: 120 },
  { title: '上传时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 180 },
  { title: '大小(B)', dataIndex: 'sizeBytes', key: 'sizeBytes', width: 120 },
  { title: '链接', dataIndex: 'url', key: 'url', width: 100 }
]
const rows = ref<HrProfileDocumentItem[]>([])
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const drawerOpen = ref(false)
const form = reactive<Partial<HrProfileDocumentItem>>({})
const uploadHint = computed(() => {
  if (!form.url) return '请直接上传合同模板，系统会自动回填链接和文件大小'
  return `已上传模板：${form.name || '未命名模板'}`
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrProfileDocumentItem> = await getHrProfileTemplatePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
    })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch {
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function openDrawer() {
  form.name = undefined
  form.url = undefined
  form.sizeBytes = undefined
  form.remark = undefined
  drawerOpen.value = true
}

async function beforeUploadTemplate(file: File) {
  uploading.value = true
  try {
    const uploaded = await uploadOaFile(file, 'hr-contract-template')
    if (!form.name) {
      form.name = uploaded.originalFileName || uploaded.fileName || file.name
    }
    form.url = uploaded.fileUrl || ''
    form.sizeBytes = uploaded.fileSize || file.size || 0
    message.success('合同模板上传成功')
  } catch (error) {
    message.error(resolveHrError(error, '合同模板上传失败'))
  } finally {
    uploading.value = false
  }
  return false
}

function rowClassName(record: HrProfileDocumentItem) {
  if (!record.url) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'name', label: '模板名称' },
  { key: 'uploaderName', label: '上传人' },
  { key: 'uploadedAt', label: '上传时间' },
  { key: 'sizeBytes', label: '大小(B)' },
  { key: 'url', label: '链接' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '合同模板库-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '合同模板库-当前筛选')
}

async function submit() {
  if (!form.name || !form.url) {
    message.warning('请填写模板名称并上传合同模板')
    return
  }
  saving.value = true
  try {
    await createHrProfileTemplate(form)
    message.success('模板已新增')
    drawerOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHrError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}

.upload-hint {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
