<template>
  <PageContainer :title="`老人详情 - ${elder?.fullName || ''}`" subTitle="档案综合视图">
    <a-card class="card-elevated" :bordered="false">
      <a-descriptions :column="2" size="small" bordered>
        <a-descriptions-item label="姓名">{{ elder?.fullName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="身份证">{{ elder?.idCardNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="床位">{{ elder?.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ elder?.careLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(elder?.status) }}</a-descriptions-item>
        <a-descriptions-item label="入院日期">{{ elder?.admissionDate || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="base" tab="基础信息">
          <a-form ref="baseFormRef" :model="baseForm" :rules="baseRules" layout="vertical" style="max-width: 760px">
            <a-form-item label="老人编号" name="elderCode">
              <a-input v-model:value="baseForm.elderCode" />
            </a-form-item>
            <a-form-item label="姓名" name="fullName">
              <a-input v-model:value="baseForm.fullName" />
            </a-form-item>
            <a-form-item label="身份证" name="idCardNo">
              <a-input v-model:value="baseForm.idCardNo" />
            </a-form-item>
            <a-form-item label="手机号" name="phone">
              <a-input v-model:value="baseForm.phone" />
            </a-form-item>
            <a-form-item label="护理等级" name="careLevel">
              <a-input v-model:value="baseForm.careLevel" />
            </a-form-item>
            <a-form-item label="状态" name="status">
              <a-select v-model:value="baseForm.status" placeholder="请选择">
                <a-select-option :value="1">在院</a-select-option>
                <a-select-option :value="2">请假</a-select-option>
                <a-select-option :value="3">离院</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="备注" name="remark">
              <a-textarea v-model:value="baseForm.remark" :rows="3" />
            </a-form-item>
            <a-button type="primary" :loading="saving" @click="saveBase">保存</a-button>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="family" tab="家属绑定">
          <div class="table-actions">
            <a-space>
              <a-input v-model:value="familyQuery.keyword" placeholder="搜索姓名/电话" allow-clear style="width: 200px" />
              <a-button type="primary" @click="openFamilyBind">绑定家属</a-button>
            </a-space>
          </div>
          <a-table
            :data-source="pagedFamilies"
            :columns="familyColumns"
            :pagination="false"
            row-key="familyUserId"
            @change="onFamilyTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'isPrimary'">
                <a-tag :color="record.isPrimary === 1 ? 'green' : 'default'">{{ record.isPrimary === 1 ? '是' : '否' }}</a-tag>
              </template>
            </template>
          </a-table>
          <a-pagination
            style="margin-top: 12px; text-align: right;"
            :current="familyQuery.pageNo"
            :page-size="familyQuery.pageSize"
            :total="filteredFamilies.length"
            show-size-changer
            @change="onFamilyPageChange"
            @showSizeChange="onFamilyPageSizeChange"
          />
        </a-tab-pane>

        <a-tab-pane key="disease" tab="疾病与禁忌">
          <a-space direction="vertical" style="width: 100%">
            <a-select v-model:value="diseaseIds" mode="multiple" show-search style="width: 100%" placeholder="选择疾病标签">
              <a-select-option v-for="item in diseaseOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
            <a-button type="primary" @click="saveDiseases">保存疾病</a-button>
          </a-space>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal v-model:open="familyBindOpen" title="绑定家属" width="420px" @ok="submitFamilyBind" @cancel="() => (familyBindOpen = false)">
      <a-form ref="familyBindRef" :model="familyBind" :rules="familyBindRules" layout="vertical">
        <a-form-item label="家属" name="familyUserId">
          <a-select
            v-model:value="familyBind.familyUserId"
            allow-clear
            show-search
            :filter-option="false"
            placeholder="输入姓名/手机号搜索"
            @search="searchFamilyUsers"
            @focus="() => loadFamilyUsers()"
          >
            <a-select-option v-for="item in familyOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关系" name="relation">
          <a-input v-model:value="familyBind.relation" />
        </a-form-item>
        <a-form-item label="主联系人">
          <a-switch v-model:checked="familyBind.isPrimary" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderDetail, updateElder, getElderDiseases, updateElderDiseases, bindFamily } from '../../api/elder'
import { getDiseaseList } from '../../api/store'
import { getFamilyRelations, getFamilyUserPage } from '../../api/family'
import type { DiseaseItem, ElderItem, FamilyRelationItem, FamilyBindRequest, FamilyUserItem, PageResult } from '../../types/api'

const route = useRoute()
const elderId = computed(() => Number(route.params.id))

const elder = ref<ElderItem | null>(null)
const baseForm = reactive<Partial<ElderItem>>({})
const baseFormRef = ref<FormInstance>()
const saving = ref(false)
const activeKey = ref('base')

const baseRules: FormRules = {
  fullName: [{ required: true, message: '请输入姓名' }],
  elderCode: [{ required: true, message: '请输入老人编号' }],
  status: [{ required: true, message: '请选择状态' }]
}

const families = ref<FamilyRelationItem[]>([])
const familyQuery = reactive({
  keyword: '',
  pageNo: 1,
  pageSize: 5,
  sortBy: 'familyUserId',
  sortOrder: 'asc'
})

const familyColumns = [
  { title: '家属ID', dataIndex: 'familyUserId', key: 'familyUserId', width: 120, sorter: true },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 160, sorter: true },
  { title: '关系', dataIndex: 'relation', key: 'relation', width: 120, sorter: true },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 160, sorter: true },
  { title: '主联系人', dataIndex: 'isPrimary', key: 'isPrimary', width: 120 }
]

const familyBindOpen = ref(false)
const familyBindRef = ref<FormInstance>()
const familyBind = reactive<FamilyBindRequest>({ familyUserId: 0, elderId: 0, relation: '', isPrimary: false })
const familyBindRules: FormRules = {
  familyUserId: [{ required: true, message: '请选择家属' }],
  relation: [{ required: true, message: '请输入关系' }]
}
const familyOptions = ref<Array<{ label: string; value: number }>>([])

const diseaseIds = ref<number[]>([])
const diseaseOptions = ref<{ label: string; value: number }[]>([])

const filteredFamilies = computed(() => {
  const keyword = familyQuery.keyword.trim()
  const list = keyword
    ? families.value.filter(
        (f) =>
          (f.realName && f.realName.includes(keyword)) ||
          (f.phone && f.phone.includes(keyword)) ||
          String(f.familyUserId).includes(keyword)
      )
    : families.value
  const sorted = [...list]
  const factor = familyQuery.sortOrder === 'asc' ? 1 : -1
  sorted.sort((a: any, b: any) => {
    const av = a[familyQuery.sortBy as keyof FamilyRelationItem] ?? ''
    const bv = b[familyQuery.sortBy as keyof FamilyRelationItem] ?? ''
    if (av === bv) return 0
    return av > bv ? factor : -factor
  })
  return sorted
})

const pagedFamilies = computed(() => {
  const start = (familyQuery.pageNo - 1) * familyQuery.pageSize
  return filteredFamilies.value.slice(start, start + familyQuery.pageSize)
})

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

async function load() {
  try {
    const detail = await getElderDetail(elderId.value)
    elder.value = detail
    Object.assign(baseForm, elder.value)
  } catch {
    message.error('加载失败')
  }
}

async function loadFamilies() {
  try {
    families.value = await getFamilyRelations(elderId.value)
  } catch {
    families.value = []
  }
}

async function loadFamilyUsers(keyword?: string) {
  try {
    const res: PageResult<FamilyUserItem> = await getFamilyUserPage({
      pageNo: 1,
      pageSize: 50,
      realName: keyword,
      phone: keyword,
      status: 1
    })
    familyOptions.value = res.list.map((item) => ({
      value: item.id,
      label: `${item.realName || '未命名'}${item.phone ? `（${item.phone}）` : ''}`
    }))
  } catch {
    familyOptions.value = []
  }
}

async function searchFamilyUsers(val: string) {
  await loadFamilyUsers(val)
}

async function loadDiseases() {
  try {
    const list = await getDiseaseList()
    diseaseOptions.value = (list || []).map((d: DiseaseItem) => ({ label: d.diseaseName, value: d.id }))
    const current = await getElderDiseases(elderId.value)
    diseaseIds.value = current.map((d) => d.diseaseId)
  } catch {
    diseaseOptions.value = []
  }
}

async function saveBase() {
  if (!baseFormRef.value) return
  try {
    await baseFormRef.value.validate()
    saving.value = true
    await updateElder(elderId.value, baseForm)
    message.success('保存成功')
    await load()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

function openFamilyBind() {
  familyBind.familyUserId = 0
  familyBind.relation = ''
  familyBind.isPrimary = false
  loadFamilyUsers()
  familyBindOpen.value = true
}

async function submitFamilyBind() {
  if (!familyBindRef.value) return
  try {
    await familyBindRef.value.validate()
    familyBind.elderId = elderId.value
    await bindFamily(familyBind)
    message.success('绑定成功')
    familyBindOpen.value = false
    await loadFamilies()
  } catch {
    message.error('绑定失败')
  }
}

async function saveDiseases() {
  try {
    await updateElderDiseases(elderId.value, diseaseIds.value)
    message.success('保存成功')
  } catch {
    message.error('保存失败')
  }
}

function onFamilyTableChange(_: any, __: any, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  familyQuery.sortBy = sorter?.field || 'familyUserId'
  familyQuery.sortOrder = sorter?.order === 'ascend' ? 'asc' : sorter?.order === 'descend' ? 'desc' : 'asc'
}

function onFamilyPageChange(page: number) {
  familyQuery.pageNo = page
}

function onFamilyPageSizeChange(current: number, size: number) {
  familyQuery.pageNo = 1
  familyQuery.pageSize = size
}

onMounted(async () => {
  await load()
  await loadFamilies()
  await loadDiseases()
})
</script>

<style scoped>
.table-actions {
  margin-bottom: 12px;
}
</style>
