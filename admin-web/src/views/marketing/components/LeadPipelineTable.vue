<template>
  <PageContainer :title="title" :sub-title="subTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <template v-if="mode === 'consultation'">
          <a-form-item label="咨询人姓名">
            <a-input v-model:value="query.consultantName" placeholder="请输入 咨询人姓名" allow-clear />
          </a-form-item>
          <a-form-item label="联系电话">
            <a-input v-model:value="query.consultantPhone" placeholder="请输入 联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="老人姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 老人姓名" allow-clear />
          </a-form-item>
          <a-form-item label="老人联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 老人联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="咨询日期">
            <a-range-picker
              v-model:value="query.consultDateRange"
              value-format="YYYY-MM-DD"
              :placeholder="['开始日期', '结束日期']"
            />
          </a-form-item>
          <a-form-item label="咨询类型">
            <a-input v-model:value="query.consultType" placeholder="请选择 咨询类型" allow-clear />
          </a-form-item>
          <a-form-item label="媒体渠道">
            <a-input v-model:value="query.mediaChannel" placeholder="请选择 媒体渠道" allow-clear />
          </a-form-item>
        </template>

        <template v-else-if="mode === 'intent' || mode === 'invalid'">
          <a-form-item label="老人姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 老人姓名" allow-clear />
          </a-form-item>
          <a-form-item label="老人联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 老人联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="信息来源">
            <a-input v-model:value="query.infoSource" placeholder="请选择 信息来源" allow-clear />
          </a-form-item>
          <a-form-item label="客户标签">
            <a-input v-model:value="query.customerTag" placeholder="请选择 客户标签" allow-clear />
          </a-form-item>
          <a-form-item label="归属营销人员">
            <a-input v-model:value="query.marketerName" placeholder="请选择 归属营销人员" allow-clear />
          </a-form-item>
        </template>

        <template v-else-if="mode === 'reservation'">
          <a-form-item label="姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 姓名" allow-clear />
          </a-form-item>
          <a-form-item label="联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
          </a-form-item>
        </template>

        <template v-else>
          <a-form-item label="客户姓名">
            <a-input v-model:value="query.elderName" placeholder="请输入 客户姓名" allow-clear />
          </a-form-item>
          <a-form-item label="联系电话">
            <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
          </a-form-item>
          <a-form-item label="跟进人">
            <a-input v-model:value="query.marketerName" placeholder="请输入 跟进人" allow-clear />
          </a-form-item>
        </template>

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
          <a-button v-for="item in actionButtons" :key="item.key" :type="item.type" @click="handleTopAction(item.key)">
            {{ item.label }}
          </a-button>
        </a-space>
      </div>
      <a-table
        :data-source="displayRows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 1600 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'gender'">
            {{ genderText(record.gender) }}
          </template>
          <template v-else-if="column.key === 'refunded'">
            {{ record.refunded === 1 ? '是' : '否' }}
          </template>
          <template v-else-if="column.key === 'reservationAmount'">
            {{ record.reservationAmount == null ? '-' : `¥${Number(record.reservationAmount).toFixed(2)}` }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-space wrap>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button v-if="mode === 'consultation' || mode === 'intent' || mode === 'reservation'" type="link" @click="viewMore(record)">查看更多</a-button>
              <a-button v-if="mode === 'reservation'" type="link" @click="toggleRefund(record)">退款</a-button>
              <a-button v-if="mode === 'reservation'" type="link" @click="transferToAdmission(record)">转入住</a-button>
              <a-button v-if="mode === 'invalid'" type="link" @click="viewMore(record)">查看</a-button>
              <a-button v-if="mode === 'invalid'" type="link" @click="recoverLead(record)">恢复客户</a-button>
              <a-button v-if="mode === 'callback'" type="link" @click="executeCallback(record)">执行</a-button>
              <a-button type="link" danger @click="remove(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="displayTotal"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="open" :title="modalTitle" width="820px" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="咨询人姓名" name="consultantName">
              <a-input v-model:value="form.consultantName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系电话" name="consultantPhone">
              <a-input v-model:value="form.consultantPhone" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="老人姓名" name="elderName">
              <a-input v-model:value="form.elderName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="老人联系电话" name="elderPhone">
              <a-input v-model:value="form.elderPhone" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="性别">
              <a-select v-model:value="form.gender" allow-clear>
                <a-select-option :value="1">男</a-select-option>
                <a-select-option :value="2">女</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="年龄">
              <a-input-number v-model:value="form.age" :min="0" :max="120" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="咨询日期">
              <a-date-picker v-model:value="form.consultDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="咨询类型">
              <a-input v-model:value="form.consultType" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="媒体渠道">
              <a-input v-model:value="form.mediaChannel" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="信息来源">
              <a-input v-model:value="form.infoSource" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="接待人">
              <a-input v-model:value="form.receptionistName" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="家庭地址">
              <a-input v-model:value="form.homeAddress" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="归属营销人员">
              <a-input v-model:value="form.marketerName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="客户标签">
              <a-input v-model:value="form.customerTag" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预订房号">
              <a-input v-model:value="form.reservationRoomNo" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预订金额">
              <a-input-number v-model:value="form.reservationAmount" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="收款时间">
              <a-date-picker
                v-model:value="form.paymentTime"
                value-format="YYYY-MM-DD HH:mm:ss"
                show-time
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="预约渠道">
              <a-input v-model:value="form.reservationChannel" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划执行时间">
              <a-date-picker v-model:value="form.nextFollowDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="回访状态">
              <a-input v-model:value="form.followupStatus" placeholder="已回访/未回访" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="备注">
              <a-textarea v-model:value="form.remark" :rows="2" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import { createCrmLead, deleteCrmLead, getLeadPage, updateCrmLead } from '../../../api/marketing'
import type { CrmLeadItem, PageResult } from '../../../types'

const props = withDefaults(defineProps<{
  mode: 'consultation' | 'intent' | 'reservation' | 'invalid' | 'callback'
  title: string
  subTitle: string
}>(), {
  mode: 'consultation'
})

const router = useRouter()
const loading = ref(false)
const rows = ref<CrmLeadItem[]>([])
const total = ref(0)

const query = reactive({
  consultantName: '',
  consultantPhone: '',
  elderName: '',
  elderPhone: '',
  consultDateRange: [] as string[],
  consultType: '',
  mediaChannel: '',
  infoSource: '',
  customerTag: '',
  marketerName: '',
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<CrmLeadItem>>({})

const rules: FormRules = {
  consultantName: [{ required: true, message: '请输入咨询人姓名' }],
  elderName: [{ required: true, message: '请输入老人姓名' }]
}

const modalTitle = computed(() => form.id ? '编辑客户' : '新增客户')

const columns = computed(() => {
  if (props.mode === 'consultation') {
    return [
      { title: '咨询人姓名', dataIndex: 'consultantName', key: 'consultantName', width: 130 },
      { title: '联系电话', dataIndex: 'consultantPhone', key: 'consultantPhone', width: 130 },
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 130 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '咨询日期', dataIndex: 'consultDate', key: 'consultDate', width: 120 },
      { title: '咨询类型', dataIndex: 'consultType', key: 'consultType', width: 120 },
      { title: '媒体渠道', dataIndex: 'mediaChannel', key: 'mediaChannel', width: 120 },
      { title: '信息来源', dataIndex: 'infoSource', key: 'infoSource', width: 120 },
      { title: '接待人', dataIndex: 'receptionistName', key: 'receptionistName', width: 120 },
      { title: '家庭地址', dataIndex: 'homeAddress', key: 'homeAddress', width: 160 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'operation', fixed: 'right', width: 220 }
    ]
  }
  if (props.mode === 'intent') {
    return [
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
      { title: '信息来源', dataIndex: 'infoSource', key: 'infoSource', width: 120 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '回访状态', dataIndex: 'followupStatus', key: 'followupStatus', width: 120 },
      { title: '推荐渠道', dataIndex: 'referralChannel', key: 'referralChannel', width: 120 },
      { title: '客户标签', dataIndex: 'customerTag', key: 'customerTag', width: 120 },
      { title: '归属营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 140 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'operation', fixed: 'right', width: 180 }
    ]
  }
  if (props.mode === 'reservation') {
    return [
      { title: '姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '身份证号', dataIndex: 'idCardNo', key: 'idCardNo', width: 160 },
      { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 130 },
      { title: '定金额度', dataIndex: 'reservationAmount', key: 'reservationAmount', width: 120 },
      { title: '预订房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 140 },
      { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
      { title: '收款时间', dataIndex: 'paymentTime', key: 'paymentTime', width: 170 },
      { title: '是否退款', dataIndex: 'refunded', key: 'refunded', width: 100 },
      { title: '预约渠道', dataIndex: 'reservationChannel', key: 'reservationChannel', width: 120 },
      { title: '状态', dataIndex: 'reservationStatus', key: 'reservationStatus', width: 100 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'operation', fixed: 'right', width: 260 }
    ]
  }
  if (props.mode === 'invalid') {
    return [
      { title: '序号', dataIndex: 'index', key: 'index', width: 80 },
      { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
      { title: '老人联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
      { title: '信息来源', dataIndex: 'infoSource', key: 'infoSource', width: 120 },
      { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
      { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
      { title: '推荐渠道', dataIndex: 'referralChannel', key: 'referralChannel', width: 120 },
      { title: '客户标签', dataIndex: 'customerTag', key: 'customerTag', width: 120 },
      { title: '归属营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 140 },
      { title: '失效时间', dataIndex: 'invalidTime', key: 'invalidTime', width: 170 },
      { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
      { title: '操作', key: 'operation', fixed: 'right', width: 200 }
    ]
  }
  return [
    { title: '客户姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
    { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
    { title: '跟进人', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
    { title: '计划执行时间', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 170 },
    { title: '计划标题', dataIndex: 'remark', key: 'remark', width: 160 },
    { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
    { title: '操作', key: 'operation', fixed: 'right', width: 140 }
  ]
})

const actionButtons = computed(() => {
  if (props.mode === 'consultation') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'abandon', label: '放弃客户', type: 'default' as const }
    ]
  }
  if (props.mode === 'intent') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'tag', label: '设置客户标签', type: 'default' as const },
      { key: 'callback', label: '添加回访计划', type: 'default' as const },
      { key: 'abandon', label: '放弃客户', type: 'default' as const }
    ]
  }
  if (props.mode === 'reservation') {
    return [
      { key: 'create', label: '新增', type: 'primary' as const },
      { key: 'batchDelete', label: '删除', type: 'default' as const }
    ]
  }
  if (props.mode === 'invalid') {
    return [{ key: 'recover', label: '恢复客户', type: 'primary' as const }]
  }
  return [
    { key: 'deletePlan', label: '删除', type: 'default' as const }
  ]
})

const callbackRows = computed(() => rows.value.filter((item) => !!item.nextFollowDate))
const displayRows = computed(() => {
  const list = props.mode === 'callback' ? callbackRows.value : rows.value
  return list.map((item, index) => ({ ...item, index: index + 1 }))
})
const displayTotal = computed(() => props.mode === 'callback' ? callbackRows.value.length : total.value)

function defaultStatus(mode: string) {
  if (mode === 'consultation') return 0
  if (mode === 'intent') return 1
  if (mode === 'reservation') return 2
  if (mode === 'invalid') return 3
  return undefined
}

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
}

function buildQueryParams() {
  return {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    status: defaultStatus(props.mode),
    consultantName: query.consultantName || undefined,
    consultantPhone: query.consultantPhone || undefined,
    elderName: query.elderName || undefined,
    elderPhone: query.elderPhone || undefined,
    consultDateFrom: query.consultDateRange?.[0] || undefined,
    consultDateTo: query.consultDateRange?.[1] || undefined,
    consultType: query.consultType || undefined,
    mediaChannel: query.mediaChannel || undefined,
    infoSource: query.infoSource || undefined,
    customerTag: query.customerTag || undefined,
    marketerName: query.marketerName || undefined
  }
}

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<CrmLeadItem> = await getLeadPage(buildQueryParams())
    rows.value = page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.consultantName = ''
  query.consultantPhone = ''
  query.elderName = ''
  query.elderPhone = ''
  query.consultDateRange = []
  query.consultType = ''
  query.mediaChannel = ''
  query.infoSource = ''
  query.customerTag = ''
  query.marketerName = ''
  query.pageNo = 1
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

function handleTopAction(key: string) {
  if (key === 'create') {
    openForm()
    return
  }
  if (key === 'abandon') {
    batchMoveToInvalid()
    return
  }
  if (key === 'recover') {
    message.info('请在列表中选择具体客户执行恢复')
    return
  }
  if (key === 'callback') {
    message.info('可在编辑里填写计划执行时间与计划标题')
    return
  }
  if (key === 'tag') {
    message.info('可在编辑里设置客户标签')
    return
  }
  if (key === 'batchDelete' || key === 'deletePlan') {
    message.info('请在列表中逐条删除')
  }
}

function openForm(row?: CrmLeadItem) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: undefined,
      name: '',
      phone: '',
      consultantName: '',
      consultantPhone: '',
      elderName: '',
      elderPhone: '',
      gender: undefined,
      age: undefined,
      consultDate: dayjs().format('YYYY-MM-DD'),
      consultType: '',
      mediaChannel: '',
      infoSource: '',
      receptionistName: '',
      homeAddress: '',
      marketerName: '',
      followupStatus: '未回访',
      customerTag: '',
      nextFollowDate: '',
      reservationAmount: undefined,
      reservationRoomNo: '',
      reservationChannel: '',
      reservationStatus: props.mode === 'reservation' ? '预定' : '',
      refunded: 0,
      orgName: '德合养老院',
      status: defaultStatus(props.mode) ?? 0
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
      name: form.name || form.elderName || form.consultantName || '未命名线索',
      phone: form.phone || form.elderPhone || form.consultantPhone,
      status: defaultStatus(props.mode) ?? form.status ?? 0
    }
    if (form.id) {
      await updateCrmLead(form.id, payload)
    } else {
      await createCrmLead(payload)
    }
    message.success('保存成功')
    open.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

function viewMore(record: CrmLeadItem) {
  Modal.info({
    title: '客户详情',
    content: `${record.elderName || '-'} / ${record.elderPhone || '-'} / ${record.remark || '-'}`
  })
}

function transferToAdmission(record: CrmLeadItem) {
  updateCrmLead(record.id, { ...record, contractSignedFlag: 1, contractSignedAt: dayjs().format('YYYY-MM-DD HH:mm:ss') })
    .then(() => {
      message.success('已标记转入住')
      router.push('/marketing/contract-signing')
    })
    .catch(() => message.error('转入住失败'))
}

function executeCallback(record: CrmLeadItem) {
  updateCrmLead(record.id, { ...record, followupStatus: '已回访', nextFollowDate: dayjs().format('YYYY-MM-DD') })
    .then(() => {
      message.success('已执行回访')
      fetchData()
    })
    .catch(() => message.error('执行失败'))
}

function recoverLead(record: CrmLeadItem) {
  updateCrmLead(record.id, {
    ...record,
    status: 1,
    invalidTime: undefined
  }).then(() => {
    message.success('客户已恢复到意向客户')
    fetchData()
  }).catch(() => message.error('恢复失败'))
}

function toggleRefund(record: CrmLeadItem) {
  updateCrmLead(record.id, { ...record, refunded: record.refunded === 1 ? 0 : 1 })
    .then(() => {
      message.success('退款状态已更新')
      fetchData()
    })
    .catch(() => message.error('更新失败'))
}

function batchMoveToInvalid() {
  if (!rows.value.length) {
    message.info('暂无可处理客户')
    return
  }
  const first = rows.value[0]
  updateCrmLead(first.id, { ...first, status: 3, invalidTime: dayjs().format('YYYY-MM-DD HH:mm:ss') })
    .then(() => {
      message.success('已放弃1条客户')
      fetchData()
    })
    .catch(() => message.error('放弃失败'))
}

function remove(id: number) {
  Modal.confirm({
    title: '确认删除该客户记录吗？',
    onOk: async () => {
      await deleteCrmLead(id)
      message.success('已删除')
      fetchData()
    }
  })
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
