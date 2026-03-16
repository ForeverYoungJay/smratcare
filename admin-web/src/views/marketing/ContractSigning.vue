<template>
  <PageContainer :title="pageTitle" :sub-title="pageSubTitle">
    <MarketingQuickNav />
    <a-card class="card-elevated" :bordered="false">
      <div class="board-head">
        <span class="board-head-title">流程阶段看板</span>
        <a-space size="small">
          <span class="board-head-meta">更新时间：{{ stageBoardGeneratedAtText }}</span>
          <a-button size="small" @click="fetchBoardSummary">刷新看板</a-button>
        </a-space>
      </div>
      <a-row :gutter="12">
        <a-col :xs="12" :md="6">
          <div class="board-item">
            <div class="board-title">待评估 <a-badge :count="stageBoard.pendingAssessmentOverdue" color="#ff4d4f" /></div>
            <div class="board-value">{{ stageBoard.pendingAssessment }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :md="6">
          <div class="board-item">
            <div class="board-title">待办理入住</div>
            <div class="board-value">{{ stageBoard.pendingBedSelect }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :md="6">
          <div class="board-item">
            <div class="board-title">待签署 <a-badge :count="stageBoard.pendingSignOverdue" color="#ff4d4f" /></div>
            <div class="board-value">{{ stageBoard.pendingSign }}</div>
          </div>
        </a-col>
        <a-col :xs="12" :md="6">
          <div class="board-item">
            <div class="board-title">已签署</div>
            <div class="board-value">{{ stageBoard.signed }}</div>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="合同编号">
          <a-input v-model:value="query.contractNo" placeholder="请输入 合同编号" allow-clear />
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="query.elderName" placeholder="请输入 姓名" allow-clear />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
        </a-form-item>
        <a-form-item label="营销人员">
          <a-input v-model:value="query.marketerName" placeholder="请输入 营销人员" allow-clear />
        </a-form-item>
        <a-form-item label="优惠政策">
          <a-select
            v-model:value="query.orgName"
            style="width: 220px"
            allow-clear
            show-search
            :loading="policyLoading"
            :options="policyOptions"
            placeholder="筛选运营政策"
            :filter-option="policyFilterOption"
          />
        </a-form-item>
        <a-form-item label="流程阶段">
          <a-select v-model:value="query.flowStage" style="width: 150px" allow-clear>
            <a-select-option value="PENDING_ASSESSMENT">待评估</a-select-option>
            <a-select-option value="PENDING_BED_SELECT">待办理入住</a-select-option>
            <a-select-option value="PENDING_SIGN">待签署</a-select-option>
            <a-select-option value="SIGNED">已签署</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="仅看我负责">
          <a-switch v-model:checked="onlyMineDept" @change="onMineSwitchChange" />
        </a-form-item>
        <a-form-item label="责任部门">
          <a-segmented
            v-model:value="mineDept"
            :options="mineDeptOptions"
            :disabled="!onlyMineDept"
            @change="onMineDeptChange"
          />
        </a-form-item>
        <a-form-item label="只看超时">
          <a-switch v-model:checked="onlyOverdue" @change="onOverdueSwitchChange" />
        </a-form-item>
        <a-form-item label="超时排序">
          <a-switch v-model:checked="sortByOverdue" @change="onSortSwitchChange" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runSearch">搜 索</a-button>
            <a-button @click="reset">清 空</a-button>
            <a-button @click="copySearchLink">复制筛选链接</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <FlowGuardBar
        :title="'合同流程守卫'"
        :subject="flowGuardSubject"
        :stage-text="flowGuardStageText"
        :stage-color="flowGuardStageColor"
        :steps="flowSteps"
        :current-index="flowGuardCurrentIndex"
        :blockers="flowGuardBlockers"
        :hint="flowGuardHint"
        @action="handleFlowGuardAction"
      />
      <MarketingListToolbar :selected-count="selectedCount" tip="支持批量删除与流程推进">
        <a-space>
          <a-button v-if="!isSignedMode" type="primary" @click="openForm()">新增合同</a-button>
          <a-button :disabled="!hasSingleSelection" @click="viewSelected">查看</a-button>
          <a-button :disabled="!hasSingleSelection" @click="editSelected">编辑</a-button>
          <a-button :disabled="!hasSingleSelection" @click="openAttachmentSelected">附件</a-button>
          <a-button :disabled="!hasSingleSelection" @click="admissionSelected">入住办理</a-button>
          <a-button :disabled="!hasSingleSelection" @click="finalizeSelected">最终签署</a-button>
          <a-button :disabled="selectedCount === 0" danger @click="batchDelete">删除</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table
        :data-source="tableRows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ x: 2400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'flowStage'">
            <a-tag :color="flowStageColor(normalizedFlowStage(record))">{{ flowStageText(normalizedFlowStage(record)) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'orgName'">
            <a-space wrap>
              <a-tag v-for="item in parsePolicyValues(record.orgName)" :key="item" color="blue">{{ item }}</a-tag>
              <span v-if="!parsePolicyValues(record.orgName).length">-</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'slaWarning'">
            <a-tag v-if="getOverdueLevel(record) === 'high'" color="red">{{ overdueText(record) }}</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'currentOwnerDept'">
            <a-tag>{{ ownerDeptText(normalizedOwnerDept(record)) }}</a-tag>
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

    <a-modal
      v-model:open="open"
      :title="contractModalTitle"
      width="1120px"
      :confirm-loading="submitting"
      :footer="contractViewMode ? null : undefined"
      @ok="onContractModalOk"
    >
      <a-spin :spinning="elderSnapshotLoading">
        <a-alert
          type="info"
          show-icon
          style="margin-bottom: 16px"
          :message="contractFormGuideText"
        />
        <div class="contract-workbench">
          <div class="contract-workbench-head">
            <div>
              <div class="contract-workbench-kicker">{{ contractViewMode ? '合同详情' : (isCreateContractMode ? '新建合同' : '编辑合同') }}</div>
              <div class="contract-workbench-title">{{ contractViewMode ? '合同与长者资料概览' : '一屏完成合同、长者、家属与疾病信息' }}</div>
            </div>
            <div class="contract-workbench-badges">
              <a-tag color="blue">流程 {{ flowStageText((form.flowStage as any) || 'PENDING_ASSESSMENT') }}</a-tag>
              <a-tag color="green">{{ form.contractStatus || '待评估' }}</a-tag>
            </div>
          </div>
          <div v-if="contractViewMode" class="contract-readonly">
            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">合同摘要</div>
                <div class="contract-section-desc">聚焦签约状态、营销归属与当前推进结果。</div>
              </div>
              <div class="readonly-grid readonly-grid-4">
                <div class="readonly-item">
                  <span class="readonly-label">合同编号</span>
                  <strong class="readonly-value">{{ form.contractNo || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">合同状态</span>
                  <strong class="readonly-value">{{ form.contractStatus || '待评估' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">流程阶段</span>
                  <strong class="readonly-value">{{ flowStageText((form.flowStage as any) || 'PENDING_ASSESSMENT') }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">营销人员</span>
                  <strong class="readonly-value">{{ form.marketerName || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">签约日期</span>
                  <strong class="readonly-value">{{ form.contractSignedAt || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">合同有效期止</span>
                  <strong class="readonly-value">{{ form.contractExpiryDate || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">签约房号</span>
                  <strong class="readonly-value">{{ form.reservationRoomNo || '-' }}</strong>
                </div>
                <div class="readonly-item readonly-policies">
                  <span class="readonly-label">优惠政策</span>
                  <div class="readonly-tags">
                    <a-tag v-for="item in selectedPolicyValues" :key="item" color="blue">{{ item }}</a-tag>
                    <span v-if="!selectedPolicyValues.length" class="readonly-empty">未配置</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">长者信息</div>
                <div class="contract-section-desc">身份、联系与照护信息统一查看，避免来回切换页面。</div>
              </div>
              <div class="readonly-grid readonly-grid-4">
                <div class="readonly-item">
                  <span class="readonly-label">姓名</span>
                  <strong class="readonly-value">{{ form.elderName || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">联系电话</span>
                  <strong class="readonly-value">{{ form.elderPhone || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">身份证号</span>
                  <strong class="readonly-value">{{ form.idCardNo || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">性别 / 年龄</span>
                  <strong class="readonly-value">{{ genderText(form.gender) }} / {{ form.age ?? '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">生日</span>
                  <strong class="readonly-value">{{ formDerivedBirthDate || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">护理等级</span>
                  <strong class="readonly-value">{{ elderFormExtra.careLevel || '-' }}</strong>
                </div>
                <div class="readonly-item">
                  <span class="readonly-label">风险预担</span>
                  <strong class="readonly-value">{{ riskPrecommitLabel(elderFormExtra.riskPrecommit) }}</strong>
                </div>
                <div class="readonly-item readonly-address">
                  <span class="readonly-label">家庭地址</span>
                  <strong class="readonly-value">{{ form.homeAddress || '-' }}</strong>
                </div>
              </div>
            </div>

            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">家属与疾病</div>
                <div class="contract-section-desc">家属联系人与基础疾病集中概览。</div>
              </div>
              <div class="readonly-family-list">
                <div v-for="item in familyDraftRows" :key="item.key" class="family-card">
                  <div class="family-card-head">
                    <strong>{{ item.realName || '未命名家属' }}</strong>
                    <a-tag :color="item.isPrimary ? 'green' : 'default'">{{ item.isPrimary ? '主联系人' : '联系人' }}</a-tag>
                  </div>
                  <div class="family-card-meta">{{ item.relation || '关系未填' }}</div>
                  <div class="family-card-fields">
                    <span>手机号 {{ item.phone || '-' }}</span>
                    <span>身份证 {{ item.idCardNo || '-' }}</span>
                  </div>
                </div>
                <a-empty v-if="!familyDraftRows.length" :image="null" description="暂无家属信息" />
              </div>
              <div class="readonly-disease-block">
                <span class="readonly-label">基础疾病</span>
                <div class="readonly-tags">
                  <a-tag v-for="item in selectedDiseaseLabels" :key="item" color="blue">{{ item }}</a-tag>
                  <span v-if="!selectedDiseaseLabels.length" class="readonly-empty">暂无疾病信息</span>
                </div>
              </div>
            </div>

            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">补充备注</div>
              </div>
              <div class="readonly-remark">{{ form.remark || '暂无补充备注' }}</div>
            </div>
          </div>
          <a-form v-else ref="formRef" :model="form" :rules="rules" layout="vertical">
            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">合同信息</div>
                <div class="contract-section-desc">先确认合同节奏，再补齐营销与签约字段。</div>
              </div>
              <a-row :gutter="[16, 4]">
                <a-col :xs="24" :md="8">
                  <a-form-item label="合同编号" name="contractNo">
                    <a-input :value="form.contractNo" disabled placeholder="保存后后端自动生成（gfyy+年月日+编号）" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="合同状态">
                    <a-input :value="form.contractStatus || '待评估'" disabled />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="流程阶段">
                    <a-input :value="flowStageText((form.flowStage as any) || 'PENDING_ASSESSMENT')" disabled />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="签约日期">
                    <a-date-picker v-model:value="form.contractSignedAt" value-format="YYYY-MM-DD HH:mm:ss" show-time style="width: 100%" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="合同有效期止">
                    <a-date-picker v-model:value="form.contractExpiryDate" value-format="YYYY-MM-DD" style="width: 100%" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="营销人员">
                    <a-input v-model:value="form.marketerName" placeholder="请输入负责营销人员姓名" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="优惠政策（最多 2 条）">
                    <a-select
                      v-model:value="selectedPolicyValues"
                      mode="multiple"
                      :max-tag-count="2"
                      allow-clear
                      show-search
                      :loading="policyLoading"
                      :options="policyOptions"
                      placeholder="请选择运营政策（最多2条）"
                      :filter-option="policyFilterOption"
                      @change="onPolicySelectionChange"
                    />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="签约房号">
                    <a-input
                      v-model:value="form.reservationRoomNo"
                      :disabled="!form.id"
                      :placeholder="form.id ? '入住办理后自动回填，可手动修正' : '新增合同无需填写，入住办理时自动选择'"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </div>

            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">长者身份信息</div>
                <div class="contract-section-desc">身份证、联系方式、地址集中录入，自动识别生日与年龄。</div>
              </div>
              <a-row :gutter="[16, 4]">
                <a-col :xs="24" :md="8">
                  <a-form-item name="elderName">
                    <template #label><span class="required-label">姓名</span></template>
                    <a-input ref="elderNameInputRef" v-model:value="form.elderName" placeholder="请输入长者姓名" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item name="elderPhone">
                    <template #label><span class="required-label">联系电话</span></template>
                    <a-input v-model:value="form.elderPhone" placeholder="请输入长者联系电话" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item name="idCardNo">
                    <template #label><span class="required-label">身份证号</span></template>
                    <a-input v-model:value="form.idCardNo" placeholder="自动识别生日/年龄" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="6">
                  <a-form-item name="gender">
                    <template #label><span class="required-label">性别</span></template>
                    <a-select v-model:value="form.gender" allow-clear>
                      <a-select-option :value="1">男</a-select-option>
                      <a-select-option :value="2">女</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="6">
                  <a-form-item label="年龄">
                    <a-input-number v-model:value="form.age" :min="0" :max="120" style="width: 100%" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="6">
                  <a-form-item label="生日">
                    <a-input :value="formDerivedBirthDate || '-'" readonly />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="6">
                  <a-form-item label="身份证识别">
                    <a-input :value="formDerivedBirthDate ? '已识别' : '未识别'" readonly />
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <a-form-item name="homeAddress">
                    <template #label><span class="required-label">家庭地址</span></template>
                    <a-input v-model:value="form.homeAddress" placeholder="请输入长者家庭住址" />
                  </a-form-item>
                </a-col>
              </a-row>
            </div>

            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">照护资料</div>
                <div class="contract-section-desc">护理等级、风险预担与疾病信息放在一起，便于评估和入住衔接。</div>
              </div>
              <a-row :gutter="[16, 4]">
                <a-col :xs="24" :md="12">
                  <a-form-item label="护理等级">
                    <a-input v-model:value="elderFormExtra.careLevel" placeholder="如：一级护理/二级护理" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="风险预担">
                    <a-select v-model:value="elderFormExtra.riskPrecommit" allow-clear placeholder="请选择风险处置策略">
                      <a-select-option value="RESCUE_FIRST">第一时间抢救</a-select-option>
                      <a-select-option value="NOTIFY_FAMILY_FIRST">第一时间通知家属</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="24">
                  <a-form-item label="基础疾病信息">
                    <a-select
                      v-model:value="selectedDiseaseIds"
                      mode="multiple"
                      allow-clear
                      show-search
                      :options="diseaseOptions"
                      placeholder="请选择基础疾病"
                      :disabled="contractViewMode"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </div>

            <div class="contract-section">
              <div class="contract-section-head family-head">
                <div>
                  <div class="contract-section-title required-section-title">家属信息</div>
                  <div class="contract-section-desc">常用联系人集中录入，手机号与身份证号要求完整。</div>
                </div>
                <a-button size="small" type="dashed" @click="addFamilyDraftRow">新增家属</a-button>
              </div>
              <div class="family-editor-list">
                <div v-for="item in familyDraftRows" :key="item.key" class="family-card family-card-editable">
                  <div class="family-card-toolbar">
                    <a-tag :color="item.isPrimary ? 'green' : 'default'">{{ item.isPrimary ? '主联系人' : '普通联系人' }}</a-tag>
                    <a-button type="link" danger @click="removeFamilyDraftRow(item.key)">删除</a-button>
                  </div>
                  <a-row :gutter="[12, 4]">
                    <a-col :xs="24" :md="10">
                      <a-form-item class="compact-form-item">
                        <template #label><span class="required-label">姓名</span></template>
                        <a-input v-model:value="item.realName" placeholder="姓名" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="8">
                      <a-form-item label="关系" class="compact-form-item">
                        <a-select
                          v-model:value="item.relation"
                          :options="familyRelationOptions"
                          allow-clear
                          placeholder="请选择关系"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="6">
                      <a-form-item label="主联系人" class="compact-form-item">
                        <a-switch v-model:checked="item.isPrimary" @change="() => setPrimaryFamilyDraftRow(item.key)" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="12">
                      <a-form-item class="compact-form-item">
                        <template #label><span class="required-label">手机号</span></template>
                        <a-input v-model:value="item.phone" placeholder="手机号" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="12">
                      <a-form-item class="compact-form-item">
                        <template #label><span class="required-label">身份证号</span></template>
                        <a-input v-model:value="item.idCardNo" placeholder="身份证号" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                </div>
                <a-empty v-if="!familyDraftRows.length" :image="null" description="暂无家属信息，可直接新增录入" />
              </div>
            </div>

            <div class="contract-section">
              <div class="contract-section-head">
                <div class="contract-section-title">补充备注</div>
                <div class="contract-section-desc">记录签约背景、特殊照护提示或交接说明。</div>
              </div>
              <a-form-item label="备注">
                <a-input-text-area
                  v-model:value="form.remark"
                  :rows="4"
                  placeholder="可填写签约背景、特殊照护提示、补充说明等"
                />
              </a-form-item>
            </div>
          </a-form>
        </div>
      </a-spin>
    </a-modal>

    <a-modal v-model:open="attachmentOpen" title="合同附件（病历/医保/户口等）" width="860px" :footer="null">
      <a-space style="margin-bottom: 12px;" wrap>
        <a-select v-model:value="attachmentType" style="width: 180px">
          <a-select-option value="MEDICAL_RECORD">病历资料</a-select-option>
          <a-select-option value="MEDICAL_INSURANCE">医保复印件</a-select-option>
          <a-select-option value="HOUSEHOLD">户口复印件</a-select-option>
          <a-select-option value="CONTRACT">合同附件</a-select-option>
          <a-select-option value="OTHER">其他附件</a-select-option>
        </a-select>
        <a-upload
          :show-upload-list="false"
          :custom-request="handleUpload"
          :before-upload="beforeUpload"
          :accept="uploadAccept"
        >
          <a-button type="primary" :loading="attachmentSubmitting">上传附件</a-button>
        </a-upload>
      </a-space>
      <a-table :data-source="attachments" :columns="attachmentColumns" :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preview'">
            <a-image
              v-if="isImageAttachment(record)"
              :src="record.fileUrl"
              :width="40"
              :height="40"
              style="object-fit: cover; border-radius: 4px;"
            />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'remark'">
            {{ attachmentTypeLabel(record.attachmentType || record.remark) }}
          </template>
          <template v-else-if="column.key === 'fileUrl'">
            <a :href="record.fileUrl" target="_blank">{{ record.fileName }}</a>
          </template>
          <template v-else-if="column.key === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" danger @click="removeAttachment(record.id)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal v-model:open="finalizeOpen" title="最终签署" width="620px" :confirm-loading="finalizing" @ok="submitFinalize">
      <a-form layout="vertical" :model="finalizeForm">
        <a-alert
          type="info"
          show-icon
          style="margin-bottom: 12px;"
          :message="`当前合同：${finalizeForm.contractNo || '-'}；长者：${finalizeForm.elderName || '-'}。`"
        />
        <a-form-item label="签署备注">
          <a-input v-model:value="finalizeForm.remark" placeholder="可填写最终签署说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import FlowGuardBar from '../../components/FlowGuardBar.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import {
  batchDeleteContracts,
  createContractAttachment,
  createCrmContract,
  deleteContractAttachment,
  deleteCrmContract,
  finalizeContract,
  getContractAssessmentOverview,
  getContractAttachments,
  getContractPage,
  getContractStageSummary,
  getMarketingPlanList,
  moveContractToBedSelect,
  moveContractToPendingSign,
  updateCrmContract,
  uploadMarketingFile
} from '../../api/marketing'
import { bindFamily, createElder, getElderDetail, getElderDiseases, getElderPage, updateElder, updateElderDiseases } from '../../api/elder'
import { getAdmissionRecords } from '../../api/elderLifecycle'
import { getFamilyRelations, upsertFamilyUser } from '../../api/family'
import { getDiseaseList } from '../../api/store'
import type { ContractAttachmentItem, CrmContractItem, ElderItem, Id, MarketingPlanItem, PageResult } from '../../types'

const router = useRouter()
const route = useRoute()
const props = withDefaults(defineProps<{
  statusPreset?: 'pending_sign' | 'signed' | 'pending_assessment' | 'pending_bed_select' | ''
  title?: string
  subTitle?: string
  disableDefaultFlowStagePreset?: boolean
}>(), {
  statusPreset: '',
  title: '合同签约',
  subTitle: '营销建合同 -> 长者管理入住评估 -> 入住办理 -> 最终签署',
  disableDefaultFlowStagePreset: false
})
const pageTitle = computed(() => props.title || '合同签约')
const pageSubTitle = computed(() => props.subTitle || '营销建合同 -> 长者管理入住评估 -> 入住办理 -> 最终签署')
const resolvedStatusPreset = computed(() => String(props.statusPreset || route.query.status || '').trim())
const isSignedMode = computed(() => resolvedStatusPreset.value === 'signed')
const loading = ref(false)
const submitting = ref(false)
const open = ref(false)
const contractViewMode = ref(false)
const formRef = ref<FormInstance>()
const elderNameInputRef = ref<any>()
const rows = ref<CrmContractItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<string[]>([])
const selectedRows = ref<CrmContractItem[]>([])
const onlyMineDept = ref(false)
const mineDept = ref<'MARKETING' | 'ASSESSMENT'>('MARKETING')
const onlyOverdue = ref(false)
const sortByOverdue = ref(false)
const policyOptions = ref<Array<{ label: string; value: string }>>([])
const policyLoading = ref(false)

const stageBoard = reactive({
  pendingAssessment: 0,
  pendingBedSelect: 0,
  pendingSign: 0,
  signed: 0,
  pendingAssessmentOverdue: 0,
  pendingSignOverdue: 0
})
const stageBoardGeneratedAt = ref('')
const skipNextSearchRouteWatch = ref(false)
const searchRouteSignature = ref('')
const SEARCH_ROUTE_KEYS = [
  'contractNo',
  'elderName',
  'elderPhone',
  'marketerName',
  'orgName',
  'flowStage',
  'pageNo',
  'pageSize',
  'onlyMine',
  'mineDept',
  'onlyOverdue',
  'sortByOverdue'
]
const stageBoardGeneratedAtText = computed(() =>
  stageBoardGeneratedAt.value ? dayjs(stageBoardGeneratedAt.value).format('MM-DD HH:mm:ss') : '-'
)

const mineDeptOptions = [
  { label: '营销部', value: 'MARKETING' },
  { label: '评估部', value: 'ASSESSMENT' }
]

const query = reactive({
  contractNo: '',
  elderName: '',
  elderPhone: '',
  marketerName: '',
  orgName: undefined as string | undefined,
  flowStage: undefined as CrmContractItem['flowStage'] | undefined,
  pageNo: 1,
  pageSize: 10
})

function applyStatusPreset() {
  const statusPreset = resolvedStatusPreset.value
  if (props.disableDefaultFlowStagePreset && statusPreset === 'pending_sign') {
    query.flowStage = undefined
    return
  }
  if (statusPreset === 'pending_sign') {
    query.flowStage = 'PENDING_SIGN'
    return
  }
  if (statusPreset === 'signed') {
    query.flowStage = 'SIGNED'
    return
  }
  if (statusPreset === 'pending_assessment') {
    query.flowStage = 'PENDING_ASSESSMENT'
    return
  }
  if (statusPreset === 'pending_bed_select') {
    query.flowStage = 'PENDING_BED_SELECT'
    return
  }
  query.flowStage = undefined
}

function applyAttachmentRouteFilter() {
  const openAttachmentFlag = String(route.query.openAttachment || '').trim() === '1'
  if (!openAttachmentFlag) return
  const contractNo = String(route.query.contractNo || '').trim()
  const elderName = String(route.query.elderName || '').trim()
  if (contractNo) {
    query.contractNo = contractNo
  } else if (elderName) {
    query.elderName = elderName
  }
}

function parseFlag(value: unknown) {
  const text = firstQueryText(value).toLowerCase()
  return text === '1' || text === 'true' || text === 'yes'
}

function firstQueryText(value: unknown) {
  if (Array.isArray(value)) {
    return firstQueryText(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

function normalizeRouteQueryMap(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildSearchRouteSignature(source: Record<string, unknown>) {
  return SEARCH_ROUTE_KEYS.map((key) => `${key}:${firstQueryText(source[key])}`).join('|')
}

function resetSearchFilters() {
  query.contractNo = ''
  query.elderName = ''
  query.elderPhone = ''
  query.marketerName = ''
  query.orgName = undefined
  query.pageNo = 1
  query.pageSize = 10
  onlyMineDept.value = false
  mineDept.value = 'MARKETING'
  onlyOverdue.value = false
  sortByOverdue.value = false
}

function applyQueryRouteFilter() {
  resetSearchFilters()
  const contractNo = firstQueryText(route.query.contractNo)
  const elderName = firstQueryText(route.query.elderName)
  const elderPhone = firstQueryText(route.query.elderPhone)
  const marketerName = firstQueryText(route.query.marketerName)
  const orgName = firstQueryText(route.query.orgName)
  const flowStage = firstQueryText(route.query.flowStage) as CrmContractItem['flowStage']
  const pageNo = Number(firstQueryText(route.query.pageNo))
  const pageSize = Number(firstQueryText(route.query.pageSize))
  if (contractNo) query.contractNo = contractNo
  if (elderName) query.elderName = elderName
  if (elderPhone) query.elderPhone = elderPhone
  if (marketerName) query.marketerName = marketerName
  if (orgName) query.orgName = orgName
  if (flowStage === 'PENDING_ASSESSMENT' || flowStage === 'PENDING_BED_SELECT' || flowStage === 'PENDING_SIGN' || flowStage === 'SIGNED') {
    query.flowStage = flowStage
  }
  if (Number.isFinite(pageNo) && pageNo > 0) query.pageNo = pageNo
  if (Number.isFinite(pageSize) && pageSize > 0) query.pageSize = pageSize
  onlyMineDept.value = parseFlag(route.query.onlyMine)
  onlyOverdue.value = parseFlag(route.query.onlyOverdue)
  sortByOverdue.value = parseFlag(route.query.sortByOverdue)
  const dept = firstQueryText(route.query.mineDept).toUpperCase()
  if (dept === 'ASSESSMENT' || dept === 'MARKETING') {
    mineDept.value = dept
  }
}

const form = reactive<Partial<CrmContractItem>>({})
const isCreateContractMode = computed(() => !form.id)
const contractModalTitle = computed(() => {
  if (contractViewMode.value) return '查看合同'
  return form.id ? '编辑合同' : '新增合同'
})
const contractFormGuideText = computed(() => {
  if (contractViewMode.value) {
    return '当前为只读查看模式，可切换分区查看合同详情。'
  }
  if (isCreateContractMode.value) {
    return '新建合同默认进入“待评估”阶段；仅“最终签署”后，该长者才会进入已签约列表。'
  }
  return '编辑合同建议按分区逐步检查，避免遗漏长者身份与家属信息。'
})
const formDerivedBirthDate = ref('')
const selectedPolicyValues = ref<string[]>([])
async function validateElderPhone(_rule: unknown, value?: string) {
  const text = String(value || '').trim()
  if (!text) {
    return Promise.reject(new Error('请输入长者联系电话'))
  }
  if (!/^1\d{10}$/.test(text)) {
    return Promise.reject(new Error('长者联系电话格式不正确'))
  }
  return Promise.resolve()
}

async function validateElderIdCardNo(_rule: unknown, value?: string) {
  const text = String(value || '').trim()
  if (!text) {
    return Promise.reject(new Error('请输入长者身份证号'))
  }
  if (!/^(\d{15}|\d{17}[\dXx])$/.test(text)) {
    return Promise.reject(new Error('长者身份证号格式不正确'))
  }
  return Promise.resolve()
}

const rules: FormRules = {
  elderName: [{ required: true, message: '请输入长者姓名' }],
  elderPhone: [{ validator: validateElderPhone, trigger: 'blur' }],
  idCardNo: [{ validator: validateElderIdCardNo, trigger: 'blur' }],
  gender: [{ required: true, message: '请选择长者性别', trigger: 'change' }],
  homeAddress: [{ required: true, message: '请输入长者家庭地址', trigger: 'blur' }]
}
const elderFormExtra = reactive({
  careLevel: '',
  riskPrecommit: undefined as 'RESCUE_FIRST' | 'NOTIFY_FAMILY_FIRST' | undefined
})

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>, rows: CrmContractItem[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
    selectedRows.value = rows
  }
}))
const selectedCount = computed(() => selectedRowKeys.value.length)
const hasSingleSelection = computed(() => selectedCount.value === 1)
const flowSteps = ['待评估', '待办理入住', '待签署', '已签署']
const selectedContractForFlow = computed(() => {
  if (selectedRowKeys.value.length !== 1) return null
  const key = selectedRowKeys.value[0]
  return (
    selectedRows.value.find((item) => sameId(item.id, key)) ||
    tableRows.value.find((item) => sameId(item.id, key)) ||
    rows.value.find((item) => sameId(item.id, key)) ||
    null
  )
})

const flowGuardCurrentIndex = computed(() => {
  const record = selectedContractForFlow.value
  if (!record) return 0
  const stage = normalizedFlowStage(record)
  if (stage === 'PENDING_ASSESSMENT') return 0
  if (stage === 'PENDING_BED_SELECT') return 1
  if (stage === 'PENDING_SIGN') return 2
  return 3
})
const flowGuardStageText = computed(() => {
  const record = selectedContractForFlow.value
  if (!record) return '未选择合同'
  return flowStageText(normalizedFlowStage(record))
})
const flowGuardStageColor = computed(() => {
  const record = selectedContractForFlow.value
  if (!record) return 'default'
  return flowStageColor(normalizedFlowStage(record))
})
const flowGuardSubject = computed(() => {
  const record = selectedContractForFlow.value
  if (!record) return '请先在表格中勾选 1 条合同查看流程状态'
  return `合同号 ${record.contractNo || '-'} / 长者 ${record.elderName || '-'}`
})
const flowGuardBlockers = computed(() => {
  const record = selectedContractForFlow.value
  if (!record) return [{ code: 'G001', text: '未选择合同，请先勾选 1 条合同', actionLabel: '定位合同', actionKey: 'focus-row' }]
  const blockers: Array<{ code: string; text: string; actionLabel?: string; actionKey?: string }> = []
  if (record.status === 'VOID') blockers.push({ code: 'G401', text: '合同已作废，不可继续办理' })
  if (!record.contractNo) blockers.push({ code: 'G101', text: '合同号未生成', actionLabel: '编辑合同', actionKey: 'edit' })
  const stage = normalizedFlowStage(record)
  if (stage === 'PENDING_ASSESSMENT') {
    blockers.push({ code: 'G201', text: '需先完成入住评估', actionLabel: '去评估', actionKey: 'go-assessment' })
  }
  if (stage === 'PENDING_BED_SELECT') {
    blockers.push({ code: 'G202', text: '需先办理入住并完成选床', actionLabel: '去入住办理', actionKey: 'go-admission' })
  }
  return blockers
})
const flowGuardHint = computed(() => {
  const record = selectedContractForFlow.value
  if (!record) return ''
  if (record.status === 'VOID') return ''
  const stage = normalizedFlowStage(record)
  if (stage === 'PENDING_SIGN') return '当前可执行最终签署'
  if (stage === 'SIGNED') return '合同流程已闭环完成'
  return '请按流程完成上一环节后继续'
})

function handleFlowGuardAction(item: { actionKey?: string }) {
  const record = selectedContractForFlow.value
  if (!record) return
  if (item.actionKey === 'edit') {
    openForm(record)
    return
  }
  if (item.actionKey === 'go-assessment') {
    const query: Record<string, string> = { autoOpen: '1', mode: 'new' }
    if (record.leadId) query.leadId = String(record.leadId)
    if (record.contractNo) query.contractNo = record.contractNo
    if (record.elderName) query.elderName = record.elderName
    router.push({ path: '/elder/assessment/ability/admission', query })
    return
  }
  if (item.actionKey === 'go-admission') {
    admissionSelected()
  }
}

function sameId(left: Id | undefined, right: Id | undefined) {
  if (left == null || right == null) return false
  return String(left) === String(right)
}

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '流程阶段', dataIndex: 'flowStage', key: 'flowStage', width: 120 },
  { title: '当前责任部门', dataIndex: 'currentOwnerDept', key: 'currentOwnerDept', width: 130 },
  { title: '签约房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 140 },
  { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
  { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
  { title: '优惠政策', dataIndex: 'orgName', key: 'orgName', width: 220 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 130 },
  { title: '超时预警', dataIndex: 'slaWarning', key: 'slaWarning', width: 150 }
]

const attachmentOpen = ref(false)
const attachmentSubmitting = ref(false)
const attachmentType = ref<'MEDICAL_RECORD' | 'MEDICAL_INSURANCE' | 'HOUSEHOLD' | 'CONTRACT' | 'OTHER'>('CONTRACT')
const currentAttachmentLead = ref<CrmContractItem>()
const attachments = ref<ContractAttachmentItem[]>([])
const autoAttachmentHandledKey = ref('')
const autoEditHandledKey = ref('')
const uploadAccept = '.jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip'
const attachmentColumns = [
  { title: '预览', dataIndex: 'preview', key: 'preview', width: 80 },
  { title: '类型', dataIndex: 'remark', key: 'remark', width: 120 },
  { title: '文件名', dataIndex: 'fileName', key: 'fileName', width: 200 },
  { title: '文件链接', dataIndex: 'fileUrl', key: 'fileUrl', width: 280 },
  { title: '文件类型', dataIndex: 'fileType', key: 'fileType', width: 120 },
  { title: '文件大小', dataIndex: 'fileSize', key: 'fileSize', width: 100 },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'operation', width: 80 }
]
const familyRelationOptions = [
  { label: '配偶', value: '配偶' },
  { label: '子女', value: '子女' },
  { label: '孙辈', value: '孙辈' },
  { label: '兄弟姐妹', value: '兄弟姐妹' },
  { label: '监护人', value: '监护人' },
  { label: '亲属', value: '亲属' },
  { label: '朋友', value: '朋友' },
  { label: '其他', value: '其他' }
]
const elderSnapshotLoading = ref(false)
const familyDraftRows = ref<Array<{
  key: string
  realName: string
  relation: string
  phone: string
  idCardNo: string
  isPrimary: boolean
  familyUserId?: string
}>>([])
const selectedDiseaseIds = ref<number[]>([])
const diseaseOptions = ref<Array<{ label: string; value: number }>>([])
const selectedDiseaseLabels = computed(() => {
  const optionMap = new Map(diseaseOptions.value.map((item) => [Number(item.value), item.label]))
  return selectedDiseaseIds.value
    .map((item) => optionMap.get(Number(item)) || `疾病#${item}`)
    .filter((item) => String(item || '').trim())
})

const finalizeOpen = ref(false)
const finalizing = ref(false)
const finalizeLead = ref<CrmContractItem>()
const finalizeReadyMap = ref<Record<number, boolean>>({})
const finalizeCheckingMap = ref<Record<number, boolean>>({})
const assessmentReadyMap = ref<Record<number, boolean>>({})
const assessmentCheckingMap = ref<Record<number, boolean>>({})
const finalizeForm = reactive({
  contractNo: '',
  elderName: '',
  elderId: '' as Id | '',
  remark: ''
})

const tableRows = computed(() => rows.value)
const displayTotal = computed(() => total.value)

function normalizedFlowStage(record: CrmContractItem): CrmContractItem['flowStage'] {
  return (record.flowStage || 'PENDING_ASSESSMENT') as CrmContractItem['flowStage']
}

function normalizedOwnerDept(record: CrmContractItem): CrmContractItem['currentOwnerDept'] {
  if (record.currentOwnerDept) return record.currentOwnerDept
  return normalizedFlowStage(record) === 'PENDING_ASSESSMENT' ? 'ASSESSMENT' : 'MARKETING'
}

function flowStageText(stage?: CrmContractItem['flowStage']) {
  if (stage === 'PENDING_ASSESSMENT') return '待评估'
  if (stage === 'PENDING_BED_SELECT') return '待办理入住'
  if (stage === 'PENDING_SIGN') return '待签署'
  if (stage === 'SIGNED') return '已签署'
  return '待评估'
}

function flowStageColor(stage?: CrmContractItem['flowStage']) {
  if (stage === 'PENDING_ASSESSMENT') return 'gold'
  if (stage === 'PENDING_BED_SELECT') return 'blue'
  if (stage === 'PENDING_SIGN') return 'purple'
  if (stage === 'SIGNED') return 'green'
  return 'default'
}

function ownerDeptText(dept?: CrmContractItem['currentOwnerDept']) {
  if (dept === 'ASSESSMENT') return '评估部'
  if (dept === 'MARKETING') return '营销部'
  return '-'
}

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
}

function riskPrecommitLabel(value?: string) {
  if (value === 'RESCUE_FIRST') return '第一时间抢救'
  if (value === 'NOTIFY_FAMILY_FIRST') return '第一时间通知家属'
  return '-'
}

function hoursFrom(timeText?: string) {
  if (!timeText) return 0
  const t = dayjs(timeText)
  if (!t.isValid()) return 0
  return dayjs().diff(t, 'hour')
}

function overdueHours(record: CrmContractItem) {
  const stage = normalizedFlowStage(record)
  if (stage === 'PENDING_ASSESSMENT') {
    return hoursFrom(record.createTime)
  }
  if (stage === 'PENDING_SIGN') {
    return record.updateTime ? hoursFrom(record.updateTime) : 0
  }
  return 0
}

function getOverdueLevel(record: CrmContractItem): 'none' | 'high' {
  const stage = normalizedFlowStage(record)
  const hours = overdueHours(record)
  if (stage === 'PENDING_ASSESSMENT') {
    return hours >= 48 ? 'high' : 'none'
  }
  if (stage === 'PENDING_SIGN') {
    return hours >= 24 ? 'high' : 'none'
  }
  return 'none'
}

function overdueText(record: CrmContractItem) {
  const stage = normalizedFlowStage(record)
  const hours = overdueHours(record)
  if (stage === 'PENDING_ASSESSMENT') return `待评估超时 ${hours}h`
  if (stage === 'PENDING_SIGN') return `待签署超时 ${hours}h`
  return ''
}

function isAdmissionDisabled(record: CrmContractItem) {
  const id = Number(record.id || 0)
  if (!id) return true
  if (assessmentCheckingMap.value[id]) return true
  return !assessmentReadyMap.value[id]
}

function admissionDisabledReason(record: CrmContractItem) {
  const id = Number(record.id || 0)
  if (!id) return '合同数据异常，无法办理入住'
  if (assessmentCheckingMap.value[id]) return '正在校验是否已完成入住评估...'
  if (!record.contractNo) return '请先保存并生成合同号'
  return '仅完成入住评估后的合同才可办理入住'
}

function isFinalizeDisabled(record: CrmContractItem) {
  const id = Number(record.id || 0)
  if (!id) return true
  if (finalizeCheckingMap.value[id]) return true
  return !finalizeReadyMap.value[id]
}

function finalizeDisabledReason(record: CrmContractItem) {
  const id = Number(record.id || 0)
  if (!id) return '合同数据异常，无法签署'
  if (finalizeCheckingMap.value[id]) return '正在校验是否已办理入住...'
  if (!record.contractNo) return '请先保存并生成合同号'
  return '当前合同暂不可签署'
}

async function checkFinalizeReady(record: CrmContractItem) {
  if (!record.id || !record.contractNo) return false
  const admissionPage = await getAdmissionRecords({
    pageNo: 1,
    pageSize: 10,
    contractNo: record.contractNo,
    keyword: record.elderName || record.name
  })
  return (admissionPage.list || []).length > 0
}

async function refreshFinalizeReady(rowsForCheck: CrmContractItem[]) {
  const targets = rowsForCheck.filter((item) => normalizedFlowStage(item) !== 'SIGNED')
  await Promise.all(targets.map(async (item) => {
    const id = Number(item.id || 0)
    if (!id) return
    finalizeCheckingMap.value[id] = true
    try {
      finalizeReadyMap.value[id] = await checkFinalizeReady(item)
    } catch {
      finalizeReadyMap.value[id] = false
    } finally {
      finalizeCheckingMap.value[id] = false
    }
  }))
}

async function refreshAssessmentReady(rowsForCheck: CrmContractItem[]) {
  const targets = rowsForCheck.filter((item) => normalizedFlowStage(item) !== 'SIGNED' && item.status !== 'VOID')
  await Promise.all(targets.map(async (item) => {
    const id = Number(item.id || 0)
    if (!id) return
    assessmentCheckingMap.value[id] = true
    try {
      assessmentReadyMap.value[id] = await checkAssessmentReady(item)
    } catch {
      assessmentReadyMap.value[id] = false
    } finally {
      assessmentCheckingMap.value[id] = false
    }
  }))
}

async function fetchBoardSummary() {
  const summary = await getContractStageSummary({
    currentOwnerDept: onlyMineDept.value ? mineDept.value : undefined
  })
  stageBoard.pendingAssessment = Number(summary?.pendingAssessment || 0)
  stageBoard.pendingBedSelect = Number(summary?.pendingBedSelect || 0)
  stageBoard.pendingSign = Number(summary?.pendingSign || 0)
  stageBoard.signed = Number(summary?.signed || 0)
  stageBoard.pendingAssessmentOverdue = Number(summary?.pendingAssessmentOverdue || 0)
  stageBoard.pendingSignOverdue = Number(summary?.pendingSignOverdue || 0)
  stageBoardGeneratedAt.value = String(summary?.generatedAt || '')
}

function buildContractPageParams() {
  return {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    contractNo: query.contractNo || undefined,
    elderName: query.elderName || undefined,
    elderPhone: query.elderPhone || undefined,
    marketerName: query.marketerName || undefined,
    orgName: query.orgName || undefined,
    flowStage: query.flowStage || undefined,
    currentOwnerDept: onlyMineDept.value ? mineDept.value : undefined,
    overdueOnly: onlyOverdue.value || undefined,
    sortByOverdue: sortByOverdue.value || undefined
  }
}

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<CrmContractItem> = await getContractPage(buildContractPageParams())
    rows.value = page.list || []
    total.value = page.total || 0
    await Promise.all([
      refreshFinalizeReady(tableRows.value),
      refreshAssessmentReady(tableRows.value)
    ])
    await fetchBoardSummary()
  } finally {
    loading.value = false
  }
}

function reset() {
  resetSearchFilters()
  query.flowStage = undefined
  applyStatusPreset()
  selectedRowKeys.value = []
  selectedRows.value = []
  triggerSearchRefresh()
}

function runSearch() {
  query.pageNo = 1
  triggerSearchRefresh()
}

function onPageChange(page: number) {
  query.pageNo = page
  triggerSearchRefresh()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  triggerSearchRefresh()
}

function onOverdueSwitchChange() {
  query.pageNo = 1
  triggerSearchRefresh()
}

function onSortSwitchChange() {
  query.pageNo = 1
  triggerSearchRefresh()
}

function onMineSwitchChange() {
  query.pageNo = 1
  triggerSearchRefresh()
}

function onMineDeptChange() {
  if (!onlyMineDept.value) return
  query.pageNo = 1
  triggerSearchRefresh()
}

function buildSearchRouteQuery() {
  const routeQuery: Record<string, string> = {}
  Object.entries(route.query || {}).forEach(([key, value]) => {
    if (key === 'quick' || key === 'openAttachment' || key === 'attachmentType') return
    const text = firstQueryText(value)
    if (!text) return
    routeQuery[key] = text
  })
  if (query.contractNo) routeQuery.contractNo = query.contractNo
  else delete routeQuery.contractNo
  if (query.elderName) routeQuery.elderName = query.elderName
  else delete routeQuery.elderName
  if (query.elderPhone) routeQuery.elderPhone = query.elderPhone
  else delete routeQuery.elderPhone
  if (query.marketerName) routeQuery.marketerName = query.marketerName
  else delete routeQuery.marketerName
  if (query.orgName) routeQuery.orgName = query.orgName
  else delete routeQuery.orgName
  if (query.flowStage) routeQuery.flowStage = query.flowStage
  else delete routeQuery.flowStage
  routeQuery.pageNo = String(query.pageNo)
  routeQuery.pageSize = String(query.pageSize)
  if (onlyMineDept.value) routeQuery.onlyMine = '1'
  else delete routeQuery.onlyMine
  if (onlyMineDept.value) routeQuery.mineDept = mineDept.value
  else delete routeQuery.mineDept
  if (onlyOverdue.value) routeQuery.onlyOverdue = '1'
  else delete routeQuery.onlyOverdue
  if (sortByOverdue.value) routeQuery.sortByOverdue = '1'
  else delete routeQuery.sortByOverdue
  return routeQuery
}

async function syncSearchQueryToRoute() {
  const nextQuery = buildSearchRouteQuery()
  const currentQuery = normalizeRouteQueryMap(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length === nextKeys.length && nextKeys.every((key) => currentQuery[key] === nextQuery[key])) {
    return
  }
  skipNextSearchRouteWatch.value = true
  searchRouteSignature.value = buildSearchRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

function triggerSearchRefresh() {
  fetchData()
  syncSearchQueryToRoute().catch(() => {})
}

async function copySearchLink() {
  const href = router.resolve({ path: route.path, query: buildSearchRouteQuery() }).href
  const fullUrl = /^https?:\/\//i.test(href) ? href : `${window.location.origin}${href}`
  try {
    await navigator.clipboard.writeText(fullUrl)
    message.success('筛选链接已复制')
  } catch {
    message.warning('当前环境不支持自动复制，请手动复制地址栏链接')
  }
}

function requireSingleSelection(actionLabel: string) {
  if (selectedRowKeys.value.length !== 1) {
    message.info(`请先勾选 1 条合同再执行“${actionLabel}”`)
    return null
  }
  const key = selectedRowKeys.value[0]
  const row =
    selectedRows.value.find((item) => sameId(item.id, key)) ||
    tableRows.value.find((item) => sameId(item.id, key)) ||
    rows.value.find((item) => sameId(item.id, key))
  if (!row) {
    message.warning('未找到选中的合同，请刷新后重试')
    return null
  }
  return row
}

function viewSelected() {
  const row = requireSingleSelection('查看')
  if (!row) return
  view(row)
}

function editSelected() {
  const row = requireSingleSelection('编辑')
  if (!row) return
  openForm(row)
}

async function openAttachmentSelected() {
  const row = requireSingleSelection('附件')
  if (!row) return
  await openAttachment(row)
}

async function admissionSelected() {
  const row = requireSingleSelection('入住办理')
  if (!row) return
  await goAdmissionProcessing(row)
}

async function finalizeSelected() {
  const row = requireSingleSelection('最终签署')
  if (!row) return
  await openFinalize(row)
}

function onContractModalOk() {
  if (contractViewMode.value) {
    open.value = false
    return
  }
  submit()
}

function openForm(record?: CrmContractItem, readonly = false) {
  contractViewMode.value = readonly
  Object.keys(form).forEach((key) => {
    delete (form as Record<string, any>)[key]
  })
  if (record) {
    Object.assign(form, record)
    form.contractStatus = record.contractStatus || '待评估'
    selectedPolicyValues.value = parsePolicyValues(record.orgName)
    syncIdCardDerivedFields(record.idCardNo, false)
    elderFormExtra.careLevel = String(record.careLevel || '')
    elderFormExtra.riskPrecommit = (record.riskPrecommit as any) || undefined
  } else {
    Object.assign(form, {
      id: undefined,
      contractNo: undefined,
      status: 'DRAFT',
      contractSignedFlag: 0,
      contractSignedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      contractStatus: '待评估',
      flowStage: 'PENDING_ASSESSMENT',
      currentOwnerDept: 'ASSESSMENT',
      reservationRoomNo: undefined,
      orgName: undefined
    } as Partial<CrmContractItem>)
    selectedPolicyValues.value = []
    formDerivedBirthDate.value = ''
    elderFormExtra.careLevel = ''
    elderFormExtra.riskPrecommit = undefined
    familyDraftRows.value = []
    selectedDiseaseIds.value = []
  }
  open.value = true
  loadDiseaseOptions().catch(() => {})
  loadElderSnapshotByForm().catch(() => {})
}

async function fetchPolicyOptions() {
  policyLoading.value = true
  try {
    const list = await getMarketingPlanList('POLICY')
    const options = (list || [])
      .filter((item: MarketingPlanItem) => ['PUBLISHED', 'ACTIVE'].includes(String(item.status || '')))
      .map((item: MarketingPlanItem) => {
        const quarter = item.quarterLabel ? `${item.quarterLabel} · ` : ''
        return {
          label: `${quarter}${item.title}`,
          value: item.title
        }
      })
    const dedup = new Map<string, { label: string; value: string }>()
    options.forEach((item) => {
      if (!dedup.has(item.value)) {
        dedup.set(item.value, item)
      }
    })
    policyOptions.value = Array.from(dedup.values())
  } catch (error: any) {
    message.warning(error?.message || '运营政策加载失败，请稍后重试')
    policyOptions.value = []
  } finally {
    policyLoading.value = false
  }
}

function policyFilterOption(input: string, option: any) {
  const label = String(option?.label || '')
  const value = String(option?.value || '')
  const keyword = String(input || '').trim().toLowerCase()
  if (!keyword) return true
  return label.toLowerCase().includes(keyword) || value.toLowerCase().includes(keyword)
}

function parsePolicyValues(raw?: string) {
  if (!raw) return []
  return String(raw)
    .split(/[；;,，]/)
    .map((item) => item.trim())
    .filter((item) => !!item)
}

function onPolicySelectionChange(values: string[]) {
  const normalized = Array.from(new Set((values || []).map((item) => String(item || '').trim()).filter((item) => !!item)))
  if (normalized.length > 2) {
    message.warning('每位老人最多享受 2 条营销优惠政策')
    selectedPolicyValues.value = normalized.slice(0, 2)
    return
  }
  selectedPolicyValues.value = normalized
}

async function submit() {
  if (!formRef.value) return
  let savedContract: CrmContractItem | undefined
  try {
    await formRef.value.validate()
    validateFamilyDraftRows()
    submitting.value = true
    const isCreate = !form.id
    const payload: Partial<CrmContractItem> = {
      ...form,
      name: form.elderName || form.name || '签约客户',
      phone: form.elderPhone || form.phone,
      contractNo: isCreate ? undefined : form.contractNo,
      status: form.status || 'DRAFT',
      contractSignedFlag: 0,
      flowStage: isCreate ? 'PENDING_ASSESSMENT' : (form.flowStage || 'PENDING_ASSESSMENT'),
      currentOwnerDept: isCreate ? 'ASSESSMENT' : (form.currentOwnerDept || 'ASSESSMENT'),
      contractStatus: isCreate ? '待评估' : (form.contractStatus || '待评估'),
      reservationRoomNo: isCreate ? undefined : form.reservationRoomNo,
      orgName: selectedPolicyValues.value.length ? selectedPolicyValues.value.join('；') : undefined,
      age: Number.isFinite(Number(form.age)) ? Number(form.age) : undefined
    }
    if (form.id) {
      savedContract = await updateCrmContract(form.id, payload)
    } else {
      savedContract = await createCrmContract(payload)
      form.id = savedContract?.id
    }
    if (savedContract?.contractNo) {
      form.contractNo = savedContract.contractNo
    }
    if (savedContract?.leadId) {
      form.leadId = savedContract.leadId
    }
    if (savedContract?.elderId) {
      form.elderId = savedContract.elderId
    }
    try {
      await syncContractElderData(savedContract)
    } catch (syncError: any) {
      message.warning(`合同已保存，但长者资料同步失败：${syncError?.message || '请稍后重试'}`)
      await fetchData()
      return
    }
    message.success(`保存成功，合同号：${savedContract?.contractNo || '生成中'}`)
    open.value = false
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

function batchDelete() {
  const ids = selectedRowKeys.value
  if (!ids.length) {
    message.info('请先勾选要删除的合同')
    return
  }
  const contractNos = selectedRows.value
    .map((item) => item.contractNo)
    .filter((item): item is string => Boolean(item))
  Modal.confirm({
    title: `确认删除 ${ids.length} 条合同记录吗？`,
    onOk: async () => {
      let affected = contractNos.length
        ? await batchDeleteContracts({ contractNos })
        : await batchDeleteContracts({ ids })
      if (!affected && ids.length) {
        await Promise.all(ids.map((id) => deleteCrmContract(id)))
        affected = ids.length
      }
      selectedRowKeys.value = []
      selectedRows.value = []
      if (!affected) {
        message.warning('未删除任何合同，请刷新后重试')
        return
      }
      message.success(`删除成功（${affected} 条）`)
      fetchData()
    }
  })
}

function removeContract(record: CrmContractItem) {
  Modal.confirm({
    title: `确认删除合同 ${record.contractNo || '-'} 吗？`,
    onOk: async () => {
      let affected = 0
      if (record.contractNo) {
        affected = await batchDeleteContracts({ contractNos: [record.contractNo] })
      }
      if (!affected) {
        await deleteCrmContract(record.id)
        affected = 1
      }
      if (!affected) {
        message.warning('未删除任何合同，请刷新后重试')
        return
      }
      message.success('删除成功')
      await fetchData()
    }
  })
}

function view(record: CrmContractItem) {
  openForm(record, true)
}

async function goAdmissionProcessing(record: CrmContractItem) {
  try {
    if (record.status === 'VOID') {
      message.warning('作废合同不可办理入住')
      return
    }
    if (normalizedFlowStage(record) === 'SIGNED' || record.status === 'SIGNED' || record.status === 'EFFECTIVE') {
      message.warning('合同已完成最终签署，无需重复办理入住')
      return
    }
    const hasCompletedAssessment = await checkAssessmentReady(record)
    if (!hasCompletedAssessment) {
      message.warning('仅完成入住评估后的合同才可办理入住，请先在入住评估中完成评估并保存结果')
      return
    }
    const lead = await ensureContractNo(record)
    const elder = await ensureElderFromLead(lead)
    await moveContractToBedSelect(lead.id)
    router.push(
      `/elder/admission-processing?residentId=${elder.id}&leadId=${lead.leadId || lead.id}&contractNo=${lead.contractNo || ''}&elderName=${encodeURIComponent(elder.fullName || lead.elderName || '')}`
    )
  } catch (error: any) {
    message.error(error?.message || '跳转入住办理失败')
  }
}

async function checkAssessmentReady(record: CrmContractItem) {
  const stage = normalizedFlowStage(record)
  if (stage === 'PENDING_BED_SELECT' || stage === 'PENDING_SIGN' || stage === 'SIGNED') {
    return true
  }
  const leadId = record.leadId || record.id
  const elderId = record.elderId
  if (!leadId && !elderId) return false
  const overview = await getContractAssessmentOverview({
    leadId: leadId || undefined,
    elderId: elderId || undefined
  })
  const contract = (overview?.contracts || []).find((item) => {
    if (record.id && item.contractId && String(item.contractId) === String(record.id)) return true
    if (record.contractNo && item.contractNo) return String(item.contractNo) === String(record.contractNo)
    return false
  })
  if (!contract) return false
  return (contract.reports || []).some((item) => item.status && item.status !== 'DRAFT')
}

async function openFinalize(record: CrmContractItem) {
  try {
    if (record.status === 'VOID') {
      message.warning('作废合同不可最终签署')
      return
    }
    if (normalizedFlowStage(record) === 'PENDING_ASSESSMENT') {
      message.warning('请先完成入住评估，再进行后续办理')
      return
    }
    const lead = await ensureContractNo(record)
    const admissionPage = await getAdmissionRecords({
      pageNo: 1,
      pageSize: 20,
      contractNo: lead.contractNo
    })
    const hasAdmission = (admissionPage.list || []).length > 0
    if (!hasAdmission) {
      const elder = await ensureElderFromLead(lead)
      router.push(
        `/elder/admission-processing?residentId=${elder.id}&leadId=${lead.leadId || lead.id}&contractNo=${lead.contractNo || ''}&elderName=${encodeURIComponent(elder.fullName || lead.elderName || '')}`
      )
      return
    }
    const elder = await ensureElderFromLead(lead)
    const signingLead = normalizedFlowStage(lead) === 'PENDING_SIGN'
      ? lead
      : await moveContractToPendingSign(lead.id)
    finalizeLead.value = signingLead
    finalizeForm.contractNo = signingLead.contractNo || ''
    finalizeForm.elderName = elder.fullName
    finalizeForm.elderId = elder.id
    finalizeForm.remark = ''
    finalizeOpen.value = true
  } catch (error: any) {
    message.error(error?.message || '进入最终签署失败')
  }
}

async function submitFinalize() {
  if (!finalizeLead.value) return
  finalizing.value = true
  try {
    await finalizeContract(finalizeLead.value.id, finalizeForm.remark || finalizeLead.value.remark)
    message.success('最终签署完成')
    finalizeOpen.value = false
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '最终签署失败')
  } finally {
    finalizing.value = false
  }
}

async function openAttachment(record: CrmContractItem) {
  currentAttachmentLead.value = record
  attachments.value = await getContractAttachments(record.id)
  attachmentOpen.value = true
}

function resolveAttachmentTypeFromRoute() {
  const raw = String(route.query.attachmentType || '').trim().toUpperCase()
  if (raw === 'MEDICAL_RECORD' || raw === 'MEDICAL_INSURANCE' || raw === 'HOUSEHOLD' || raw === 'CONTRACT' || raw === 'OTHER') {
    attachmentType.value = raw
    return
  }
  attachmentType.value = 'CONTRACT'
}

async function autoOpenAttachmentFromRoute() {
  const openAttachmentFlag = String(route.query.openAttachment || '').trim() === '1'
  if (!openAttachmentFlag) return
  const contractNo = String(route.query.contractNo || '').trim()
  const elderName = String(route.query.elderName || '').trim()
  const elderId = String(route.query.elderId || route.query.residentId || '').trim()
  const handledKey = `${contractNo}|${elderName}|${elderId}|${String(route.query.attachmentType || '')}`
  if (autoAttachmentHandledKey.value === handledKey) return
  const target = tableRows.value.find((item) => {
    if (contractNo && String(item.contractNo || '').trim() === contractNo) return true
    if (!contractNo && elderName && String(item.elderName || '').trim() === elderName) return true
    if (!contractNo && !elderName && elderId && String(item.elderId || '').trim() === elderId) return true
    return false
  }) || rows.value.find((item) => {
    if (contractNo && String(item.contractNo || '').trim() === contractNo) return true
    if (!contractNo && elderName && String(item.elderName || '').trim() === elderName) return true
    if (!contractNo && !elderName && elderId && String(item.elderId || '').trim() === elderId) return true
    return false
  })
  if (!target) {
    if (contractNo || elderName) {
      message.warning('未找到目标合同，请先检查筛选条件后重试')
    }
    return
  }
  selectedRowKeys.value = [target.id]
  selectedRows.value = [target]
  resolveAttachmentTypeFromRoute()
  await openAttachment(target)
  autoAttachmentHandledKey.value = handledKey
}

function focusEditTargetFromRoute() {
  if (String(route.query.focus || '').trim() !== 'elder-info') return
  nextTick(() => {
    elderNameInputRef.value?.focus?.()
  })
}

async function autoOpenEditFromRoute() {
  const openEditFlag = String(route.query.openEdit || '').trim() === '1'
  if (!openEditFlag) return
  const contractNo = String(route.query.contractNo || '').trim()
  const elderName = String(route.query.elderName || '').trim()
  const elderId = String(route.query.elderId || route.query.residentId || '').trim()
  const handledKey = `${contractNo}|${elderName}|${elderId}`
  if (autoEditHandledKey.value === handledKey) return
  const target = tableRows.value.find((item) => {
    if (contractNo && String(item.contractNo || '').trim() === contractNo) return true
    if (!contractNo && elderName && String(item.elderName || '').trim() === elderName) return true
    if (!contractNo && !elderName && elderId && String(item.elderId || '').trim() === elderId) return true
    return false
  }) || rows.value.find((item) => {
    if (contractNo && String(item.contractNo || '').trim() === contractNo) return true
    if (!contractNo && elderName && String(item.elderName || '').trim() === elderName) return true
    if (!contractNo && !elderName && elderId && String(item.elderId || '').trim() === elderId) return true
    return false
  })
  if (!target) {
    if (contractNo || elderName) {
      message.warning('未找到目标合同，请先检查筛选条件后重试')
    }
    return
  }
  selectedRowKeys.value = [target.id]
  selectedRows.value = [target]
  openForm(target)
  focusEditTargetFromRoute()
  autoEditHandledKey.value = handledKey
}

function beforeUpload(file: File) {
  const allowExt = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'zip']
  const ext = file.name?.split('.').pop()?.toLowerCase() || ''
  if (!allowExt.includes(ext)) {
    message.error(`文件类型不支持，仅允许: ${allowExt.join(', ')}`)
    return false
  }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('文件大小超限，最大支持 10MB')
    return false
  }
  return true
}

async function handleUpload(option: any) {
  const file = option?.file as File
  if (!file || !currentAttachmentLead.value) {
    option?.onError?.(new Error('未选择合同或文件'))
    return
  }
  attachmentSubmitting.value = true
  try {
    const uploaded = await uploadMarketingFile(file, 'marketing-contract')
    await createContractAttachment(currentAttachmentLead.value.id, {
      contractNo: currentAttachmentLead.value.contractNo,
      attachmentType: attachmentType.value,
      fileName: uploaded.originalFileName || uploaded.fileName,
      fileUrl: uploaded.fileUrl,
      fileType: uploaded.fileType,
      fileSize: uploaded.fileSize,
      remark: attachmentType.value
    })
    attachments.value = await getContractAttachments(currentAttachmentLead.value.id)
    message.success('附件上传成功')
    option?.onSuccess?.(uploaded)
  } catch (error) {
    option?.onError?.(error)
  } finally {
    attachmentSubmitting.value = false
  }
}

function attachmentTypeLabel(value?: string) {
  if (value === 'MEDICAL_RECORD') return '病历资料'
  if (value === 'MEDICAL_INSURANCE') return '医保复印件'
  if (value === 'HOUSEHOLD') return '户口复印件'
  if (value === 'CONTRACT') return '合同附件'
  if (value === 'OTHER') return '其他附件'
  return value || '未分类'
}

function isImageAttachment(record: ContractAttachmentItem) {
  const type = (record.fileType || '').toLowerCase()
  if (type.startsWith('image/')) return true
  const url = (record.fileUrl || '').toLowerCase()
  return ['.jpg', '.jpeg', '.png', '.gif', '.webp'].some((suffix) => url.endsWith(suffix))
}

function formatFileSize(size?: number) {
  if (size == null || Number.isNaN(size)) return '-'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(2)}MB`
}

async function removeAttachment(attachmentId: number) {
  await deleteContractAttachment(attachmentId)
  if (currentAttachmentLead.value) {
    attachments.value = await getContractAttachments(currentAttachmentLead.value.id)
  }
  message.success('附件已删除')
}

function normalizeLeadName(lead: CrmContractItem) {
  const explicit = (lead.elderName || lead.name || '').trim()
  if (explicit) return explicit
  const phoneTail = String(lead.elderPhone || lead.phone || '').replace(/\D/g, '').slice(-4)
  if (phoneTail) return `长者${phoneTail}`
  const idCardTail = String(lead.idCardNo || '').trim().slice(-4)
  if (idCardTail) return `长者${idCardTail}`
  const contractNo = String(lead.contractNo || '').trim()
  if (contractNo) return `长者${contractNo.slice(-4)}`
  return `长者${String(lead.id || '').slice(-4) || '未命名'}`
}

function resolveBirthDateAndAgeFromIdCard(idCardNo?: string) {
  const normalized = String(idCardNo || '').trim()
  const now = dayjs()
  const toValidBirthDate = (value: string) => {
    const d = dayjs(value)
    if (!d.isValid()) return null
    if (d.format('YYYY-MM-DD') !== value) return null
    if (d.isAfter(now)) return null
    return d
  }
  if (/^\d{17}[\dXx]$/.test(normalized)) {
    const yyyy = normalized.slice(6, 10)
    const mm = normalized.slice(10, 12)
    const dd = normalized.slice(12, 14)
    const birthDate = toValidBirthDate(`${yyyy}-${mm}-${dd}`)
    if (!birthDate) return { birthDate: '', age: undefined as number | undefined }
    return { birthDate: birthDate.format('YYYY-MM-DD'), age: now.diff(birthDate, 'year') }
  }
  if (/^\d{15}$/.test(normalized)) {
    const yy = normalized.slice(6, 8)
    const mm = normalized.slice(8, 10)
    const dd = normalized.slice(10, 12)
    const birthDate = toValidBirthDate(`19${yy}-${mm}-${dd}`)
    if (!birthDate) return { birthDate: '', age: undefined as number | undefined }
    return { birthDate: birthDate.format('YYYY-MM-DD'), age: now.diff(birthDate, 'year') }
  }
  return { birthDate: '', age: undefined as number | undefined }
}

function syncIdCardDerivedFields(idCardNo?: string, overrideAge = true) {
  const { birthDate, age } = resolveBirthDateAndAgeFromIdCard(idCardNo)
  formDerivedBirthDate.value = birthDate || ''
  if (!birthDate) {
    if (overrideAge) {
      form.age = undefined
    }
    return
  }
  if (overrideAge || form.age == null) {
    form.age = age
  }
}

function addFamilyDraftRow() {
  familyDraftRows.value.push({
    key: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    realName: '',
    relation: '',
    phone: '',
    idCardNo: '',
    isPrimary: familyDraftRows.value.length === 0
  })
}

function removeFamilyDraftRow(key: string) {
  familyDraftRows.value = familyDraftRows.value.filter((item) => item.key !== key)
  if (!familyDraftRows.value.some((item) => item.isPrimary) && familyDraftRows.value.length) {
    familyDraftRows.value[0].isPrimary = true
  }
}

function setPrimaryFamilyDraftRow(key: string) {
  familyDraftRows.value.forEach((item) => {
    item.isPrimary = item.key === key
  })
}

async function loadDiseaseOptions() {
  try {
    const list = await getDiseaseList()
    diseaseOptions.value = (list || []).map((item: any) => ({
      label: String(item.diseaseName || item.name || `疾病#${item.id}`),
      value: Number(item.id)
    }))
  } catch {
    diseaseOptions.value = []
  }
}

async function findElderByContractForm() {
  if (form.elderId) {
    try {
      return await getElderDetail(String(form.elderId))
    } catch {
      // ignore
    }
  }
  const keywordCandidates = [
    String(form.idCardNo || '').trim(),
    String(form.elderName || '').trim(),
    String(form.elderPhone || form.phone || '').trim()
  ].filter(Boolean)
  for (const keyword of keywordCandidates) {
    const page = await getElderPage({ pageNo: 1, pageSize: 120, keyword })
    const list = page.list || []
    const byIdCard = String(form.idCardNo || '').trim()
      ? list.find((item) => String(item.idCardNo || '').trim() === String(form.idCardNo || '').trim())
      : undefined
    if (byIdCard) return byIdCard
    const byNamePhone = list.find((item) =>
      String(item.fullName || '').trim() === String(form.elderName || '').trim()
      && String(item.phone || '').trim() === String(form.elderPhone || form.phone || '').trim())
    if (byNamePhone) return byNamePhone
    const byName = list.find((item) => String(item.fullName || '').trim() === String(form.elderName || '').trim())
    if (byName) return byName
  }
  return undefined
}

async function loadElderSnapshotByForm() {
  elderSnapshotLoading.value = true
  try {
    const elder = await findElderByContractForm()
    if (!elder?.id) {
      familyDraftRows.value = []
      selectedDiseaseIds.value = []
      return
    }
    elderFormExtra.careLevel = String(elder.careLevel || elderFormExtra.careLevel || '')
    elderFormExtra.riskPrecommit = elder.riskPrecommit as any
    const [families, diseases] = await Promise.all([
      getFamilyRelations(String(elder.id)).catch(() => []),
      getElderDiseases(String(elder.id)).catch(() => [])
    ])
    familyDraftRows.value = (families || []).map((item, index) => ({
      key: `existing-${String(item.id || item.familyUserId || index)}`,
      familyUserId: String(item.familyUserId || ''),
      realName: String(item.realName || ''),
      relation: String(item.relation || ''),
      phone: String(item.phone || ''),
      idCardNo: String((item as any).idCardNo || ''),
      isPrimary: Number(item.isPrimary || 0) === 1
    }))
    if (!familyDraftRows.value.some((item) => item.isPrimary) && familyDraftRows.value.length) {
      familyDraftRows.value[0].isPrimary = true
    }
    selectedDiseaseIds.value = (diseases || [])
      .map((item) => Number(item.diseaseId))
      .filter((item) => Number.isFinite(item) && item > 0)
  } finally {
    elderSnapshotLoading.value = false
  }
}

async function syncContractElderData(savedContract: CrmContractItem) {
  const { birthDate } = resolveBirthDateAndAgeFromIdCard(String(form.idCardNo || '').trim())
  const fallbackName = (() => {
    const explicit = String(form.elderName || form.name || '').trim()
    if (explicit) return explicit
    const phoneTail = String(form.elderPhone || form.phone || '').replace(/\D/g, '').slice(-4)
    if (phoneTail) return `长者${phoneTail}`
    const idCardTail = String(form.idCardNo || '').trim().slice(-4)
    if (idCardTail) return `长者${idCardTail}`
    const contractTail = String(savedContract.contractNo || '').trim().slice(-4)
    if (contractTail) return `长者${contractTail}`
    return '未命名长者'
  })()
  const payload = {
    fullName: fallbackName,
    idCardNo: String(form.idCardNo || '').trim() || undefined,
    gender: form.gender,
    birthDate: birthDate || undefined,
    phone: String(form.elderPhone || form.phone || '').trim() || undefined,
    homeAddress: String(form.homeAddress || '').trim() || undefined,
    careLevel: elderFormExtra.careLevel || undefined,
    riskPrecommit: elderFormExtra.riskPrecommit,
    remark: String(form.remark || '').trim() || undefined
  } as Partial<ElderItem>

  const existed = await findElderByContractForm()
  let elderId = String(existed?.id || '').trim()
  if (elderId) {
    await updateElder(elderId, payload)
  } else {
    const created = await createElder({
      ...payload,
      status: 0
    })
    elderId = String(created?.id || '').trim()
  }
  if (!elderId) return

  if (!savedContract.elderId || String(savedContract.elderId) !== elderId) {
    await updateCrmContract(savedContract.id, {
      ...savedContract,
      elderId
    })
  }

  if (selectedDiseaseIds.value.length > 0) {
    await updateElderDiseases(elderId, selectedDiseaseIds.value)
  }

  if (familyDraftRows.value.length > 0) {
    await getFamilyRelations(elderId).catch(() => [])
    for (let i = 0; i < familyDraftRows.value.length; i += 1) {
      const familyRow = familyDraftRows.value[i]
      const user = await upsertFamilyUser({
        realName: String(familyRow.realName || '').trim(),
        phone: String(familyRow.phone || '').trim(),
        idCardNo: String(familyRow.idCardNo || '').trim(),
        status: 1
      })
      const familyUserId = String(user?.id || familyRow.familyUserId || '').trim()
      if (!familyUserId) continue
      await bindFamily({
        elderId,
        familyUserId,
        relation: String(familyRow.relation || '').trim() || '家属',
        isPrimary: (familyRow.isPrimary || (!familyDraftRows.value.some((item) => item.isPrimary) && i === 0)) ? 1 : 0
      })
    }
  }

  await loadElderSnapshotByForm()
}

function validateFamilyDraftRows() {
  if (familyDraftRows.value.length === 0) {
    throw new Error('请至少填写一位家属信息')
  }
  const invalidRow = familyDraftRows.value.find((item) =>
    !String(item.realName || '').trim()
    || !String(item.phone || '').trim()
    || !String(item.idCardNo || '').trim())
  if (invalidRow) {
    throw new Error('家属信息需填写姓名、手机号和身份证号')
  }
  const invalidPhoneRow = familyDraftRows.value.find((item) => !/^1\d{10}$/.test(String(item.phone || '').trim()))
  if (invalidPhoneRow) {
    throw new Error(`家属手机号格式不正确：${String(invalidPhoneRow.realName || '未命名家属')}`)
  }
  const invalidIdCardRow = familyDraftRows.value.find((item) => !/^(\d{15}|\d{17}[\dXx])$/.test(String(item.idCardNo || '').trim()))
  if (invalidIdCardRow) {
    throw new Error(`家属身份证号格式不正确：${String(invalidIdCardRow.realName || '未命名家属')}`)
  }
}

async function ensureContractNo(record: CrmContractItem) {
  if (record.contractNo) return record
  return updateCrmContract(record.id, {
    ...record,
    status: record.status || 'DRAFT',
    flowStage: normalizedFlowStage(record),
    currentOwnerDept: normalizedOwnerDept(record),
    contractStatus: record.contractStatus || '待评估'
  })
}

function pickLatestAttachment(leadAttachments: ContractAttachmentItem[], type: string) {
  return leadAttachments.find((item) => (item.attachmentType || item.remark) === type)
}

async function findExistingElderByLead(lead: CrmContractItem): Promise<ElderItem | undefined> {
  const page = await getElderPage({ pageNo: 1, pageSize: 300, keyword: normalizeLeadName(lead) })
  const list = page.list || []
  const byIdCard = lead.idCardNo ? list.find((item) => item.idCardNo === lead.idCardNo) : undefined
  if (byIdCard) return byIdCard
  return list.find((item) => item.fullName === normalizeLeadName(lead) && item.phone === (lead.elderPhone || lead.phone))
}

async function ensureElderFromLead(lead: CrmContractItem): Promise<ElderItem> {
  const leadAttachments = await getContractAttachments(lead.id)
  const existing = await findExistingElderByLead(lead)
  const { birthDate } = resolveBirthDateAndAgeFromIdCard(lead.idCardNo)
  const elderPayload = {
    fullName: normalizeLeadName(lead),
    idCardNo: lead.idCardNo,
    gender: lead.gender,
    birthDate: birthDate || undefined,
    phone: lead.elderPhone || lead.phone,
    homeAddress: lead.homeAddress,
    medicalRecordFileUrl: pickLatestAttachment(leadAttachments, 'MEDICAL_RECORD')?.fileUrl,
    medicalInsuranceCopyUrl: pickLatestAttachment(leadAttachments, 'MEDICAL_INSURANCE')?.fileUrl,
    householdCopyUrl: pickLatestAttachment(leadAttachments, 'HOUSEHOLD')?.fileUrl,
    remark: lead.remark
  }
  if (existing?.id) {
    await updateElder(existing.id, elderPayload)
    return { ...existing, ...elderPayload, id: existing.id }
  }
  return createElder({
    ...elderPayload,
    admissionDate: dayjs().format('YYYY-MM-DD'),
    status: 1
  })
}

function consumeQuickQuery() {
  if (String(route.query.quick || '').trim() !== '1') {
    return
  }
  const nextQuery: Record<string, any> = { ...route.query }
  delete nextQuery.quick
  router.replace({ path: route.path, query: nextQuery })
}

onMounted(() => {
  applyStatusPreset()
  applyAttachmentRouteFilter()
  applyQueryRouteFilter()
  searchRouteSignature.value = buildSearchRouteSignature(route.query as Record<string, unknown>)
  fetchPolicyOptions()
  fetchData()
    .then(() => autoOpenAttachmentFromRoute())
    .then(() => autoOpenEditFromRoute())
    .then(() => syncSearchQueryToRoute().catch(() => {}))
  const quick = String(route.query.quick || '').trim()
  if (quick === '1' && !isSignedMode.value) {
    openForm()
    consumeQuickQuery()
  }
})

watch(
  () => route.query,
  async () => {
    const nextSignature = buildSearchRouteSignature(route.query as Record<string, unknown>)
    if (skipNextSearchRouteWatch.value) {
      skipNextSearchRouteWatch.value = false
      searchRouteSignature.value = nextSignature
      const quick = String(route.query.quick || '').trim()
      if (quick === '1' && !isSignedMode.value) {
        openForm()
        consumeQuickQuery()
      }
      if (String(route.query.openAttachment || '').trim() === '1') {
        await autoOpenAttachmentFromRoute()
      }
      if (String(route.query.openEdit || '').trim() === '1') {
        await autoOpenEditFromRoute()
      }
      return
    }
    const signatureChanged = nextSignature !== searchRouteSignature.value
    searchRouteSignature.value = nextSignature
    applyStatusPreset()
    applyAttachmentRouteFilter()
    applyQueryRouteFilter()
    if (signatureChanged) {
      await fetchData()
    }
    await autoOpenAttachmentFromRoute()
    await autoOpenEditFromRoute()
    const quick = String(route.query.quick || '').trim()
    if (quick === '1' && !isSignedMode.value) {
      openForm()
      consumeQuickQuery()
    }
  },
  { deep: true }
)
watch(
  () => props.statusPreset,
  () => {
    applyStatusPreset()
    query.pageNo = 1
    triggerSearchRefresh()
  }
)

watch(
  () => tableRows.value.map((item) => `${item.id}-${item.contractNo || ''}`).join(','),
  () => {
    refreshFinalizeReady(tableRows.value)
  }
)
watch(
  () => form.idCardNo,
  (value) => {
    syncIdCardDerivedFields(value, true)
  }
)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.board-head {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.board-head-title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.board-head-meta {
  color: #64748b;
  font-size: 12px;
}

.board-item {
  padding: 10px 12px;
  border: 1px solid #e6f4ff;
  border-radius: 8px;
  background: linear-gradient(122deg, rgba(248, 251, 255, 0.95) 0%, rgba(240, 247, 255, 0.95) 100%);
}

.board-title {
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
}

.board-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 600;
}

.contract-workbench {
  --contract-bg: linear-gradient(180deg, #f8fbff 0%, #fdfefe 100%);
  --contract-border: rgba(100, 116, 139, 0.16);
  --contract-strong: #0f172a;
  --contract-muted: #64748b;
  --contract-accent: #0f766e;
  padding: 4px;
}

.contract-workbench-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 18px;
  margin-bottom: 16px;
  border: 1px solid var(--contract-border);
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.12), transparent 36%),
    radial-gradient(circle at bottom left, rgba(59, 130, 246, 0.1), transparent 28%),
    var(--contract-bg);
}

.contract-workbench-kicker {
  color: var(--contract-accent);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.contract-workbench-title {
  margin-top: 6px;
  color: var(--contract-strong);
  font-size: 22px;
  line-height: 1.2;
  font-weight: 700;
}

.contract-workbench-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.contract-section {
  margin-bottom: 16px;
  padding: 18px 18px 6px;
  border: 1px solid var(--contract-border);
  border-radius: 18px;
  background: var(--contract-bg);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.contract-section-head {
  margin-bottom: 12px;
}

.family-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.contract-section-title {
  color: var(--contract-strong);
  font-size: 16px;
  font-weight: 700;
}

.contract-section-desc {
  margin-top: 4px;
  color: var(--contract-muted);
  font-size: 13px;
  line-height: 1.5;
}

.contract-readonly {
  display: grid;
  gap: 16px;
}

.readonly-grid {
  display: grid;
  gap: 12px;
}

.readonly-grid-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.readonly-item {
  min-height: 88px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.72);
}

.readonly-label {
  display: block;
  color: var(--contract-muted);
  font-size: 12px;
  margin-bottom: 8px;
}

.readonly-value {
  display: block;
  color: var(--contract-strong);
  font-size: 15px;
  line-height: 1.5;
}

.readonly-policies,
.readonly-address {
  grid-column: span 2;
}

.readonly-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.readonly-empty {
  color: var(--contract-muted);
  font-size: 13px;
}

.readonly-family-list,
.family-editor-list {
  display: grid;
  gap: 12px;
}

.family-card {
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: rgba(255, 255, 255, 0.82);
}

.family-card-head,
.family-card-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.family-card-meta {
  color: var(--contract-muted);
  font-size: 13px;
}

.family-card-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  margin-top: 10px;
  color: var(--contract-strong);
  font-size: 13px;
}

.family-card-editable {
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.96) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.compact-form-item {
  margin-bottom: 8px;
}

.required-label::before,
.required-section-title::before {
  content: "*";
  color: #ff4d4f;
  margin-right: 4px;
  font-weight: 700;
}

.readonly-disease-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed rgba(148, 163, 184, 0.34);
}

.readonly-remark {
  min-height: 76px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--contract-strong);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .contract-workbench-head,
  .family-head {
    flex-direction: column;
  }

  .contract-workbench-title {
    font-size: 18px;
  }

  .readonly-grid-4 {
    grid-template-columns: 1fr;
  }

  .readonly-policies,
  .readonly-address {
    grid-column: span 1;
  }
}
</style>
