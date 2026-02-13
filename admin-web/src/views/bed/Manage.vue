<template>
  <PageContainer title="资产与床位" subTitle="楼栋/楼层/房间/床位统一维护">
    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="8">
        <a-card class="summary-card" :bordered="false">
          <div class="summary-title">房间总数</div>
          <div class="summary-value">{{ roomStats.total }}</div>
          <div class="summary-sub">可用 {{ roomStats.active }} / 停用 {{ roomStats.inactive }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card class="summary-card" :bordered="false">
          <div class="summary-title">床位总数</div>
          <div class="summary-value">{{ bedStats.total }}</div>
          <div class="summary-sub">
            空床 {{ bedStats.available }} / 入住 {{ bedStats.occupied }} / 维护 {{ bedStats.maintenance }}
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card class="summary-card" :bordered="false">
          <div class="summary-title">当前模块</div>
          <div class="summary-value">{{ activeTabLabel }}</div>
          <div class="summary-sub">已选床位 {{ selected.length }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :xs="24" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <div class="tree-header">
            <div class="tree-title">资产树</div>
            <a-button size="small" @click="refreshTree">刷新</a-button>
          </div>
          <a-tree
            :tree-data="assetTree"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            :loading="treeLoading"
            show-line
            @select="(_, info) => onTreeSelect(info.node)"
          >
            <template #title="node">
              <span class="tree-node">
                <span class="tree-node-type">{{ treeTypeLabel(node.type || node.dataRef?.type) }}</span>
                <span class="tree-node-name">{{ node.name || node.dataRef?.name }}</span>
              </span>
            </template>
          </a-tree>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="18">
        <a-card class="card-elevated" :bordered="false">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="buildings" tab="楼栋管理">
              <a-form :model="buildingQuery" layout="inline" class="search-bar">
                <a-form-item label="关键字">
                  <a-input v-model:value="buildingQuery.keyword" placeholder="楼栋名称/编码" allow-clear />
                </a-form-item>
                <a-form-item label="状态">
                  <a-select v-model:value="buildingQuery.status" allow-clear style="min-width: 140px">
                    <a-select-option :value="1">启用</a-select-option>
                    <a-select-option :value="0">停用</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="searchBuildings">查询</a-button>
                    <a-button @click="resetBuildings">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <div class="table-actions">
                <a-space>
                  <a-button type="primary" @click="openBuilding()">新增楼栋</a-button>
                  <a-button @click="refreshBuildings">刷新</a-button>
                </a-space>
              </div>

              <a-table
                :data-source="buildings"
                :columns="buildingColumns"
                :loading="loadingBuildings"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="record.status === 1 ? 'green' : 'default'">
                      {{ record.status === 1 ? '启用' : '停用' }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button type="link" @click="openBuilding(record)">编辑</a-button>
                      <a-button type="link" danger @click="removeBuilding(record.id)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
              <div class="pager">
                <a-pagination
                  :current="buildingPage.pageNo"
                  :page-size="buildingPage.pageSize"
                  :total="buildingPage.total"
                  show-size-changer
                  @change="onBuildingPageChange"
                  @showSizeChange="onBuildingPageSizeChange"
                />
              </div>
            </a-tab-pane>

            <a-tab-pane key="floors" tab="楼层管理">
              <a-form :model="floorQuery" layout="inline" class="search-bar">
                <a-form-item label="楼栋">
                  <a-select v-model:value="floorQuery.buildingId" allow-clear style="min-width: 160px">
                    <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="关键字">
                  <a-input v-model:value="floorQuery.keyword" placeholder="楼层/名称" allow-clear />
                </a-form-item>
                <a-form-item label="状态">
                  <a-select v-model:value="floorQuery.status" allow-clear style="min-width: 140px">
                    <a-select-option :value="1">启用</a-select-option>
                    <a-select-option :value="0">停用</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="searchFloors">查询</a-button>
                    <a-button @click="resetFloors">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <div class="table-actions">
                <a-space>
                  <a-button type="primary" @click="openFloor()">新增楼层</a-button>
                  <a-button @click="refreshFloors">刷新</a-button>
                </a-space>
              </div>

              <a-table
                :data-source="floors"
                :columns="floorColumns"
                :loading="loadingFloors"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'buildingId'">
                    {{ buildingNameMap[record.buildingId] || '-' }}
                  </template>
                  <template v-else-if="column.key === 'status'">
                    <a-tag :color="record.status === 1 ? 'green' : 'default'">
                      {{ record.status === 1 ? '启用' : '停用' }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button type="link" @click="openFloor(record)">编辑</a-button>
                      <a-button type="link" danger @click="removeFloor(record.id)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
              <div class="pager">
                <a-pagination
                  :current="floorPage.pageNo"
                  :page-size="floorPage.pageSize"
                  :total="floorPage.total"
                  show-size-changer
                  @change="onFloorPageChange"
                  @showSizeChange="onFloorPageSizeChange"
                />
              </div>
            </a-tab-pane>

            <a-tab-pane key="rooms" tab="房间管理">
              <a-form :model="roomQuery" layout="inline" class="search-bar">
                <a-form-item label="房间号">
                  <a-input v-model:value="roomQuery.roomNo" placeholder="如 A101" allow-clear />
                </a-form-item>
                <a-form-item label="楼栋">
                  <a-select v-model:value="roomQuery.buildingId" allow-clear style="min-width: 140px">
                    <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="楼层">
                  <a-select v-model:value="roomQuery.floorId" allow-clear style="min-width: 140px">
                    <a-select-option v-for="f in floorList" :key="f.id" :value="f.id">{{ f.floorNo }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="状态">
                  <a-select v-model:value="roomQuery.status" allow-clear style="min-width: 140px">
                    <a-select-option :value="1">可用</a-select-option>
                    <a-select-option :value="0">停用</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="searchRooms">查询</a-button>
                    <a-button @click="resetRooms">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <div class="table-actions">
                <a-space>
                  <a-button type="primary" @click="openRoom()">新增房间</a-button>
                  <a-button @click="refreshRooms">刷新</a-button>
                </a-space>
              </div>

              <a-table
                :data-source="rooms"
                :columns="roomColumns"
                :loading="loadingRooms"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
                @change="onRoomTableChange"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="record.status === 1 ? 'green' : 'default'">
                      {{ record.status === 1 ? '可用' : '停用' }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button type="link" @click="openRoom(record)">编辑</a-button>
                      <a-button type="link" @click="toggleRoomStatus(record)">{{ record.status === 1 ? '停用' : '启用' }}</a-button>
                      <a-button type="link" danger @click="removeRoom(record.id)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
              <div class="pager">
                <a-pagination
                  :current="roomPage.pageNo"
                  :page-size="roomPage.pageSize"
                  :total="roomPage.total"
                  show-size-changer
                  @change="onRoomPageChange"
                  @showSizeChange="onRoomPageSizeChange"
                />
              </div>
            </a-tab-pane>

            <a-tab-pane key="beds" tab="床位管理">
              <a-form :model="bedQuery" layout="inline" class="search-bar">
                <a-form-item label="床位号">
                  <a-input v-model:value="bedQuery.bedNo" placeholder="如 01" allow-clear />
                </a-form-item>
                <a-form-item label="房间号">
                  <a-input v-model:value="bedQuery.roomNo" placeholder="如 A101" allow-clear />
                </a-form-item>
                <a-form-item label="老人">
                  <a-input v-model:value="bedQuery.elderName" placeholder="姓名" allow-clear />
                </a-form-item>
                <a-form-item label="状态">
                  <a-select v-model:value="bedQuery.status" allow-clear style="min-width: 140px">
                    <a-select-option :value="1">空床</a-select-option>
                    <a-select-option :value="2">入住</a-select-option>
                    <a-select-option :value="3">维护</a-select-option>
                    <a-select-option :value="0">停用</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="searchBeds">查询</a-button>
                    <a-button @click="resetBeds">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-form>

              <div class="table-actions">
                <a-space>
                  <a-button type="primary" @click="openBed()">新增床位</a-button>
                  <a-button @click="batchQr">批量二维码</a-button>
                  <a-button @click="exportBedCsv">导出CSV</a-button>
                  <a-button @click="refreshBeds">刷新</a-button>
                </a-space>
                <div class="selection-info">已选 {{ selected.length }} 个床位</div>
              </div>

              <a-table
                :data-source="beds"
                :columns="bedColumns"
                :loading="loadingBeds"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
                :row-selection="bedRowSelection"
                @change="onBedTableChange"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="statusTag(record.status)">{{ statusLabel(record.status) }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button type="link" @click="openBed(record)">编辑</a-button>
                      <a-button type="link" @click="printSingle(record)">二维码</a-button>
                      <a-button type="link" @click="toggleBedStatus(record)">{{ record.status === 1 ? '停用' : '启用' }}</a-button>
                      <a-button type="link" danger @click="removeBed(record.id)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
              <div class="pager">
                <a-pagination
                  :current="bedPage.pageNo"
                  :page-size="bedPage.pageSize"
                  :total="bedPage.total"
                  show-size-changer
                  @change="onBedPageChange"
                  @showSizeChange="onBedPageSizeChange"
                />
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <a-modal v-model:open="buildingOpen" title="楼栋信息" width="420px" :confirm-loading="buildingSubmitting" @ok="submitBuilding" @cancel="() => (buildingOpen = false)">
      <a-form ref="buildingFormRef" :model="buildingForm" :rules="buildingRules" layout="vertical">
        <a-form-item label="楼栋名称" name="name">
          <a-input v-model:value="buildingForm.name" placeholder="如 1 号楼" />
        </a-form-item>
        <a-form-item label="编码" name="code">
          <a-input v-model:value="buildingForm.code" placeholder="如 B1" />
        </a-form-item>
        <a-form-item label="排序" name="sortNo">
          <a-input-number v-model:value="buildingForm.sortNo" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="buildingForm.status" placeholder="请选择">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="buildingForm.remark" placeholder="可选" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="floorOpen" title="楼层信息" width="420px" :confirm-loading="floorSubmitting" @ok="submitFloor" @cancel="() => (floorOpen = false)">
      <a-form ref="floorFormRef" :model="floorForm" :rules="floorRules" layout="vertical">
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="floorForm.buildingId" placeholder="请选择">
            <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层编号" name="floorNo">
          <a-input v-model:value="floorForm.floorNo" placeholder="如 3" />
        </a-form-item>
        <a-form-item label="名称" name="name">
          <a-input v-model:value="floorForm.name" placeholder="如 三层" />
        </a-form-item>
        <a-form-item label="排序" name="sortNo">
          <a-input-number v-model:value="floorForm.sortNo" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="floorForm.status" placeholder="请选择">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="roomOpen" title="房间信息" width="420px" :confirm-loading="roomSubmitting" @ok="submitRoom" @cancel="() => (roomOpen = false)">
      <a-form ref="roomFormRef" :model="roomForm" :rules="roomRules" layout="vertical">
        <a-form-item label="房间号" name="roomNo">
          <a-input v-model:value="roomForm.roomNo" placeholder="如 A101" />
        </a-form-item>
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="roomForm.buildingId" placeholder="请选择">
            <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="roomForm.floorId" placeholder="请选择">
            <a-select-option v-for="f in floorList" :key="f.id" :value="f.id">{{ f.floorNo }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位容量" name="capacity">
          <a-input-number v-model:value="roomForm.capacity" style="width: 100%" :min="1" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="roomForm.status" placeholder="请选择">
            <a-select-option :value="1">可用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="bedOpen" title="床位信息" width="420px" :confirm-loading="bedSubmitting" @ok="submitBed" @cancel="() => (bedOpen = false)">
      <a-form ref="bedFormRef" :model="bedForm" :rules="bedRules" layout="vertical">
        <a-form-item label="房间" name="roomId">
          <a-select v-model:value="bedForm.roomId" placeholder="请选择">
            <a-select-option v-for="option in roomOptions" :key="option.value" :value="option.value">{{ option.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位号" name="bedNo">
          <a-input v-model:value="bedForm.bedNo" placeholder="如 01" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="bedForm.status" placeholder="请选择">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维护</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="printOpen" title="二维码打印" width="520px" @ok="print" ok-text="打印" @cancel="() => (printOpen = false)">
      <div class="print-grid">
        <div v-for="item in printList" :key="item.id" class="print-item">
          <img :src="item.qr" style="width: 160px; height: 160px" />
          <div class="print-text">{{ item.text }}</div>
        </div>
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import {
  getRoomPage,
  getBedPage,
  getRoomList,
  getBedList,
  createRoom,
  updateRoom,
  deleteRoom,
  createBed,
  updateBed,
  deleteBed,
  getBuildingPage,
  getBuildingList,
  createBuilding,
  updateBuilding,
  deleteBuilding,
  getFloorPage,
  getFloorList,
  createFloor,
  updateFloor,
  deleteFloor,
  getAssetTree
} from '../../api/bed'
import type { BedItem, RoomItem, BuildingItem, FloorItem, AssetTreeNode, PageResult } from '../../types'

const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])
const roomList = ref<RoomItem[]>([])
const bedList = ref<BedItem[]>([])
const buildings = ref<BuildingItem[]>([])
const buildingList = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const floorList = ref<FloorItem[]>([])
const selected = ref<BedItem[]>([])
const selectedRowKeys = ref<(string | number)[]>([])
const loadingRooms = ref(false)
const loadingBeds = ref(false)
const loadingBuildings = ref(false)
const loadingFloors = ref(false)
const activeTab = ref<'buildings' | 'floors' | 'rooms' | 'beds'>('buildings')

const roomOpen = ref(false)
const bedOpen = ref(false)
const buildingOpen = ref(false)
const floorOpen = ref(false)
const roomForm = reactive<Partial<RoomItem>>({ status: 1 })
const bedForm = reactive<Partial<BedItem>>({ status: 1 })
const buildingForm = reactive<Partial<BuildingItem>>({ status: 1, sortNo: 0 })
const floorForm = reactive<Partial<FloorItem>>({ status: 1, sortNo: 0 })
const roomFormRef = ref<FormInstance>()
const bedFormRef = ref<FormInstance>()
const buildingFormRef = ref<FormInstance>()
const floorFormRef = ref<FormInstance>()
const roomSubmitting = ref(false)
const bedSubmitting = ref(false)
const buildingSubmitting = ref(false)
const floorSubmitting = ref(false)

const assetTree = ref<AssetTreeNode[]>([])
const treeLoading = ref(false)

const roomQuery = reactive({
  roomNo: '',
  building: '',
  floorNo: '',
  buildingId: undefined as number | undefined,
  floorId: undefined as number | undefined,
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})
const bedQuery = reactive({
  bedNo: '',
  roomNo: '',
  elderName: '',
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})
const buildingQuery = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const floorQuery = reactive({
  buildingId: undefined as number | undefined,
  keyword: '',
  status: undefined as number | undefined
})

const roomPage = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const bedPage = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const buildingPage = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const floorPage = reactive({ pageNo: 1, pageSize: 10, total: 0 })

const roomOptions = computed(() =>
  roomList.value.map((r) => ({ label: `${r.roomNo}${r.building ? '-' + r.building : ''}`, value: r.id }))
)

const buildingNameMap = computed(() =>
  buildingList.value.reduce((acc, item) => {
    acc[item.id] = item.name
    return acc
  }, {} as Record<number, string>)
)

const roomStats = computed(() => {
  const total = roomList.value.length
  const active = roomList.value.filter((r) => r.status === 1).length
  return { total, active, inactive: total - active }
})

const bedStats = computed(() => {
  const total = bedList.value.length
  const available = bedList.value.filter((b) => b.status === 1).length
  const occupied = bedList.value.filter((b) => b.status === 2).length
  const maintenance = bedList.value.filter((b) => b.status === 3).length
  return { total, available, occupied, maintenance }
})

const activeTabLabel = computed(() => {
  if (activeTab.value === 'buildings') return '楼栋管理'
  if (activeTab.value === 'floors') return '楼层管理'
  if (activeTab.value === 'rooms') return '房间管理'
  return '床位管理'
})

const buildingRules: FormRules = {
  name: [{ required: true, message: '请输入楼栋名称' }]
}

const floorRules: FormRules = {
  buildingId: [{ required: true, message: '请选择楼栋' }],
  floorNo: [{ required: true, message: '请输入楼层编号' }]
}

const roomRules: FormRules = {
  roomNo: [{ required: true, message: '请输入房间号' }],
  buildingId: [{ required: true, message: '请选择楼栋' }],
  floorId: [{ required: true, message: '请选择楼层' }],
  status: [{ required: true, message: '请选择状态' }]
}

const bedRules: FormRules = {
  roomId: [{ required: true, message: '请选择房间' }],
  bedNo: [{ required: true, message: '请输入床位号' }],
  status: [{ required: true, message: '请选择状态' }]
}

const buildingColumns = [
  { title: '楼栋名称', dataIndex: 'name', key: 'name', width: 160 },
  { title: '编码', dataIndex: 'code', key: 'code', width: 120 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
]

const floorColumns = [
  { title: '楼层编号', dataIndex: 'floorNo', key: 'floorNo', width: 120 },
  { title: '楼栋', dataIndex: 'buildingId', key: 'buildingId', width: 160 },
  { title: '名称', dataIndex: 'name', key: 'name', width: 160 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
]

const roomColumns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120, sorter: true },
  { title: '楼栋', dataIndex: 'building', key: 'building', width: 120, sorter: true },
  { title: '楼层', dataIndex: 'floorNo', key: 'floorNo', width: 100, sorter: true },
  { title: '容量', dataIndex: 'capacity', key: 'capacity', width: 100, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120, sorter: true },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
]

const bedColumns = [
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 100, sorter: true },
  { title: '房间', dataIndex: 'roomNo', key: 'roomNo', width: 120, sorter: true },
  { title: '楼栋', dataIndex: 'building', key: 'building', width: 120 },
  { title: '楼层', dataIndex: 'floorNo', key: 'floorNo', width: 100 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140, sorter: true },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120, sorter: true },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const }
]

const bedRowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[], rows: BedItem[]) => {
    selectedRowKeys.value = keys
    selected.value = rows
  }
}))

function statusLabel(status?: number) {
  if (status === 1) return '空床'
  if (status === 2) return '入住'
  if (status === 3) return '维护'
  if (status === 0) return '停用'
  return '-'
}

function statusTag(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  return 'default'
}

function treeTypeLabel(type?: string) {
  if (type === 'BUILDING') return '楼栋'
  if (type === 'FLOOR') return '楼层'
  if (type === 'ROOM') return '房间'
  if (type === 'BED') return '床位'
  return '-'
}

async function searchBuildings() {
  loadingBuildings.value = true
  try {
    const res: PageResult<BuildingItem> = await getBuildingPage({
      pageNo: buildingPage.pageNo,
      pageSize: buildingPage.pageSize,
      keyword: buildingQuery.keyword,
      status: buildingQuery.status
    })
    buildings.value = res.list
    buildingPage.total = res.total
  } finally {
    loadingBuildings.value = false
  }
}

async function searchFloors() {
  loadingFloors.value = true
  try {
    const res: PageResult<FloorItem> = await getFloorPage({
      pageNo: floorPage.pageNo,
      pageSize: floorPage.pageSize,
      buildingId: floorQuery.buildingId,
      keyword: floorQuery.keyword,
      status: floorQuery.status
    })
    floors.value = res.list
    floorPage.total = res.total
  } finally {
    loadingFloors.value = false
  }
}

async function searchRooms() {
  loadingRooms.value = true
  try {
    const res: PageResult<RoomItem> = await getRoomPage({
      pageNo: roomPage.pageNo,
      pageSize: roomPage.pageSize,
      roomNo: roomQuery.roomNo,
      building: roomQuery.building,
      floorNo: roomQuery.floorNo,
      buildingId: roomQuery.buildingId,
      floorId: roomQuery.floorId,
      status: roomQuery.status,
      sortBy: roomQuery.sortBy,
      sortOrder: roomQuery.sortOrder
    })
    rooms.value = res.list
    roomPage.total = res.total
  } finally {
    loadingRooms.value = false
  }
}

async function searchBeds() {
  loadingBeds.value = true
  try {
    const res: PageResult<BedItem> = await getBedPage({
      pageNo: bedPage.pageNo,
      pageSize: bedPage.pageSize,
      bedNo: bedQuery.bedNo,
      roomNo: bedQuery.roomNo,
      elderName: bedQuery.elderName,
      status: bedQuery.status,
      sortBy: bedQuery.sortBy,
      sortOrder: bedQuery.sortOrder
    })
    beds.value = res.list
    bedPage.total = res.total
  } finally {
    loadingBeds.value = false
  }
}

async function refreshTree() {
  treeLoading.value = true
  try {
    assetTree.value = await getAssetTree()
  } finally {
    treeLoading.value = false
  }
}

function onTreeSelect(node: any) {
  const data = node?.dataRef || node
  if (data.type === 'BUILDING') {
    roomQuery.buildingId = data.id
    roomQuery.floorId = undefined
    activeTab.value = 'rooms'
    searchRooms()
    return
  }
  if (data.type === 'FLOOR') {
    roomQuery.floorId = data.id
    activeTab.value = 'rooms'
    searchRooms()
    return
  }
  if (data.type === 'ROOM') {
    bedQuery.roomNo = data.roomNo || data.name
    activeTab.value = 'beds'
    searchBeds()
    return
  }
  if (data.type === 'BED') {
    bedQuery.bedNo = data.bedNo || data.name
    activeTab.value = 'beds'
    searchBeds()
  }
}

async function refreshBuildings() {
  await searchBuildings()
  await refreshTree()
}

async function refreshFloors() {
  await searchFloors()
  await refreshTree()
}

async function refreshRooms() {
  await searchRooms()
  await refreshTree()
}

async function refreshBeds() {
  await searchBeds()
  await refreshTree()
}

function resetBuildings() {
  buildingQuery.keyword = ''
  buildingQuery.status = undefined
  buildingPage.pageNo = 1
  searchBuildings()
}

function resetFloors() {
  floorQuery.keyword = ''
  floorQuery.status = undefined
  floorQuery.buildingId = undefined
  floorPage.pageNo = 1
  searchFloors()
}

function resetRooms() {
  roomQuery.roomNo = ''
  roomQuery.building = ''
  roomQuery.floorNo = ''
  roomQuery.buildingId = undefined
  roomQuery.floorId = undefined
  roomQuery.status = undefined
  roomQuery.sortBy = undefined
  roomQuery.sortOrder = undefined
  roomPage.pageNo = 1
  searchRooms()
}

function resetBeds() {
  bedQuery.bedNo = ''
  bedQuery.roomNo = ''
  bedQuery.elderName = ''
  bedQuery.status = undefined
  bedQuery.sortBy = undefined
  bedQuery.sortOrder = undefined
  bedPage.pageNo = 1
  searchBeds()
}

function onBuildingPageChange(page: number) {
  buildingPage.pageNo = page
  searchBuildings()
}

function onBuildingPageSizeChange(current: number, size: number) {
  buildingPage.pageNo = 1
  buildingPage.pageSize = size
  searchBuildings()
}

function onFloorPageChange(page: number) {
  floorPage.pageNo = page
  searchFloors()
}

function onFloorPageSizeChange(current: number, size: number) {
  floorPage.pageNo = 1
  floorPage.pageSize = size
  searchFloors()
}

function onRoomTableChange(_: any, __: any, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  roomQuery.sortBy = sorter?.field || undefined
  roomQuery.sortOrder = sorter?.order === 'ascend' ? 'asc' : sorter?.order === 'descend' ? 'desc' : undefined
  searchRooms()
}

function onBedTableChange(_: any, __: any, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  bedQuery.sortBy = sorter?.field || undefined
  bedQuery.sortOrder = sorter?.order === 'ascend' ? 'asc' : sorter?.order === 'descend' ? 'desc' : undefined
  searchBeds()
}

function onRoomPageChange(page: number) {
  roomPage.pageNo = page
  searchRooms()
}

function onRoomPageSizeChange(current: number, size: number) {
  roomPage.pageNo = 1
  roomPage.pageSize = size
  searchRooms()
}

function onBedPageChange(page: number) {
  bedPage.pageNo = page
  searchBeds()
}

function onBedPageSizeChange(current: number, size: number) {
  bedPage.pageNo = 1
  bedPage.pageSize = size
  searchBeds()
}

function openBuilding(row?: BuildingItem) {
  if (row) {
    Object.assign(buildingForm, row)
  } else {
    Object.assign(buildingForm, { id: undefined, name: '', code: '', status: 1, sortNo: 0, remark: '' })
  }
  buildingOpen.value = true
}

async function submitBuilding() {
  if (!buildingFormRef.value) return
  try {
    await buildingFormRef.value.validate()
    buildingSubmitting.value = true
    if (buildingForm.id) {
      await updateBuilding(buildingForm.id, buildingForm)
    } else {
      await createBuilding(buildingForm)
    }
    message.success('保存成功')
    buildingOpen.value = false
    await refreshBuildings()
    await loadBuildingList()
  } catch {
    message.error('保存失败')
  } finally {
    buildingSubmitting.value = false
  }
}

function openFloor(row?: FloorItem) {
  if (row) {
    Object.assign(floorForm, row)
  } else {
    Object.assign(floorForm, { id: undefined, buildingId: undefined, floorNo: '', name: '', status: 1, sortNo: 0 })
  }
  floorOpen.value = true
}

async function submitFloor() {
  if (!floorFormRef.value) return
  try {
    await floorFormRef.value.validate()
    floorSubmitting.value = true
    if (floorForm.id) {
      await updateFloor(floorForm.id, floorForm)
    } else {
      await createFloor(floorForm)
    }
    message.success('保存成功')
    floorOpen.value = false
    await refreshFloors()
    await loadFloorList()
  } catch {
    message.error('保存失败')
  } finally {
    floorSubmitting.value = false
  }
}

function openRoom(row?: RoomItem) {
  if (row) {
    Object.assign(roomForm, row)
  } else {
    Object.assign(roomForm, { id: undefined, roomNo: '', buildingId: undefined, floorId: undefined, capacity: 1, status: 1 })
  }
  roomOpen.value = true
}

async function submitRoom() {
  if (!roomFormRef.value) return
  try {
    await roomFormRef.value.validate()
    roomSubmitting.value = true
    if (roomForm.id) {
      await updateRoom(roomForm.id, roomForm)
    } else {
      await createRoom(roomForm)
    }
    message.success('保存成功')
    roomOpen.value = false
    await refreshRooms()
    await loadRoomList()
  } catch {
    message.error('保存失败')
  } finally {
    roomSubmitting.value = false
  }
}

function openBed(row?: BedItem) {
  if (row) {
    Object.assign(bedForm, row)
  } else {
    Object.assign(bedForm, { id: undefined, roomId: undefined, bedNo: '', status: 1 })
  }
  bedOpen.value = true
}

async function submitBed() {
  if (!bedFormRef.value) return
  try {
    await bedFormRef.value.validate()
    bedSubmitting.value = true
    if (bedForm.id) {
      await updateBed(bedForm.id, bedForm)
    } else {
      await createBed(bedForm)
    }
    message.success('保存成功')
    bedOpen.value = false
    await refreshBeds()
    await loadBedList()
  } catch {
    message.error('保存失败')
  } finally {
    bedSubmitting.value = false
  }
}

async function toggleRoomStatus(row: RoomItem) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateRoom(row.id, { ...row, status: nextStatus })
  await refreshRooms()
  await loadRoomList()
}

async function toggleBedStatus(row: BedItem) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateBed(row.id, { ...row, status: nextStatus })
  await refreshBeds()
  await loadBedList()
}

function confirmAction(content: string, title = '提示') {
  return new Promise<void>((resolve, reject) => {
    Modal.confirm({
      title,
      content,
      onOk: () => resolve(),
      onCancel: () => reject()
    })
  })
}

async function removeBuilding(id: number) {
  try {
    await confirmAction('确认删除该楼栋吗？', '提示')
    await deleteBuilding(id)
    message.success('已删除')
    await refreshBuildings()
    await loadBuildingList()
  } catch {
    // ignore
  }
}

async function removeFloor(id: number) {
  try {
    await confirmAction('确认删除该楼层吗？', '提示')
    await deleteFloor(id)
    message.success('已删除')
    await refreshFloors()
    await loadFloorList()
  } catch {
    // ignore
  }
}

async function removeRoom(id: number) {
  try {
    await confirmAction('确认删除该房间吗？', '提示')
    await deleteRoom(id)
    message.success('已删除')
    await refreshRooms()
    await loadRoomList()
  } catch {
    // ignore
  }
}

async function removeBed(id: number) {
  try {
    await confirmAction('确认删除该床位吗？', '提示')
    await deleteBed(id)
    message.success('已删除')
    await refreshBeds()
    await loadBedList()
  } catch {
    // ignore
  }
}

const printOpen = ref(false)
const printList = ref<{ id: number; qr: string; text: string }[]>([])

async function printSingle(row: BedItem) {
  const text = `BED:${row.id}`
  const qr = await QRCode.toDataURL(text)
  printList.value = [{ id: row.id, qr, text }]
  printOpen.value = true
}

async function batchQr() {
  if (!selected.value.length) {
    message.warning('请先选择床位')
    return
  }
  printList.value = await Promise.all(
    selected.value.map(async (row) => ({
      id: row.id,
      text: `BED:${row.id}`,
      qr: await QRCode.toDataURL(`BED:${row.id}`)
    }))
  )
  printOpen.value = true
}

function print() {
  const win = window.open('', '_blank')
  if (!win) return
  const html = printList.value
    .map(
      (item) =>
        `<div style="display:inline-block;width:220px;text-align:center;margin:8px;">
           <img src="${item.qr}" style="width:180px;height:180px"/>
           <div style="font-size:12px;margin-top:6px;">${item.text}</div>
         </div>`
    )
    .join('')
  win.document.write(html)
  win.print()
  win.close()
}

function exportBedCsv() {
  exportCsv(
    beds.value.map((b) => ({
      床位号: b.bedNo,
      房间: b.roomNo || '',
      老人: b.elderName || '',
      护理等级: b.careLevel || '',
      状态: statusLabel(b.status)
    })),
    'bed-list.csv'
  )
}

async function loadRoomList() {
  roomList.value = await getRoomList()
}

async function loadBedList() {
  bedList.value = await getBedList()
}

async function loadBuildingList() {
  buildingList.value = await getBuildingList()
}

async function loadFloorList() {
  floorList.value = await getFloorList()
}

onMounted(async () => {
  await refreshTree()
  await searchBuildings()
  await searchFloors()
  await searchRooms()
  await searchBeds()
  await loadRoomList()
  await loadBedList()
  await loadBuildingList()
  await loadFloorList()
})
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}
.summary-card {
  padding: 16px;
}
.summary-title {
  color: var(--muted);
  font-size: 12px;
}
.summary-value {
  font-size: 24px;
  margin: 6px 0;
}
.summary-sub {
  font-size: 12px;
  color: var(--muted);
}
.search-bar {
  margin-bottom: 12px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.selection-info {
  color: var(--muted);
}
.pager {
  margin-top: 12px;
  text-align: right;
}
.print-grid {
  display: flex;
  flex-wrap: wrap;
}
.print-item {
  width: 200px;
  text-align: center;
  margin: 8px;
}
.print-text {
  font-size: 12px;
  margin-top: 6px;
}
.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.tree-title {
  font-weight: 600;
}
.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}
.tree-node-type {
  font-size: 12px;
  color: var(--muted);
}
.tree-node-name {
  font-weight: 500;
}
</style>
