<template>
  <PageContainer title="相册管理" subTitle="活动照片与影像资料管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="相册标题/备注" allow-clear />
      </a-form-item>
      <a-form-item label="分类">
        <a-input v-model:value="query.category" placeholder="如：节日活动" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增相册</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
        <a-button :disabled="!canPublishSingle" @click="publishSelected">发布</a-button>
        <a-button :disabled="!canArchiveSingle" @click="archiveSelected">归档</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchPublish">批量发布</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchArchive">批量归档</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'coverUrl'">
          <a-image v-if="record.coverUrl" :src="record.coverUrl" :width="48" :height="48" style="object-fit: cover; border-radius: 6px;" />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑相册' : '新增相册'" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="相册标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="分类">
          <a-input v-model:value="form.category" />
        </a-form-item>
        <a-form-item label="封面地址">
          <a-input v-model:value="form.coverUrl" />
        </a-form-item>
        <a-form-item label="照片上传">
          <a-upload :show-upload-list="false" :before-upload="beforeUploadPhoto">
            <a-button :loading="uploading">上传照片</a-button>
          </a-upload>
          <div v-if="photoFiles.length" class="photo-list">
            <div v-for="(item, idx) in photoFiles" :key="`${item.url}-${idx}`" class="photo-item">
              <a :href="item.url" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
              <a-space>
                <a-button type="link" size="small" @click="setCover(item.url)">设为封面</a-button>
                <a-button type="link" danger size="small" @click="removePhoto(idx)">删除</a-button>
              </a-space>
            </div>
          </div>
        </a-form-item>
        <a-form-item label="照片数量">
          <a-input-number v-model:value="form.photoCount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="拍摄日期">
          <a-date-picker v-model:value="shootDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createAlbum, deleteAlbum, getAlbumPage, updateAlbum, uploadOaFile } from '../../api/oa'
import type { OaAlbum, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaAlbum[]>([])
const selectedRowKeys = ref<string[]>([])
const query = reactive({ keyword: '', category: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '封面', dataIndex: 'coverUrl', key: 'coverUrl', width: 90 },
  { title: '相册标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 140 },
  { title: '照片数量', dataIndex: 'photoCount', key: 'photoCount', width: 100 },
  { title: '拍摄日期', dataIndex: 'shootDate', key: 'shootDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]

const editOpen = ref(false)
const shootDate = ref<Dayjs | undefined>()
const form = reactive<Partial<OaAlbum>>({ status: 'DRAFT', photoCount: 0 })
const uploading = ref(false)
const photoFiles = ref<Array<{ name: string; url: string }>>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canPublishSingle = computed(
  () => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'PUBLISHED'
)
const canArchiveSingle = computed(
  () => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'ARCHIVED'
)

function statusText(status?: string) {
  if (status === 'PUBLISHED') return '已发布'
  if (status === 'ARCHIVED') return '已归档'
  return '草稿'
}

function statusColor(status?: string) {
  if (status === 'PUBLISHED') return 'green'
  if (status === 'ARCHIVED') return 'blue'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaAlbum> = await getAlbumPage(query)
    rows.value = (res.list || []).map((item) => {
      const parsed = parseAlbumRemark(item.remark)
      return {
        ...item,
        id: String(item.id),
        coverUrl: item.coverUrl || parsed.photoUrls[0]?.url || '',
        photoCount: Math.max(Number(item.photoCount || 0), parsed.photoUrls.length)
      }
    })
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
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
  query.keyword = ''
  query.category = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaAlbum) {
  Object.assign(form, record || { status: 'DRAFT', photoCount: 0, title: '', category: '', coverUrl: '', remark: '' })
  const parsed = parseAlbumRemark(form.remark)
  form.remark = parsed.text
  photoFiles.value = parsed.photoUrls
  if (!form.coverUrl && photoFiles.value.length) {
    form.coverUrl = photoFiles.value[0].url
  }
  if (!form.photoCount && photoFiles.value.length) {
    form.photoCount = photoFiles.value.length
  }
  shootDate.value = form.shootDate ? dayjs(form.shootDate) : undefined
  editOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    const mergedRemark = JSON.stringify({
      text: form.remark || '',
      photoUrls: photoFiles.value
    })
    const payload = {
      ...form,
      coverUrl: form.coverUrl || photoFiles.value[0]?.url,
      photoCount: Math.max(Number(form.photoCount || 0), photoFiles.value.length),
      shootDate: shootDate.value ? shootDate.value.format('YYYY-MM-DD') : undefined,
      remark: mergedRemark
    }
    if (form.id) {
      await updateAlbum(String(form.id), payload)
    } else {
      await createAlbum(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaAlbum) {
  await deleteAlbum(String(record.id))
  fetchData()
}

async function updateAlbumStatus(record: OaAlbum, status: 'PUBLISHED' | 'ARCHIVED') {
  await updateAlbum(String(record.id), { ...record, status })
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条相册后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openEdit(record)
}

async function publishSelected() {
  const record = requireSingleSelection('发布')
  if (!record) return
  if (record.status === 'PUBLISHED') {
    message.info('所选相册已发布')
    return
  }
  await updateAlbumStatus(record, 'PUBLISHED')
  message.success('发布成功')
  fetchData()
}

async function archiveSelected() {
  const record = requireSingleSelection('归档')
  if (!record) return
  if (record.status === 'ARCHIVED') {
    message.info('所选相册已归档')
    return
  }
  await updateAlbumStatus(record, 'ARCHIVED')
  message.success('归档成功')
  fetchData()
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchPublish() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => item.status !== 'PUBLISHED')
  if (validRecords.length === 0) {
    message.info('勾选项中没有可发布相册')
    return
  }
  await Promise.all(validRecords.map((item) => updateAlbumStatus(item, 'PUBLISHED')))
  message.success(`批量发布完成，共处理 ${validRecords.length} 条`)
  fetchData()
}

async function batchArchive() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => item.status !== 'ARCHIVED')
  if (validRecords.length === 0) {
    message.info('勾选项中没有可归档相册')
    return
  }
  await Promise.all(validRecords.map((item) => updateAlbumStatus(item, 'ARCHIVED')))
  message.success(`批量归档完成，共处理 ${validRecords.length} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  await Promise.all(selectedRecords.value.map((item) => deleteAlbum(String(item.id))))
  message.success(`批量删除完成，共处理 ${selectedRecords.value.length} 条`)
  fetchData()
}

function parseAlbumRemark(remark?: string) {
  if (!remark) return { text: '', photoUrls: [] as Array<{ name: string; url: string }> }
  try {
    const parsed = JSON.parse(remark)
    const text = parsed?.text ? String(parsed.text) : ''
    const sourceList = Array.isArray(parsed?.photoUrls)
      ? parsed.photoUrls
      : Array.isArray(parsed?.photos)
      ? parsed.photos
      : []
    const photoUrls = sourceList
      .map((item: any) => {
        if (typeof item === 'string') {
          return { name: '相册照片', url: item }
        }
        if (item?.url) {
          return {
            name: String(item.name || item.originalFileName || item.fileName || '相册照片'),
            url: String(item.url)
          }
        }
        if (item?.fileUrl) {
          return {
            name: String(item.name || item.originalFileName || item.fileName || '相册照片'),
            url: String(item.fileUrl)
          }
        }
        return null
      })
      .filter((item: any) => !!item?.url)
    return { text, photoUrls }
  } catch {
    return { text: remark, photoUrls: [] as Array<{ name: string; url: string }> }
  }
}

async function beforeUploadPhoto(file: File) {
  uploading.value = true
  try {
    const uploaded = await uploadOaFile(file, 'oa-album-photo')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回图片地址')
      return false
    }
    photoFiles.value.push({
      name: uploaded.originalFileName || uploaded.fileName || file.name,
      url
    })
    if (!form.coverUrl) {
      form.coverUrl = url
    }
    form.photoCount = Math.max(Number(form.photoCount || 0), photoFiles.value.length)
    message.success('照片上传成功')
  } catch (error: any) {
    message.error(error?.message || '照片上传失败')
  } finally {
    uploading.value = false
  }
  return false
}

function setCover(url: string) {
  form.coverUrl = url
}

function removePhoto(index: number) {
  const target = photoFiles.value[index]
  photoFiles.value.splice(index, 1)
  if (target?.url && form.coverUrl === target.url) {
    form.coverUrl = photoFiles.value[0]?.url || ''
  }
  form.photoCount = Math.max(0, photoFiles.value.length)
}

fetchData()
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>

<style scoped>
.photo-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.photo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
</style>
