<template>
  <PageContainer title="老人列表" subTitle="档案查询与床位管理">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-form">
        <a-form-item label="姓名">
          <a-input v-model:value="query.fullName" placeholder="姓名" allow-clear />
        </a-form-item>
        <a-form-item label="身份证">
          <a-input v-model:value="query.idCardNo" placeholder="身份证号" allow-clear />
        </a-form-item>
        <a-form-item label="床位号">
          <a-input v-model:value="query.bedNo" placeholder="床位号" allow-clear />
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="query.careLevel" placeholder="等级" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">在院</a-select-option>
            <a-select-option :value="2">请假</a-select-option>
            <a-select-option :value="3">离院</a-select-option>
          </a-select>
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
          <a-button type="primary" @click="goCreate">新增老人</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>

      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ y: 520 }"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="goDetail(record.id)">详情</a-button>
              <a-button type="link" @click="goEdit(record.id)">编辑</a-button>
              <a-button type="link" @click="openChangeBed(record)">换床</a-button>
              <a-button type="link" @click="openCheckout(record)">退住</a-button>
              <a-button type="link" @click="openBindFamily(record)">绑定家属</a-button>
              <a-button type="link" @click="printElderQr(record)">打印二维码</a-button>
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

    <a-modal v-model:open="changeBedOpen" title="换床" width="420px" @ok="submitChangeBed" @cancel="() => (changeBedOpen = false)">
      <a-form ref="changeBedFormRef" :model="changeBedForm" :rules="changeBedRules" layout="vertical">
        <a-form-item label="选择床位" name="bedId">
          <a-select v-model:value="changeBedForm.bedId" placeholder="请选择床位">
            <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始日期" name="startDate">
          <a-date-picker v-model:value="changeBedForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="checkoutOpen" title="退住" width="420px" @ok="submitCheckout" @cancel="() => (checkoutOpen = false)">
      <a-form ref="checkoutFormRef" :model="checkoutForm" :rules="checkoutRules" layout="vertical">
        <a-form-item label="退住日期" name="endDate">
          <a-date-picker v-model:value="checkoutForm.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="原因" name="reason">
          <a-select v-model:value="checkoutForm.reason" placeholder="请选择退住费用设置">
            <a-select-option v-for="item in dischargeFeeConfigOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="bindOpen" title="绑定家属" width="420px" @ok="submitBindFamily" @cancel="() => (bindOpen = false)">
      <a-form ref="bindFormRef" :model="bindForm" :rules="bindRules" layout="vertical">
        <a-form-item label="家属" name="familyUserId">
          <a-select
            v-model:value="bindForm.familyUserId"
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
          <a-input v-model:value="bindForm.relation" placeholder="如：子女/配偶" />
        </a-form-item>
        <a-form-item label="主联系人">
          <a-switch v-model:checked="bindForm.isPrimary" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="qrOpen" title="老人二维码" width="360px" @ok="printQr" ok-text="打印" @cancel="() => (qrOpen = false)">
      <div class="qr-preview">
        <img :src="qrDataUrl" alt="qr" />
        <div class="qr-text">{{ qrText }}</div>
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { exportCsv } from '../../utils/export'
import { getElderPage, assignBed, unbindBed, bindFamily } from '../../api/elder'
import { getBedList } from '../../api/bed'
import { getFamilyUserPage } from '../../api/family'
import type { BaseConfigItem, BedItem, ElderItem, FamilyBindRequest, PageResult, FamilyUserItem } from '../../types/api'

const router = useRouter()
const loading = ref(false)
const rows = ref<ElderItem[]>([])
const total = ref(0)

const query = reactive({
  fullName: undefined as string | undefined,
  idCardNo: undefined as string | undefined,
  bedNo: undefined as string | undefined,
  careLevel: undefined as string | undefined,
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '姓名', dataIndex: 'fullName', key: 'fullName', width: 120, sorter: true },
  { title: '身份证', dataIndex: 'idCardNo', key: 'idCardNo', width: 180 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120, sorter: true },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, sorter: true },
  { title: '操作', key: 'action', width: 360, fixed: 'right' as const }
]

const beds = ref<BedItem[]>([])
const bedOptions = computed(() =>
  beds.value
    .filter((b) => !b.elderId && (b.status === 1 || b.status === undefined))
    .map((b) => ({ label: `${b.roomNo || b.roomId}-${b.bedNo}`, value: b.id }))
)

const changeBedOpen = ref(false)
const changeBedFormRef = ref<FormInstance>()
const changeBedForm = reactive<{ elderId?: number; bedId?: number; startDate?: string }>({})
const changeBedRules: FormRules = {
  bedId: [{ required: true, message: '请选择床位' }],
  startDate: [{ required: true, message: '请选择开始日期' }]
}

const checkoutOpen = ref(false)
const checkoutFormRef = ref<FormInstance>()
const checkoutForm = reactive<{ elderId?: number; endDate?: string; reason?: string }>({})
const dischargeFeeConfigOptions = ref<Array<{ label: string; value: string }>>([])
const checkoutRules: FormRules = {
  endDate: [{ required: true, message: '请选择退住日期' }]
}

const bindOpen = ref(false)
const bindFormRef = ref<FormInstance>()
const bindForm = reactive<FamilyBindRequest>({ familyUserId: 0, elderId: 0, relation: '', isPrimary: false })
const bindRules: FormRules = {
  familyUserId: [{ required: true, message: '请选择家属' }],
  relation: [{ required: true, message: '请输入关系' }]
}
const familyOptions = ref<Array<{ label: string; value: number }>>([])

const qrOpen = ref(false)
const qrDataUrl = ref('')
const qrText = ref('')

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '未知'
}

function statusTag(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ElderItem> = await getElderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      fullName: query.fullName,
      idCardNo: query.idCardNo,
      bedNo: query.bedNo,
      careLevel: query.careLevel,
      status: query.status,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    rows.value = res.list.map((r: any) => ({
      ...r,
      bedNo: r.bedNo ?? r.currentBed?.bedNo,
      roomNo: r.roomNo ?? r.currentBed?.roomNo
    }))
    total.value = res.total || res.list.length
  } catch {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadBeds() {
  try {
    beds.value = await getBedList()
  } catch {
    beds.value = []
  }
}

async function loadDischargeFeeConfigOptions() {
  try {
    const options = await getBaseConfigItemList({ configGroup: 'DISCHARGE_FEE_CONFIG', status: 1 })
    dischargeFeeConfigOptions.value = (options || []).map((item: BaseConfigItem) => ({
      label: item.itemName,
      value: item.itemName
    }))
  } catch {
    dischargeFeeConfigOptions.value = []
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

function reset() {
  query.pageNo = 1
  query.pageSize = 10
  query.fullName = undefined
  query.idCardNo = undefined
  query.bedNo = undefined
  query.careLevel = undefined
  query.status = undefined
  query.sortBy = undefined
  query.sortOrder = undefined
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

function onTableChange(_: any, __: any, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  query.sortBy = sorter?.field || undefined
  query.sortOrder = sorter?.order === 'ascend' ? 'asc' : sorter?.order === 'descend' ? 'desc' : undefined
  fetchData()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((r) => ({
      姓名: r.fullName,
      身份证: r.idCardNo || '',
      床位号: r.bedNo || '',
      护理等级: r.careLevel || '',
      状态: statusText(r.status)
    })),
    'elder-list.csv'
  )
}

function goCreate() {
  router.push('/elder/create')
}

function goEdit(id: number) {
  router.push(`/elder/edit/${id}`)
}

function goDetail(id: number) {
  router.push(`/elder/detail/${id}`)
}

function openChangeBed(row: ElderItem) {
  changeBedForm.elderId = row.id
  changeBedForm.bedId = undefined
  changeBedForm.startDate = undefined
  changeBedOpen.value = true
}

async function submitChangeBed() {
  if (!changeBedFormRef.value) return
  try {
    await changeBedFormRef.value.validate()
    if (!changeBedForm.elderId || !changeBedForm.bedId || !changeBedForm.startDate) return
    await assignBed(changeBedForm.elderId, changeBedForm.bedId, changeBedForm.startDate)
    message.success('换床成功')
    changeBedOpen.value = false
    fetchData()
  } catch {
    message.error('换床失败')
  }
}

function openCheckout(row: ElderItem) {
  checkoutForm.elderId = row.id
  checkoutForm.endDate = undefined
  checkoutForm.reason = ''
  checkoutOpen.value = true
}

async function submitCheckout() {
  if (!checkoutFormRef.value || !checkoutForm.elderId) return
  try {
    await checkoutFormRef.value.validate()
    await unbindBed(checkoutForm.elderId as number, checkoutForm.endDate, checkoutForm.reason)
    message.success('已退住')
    checkoutOpen.value = false
    fetchData()
  } catch {
    message.error('退住失败')
  }
}

function openBindFamily(row: ElderItem) {
  bindForm.elderId = row.id
  bindForm.familyUserId = 0
  bindForm.relation = ''
  bindForm.isPrimary = false
  loadFamilyUsers()
  bindOpen.value = true
}

async function submitBindFamily() {
  if (!bindFormRef.value) return
  try {
    await bindFormRef.value.validate()
    if (!bindForm.elderId) return
    await bindFamily(bindForm)
    message.success('绑定成功')
    bindOpen.value = false
  } catch {
    message.error('绑定失败')
  }
}

async function printElderQr(row: ElderItem) {
  qrText.value = `ELDER:${row.id}`
  qrDataUrl.value = await QRCode.toDataURL(qrText.value)
  qrOpen.value = true
}

function printQr() {
  if (!qrDataUrl.value) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`<img src="${qrDataUrl.value}" style="width:220px;height:220px"/>`)
  win.document.write(`<div style="margin-top:8px;font-size:12px">${qrText.value}</div>`)
  win.print()
  win.close()
}

onMounted(() => {
  loadDischargeFeeConfigOptions()
  fetchData()
  loadBeds()
})
</script>

<style scoped>
.search-form {
  row-gap: 12px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.qr-preview {
  display: grid;
  justify-items: center;
  gap: 8px;
}
.qr-text {
  font-size: 12px;
  color: var(--muted);
}
</style>
