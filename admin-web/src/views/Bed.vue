<template>
  <PageContainer title="房态床位" subTitle="房间与床位管理">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="12">
        <SearchForm :model="roomQuery" @search="onRoomSearch" @reset="onRoomSearch">
          <a-form-item label="房间检索">
            <a-input v-model:value="roomQuery.keyword" placeholder="房间号/楼栋/楼层" />
          </a-form-item>
          <template #extra>
            <a-button type="primary" @click="openRoomModal()">新增房间</a-button>
          </template>
        </SearchForm>
        <DataTable
          rowKey="id"
          :columns="roomColumns"
          :data-source="rooms"
          :loading="loadingRooms"
          :pagination="roomPagination"
          @change="onRoomPage"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="openRoomModal(record)">编辑</a>
                <a @click="removeRoom(record.id)">删除</a>
              </a-space>
            </template>
          </template>
        </DataTable>
      </a-col>

      <a-col :xs="24" :lg="12">
        <SearchForm :model="bedQuery" @search="onBedSearch" @reset="onBedSearch">
          <a-form-item label="床位检索">
            <a-input v-model:value="bedQuery.keyword" placeholder="床位号/二维码" />
          </a-form-item>
          <template #extra>
            <a-button type="primary" @click="openBedModal()">新增床位</a-button>
          </template>
        </SearchForm>
        <DataTable
          rowKey="id"
          :columns="bedColumns"
          :data-source="beds"
          :loading="loadingBeds"
          :pagination="bedPagination"
          @change="onBedPage"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'default'">
                {{ record.status === 1 ? '可用' : '停用' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a @click="openBedModal(record)">编辑</a>
                <a @click="removeBed(record.id)">删除</a>
              </a-space>
            </template>
          </template>
        </DataTable>
      </a-col>
    </a-row>

    <a-card class="card-elevated" title="房态图" :bordered="false" style="margin-top: 16px">
      <div class="bed-grid">
        <div v-for="bed in bedAll" :key="bed.id" class="bed-card">
          <div class="bed-header">
            <span>{{ bed.bedNo }}</span>
            <a-tag :color="bed.elderName ? 'blue' : 'green'">{{ bed.elderName ? '已入住' : '空闲' }}</a-tag>
          </div>
          <div class="bed-meta">房间 {{ bed.roomNo || bed.roomId }}</div>
          <div class="bed-meta">老人 {{ bed.elderName || '-' }}</div>
          <div class="bed-meta">二维码 {{ bed.bedQrCode || '-' }}</div>
        </div>
      </div>
    </a-card>

    <a-modal v-model:open="roomModalOpen" title="房间信息" @ok="submitRoom">
      <a-form layout="vertical">
        <a-form-item label="楼栋">
          <a-input v-model:value="roomForm.building" />
        </a-form-item>
        <a-form-item label="楼层">
          <a-input v-model:value="roomForm.floorNo" />
        </a-form-item>
        <a-form-item label="房间号" required>
          <a-input v-model:value="roomForm.roomNo" />
        </a-form-item>
        <a-form-item label="床位容量">
          <a-input-number v-model:value="roomForm.capacity" style="width:100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="roomForm.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="bedModalOpen" title="床位信息" @ok="submitBed">
      <a-form layout="vertical">
        <a-form-item label="房间" required>
          <a-select v-model:value="bedForm.roomId" :options="roomOptions" />
        </a-form-item>
        <a-form-item label="床位号" required>
          <a-input v-model:value="bedForm.bedNo" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="bedForm.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getRoomPage, getBedPage, getRoomList, getBedList, createRoom, updateRoom, deleteRoom, createBed, updateBed, deleteBed } from '../api/bed'
import type { RoomItem, BedItem, PageResult } from '../types/api'

const rooms = ref<RoomItem[]>([])
const roomAll = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])
const bedAll = ref<BedItem[]>([])
const roomQuery = reactive({ keyword: '', pageNo: 1, pageSize: 5 })
const bedQuery = reactive({ keyword: '', pageNo: 1, pageSize: 5 })
const roomPagination = reactive({ current: 1, pageSize: 5, total: 0, showSizeChanger: true })
const bedPagination = reactive({ current: 1, pageSize: 5, total: 0, showSizeChanger: true })
const roomModalOpen = ref(false)
const bedModalOpen = ref(false)
const roomForm = reactive<Partial<RoomItem>>({})
const bedForm = reactive<Partial<BedItem>>({})
const loadingRooms = ref(false)
const loadingBeds = ref(false)

const statusOptions = [
  { label: '可用', value: 1 },
  { label: '停用', value: 0 }
]
const roomOptions = computed(() => roomAll.value.map((r) => ({ label: r.roomNo, value: r.id })))

const roomColumns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '楼栋', dataIndex: 'building', key: 'building', width: 120 },
  { title: '楼层', dataIndex: 'floorNo', key: 'floorNo', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const bedColumns = [
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 100 },
  { title: '房间', dataIndex: 'roomId', key: 'roomId', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

async function load() {
  loadingRooms.value = true
  loadingBeds.value = true
  try {
    const roomRes: PageResult<RoomItem> = await getRoomPage(roomQuery)
    const bedRes: PageResult<BedItem> = await getBedPage(bedQuery)
    const roomListRes: RoomItem[] = await getRoomList()
    const bedListRes: BedItem[] = await getBedList()
    rooms.value = roomRes.list
    roomPagination.total = roomRes.total || roomRes.list.length
    beds.value = bedRes.list
    bedPagination.total = bedRes.total || bedRes.list.length
    roomAll.value = roomListRes
    bedAll.value = bedListRes
  } catch {
    rooms.value = [
      { id: 10, roomNo: 'A101' },
      { id: 11, roomNo: 'A102' }
    ]
    roomAll.value = rooms.value
    beds.value = [
      { id: 100, bedNo: '01', roomId: 10, status: 1 },
      { id: 101, bedNo: '02', roomId: 11, status: 1 }
    ]
    bedAll.value = beds.value
  } finally {
    loadingRooms.value = false
    loadingBeds.value = false
  }
}

function onRoomPage(pag: any) {
  roomQuery.pageNo = pag.current
  roomQuery.pageSize = pag.pageSize
  roomPagination.current = pag.current
  roomPagination.pageSize = pag.pageSize
  load()
}

function onBedPage(pag: any) {
  bedQuery.pageNo = pag.current
  bedQuery.pageSize = pag.pageSize
  bedPagination.current = pag.current
  bedPagination.pageSize = pag.pageSize
  load()
}

function onRoomSearch() {
  roomQuery.pageNo = 1
  roomPagination.current = 1
  load()
}

function onBedSearch() {
  bedQuery.pageNo = 1
  bedPagination.current = 1
  load()
}

function openRoomModal(record?: RoomItem) {
  Object.assign(roomForm, record || { roomNo: '', status: 1 })
  roomModalOpen.value = true
}

function openBedModal(record?: BedItem) {
  Object.assign(bedForm, record || { roomId: undefined, bedNo: '', status: 1 })
  bedModalOpen.value = true
}

async function submitRoom() {
  try {
    if (roomForm.id) {
      await updateRoom(roomForm.id, roomForm)
    } else {
      await createRoom(roomForm)
    }
    message.success('保存成功')
    roomModalOpen.value = false
    load()
  } catch {
    message.error('保存失败')
  }
}

async function submitBed() {
  try {
    if (bedForm.id) {
      await updateBed(bedForm.id, bedForm)
    } else {
      await createBed(bedForm)
    }
    message.success('保存成功')
    bedModalOpen.value = false
    load()
  } catch {
    message.error('保存失败')
  }
}

async function removeRoom(id: number) {
  try {
    await deleteRoom(id)
    message.success('已删除')
    load()
  } catch {
    message.error('删除失败')
  }
}

async function removeBed(id: number) {
  try {
    await deleteBed(id)
    message.success('已删除')
    load()
  } catch {
    message.error('删除失败')
  }
}

load()
</script>

<style scoped>
.bed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.bed-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
}

.bed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 8px;
}

.bed-meta {
  color: var(--muted);
  font-size: 12px;
}
</style>
