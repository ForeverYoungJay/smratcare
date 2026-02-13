<template>
  <PageContainer title="商品标签" subTitle="维护标签与分类">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" class="search-form" :model="query">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="标签名称/编码" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openCreate">新增标签</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="rows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="tagCode" title="标签编码" width="160" />
        <vxe-column field="tagName" title="标签名称" min-width="180" />
        <vxe-column field="tagType" title="标签类型" width="160" />
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="row.status === 1 ? 'green' : 'default'">{{ row.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="200" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a @click="openEdit(row)">编辑</a>
              <a @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</a>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-drawer v-model:open="editorOpen" title="标签信息" width="420">
      <a-form layout="vertical" :model="form" :rules="rules" ref="formRef">
        <a-form-item label="标签名称" name="tagName" required>
          <a-input v-model:value="form.tagName" />
        </a-form-item>
        <a-form-item label="标签编码" name="tagCode">
          <a-input v-model:value="form.tagCode" placeholder="留空自动生成" />
        </a-form-item>
        <a-form-item label="标签类型">
          <a-input v-model:value="form.tagType" placeholder="如 SUGAR/SEAFOOD" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.status" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>

      <div class="drawer-footer">
        <a-space>
          <a-button @click="editorOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </div>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getProductTagList, createProductTag, updateProductTag } from '../../api/store'
import type { ProductTagItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<ProductTagItem[]>([])
const editorOpen = ref(false)
const formRef = ref()

const query = reactive({ keyword: '' })
const form = reactive<any>({ id: undefined, tagCode: '', tagName: '', tagType: '', status: true })

const rules = {
  tagName: [{ required: true, message: '请输入标签名称' }],
  tagCode: [
    { pattern: /^[A-Za-z0-9_]*$/, message: '标签编码仅允许字母/数字/下划线' }
  ]
}

async function fetchData() {
  loading.value = true
  try {
    const list = await getProductTagList()
    rows.value = query.keyword
      ? list.filter((t) => (t.tagName || '').includes(query.keyword) || (t.tagCode || '').includes(query.keyword))
      : list
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  fetchData()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((t) => ({
      编码: t.tagCode,
      名称: t.tagName,
      类型: t.tagType,
      状态: t.status === 1 ? '启用' : '停用'
    })),
    '商品标签'
  )
}

function openCreate() {
  Object.assign(form, { id: undefined, tagCode: '', tagName: '', tagType: '', status: true })
  editorOpen.value = true
}

function openEdit(row: ProductTagItem) {
  Object.assign(form, {
    id: row.id,
    tagCode: row.tagCode,
    tagName: row.tagName,
    tagType: row.tagType,
    status: row.status === 1
  })
  editorOpen.value = true
}

async function submit() {
  await formRef.value?.validate()
  if (!form.id) {
    message.error('标签ID缺失，请刷新列表后重试')
    return
  }
  saving.value = true
  try {
    const payload = {
      tagCode: form.tagCode,
      tagName: form.tagName,
      tagType: form.tagType,
      status: form.status ? 1 : 0
    }
    if (form.id) {
      await updateProductTag(form.id, payload)
      message.success('保存成功')
    } else {
      await createProductTag(payload)
      message.success('创建成功')
    }
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function toggleStatus(row: ProductTagItem) {
  Modal.confirm({
    title: row.status === 1 ? '确认停用该标签？' : '确认启用该标签？',
    onOk: async () => {
      await updateProductTag(row.id, {
        tagCode: row.tagCode,
        tagName: row.tagName,
        tagType: row.tagType,
        status: row.status === 1 ? 0 : 1
      })
      message.success('操作成功')
      fetchData()
    }
  })
}

onMounted(fetchData)
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.drawer-footer {
  margin-top: 16px;
  text-align: right;
}
</style>
