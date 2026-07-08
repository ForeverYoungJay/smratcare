<template>
  <PageContainer title="相册管理" subTitle="活动照片与影像资料管理">
    <template #extra>
      <a-space wrap>
        <a-button type="primary" @click="openEdit()">新增相册</a-button>
      </a-space>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="相册标题/备注" allow-clear />
      </a-form-item>
      <a-form-item label="分类">
        <a-input v-model:value="query.category" placeholder="如：节日活动" allow-clear />
      </a-form-item>
      <a-form-item label="照片夹">
        <a-auto-complete
          v-model:value="query.folderName"
          :options="folderOptions"
          placeholder="如：2026 春季活动"
          allow-clear
          style="width: 220px"
        />
      </a-form-item>
      <a-form-item label="相册类型">
        <a-select v-model:value="query.albumScope" allow-clear style="width: 160px" :options="albumScopeOptions" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
    </SearchForm>

    <div v-if="selectedRowKeys.length" class="selection-bar">
      <span class="selection-bar__count">已勾选 {{ selectedRowKeys.length }} 条</span>
      <template v-if="selectedRowKeys.length === 1">
        <a-button size="small" @click="editSelected">编辑</a-button>
        <a-button size="small" :disabled="!canPublishSingle" @click="publishSelected">发布</a-button>
        <a-button size="small" :disabled="!canArchiveSingle" @click="archiveSelected">归档</a-button>
        <a-button size="small" danger @click="removeSelected">删除</a-button>
      </template>
      <a-button size="small" @click="batchPublish">批量发布</a-button>
      <a-button size="small" @click="batchArchive">批量归档</a-button>
      <a-button size="small" danger @click="batchRemove">批量删除</a-button>
      <a-button size="small" type="text" class="selection-bar__clear" @click="selectedRowKeys = []">取消选择</a-button>
    </div>

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
        <template v-else-if="column.key === 'albumScope'">
          <a-tag :color="record.albumScope === 'PERSONAL' ? 'purple' : 'green'">{{ albumScopeText(record.albumScope) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'elderId'">
          <span>{{ record.albumScope === 'PERSONAL' ? findElderName(record.elderId) || record.elderId || '-' : '全体可见' }}</span>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑相册' : '新增相册'" @ok="submit" :confirm-loading="saving" width="680px">
      <a-form layout="vertical">
        <a-form-item label="相册标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="分类">
              <a-input v-model:value="form.category" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="照片夹">
              <a-auto-complete
                v-model:value="form.folderName"
                :options="folderOptions"
                placeholder="例如：2026 春季活动"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="相册类型" required>
              <a-radio-group v-model:value="form.albumScope">
                <a-radio-button value="PERSONAL">个人相册</a-radio-button>
                <a-radio-button value="GROUP">集体活动相册</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="所属老人" :required="form.albumScope === 'PERSONAL'">
              <a-select
                v-model:value="form.elderId"
                :disabled="form.albumScope !== 'PERSONAL'"
                :options="elderOptions"
                :loading="elderLoading"
                show-search
                allow-clear
                placeholder="选择个人相册所属老人"
                :filter-option="false"
                @search="searchElders"
                @focus="() => !elderOptions.length && searchElders('')"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="封面地址">
          <a-input v-model:value="form.coverUrl" />
        </a-form-item>
        <a-form-item label="照片上传">
          <a-space>
            <a-upload :show-upload-list="false" accept="image/*" :before-upload="beforeUploadPhoto">
              <a-button :loading="uploading">上传照片</a-button>
            </a-upload>
            <a-button :disabled="photoFiles.length === 0" @click="clearPhotos">清空照片</a-button>
          </a-space>
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
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="照片数量">
              <a-input-number :value="Number(form.photoCount || photoFiles.length)" :min="0" style="width: 100%" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="拍摄日期">
              <a-date-picker v-model:value="shootDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
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
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createAlbum, deleteAlbum, getAlbumFolders, getAlbumPage, updateAlbum, uploadOaFile } from '../../api/oa'
import type { OaAlbum, PageResult } from '../../types'
import { useElderOptions } from '../../composables/useElderOptions'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaAlbum[]>([])
const selectedRowKeys = ref<string[]>([])
const query = reactive({
  keyword: '',
  category: '',
  folderName: '',
  albumScope: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, elderLoading, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 80 })

const columns = [
  { title: '封面', dataIndex: 'coverUrl', key: 'coverUrl', width: 90 },
  { title: '相册标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '相册类型', dataIndex: 'albumScope', key: 'albumScope', width: 130 },
  { title: '可见对象', dataIndex: 'elderId', key: 'elderId', width: 150 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 140 },
  { title: '照片夹', dataIndex: 'folderName', key: 'folderName', width: 160 },
  { title: '照片数量', dataIndex: 'photoCount', key: 'photoCount', width: 100 },
  { title: '拍摄日期', dataIndex: 'shootDate', key: 'shootDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]
const albumScopeOptions = [
  { label: '个人相册', value: 'PERSONAL' },
  { label: '集体活动相册', value: 'GROUP' }
]
const folderOptions = ref<Array<{ value: string }>>([])

const editOpen = ref(false)
const shootDate = ref<Dayjs | undefined>()
const form = reactive<Partial<OaAlbum>>({ status: 'DRAFT', photoCount: 0, category: '', folderName: '', albumScope: 'GROUP' })
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
const canPublishSingle = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'PUBLISHED')
const canArchiveSingle = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'ARCHIVED')

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

function albumScopeText(scope?: string) {
  return scope === 'PERSONAL' ? '个人相册' : '集体活动相册'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OaAlbum> = await getAlbumPage(query)
    rows.value = (res.list || []).map((item) => {
      const parsed = parseAlbumPhotos(item.photosJson, item.remark)
      return {
        ...item,
        id: String(item.id),
        albumScope: item.albumScope === 'PERSONAL' ? 'PERSONAL' : 'GROUP',
        coverUrl: item.coverUrl || parsed[0]?.url || '',
        photoCount: Math.max(Number(item.photoCount || 0), parsed.length)
      }
    })
    rows.value
      .filter((item) => item.albumScope === 'PERSONAL' && item.elderId)
      .forEach((item) => ensureSelectedElder(item.elderId))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

async function fetchFolderOptions() {
  try {
    const folders = await getAlbumFolders()
    folderOptions.value = (folders || []).map((item) => ({ value: String(item) }))
  } catch {
    folderOptions.value = []
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
  query.folderName = ''
  query.albumScope = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaAlbum) {
  Object.assign(form, record || {
    status: 'DRAFT',
    photoCount: 0,
    title: '',
    category: '',
    folderName: '',
    albumScope: 'GROUP',
    elderId: undefined,
    coverUrl: '',
    remark: '',
    photosJson: ''
  })
  form.albumScope = form.albumScope === 'PERSONAL' ? 'PERSONAL' : 'GROUP'
  if (form.albumScope !== 'PERSONAL') {
    form.elderId = undefined
  } else if (form.elderId) {
    ensureSelectedElder(form.elderId)
  }
  const parsed = parseAlbumPhotos(form.photosJson, form.remark)
  form.remark = parseRemarkText(form.remark)
  photoFiles.value = parsed
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
  if (form.albumScope === 'PERSONAL' && !form.elderId) {
    message.warning('个人相册请选择所属老人')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    const payload = {
      ...form,
      albumScope: form.albumScope === 'PERSONAL' ? 'PERSONAL' : 'GROUP',
      elderId: form.albumScope === 'PERSONAL' ? form.elderId : undefined,
      coverUrl: form.coverUrl || photoFiles.value[0]?.url,
      photosJson: JSON.stringify(photoFiles.value),
      photoCount: Math.max(Number(form.photoCount || 0), photoFiles.value.length),
      shootDate: shootDate.value ? shootDate.value.format('YYYY-MM-DD') : undefined
    }
    if (form.id) {
      await updateAlbum(String(form.id), payload)
    } else {
      await createAlbum(payload)
    }
    editOpen.value = false
    await fetchData()
    await fetchFolderOptions()
    message.success('保存成功')
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(record: OaAlbum) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除相册「${record.title || '-'}」吗？`,
    okType: 'danger',
    async onOk() {
      await deleteAlbum(String(record.id))
      message.success('删除成功')
      await fetchData()
      await fetchFolderOptions()
    }
  })
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
  await fetchData()
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
  await fetchData()
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
  await fetchData()
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
  await fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '批量删除',
    content: `确认删除选中的 ${selectedRecords.value.length} 条相册吗？`,
    okType: 'danger',
    async onOk() {
      await Promise.all(selectedRecords.value.map((item) => deleteAlbum(String(item.id))))
      message.success(`批量删除完成，共处理 ${selectedRecords.value.length} 条`)
      await fetchData()
      await fetchFolderOptions()
    }
  })
}

function parseAlbumPhotos(photosJson?: string, remark?: string) {
  const fromJson = parseFileListJson(photosJson)
  if (fromJson.length) return fromJson
  const fromRemark = parseFileListJson(remark, true)
  return fromRemark
}

function parseFileListJson(source?: string, isRemark = false) {
  if (!source) return [] as Array<{ name: string; url: string }>
  try {
    const parsed = JSON.parse(source)
    const list = isRemark
      ? (Array.isArray(parsed?.photoUrls) ? parsed.photoUrls : Array.isArray(parsed?.photos) ? parsed.photos : [])
      : (Array.isArray(parsed) ? parsed : [])
    return list
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
  } catch {
    return [] as Array<{ name: string; url: string }>
  }
}

function parseRemarkText(source?: string) {
  if (!source) return ''
  try {
    const parsed = JSON.parse(source)
    if (typeof parsed?.text === 'string') {
      return parsed.text
    }
  } catch {
    // ignore
  }
  return source
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

function clearPhotos() {
  photoFiles.value = []
  form.coverUrl = ''
  form.photoCount = 0
}

fetchData()
fetchFolderOptions()
</script>

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
  border: 1px solid var(--border-soft);
  border-radius: 6px;
}
</style>
