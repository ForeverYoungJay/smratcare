<template>
  <PageContainer title="床位管理" subTitle="楼栋/楼层/房间/床位统一维护（V120联动）">
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
    <div class="entry-actions">
      <a-space wrap>
        <a-button @click="openBedMap">查看床位全景</a-button>
        <a-button type="primary" @click="openElderBedPanorama">打开长者管理床态全景</a-button>
      </a-space>
    </div>

    <a-row :gutter="16">
      <a-col :xs="24" :md="6">
        <a-card class="card-elevated" :bordered="false">
          <div class="tree-header">
            <div class="tree-title">资产树</div>
            <a-button size="small" @click="refreshTree">刷新</a-button>
          </div>
          <a-tree
            :tree-data="treeData"
            :field-names="{ title: 'name', key: 'treeKey', children: 'children' }"
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
                <a-space wrap>
                  <a-button type="primary" @click="openBuilding()">新增楼栋</a-button>
                  <a-button @click="quickGenerateBuildings">一键生成</a-button>
                  <a-button danger @click="quickDeleteBuildings">一键删除</a-button>
                  <a-button type="primary" @click="openBootstrap">一键生成楼层房床</a-button>
                  <a-button @click="refreshBuildings">刷新</a-button>
                </a-space>
                <a-space wrap>
                  <div class="selection-info">已选 {{ selectedBuildings.length }} 个楼栋</div>
                  <a-button :disabled="!selectedBuildings.length" @click="batchUpdateBuildingsStatus(1)">批量启用</a-button>
                  <a-button :disabled="!selectedBuildings.length" @click="batchUpdateBuildingsStatus(0)">批量停用</a-button>
                </a-space>
              </div>

              <a-table
                :data-source="buildings"
                :columns="buildingColumns"
                :loading="loadingBuildings"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
                :row-selection="buildingRowSelection"
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
                <a-space wrap>
                  <a-button type="primary" @click="openFloor()">新增楼层</a-button>
                  <a-button @click="quickGenerateFloors">一键生成</a-button>
                  <a-button danger @click="quickDeleteFloors">一键删除</a-button>
                  <a-button @click="refreshFloors">刷新</a-button>
                </a-space>
                <a-space wrap>
                  <div class="selection-info">已选 {{ selectedFloors.length }} 个楼层</div>
                  <a-button :disabled="!selectedFloors.length" @click="batchUpdateFloorsStatus(1)">批量启用</a-button>
                  <a-button :disabled="!selectedFloors.length" @click="batchUpdateFloorsStatus(0)">批量停用</a-button>
                </a-space>
              </div>

              <a-table
                :data-source="floors"
                :columns="floorColumns"
                :loading="loadingFloors"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
                :row-selection="floorRowSelection"
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
                <a-form-item label="房型">
                  <a-select v-model:value="roomQuery.roomType" allow-clear style="min-width: 160px" placeholder="全部房型">
                    <a-select-option v-for="item in roomTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
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
                <a-space wrap>
                  <a-button type="primary" @click="openRoom()">新增房间</a-button>
                  <a-button @click="quickGenerateRooms">一键生成</a-button>
                  <a-button danger @click="quickDeleteRooms">一键删除</a-button>
                  <a-button @click="refreshRooms">刷新</a-button>
                </a-space>
                <a-space wrap>
                  <div class="selection-info">已选 {{ selectedRooms.length }} 个房间</div>
                  <a-button :disabled="!selectedRooms.length" @click="batchUpdateRoomsStatus(1)">批量启用</a-button>
                  <a-button :disabled="!selectedRooms.length" @click="batchUpdateRoomsStatus(0)">批量停用</a-button>
                </a-space>
              </div>

              <a-table
                :data-source="rooms"
                :columns="roomColumns"
                :loading="loadingRooms"
                :pagination="false"
                row-key="id"
                :scroll="{ y: 420 }"
                :row-selection="roomRowSelection"
                @change="onRoomTableChange"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'roomType'">
                    {{ resolveRoomTypeLabel(record.roomType) }}
                  </template>
                  <template v-else-if="column.key === 'capacity'">
                    <a-tag v-if="resolveCapacityMismatch(record)" color="gold">
                      {{ resolveRoomCapacity(record) }}（已按房型修正）
                    </a-tag>
                    <span v-else>{{ resolveRoomCapacity(record) }}</span>
                  </template>
                  <template v-else-if="column.key === 'status'">
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
                <a-form-item label="房型">
                  <a-select v-model:value="bedQuery.roomType" allow-clear style="min-width: 160px" placeholder="全部房型">
                    <a-select-option v-for="item in roomTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="床型">
                  <a-select v-model:value="bedQuery.bedType" allow-clear style="min-width: 160px" placeholder="全部床型">
                    <a-select-option v-for="item in bedTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
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
                <a-space wrap>
                  <a-button type="primary" @click="openBed()">新增床位</a-button>
                  <a-button @click="quickGenerateBeds">一键生成</a-button>
                  <a-button danger @click="quickDeleteBeds">一键删除</a-button>
                  <a-button @click="batchQr">批量二维码</a-button>
                  <a-button @click="exportBedCsv">导出CSV</a-button>
                  <a-button @click="refreshBeds">刷新</a-button>
                </a-space>
                <a-space wrap>
                  <div class="selection-info">已选 {{ selected.length }} 个床位</div>
                  <a-button :disabled="!selected.length" @click="batchUpdateBedsStatus(1)">批量启用</a-button>
                  <a-button :disabled="!selected.length" @click="batchUpdateBedsStatus(0)">批量停用</a-button>
                </a-space>
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
                  <template v-if="column.key === 'bedType'">
                    {{ resolveBedTypeLabel(record.bedType) }}
                  </template>
                  <template v-else-if="column.key === 'status'">
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
        <a-form-item label="区域" name="areaCode">
          <a-select v-model:value="buildingForm.areaCode" allow-clear placeholder="请选择区域">
            <a-select-option v-for="item in areaOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
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
          <a-select v-model:value="roomForm.buildingId" placeholder="请选择" show-search option-filter-prop="label">
            <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="roomForm.floorId" placeholder="请选择">
            <a-select-option v-for="f in roomFloorOptions" :key="f.id" :value="f.id">{{ f.floorNo }}</a-select-option>
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
        <a-form-item label="床位类型" name="bedType">
          <a-select v-model:value="bedForm.bedType" allow-clear placeholder="请选择">
            <a-select-option v-for="item in bedTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
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

    <a-modal
      v-model:open="bootstrapOpen"
      title="一键生成楼层房床"
      width="520px"
      :confirm-loading="bootstrapSubmitting"
      @ok="submitBootstrap"
      @cancel="() => (bootstrapOpen = false)"
    >
      <a-form :model="bootstrapForm" layout="vertical">
        <div class="bootstrap-presets">
          <a-space wrap>
            <a-button size="small" @click="applyBootstrapPreset('AB_F1_6_R101_130_B01_03')">
              模板：A栋/B栋 1F-6F 101-130 01-03床
            </a-button>
            <a-button size="small" @click="applyBootstrapPreset('CUSTOM')">模板：自定义参数</a-button>
          </a-space>
          <div class="bootstrap-hint" v-if="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'">
            说明：房间号按 A101-A130/A201-A230...、B101-B130/B201-B230... 自动生成，床位号 01-03。
          </div>
        </div>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="楼栋数量">
              <a-input-number v-model:value="bootstrapForm.buildingCount" :min="1" :max="20" style="width: 100%" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="每栋楼层数">
              <a-input-number v-model:value="bootstrapForm.floorsPerBuilding" :min="1" :max="20" style="width: 100%" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="每层房间数">
              <a-input-number v-model:value="bootstrapForm.roomsPerFloor" :min="1" :max="200" style="width: 100%" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="每房床位数">
              <a-input-number v-model:value="bootstrapForm.bedsPerRoom" :min="1" :max="12" style="width: 100%" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="起始编号">
              <a-input-number v-model:value="bootstrapForm.startNo" :min="1" :max="9999" style="width: 100%" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="床位类型">
              <a-select v-model:value="bootstrapForm.bedType" allow-clear placeholder="默认取系统首个启用床型">
                <a-select-option v-for="item in bedTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="楼栋前缀">
              <a-input v-model:value="bootstrapForm.buildingPrefix" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="楼层前缀">
              <a-input v-model:value="bootstrapForm.floorPrefix" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="房间前缀">
              <a-input v-model:value="bootstrapForm.roomPrefix" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="床位前缀">
              <a-input v-model:value="bootstrapForm.bedPrefix" :disabled="bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03'" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
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
  bootstrapResidence,
  getFloorPage,
  getFloorList,
  createFloor,
  updateFloor,
  deleteFloor,
  getAssetTree
} from '../../api/bed'
import type {
  BaseConfigItem,
  BedItem,
  RoomItem,
  BuildingItem,
  FloorItem,
  AssetTreeNode,
  PageResult,
  ResidenceBootstrapRequest,
  Id
} from '../../types'

const router = useRouter()
const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])
const roomList = ref<RoomItem[]>([])
const bedList = ref<BedItem[]>([])
const buildings = ref<BuildingItem[]>([])
const buildingList = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const floorList = ref<FloorItem[]>([])
const selected = ref<BedItem[]>([])
const selectedRowKeys = ref<Id[]>([])
const selectedBuildings = ref<BuildingItem[]>([])
const selectedBuildingRowKeys = ref<Id[]>([])
const selectedFloors = ref<FloorItem[]>([])
const selectedFloorRowKeys = ref<Id[]>([])
const selectedRooms = ref<RoomItem[]>([])
const selectedRoomRowKeys = ref<Id[]>([])
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
const bootstrapOpen = ref(false)
const bootstrapSubmitting = ref(false)

const assetTree = ref<AssetTreeNode[]>([])
const treeLoading = ref(false)
const roomTypeItems = ref<BaseConfigItem[]>([])
const bedTypeItems = ref<BaseConfigItem[]>([])
const areaItems = ref<BaseConfigItem[]>([])
type TreeNode = AssetTreeNode & { treeKey: string; children?: TreeNode[] }

const fallbackRoomTypes: BaseConfigItem[] = [
  { id: -1, configGroup: 'ADMISSION_ROOM_TYPE', itemCode: 'ROOM_SINGLE', itemName: '单人间', status: 1, sortNo: 10 },
  { id: -2, configGroup: 'ADMISSION_ROOM_TYPE', itemCode: 'ROOM_DOUBLE', itemName: '双人间', status: 1, sortNo: 20 },
  { id: -3, configGroup: 'ADMISSION_ROOM_TYPE', itemCode: 'ROOM_TRIPLE', itemName: '三人间', status: 1, sortNo: 30 }
]
const fallbackBedTypes: BaseConfigItem[] = [
  { id: -11, configGroup: 'ADMISSION_BED_TYPE', itemCode: 'BED_STANDARD', itemName: '标准床', status: 1, sortNo: 10 },
  { id: -12, configGroup: 'ADMISSION_BED_TYPE', itemCode: 'BED_CARE', itemName: '护理床', status: 1, sortNo: 20 }
]
const fallbackAreas: BaseConfigItem[] = [
  { id: -21, configGroup: 'ADMISSION_AREA', itemCode: 'AREA_A', itemName: 'A区', status: 1, sortNo: 10 },
  { id: -22, configGroup: 'ADMISSION_AREA', itemCode: 'AREA_B', itemName: 'B区', status: 1, sortNo: 20 }
]

const bootstrapForm = reactive<ResidenceBootstrapRequest>({
  buildingCount: 1,
  floorsPerBuilding: 3,
  roomsPerFloor: 10,
  bedsPerRoom: 2,
  startNo: 1,
  buildingPrefix: '楼栋',
  floorPrefix: 'F',
  roomPrefix: 'R',
  bedPrefix: 'B',
  templateCode: 'CUSTOM'
})

const roomQuery = reactive({
  roomNo: '',
  building: '',
  floorNo: '',
  buildingId: undefined as Id | undefined,
  floorId: undefined as Id | undefined,
  roomType: undefined as string | undefined,
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})
const bedQuery = reactive({
  bedNo: '',
  roomNo: '',
  elderName: '',
  roomType: undefined as string | undefined,
  bedType: undefined as string | undefined,
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})
const buildingQuery = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const floorQuery = reactive({
  buildingId: undefined as Id | undefined,
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
const roomTypeOptions = computed(() => roomTypeItems.value.map((item) => ({ label: item.itemName, value: item.itemCode })))
const bedTypeOptions = computed(() => bedTypeItems.value.map((item) => ({ label: item.itemName, value: item.itemCode })))
const areaOptions = computed(() => areaItems.value.map((item) => ({ label: item.itemName, value: item.itemCode })))
const roomTypeLabelMap = computed(() =>
  roomTypeItems.value.reduce((acc, item) => {
    acc[item.itemCode] = item.itemName
    return acc
  }, {} as Record<string, string>)
)
const bedTypeLabelMap = computed(() =>
  bedTypeItems.value.reduce((acc, item) => {
    acc[item.itemCode] = item.itemName
    return acc
  }, {} as Record<string, string>)
)
const roomFloorOptions = computed(() => {
  if (!roomForm.buildingId) return floorList.value
  return floorList.value.filter((item) => item.buildingId === roomForm.buildingId)
})
const treeData = computed<TreeNode[]>(() => buildTreeKey(assetTree.value, 'ROOT'))
const areaNameByCode = computed(() =>
  areaItems.value.reduce((acc, item) => {
    acc[item.itemCode] = item.itemName
    return acc
  }, {} as Record<string, string>)
)

const buildingNameMap = computed(() =>
  buildingList.value.reduce((acc, item) => {
    acc[item.id] = item.name
    return acc
  }, {} as Record<string, string>)
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
  { title: '区域', dataIndex: 'areaName', key: 'areaName', width: 140 },
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
  { title: '房型', dataIndex: 'roomType', key: 'roomType', width: 120 },
  { title: '容量', dataIndex: 'capacity', key: 'capacity', width: 100, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120, sorter: true },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
]

const bedColumns = [
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 100, sorter: true },
  { title: '床位类型', dataIndex: 'bedType', key: 'bedType', width: 120 },
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
  onChange: (keys: Id[], rows: BedItem[]) => {
    selectedRowKeys.value = keys
    selected.value = rows
  }
}))

const buildingRowSelection = computed(() => ({
  selectedRowKeys: selectedBuildingRowKeys.value,
  onChange: (keys: Id[], rows: BuildingItem[]) => {
    selectedBuildingRowKeys.value = keys
    selectedBuildings.value = rows
  }
}))

const floorRowSelection = computed(() => ({
  selectedRowKeys: selectedFloorRowKeys.value,
  onChange: (keys: Id[], rows: FloorItem[]) => {
    selectedFloorRowKeys.value = keys
    selectedFloors.value = rows
  }
}))

const roomRowSelection = computed(() => ({
  selectedRowKeys: selectedRoomRowKeys.value,
  onChange: (keys: Id[], rows: RoomItem[]) => {
    selectedRoomRowKeys.value = keys
    selectedRooms.value = rows
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

function resolveRoomTypeLabel(roomType?: string) {
  if (!roomType) return '-'
  return roomTypeLabelMap.value[roomType] || roomType
}

function resolveBedTypeLabel(bedType?: string) {
  if (!bedType) return '-'
  return bedTypeLabelMap.value[bedType] || bedType
}

function inferCapacityByRoomType(roomType?: string) {
  const raw = String(roomType || '')
  const normalized = raw.toUpperCase()
  if (!normalized) return undefined
  if (normalized === '1' || normalized.includes('SINGLE') || normalized.includes('ROOM_SINGLE') || raw.includes('单人')) return 1
  if (normalized === '2' || normalized.includes('DOUBLE') || normalized.includes('ROOM_DOUBLE') || raw.includes('双人')) return 2
  if (normalized === '3' || normalized.includes('TRIPLE') || normalized.includes('ROOM_TRIPLE') || raw.includes('三人')) return 3
  const matched = roomTypeItems.value.find((item) => item.itemCode === raw || item.itemName === raw)
  if (matched) {
    const name = String(matched.itemName || '')
    if (name.includes('单人')) return 1
    if (name.includes('双人')) return 2
    if (name.includes('三人')) return 3
  }
  return undefined
}

function resolveRoomCapacity(room: RoomItem) {
  const inferred = inferCapacityByRoomType(room.roomType)
  if (inferred) return inferred
  return Number(room.capacity || 0) || '-'
}

function resolveCapacityMismatch(room: RoomItem) {
  const inferred = inferCapacityByRoomType(room.roomType)
  if (!inferred) return false
  const current = Number(room.capacity || 0)
  return current > 0 && current !== inferred
}

function inferRoomTypeByCapacity(capacity?: number) {
  if (![1, 2, 3].includes(Number(capacity || 0))) return undefined
  const fallbackCode = capacity === 1 ? 'ROOM_SINGLE' : capacity === 2 ? 'ROOM_DOUBLE' : 'ROOM_TRIPLE'
  const preferredName = capacity === 1 ? '单人间' : capacity === 2 ? '双人间' : '三人间'
  const exact = roomTypeItems.value.find((item) => item.itemName === preferredName)
  if (exact) return exact.itemCode
  const fallback = roomTypeItems.value.find((item) => item.itemCode === fallbackCode)
  if (fallback) return fallback.itemCode
  const fuzzy = roomTypeItems.value.find((item) => String(item.itemName || '').includes(preferredName.slice(0, 1)))
  return fuzzy?.itemCode
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
    selectedBuildingRowKeys.value = selectedBuildingRowKeys.value.filter((id) => buildings.value.some((item) => item.id === id))
    selectedBuildings.value = selectedBuildings.value.filter((item) => selectedBuildingRowKeys.value.includes(item.id))
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
    selectedFloorRowKeys.value = selectedFloorRowKeys.value.filter((id) => floors.value.some((item) => item.id === id))
    selectedFloors.value = selectedFloors.value.filter((item) => selectedFloorRowKeys.value.includes(item.id))
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
      roomType: roomQuery.roomType,
      status: roomQuery.status,
      sortBy: roomQuery.sortBy,
      sortOrder: roomQuery.sortOrder
    })
    rooms.value = res.list
    roomPage.total = res.total
    selectedRoomRowKeys.value = selectedRoomRowKeys.value.filter((id) => rooms.value.some((item) => item.id === id))
    selectedRooms.value = selectedRooms.value.filter((item) => selectedRoomRowKeys.value.includes(item.id))
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
      roomType: bedQuery.roomType,
      bedType: bedQuery.bedType,
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
    roomQuery.roomNo = ''
    roomQuery.building = ''
    roomQuery.floorNo = ''
    roomQuery.buildingId = data.id
    roomQuery.floorId = undefined
    bedQuery.bedNo = ''
    bedQuery.roomNo = ''
    activeTab.value = 'rooms'
    searchRooms()
    return
  }
  if (data.type === 'FLOOR') {
    roomQuery.roomNo = ''
    roomQuery.building = ''
    roomQuery.floorNo = ''
    roomQuery.buildingId = data.buildingId
    roomQuery.floorId = data.id
    bedQuery.bedNo = ''
    bedQuery.roomNo = ''
    activeTab.value = 'rooms'
    searchRooms()
    return
  }
  if (data.type === 'ROOM') {
    bedQuery.bedNo = ''
    bedQuery.roomNo = data.roomNo || data.name
    activeTab.value = 'beds'
    searchBeds()
    return
  }
  if (data.type === 'BED') {
    bedQuery.roomNo = ''
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
  roomQuery.roomType = undefined
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
  bedQuery.roomType = undefined
  bedQuery.bedType = undefined
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
    Object.assign(buildingForm, { id: undefined, name: '', code: '', areaCode: undefined, areaName: undefined, status: 1, sortNo: 0, remark: '' })
  }
  buildingOpen.value = true
}

async function submitBuilding() {
  if (!buildingFormRef.value) return
  try {
    await buildingFormRef.value.validate()
    buildingSubmitting.value = true
    const payload = {
      ...buildingForm,
      areaName: buildingForm.areaCode ? areaNameByCode.value[buildingForm.areaCode] : undefined
    }
    if (buildingForm.id) {
      await updateBuilding(buildingForm.id, payload)
    } else {
      await createBuilding(payload)
    }
    message.success('保存成功')
    buildingOpen.value = false
    await refreshBuildings()
    await loadBuildingList()
  } catch (error: any) {
    message.error(errorMessage(error, '保存失败'))
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
  } catch (error: any) {
    message.error(errorMessage(error, '保存失败'))
  } finally {
    floorSubmitting.value = false
  }
}

function openRoom(row?: RoomItem) {
  if (row) {
    Object.assign(roomForm, row)
    const inferred = inferCapacityByRoomType(row.roomType)
    if (inferred && roomForm.capacity !== inferred) {
      roomForm.capacity = inferred
    }
  } else {
    Object.assign(roomForm, { id: undefined, roomNo: '', buildingId: undefined, floorId: undefined, roomType: undefined, capacity: 1, status: 1 })
  }
  roomOpen.value = true
}

async function submitRoom() {
  if (!roomFormRef.value) return
  try {
    await roomFormRef.value.validate()
    const inferredRoomType = inferRoomTypeByCapacity(Number(roomForm.capacity || 0))
    if (inferredRoomType) {
      roomForm.roomType = inferredRoomType
    }
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
  } catch (error: any) {
    message.error(errorMessage(error, '保存失败'))
  } finally {
    roomSubmitting.value = false
  }
}

function openBed(row?: BedItem) {
  if (row) {
    Object.assign(bedForm, row)
  } else {
    Object.assign(bedForm, { id: undefined, roomId: undefined, bedNo: '', bedType: undefined, status: 1 })
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
  } catch (error: any) {
    message.error(errorMessage(error, '保存失败'))
  } finally {
    bedSubmitting.value = false
  }
}

async function toggleRoomStatus(row: RoomItem) {
  try {
    const nextStatus = row.status === 1 ? 0 : 1
    await updateRoom(row.id, { ...row, status: nextStatus })
    await refreshRooms()
    await loadRoomList()
  } catch (error: any) {
    message.warning(errorMessage(error, '更新房间状态失败'))
  }
}

async function toggleBedStatus(row: BedItem) {
  try {
    const nextStatus = row.status === 1 ? 0 : 1
    await updateBed(row.id, { ...row, status: nextStatus })
    await refreshBeds()
    await loadBedList()
  } catch (error: any) {
    message.warning(errorMessage(error, '更新床位状态失败'))
  }
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

function errorMessage(error: any, fallback: string) {
  return error?.message || error?.msg || error?.response?.data?.message || fallback
}

function buildTreeKey(nodes: AssetTreeNode[], parentKey: string): TreeNode[] {
  return nodes.map((node) => {
    const type = node.type || 'NODE'
    const key = `${parentKey}-${type}-${node.id}`
    const children = node.children ? buildTreeKey(node.children, key) : undefined
    return { ...node, treeKey: key, children }
  })
}

function openBootstrap() {
  bootstrapOpen.value = true
}

function applyBootstrapPreset(preset: 'AB_F1_6_R101_130_B01_03' | 'CUSTOM') {
  if (preset === 'AB_F1_6_R101_130_B01_03') {
    Object.assign(bootstrapForm, {
      templateCode: preset,
      buildingCount: 2,
      floorsPerBuilding: 6,
      roomsPerFloor: 30,
      bedsPerRoom: 3,
      startNo: 1,
      buildingPrefix: '楼栋',
      floorPrefix: 'F',
      roomPrefix: 'R',
      bedPrefix: 'B'
    })
    return
  }
  Object.assign(bootstrapForm, {
    templateCode: 'CUSTOM'
  })
}

async function submitBootstrap() {
  try {
    const inferredRoomType = inferRoomTypeByCapacity(Number(bootstrapForm.bedsPerRoom || 0))
    if (inferredRoomType) {
      bootstrapForm.roomType = inferredRoomType
    }
    bootstrapSubmitting.value = true
    const result = await bootstrapResidence(bootstrapForm)
    bootstrapOpen.value = false
    message.success(`已生成 ${result.buildingCount} 栋 / ${result.floorCount} 层 / ${result.roomCount} 间 / ${result.bedCount} 床`)
    await Promise.all([
      refreshTree(),
      searchBuildings(),
      searchFloors(),
      searchRooms(),
      searchBeds(),
      loadRoomList(),
      loadBedList(),
      loadBuildingList(),
      loadFloorList()
    ])
  } catch (error: any) {
    message.error(errorMessage(error, '一键生成失败'))
  } finally {
    bootstrapSubmitting.value = false
  }
}

async function batchUpdateBuildingsStatus(status: 1 | 0) {
  const selectedTargets = selectedBuildings.value
  if (!selectedTargets.length) {
    message.warning('请先勾选楼栋')
    return
  }
  const actionLabel = status === 1 ? '启用' : '停用'
  const targets = selectedTargets.filter((item) => Number(item.status ?? 0) !== status)
  if (!targets.length) {
    message.info(`已选楼栋都已是${actionLabel}状态`)
    return
  }
  try {
    const skipped = selectedTargets.length - targets.length
    await confirmAction(
      skipped > 0
        ? `确认批量${actionLabel} ${targets.length} 个楼栋吗？（${skipped} 个状态一致已跳过）`
        : `确认批量${actionLabel} ${targets.length} 个楼栋吗？`,
      `批量${actionLabel}楼栋`
    )
    const results = await Promise.allSettled(targets.map((item) => updateBuilding(item.id, { status })))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`楼栋批量${actionLabel}完成：成功 ${success}，失败 ${failed}`)
    selectedBuildingRowKeys.value = []
    selectedBuildings.value = []
    await Promise.all([refreshBuildings(), loadBuildingList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, `楼栋批量${actionLabel}失败`))
  }
}

async function batchUpdateFloorsStatus(status: 1 | 0) {
  const selectedTargets = selectedFloors.value
  if (!selectedTargets.length) {
    message.warning('请先勾选楼层')
    return
  }
  const actionLabel = status === 1 ? '启用' : '停用'
  const targets = selectedTargets.filter((item) => Number(item.status ?? 0) !== status)
  if (!targets.length) {
    message.info(`已选楼层都已是${actionLabel}状态`)
    return
  }
  try {
    const skipped = selectedTargets.length - targets.length
    await confirmAction(
      skipped > 0
        ? `确认批量${actionLabel} ${targets.length} 个楼层吗？（${skipped} 个状态一致已跳过）`
        : `确认批量${actionLabel} ${targets.length} 个楼层吗？`,
      `批量${actionLabel}楼层`
    )
    const results = await Promise.allSettled(targets.map((item) => updateFloor(item.id, { status })))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`楼层批量${actionLabel}完成：成功 ${success}，失败 ${failed}`)
    selectedFloorRowKeys.value = []
    selectedFloors.value = []
    await Promise.all([refreshFloors(), loadFloorList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, `楼层批量${actionLabel}失败`))
  }
}

async function batchUpdateRoomsStatus(status: 1 | 0) {
  const selectedTargets = selectedRooms.value
  if (!selectedTargets.length) {
    message.warning('请先勾选房间')
    return
  }
  const actionLabel = status === 1 ? '启用' : '停用'
  const targets = selectedTargets.filter((item) => Number(item.status ?? 0) !== status)
  if (!targets.length) {
    message.info(`已选房间都已是${actionLabel}状态`)
    return
  }
  try {
    const skipped = selectedTargets.length - targets.length
    await confirmAction(
      skipped > 0
        ? `确认批量${actionLabel} ${targets.length} 个房间吗？（${skipped} 个状态一致已跳过）`
        : `确认批量${actionLabel} ${targets.length} 个房间吗？`,
      `批量${actionLabel}房间`
    )
    const results = await Promise.allSettled(targets.map((item) => updateRoom(item.id, { status })))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`房间批量${actionLabel}完成：成功 ${success}，失败 ${failed}`)
    selectedRoomRowKeys.value = []
    selectedRooms.value = []
    await Promise.all([refreshRooms(), loadRoomList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, `房间批量${actionLabel}失败`))
  }
}

async function batchUpdateBedsStatus(status: 1 | 0) {
  const selectedTargets = selected.value
  if (!selectedTargets.length) {
    message.warning('请先勾选床位')
    return
  }
  const actionLabel = status === 1 ? '启用' : '停用'
  const targets = selectedTargets.filter((item) => Number(item.status ?? 0) !== status)
  if (!targets.length) {
    message.info(`已选床位都已是${actionLabel}状态`)
    return
  }
  try {
    const skipped = selectedTargets.length - targets.length
    await confirmAction(
      skipped > 0
        ? `确认批量${actionLabel} ${targets.length} 个床位吗？（${skipped} 个状态一致已跳过）`
        : `确认批量${actionLabel} ${targets.length} 个床位吗？`,
      `批量${actionLabel}床位`
    )
    const results = await Promise.allSettled(targets.map((item) => updateBed(item.id, { status })))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`床位批量${actionLabel}完成：成功 ${success}，失败 ${failed}`)
    selectedRowKeys.value = []
    selected.value = []
    await Promise.all([refreshBeds(), loadBedList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, `床位批量${actionLabel}失败`))
  }
}

async function quickGenerateBuildings() {
  try {
    const numbers = buildingList.value
      .map((item) => Number(String(item.name || '').match(/\d+/)?.[0] || 0))
      .filter((item) => item > 0)
    const nextNo = (numbers.length ? Math.max(...numbers) : 0) + 1
    const name = `楼栋${nextNo}`
    await createBuilding({
      name,
      code: `B${nextNo}`,
      status: 1,
      sortNo: nextNo
    })
    message.success(`已生成 ${name}`)
    await Promise.all([refreshBuildings(), loadBuildingList(), refreshTree()])
  } catch (error: any) {
    message.error(errorMessage(error, '一键生成楼栋失败'))
  }
}

async function quickDeleteBuildings() {
  const targets = selectedBuildings.value.length ? selectedBuildings.value : buildings.value
  if (!targets.length) {
    message.warning('当前页没有可删除楼栋')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个楼栋吗？`, '一键删除楼栋')
    const results = await Promise.allSettled(targets.map((item) => deleteBuilding(item.id)))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`楼栋一键删除完成：成功 ${success}，失败 ${failed}`)
    selectedBuildingRowKeys.value = []
    selectedBuildings.value = []
    await Promise.all([refreshBuildings(), loadBuildingList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, '一键删除楼栋失败'))
  }
}

async function quickGenerateFloors() {
  const buildingId = floorQuery.buildingId || buildingList.value[0]?.id
  if (!buildingId) {
    message.warning('请先新增楼栋')
    return
  }
  try {
    const allFloors = await getFloorList({ buildingId })
    const existed = new Set(allFloors.map((item) => String(item.floorNo || '').toUpperCase()))
    const tasks: Promise<void>[] = []
    for (let i = 1; i <= 6; i++) {
      const floorNo = `${i}F`
      if (existed.has(floorNo)) continue
      tasks.push(
        createFloor({
          buildingId,
          floorNo,
          name: `${i}F`,
          status: 1,
          sortNo: i
        })
      )
    }
    if (!tasks.length) {
      message.info('当前楼栋楼层已齐全（1F-6F）')
      return
    }
    await Promise.all(tasks)
    message.success(`已生成 ${tasks.length} 层`)
    await Promise.all([refreshFloors(), loadFloorList(), refreshTree()])
  } catch (error: any) {
    message.error(errorMessage(error, '一键生成楼层失败'))
  }
}

async function quickDeleteFloors() {
  const targets = selectedFloors.value.length ? selectedFloors.value : floors.value
  if (!targets.length) {
    message.warning('当前页没有可删除楼层')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个楼层吗？`, '一键删除楼层')
    const results = await Promise.allSettled(targets.map((item) => deleteFloor(item.id)))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`楼层一键删除完成：成功 ${success}，失败 ${failed}`)
    selectedFloorRowKeys.value = []
    selectedFloors.value = []
    await Promise.all([refreshFloors(), loadFloorList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, '一键删除楼层失败'))
  }
}

async function quickGenerateRooms() {
  const floorId = roomQuery.floorId || floorList.value[0]?.id
  if (!floorId) {
    message.warning('请先选择楼层或先新增楼层')
    return
  }
  const floor = floorList.value.find((item) => item.id === floorId)
  const building = buildingList.value.find((item) => item.id === floor?.buildingId)
  if (!floor || !building) {
    message.warning('楼栋/楼层信息缺失，请刷新后重试')
    return
  }
  const floorNoNumeric = Number(String(floor.floorNo || '').replace(/[^\d]/g, '') || 1)
  const head = String(building.name || 'A').slice(0, 1).toUpperCase()
  const existed = new Set(roomList.value.map((item) => item.roomNo))
  const tasks: Promise<void>[] = []
  for (let i = 1; i <= 30; i++) {
    const roomNo = `${head}${floorNoNumeric}${String(i).padStart(2, '0')}`
    if (existed.has(roomNo)) continue
    tasks.push(
      createRoom({
        buildingId: building.id,
        floorId: floor.id,
        roomNo,
        capacity: 2,
        roomType: inferRoomTypeByCapacity(2),
        status: 1
      })
    )
  }
  if (!tasks.length) {
    message.info('当前楼层房间已齐全（01-30）')
    return
  }
  try {
    await Promise.all(tasks)
    message.success(`已生成 ${tasks.length} 间房`)
    await Promise.all([refreshRooms(), loadRoomList(), refreshTree()])
  } catch (error: any) {
    message.error(errorMessage(error, '一键生成房间失败'))
  }
}

async function quickDeleteRooms() {
  const targets = selectedRooms.value.length ? selectedRooms.value : rooms.value
  if (!targets.length) {
    message.warning('当前页没有可删除房间')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个房间吗？`, '一键删除房间')
    const results = await Promise.allSettled(targets.map((item) => deleteRoom(item.id)))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`房间一键删除完成：成功 ${success}，失败 ${failed}`)
    selectedRoomRowKeys.value = []
    selectedRooms.value = []
    await Promise.all([refreshRooms(), loadRoomList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, '一键删除房间失败'))
  }
}

async function quickGenerateBeds() {
  const roomMap = new Map(roomList.value.map((item) => [item.id, item]))
  const bedCountByRoom = bedList.value.reduce((acc, item) => {
    acc[item.roomId] = (acc[item.roomId] || 0) + 1
    return acc
  }, {} as Record<string | number, number>)
  const targets = (rooms.value.length ? rooms.value : roomList.value).slice(0, 80)
  const bedType = bedTypeOptions.value[0]?.value
  const tasks: Promise<void>[] = []
  targets.forEach((room) => {
    const roomData = roomMap.get(room.id) || room
    const capacity = Number(resolveRoomCapacity(roomData) || 0)
    if (!capacity) return
    const existing = Number(bedCountByRoom[room.id] || 0)
    for (let i = existing + 1; i <= capacity; i++) {
      tasks.push(
        createBed({
          roomId: room.id,
          bedNo: String(i).padStart(2, '0'),
          bedType,
          status: 1
        })
      )
    }
  })
  if (!tasks.length) {
    message.info('当前范围内床位已与房间容量一致')
    return
  }
  try {
    await Promise.all(tasks)
    message.success(`已生成 ${tasks.length} 个床位`)
    await Promise.all([refreshBeds(), loadBedList(), refreshTree()])
  } catch (error: any) {
    message.error(errorMessage(error, '一键生成床位失败'))
  }
}

async function quickDeleteBeds() {
  const targets = selected.value.length ? selected.value : beds.value
  if (!targets.length) {
    message.warning('当前页没有可删除床位')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个床位吗？`, '一键删除床位')
    const results = await Promise.allSettled(targets.map((item) => deleteBed(item.id)))
    const success = results.filter((item) => item.status === 'fulfilled').length
    const failed = results.length - success
    message.info(`床位一键删除完成：成功 ${success}，失败 ${failed}`)
    selectedRowKeys.value = []
    selected.value = []
    await Promise.all([refreshBeds(), loadBedList(), refreshTree()])
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, '一键删除床位失败'))
  }
}

async function removeBuilding(id: Id) {
  try {
    await confirmAction('确认删除该楼栋吗？', '提示')
    await deleteBuilding(id)
    message.success('已删除')
    await refreshBuildings()
    await loadBuildingList()
  } catch (error: any) {
    if (!error) return
    message.warning(errorMessage(error, '删除失败'))
  }
}

async function removeFloor(id: Id) {
  try {
    await confirmAction('确认删除该楼层吗？', '提示')
    await deleteFloor(id)
    message.success('已删除')
    await refreshFloors()
    await loadFloorList()
  } catch (error: any) {
    if (!error) return
    message.warning(errorMessage(error, '删除失败'))
  }
}

async function removeRoom(id: Id) {
  try {
    await confirmAction('确认删除该房间吗？', '提示')
    await deleteRoom(id)
    message.success('已删除')
    await refreshRooms()
    await loadRoomList()
  } catch (error: any) {
    if (!error) return
    message.warning(errorMessage(error, '删除失败'))
  }
}

async function removeBed(id: Id) {
  try {
    await confirmAction('确认删除该床位吗？', '提示')
    await deleteBed(id)
    message.success('已删除')
    await refreshBeds()
    await loadBedList()
  } catch (error: any) {
    if (!error) return
    message.warning(errorMessage(error, '删除失败'))
  }
}

const printOpen = ref(false)
const printList = ref<{ id: Id; qr: string; text: string }[]>([])

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

function openBedMap() {
  router.push('/bed/map')
}

function openElderBedPanorama() {
  router.push('/elder/bed-panorama')
}

function exportBedCsv() {
  exportCsv(
    beds.value.map((b) => ({
      床位号: b.bedNo,
      床位类型: resolveBedTypeLabel(b.bedType),
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

async function loadResidenceConfigOptions() {
  try {
    const [roomTypes, bedTypes, areas] = await Promise.all([
      getBaseConfigItemList({ configGroup: 'ADMISSION_ROOM_TYPE', status: 1 }),
      getBaseConfigItemList({ configGroup: 'ADMISSION_BED_TYPE', status: 1 }),
      getBaseConfigItemList({ configGroup: 'ADMISSION_AREA', status: 1 })
    ])
    roomTypeItems.value = roomTypes.length ? roomTypes : fallbackRoomTypes
    bedTypeItems.value = bedTypes.length ? bedTypes : fallbackBedTypes
    areaItems.value = areas.length ? areas : fallbackAreas
  } catch {
    roomTypeItems.value = fallbackRoomTypes
    bedTypeItems.value = fallbackBedTypes
    areaItems.value = fallbackAreas
    message.warning('加载入住基础配置失败，已使用默认房型/床型/区域')
  }
}

watch(
  () => roomForm.buildingId,
  () => {
    if (!roomForm.floorId) return
    const exists = roomFloorOptions.value.some((item) => item.id === roomForm.floorId)
    if (!exists) roomForm.floorId = undefined
  }
)

watch(
  () => roomForm.roomType,
  (roomType) => {
    const nextCapacity = inferCapacityByRoomType(roomType)
    if (!nextCapacity) return
    if (roomForm.capacity !== nextCapacity) {
      roomForm.capacity = nextCapacity
    }
  }
)

watch(
  () => roomForm.capacity,
  (capacity) => {
    const nextRoomType = inferRoomTypeByCapacity(Number(capacity || 0))
    if (!nextRoomType) return
    if (roomForm.roomType !== nextRoomType) {
      roomForm.roomType = nextRoomType
    }
  }
)

watch(
  () => bootstrapForm.bedsPerRoom,
  (bedsPerRoom) => {
    if (bootstrapForm.templateCode === 'AB_F1_6_R101_130_B01_03') return
    const next = inferRoomTypeByCapacity(Number(bedsPerRoom || 0))
    if (!next) return
    if (bootstrapForm.roomType !== next) {
      bootstrapForm.roomType = next
    }
  }
)

useLiveSyncRefresh({
  topics: ['bed', 'elder'],
  refresh: async () => {
    await Promise.all([
      refreshTree(),
      searchBuildings(),
      searchFloors(),
      searchRooms(),
      searchBeds(),
      loadRoomList(),
      loadBedList(),
      loadBuildingList(),
      loadFloorList()
    ])
  }
})

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
  await loadResidenceConfigOptions()
})
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}
.entry-actions {
  margin-bottom: 12px;
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
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.selection-info {
  color: var(--muted);
  font-size: 13px;
  white-space: nowrap;
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
.bootstrap-presets {
  margin-bottom: 12px;
}
.bootstrap-hint {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
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
