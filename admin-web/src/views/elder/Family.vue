<template>
  <PageContainer title="家属账号与绑定" subTitle="统一处理家属账号、绑定老人、沟通协同和收费跟进">
    <template #extra>
      <a-space wrap>
        <a-button @click="fetchData">刷新</a-button>
        <a-button type="primary" @click="openUserEditor()">新增家属</a-button>
      </a-space>
    </template>
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-form">
        <a-form-item label="姓名">
          <a-input v-model:value="query.realName" placeholder="姓名" allow-clear />
        </a-form-item>
        <a-form-item label="电话">
          <a-input v-model:value="query.phone" placeholder="手机号" allow-clear />
        </a-form-item>
        <a-form-item label="关联老人">
          <ElderNameAutocomplete
            v-model:value="query.elderName"
            placeholder="老人姓名(编号)"
            width="220px"
            @select="() => { query.pageNo = 1; fetchData() }"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
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

    <section class="summary-grid">
      <div class="summary-card">
        <span>家属账号数</span>
        <strong>{{ total }}</strong>
        <small>当前筛选结果下的账号总量</small>
      </div>
      <div class="summary-card">
        <span>正常账号</span>
        <strong>{{ activeUserCount }}</strong>
        <small>可继续绑定老人和接收通知</small>
      </div>
      <div class="summary-card">
        <span>已绑定老人</span>
        <strong>{{ totalLinkedElders }}</strong>
        <small>按当前页家属账号汇总的绑定人数</small>
      </div>
      <div class="summary-card">
        <span>待补绑定账号</span>
        <strong>{{ unlinkedUserCount }}</strong>
        <small>尚未绑定任何老人的家属账号</small>
      </div>
    </section>

    <a-card class="card-elevated" :bordered="false">
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 1280 }"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '正常' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'elderCount'">
            <a-tag :color="Number(record.elderCount || 0) > 0 ? 'blue' : 'default'">{{ record.elderCount || 0 }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="row-action-links">
              <a-button type="link" size="small" @click="openLinkedElders(record)">查看绑定</a-button>
              <a-button type="link" size="small" @click="openBind(record)">绑定老人</a-button>
              <a-dropdown trigger="click">
                <a-button type="link" size="small">更多 <DownOutlined /></a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item key="edit" @click="openUserEditor(record)">编辑账号</a-menu-item>
                    <a-menu-item key="communication" @click="go('/oa/work-execution/family-communication')">沟通处理</a-menu-item>
                    <a-menu-item key="recharge" @click="go('/oa/family-recharge')">充值台账</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
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

    <a-modal
      v-model:open="bindOpen"
      title="绑定老人"
      width="420px"
      @ok="submitBind"
      @cancel="() => (bindOpen = false)"
    >
      <a-form ref="bindFormRef" :model="bindForm" :rules="bindRules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select
            v-model:value="bindForm.elderId"
            allow-clear
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="选择老人"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="关系" name="relation">
          <a-input v-model:value="bindForm.relation" placeholder="如 子女/配偶" />
        </a-form-item>
        <a-form-item label="主联系人">
          <a-switch v-model:checked="bindForm.isPrimary" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="userEditorOpen"
      :title="userForm.id ? '编辑家属账号' : '新增家属账号'"
      width="460px"
      :confirm-loading="submittingUser"
      @ok="submitUser"
      @cancel="() => (userEditorOpen = false)"
    >
      <a-form ref="userFormRef" :model="userForm" :rules="userRules" layout="vertical">
        <a-form-item label="姓名" name="realName">
          <a-input v-model:value="userForm.realName" placeholder="请输入家属姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="userForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="身份证号" name="idCardNo">
          <a-input v-model:value="userForm.idCardNo" placeholder="请输入身份证号" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="userForm.status">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="linkedEldersOpen" title="已绑定老人" width="560" placement="right">
      <div class="linked-toolbar">
        <a-space>
          <strong>{{ linkedFamilyName }}</strong>
          <a-tag color="blue">{{ linkedElders.length }} 位老人</a-tag>
        </a-space>
      </div>
      <a-empty v-if="!linkedElders.length" description="当前家属还没有绑定老人" />
      <div v-else class="linked-elder-list">
        <div v-for="item in linkedElders" :key="`${linkedFamilyId}-${item.elderId}`" class="linked-elder-card">
          <div class="linked-elder-head">
            <div>
              <strong>{{ item.elderName || `老人 #${item.elderId}` }}</strong>
              <div class="linked-meta">
                <span>{{ elderStatusText(item.elderStatus) }}</span>
                <span>{{ item.careLevel || '护理等级待补充' }}</span>
                <span>{{ item.relation || '关系待补充' }}</span>
                <span>{{ item.admissionDate || '入院日期待补充' }}</span>
              </div>
            </div>
            <a-tag :color="Number(item.isPrimary) === 1 ? 'green' : 'default'">{{ Number(item.isPrimary) === 1 ? '主联系人' : '普通联系人' }}</a-tag>
          </div>
          <div class="linked-actions">
            <a-button size="small" @click="go(`/elder/detail/${item.elderId}?tab=family`)">老人详情</a-button>
            <a-button size="small" @click="go('/oa/work-execution/family-communication')">沟通</a-button>
            <a-button size="small" danger @click="unbindFromLinked(item)">解除绑定</a-button>
          </div>
        </div>
      </div>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import { DownOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { getFamilyLinkedElders, getFamilyRelations, getFamilyUserPage, removeFamilyRelation, upsertFamilyUser } from '../../api/family'
import { bindFamily } from '../../api/elder'
import type { FamilyBindRequest, FamilyElderItem, FamilyRelationItem, FamilyUserItem, Id, PageResult } from '../../types/api'
import { residentStatusText as elderStatusText } from '../../utils/elderStatus'

const router = useRouter()
const loading = ref(false)
const rows = ref<FamilyUserItem[]>([])
const total = ref(0)

const query = reactive({
  realName: '',
  phone: '',
  elderName: '',
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 160, sorter: true },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 160, sorter: true },
  { title: '身份证', dataIndex: 'idCardNo', key: 'idCardNo', width: 180 },
  { title: '关联人数', dataIndex: 'elderCount', key: 'elderCount', width: 120, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, sorter: true },
  { title: '操作', key: 'action', width: 420, fixed: 'right' as const }
]

const bindOpen = ref(false)
const bindFormRef = ref<FormInstance>()
const bindForm = reactive<FamilyBindRequest>({ familyUserId: '' as Id, elderId: '' as Id, relation: '', isPrimary: false })
const bindRules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  relation: [{ required: true, message: '请输入关系' }]
}
const { elderOptions, elderLoading, searchElders: searchElderOptions } = useElderOptions({ pageSize: 200 })

const userEditorOpen = ref(false)
const userFormRef = ref<FormInstance>()
const submittingUser = ref(false)
const userForm = reactive({
  id: '' as Id | '',
  realName: '',
  phone: '',
  idCardNo: '',
  status: 1
})
const userRules: FormRules = {
  realName: [{ required: true, message: '请输入家属姓名' }],
  phone: [{ required: true, message: '请输入手机号' }],
  idCardNo: [{ required: true, message: '请输入身份证号' }]
}

const linkedEldersOpen = ref(false)
const linkedFamilyId = ref<Id | ''>('')
const linkedFamilyName = ref('')
const linkedElders = ref<FamilyElderItem[]>([])

const activeUserCount = computed(() => rows.value.filter((item) => item.status === 1).length)
const totalLinkedElders = computed(() => rows.value.reduce((sum, item) => sum + Number(item.elderCount || 0), 0))
const unlinkedUserCount = computed(() => rows.value.filter((item) => Number(item.elderCount || 0) === 0).length)

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<FamilyUserItem> = await getFamilyUserPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      realName: query.realName,
      phone: query.phone,
      elderName: query.elderName,
      status: query.status,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    rows.value = res.list || []
    total.value = res.total || 0
  } catch (error: any) {
    rows.value = []
    total.value = 0
    message.error(error?.message || '家属账号加载失败')
  } finally {
    loading.value = false
  }
}

async function searchElders(val: string) {
  await searchElderOptions(val)
}

function reset() {
  query.realName = ''
  query.phone = ''
  query.elderName = ''
  query.status = undefined
  query.sortBy = undefined
  query.sortOrder = undefined
  query.pageNo = 1
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

function openBind(row: FamilyUserItem) {
  bindForm.familyUserId = row.id
  bindForm.elderId = '' as Id
  bindForm.relation = ''
  bindForm.isPrimary = false
  bindOpen.value = true
}

function openUserEditor(row?: FamilyUserItem) {
  userForm.id = row?.id || ''
  userForm.realName = row?.realName || ''
  userForm.phone = row?.phone || ''
  userForm.idCardNo = row?.idCardNo || ''
  userForm.status = row?.status ?? 1
  userEditorOpen.value = true
}

async function submitUser() {
  if (!userFormRef.value) return
  try {
    await userFormRef.value.validate()
    submittingUser.value = true
    await upsertFamilyUser({
      realName: userForm.realName,
      phone: userForm.phone,
      idCardNo: userForm.idCardNo,
      status: userForm.status
    })
    message.success(userForm.id ? '家属账号已更新' : '家属账号已创建')
    userEditorOpen.value = false
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '保存家属账号失败')
  } finally {
    submittingUser.value = false
  }
}

async function openLinkedElders(row: FamilyUserItem) {
  linkedFamilyId.value = row.id
  linkedFamilyName.value = row.realName || `家属 #${row.id}`
  linkedEldersOpen.value = true
  try {
    linkedElders.value = await getFamilyLinkedElders(row.id)
  } catch {
    linkedElders.value = []
  }
}

async function submitBind() {
  if (!bindFormRef.value) return
  try {
    await bindFormRef.value.validate()
    await bindFamily(bindForm)
    message.success('绑定成功')
    bindOpen.value = false
    if (linkedEldersOpen.value && bindForm.familyUserId === linkedFamilyId.value) {
      linkedElders.value = await getFamilyLinkedElders(bindForm.familyUserId)
    }
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '绑定失败')
  }
}

function unbindFromLinked(item: FamilyElderItem) {
  if (!linkedFamilyId.value || !item.elderId) return
  Modal.confirm({
    title: '确认解除绑定',
    content: `将解除 ${linkedFamilyName.value} 与 ${item.elderName || `老人 #${item.elderId}`} 的关联关系，是否继续？`,
    okText: '确认解除',
    cancelText: '取消',
    async onOk() {
      try {
        const relations: FamilyRelationItem[] = await getFamilyRelations(item.elderId)
        const matched = relations.find((relation) => String(relation.familyUserId) === String(linkedFamilyId.value))
        if (!matched?.id) {
          throw new Error('未找到对应绑定关系')
        }
        await removeFamilyRelation(matched.id)
        message.success('已解除绑定')
        linkedElders.value = await getFamilyLinkedElders(linkedFamilyId.value)
        await fetchData()
      } catch (error: any) {
        message.error(error?.message || '解除绑定失败')
      }
    }
  })
}

function go(path?: string) {
  if (!path) return
  router.push(path)
}

onMounted(async () => {
  await searchElderOptions('')
  await fetchData()
})

useLiveSyncRefresh({
  topics: ['elder', 'system', 'hr'],
  refresh: () => {
    if (loading.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.search-form {
  row-gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin: 16px 0;
}

.summary-card {
  display: grid;
  gap: 10px;
  min-height: 128px;
  padding: 18px;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
}

.summary-card span,
.summary-card small,
.linked-meta {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.summary-card strong {
  color: var(--ink);
  font-size: 30px;
  line-height: 1.1;
}

.linked-toolbar {
  margin-bottom: 12px;
}

.linked-elder-list {
  display: grid;
  gap: 12px;
}

.linked-elder-card {
  display: grid;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--border-soft);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
}

.linked-elder-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.linked-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.linked-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .linked-elder-head {
    flex-direction: column;
  }
}
</style>
