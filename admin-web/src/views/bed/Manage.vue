<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <div class="bed-management-page">
      <a-row :gutter="20" class="workspace-grid">
        <a-col :xs="24" :xl="6">
          <a-card class="asset-tree-panel" :bordered="false">
            <div class="tree-panel-head">
              <div>
                <div class="panel-eyebrow">资产结构</div>
                <div class="tree-title">楼栋 - 楼层 - 房间 - 床位</div>
                <div class="tree-summary">
                  {{ treeStats.buildings }} 栋 / {{ treeStats.floors }} 层 / {{ treeStats.rooms }} 房 / {{ treeStats.beds }} 床
                </div>
              </div>
              <a-button size="small" @click="refreshTree">刷新</a-button>
            </div>
            <a-tree
              :tree-data="treeData"
              :field-names="{ title: 'name', key: 'treeKey', children: 'children' }"
              :loading="treeLoading"
              show-line
              class="asset-tree"
              @select="(_, info) => onTreeSelect(info.node)"
            >
              <template #title="node">
                <div class="tree-node-card">
                  <div class="tree-node-main">
                    <span class="tree-node-type">{{ treeTypeLabel(node.type || node.dataRef?.type) }}</span>
                    <span class="tree-node-name">{{ node.name || node.dataRef?.name }}</span>
                  </div>
                  <div class="tree-node-meta">
                    {{ formatTreeNodeSummary(node.dataRef || node) }}
                  </div>
                </div>
              </template>
            </a-tree>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="18">
          <div class="workspace-shell">
            <a-card class="workspace-hero" :bordered="false">
              <div class="workspace-topbar">
                <div>
                  <div class="workspace-path">{{ currentPathText }}</div>
                  <h2 class="workspace-title">{{ activeTabLabel }}</h2>
                  <p class="workspace-subtitle">{{ currentScopeHint }}</p>
                </div>
                <a-radio-group :value="currentWorkspaceMode" button-style="solid" @change="onWorkspaceModeChange">
                  <a-radio-button value="manage">床位管理</a-radio-button>
                  <a-radio-button value="map">房态图</a-radio-button>
                  <a-radio-button value="panorama">床态全景</a-radio-button>
                </a-radio-group>
              </div>
              <a-tabs v-model:activeKey="activeTab" class="management-tabs">
                <a-tab-pane v-if="isTabEnabled('buildings')" key="buildings" tab="楼栋管理" />
                <a-tab-pane v-if="isTabEnabled('floors')" key="floors" tab="楼层管理" />
                <a-tab-pane v-if="isTabEnabled('rooms')" key="rooms" tab="房间管理" />
                <a-tab-pane v-if="isTabEnabled('beds')" key="beds" tab="床位管理" />
              </a-tabs>
            </a-card>

            <a-row :gutter="[16, 16]" class="overview-row">
              <a-col v-for="card in overviewCards" :key="card.label" :xs="24" :md="12" :xl="6">
                <a-card class="overview-card" :bordered="false">
                  <div class="overview-label">{{ card.label }}</div>
                  <div class="overview-value">{{ card.value }}</div>
                  <div class="overview-meta">{{ card.meta }}</div>
                </a-card>
              </a-col>
            </a-row>

            <div v-if="activeTab === 'buildings'" class="workspace-stack">
              <a-card class="section-card filter-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">筛选条件</div>
                    <div class="section-desc">快速定位楼栋并查看启用状态与区域信息</div>
                  </div>
                  <a-button type="link" @click="resetBuildings">清空筛选</a-button>
                </div>
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
              </a-card>

              <a-card class="section-card action-card" :bordered="false">
                <div class="toolbar-row">
                  <div class="toolbar-main">
                    <a-button type="primary" @click="openBuilding()">新增楼栋</a-button>
                    <a-button @click="openGenerateWizard('buildings')">批量生成</a-button>
                    <a-button danger :disabled="!selectedBuildings.length" @click="quickDeleteBuildings">批量删除</a-button>
                  </div>
                  <div class="toolbar-side">
                    <div class="selection-badge">已选 {{ selectedBuildings.length }} 个楼栋</div>
                    <a-button :disabled="!selectedBuildings.length" @click="batchUpdateBuildingsStatus(1)">批量启用</a-button>
                    <a-button :disabled="!selectedBuildings.length" @click="batchUpdateBuildingsStatus(0)">批量停用</a-button>
                    <a-dropdown>
                      <a-button>更多操作</a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="structure" @click="openBootstrap">结构生成向导</a-menu-item>
                          <a-menu-item key="refresh" @click="refreshBuildings">刷新列表</a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </div>
              </a-card>

              <a-card class="section-card table-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">楼栋列表</div>
                    <div class="section-desc">当前共 {{ buildingPage.total }} 条记录</div>
                  </div>
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
                    <template v-else-if="column.key === 'remark'">
                      {{ resolvePublicRemark(record.remark) || '-' }}
                    </template>
                    <template v-else-if="column.key === 'action'">
                      <div class="row-actions">
                        <a-button type="link" @click="openBuilding(record, 'view')">查看</a-button>
                        <a-button type="link" @click="openBuilding(record, 'edit')">编辑</a-button>
                        <a-dropdown>
                          <a-button type="link">更多</a-button>
                          <template #overlay>
                            <a-menu>
                              <a-menu-item :key="`toggle-${record.id}`" @click="toggleBuildingStatus(record)">
                                {{ record.status === 1 ? '停用' : '启用' }}
                              </a-menu-item>
                              <a-menu-item :key="`delete-${record.id}`" danger @click="removeBuilding(record.id)">删除</a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </div>
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
              </a-card>
            </div>

            <div v-if="activeTab === 'floors'" class="workspace-stack">
              <a-card class="section-card filter-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">筛选条件</div>
                    <div class="section-desc">先选楼栋，再按楼层范围管理楼层资产</div>
                  </div>
                  <a-button type="link" @click="resetFloors">清空筛选</a-button>
                </div>
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
              </a-card>

              <a-card class="section-card action-card" :bordered="false">
                <div class="toolbar-row">
                  <div class="toolbar-main">
                    <a-button type="primary" @click="openFloor()">新增楼层</a-button>
                    <a-button @click="openGenerateWizard('floors')">批量生成</a-button>
                    <a-button danger :disabled="!selectedFloors.length" @click="quickDeleteFloors">批量删除</a-button>
                  </div>
                  <div class="toolbar-side">
                    <div class="selection-badge">已选 {{ selectedFloors.length }} 个楼层</div>
                    <a-button :disabled="!selectedFloors.length" @click="batchUpdateFloorsStatus(1)">批量启用</a-button>
                    <a-button :disabled="!selectedFloors.length" @click="batchUpdateFloorsStatus(0)">批量停用</a-button>
                    <a-dropdown>
                      <a-button>更多操作</a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="refresh" @click="refreshFloors">刷新列表</a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </div>
              </a-card>

              <a-card class="section-card table-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">楼层列表</div>
                    <div class="section-desc">当前共 {{ floorPage.total }} 条记录</div>
                  </div>
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
                      <div class="row-actions">
                        <a-button type="link" @click="openFloor(record, 'view')">查看</a-button>
                        <a-button type="link" @click="openFloor(record, 'edit')">编辑</a-button>
                        <a-dropdown>
                          <a-button type="link">更多</a-button>
                          <template #overlay>
                            <a-menu>
                              <a-menu-item :key="`toggle-${record.id}`" @click="toggleFloorStatus(record)">
                                {{ record.status === 1 ? '停用' : '启用' }}
                              </a-menu-item>
                              <a-menu-item :key="`delete-${record.id}`" danger @click="removeFloor(record.id)">删除</a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </div>
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
              </a-card>
            </div>

            <div v-if="activeTab === 'rooms'" class="workspace-stack">
              <a-card class="section-card filter-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">筛选条件</div>
                    <div class="section-desc">按楼栋、楼层和房型快速定位房间</div>
                  </div>
                  <a-button type="link" @click="resetRooms">清空筛选</a-button>
                </div>
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
              </a-card>

              <a-card class="section-card action-card" :bordered="false">
                <div class="toolbar-row">
                  <div class="toolbar-main">
                    <a-button type="primary" @click="openRoom()">新增房间</a-button>
                    <a-button @click="openGenerateWizard('rooms')">批量生成</a-button>
                    <a-button danger :disabled="!selectedRooms.length" @click="quickDeleteRooms">批量删除</a-button>
                  </div>
                  <div class="toolbar-side">
                    <div class="selection-badge">已选 {{ selectedRooms.length }} 个房间</div>
                    <a-button :disabled="!selectedRooms.length" @click="batchUpdateRoomsStatus(1)">批量启用</a-button>
                    <a-button :disabled="!selectedRooms.length" @click="batchUpdateRoomsStatus(0)">批量停用</a-button>
                    <a-dropdown>
                      <a-button>更多操作</a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="refresh" @click="refreshRooms">刷新列表</a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </div>
              </a-card>

              <a-card class="section-card table-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">房间列表</div>
                    <div class="section-desc">当前共 {{ roomPage.total }} 条记录</div>
                  </div>
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
                    <template v-else-if="column.key === 'remark'">
                      {{ resolvePublicRemark(record.remark) || '-' }}
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
                      <div class="row-actions">
                        <a-button type="link" @click="openRoom(record, 'view')">查看</a-button>
                        <a-button type="link" @click="openRoom(record, 'edit')">编辑</a-button>
                        <a-dropdown>
                          <a-button type="link">更多</a-button>
                          <template #overlay>
                            <a-menu>
                              <a-menu-item :key="`toggle-${record.id}`" @click="toggleRoomStatus(record)">
                                {{ record.status === 1 ? '停用' : '启用' }}
                              </a-menu-item>
                              <a-menu-item :key="`delete-${record.id}`" danger @click="removeRoom(record.id)">删除</a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </div>
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
              </a-card>
            </div>

            <div v-if="activeTab === 'beds'" class="workspace-stack">
              <a-card class="section-card filter-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">筛选条件</div>
                    <div class="section-desc">按楼栋路径和状态快速定位床位与入住信息</div>
                  </div>
                  <a-button type="link" @click="resetBeds">清空筛选</a-button>
                </div>
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
              </a-card>

              <a-card class="section-card action-card" :bordered="false">
                <div class="toolbar-row">
                  <div class="toolbar-main">
                    <a-button type="primary" @click="openBed()">新增床位</a-button>
                    <a-button @click="openGenerateWizard('beds')">批量生成</a-button>
                    <a-button danger :disabled="!selected.length" @click="quickDeleteBeds">批量删除</a-button>
                  </div>
                  <div class="toolbar-side">
                    <div class="selection-badge">已选 {{ selected.length }} 个床位</div>
                    <a-button :disabled="!selected.length" @click="batchUpdateBedsStatus(1)">批量启用</a-button>
                    <a-button :disabled="!selected.length" @click="batchUpdateBedsStatus(0)">批量停用</a-button>
                    <a-dropdown>
                      <a-button>更多操作</a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="qr" :disabled="!selected.length" @click="batchQr">批量二维码</a-menu-item>
                          <a-menu-item key="csv" @click="exportBedCsv">导出 CSV</a-menu-item>
                          <a-menu-item key="refresh" @click="refreshBeds">刷新列表</a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </div>
              </a-card>

              <a-card class="section-card table-card" :bordered="false">
                <div class="section-head">
                  <div>
                    <div class="section-title">床位列表</div>
                    <div class="section-desc">当前共 {{ bedPage.total }} 条记录</div>
                  </div>
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
                      <div class="row-actions">
                        <a-button type="link" @click="openBed(record, 'view')">查看</a-button>
                        <a-button type="link" @click="openBed(record, 'edit')">编辑</a-button>
                        <a-dropdown>
                          <a-button type="link">更多</a-button>
                          <template #overlay>
                            <a-menu>
                              <a-menu-item :key="`qr-${record.id}`" @click="printSingle(record)">生成二维码</a-menu-item>
                              <a-menu-item :key="`toggle-${record.id}`" @click="toggleBedStatus(record)">
                                {{ record.status === 1 ? '停用' : '启用' }}
                              </a-menu-item>
                              <a-menu-item :key="`delete-${record.id}`" danger @click="removeBed(record.id)">删除</a-menu-item>
                            </a-menu>
                          </template>
                        </a-dropdown>
                      </div>
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
              </a-card>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <a-drawer
      v-model:open="buildingOpen"
      :title="buildingDrawerTitle"
      width="480"
      placement="right"
      :destroy-on-close="false"
      @close="() => (buildingOpen = false)"
    >
      <a-form ref="buildingFormRef" :model="buildingForm" :rules="buildingRules" layout="vertical">
        <a-form-item label="楼栋名称" name="name">
          <a-input v-model:value="buildingForm.name" :disabled="buildingDrawerMode === 'view'" placeholder="如 1 号楼" />
        </a-form-item>
        <a-form-item label="编码" name="code">
          <a-input v-model:value="buildingForm.code" disabled placeholder="系统自动生成" />
        </a-form-item>
        <a-form-item label="区域" name="areaCode">
          <a-select v-model:value="buildingForm.areaCode" :disabled="buildingDrawerMode === 'view'" allow-clear placeholder="请选择区域">
            <a-select-option v-for="item in areaOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序" name="sortNo">
          <a-input-number v-model:value="buildingForm.sortNo" disabled style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="buildingForm.status" :disabled="buildingDrawerMode === 'view'" placeholder="请选择">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="buildingForm.remark" :disabled="buildingDrawerMode === 'view'" placeholder="可选" />
        </a-form-item>
        <a-form-item label="备注1（可公开）">
          <a-space style="display: flex">
            <a-switch v-model:checked="buildingRemarkSlots[0].visible" :disabled="buildingDrawerMode === 'view'" checked-children="显示" un-checked-children="隐藏" />
            <a-input v-model:value="buildingRemarkSlots[0].text" :disabled="buildingDrawerMode === 'view'" placeholder="例如：自理老人楼" />
          </a-space>
        </a-form-item>
        <a-form-item label="备注2（可公开）">
          <a-space style="display: flex">
            <a-switch v-model:checked="buildingRemarkSlots[1].visible" :disabled="buildingDrawerMode === 'view'" checked-children="显示" un-checked-children="隐藏" />
            <a-input v-model:value="buildingRemarkSlots[1].text" :disabled="buildingDrawerMode === 'view'" placeholder="例如：靠近活动中心" />
          </a-space>
        </a-form-item>
        <a-form-item label="备注3（可公开）">
          <a-space style="display: flex">
            <a-switch v-model:checked="buildingRemarkSlots[2].visible" :disabled="buildingDrawerMode === 'view'" checked-children="显示" un-checked-children="隐藏" />
            <a-input v-model:value="buildingRemarkSlots[2].text" :disabled="buildingDrawerMode === 'view'" placeholder="例如：夜间安静区" />
          </a-space>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="buildingOpen = false">关闭</a-button>
          <a-button v-if="buildingDrawerMode === 'view'" type="primary" @click="buildingDrawerMode = 'edit'">转为编辑</a-button>
          <a-button v-else type="primary" :loading="buildingSubmitting" @click="submitBuilding">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer
      v-model:open="floorOpen"
      :title="floorDrawerTitle"
      width="480"
      placement="right"
      :destroy-on-close="false"
      @close="() => (floorOpen = false)"
    >
      <a-form ref="floorFormRef" :model="floorForm" :rules="floorRules" layout="vertical">
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="floorForm.buildingId" :disabled="floorDrawerMode === 'view'" placeholder="请选择">
            <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层编号" name="floorNo">
          <a-input v-model:value="floorForm.floorNo" disabled placeholder="系统自动生成，如 1F / 2F" />
        </a-form-item>
        <a-form-item label="名称" name="name">
          <a-input v-model:value="floorForm.name" :disabled="floorDrawerMode === 'view'" placeholder="如 三层" />
        </a-form-item>
        <a-form-item label="排序" name="sortNo">
          <a-input-number v-model:value="floorForm.sortNo" disabled style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="floorForm.status" :disabled="floorDrawerMode === 'view'" placeholder="请选择">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="floorOpen = false">关闭</a-button>
          <a-button v-if="floorDrawerMode === 'view'" type="primary" @click="floorDrawerMode = 'edit'">转为编辑</a-button>
          <a-button v-else type="primary" :loading="floorSubmitting" @click="submitFloor">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer
      v-model:open="roomOpen"
      :title="roomDrawerTitle"
      width="520"
      placement="right"
      :destroy-on-close="false"
      @close="() => (roomOpen = false)"
    >
      <a-form ref="roomFormRef" :model="roomForm" :rules="roomRules" layout="vertical">
        <a-form-item label="房间号" name="roomNo">
          <a-input v-model:value="roomForm.roomNo" disabled placeholder="系统自动生成，如 301 / 302" />
        </a-form-item>
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="roomForm.buildingId" :disabled="roomDrawerMode === 'view'" placeholder="请选择" show-search option-filter-prop="label">
            <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="roomForm.floorId" :disabled="roomDrawerMode === 'view'" placeholder="请选择">
            <a-select-option v-for="f in roomFloorOptions" :key="f.id" :value="f.id">{{ f.floorNo }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位容量" name="capacity">
          <a-input-number v-model:value="roomForm.capacity" :disabled="roomDrawerMode === 'view'" style="width: 100%" :min="1" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="roomForm.status" :disabled="roomDrawerMode === 'view'" placeholder="请选择">
            <a-select-option :value="1">可用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="roomForm.remark" :disabled="roomDrawerMode === 'view'" placeholder="如 重点照护区/近电梯口" />
        </a-form-item>
        <a-form-item label="备注1（可公开）">
          <a-space style="display: flex">
            <a-switch v-model:checked="roomRemarkSlots[0].visible" :disabled="roomDrawerMode === 'view'" checked-children="显示" un-checked-children="隐藏" />
            <a-input v-model:value="roomRemarkSlots[0].text" :disabled="roomDrawerMode === 'view'" placeholder="例如：高风险重点巡视房" />
          </a-space>
        </a-form-item>
        <a-form-item label="备注2（可公开）">
          <a-space style="display: flex">
            <a-switch v-model:checked="roomRemarkSlots[1].visible" :disabled="roomDrawerMode === 'view'" checked-children="显示" un-checked-children="隐藏" />
            <a-input v-model:value="roomRemarkSlots[1].text" :disabled="roomDrawerMode === 'view'" placeholder="例如：近护士站" />
          </a-space>
        </a-form-item>
        <a-form-item label="备注3（可公开）">
          <a-space style="display: flex">
            <a-switch v-model:checked="roomRemarkSlots[2].visible" :disabled="roomDrawerMode === 'view'" checked-children="显示" un-checked-children="隐藏" />
            <a-input v-model:value="roomRemarkSlots[2].text" :disabled="roomDrawerMode === 'view'" placeholder="例如：家属探视优先房" />
          </a-space>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="roomOpen = false">关闭</a-button>
          <a-button v-if="roomDrawerMode === 'view'" type="primary" @click="roomDrawerMode = 'edit'">转为编辑</a-button>
          <a-button v-else type="primary" :loading="roomSubmitting" @click="submitRoom">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer
      v-model:open="bedOpen"
      :title="bedDrawerTitle"
      width="520"
      placement="right"
      :destroy-on-close="false"
      @close="() => (bedOpen = false)"
    >
      <a-form ref="bedFormRef" :model="bedForm" :rules="bedRules" layout="vertical">
        <a-form-item label="房间" name="roomId">
          <a-select v-model:value="bedForm.roomId" :disabled="bedDrawerMode === 'view'" placeholder="请选择">
            <a-select-option v-for="option in roomOptions" :key="option.value" :value="option.value">{{ option.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位号" name="bedNo">
          <a-input v-model:value="bedForm.bedNo" disabled placeholder="系统自动生成，如 301-A / 301-B" />
        </a-form-item>
        <a-form-item label="床位类型" name="bedType">
          <a-select v-model:value="bedForm.bedType" :disabled="bedDrawerMode === 'view'" allow-clear placeholder="请选择">
            <a-select-option v-for="item in bedTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="bedForm.status" :disabled="bedDrawerMode === 'view'" placeholder="请选择">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维护</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="bedOpen = false">关闭</a-button>
          <a-button v-if="bedDrawerMode === 'view'" type="primary" @click="bedDrawerMode = 'edit'">转为编辑</a-button>
          <a-button v-else type="primary" :loading="bedSubmitting" @click="submitBed">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

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
      title="结构生成向导"
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

    <a-modal
      v-model:open="generateOpen"
      :title="generateWizardTitle"
      width="560px"
      :confirm-loading="generateSubmitting"
      ok-text="开始生成"
      @ok="submitGenerateWizard"
      @cancel="() => (generateOpen = false)"
    >
      <div class="wizard-steps">
        <div class="wizard-step is-active">1. 选择范围</div>
        <div class="wizard-step is-active">2. 设置参数</div>
        <div class="wizard-step is-active">3. 确认生成</div>
      </div>
      <div class="wizard-hint">{{ generateWizardHint }}</div>
      <a-form :model="generateForm" layout="vertical">
        <template v-if="generateTarget === 'buildings'">
          <a-form-item label="生成数量">
            <a-input-number v-model:value="generateForm.buildingCount" style="width: 100%" :min="1" :max="20" />
          </a-form-item>
          <a-form-item label="楼栋前缀">
            <a-input v-model:value="generateForm.buildingPrefix" placeholder="如 楼栋" />
          </a-form-item>
        </template>

        <template v-else-if="generateTarget === 'floors'">
          <a-form-item label="所属楼栋">
            <a-select v-model:value="generateForm.buildingId" placeholder="请选择楼栋">
              <a-select-option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.name }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-row :gutter="12">
            <a-col :span="12">
              <a-form-item label="起始楼层">
                <a-input-number v-model:value="generateForm.floorStart" style="width: 100%" :min="1" :max="50" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="结束楼层">
                <a-input-number v-model:value="generateForm.floorEnd" style="width: 100%" :min="1" :max="50" />
              </a-form-item>
            </a-col>
          </a-row>
        </template>

        <template v-else-if="generateTarget === 'rooms'">
          <a-form-item label="所属楼层">
            <a-select v-model:value="generateForm.floorId" placeholder="请选择楼层">
              <a-select-option v-for="f in floorList" :key="f.id" :value="f.id">
                {{ buildingNameMap[f.buildingId] || '未命名楼栋' }} / {{ f.floorNo }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-row :gutter="12">
            <a-col :span="12">
              <a-form-item label="起始房号">
                <a-input-number v-model:value="generateForm.roomStart" style="width: 100%" :min="1" :max="999" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="结束房号">
                <a-input-number v-model:value="generateForm.roomEnd" style="width: 100%" :min="1" :max="999" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item label="每间房床位容量">
            <a-input-number v-model:value="generateForm.roomCapacity" style="width: 100%" :min="1" :max="8" />
          </a-form-item>
        </template>

        <template v-else>
          <a-form-item label="补齐范围">
            <a-radio-group v-model:value="generateForm.bedScope">
              <a-radio value="current">当前列表范围</a-radio>
              <a-radio value="all">全部房间</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="默认床型">
            <a-select v-model:value="generateForm.bedType" allow-clear placeholder="默认取系统首个启用床型">
              <a-select-option v-for="item in bedTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
            </a-select>
          </a-form-item>
        </template>
      </a-form>
      <div class="wizard-preview">
        {{ generatePreviewText }}
      </div>
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
  previewResidenceBatchGeneration,
  commitResidenceBatchGeneration,
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
  ResidenceBatchGenerationRequest,
  ResidenceBatchPreviewResponse,
  Id
} from '../../types'

const props = withDefaults(defineProps<{
  initialTab?: 'buildings' | 'floors' | 'rooms' | 'beds'
  allowedTabs?: Array<'buildings' | 'floors' | 'rooms' | 'beds'>
}>(), {
  initialTab: 'buildings'
})

type TabKey = 'buildings' | 'floors' | 'rooms' | 'beds'
type DrawerMode = 'create' | 'edit' | 'view'
type WorkspaceMode = 'manage' | 'map' | 'panorama'

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
const enabledTabs = computed<Array<TabKey>>(() => {
  const tabs = (props.allowedTabs && props.allowedTabs.length ? props.allowedTabs : ['buildings', 'floors', 'rooms', 'beds'])
    .filter((item, index, list) => list.indexOf(item) === index)
  return tabs.length ? tabs : ['beds']
})
const activeTab = ref<TabKey>(enabledTabs.value.includes(props.initialTab) ? props.initialTab : enabledTabs.value[0])
const pageTitle = computed(() => enabledTabs.value.length === 1 && enabledTabs.value[0] === 'beds' ? '床位管理' : '床位管理')
const pageSubTitle = computed(() => enabledTabs.value.length === 1 && enabledTabs.value[0] === 'beds'
  ? '后勤保障模块下的床位维护与状态同步'
  : '楼栋/楼层/房间/床位统一维护（V120联动）')

watch(
  () => [props.initialTab, enabledTabs.value.join(',')],
  ([value]) => {
    if (value && enabledTabs.value.includes(value as TabKey) && activeTab.value !== value) {
      activeTab.value = value as TabKey
      return
    }
    if (!enabledTabs.value.includes(activeTab.value)) {
      activeTab.value = enabledTabs.value[0]
    }
  }
)

function isTabEnabled(tab: TabKey) {
  return enabledTabs.value.includes(tab)
}

const roomOpen = ref(false)
const bedOpen = ref(false)
const buildingOpen = ref(false)
const floorOpen = ref(false)
const buildingDrawerMode = ref<DrawerMode>('create')
const floorDrawerMode = ref<DrawerMode>('create')
const roomDrawerMode = ref<DrawerMode>('create')
const bedDrawerMode = ref<DrawerMode>('create')
const roomForm = reactive<Partial<RoomItem>>({ status: 1 })
const bedForm = reactive<Partial<BedItem>>({ status: 1 })
const buildingForm = reactive<Partial<BuildingItem>>({ status: 1, sortNo: 0 })
const floorForm = reactive<Partial<FloorItem>>({ status: 1, sortNo: 0 })
const buildingRemarkSlots = reactive([
  { text: '', visible: true },
  { text: '', visible: true },
  { text: '', visible: false }
])
const roomRemarkSlots = reactive([
  { text: '', visible: true },
  { text: '', visible: true },
  { text: '', visible: false }
])
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
const generateOpen = ref(false)
const generateSubmitting = ref(false)
const generateTarget = ref<TabKey>('beds')
const selectedTreeNode = ref<AssetTreeNode | null>(null)
const selectedTreePath = ref<AssetTreeNode[]>([])

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

const generateForm = reactive({
  buildingCount: 1,
  buildingPrefix: '楼栋',
  buildingId: undefined as Id | undefined,
  floorId: undefined as Id | undefined,
  floorStart: 1,
  floorEnd: 6,
  roomStart: 1,
  roomEnd: 30,
  roomCapacity: 2,
  bedType: undefined as string | undefined,
  bedScope: 'current' as 'current' | 'all'
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

const currentWorkspaceMode = computed<WorkspaceMode>(() => 'manage')

const buildingDrawerTitle = computed(() => drawerTitle('楼栋', buildingDrawerMode.value))
const floorDrawerTitle = computed(() => drawerTitle('楼层', floorDrawerMode.value))
const roomDrawerTitle = computed(() => drawerTitle('房间', roomDrawerMode.value))
const bedDrawerTitle = computed(() => drawerTitle('床位', bedDrawerMode.value))

const currentPathText = computed(() => {
  const labels = selectedTreePath.value.map((item) => item.name).filter(Boolean)
  return [...(labels.length ? labels : ['全院']), activeTabLabel.value].join(' / ')
})

const currentScopeHint = computed(() => {
  const scopeName = selectedTreeNode.value?.name || '当前院区'
  if (activeTab.value === 'buildings') return `聚焦 ${scopeName} 的楼栋档案、区域属性与启停状态。`
  if (activeTab.value === 'floors') return `聚焦 ${scopeName} 的楼层结构，适合分层维护和批量补齐。`
  if (activeTab.value === 'rooms') return `聚焦 ${scopeName} 的房间配置，便于容量、房型和使用状态统一管理。`
  return `聚焦 ${scopeName} 的床位分配与状态维护，适合快速处理入住、停用和二维码操作。`
})

const treeStats = computed(() => countTreeStats(assetTree.value))

const overviewCards = computed(() => {
  if (activeTab.value === 'buildings') {
    return [
      { label: '楼栋总数', value: String(buildingPage.total || buildingList.value.length), meta: `启用 ${buildingList.value.filter((item) => item.status === 1).length} 栋` },
      { label: '楼层覆盖', value: `${treeStats.value.floors}`, meta: '来自当前资产树统计' },
      { label: '选中数量', value: `${selectedBuildings.value.length}`, meta: '可用于批量启停与删除' },
      { label: '当前路径', value: selectedTreeNode.value?.name || '全院', meta: '资产树焦点范围' }
    ]
  }
  if (activeTab.value === 'floors') {
    return [
      { label: '楼层总数', value: String(floorPage.total || floorList.value.length), meta: `所属楼栋 ${new Set(floorList.value.map((item) => item.buildingId)).size} 栋` },
      { label: '当前楼栋', value: floorQuery.buildingId ? buildingNameMap.value[floorQuery.buildingId] || '已选楼栋' : '未限定', meta: '筛选范围' },
      { label: '选中数量', value: `${selectedFloors.value.length}`, meta: '可批量启停与删除' },
      { label: '已启用', value: `${floorList.value.filter((item) => item.status === 1).length}`, meta: '楼层可用状态' }
    ]
  }
  if (activeTab.value === 'rooms') {
    return [
      { label: '房间总数', value: `${roomStats.value.total}`, meta: `可用 ${roomStats.value.active} / 停用 ${roomStats.value.inactive}` },
      { label: '当前筛选', value: roomQuery.floorId ? '单层' : roomQuery.buildingId ? '单栋' : '全院', meta: '按楼栋楼层快速聚焦' },
      { label: '已选房间', value: `${selectedRooms.value.length}`, meta: '适合批量启停和删除' },
      { label: '房型配置', value: `${roomTypeOptions.value.length}`, meta: '当前启用房型项' }
    ]
  }
  return [
    { label: '床位总数', value: `${bedStats.value.total}`, meta: `空床 ${bedStats.value.available} / 入住 ${bedStats.value.occupied} / 维护 ${bedStats.value.maintenance}` },
    { label: '已选床位', value: `${selected.value.length}`, meta: '支持批量二维码、启停、删除' },
    { label: '当前焦点', value: selectedTreeNode.value?.name || '全院', meta: '结合资产树快速定位' },
    { label: '入住老人', value: `${bedStats.value.occupied}`, meta: '床位入住中数量' }
  ]
})

const generateWizardTitle = computed(() => {
  if (generateTarget.value === 'buildings') return '批量生成楼栋'
  if (generateTarget.value === 'floors') return '批量生成楼层'
  if (generateTarget.value === 'rooms') return '批量生成房间'
  return '批量生成床位'
})

const generateWizardHint = computed(() => {
  if (generateTarget.value === 'buildings') return '先确认需要新增多少楼栋，再统一生成基础楼栋编码。'
  if (generateTarget.value === 'floors') return '在指定楼栋内补齐楼层，已存在的楼层会自动跳过。'
  if (generateTarget.value === 'rooms') return '在指定楼层内连续补齐房号，并自动配置房间容量和房型。'
  return '根据房间容量补齐缺失床位，不会重复生成已存在的床位。'
})

const generatePreviewText = computed(() => {
  if (generateTarget.value === 'buildings') {
    return `将新增 ${generateForm.buildingCount} 栋楼，名称前缀为“${generateForm.buildingPrefix || '楼栋'}”。`
  }
  if (generateTarget.value === 'floors') {
    return `将在所选楼栋内尝试生成 ${generateForm.floorStart}F - ${generateForm.floorEnd}F，已存在楼层自动跳过。`
  }
  if (generateTarget.value === 'rooms') {
    return `将在所选楼层内尝试补齐 ${String(generateForm.roomStart).padStart(2, '0')} - ${String(generateForm.roomEnd).padStart(2, '0')} 号房，每间 ${generateForm.roomCapacity} 床。`
  }
  return generateForm.bedScope === 'all'
    ? '将扫描全部房间，按房间容量补齐缺失床位。'
    : '将仅扫描当前列表范围内的房间，按房间容量补齐缺失床位。'
})

const buildingRules: FormRules = {
  name: [{ required: true, message: '请输入楼栋名称' }]
}

const floorRules: FormRules = {
  buildingId: [{ required: true, message: '请选择楼栋' }]
}

const roomRules: FormRules = {
  buildingId: [{ required: true, message: '请选择楼栋' }],
  floorId: [{ required: true, message: '请选择楼层' }],
  status: [{ required: true, message: '请选择状态' }]
}

const bedRules: FormRules = {
  roomId: [{ required: true, message: '请选择房间' }],
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
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 180 },
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

function drawerTitle(label: string, mode: DrawerMode) {
  if (mode === 'view') return `查看${label}`
  if (mode === 'edit') return `编辑${label}`
  return `新增${label}`
}

function countTreeStats(nodes: AssetTreeNode[]) {
  const stats = { buildings: 0, floors: 0, rooms: 0, beds: 0 }
  const walk = (items: AssetTreeNode[]) => {
    items.forEach((node) => {
      if (node.type === 'BUILDING') stats.buildings += 1
      if (node.type === 'FLOOR') stats.floors += 1
      if (node.type === 'ROOM') stats.rooms += 1
      if (node.type === 'BED') stats.beds += 1
      if (node.children?.length) walk(node.children)
    })
  }
  walk(nodes)
  return stats
}

function summarizeNode(node?: AssetTreeNode | null) {
  const target = node || undefined
  if (!target) return { floors: 0, rooms: 0, beds: 0 }
  const stats = { floors: 0, rooms: 0, beds: 0 }
  const walk = (item?: AssetTreeNode) => {
    if (!item) return
    if (item.type === 'FLOOR') stats.floors += 1
    if (item.type === 'ROOM') stats.rooms += 1
    if (item.type === 'BED') stats.beds += 1
    item.children?.forEach((child) => walk(child))
  }
  walk(target)
  return stats
}

function formatTreeNodeSummary(node?: AssetTreeNode | null) {
  const stats = summarizeNode(node)
  if (!node) return ''
  if (node.type === 'BUILDING') return `${stats.floors}层 / ${stats.rooms}房 / ${stats.beds}床`
  if (node.type === 'FLOOR') return `${stats.rooms}房 / ${stats.beds}床`
  if (node.type === 'ROOM') return `${stats.beds}床`
  return statusLabel(node.status)
}

function findTreePath(nodes: AssetTreeNode[], id: Id): AssetTreeNode[] {
  const path: AssetTreeNode[] = []
  const dfs = (items: AssetTreeNode[], parents: AssetTreeNode[]): boolean => {
    for (const item of items) {
      const nextPath = [...parents, item]
      if (item.id === id) {
        path.push(...nextPath)
        return true
      }
      if (item.children?.length && dfs(item.children, nextPath)) {
        return true
      }
    }
    return false
  }
  dfs(nodes, [])
  return path
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
  selectedTreeNode.value = data
  selectedTreePath.value = findTreePath(assetTree.value, data.id)
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

async function submitBuilding() {
  if (!buildingFormRef.value) return
  try {
    await buildingFormRef.value.validate()
    buildingSubmitting.value = true
    const payload = {
      ...buildingForm,
      areaName: buildingForm.areaCode ? areaNameByCode.value[buildingForm.areaCode] : undefined,
      remark: stringifyRemarkSlots(buildingRemarkSlots, buildingForm.remark)
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

function openFloor(row?: FloorItem, mode: DrawerMode = row ? 'edit' : 'create') {
  floorDrawerMode.value = mode
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

function openRoom(row?: RoomItem, mode: DrawerMode = row ? 'edit' : 'create') {
  roomDrawerMode.value = mode
  if (row) {
    Object.assign(roomForm, row)
    hydrateRemarkSlots(roomRemarkSlots, row.remark)
    const inferred = inferCapacityByRoomType(row.roomType)
    if (inferred && roomForm.capacity !== inferred) {
      roomForm.capacity = inferred
    }
  } else {
    Object.assign(roomForm, { id: undefined, roomNo: '', buildingId: undefined, floorId: undefined, roomType: undefined, capacity: 1, status: 1, remark: '' })
    hydrateRemarkSlots(roomRemarkSlots, '')
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
    roomForm.remark = stringifyRemarkSlots(roomRemarkSlots, roomForm.remark)
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

function openBed(row?: BedItem, mode: DrawerMode = row ? 'edit' : 'create') {
  bedDrawerMode.value = mode
  if (row) {
    Object.assign(bedForm, row)
  } else {
    Object.assign(bedForm, { id: undefined, roomId: undefined, bedNo: '', bedType: undefined, status: 1 })
  }
  bedOpen.value = true
}

function openBuilding(row?: BuildingItem, mode: DrawerMode = row ? 'edit' : 'create') {
  buildingDrawerMode.value = mode
  if (row) {
    Object.assign(buildingForm, row)
    hydrateRemarkSlots(buildingRemarkSlots, row.remark)
  } else {
    Object.assign(buildingForm, { id: undefined, name: '', code: '', areaCode: undefined, areaName: undefined, status: 1, sortNo: 0, remark: '' })
    hydrateRemarkSlots(buildingRemarkSlots, '')
  }
  buildingOpen.value = true
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

async function toggleBuildingStatus(row: BuildingItem) {
  try {
    const nextStatus = row.status === 1 ? 0 : 1
    await updateBuilding(row.id, { ...row, status: nextStatus })
    await refreshBuildings()
    await loadBuildingList()
  } catch (error: any) {
    message.warning(errorMessage(error, '更新楼栋状态失败'))
  }
}

async function toggleFloorStatus(row: FloorItem) {
  try {
    const nextStatus = row.status === 1 ? 0 : 1
    await updateFloor(row.id, { ...row, status: nextStatus })
    await refreshFloors()
    await loadFloorList()
  } catch (error: any) {
    message.warning(errorMessage(error, '更新楼层状态失败'))
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

function hydrateRemarkSlots(target: Array<{ text: string; visible: boolean }>, raw?: string) {
  target.forEach((item, index) => {
    item.text = ''
    item.visible = index < 2
  })
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    const slots = Array.isArray(parsed?.slots) ? parsed.slots : [parsed?.remark1, parsed?.remark2, parsed?.remark3]
    slots.forEach((slot: any, index: number) => {
      if (index > 2 || !target[index]) return
      if (typeof slot === 'string') {
        target[index].text = slot
        target[index].visible = true
        return
      }
      target[index].text = String(slot?.text || slot?.value || '')
      target[index].visible = slot?.visible !== false
    })
  } catch {
    target[0].text = raw
    target[0].visible = true
  }
}

function stringifyRemarkSlots(target: Array<{ text: string; visible: boolean }>, fallback?: string) {
  const slots = target.map((item) => ({
    text: String(item.text || '').trim(),
    visible: item.visible
  }))
  if (!slots.some((item) => item.text)) {
    return fallback || ''
  }
  return JSON.stringify({ slots })
}

function resolvePublicRemark(raw?: string) {
  if (!raw) return ''
  try {
    const parsed = JSON.parse(raw)
    const slots = Array.isArray(parsed?.slots) ? parsed.slots : [parsed?.remark1, parsed?.remark2, parsed?.remark3]
    return slots
      .map((slot: any) => {
        if (!slot) return ''
        if (typeof slot === 'string') return slot
        if (slot.visible === false) return ''
        return String(slot.text || slot.value || '')
      })
      .filter((item: string) => item)
      .join('；')
  } catch {
    return raw
  }
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

function openGenerateWizard(target: TabKey = activeTab.value) {
  generateTarget.value = target
  generateForm.buildingCount = 1
  generateForm.buildingPrefix = '楼栋'
  generateForm.buildingId = floorQuery.buildingId || roomQuery.buildingId || buildingList.value[0]?.id
  generateForm.floorId = roomQuery.floorId || floorList.value[0]?.id
  generateForm.floorStart = 1
  generateForm.floorEnd = 6
  generateForm.roomStart = 1
  generateForm.roomEnd = 30
  generateForm.roomCapacity = 2
  generateForm.bedType = bedTypeOptions.value[0]?.value
  generateForm.bedScope = 'current'
  generateOpen.value = true
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

async function submitGenerateWizard() {
  generateSubmitting.value = true
  try {
    const preview = await previewResidenceBatchGeneration(buildResidenceBatchRequest())
    if (preview.conflictCount > 0) {
      Modal.warning({
        title: '预览存在冲突，暂不能提交',
        content: formatPreviewSummary(preview)
      })
      return
    }
    await new Promise<void>((resolve, reject) => {
      Modal.confirm({
        title: '请确认批量生成预览',
        content: formatPreviewSummary(preview),
        okText: '确认提交',
        cancelText: '返回调整',
        onOk: async () => {
          try {
            const result = await commitResidenceBatchGeneration(preview.previewToken)
            message.success(
              `批量生成完成：新增 ${result.createdBuildingCount} 栋 / ${result.createdFloorCount} 层 / ${result.createdRoomCount} 间 / ${result.createdBedCount} 床`
            )
            if (result.updatedRoomCount || result.updatedBedCount) {
              message.info(`安全覆盖：房间 ${result.updatedRoomCount}，床位 ${result.updatedBedCount}`)
            }
            generateOpen.value = false
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
            resolve()
          } catch (error) {
            reject(error)
          }
        },
        onCancel: () => reject()
      })
    })
  } catch (error: any) {
    if (!error) return
    message.error(errorMessage(error, '批量生成失败'))
  } finally {
    generateSubmitting.value = false
  }
}

function buildResidenceBatchRequest(): ResidenceBatchGenerationRequest {
  if (generateTarget.value === 'buildings') {
    return {
      mode: 'BUILDING_ONLY',
      strategy: 'FILL_MISSING',
      buildingCount: Number(generateForm.buildingCount || 1),
      buildingStartNo: 1,
      buildingNameStyle: 'NUMERIC_SUFFIX',
      buildingCodePrefix: 'B'
    }
  }
  if (generateTarget.value === 'floors') {
    return {
      mode: 'FLOOR_ONLY',
      strategy: 'FILL_MISSING',
      buildingId: generateForm.buildingId || buildingList.value[0]?.id,
      floorStartNo: Number(generateForm.floorStart || 1),
      floorEndNo: Number(generateForm.floorEnd || 1),
      roomStartNo: 1,
      roomEndNo: 1,
      roomSeqWidth: 2,
      bedsPerRoom: 0,
      roomType: 'ROOM_DOUBLE',
      bedLabelStyle: 'ALPHA',
      floorRules: Array.from({ length: Math.max(Number(generateForm.floorEnd || 1) - Number(generateForm.floorStart || 1) + 1, 0) }).map((_, index) => ({
        floorNo: Number(generateForm.floorStart || 1) + index,
        skipRoomGeneration: true
      }))
    }
  }
  if (generateTarget.value === 'rooms') {
    return {
      mode: 'ROOM_ONLY',
      strategy: 'FILL_MISSING',
      floorId: generateForm.floorId || floorList.value[0]?.id,
      roomStartNo: Number(generateForm.roomStart || 1),
      roomEndNo: Number(generateForm.roomEnd || 1),
      roomSeqWidth: 2,
      roomType: inferRoomTypeByCapacity(Number(generateForm.roomCapacity || 2)) || 'ROOM_DOUBLE',
      bedsPerRoom: Number(generateForm.roomCapacity || 2),
      bedLabelStyle: 'ALPHA',
      defaultBedType: generateForm.bedType || bedTypeOptions.value[0]?.value
    }
  }
  const targetRoomId = generateForm.bedScope === 'all'
    ? undefined
    : rooms.value.length === 1
      ? rooms.value[0]?.id
      : selectedTreeNode.value?.type === 'ROOM'
        ? selectedTreeNode.value.id
        : undefined
  return {
    mode: 'BED_ONLY',
    strategy: 'FILL_MISSING',
    roomId: targetRoomId || roomList.value[0]?.id,
    defaultBedType: generateForm.bedType || bedTypeOptions.value[0]?.value,
    bedLabelStyle: 'ALPHA'
  }
}

function formatPreviewSummary(preview: ResidenceBatchPreviewResponse) {
  const lines = [
    `新增楼栋：${preview.createBuildingCount}`,
    `新增楼层：${preview.createFloorCount}`,
    `新增房间：${preview.createRoomCount}`,
    `新增床位：${preview.createBedCount}`,
    `安全覆盖：${preview.overwriteSafeCount}`,
    `跳过数量：${preview.skipCount}`,
    `冲突数量：${preview.conflictCount}`
  ]
  if (preview.warnings?.length) {
    lines.push(`提示：${preview.warnings.slice(0, 3).join('；')}`)
  }
  if (preview.conflicts?.length) {
    lines.push(`冲突：${preview.conflicts.slice(0, 3).join('；')}`)
  }
  return lines.join('\n')
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
  const targets = selectedBuildings.value
  if (!targets.length) {
    message.warning('请先勾选需要删除的楼栋')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个楼栋吗？将联动删除其下未入住的楼层、房间和床位，请确认影响范围。`, '批量删除楼栋')
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
  const targets = selectedFloors.value
  if (!targets.length) {
    message.warning('请先勾选需要删除的楼层')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个楼层吗？将联动删除其下未入住的房间和床位，请确认影响范围。`, '批量删除楼层')
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
  const targets = selectedRooms.value
  if (!targets.length) {
    message.warning('请先勾选需要删除的房间')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个房间吗？将联动删除其下未入住的床位，请确认影响范围。`, '批量删除房间')
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
  const targets = selected.value
  if (!targets.length) {
    message.warning('请先勾选需要删除的床位')
    return
  }
  try {
    await confirmAction(`确认删除 ${targets.length} 个床位吗？删除后将无法恢复，请确认影响范围。`, '批量删除床位')
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
    await confirmAction('确认删除该楼栋吗？系统会自动删除楼栋下未入住的楼层、房间和床位。', '提示')
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
    await confirmAction('确认删除该楼层吗？系统会自动删除楼层下未入住的房间和床位。', '提示')
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
    await confirmAction('确认删除该房间吗？系统会自动删除房间下未入住的床位。', '提示')
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
  router.push('/logistics/assets/room-state-map')
}

function openElderBedPanorama() {
  router.push('/elder/bed-panorama')
}

function onWorkspaceModeChange(event: any) {
  const mode = event?.target?.value as WorkspaceMode
  if (mode === 'map') {
    openBedMap()
    return
  }
  if (mode === 'panorama') {
    openElderBedPanorama()
    return
  }
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
.bed-management-page {
  --panel-bg: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
  --panel-border: rgba(15, 23, 42, 0.08);
  --panel-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
  --accent: #2563eb;
  --accent-soft: rgba(37, 99, 235, 0.08);
  --text-main: #10203a;
  --text-sub: #5b6b84;
  --surface-soft: #f5f8fc;
}
.workspace-grid {
  align-items: stretch;
}
.asset-tree-panel,
.workspace-hero,
.section-card,
.overview-card {
  border: 1px solid var(--panel-border);
  background: var(--panel-bg);
  box-shadow: var(--panel-shadow);
  border-radius: 20px;
}
.asset-tree-panel {
  min-height: 100%;
}
.tree-panel-head,
.workspace-topbar,
.toolbar-row,
.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
.panel-eyebrow,
.workspace-path,
.overview-label {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-sub);
}
.tree-title,
.workspace-title,
.section-title {
  color: var(--text-main);
  font-weight: 700;
}
.tree-title {
  font-size: 20px;
  margin-top: 4px;
}
.tree-summary,
.workspace-subtitle,
.section-desc,
.overview-meta,
.wizard-hint,
.wizard-preview,
.bootstrap-hint,
.print-text {
  color: var(--text-sub);
  font-size: 13px;
}
.asset-tree {
  margin-top: 18px;
}
.tree-node-card {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
  padding: 6px 0;
}
.tree-node-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.tree-node-type {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--text-sub);
  font-size: 12px;
}
.tree-node-name {
  color: var(--text-main);
  font-weight: 600;
}
.tree-node-meta {
  padding-left: 52px;
  font-size: 12px;
  color: var(--text-sub);
}
.workspace-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.workspace-hero {
  overflow: hidden;
}
.workspace-title {
  margin: 8px 0 6px;
  font-size: 30px;
  line-height: 1.1;
}
.workspace-subtitle {
  margin: 0;
}
.management-tabs {
  margin-top: 16px;
}
.overview-row {
  margin: 0;
}
.overview-card :deep(.ant-card-body) {
  padding: 18px 20px;
}
.overview-value {
  margin-top: 10px;
  color: var(--text-main);
  font-size: 28px;
  font-weight: 700;
}
.workspace-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.section-card :deep(.ant-card-body) {
  padding: 18px 20px;
}
.section-title {
  font-size: 17px;
}
.filter-card :deep(.ant-form-item) {
  margin-bottom: 12px;
}
.search-bar {
  margin-top: 16px;
}
.toolbar-row {
  align-items: center;
  flex-wrap: wrap;
}
.toolbar-main,
.toolbar-side,
.row-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.selection-badge {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--text-sub);
  font-size: 13px;
}
.table-card :deep(.ant-table-wrapper) {
  margin-top: 16px;
}
.row-actions :deep(.ant-btn-link) {
  padding-inline: 4px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
.print-grid {
  display: flex;
  flex-wrap: wrap;
}
.print-item {
  width: 200px;
  margin: 8px;
  text-align: center;
}
.bootstrap-presets {
  margin-bottom: 12px;
}
.wizard-steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}
.wizard-step {
  padding: 10px 12px;
  border-radius: 14px;
  background: var(--surface-soft);
  color: var(--text-sub);
  text-align: center;
  font-size: 13px;
}
.wizard-step.is-active {
  background: var(--accent-soft);
  color: var(--accent);
  font-weight: 600;
}
.wizard-hint {
  margin-bottom: 12px;
}
.wizard-preview {
  margin-top: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--surface-soft);
}
@media (max-width: 1200px) {
  .workspace-topbar {
    flex-direction: column;
  }
}
@media (max-width: 768px) {
  .workspace-title {
    font-size: 24px;
  }
  .tree-panel-head,
  .toolbar-row,
  .section-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
