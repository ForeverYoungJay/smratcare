<template>
  <PageContainer class="rm-page" title="入住办理中心" subTitle="登记、评估、合同、缴费、分床、确认在同一页面完成" kicker="RESIDENT MANAGEMENT / 04">
    <template #extra>
      <a-space wrap>
        <a-button v-if="activeTab !== 'trial'" @click="scrollToRecords">查看入住记录</a-button>
        <a-button v-if="canOpenAdmissionPath(NEW_ADMISSION_PATH)" type="primary" @click="router.push(NEW_ADMISSION_PATH)">新建入住</a-button>
      </a-space>
    </template>

    <a-alert v-if="lifecycleContext.active" type="info" show-icon style="margin-bottom: 12px" :message="lifecycleContext.message">
      <template #description>
        <a-space wrap>
          <a-tag color="blue">场景：入住状态变更联动</a-tag>
          <a-button type="link" size="small" @click="router.push('/elder/status-change')">返回状态变更中心</a-button>
          <a-button type="link" size="small" @click="router.push('/logistics/task-center?source=lifecycle&scene=status-change&tab=maintenance&overdueOnly=1')">
            查看后勤交接任务
          </a-button>
        </a-space>
      </template>
    </a-alert>

    <a-tabs :active-key="activeTab" class="adm-tabs" @change="(key: string | number) => switchTab(key as AdmissionTab)">
      <a-tab-pane key="admission" tab="入住办理" />
      <a-tab-pane key="trial" tab="试住登记" />
    </a-tabs>

    <AdmissionTrialPanel
      v-if="activeTab === 'trial'"
      :elder-id="trialElderId"
      :auto-create="String(route.query.create || '') === '1'"
      @convert="onTrialConvert"
    />

    <div v-show="activeTab !== 'trial'">
      <div class="rm-kpis">
        <div class="rm-kpi">
          <span class="rm-kpi__icon"><FileDoneOutlined /></span>
          <span class="rm-kpi__label">待办理入住</span>
          <span class="rm-kpi__value">{{ kpiText(kpi.stage?.pendingBedSelect) }}<small>人</small></span>
          <span class="rm-kpi__hint">合同已过评估，待分床入住</span>
        </div>
        <div class="rm-kpi">
          <span class="rm-kpi__icon rm-kpi__icon--warning"><CalendarOutlined /></span>
          <span class="rm-kpi__label">今日预约参观</span>
          <span class="rm-kpi__value">{{ kpiText(kpi.visitToday) }}<small>人</small></span>
          <span class="rm-kpi__hint">今日已办理入住 {{ kpiText(kpi.todayAdmitted) }} 人</span>
        </div>
        <div class="rm-kpi">
          <span class="rm-kpi__icon rm-kpi__icon--info"><HeartOutlined /></span>
          <span class="rm-kpi__label">待完成评估</span>
          <span class="rm-kpi__value">{{ kpiText(kpi.stage?.pendingAssessment) }}<small>人</small></span>
          <span class="rm-kpi__hint">需要护理/医务协同{{ kpi.stage?.pendingAssessmentOverdue ? `，超时 ${kpi.stage.pendingAssessmentOverdue}` : '' }}</span>
        </div>
        <div class="rm-kpi">
          <span class="rm-kpi__icon rm-kpi__icon--danger"><WarningOutlined /></span>
          <span class="rm-kpi__label">待补充资料</span>
          <span class="rm-kpi__value">{{ kpiText(kpi.returned?.length) }}<small>人</small></span>
          <span class="rm-kpi__hint">已退回补充，待资料补齐</span>
        </div>
      </div>

      <section class="rm-card adm-case">
        <a-form id="admission-form" ref="formRef" :model="form" :rules="rules" layout="vertical">
          <div class="rm-card__head">
            <h3 class="rm-card__title">在办入住 · {{ caseName || '未选择' }}</h3>
            <a-form-item name="elderId" class="adm-case__picker">
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="admissionElderOptions"
                :loading="elderLoading"
                placeholder="搜索长者姓名/拼音首字母切换办理对象"
                @search="onElderSearch"
                @focus="() => !admissionElderOptions.length && searchElders('')"
                @change="onElderChange"
              />
            </a-form-item>
            <div class="rm-card__extra">
              <a-space :size="6">
                <span v-if="form.contractNo" class="rm-pill rm-pill--info">{{ form.contractNo }}</span>
                <span v-if="form.elderId" class="rm-pill rm-pill--warning">第 {{ activeStep }} / 6 步</span>
              </a-space>
            </div>
          </div>

          <ol class="adm-steps">
            <li v-for="(step, index) in admissionSteps" :key="step.key">
              <button type="button" :class="['adm-step', `is-${step.state}`, { 'is-active': activeStep === index + 1 }]" @click="activeStep = index + 1">
                <span class="adm-step__index">{{ step.state === 'done' ? '✓' : index + 1 }}</span>
                <span>{{ step.label }}</span>
              </button>
            </li>
          </ol>

          <div class="rm-split">
            <a-spin :spinning="caseLoading">
              <div class="rm-card adm-pane">
                <h4 class="adm-pane__title">第 {{ activeStep }} 步 · {{ activeStepMeta.label }}</h4>
                <div class="rm-note adm-pane__note">责任部门：{{ activeStepMeta.owner }}<template v-if="activeStepMeta.key === 'assessment'">；护理部提交评估，医务部按权限审核</template></div>
                <a-alert v-if="caseProgress?.status === 'RETURNED'" type="warning" show-icon style="margin-bottom: 12px" :message="`已退回补充：${caseProgress.returnReason || '-'}`" description="补齐资料后点「暂存草稿」或「保存并下一步」即解除退回状态。" />

                <a-empty v-if="!form.elderId" :description="form.contractNo ? '该合同尚未建立长者档案，请先到合同签约完善长者信息' : '请从右侧待办名单选择，或在上方搜索长者开始办理'">
                  <a-button v-if="form.contractNo && canOpenAdmissionPath(checklistRoute('elder-base'))" @click="jumpToChecklist('elder-base')">去完善长者信息</a-button>
                </a-empty>

                <template v-else-if="activeStepMeta.key === 'register'">
                  <div v-for="item in submitChecklist" :key="item.key" class="rm-row">
                    <span :class="['rm-pill', item.ready ? 'rm-pill--success' : item.key === 'disease' ? 'rm-pill--warning' : 'rm-pill--danger']">{{ item.ready ? '已完善' : '待完善' }}</span>
                    <div class="rm-row__main">
                      <span class="rm-row__title">{{ item.label }}</span>
                      <span class="rm-row__meta">{{ item.note }} · 责任部门：{{ checklistOwner(item.key) }}</span>
                    </div>
                    <button v-if="canOpenAdmissionPath(checklistRoute(item.key))" type="button" class="rm-link" @click="jumpToChecklist(item.key)">{{ item.ready ? '查看资料' : '去补全' }}</button>
                  </div>
                </template>

                <template v-else-if="activeStepMeta.key === 'assessment'">
                  <a-descriptions v-if="caseAssessment" :column="2" size="small" bordered>
                    <a-descriptions-item label="评估日期">{{ caseAssessment.assessmentDate || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="状态">{{ caseAssessment.statusLabel || caseAssessment.status || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="评估等级">{{ caseAssessment.levelCode || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="评分">{{ caseAssessment.score ?? '--' }}</a-descriptions-item>
                    <a-descriptions-item label="评估人">{{ caseAssessment.assessorName || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="结论">{{ caseAssessment.resultSummary || '--' }}</a-descriptions-item>
                  </a-descriptions>
                  <p v-else class="adm-muted">{{ caseFacts.assessmentDone ? '合同已完成评估流转，暂未查到入住评估记录明细。' : '尚无入住评估记录。' }}</p>
                  <a-button v-if="canOpenAdmissionPath(ASSESSMENT_PATH)" style="margin-top: 12px" @click="router.push(admissionPath(ASSESSMENT_PATH))">{{ caseAssessment ? '查看/补充入住评估' : '去做入住评估' }}</a-button>
                </template>

                <template v-else-if="activeStepMeta.key === 'contract'">
                  <a-descriptions v-if="guardContract" :column="2" size="small" bordered>
                    <a-descriptions-item label="合同号">{{ guardContract.contractNo || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="合同阶段">{{ contractStageText(guardContract.flowStage) }}</a-descriptions-item>
                    <a-descriptions-item label="合同状态">{{ guardContract.contractStatus || guardContract.status || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="营销负责人">{{ guardContract.marketerName || '--' }}</a-descriptions-item>
                    <a-descriptions-item label="合同金额">{{ guardContract.contractAmount ?? guardContract.amount ?? '--' }}</a-descriptions-item>
                    <a-descriptions-item label="押金">{{ guardContract.depositAmount ?? '--' }}</a-descriptions-item>
                  </a-descriptions>
                  <p v-else class="adm-muted">该长者暂无可用合同，请先在合同签约中创建/确认合同。</p>
                  <p class="adm-muted">合同评估通过后即可办理入住；最终签署（待签署→已签署）在入住后由市场部完成。</p>
                  <a-button v-if="canOpenAdmissionPath(checklistRoute('contract'))" @click="jumpToChecklist('contract')">{{ guardContract ? '查看合同' : '去合同签约' }}</a-button>
                </template>

                <template v-else-if="activeStepMeta.key === 'fee'">
                  <a-descriptions v-if="caseFeeAudit" :column="2" size="small" bordered>
                    <a-descriptions-item label="审核状态">
                      <span :class="['rm-pill', feeStatusPill(caseFeeAudit.status)]">{{ feeStatusText(caseFeeAudit.status) }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item label="提交时间">{{ formatRecordTime(caseFeeAudit.createTime) }}</a-descriptions-item>
                    <a-descriptions-item label="首期费用（元）">{{ caseFeeAudit.totalAmount ?? '--' }}</a-descriptions-item>
                    <a-descriptions-item label="押金（元）">{{ caseFeeAudit.depositAmount ?? '--' }}</a-descriptions-item>
                    <a-descriptions-item label="审核备注" :span="2">{{ caseFeeAudit.auditRemark || '--' }}</a-descriptions-item>
                  </a-descriptions>
                  <div v-if="!caseFeeAudit || caseFeeAudit.status === 'REJECTED'" class="adm-fee-form">
                    <h4 class="rm-section-title">提交入住费用审核</h4>
                    <a-row :gutter="12">
                      <a-col :xs="24" :md="8"><a-form-item label="首期费用（元）"><a-input-number v-model:value="feeForm.totalAmount" :min="0" style="width: 100%" /></a-form-item></a-col>
                      <a-col :xs="24" :md="8"><a-form-item label="押金（元）"><a-input-number v-model:value="feeForm.depositAmount" :min="0" style="width: 100%" /></a-form-item></a-col>
                      <a-col :xs="24" :md="8"><a-form-item label="备注"><a-input v-model:value="feeForm.remark" :maxlength="200" /></a-form-item></a-col>
                    </a-row>
                    <a-button type="primary" ghost :loading="feeSubmitting" @click="submitFeeAudit">提交财务审核</a-button>
                  </div>
                  <a-button v-if="canOpenAdmissionPath(FEE_AUDIT_PATH)" style="margin-top: 12px" @click="router.push(FEE_AUDIT_PATH)">去财务审核页</a-button>
                </template>

                <template v-else-if="activeStepMeta.key === 'bed'">
                  <a-alert v-if="caseElder?.bedId" type="success" show-icon style="margin-bottom: 12px" :message="`已占用床位：${[caseElder.roomNo, caseElder.bedNo].filter(Boolean).join(' / ') || caseElder.bedId}`" />
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="8">
                      <a-form-item label="楼栋" name="buildingId">
                        <a-select v-model:value="assetSelect.buildingId" allow-clear placeholder="请选择楼栋" :options="buildingOptions" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="8">
                      <a-form-item label="楼层" name="floorId">
                        <a-select v-model:value="assetSelect.floorId" allow-clear placeholder="请选择楼层" :options="floorOptions" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="8">
                      <a-form-item label="房间" name="roomId">
                        <a-select v-model:value="assetSelect.roomId" allow-clear placeholder="请选择房间" :options="roomOptions" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="12">
                      <a-form-item label="入住方式" name="occupancyMode">
                        <a-radio-group v-model:value="form.occupancyMode" button-style="solid">
                          <a-radio-button value="BED">按床位入住</a-radio-button>
                          <a-radio-button value="WHOLE_ROOM">多人房整租</a-radio-button>
                        </a-radio-group>
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="12">
                      <a-form-item label="床位" name="bedId">
                        <a-select
                          v-model:value="form.bedId"
                          allow-clear
                          show-search
                          option-filter-prop="label"
                          :options="bedOptions"
                          :disabled="!assetSelect.roomId"
                          :placeholder="assetSelect.roomId ? '请选择床位' : '请先选择房间'"
                        >
                          <template #notFoundContent>
                            <a-empty :image="undefined" :description="assetSelect.roomId ? '该房间暂无空闲床位，可更换房间或到床位管理新增/释放床位' : '请先选择房间'" />
                          </template>
                        </a-select>
                        <div v-if="form.occupancyMode === 'WHOLE_ROOM'" class="adm-muted">整租会保留当前房型，并自动锁定同房其他床位。</div>
                      </a-form-item>
                    </a-col>
                  </a-row>
                </template>

                <template v-else>
                  <a-result v-if="caseFacts.admitted" status="success" title="已办理入住" :sub-title="`${caseName} 当前为在住状态。`" />
                  <template v-else>
                    <a-row :gutter="[12, 0]">
                      <a-col :xs="24" :md="12">
                        <a-form-item label="收费开始日期" name="admissionDate">
                          <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="12">
                        <a-form-item label="合同号（自动回填）" name="contractNo">
                          <a-input :value="form.contractNo" disabled placeholder="选择老人后自动填入，不可修改" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="12">
                        <a-form-item label="初始余额(押金)" name="depositAmount">
                          <a-input-number v-model:value="form.depositAmount" :min="0" style="width: 100%" placeholder="请输入押金金额" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="12">
                        <a-form-item label="操作员姓名">
                          <a-input :value="currentStaffDisplayName" disabled />
                        </a-form-item>
                      </a-col>
                      <a-col :span="24">
                        <a-form-item label="备注" name="remark">
                          <a-input v-model:value="form.remark" placeholder="请输入备注" />
                        </a-form-item>
                      </a-col>
                    </a-row>
                    <h4 class="rm-section-title">入住条件核验</h4>
                    <div v-for="step in admissionSteps.slice(0, 5)" :key="step.key" class="rm-row">
                      <span :class="['rm-pill', step.state === 'done' ? 'rm-pill--success' : 'rm-pill--danger']">{{ step.state === 'done' ? '已满足' : '未满足' }}</span>
                      <span class="rm-row__title">{{ step.label }}</span>
                    </div>
                    <a-alert v-if="missingGates.length" type="error" show-icon style="margin-top: 12px" :message="`还缺：${missingGates.join('、')}，满足后才能确认入住。`" />
                  </template>
                </template>

                <div v-if="form.elderId" class="adm-pane__footer">
                  <a-space wrap>
                    <a-button :loading="draftSaving" @click="saveDraft(false)">暂存草稿</a-button>
                    <a-button danger @click="returnOpen = true">退回补充</a-button>
                  </a-space>
                  <a-space wrap>
                    <a-button :disabled="activeStep <= 1" @click="activeStep -= 1">上一步</a-button>
                    <a-button v-if="activeStep < 6" type="primary" :loading="draftSaving" @click="saveDraft(true)">保存并下一步</a-button>
                    <a-button
                      v-else-if="!caseFacts.admitted"
                      type="primary"
                      :disabled="missingGates.length > 0"
                      :loading="submitting || submitChecklistLoading"
                      @click="openSubmitConfirm"
                    >确认入住</a-button>
                  </a-space>
                </div>
              </div>
            </a-spin>

            <aside class="adm-side">
              <div class="rm-card">
                <h4 class="rm-card__title adm-side__title">当前办理对象</h4>
                <div class="rm-row adm-object">
                  <span class="rm-avatar">{{ caseName ? caseName.slice(0, 1) : '--' }}</span>
                  <div class="rm-row__main">
                    <span class="rm-row__title">{{ caseName || '--' }}</span>
                    <span class="rm-row__meta">{{ caseProfileText }}</span>
                  </div>
                  <span v-if="caseElder" class="rm-pill rm-pill--warning">{{ residentStatusText(caseElder) }}</span>
                </div>
                <div class="adm-kv"><span>预计入住</span><strong>{{ caseDraft.admissionDate || '--' }}</strong></div>
                <div class="adm-kv"><span>当前完成度</span><strong>{{ form.elderId ? `${casePercent}%` : '--' }}</strong></div>
                <a-progress :percent="form.elderId ? casePercent : 0" :show-info="false" stroke-color="#21705f" />
                <h4 class="rm-section-title adm-side__section">办理节点状态</h4>
                <div v-for="(step, index) in admissionSteps" :key="step.key" class="rm-row">
                  <span class="adm-node__index">{{ index + 1 }}</span>
                  <span class="rm-row__main rm-row__title">{{ step.label }}</span>
                  <span :class="['rm-pill', step.state === 'done' ? 'rm-pill--success' : step.state === 'current' ? 'rm-pill--warning' : '']">
                    {{ step.state === 'done' ? '已完成' : step.state === 'current' ? '办理中' : '待办理' }}
                  </span>
                </div>
              </div>

              <div class="rm-card">
                <h4 class="rm-card__title adm-side__title">待办理名单 <span class="rm-card__sub">{{ pendingCases.length }} 人</span></h4>
                <div class="adm-queue">
                  <button v-for="item in pendingCases" :key="item.id" type="button" :class="['rm-row', 'adm-queue__item', { 'is-active': item.contractNo === form.contractNo }]" @click="pickCase(item)">
                    <span class="rm-avatar">{{ String(item.elderName || item.name || '?').slice(0, 1) }}</span>
                    <span class="rm-row__main">
                      <span class="rm-row__title">{{ item.elderName || item.name || '--' }}</span>
                      <span class="rm-row__meta">{{ item.contractNo || '无合同号' }}</span>
                    </span>
                    <span v-if="returnedElderIds.has(String(item.elderId || ''))" class="rm-pill rm-pill--danger">退回补充</span>
                    <span v-else :class="['rm-pill', item.flowStage === 'PENDING_ASSESSMENT' ? 'rm-pill--info' : 'rm-pill--warning']">{{ contractStageText(item.flowStage) }}</span>
                  </button>
                  <a-empty v-if="!pendingCases.length" description="暂无待办理入住" />
                </div>
              </div>
            </aside>
          </div>
        </a-form>
      </section>

      <div class="rm-note">合同、评估、费用、床位均满足业务条件后方可正式入住；未完成流程可随时暂存，后续继续办理。</div>

      <h3 id="admission-records" class="rm-section-title adm-records-title">入住记录</h3>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
        <QueryBar :model="recordQuery" layout="inline" class="search-bar">
          <a-form-item label="老人姓名">
            <ElderNameAutocomplete v-model:value="recordQuery.keyword" placeholder="请输入老人姓名(编号)" width="220px" @select="runRecordSearch" />
          </a-form-item>
          <a-form-item label="合同号">
            <a-input v-model:value="recordQuery.contractNo" placeholder="请输入合同号" allow-clear />
          </a-form-item>
          <a-form-item label="入住状态">
            <a-select v-model:value="recordQuery.elderStatus" allow-clear style="width: 140px" placeholder="请选择入住状态">
              <a-select-option :value="1">在院</a-select-option>
              <a-select-option :value="2">请假</a-select-option>
              <a-select-option :value="3">离院</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="收费开始日期">
            <a-range-picker v-model:value="recordQuery.admissionDateRange" value-format="YYYY-MM-DD" />
          </a-form-item>
          <template #actions>
            <ActionToolbar>
              <a-space>
                <a-button type="primary" @click="runRecordSearch">搜索</a-button>
                <a-button @click="resetRecordQuery">清空</a-button>
                <a-button @click="exportRecords">导出</a-button>
              </a-space>
            </ActionToolbar>
          </template>
        </QueryBar>
      </a-card>

      <a-card class="card-elevated admission-summary-card" :bordered="false" style="margin-top: 16px;">
        <a-spin :spinning="recordSummaryLoading">
          <div class="admission-summary-head">
            <span class="admission-summary-title">入住记录筛选统计</span>
            <span class="admission-summary-meta">更新时间：{{ recordSummaryGeneratedAtText }}</span>
          </div>
          <a-row :gutter="[12, 12]">
            <a-col v-for="item in recordSummaryCards" :key="item.key" :xs="12" :md="8" :xl="4">
              <div class="admission-summary-tile">
                <div class="admission-summary-label">{{ item.label }}</div>
                <div class="admission-summary-value">{{ item.value }}</div>
                <div class="admission-summary-hint">{{ item.hint }}</div>
              </div>
            </a-col>
          </a-row>
          <div class="admission-distribution-panel">
            <div class="admission-distribution-head">
              <span class="admission-distribution-title">入住空间分布占比</span>
              <a-space :size="8">
                <a-segmented
                  v-model:value="recordDimension"
                  :options="[
                    { label: '楼栋维度', value: 'BUILDING' },
                    { label: '楼层维度', value: 'FLOOR' }
                  ]"
                />
                <a-tag color="blue">样本 {{ Number(recordSummary.totalCount || 0) }} 人</a-tag>
              </a-space>
            </div>
            <div v-if="activeDistributionRows.length > 0" class="admission-distribution-list">
              <div
                v-for="item in activeDistributionRows"
                :key="`${recordDimension}-${item.dimensionKey}`"
                class="admission-distribution-item"
              >
                <div class="admission-distribution-row">
                  <span class="admission-distribution-label">{{ item.dimensionLabel }}</span>
                  <span class="admission-distribution-meta">{{ item.count }} 人 · {{ formatDistributionRatio(item.ratio) }}</span>
                </div>
                <div class="admission-distribution-track">
                  <div
                    class="admission-distribution-fill"
                    :style="{ width: `${Math.max(4, Math.round(Number(item.ratio || 0) * 100))}%` }"
                  />
                </div>
              </div>
            </div>
            <a-empty v-else description="当前筛选条件下暂无楼栋/楼层分布数据" />
          </div>
        </a-spin>
      </a-card>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
        <a-table :data-source="admissionRows" :columns="columns" :loading="recordLoading" :pagination="false" row-key="id" :scroll="{ x: 'max-content' }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'elderName'">
              {{ displayElderName(record) }}
            </template>
            <template v-else-if="column.key === 'elderStatus'">
              <a-tag :color="record.elderStatus === 1 ? 'green' : record.elderStatus === 2 ? 'orange' : 'default'">
                {{ residentStatusText(record.elderStatus) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'operatorName'">
              {{ record.operatorName || record.createByName || record.creatorName || '-' }}
            </template>
            <template v-else-if="column.key === 'createTime'">
              {{ formatRecordTime(record.createTime) }}
            </template>
          </template>
        </a-table>
        <a-pagination
          style="margin-top: 16px; text-align: right;"
          :current="recordQuery.pageNo"
          :page-size="recordQuery.pageSize"
          :total="recordTotal"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </a-card>
    </div>

    <a-modal
      v-model:open="submitConfirmOpen"
      title="提交确认"
      :confirm-loading="submitting"
      :ok-button-props="{ disabled: submitBlockingCount > 0 }"
      ok-text="确认提交"
      cancel-text="返回检查"
      @ok="confirmSubmit"
    >
      <a-space direction="vertical" style="width: 100%">
        <div>请确认以下信息填写及上传资料是否正确：</div>
        <a-list bordered size="small" :data-source="submitChecklist">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-space>
                <span>{{ item.label }}</span>
                <a-tag :color="item.ready ? 'green' : 'red'">{{ item.ready ? '已完善' : '待完善' }}</a-tag>
                <span style="color: var(--muted)">{{ item.note }}</span>
                <span>责任部门：{{ checklistOwner(item.key) }}</span>
                <a-button v-if="canOpenAdmissionPath(checklistRoute(item.key))" type="link" @click="jumpToChecklist(item.key)">{{ item.ready ? '查看资料' : '去补全' }}</a-button>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
        <a-alert
          type="warning"
          show-icon
          :message="`确认无误后将正式建立老人档案，后续资料修改需申请审批。当前待完善项：${submitChecklist.filter((item) => !item.ready).length}`"
        />
        <a-alert
          v-if="submitBlockingCount > 0"
          type="error"
          show-icon
          :message="`存在 ${submitBlockingCount} 项必须补全的资料（如身份证/户口本附件），补全前无法提交。点击上方条目可跳转补全。`"
        />
        <a-alert
          v-else-if="submitChecklist.some((item) => !item.ready)"
          type="warning"
          show-icon
          message="基础疾病信息未维护不影响本次提交，建议后续在健康档案中补全。"
        />
      </a-space>
    </a-modal>

    <a-modal v-model:open="returnOpen" title="退回补充" ok-text="确认退回" :confirm-loading="draftSaving" @ok="submitReturn">
      <a-textarea v-model:value="returnReason" :rows="3" :maxlength="200" show-count placeholder="请写明需要补充的资料，如：缺身份证复印件、家属签字" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { useCompletionGuide } from '../../composables/useCompletionGuide'
import ActionToolbar from '../../components/ActionToolbar.vue'
import QueryBar from '../../components/QueryBar.vue'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useElderOptions } from '../../composables/useElderOptions'
import { getFamilyRelations } from '../../api/family'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { bedSelectOption, isBedFree } from '../../utils/bedOptions'
import { admitElder, exportAdmissionRecords, getAdmissionProgress, getAdmissionRecordSummary, getAdmissionRecords, saveAdmissionProgress, type AdmissionProgressItem } from '../../api/elderLifecycle'
import { getElderDetail, getElderDiseases } from '../../api/elder'
import { getContractAttachments, getContractPage, getContractStageSummary, getVisitAppointmentPage } from '../../api/marketing'
import { normalizeId, normalizeResidentId } from '../../utils/id'
import { ageOf, residentStatusText, resolveResidentLifecycleStatus } from '../../utils/elderStatus'
import { withAdmissionContext } from '../../utils/admissionFlow'
import { getAssessmentRecordPage } from '../../api/assessment'
import { createAdmissionFeeAudit, getAdmissionFeeAuditPage } from '../../api/financeFee'
import { ADMISSION_STEPS, assetsFromRooms, completionPercent, isAssessmentDone, isBedAvailable, isContractValid, missingForConfirm, resolveAdmissionSteps, type AdmissionFacts } from './admissionSteps'
import { CalendarOutlined, FileDoneOutlined, HeartOutlined, WarningOutlined } from '@ant-design/icons-vue'
import './residentUi.css'
import AdmissionTrialPanel from './AdmissionTrialPanel.vue'
import { resolveAdmissionTab, type AdmissionTab } from './admissionTrial'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { useUserStore } from '../../stores/user'
import type {
  AdmissionFeeAuditItem,
  AdmissionRecordDimensionItem,
  AssessmentRecord,
  CrmContractStageSummary,
  ElderItem,
  AdmissionRecordItem,
  AdmissionRecordSummary,
  AdmissionRequest,
  BedItem,
  BuildingItem,
  CrmContractItem,
  FloorItem,
  Id,
  PageResult,
  RoomItem
} from '../../types'

const showCompletion = useCompletionGuide()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前由状态变更联动进入，建议优先核对床位交接与合同资料后再提交入住办理。' : ''
  }
})
const currentStaffDisplayName = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  if (realName) return realName
  return String(userStore.staffInfo?.username || '').trim()
})
const formRef = ref<FormInstance>()
const form = reactive<AdmissionRequest>({ elderId: '' as Id, admissionDate: '', bedId: undefined, bedStartDate: undefined, occupancyMode: 'BED' })
const submitting = ref(false)
const submitChecklistLoading = ref(false)
const submitConfirmOpen = ref(false)
const linkedLeadId = ref<Id>()
const guardContract = ref<CrmContractItem | null>(null)
const completedAdmission = ref('')
function admissionPath(path: string) {
  return withAdmissionContext(path, { elderId: form.elderId, contractNo: form.contractNo, leadId: linkedLeadId.value, bedId: form.bedId })
}
function canOpenAdmissionPath(path: string) {
  return resolveRouteAccess(router, userStore.roles, admissionPath(path), userStore.pagePermissions, userStore.permissions).canAccess
}
const { elderOptions, elderLoading, searchElders, findElderName, ensureSelectedElder } = useElderOptions({
  pageSize: 120,
  inHospitalOnly: false
})

// ---- 入住办理中心：六步办理（完成情况从既有业务数据推导） ----
const NEW_ADMISSION_PATH = '/marketing/contract-signing?quick=1'
const ASSESSMENT_PATH = '/elder/assessment/ability/admission?autoOpen=1&closeLoop=1'
const FEE_AUDIT_PATH = '/finance/admission-fee-audit'
const activeStep = ref(1)
const caseLoading = ref(false)
const caseElder = ref<ElderItem | null>(null)
const caseAssessments = ref<AssessmentRecord[]>([])
const caseFeeAudits = ref<AdmissionFeeAuditItem[]>([])
const caseProgress = ref<AdmissionProgressItem | null>(null)
const checklistLoaded = ref(false)
const pendingAssessmentContracts = ref<CrmContractItem[]>([])
const draftSaving = ref(false)
const returnOpen = ref(false)
const returnReason = ref('')
const feeSubmitting = ref(false)
const feeForm = reactive({ totalAmount: undefined as number | undefined, depositAmount: undefined as number | undefined, remark: '' })
const kpi = reactive({
  stage: null as CrmContractStageSummary | null,
  visitToday: null as number | null,
  todayAdmitted: null as number | null,
  returned: null as AdmissionProgressItem[] | null
})

const caseAssessment = computed(() => caseAssessments.value[0] || null)
const caseFeeAudit = computed(() => caseFeeAudits.value[0] || null)
const caseName = computed(() => caseElder.value?.fullName || guardContract.value?.elderName || (form.elderId ? findElderName(String(form.elderId)) : '') || '')
const caseProfileText = computed(() => {
  const elder = caseElder.value
  if (!elder) return '--'
  const gender = elder.gender === 1 ? '男' : elder.gender === 2 ? '女' : ''
  const age = ageOf(elder.birthDate)
  return [gender, age != null ? `${age}岁` : ''].filter(Boolean).join(' · ') || '--'
})
const caseDraft = computed<Partial<AdmissionRequest>>(() => {
  try {
    return caseProgress.value?.draftJson ? JSON.parse(caseProgress.value.draftJson) : {}
  } catch {
    return {}
  }
})
const caseFacts = computed<AdmissionFacts>(() => ({
  identityReady: checklistLoaded.value && submitBlockingCount.value === 0,
  assessmentDone: isAssessmentDone(caseAssessments.value, guardContract.value?.flowStage),
  contractValid: isContractValid(guardContract.value),
  feeConfirmed: caseFeeAudits.value.some((item) => item.status === 'APPROVED'),
  bedReady: Boolean(caseElder.value?.bedId) || isBedAvailable(beds.value.find((bed) => String(bed.id) === String(form.bedId || '')), form.elderId),
  admitted: completedAdmission.value === `${form.elderId}:${form.contractNo}` || resolveResidentLifecycleStatus(caseElder.value || undefined) === 'IN_HOSPITAL'
}))
const admissionSteps = computed(() => resolveAdmissionSteps(form.elderId ? caseFacts.value : { identityReady: false, assessmentDone: false, contractValid: false, feeConfirmed: false, bedReady: false, admitted: false }))
const activeStepMeta = computed(() => ADMISSION_STEPS[activeStep.value - 1])
const missingGates = computed(() => missingForConfirm(caseFacts.value))
const casePercent = computed(() => completionPercent(caseFacts.value))
const returnedElderIds = computed(() => new Set((kpi.returned || []).map((item) => String(item.elderId))))
const pendingCases = computed(() =>
  [...pendingBedSelectContracts.value, ...pendingAssessmentContracts.value].filter((item) => item.status !== 'VOID')
)

// ---- 试住登记（tab=trial；elderId 预选，create=1 打开新增） ----
const activeTab = computed(() => resolveAdmissionTab(route.query.tab))
const trialElderId = computed(() => normalizeResidentId(route.query as Record<string, unknown>) || undefined)

function switchTab(tab: AdmissionTab) {
  if (tab === activeTab.value) return
  const query = { ...route.query, tab: tab === 'trial' ? 'trial' : undefined, create: undefined }
  router.replace({ path: route.path, query }).catch(() => {})
}

/** 试住转正式入住：切回六步办理并把该长者设为当前办理对象 */
async function onTrialConvert(elderId: Id, elderName?: string) {
  switchTab('admission')
  ensureSelectedElder(String(elderId), elderName || undefined)
  form.elderId = elderId
  linkedLeadId.value = undefined
  await resolveContractNoByElder(elderId)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function kpiText(value?: number | null) {
  return value == null ? '--' : value
}

function contractStageText(stage?: string) {
  return ({ PENDING_ASSESSMENT: '待评估', PENDING_BED_SELECT: '待办理入住', PENDING_SIGN: '待签署', SIGNED: '已签署' } as Record<string, string>)[String(stage || '')] || '--'
}

function feeStatusText(status?: string) {
  return ({ PENDING: '待审核', APPROVED: '已确认', REJECTED: '已驳回' } as Record<string, string>)[String(status || '')] || status || '--'
}

function feeStatusPill(status?: string) {
  return status === 'APPROVED' ? 'rm-pill--success' : status === 'REJECTED' ? 'rm-pill--danger' : 'rm-pill--warning'
}

function scrollToRecords() {
  document.getElementById('admission-records')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function loadKpis() {
  const today = dayjs().format('YYYY-MM-DD')
  const [stage, visits, summary, returned] = await Promise.allSettled([
    getContractStageSummary(),
    getVisitAppointmentPage({ pageNo: 1, pageSize: 1, dateFrom: today, dateTo: today }, { silent403: true }),
    getAdmissionRecordSummary({ admissionDateStart: today, admissionDateEnd: today }),
    getAdmissionProgress({ status: 'RETURNED' })
  ])
  kpi.stage = stage.status === 'fulfilled' ? stage.value : null
  kpi.visitToday = visits.status === 'fulfilled' ? Number(visits.value.total || 0) : null
  kpi.todayAdmitted = summary.status === 'fulfilled' ? Number(summary.value?.totalCount || 0) : null
  kpi.returned = returned.status === 'fulfilled' ? returned.value || [] : null
}

async function loadPendingAssessmentContracts() {
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({ pageNo: 1, pageSize: 300, flowStage: 'PENDING_ASSESSMENT' })
    pendingAssessmentContracts.value = page.list || []
  } catch {
    pendingAssessmentContracts.value = []
  }
}

let caseSeq = 0
let loadedCaseElderId = ''
async function loadCase() {
  const seq = ++caseSeq
  const elderId = String(form.elderId || '').trim()
  if (!elderId) {
    caseElder.value = null
    caseAssessments.value = []
    caseFeeAudits.value = []
    caseProgress.value = null
    checklistLoaded.value = false
    loadedCaseElderId = ''
    return
  }
  caseLoading.value = true
  try {
    const [checklist, assessments, fees, progress] = await Promise.all([
      buildSubmitChecklist().catch(() => null),
      getAssessmentRecordPage({ pageNo: 1, pageSize: 5, elderId, assessmentType: 'ADMISSION' }).then((res) => res.list || []).catch(() => []),
      getAdmissionFeeAuditPage({ pageNo: 1, pageSize: 5, elderId }).then((res) => res.list || []).catch(() => []),
      getAdmissionProgress({ elderId }).then((res) => res?.[0] || null).catch(() => null)
    ])
    if (seq !== caseSeq) return
    checklistLoaded.value = Boolean(checklist)
    if (checklist) submitChecklist.value = checklist
    caseAssessments.value = assessments
    caseFeeAudits.value = fees
    caseProgress.value = progress
    // 实时刷新只更新数据；切换办理对象时才恢复草稿、预填费用并跳到上次步骤
    if (elderId === loadedCaseElderId) return
    loadedCaseElderId = elderId
    form.depositAmount = undefined
    form.remark = undefined
    const draft = caseDraft.value
    if (draft.admissionDate) form.admissionDate = draft.admissionDate
    if (draft.depositAmount != null) form.depositAmount = draft.depositAmount
    if (draft.remark) form.remark = draft.remark
    if (draft.occupancyMode) form.occupancyMode = draft.occupancyMode
    if (draft.bedId && !route.query.bedId) await applyBedRoutePrefill(draft.bedId)
    const approvedFee = fees.find((item) => item.status === 'APPROVED')
    if (form.depositAmount == null && approvedFee) form.depositAmount = Number(approvedFee.depositAmount)
    feeForm.totalAmount = guardContract.value?.contractAmount ?? guardContract.value?.amount ?? undefined
    feeForm.depositAmount = guardContract.value?.depositAmount ?? undefined
    // 继续上次步骤；没有记录时停在第一个未完成步骤
    const currentIndex = admissionSteps.value.findIndex((step) => step.state === 'current')
    activeStep.value = progress?.currentStep || (currentIndex >= 0 ? currentIndex + 1 : 6)
  } finally {
    if (seq === caseSeq) caseLoading.value = false
  }
}

function pickCase(item: CrmContractItem) {
  form.contractNo = String(item.contractNo || '').trim()
  form.elderId = (normalizeId(item.elderId) || '') as Id
  if (item.elderId) ensureSelectedElder(String(item.elderId), String(item.elderName || item.name || '').trim() || undefined)
  if (item.leadId) linkedLeadId.value = normalizeId(item.leadId)
  guardContract.value = item
}

function currentDraft() {
  return {
    admissionDate: form.admissionDate || undefined,
    depositAmount: form.depositAmount,
    remark: form.remark || undefined,
    occupancyMode: form.occupancyMode,
    bedId: form.bedId
  }
}

async function persistProgress(status: 'DRAFT' | 'RETURNED', currentStep: number, reason?: string) {
  caseProgress.value = await saveAdmissionProgress({
    elderId: form.elderId,
    contractNo: form.contractNo || undefined,
    currentStep,
    status,
    returnReason: reason,
    draftJson: JSON.stringify(currentDraft())
  })
  kpi.returned = await getAdmissionProgress({ status: 'RETURNED' }).catch(() => kpi.returned)
}

async function saveDraft(advance: boolean) {
  if (!form.elderId) {
    message.warning('请先选择办理对象')
    return
  }
  const step = advance ? Math.min(6, activeStep.value + 1) : activeStep.value
  draftSaving.value = true
  try {
    await persistProgress('DRAFT', step)
    activeStep.value = step
    message.success(advance ? '已保存，进入下一步' : '草稿已暂存')
  } catch (error: any) {
    message.error(error?.message || '暂存失败')
  } finally {
    draftSaving.value = false
  }
}

async function submitReturn() {
  const reason = returnReason.value.trim()
  if (!reason) {
    message.warning('请填写需要补充的资料')
    return
  }
  draftSaving.value = true
  try {
    await persistProgress('RETURNED', activeStep.value, reason)
    returnOpen.value = false
    returnReason.value = ''
    message.success('已退回补充')
  } catch (error: any) {
    message.error(error?.message || '退回失败')
  } finally {
    draftSaving.value = false
  }
}

async function submitFeeAudit() {
  if (!feeForm.totalAmount || feeForm.totalAmount <= 0) {
    message.warning('请填写首期费用')
    return
  }
  feeSubmitting.value = true
  try {
    await createAdmissionFeeAudit({
      elderId: String(form.elderId),
      totalAmount: feeForm.totalAmount,
      depositAmount: feeForm.depositAmount ?? 0,
      remark: feeForm.remark || undefined
    })
    message.success('已提交财务审核')
    caseFeeAudits.value = (await getAdmissionFeeAuditPage({ pageNo: 1, pageSize: 5, elderId: form.elderId })).list || []
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    feeSubmitting.value = false
  }
}
const pendingBedSelectContracts = ref<CrmContractItem[]>([])
const elderSearchKeyword = ref('')
const admissionElderOptions = computed(() => {
  const map = new Map<string, { label: string; value: string; name: string }>()
  ;(elderOptions.value || []).forEach((item) => {
    const id = String(item.value || '').trim()
    if (!id) return
    map.set(id, {
      label: String(item.label || item.name || id),
      value: id,
      name: String(item.name || item.label || id)
    })
  })
  // 合同来源的候选人也要跟随搜索关键词过滤，否则输入关键词后列表看起来“搜不动”
  const keyword = elderSearchKeyword.value.trim()
  ;(pendingBedSelectContracts.value || []).forEach((item) => {
    const elderId = String(item.elderId || '').trim()
    const elderName = String(item.elderName || item.name || '').trim()
    if (!elderId || !elderName || map.has(elderId)) return
    if (keyword && !elderName.includes(keyword)) return
    map.set(elderId, { label: elderName, value: elderId, name: elderName })
  })
  return Array.from(map.values())
})

function onElderSearch(keyword: string) {
  elderSearchKeyword.value = String(keyword || '')
  void searchElders(keyword)
}
const buildings = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])
const syncingAssetSelection = ref(false)

const assetSelect = reactive({
  buildingId: undefined as Id | undefined,
  floorId: undefined as Id | undefined,
  roomId: undefined as Id | undefined
})

const buildingOptions = computed(() =>
  buildings.value.map((b) => ({ label: b.name, value: b.id }))
)
const buildingNameById = computed(() =>
  new Map(buildings.value.map((item) => [String(item.id), String(item.name || '').trim()]))
)
const floorOptions = computed(() =>
  floors.value
    .filter((f) => (assetSelect.buildingId ? f.buildingId === assetSelect.buildingId : true))
    .map((f) => ({ label: f.floorNo, value: f.id }))
)
const floorNoById = computed(() =>
  new Map(floors.value.map((item) => [String(item.id), String(item.floorNo || '').trim()]))
)
const roomOptions = computed(() =>
  rooms.value
    .filter((r) => (assetSelect.floorId ? r.floorId === assetSelect.floorId : true))
    .map((r) => ({ label: r.roomNo, value: r.id }))
)
const roomById = computed(() =>
  new Map(rooms.value.map((item) => [String(item.id), item]))
)

function formatBedOptionLabel(bed: BedItem) {
  const room = roomById.value.get(String(bed.roomId || ''))
  const building = room?.buildingId != null ? buildingNameById.value.get(String(room.buildingId)) : ''
  const floorNo = room?.floorId != null ? floorNoById.value.get(String(room.floorId)) : ''
  const roomNo = String(room?.roomNo || bed.roomNo || '').trim()
  const bedNo = String(bed.bedNo || '').trim()
  return [building, floorNo, roomNo, bedNo].filter(Boolean).join(' / ') || bedNo || String(bed.id)
}

const bedOptions = computed(() =>
  beds.value
    .filter((b) => (assetSelect.roomId ? b.roomId === assetSelect.roomId : false))
    .filter(isBedFree)
    .map((b) => bedSelectOption(b, { leadId: linkedLeadId.value, contractNo: form.contractNo }, formatBedOptionLabel(b)))
)

const rules: Record<string, Rule[]> = {
  elderId: [{ required: true, message: '请选择老人姓名' }],
  admissionDate: [{ required: true, message: '请选择收费开始日期' }],
  occupancyMode: [{ required: true, message: '请选择入住方式' }],
  contractNo: [{ required: true, message: '请输入合同号' }]
}

const recordLoading = ref(false)
const recordSummaryLoading = ref(false)
const admissionRows = ref<AdmissionRecordItem[]>([])
const recordTotal = ref(0)
const recordSummary = reactive<AdmissionRecordSummary>({
  totalCount: 0,
  inHospitalCount: 0,
  leaveCount: 0,
  dischargedCount: 0,
  otherStatusCount: 0,
  withContractCount: 0,
  withoutContractCount: 0,
  todayAdmissionCount: 0,
  buildingDistribution: [],
  floorDistribution: []
})
const recordDimension = ref<'BUILDING' | 'FLOOR'>('BUILDING')
const skipNextRecordRouteWatch = ref(false)
const recordRouteSignature = ref('')
type SubmitChecklistKey = 'elder-base' | 'family' | 'disease' | 'contract' | 'attachment'
type SubmitChecklistItem = { key: SubmitChecklistKey; label: string; ready: boolean; note: string }
const submitChecklist = ref<SubmitChecklistItem[]>([
  { key: 'elder-base', label: '老人基础信息', ready: false, note: '待校验' },
  { key: 'family', label: '家属信息', ready: false, note: '待校验' },
  { key: 'disease', label: '基础疾病信息', ready: false, note: '待校验' },
  { key: 'contract', label: '合同信息', ready: false, note: '待校验' },
  { key: 'attachment', label: '身份证/资料附件', ready: false, note: '待校验' }
])
const submitBlockingCount = computed(() =>
  submitChecklist.value.filter((item) => !item.ready && item.key !== 'disease').length
)
const recordQuery = reactive({
  keyword: undefined as string | undefined,
  contractNo: undefined as string | undefined,
  elderStatus: undefined as number | undefined,
  admissionDateRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})
const RECORD_ROUTE_KEYS = new Set([
  'recordKeyword',
  'recordContractNo',
  'recordElderStatus',
  'recordAdmissionDateStart',
  'recordAdmissionDateEnd',
  'recordDimension',
  'recordPageNo',
  'recordPageSize'
])
const SORTED_RECORD_ROUTE_KEYS = [...RECORD_ROUTE_KEYS].sort()

const recordSummaryGeneratedAtText = computed(() =>
  recordSummary.generatedAt ? dayjs(recordSummary.generatedAt).format('MM-DD HH:mm:ss') : '-'
)
const recordSummaryCards = computed(() => {
  const cards = [
    {
      key: 'total',
      label: '筛选结果',
      value: Number(recordSummary.totalCount || 0),
      hint: `今日办理 ${Number(recordSummary.todayAdmissionCount || 0)}`
    },
    {
      key: 'inHospital',
      label: '在院',
      value: Number(recordSummary.inHospitalCount || 0),
      hint: '当前状态 = 在院'
    },
    {
      key: 'leave',
      label: '请假',
      value: Number(recordSummary.leaveCount || 0),
      hint: '当前状态 = 请假'
    },
    {
      key: 'discharged',
      label: '离院',
      value: Number(recordSummary.dischargedCount || 0),
      hint: '当前状态 = 离院'
    },
    {
      key: 'withContract',
      label: '有合同号',
      value: Number(recordSummary.withContractCount || 0),
      hint: `缺合同 ${Number(recordSummary.withoutContractCount || 0)}`
    },
    {
      key: 'other',
      label: '其他状态',
      value: Number(recordSummary.otherStatusCount || 0),
      hint: '异常状态核查'
    }
  ]
  return cards
})

const activeDistributionRows = computed<AdmissionRecordDimensionItem[]>(() => {
  const source = recordDimension.value === 'BUILDING'
    ? recordSummary.buildingDistribution
    : recordSummary.floorDistribution
  if (!Array.isArray(source)) {
    return []
  }
  return source
    .filter((item) => Number(item?.count || 0) > 0)
    .slice(0, 12)
})

function formatDistributionRatio(value: number) {
  const ratio = Number(value || 0)
  if (!Number.isFinite(ratio) || ratio <= 0) {
    return '0.0%'
  }
  return `${(ratio * 100).toFixed(1)}%`
}

function firstQueryText(value: unknown) {
  if (Array.isArray(value)) {
    return firstQueryText(value[0])
  }
  if (value == null) return ''
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

function buildRecordRouteSignature(source: Record<string, unknown>) {
  return SORTED_RECORD_ROUTE_KEYS.map((key) => `${key}:${firstQueryText(source[key])}`).join('|')
}

function resetRecordQueryState() {
  recordQuery.keyword = undefined
  recordQuery.contractNo = undefined
  recordQuery.elderStatus = undefined
  recordQuery.admissionDateRange = undefined
  recordDimension.value = 'BUILDING'
  recordQuery.pageNo = 1
  recordQuery.pageSize = 10
}

function applyRecordQueryFromRoute() {
  resetRecordQueryState()
  const keyword = firstQueryText(route.query.recordKeyword)
  const contractNo = firstQueryText(route.query.recordContractNo)
  const elderStatus = Number(firstQueryText(route.query.recordElderStatus))
  const admissionDateStart = firstQueryText(route.query.recordAdmissionDateStart)
  const admissionDateEnd = firstQueryText(route.query.recordAdmissionDateEnd)
  const dimension = firstQueryText(route.query.recordDimension).toUpperCase()
  const pageNo = Number(firstQueryText(route.query.recordPageNo))
  const pageSize = Number(firstQueryText(route.query.recordPageSize))

  if (keyword) recordQuery.keyword = keyword
  if (contractNo) recordQuery.contractNo = contractNo
  if (Number.isFinite(elderStatus) && elderStatus > 0) recordQuery.elderStatus = elderStatus
  if (admissionDateStart && admissionDateEnd) {
    recordQuery.admissionDateRange = [admissionDateStart, admissionDateEnd]
  }
  if (dimension === 'BUILDING' || dimension === 'FLOOR') {
    recordDimension.value = dimension
  }
  if (Number.isFinite(pageNo) && pageNo > 0) recordQuery.pageNo = pageNo
  if (Number.isFinite(pageSize) && pageSize > 0) recordQuery.pageSize = pageSize
}

function buildRecordRouteQuery() {
  const query: Record<string, string> = {}
  Object.entries(route.query || {}).forEach(([key, value]) => {
    if (RECORD_ROUTE_KEYS.has(key)) return
    const text = firstQueryText(value)
    if (!text) return
    query[key] = text
  })
  if (recordQuery.keyword) query.recordKeyword = recordQuery.keyword
  if (recordQuery.contractNo) query.recordContractNo = recordQuery.contractNo
  if (recordQuery.elderStatus) query.recordElderStatus = String(recordQuery.elderStatus)
  if (recordQuery.admissionDateRange?.[0]) query.recordAdmissionDateStart = recordQuery.admissionDateRange[0]
  if (recordQuery.admissionDateRange?.[1]) query.recordAdmissionDateEnd = recordQuery.admissionDateRange[1]
  query.recordDimension = recordDimension.value
  query.recordPageNo = String(recordQuery.pageNo)
  query.recordPageSize = String(recordQuery.pageSize)
  return query
}

async function syncRecordQueryToRoute() {
  const nextQuery = buildRecordRouteQuery()
  const currentQuery = normalizeRouteQueryMap(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length === nextKeys.length && nextKeys.every((key) => currentQuery[key] === nextQuery[key])) {
    return
  }
  skipNextRecordRouteWatch.value = true
  recordRouteSignature.value = buildRecordRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '合同号', dataIndex: 'contractNo', key: 'contractNo', width: 160 },
  { title: '收费开始日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 140 },
  { title: '押金（元）', dataIndex: 'depositAmount', key: 'depositAmount', width: 120 },
  { title: '入住状态', key: 'elderStatus', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作员姓名', key: 'operatorName', width: 140 },
  { title: '登记时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]
function displayElderName(record: AdmissionRecordItem) {
  const name = String(record.elderName || '').trim()
  if (name) return name
  const elderId = String(record.elderId || '').trim()
  if (elderId) {
    const fallback = String(findElderName(elderId) || '').trim()
    if (fallback) return fallback
  }
  return '-'
}

function formatRecordTime(value?: string) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (form.occupancyMode === 'WHOLE_ROOM' && !form.bedId) {
      message.warning('整租入住需要先选择一个主床位')
      return
    }
    if (submitting.value) return
    submitting.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    await admitElder(form)
    completedAdmission.value = `${form.elderId}:${form.contractNo}`
    message.success('入住办理成功')
    showCompletion('入住已办理', [
      { label: '查看长者档案', path: admissionPath('/elder/resident-360') },
      { label: '核对首期账单', path: admissionPath('/finance/bills/in-admission') },
      { label: '护理交接任务', path: admissionPath('/medical-care/care-task-board?source=contract_signed&entryScene=signed_onboarding') }
    ])
    await Promise.all([refreshAdmissionRecordPanel(), loadCase(), loadKpis()])
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function checklistRoute(key: SubmitChecklistKey) {
  const elderId = String(form.elderId || '').trim()
  const contractNo = encodeURIComponent(String(form.contractNo || '').trim())
  const toContractElderInfo = () => {
    if (contractNo) {
      return admissionPath(`/marketing/contract-signing?contractNo=${contractNo}&openEdit=1&focus=elder-info`)
    }
    return admissionPath('/marketing/contract-signing?quick=1')
  }
  if (key === 'elder-base') return toContractElderInfo()
  if (key === 'family') return toContractElderInfo()
  if (key === 'disease') return admissionPath('/medical-care/basic-diseases')
  if (key === 'contract') return admissionPath(contractNo ? `/marketing/contract-signing?contractNo=${contractNo}&openEdit=1` : '/marketing/contract-signing')
  if (key === 'attachment') {
    if (contractNo) {
      return admissionPath(`/marketing/contract-signing?contractNo=${contractNo}&openAttachment=1&attachmentType=HOUSEHOLD&from=admission_checklist`)
    }
    return admissionPath(elderId ? `/marketing/contract-signing?elderId=${encodeURIComponent(elderId)}&openAttachment=1&from=admission_checklist` : '/marketing/contract-signing')
  }
  return admissionPath('/elder/list')
}

function checklistOwner(key: SubmitChecklistKey) {
  return key === 'disease' ? '护理部 / 医务部' : '市场部'
}

function jumpToChecklist(key: SubmitChecklistKey) {
  if (!canOpenAdmissionPath(checklistRoute(key))) return
  submitConfirmOpen.value = false
  router.push(checklistRoute(key))
}

async function buildSubmitChecklist() {
  let elderId = String(form.elderId || '').trim()
  const contractNo = String(form.contractNo || '').trim()
  const contractPageRes = contractNo
    ? await getContractPage({ pageNo: 1, pageSize: 20, contractNo })
    : ({ list: [] as CrmContractItem[] })
  const contract = (contractPageRes.list || []).find((item) => String(item.contractNo || '').trim() === contractNo) || null
  if (!elderId && contract?.elderId != null) {
    elderId = String(contract.elderId).trim()
  }
  const [elderDetailRes, familyRes, diseaseRes] = await Promise.all([
    elderId ? getElderDetail(elderId) : Promise.resolve(null),
    elderId ? getFamilyRelations(elderId) : Promise.resolve([]),
    elderId ? getElderDiseases(elderId).catch(() => null) : Promise.resolve([])
  ])
  caseElder.value = elderDetailRes || null
  const attachments = contract?.id
    ? await getContractAttachments(contract.id)
    : []
  const hasIdentityOrHouseholdDoc = attachments.some((item) => {
    const type = String(item.attachmentType || '').toUpperCase()
    const name = String(item.fileName || '').toUpperCase()
    return (
      type.includes('ID')
      || type.includes('IDENT')
      || type.includes('HOUSEHOLD')
      || type.includes('HUKOU')
      || name.includes('身份证')
      || name.includes('户口')
      || name.includes('户籍')
    )
  })
  return [
    {
      key: 'elder-base' as const,
      label: '老人基础信息',
      ready: Boolean(elderDetailRes?.fullName && elderDetailRes?.idCardNo),
      note: elderDetailRes?.fullName && elderDetailRes?.idCardNo ? '姓名/身份证已填写' : '缺少姓名或身份证'
    },
    {
      key: 'family' as const,
      label: '家属信息',
      ready: Array.isArray(familyRes) && familyRes.length > 0,
      note: Array.isArray(familyRes) && familyRes.length > 0 ? `已绑定 ${familyRes.length} 位家属` : '未绑定家属'
    },
    {
      key: 'disease' as const,
      label: '基础疾病信息',
      ready: Array.isArray(diseaseRes) && diseaseRes.length > 0,
      note: diseaseRes === null ? '疾病资料暂未读取，可稍后由护理部核对' : Array.isArray(diseaseRes) && diseaseRes.length > 0 ? `已维护 ${diseaseRes.length} 项疾病信息` : '未维护疾病信息'
    },
    {
      key: 'contract' as const,
      label: '合同信息',
      ready: Boolean(contract && contract.contractNo),
      note: contract ? `合同号 ${contract.contractNo || '-'}（${contract.status || contract.flowStage || '待处理'}）` : '未找到合同'
    },
    {
      key: 'attachment' as const,
      label: '身份证/资料附件',
      ready: attachments.length > 0 && hasIdentityOrHouseholdDoc,
      note: attachments.length
        ? (hasIdentityOrHouseholdDoc
          ? `已上传 ${attachments.length} 份附件（含身份证或户口本）`
          : `已上传 ${attachments.length} 份附件（缺身份证/户口本）`)
        : '未上传附件'
    }
  ]
}

async function openSubmitConfirm() {
  if (!form.elderId) {
    message.warning('请先选择老人')
    return
  }
  if (!form.contractNo) {
    message.warning('请先自动回填合同号')
    return
  }
  submitChecklistLoading.value = true
  try {
    submitChecklist.value = await buildSubmitChecklist()
    submitConfirmOpen.value = true
  } catch (error: any) {
    message.error(error?.message || '入住资料检查失败，请重试；本次未提交入住')
  } finally {
    submitChecklistLoading.value = false
  }
}

async function confirmSubmit() {
  if (missingGates.value.length) {
    message.warning(`还缺：${missingGates.value.join('、')}，满足后才能确认入住`)
    return
  }
  if (submitBlockingCount.value > 0) {
    message.warning('存在关键资料未完善，请先返回检查并补齐后再提交')
    return
  }
  submitConfirmOpen.value = false
  await submit()
}

async function loadContractGuardState() {
  const contractNo = String(form.contractNo || '').trim()
  if (!contractNo) {
    guardContract.value = null
    return
  }
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 20,
      contractNo
    })
    guardContract.value = (page.list || []).find((item) => item.contractNo === contractNo) || null
    if (guardContract.value?.elderId) {
      form.elderId = guardContract.value.elderId as any
      ensureSelectedElder(String(guardContract.value.elderId), guardContract.value.elderName || undefined)
      if (!recordQuery.keyword && guardContract.value.elderName) {
        recordQuery.keyword = guardContract.value.elderName
      }
    }
  } catch {
    guardContract.value = null
  }
}

function contractPriority(item: CrmContractItem) {
  const status = String(item.status || '')
  const stage = String(item.flowStage || '')
  if (status === 'VOID') return -1
  if (stage === 'PENDING_BED_SELECT') return 500
  if (stage === 'PENDING_SIGN') return 450
  if (stage === 'SIGNED') return 400
  if (status === 'EFFECTIVE') return 350
  if (stage === 'PENDING_ASSESSMENT') return 300
  return 100
}

async function resolveContractNoByElder(elderIdRaw: string | number | undefined) {
  const elderId = String(elderIdRaw || '').trim()
  if (!elderId) {
    form.contractNo = ''
    guardContract.value = null
    return
  }
  const elderName = findElderName(elderId)
  const page: PageResult<CrmContractItem> = await getContractPage({
    pageNo: 1,
    pageSize: 200,
    elderId,
    elderName: elderName || undefined
  })
  const list = (page.list || []).filter((item) => String(item.elderId || '').trim() === elderId || !item.elderId)
  if (!list.length) {
    form.contractNo = ''
    guardContract.value = null
    message.warning('该老人暂无可用合同，请先在合同签约中创建/确认合同')
    return
  }
  const picked = [...list]
    .sort((a, b) => {
      const diff = contractPriority(b) - contractPriority(a)
      if (diff !== 0) return diff
      const ta = new Date(a.updateTime || a.createTime || 0).getTime()
      const tb = new Date(b.updateTime || b.createTime || 0).getTime()
      return tb - ta
    })[0]
  form.contractNo = String(picked.contractNo || '').trim()
  guardContract.value = picked || null
  if (picked.leadId && !linkedLeadId.value) {
    linkedLeadId.value = normalizeId(picked.leadId)
  }
}

async function loadPendingBedSelectContracts() {
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 300,
      flowStage: 'PENDING_BED_SELECT'
    })
    pendingBedSelectContracts.value = page.list || []
    pendingBedSelectContracts.value.forEach((item) => {
      if (!item?.elderId) return
      ensureSelectedElder(String(item.elderId), String(item.elderName || item.name || '').trim() || undefined)
    })
  } catch {
    pendingBedSelectContracts.value = []
  }
}

async function onElderChange(value: string | number | undefined) {
  form.elderId = (normalizeId(value) || ('' as Id)) as Id
  await resolveContractNoByElder(value)
}

function buildAdmissionRecordFilterParams() {
  const routeElderId = normalizeResidentId(route.query as Record<string, unknown>)
  const [admissionDateStart, admissionDateEnd] = recordQuery.admissionDateRange || []
  return {
    elderId: routeElderId,
    keyword: recordQuery.keyword,
    contractNo: recordQuery.contractNo,
    elderStatus: recordQuery.elderStatus,
    admissionDateStart,
    admissionDateEnd
  }
}

async function fetchAdmissionRecords() {
  recordLoading.value = true
  try {
    const res: PageResult<AdmissionRecordItem> = await getAdmissionRecords({
      pageNo: recordQuery.pageNo,
      pageSize: recordQuery.pageSize,
      ...buildAdmissionRecordFilterParams()
    })
    admissionRows.value = res.list || []
    admissionRows.value.forEach((item) => {
      const elderId = String(item.elderId || '').trim()
      if (elderId) {
        ensureSelectedElder(elderId, String(item.elderName || '').trim() || undefined)
      }
    })
    recordTotal.value = Number(res.total || 0)
  } finally {
    recordLoading.value = false
  }
}

async function fetchAdmissionRecordSummary() {
  recordSummaryLoading.value = true
  try {
    const data = await getAdmissionRecordSummary(buildAdmissionRecordFilterParams())
    Object.assign(recordSummary, {
      totalCount: Number(data?.totalCount || 0),
      inHospitalCount: Number(data?.inHospitalCount || 0),
      leaveCount: Number(data?.leaveCount || 0),
      dischargedCount: Number(data?.dischargedCount || 0),
      otherStatusCount: Number(data?.otherStatusCount || 0),
      withContractCount: Number(data?.withContractCount || 0),
      withoutContractCount: Number(data?.withoutContractCount || 0),
      todayAdmissionCount: Number(data?.todayAdmissionCount || 0),
      buildingDistribution: Array.isArray(data?.buildingDistribution)
        ? data.buildingDistribution.map((item) => ({
            dimensionKey: String(item?.dimensionKey || ''),
            dimensionLabel: String(item?.dimensionLabel || item?.dimensionKey || '-'),
            count: Number(item?.count || 0),
            ratio: Number(item?.ratio || 0)
          }))
        : [],
      floorDistribution: Array.isArray(data?.floorDistribution)
        ? data.floorDistribution.map((item) => ({
            dimensionKey: String(item?.dimensionKey || ''),
            dimensionLabel: String(item?.dimensionLabel || item?.dimensionKey || '-'),
            count: Number(item?.count || 0),
            ratio: Number(item?.ratio || 0)
          }))
        : [],
      generatedAt: data?.generatedAt
    })
  } catch {
    Object.assign(recordSummary, {
      totalCount: 0,
      inHospitalCount: 0,
      leaveCount: 0,
      dischargedCount: 0,
      otherStatusCount: 0,
      withContractCount: 0,
      withoutContractCount: 0,
      todayAdmissionCount: 0,
      buildingDistribution: [],
      floorDistribution: [],
      generatedAt: undefined
    })
  } finally {
    recordSummaryLoading.value = false
  }
}

async function refreshAdmissionRecordPanel() {
  await Promise.all([fetchAdmissionRecords(), fetchAdmissionRecordSummary()])
}

function runRecordSearch() {
  recordQuery.pageNo = 1
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

function resetRecordQuery() {
  resetRecordQueryState()
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

async function exportRecords() {
  await exportAdmissionRecords({
    ...buildAdmissionRecordFilterParams()
  })
}

async function loadAssets() {
  // 各列表权限不同，分别加载：某一项无权限时其余照常可用
  const [buildingList, floorList, roomList, bedList] = await Promise.allSettled([
    getBuildingList({ silent403: true }),
    getFloorList(undefined, { silent403: true }),
    getRoomList(),
    getBedList()
  ])
  rooms.value = roomList.status === 'fulfilled' ? roomList.value : []
  const fromRooms = assetsFromRooms(rooms.value)
  buildings.value = buildingList.status === 'fulfilled' ? buildingList.value : fromRooms.buildings
  floors.value = floorList.status === 'fulfilled' ? floorList.value : fromRooms.floors
  beds.value = bedList.status === 'fulfilled' ? bedList.value : []
}

async function applyBedRoutePrefill(bedId: Id | undefined) {
  const normalizedBedId = normalizeId(bedId)
  const routeBuildingId = normalizeId(route.query.buildingId)
  const routeFloorId = normalizeId(route.query.floorId)
  const routeRoomId = normalizeId(route.query.roomId)
  const matchedBed = normalizedBedId
    ? beds.value.find((item) => String(item.id || '').trim() === normalizedBedId)
    : undefined
  const matchedRoom = roomById.value.get(String(matchedBed?.roomId || routeRoomId || ''))
  const targetBuildingId = matchedRoom?.buildingId || routeBuildingId
  const targetFloorId = matchedRoom?.floorId || routeFloorId
  const targetRoomId = matchedBed?.roomId || routeRoomId
  if (!targetBuildingId && !targetFloorId && !targetRoomId && !matchedBed) {
    return
  }
  syncingAssetSelection.value = true
  assetSelect.buildingId = targetBuildingId
  assetSelect.floorId = targetFloorId
  assetSelect.roomId = targetRoomId
  form.bedId = matchedBed?.id || normalizedBedId
  await nextTick()
  syncingAssetSelection.value = false
}

function applyRoutePrefill() {
  applyRecordQueryFromRoute()
  const residentIdText = normalizeResidentId(route.query as Record<string, unknown>)
  const leadId = normalizeId(route.query.leadId)
  const contractNo = String(route.query.contractNo || '').trim()
  const elderName = String(route.query.elderName || '').trim()
  const bedId = normalizeId(route.query.bedId)
  if (residentIdText && !recordQuery.keyword) {
    ensureSelectedElder(residentIdText, elderName || findElderName(residentIdText) || undefined)
    form.elderId = residentIdText as Id
    recordQuery.keyword = elderName || findElderName(residentIdText) || ''
  }
  if (!recordQuery.keyword && elderName) {
    recordQuery.keyword = elderName
  }
  if (leadId) {
    linkedLeadId.value = leadId
  }
  if (contractNo) {
    form.contractNo = contractNo
    if (!recordQuery.contractNo) {
      recordQuery.contractNo = contractNo
    }
  }
  if (!form.admissionDate) {
    form.admissionDate = dayjs().format('YYYY-MM-DD')
  }
  if (bedId) {
    applyBedRoutePrefill(bedId as Id).catch(() => {})
  }
}

function onPageChange(page: number) {
  recordQuery.pageNo = page
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

function onPageSizeChange(current: number, size: number) {
  recordQuery.pageNo = 1
  recordQuery.pageSize = size
  refreshAdmissionRecordPanel()
  syncRecordQueryToRoute().catch(() => {})
}

watch(
  () => assetSelect.buildingId,
  () => {
    if (syncingAssetSelection.value) {
      return
    }
    assetSelect.floorId = undefined
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.floorId,
  () => {
    if (syncingAssetSelection.value) {
      return
    }
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.roomId,
  () => {
    if (syncingAssetSelection.value) {
      return
    }
    form.bedId = undefined
  }
)
watch(
  () => route.query,
  () => {
    const nextSignature = buildRecordRouteSignature(route.query as Record<string, unknown>)
    if (skipNextRecordRouteWatch.value) {
      skipNextRecordRouteWatch.value = false
      recordRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === recordRouteSignature.value) {
      return
    }
    recordRouteSignature.value = nextSignature
    applyRecordQueryFromRoute()
    refreshAdmissionRecordPanel()
  },
  { deep: true }
)
let caseWatchReady = false
watch(() => `${form.elderId || ''}|${form.contractNo || ''}`, () => {
  if (caseWatchReady) void loadCase()
})
watch(recordDimension, () => {
  syncRecordQueryToRoute().catch(() => {})
})

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care'],
  refresh: () => {
    searchElders('')
    loadPendingBedSelectContracts()
    loadPendingAssessmentContracts()
    loadAssets()
    refreshAdmissionRecordPanel()
    loadKpis()
    loadCase()
  },
  debounceMs: 700
})

onMounted(async () => {
  void loadKpis()
  await searchElders('')
  await Promise.all([loadPendingBedSelectContracts(), loadPendingAssessmentContracts(), loadAssets()])
  applyRoutePrefill()
  if (!form.contractNo && form.elderId) {
    await resolveContractNoByElder(form.elderId as any)
  } else if (form.contractNo) {
    await loadContractGuardState()
  }
  await Promise.all([refreshAdmissionRecordPanel(), loadCase()])
  caseWatchReady = true
  recordRouteSignature.value = buildRecordRouteSignature(route.query as Record<string, unknown>)
  await syncRecordQueryToRoute().catch(() => {})
})
</script>

<style scoped>
.adm-case { margin-bottom: 0; }
.adm-case__picker { flex: 0 1 320px; min-width: 220px; margin-bottom: 0; }
.adm-steps { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 10px; margin: 0 0 16px; padding: 0; list-style: none; }
.adm-step { display: flex; flex-direction: column; align-items: center; gap: 8px; width: 100%; padding: 14px 8px; border: 1px solid var(--rm-border); border-radius: 10px; background: #fff; color: var(--rm-muted); font: inherit; font-size: 13px; cursor: pointer; }
.adm-step:hover { border-color: var(--rm-green); }
.adm-step__index { display: grid; place-items: center; width: 26px; height: 26px; border-radius: 50%; background: #eff2f1; font-size: 12px; font-weight: 600; }
.adm-step.is-done .adm-step__index { background: var(--rm-green-soft); color: var(--rm-green); }
.adm-step.is-current { color: var(--rm-warning); }
.adm-step.is-current .adm-step__index { background: #fff5e1; color: var(--rm-warning); }
.adm-step.is-active { border-color: var(--rm-green); background: var(--rm-green-soft); color: var(--rm-green-dark); font-weight: 600; }
.adm-step.is-active .adm-step__index { background: var(--rm-green); color: #fff; }
.adm-pane__title { margin: 0 0 12px; font-size: 15px; font-weight: 600; color: var(--rm-text); }
.adm-pane__note { margin: 0 0 14px; }
.adm-pane__footer { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 12px; margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--rm-border); }
.adm-fee-form { margin-top: 16px; }
.adm-muted { margin: 8px 0; color: var(--rm-muted); font-size: 13px; }
.adm-side { display: flex; flex-direction: column; gap: 16px; }
.adm-side__title { margin-bottom: 12px; }
.adm-side__section { margin-top: 18px; }
.adm-object { padding: 12px; border: 1px solid var(--rm-border); border-radius: 10px; margin-bottom: 8px; }
.adm-kv { display: flex; justify-content: space-between; padding: 6px 0; color: var(--rm-muted); font-size: 13px; }
.adm-kv strong { color: var(--rm-text); }
.adm-node__index { display: grid; place-items: center; width: 22px; height: 22px; border-radius: 50%; background: #fff5e1; color: var(--rm-warning); font-size: 12px; }
.adm-queue { max-height: 360px; overflow-y: auto; }
.adm-queue__item { width: 100%; padding: 10px 8px; border: 0; border-bottom: 1px solid var(--rm-border); border-radius: 0; background: none; font: inherit; text-align: left; cursor: pointer; }
.adm-queue__item:hover, .adm-queue__item.is-active { background: var(--rm-green-soft); }
.adm-records-title { margin-top: 24px; }
.adm-tabs { margin-bottom: 4px; }

@media (max-width: 900px) {
  .adm-steps { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

.search-bar {
  margin-bottom: 12px;
}

.admission-summary-card {
  border: 1px solid #e6f4ff;
  background: linear-gradient(118deg, #f8fbff 0%, #f2f8ff 45%, #f9fcff 100%);
}

.admission-summary-head {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.admission-summary-title {
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.admission-summary-meta {
  color: var(--muted);
  font-size: 12px;
}

.admission-summary-tile {
  border-radius: 10px;
  border: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.8);
  padding: 10px;
  min-height: 88px;
}

.admission-summary-label {
  color: var(--muted);
  font-size: 12px;
}

.admission-summary-value {
  margin-top: 6px;
  color: var(--ink);
  font-size: 24px;
  line-height: 1.1;
  font-weight: 700;
}

.admission-summary-hint {
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
}

.admission-distribution-panel {
  margin-top: 14px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: linear-gradient(130deg, rgba(255, 255, 255, 0.95) 0%, rgba(241, 245, 249, 0.84) 100%);
}

.admission-distribution-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.admission-distribution-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}

.admission-distribution-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.admission-distribution-item {
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--border);
}

.admission-distribution-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.admission-distribution-label {
  color: var(--ink-soft);
  font-size: 12px;
  font-weight: 500;
}

.admission-distribution-meta {
  color: var(--muted);
  font-size: 12px;
}

.admission-distribution-track {
  height: 8px;
  border-radius: 999px;
  background: var(--border-strong);
  overflow: hidden;
}

.admission-distribution-fill {
  height: 100%;
  min-width: 4px;
  border-radius: inherit;
  background: linear-gradient(90deg, #3d7fa6 0%, #36cfc9 100%);
  transition: width 0.35s ease;
}

</style>
