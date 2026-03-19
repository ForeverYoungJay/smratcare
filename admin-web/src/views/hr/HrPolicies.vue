<template>
  <PageContainer title="规章制度库" subTitle="制度文档归档与检索">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="文档名/上传人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="目录">
        <a-input v-model:value="query.folder" placeholder="默认检索制度相关目录" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button type="primary" @click="openDrawer">新增制度</a-button>
          <a-button @click="fetchData">刷新</a-button>
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

    <a-drawer v-model:open="drawerOpen" title="新增规章制度" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="制度名称" required>
          <a-input v-model:value="form.name" placeholder="例如：员工考勤制度" />
        </a-form-item>
        <a-form-item label="所属目录">
          <a-input v-model:value="form.folder" placeholder="默认：规章制度" />
        </a-form-item>
        <a-form-item label="上传文件" required>
          <a-upload :show-upload-list="false" :before-upload="beforeUploadPolicy">
            <a-button :loading="uploading">上传制度文件</a-button>
          </a-upload>
          <div class="upload-hint">{{ uploadHint }}</div>
        </a-form-item>
        <a-form-item label="更新时间" required>
          <a-date-picker v-model:value="uploadedAtValue" show-time style="width: 100%" />
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
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createHrPolicy, getHrPolicyPage } from '../../api/hr'
import { uploadOaFile } from '../../api/oa'
import type { OaDocument, PageResult } from '../../types'
import { resolveHrError } from './hrError'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  folder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '文档名称', dataIndex: 'name', key: 'name', width: 240 },
  { title: '目录', dataIndex: 'folder', key: 'folder', width: 180 },
  { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName', width: 120 },
  { title: '更新时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 180 },
  { title: '链接', dataIndex: 'url', key: 'url', width: 100 }
]
const rows = ref<OaDocument[]>([])
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const drawerOpen = ref(false)
const form = reactive<Partial<OaDocument>>({})
const uploadedAtValue = ref<Dayjs | undefined>()
const uploadHint = computed(() => {
  if (!form.url) return '上传后会自动回填文件链接，更新时间可按制度实际生效时间填写'
  return `已上传：${form.name || '文件'}`
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaDocument> = await getHrPolicyPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      folder: query.folder
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
  query.folder = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function openDrawer() {
  form.name = undefined
  form.folder = '规章制度'
  form.url = undefined
  form.sizeBytes = undefined
  form.remark = undefined
  uploadedAtValue.value = dayjs()
  drawerOpen.value = true
}

async function beforeUploadPolicy(file: File) {
  uploading.value = true
  try {
    const uploaded = await uploadOaFile(file, 'hr-policy')
    if (!form.name) {
      form.name = uploaded.originalFileName || uploaded.fileName || file.name
    }
    form.url = uploaded.fileUrl || ''
    form.sizeBytes = uploaded.fileSize || file.size || 0
    message.success('制度文件上传成功')
  } catch (error) {
    message.error(resolveHrError(error, '制度文件上传失败'))
  } finally {
    uploading.value = false
  }
  return false
}

function rowClassName(record: OaDocument) {
  if (!record.url) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'name', label: '文档名称' },
  { key: 'folder', label: '目录' },
  { key: 'uploaderName', label: '上传人' },
  { key: 'uploadedAt', label: '更新时间' },
  { key: 'url', label: '链接' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '规章制度库-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '规章制度库-当前筛选')
}

async function submit() {
  if (!form.name || !form.url || !uploadedAtValue.value) {
    message.warning('请填写制度名称、上传文件和更新时间')
    return
  }
  saving.value = true
  try {
    await createHrPolicy({
      ...form,
      folder: form.folder || '规章制度',
      uploadedAt: uploadedAtValue.value.format('YYYY-MM-DD HH:mm:ss')
    })
    message.success('制度已新增')
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
