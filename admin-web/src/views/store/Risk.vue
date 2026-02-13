<template>
  <PageContainer title="禁忌规则配置" subTitle="疾病与商品标签的风险控制">
    <a-card class="card-elevated" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-space direction="vertical" style="width: 100%">
            <a-input v-model:value="keyword" placeholder="搜索疾病" allow-clear />
            <a-list
              bordered
              :data-source="filteredDiseases"
              :loading="loading"
              class="disease-list"
            >
              <template #renderItem="{ item }">
                <a-list-item
                  :class="['disease-item', item.id === currentDisease?.id ? 'active' : '']"
                  @click="selectDisease(item)"
                >
                  {{ item.diseaseName }}
                </a-list-item>
              </template>
            </a-list>
          </a-space>
        </a-col>
        <a-col :span="18">
          <a-space style="margin-bottom: 12px">
            <a-button type="primary" :loading="saving" @click="save">保存配置</a-button>
            <a-button @click="exportAll">导出CSV</a-button>
            <a-upload :before-upload="beforeImport" :show-upload-list="false" accept=".csv">
              <a-button>导入CSV</a-button>
            </a-upload>
          </a-space>

          <a-empty v-if="!currentDisease" description="请选择左侧疾病" />
          <div v-else>
            <a-alert type="info" show-icon :message="`当前疾病：${currentDisease.diseaseName}`" />
            <a-divider />
            <div v-if="tagGroups.length === 0">
              <a-empty description="暂无标签配置" />
            </div>
            <div v-else class="tag-group">
              <div v-for="(group, index) in tagGroups" :key="index" class="tag-block">
                <a-checkbox-group v-model:value="group.selectedIds" :options="group.options" />
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getDiseaseList, getForbiddenTags, updateForbiddenTags } from '../../api/store'
import type { DiseaseItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const diseases = ref<DiseaseItem[]>([])
const keyword = ref('')
const currentDisease = ref<DiseaseItem | null>(null)
const tagGroups = ref<{ selectedIds: number[]; options: { label: string; value: number }[] }[]>([])

const filteredDiseases = computed(() => {
  if (!keyword.value) return diseases.value
  return diseases.value.filter((d) => (d.diseaseName || '').includes(keyword.value))
})

function toFlatGroup(groups: any[], selectedIds: number[]) {
  const allTags = groups.flatMap((g) => g.tags || [])
  const options = allTags.map((t: any) => ({ label: t.tagName, value: t.id }))
  const selected = allTags.map((t: any) => t.id).filter((id: number) => selectedIds.includes(id))
  return [{ selectedIds: selected, options }]
}

async function selectDisease(item: DiseaseItem) {
  currentDisease.value = item
  loading.value = true
  try {
    const res: any = await getForbiddenTags(item.id)
    const groups = res?.groups || []
    const selectedIds = res?.selectedTagIds || []
    tagGroups.value = toFlatGroup(groups, selectedIds)
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!currentDisease.value) return
  saving.value = true
  try {
    const tagIds = tagGroups.value.flatMap((g) => g.selectedIds)
    await updateForbiddenTags(currentDisease.value.id, tagIds)
    message.success('保存成功')
  } finally {
    saving.value = false
  }
}

async function exportAll() {
  if (!diseases.value.length) return
  const rows: Record<string, any>[] = []
  for (const d of diseases.value) {
    const res: any = await getForbiddenTags(d.id)
    const selectedIds = res?.selectedTagIds || []
    rows.push({ diseaseId: d.id, diseaseName: d.diseaseName, tagIds: selectedIds.join(',') })
  }
  exportCsv(rows, '禁忌规则')
}

function beforeImport(file: File) {
  const reader = new FileReader()
  reader.onload = async () => {
    const text = String(reader.result || '')
    const lines = text.split('\n').map((l) => l.trim()).filter(Boolean)
    for (const line of lines.slice(1)) {
      const [diseaseId, diseaseName, tagIds] = line.split(',')
      const disease =
        diseases.value.find((d) => String(d.id) === diseaseId) ||
        diseases.value.find((d) => d.diseaseName === diseaseName)
      if (!disease) continue
      const ids = (tagIds || '')
        .split(';')
        .join(',')
        .split(',')
        .map((v) => Number(v))
        .filter(Boolean)
      await updateForbiddenTags(disease.id, ids)
    }
    message.success('导入完成')
    if (currentDisease.value) {
      selectDisease(currentDisease.value)
    }
  }
  reader.readAsText(file)
  return false
}

async function init() {
  loading.value = true
  try {
    diseases.value = await getDiseaseList()
    if (diseases.value.length) {
      selectDisease(diseases.value[0])
    }
  } finally {
    loading.value = false
  }
}

onMounted(init)
</script>

<style scoped>
.disease-list {
  max-height: 600px;
  overflow: auto;
}
.disease-item {
  cursor: pointer;
}
.disease-item.active {
  background: rgba(24, 144, 255, 0.08);
}
.tag-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.tag-block {
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}
.tag-title {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>
