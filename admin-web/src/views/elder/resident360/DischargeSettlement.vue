<template>
  <PageContainer
    class="rm-page"
    kicker="RESIDENT MANAGEMENT / 06"
    title="费用与退住"
    subTitle="账单一处查看，合同到期与退住结算、交接一站办理"
  >
    <template #extra>
      <a-space v-if="canManageDischarge">
        <a-button @click="openCreate('DEATH')">身故登记</a-button>
        <a-button type="primary" @click="openCreate('DISCHARGE')">
          <template #icon><LogoutOutlined /></template>
          新建退住
        </a-button>
      </a-space>
    </template>

    <div class="rm-kpis">
      <div class="rm-kpi">
        <span class="rm-kpi__icon"><FileTextOutlined /></span>
        <span class="rm-kpi__label">本月应收</span>
        <span class="rm-kpi__value">{{ summary ? money(summary.billTotalAmount) : '--' }}</span>
        <span class="rm-kpi__hint">按当月账单统计</span>
      </div>
      <div class="rm-kpi">
        <span class="rm-kpi__icon rm-kpi__icon--info"><CheckOutlined /></span>
        <span class="rm-kpi__label">本月实收</span>
        <span class="rm-kpi__value">{{ summary ? money(summary.billTotalAmount - summary.billOutstandingAmount) : '--' }}</span>
        <span class="rm-kpi__hint">已收款及抵扣核销</span>
      </div>
      <div class="rm-kpi">
        <span class="rm-kpi__icon rm-kpi__icon--danger"><WarningOutlined /></span>
        <span class="rm-kpi__label">欠费待处理</span>
        <span class="rm-kpi__value">{{ summary ? money(summary.billOutstandingAmount) : '--' }}</span>
        <span class="rm-kpi__hint">{{ summary ? `涉及 ${summary.billCount - summary.settledBillCount} 位长者` : '需财务权限查看' }}</span>
      </div>
      <button type="button" class="rm-kpi" @click="activeTab = 'discharge'">
        <span class="rm-kpi__icon rm-kpi__icon--warning"><LogoutOutlined /></span>
        <span class="rm-kpi__label">退住办理中</span>
        <span class="rm-kpi__value">{{ canManageDischarge ? inProgress.length : '--' }}<small v-if="canManageDischarge">人</small></span>
        <span class="rm-kpi__hint">
          {{ canManageDischarge ? `审批超时 ${timeoutCounts.REVIEW_TIMEOUT} · 办理超时 ${timeoutCounts.HANDLING_TIMEOUT}` : '待完成结算与交接' }}
        </span>
      </button>
    </div>

    <div class="rm-card">
      <div class="rm-card__head">
        <h3 class="rm-card__title">费用、合同与退住集中办理</h3>
        <a-segmented v-model:value="activeTab" class="rm-card__extra" :options="tabOptions" />
      </div>

      <template v-if="activeTab === 'fee'">
        <div v-if="!canFinance" class="rm-note">
          <SafetyCertificateOutlined />账单、收款与退款由财务部门办理，当前账号无查看权限。
        </div>
        <template v-else>
          <a-space wrap style="margin-bottom: 12px">
            <a-input-search
              v-model:value="billQuery.keyword"
              allow-clear
              placeholder="搜索长者姓名"
              style="width: 260px"
              @search="searchBills"
            />
            <a-date-picker
              v-model:value="billQuery.month"
              picker="month"
              value-format="YYYY-MM"
              :allow-clear="false"
              @change="searchBills"
            />
          </a-space>
          <a-table
            :scroll="{ x: 'max-content' }"
            row-key="id"
            size="middle"
            :loading="billLoading"
            :data-source="bills"
            :columns="billColumns"
            :pagination="billPagination"
            @change="onBillTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'items'">{{ feeItems(record) }}</template>
              <template v-else-if="column.key === 'totalAmount'">{{ money(record.totalAmount) }}</template>
              <template v-else-if="column.key === 'paidAmount'">{{ money(record.paidAmount) }}</template>
              <template v-else-if="column.key === 'status'">
                <span class="rm-pill" :class="billStatus(record).cls">{{ billStatus(record).text }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <button type="button" class="rm-link" @click="openBillDetail(record)">明细</button>
                  <button
                    v-if="Number(record.outstandingAmount || 0) > 0 && record.status !== 9"
                    type="button"
                    class="rm-link"
                    @click="goCollect(record)"
                  >处理</button>
                </a-space>
              </template>
            </template>
          </a-table>
          <div class="rm-note">
            <SafetyCertificateOutlined />收款、退款与销账在财务「在住账单」页办理，按财务岗位授权执行。
          </div>
        </template>
      </template>

      <template v-else-if="activeTab === 'contract'">
        <div v-if="!canReadContracts" class="rm-note">
          <SafetyCertificateOutlined />合同由营销、护理、医务、财务及机构管理人员查看，当前账号无查看权限。
        </div>
        <template v-else>
          <h4 class="rm-section-title">合同到期提醒</h4>
          <a-space wrap style="margin-bottom: 8px">
            <span class="rm-pill rm-pill--danger">已到期 {{ reminderCounts.EXPIRED }}</span>
            <span class="rm-pill rm-pill--warning">即将到期 {{ reminderCounts.EXPIRING }}</span>
            <span class="rm-card__sub">生效中合同到期日在 {{ CONTRACT_EXPIRY_WINDOW_DAYS }} 天内为即将到期；已过到期日且未续签为已到期</span>
          </a-space>
          <a-table
            :scroll="{ x: 'max-content' }"
            row-key="id"
            size="small"
            :loading="reminderLoading"
            :data-source="reminders"
            :columns="reminderColumns"
            :pagination="{ pageSize: 5, showSizeChanger: false, hideOnSinglePage: true }"
            :locale="{ emptyText: '暂无即将到期或已到期的合同' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'expiry'">
                <span class="rm-pill" :class="expiryPill[record.state as ContractExpiry]">{{ CONTRACT_EXPIRY_TEXT[record.state as ContractExpiry] }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <button v-if="record.elderId" type="button" class="rm-link" @click="goElderContract(record.elderId)">查看</button>
                <span v-else>--</span>
              </template>
            </template>
          </a-table>

          <h4 class="rm-section-title" style="margin-top: 20px">合同列表</h4>
          <a-space wrap style="margin-bottom: 12px">
            <a-input-search
              v-model:value="contractQuery.keyword"
              allow-clear
              placeholder="合同编号/长者姓名/联系人"
              style="width: 260px"
              @search="searchContracts"
            />
            <a-select
              v-model:value="contractQuery.status"
              allow-clear
              placeholder="合同状态"
              style="width: 140px"
              :options="contractStatusOptions"
              @change="searchContracts"
            />
          </a-space>
          <a-table
            :scroll="{ x: 'max-content' }"
            row-key="id"
            size="middle"
            :loading="contractLoading"
            :data-source="contracts"
            :columns="contractColumns"
            :pagination="contractPagination"
            @change="onContractTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <span class="rm-pill" :class="contractStatusPill(record.status)">{{ contractStatusText(record.status) }}</span>
              </template>
              <template v-else-if="column.key === 'signedAt'">{{ dateText(record.contractSignedAt) }}</template>
              <template v-else-if="column.key === 'expiry'">
                <span v-if="reminderState(record)" class="rm-pill" :class="expiryPill[reminderState(record)!]">
                  {{ CONTRACT_EXPIRY_TEXT[reminderState(record)!] }}
                </span>
                <span v-else>--</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <button v-if="record.elderId" type="button" class="rm-link" @click="goElderContract(record.elderId)">合同资料</button>
                <span v-else>--</span>
              </template>
            </template>
          </a-table>
          <div class="rm-note">
            <SafetyCertificateOutlined />合同附件、评估报告等单人资料在长者详情「合同」页查看；签约与变更在营销「合同管理」办理。
          </div>
        </template>
      </template>

      <template v-else>
        <div v-if="!canManageDischarge" class="rm-note">
          <SafetyCertificateOutlined />退住办理由护理、医务、人事及机构管理人员负责，当前账号无查看权限。
        </div>
        <template v-else>
          <div v-if="timeoutCounts.REVIEW_TIMEOUT || timeoutCounts.HANDLING_TIMEOUT" class="rm-note rm-note--warning" style="margin: 0 0 12px">
            <WarningOutlined />
            <span>
              退住超时提醒：
              <button type="button" class="rm-link" @click="showTimeouts('REVIEW_TIMEOUT')">审批超时 {{ timeoutCounts.REVIEW_TIMEOUT }}</button>
              （待审批超过 {{ DISCHARGE_REVIEW_TIMEOUT_DAYS }} 天）·
              <button type="button" class="rm-link" @click="showTimeouts('HANDLING_TIMEOUT')">办理超时 {{ timeoutCounts.HANDLING_TIMEOUT }}</button>
              （已通过未归档且已过计划退住日期）
            </span>
          </div>

          <a-space wrap style="margin-bottom: 12px">
            <a-select v-model:value="exitQuery.departureType" style="width: 120px" :options="departureTypeOptions" @change="onDepartureTypeChange" />
            <a-select
              v-model:value="exitQuery.elderId"
              allow-clear
              show-search
              :filter-option="false"
              :options="elderOptions"
              :loading="elderLoading"
              style="width: 200px"
              placeholder="长者姓名/拼音首字母"
              @search="searchElders"
              @focus="() => !elderOptions.length && searchElders('')"
            />
            <a-select v-model:value="exitQuery.status" style="width: 120px" :options="exitStatusOptions" />
            <a-input v-model:value="exitQuery.keyword" allow-clear placeholder="原因/备注/证明编号" style="width: 200px" />
            <a-range-picker
              v-if="exitQuery.departureType !== 'DEATH'"
              v-model:value="exitQuery.plannedDateRange"
              value-format="YYYY-MM-DD"
              :placeholder="['计划退住起', '计划退住止']"
            />
            <a-button type="primary" @click="loadExitRows">搜索</a-button>
            <a-button @click="resetExitQuery">清空</a-button>
            <a-button @click="exportExitRows">导出</a-button>
          </a-space>
          <div v-if="timeoutFilter" class="rm-card__sub" style="margin-bottom: 8px">
            仅显示{{ DISCHARGE_TIMEOUT_TEXT[timeoutFilter] }}的退住
            <button type="button" class="rm-link" @click="timeoutFilter = null">显示全部</button>
          </div>

          <div class="rm-split">
            <div>
              <a-table
                class="exit-table"
                :scroll="{ x: 'max-content' }"
                row-key="key"
                size="middle"
                :loading="exitLoading"
                :data-source="visibleExitRows"
                :columns="exitColumns"
                :pagination="{ pageSize: 10, showSizeChanger: false, hideOnSinglePage: true }"
                :row-class-name="exitRowClass"
                :custom-row="(row: ExitRow) => ({ onClick: () => (selectedKey = row.key) })"
                :locale="{ emptyText: '暂无符合条件的退住/身故记录' }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'kind'">
                    <span class="rm-pill" :class="record.kind === 'DEATH' ? 'rm-pill--danger' : 'rm-pill--info'">
                      {{ record.kind === 'DEATH' ? '身故' : '退住' }}
                    </span>
                  </template>
                  <template v-else-if="column.key === 'progress'">
                    <span class="rm-pill" :class="rowPill(record).cls">{{ rowPill(record).text }}</span>
                  </template>
                  <template v-else-if="column.key === 'timeout'">
                    <span v-if="record.timeout" class="rm-pill rm-pill--danger">{{ DISCHARGE_TIMEOUT_TEXT[record.timeout as DischargeTimeout] }}</span>
                    <span v-else>--</span>
                  </template>
                </template>
              </a-table>
              <div v-if="exitTruncated" class="rm-card__sub">仅显示最近 {{ EXIT_FETCH_LIMIT }} 条，请用筛选条件缩小范围或导出查看全部。</div>
            </div>

            <div v-if="selectedRow?.apply" class="rm-card">
              <h4 class="rm-section-title">退住办理进度 · {{ selectedRow.elderName }}</h4>
              <div class="discharge-meta">
                <UserOutlined /> 申请 {{ selectedRow.apply.applyDate || '--' }} · 预计退住 {{ selectedRow.apply.plannedDischargeDate || '--' }}
                <template v-if="selectedRow.apply.proofFileUrl">
                  · <a :href="selectedRow.apply.proofFileUrl" target="_blank" rel="noreferrer">证明附件</a>
                </template>
              </div>
              <div v-for="(step, index) in steps" :key="step.key" class="rm-row">
                <span class="step-index" :class="`is-${step.state}`">
                  <CheckOutlined v-if="step.state === 'done'" />
                  <template v-else>{{ index + 1 }}</template>
                </span>
                <div class="rm-row__main">
                  <span class="rm-row__title">{{ step.title }}</span>
                  <span v-if="stepHint(step)" class="rm-row__meta">{{ stepHint(step) }}</span>
                </div>
                <span class="rm-pill" :class="stepPill[step.state]">{{ step.stateText }}</span>
              </div>
              <a-space wrap style="margin-top: 16px">
                <a-button
                  type="primary"
                  :disabled="!nextAction.enabled"
                  :loading="acting"
                  @click="continueDischarge"
                >
                  <template #icon><ArrowRightOutlined /></template>
                  {{ nextAction.label }}
                </a-button>
                <a-button
                  v-if="canFinance && nextDischargeAction(selectedRow.apply) === 'SETTLEMENT'"
                  @click="goFinance('/finance/discharge/review', selectedRow.apply)"
                >退住费用审核</a-button>
                <a-popconfirm
                  v-if="selectedRow.apply.status !== 'APPROVED'"
                  :title="selectedRow.apply.status === 'PENDING' ? '撤销该退住申请？' : '删除该退住申请记录？'"
                  @confirm="removeApply(selectedRow.apply)"
                >
                  <a-button>{{ selectedRow.apply.status === 'PENDING' ? '取消申请' : '删除' }}</a-button>
                </a-popconfirm>
              </a-space>
              <div class="rm-note rm-note--warning">
                完成护理/医务交接与物品及房间交接后，财务方可结清费用；结清后系统才将长者置为已退住并释放床位。
              </div>
            </div>

            <div v-else-if="selectedRow?.death" class="rm-card">
              <h4 class="rm-section-title">身故登记 · {{ selectedRow.elderName }}</h4>
              <a-descriptions :column="1" size="small">
                <a-descriptions-item label="登记状态">
                  <span class="rm-pill" :class="rowPill(selectedRow).cls">{{ rowPill(selectedRow).text }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="死亡日期">{{ selectedRow.death.deathDate || '--' }}</a-descriptions-item>
                <a-descriptions-item label="死亡时间">{{ dateTimeText(selectedRow.death.deathTime) }}</a-descriptions-item>
                <a-descriptions-item label="地点">{{ selectedRow.death.place || '--' }}</a-descriptions-item>
                <a-descriptions-item label="死亡原因">{{ selectedRow.death.cause || '--' }}</a-descriptions-item>
                <a-descriptions-item label="证明编号">{{ selectedRow.death.certificateNo || '--' }}</a-descriptions-item>
                <a-descriptions-item label="上报人">{{ selectedRow.death.reportedBy || '--' }}</a-descriptions-item>
                <a-descriptions-item label="上报时间">{{ dateTimeText(selectedRow.death.reportedTime) }}</a-descriptions-item>
                <a-descriptions-item v-if="selectedRow.death.cancelledTime" label="作废时间">{{ dateTimeText(selectedRow.death.cancelledTime) }}</a-descriptions-item>
                <a-descriptions-item label="备注">{{ selectedRow.death.remark || '--' }}</a-descriptions-item>
              </a-descriptions>
              <a-space wrap style="margin-top: 12px">
                <template v-if="selectedRow.death.status !== 'CANCELLED'">
                  <a-button @click="openEditDeath(selectedRow.death)">更正</a-button>
                  <a-popconfirm title="作废该身故登记？" @confirm="cancelDeath(selectedRow.death)">
                    <a-button danger>作废</a-button>
                  </a-popconfirm>
                </template>
                <a-popconfirm v-else title="删除该已作废的登记记录？" @confirm="removeDeath(selectedRow.death)">
                  <a-button danger>删除</a-button>
                </a-popconfirm>
              </a-space>
              <div class="rm-note rm-note--warning">
                身故登记提交即释放床位并将长者置为离院，不经过交接、结算与归档步骤；费用请在「费用管理」核对。
              </div>
            </div>
          </div>
        </template>
      </template>
    </div>

    <a-drawer v-model:open="detailOpen" width="640" :title="detailTitle">
      <a-spin :spinning="detailLoading">
        <template v-if="detail">
          <a-descriptions :column="3" size="small" bordered>
            <a-descriptions-item label="应收">{{ money(detail.totalAmount) }}</a-descriptions-item>
            <a-descriptions-item label="已收">{{ money(detail.paidAmount) }}</a-descriptions-item>
            <a-descriptions-item label="欠费">{{ money(detail.outstandingAmount) }}</a-descriptions-item>
          </a-descriptions>
          <h4 class="rm-section-title" style="margin-top: 16px">费用明细</h4>
          <a-table row-key="itemName" size="small" :pagination="false" :data-source="detail.items" :columns="detailItemColumns">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'amount'">{{ money(record.amount) }}</template>
            </template>
          </a-table>
          <h4 class="rm-section-title" style="margin-top: 16px">收款记录</h4>
          <a-table row-key="id" size="small" :pagination="false" :data-source="detail.payments" :columns="paymentColumns">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'amount'">{{ money(record.amount) }}</template>
            </template>
          </a-table>
          <a-button type="link" style="padding: 0; margin-top: 12px" @click="router.push(`/finance/bill/${detail.billId}`)">
            查看完整账单
          </a-button>
        </template>
      </a-spin>
    </a-drawer>

    <a-drawer v-model:open="createOpen" width="520" :title="createTitle" :mask-closable="false">
      <a-segmented
        v-if="!editingDeathId"
        v-model:value="createMode"
        block
        style="margin-bottom: 16px"
        :options="[{ label: '退住', value: 'DISCHARGE' }, { label: '身故', value: 'DEATH' }]"
      />
      <a-form v-if="createMode === 'DISCHARGE'" ref="applyFormRef" :model="applyForm" :rules="applyRules" layout="vertical">
        <a-form-item label="长者" name="elderId">
          <a-select
            v-model:value="applyForm.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请输入长者姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="计划退住日期" name="plannedDischargeDate">
          <a-date-picker v-model:value="applyForm.plannedDischargeDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="申请原因" name="reason">
          <a-textarea v-model:value="applyForm.reason" :rows="4" placeholder="请填写退住申请原因" />
        </a-form-item>
        <a-form-item label="证明附件">
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadProof">
              <a-button :loading="proofUploading">上传附件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(applyForm.proofFileUrl, '未上传') }}</a-typography-text>
            <a v-if="applyForm.proofFileUrl" :href="applyForm.proofFileUrl" target="_blank" rel="noreferrer">查看</a>
          </a-space>
        </a-form-item>
      </a-form>
      <template v-else>
        <a-alert
          v-if="!editingDeathId"
          type="warning"
          show-icon
          style="margin-bottom: 16px"
          message="提交后立即释放该长者床位并置为离院（身故），请核实后再登记。"
        />
        <a-form ref="deathFormRef" :model="deathForm" :rules="deathRules" layout="vertical">
          <a-form-item label="长者" name="elderId">
            <a-select
              v-model:value="deathForm.elderId"
              show-search
              :disabled="!!editingDeathId"
              :filter-option="false"
              :options="elderOptions"
              :loading="elderLoading"
              placeholder="请输入长者姓名/拼音首字母"
              @search="searchElders"
              @focus="() => !elderOptions.length && searchElders('')"
            />
          </a-form-item>
          <a-form-item label="死亡日期" name="deathDate">
            <a-date-picker v-model:value="deathForm.deathDate" value-format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>
          <a-form-item label="死亡时间">
            <a-date-picker v-model:value="deathForm.deathTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
          </a-form-item>
          <a-form-item label="地点"><a-input v-model:value="deathForm.place" /></a-form-item>
          <a-form-item label="死亡原因"><a-input v-model:value="deathForm.cause" /></a-form-item>
          <a-form-item label="死亡证明编号"><a-input v-model:value="deathForm.certificateNo" /></a-form-item>
          <a-form-item label="上报人"><a-input v-model:value="deathForm.reportedBy" /></a-form-item>
          <a-form-item label="上报时间">
            <a-date-picker v-model:value="deathForm.reportedTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
          </a-form-item>
          <a-form-item label="备注"><a-input v-model:value="deathForm.remark" /></a-form-item>
        </a-form>
      </template>
      <template #footer>
        <a-space style="float: right">
          <a-button @click="createOpen = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="submitCreate">提交</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="reviewOpen" title="退住申请审批" :confirm-loading="acting" @ok="submitReview">
      <a-form layout="vertical">
        <a-form-item label="审批结果">
          <a-radio-group v-model:value="reviewForm.status">
            <a-radio value="APPROVED">通过</a-radio>
            <a-radio value="REJECTED">驳回</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="reviewForm.status === 'APPROVED'" label="审核清单">
          <a-space direction="vertical">
            <a-checkbox v-model:checked="reviewChecklist.proofChecked">退住原因与附件已核验</a-checkbox>
            <a-checkbox v-model:checked="reviewChecklist.planChecked">已确认计划退住日期与沟通对象</a-checkbox>
            <a-checkbox v-model:checked="reviewChecklist.financeHandoffReady">确认通过后进入交接与退住结算</a-checkbox>
          </a-space>
        </a-form-item>
        <a-form-item label="审批备注" :required="reviewForm.status === 'REJECTED'">
          <a-textarea v-model:value="reviewForm.reviewRemark" :rows="3" :maxlength="200" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  ArrowRightOutlined,
  CheckOutlined,
  FileTextOutlined,
  LogoutOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import { getBillPage } from '../../../api/bill'
import { uploadElderFile } from '../../../api/elder'
import { getFinanceBillDetail } from '../../../api/finance'
import { getFinanceHomeSummary, type FinanceHomeSummary } from '../../../api/financeHome'
import { getDischargeSettlementPage } from '../../../api/financeFee'
import { getContractPage } from '../../../api/marketing'
import {
  cancelDeathRegister,
  completeDischargeStep,
  createDeathRegister,
  createDischargeApply,
  deleteDeathRegister,
  deleteDischargeApply,
  exportDeathRegister,
  exportDischargeApply,
  getDeathRegisterPage,
  getDischargeApplyPage,
  reviewDischargeApply,
  updateDeathRegister
} from '../../../api/elderResidence'
import { useElderOptions } from '../../../composables/useElderOptions'
import { useUserStore } from '../../../stores/user'
import { normalizeResidentId } from '../../../utils/id'
import type {
  BillItem,
  CrmContractItem,
  DeathRegisterCreateRequest,
  DeathRegisterItem,
  DeathRegisterStatus,
  DischargeApplyCreateRequest,
  DischargeApplyItem,
  DischargeApplyReviewRequest,
  DischargeApplyStatus,
  DischargeSettlementItem,
  FinanceBillDetail,
  Id
} from '../../../types'
import { buildDischargeSteps, isDischargeInProgress, nextDischargeAction, type DischargeStep, type DischargeStepState } from './dischargeProgress'
import {
  CONTRACT_EXPIRY_TEXT,
  CONTRACT_EXPIRY_WINDOW_DAYS,
  DISCHARGE_REVIEW_TIMEOUT_DAYS,
  DISCHARGE_TIMEOUT_TEXT,
  contractExpiry,
  contractExpiryReminderTo,
  countDischargeTimeouts,
  dischargeTimeout,
  type ContractExpiry,
  type DischargeTimeout
} from './exitReminders'
import '../residentUi.css'

type Tab = 'fee' | 'contract' | 'discharge'
type Departure = 'DISCHARGE' | 'DEATH'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 与后端 @PreAuthorize 保持一致：FeeManagement/Bill/FinanceHome 控制器、ElderAuthz.canManageDischarge/canManageOuting/canApproveDischarge、CrmContractController
const hasRole = (roles: string[]) => (userStore.roles || []).some((role: string) => roles.includes(role))
const ADMINS = ['DIRECTOR', 'SYS_ADMIN', 'ADMIN']
const canFinance = computed(() => hasRole(['FINANCE_EMPLOYEE', 'FINANCE_MINISTER', ...ADMINS]))
// 退住申请(canManageDischarge)与死亡登记(canManageOuting)的角色集合相同
const canManageDischarge = computed(() =>
  hasRole(['HR_EMPLOYEE', 'HR_MINISTER', 'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', ...ADMINS])
)
const canApproveDischarge = computed(() => hasRole(['MEDICAL_MINISTER', 'NURSING_MINISTER', ...ADMINS]))
const canReadContracts = computed(() =>
  hasRole(['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'MARKETING_EMPLOYEE', 'MARKETING_MINISTER', ...ADMINS])
)

const residentId = computed(() => normalizeResidentId(route.query as Record<string, unknown>))
const activeTab = ref<Tab>('fee')
const tabOptions = [
  { label: '费用管理', value: 'fee' },
  { label: '合同', value: 'contract' },
  { label: '退住办理', value: 'discharge' }
]

const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 80 })

function money(value?: number) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 2 })}`
}
const dateText = (value?: string) => (value ? value.slice(0, 10) : '--')
const dateTimeText = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '--')

// ---------- 费用管理 ----------
const summary = ref<FinanceHomeSummary | null>(null)
const bills = ref<BillItem[]>([])
const billLoading = ref(false)
const billQuery = reactive({ keyword: '', month: dayjs().format('YYYY-MM'), pageNo: 1, pageSize: 10 })
const billTotal = ref(0)
const billPagination = computed(() => ({ current: billQuery.pageNo, pageSize: billQuery.pageSize, total: billTotal.value, showSizeChanger: false }))
const billColumns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName' },
  { title: '费用项目', key: 'items' },
  { title: '应收', key: 'totalAmount', align: 'right' as const },
  { title: '实收', key: 'paidAmount', align: 'right' as const },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 110 }
]

function feeItems(bill: BillItem) {
  const parts = [
    bill.bedFee ? '床位' : '',
    bill.nursingFee ? '护理' : '',
    bill.insuranceFee ? '保险' : ''
  ].filter(Boolean)
  return parts.join('/') || '--'
}

function billStatus(bill: BillItem) {
  if (bill.status === 9) return { text: '已作废', cls: '' }
  if (Number(bill.outstandingAmount || 0) <= 0) return { text: '已结清', cls: 'rm-pill--success' }
  if (Number(bill.paidAmount || 0) > 0) return { text: '部分未缴', cls: 'rm-pill--warning' }
  return { text: '未缴', cls: 'rm-pill--danger' }
}

async function loadSummary() {
  try {
    summary.value = await getFinanceHomeSummary(billQuery.month)
  } catch {
    summary.value = null
  }
}

async function loadBills() {
  billLoading.value = true
  try {
    const res = await getBillPage({
      pageNo: billQuery.pageNo,
      pageSize: billQuery.pageSize,
      month: billQuery.month,
      scene: 'RESIDENT',
      elderId: residentId.value,
      keyword: billQuery.keyword || undefined
    })
    bills.value = res.list || []
    billTotal.value = res.total || 0
  } finally {
    billLoading.value = false
  }
}

function searchBills() {
  billQuery.pageNo = 1
  loadBills()
  loadSummary()
}

function onBillTableChange(pagination: { current?: number }) {
  billQuery.pageNo = pagination.current || 1
  loadBills()
}

function goCollect(bill: BillItem) {
  router.push({ path: '/finance/bills/in-resident', query: { elderId: String(bill.elderId), billMonth: bill.billMonth || billQuery.month } })
}

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref<FinanceBillDetail | null>(null)
const detailTitle = computed(() => (detail.value ? `${detail.value.elderName || ''} · ${detail.value.billMonth} 账单明细` : '账单明细'))
const detailItemColumns = [
  { title: '项目', dataIndex: 'itemName' },
  { title: '计费依据', dataIndex: 'basis' },
  { title: '金额', key: 'amount', align: 'right' as const }
]
const paymentColumns = [
  { title: '收款时间', dataIndex: 'paidAt' },
  { title: '方式', dataIndex: 'payMethod' },
  { title: '经办人', dataIndex: 'operatorStaffName' },
  { title: '实收', key: 'amount', align: 'right' as const }
]

async function openBillDetail(bill: BillItem) {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getFinanceBillDetail(bill.id)
  } finally {
    detailLoading.value = false
  }
}

// ---------- 合同 ----------
const contracts = ref<CrmContractItem[]>([])
const contractLoading = ref(false)
const contractQuery = reactive({ keyword: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const contractTotal = ref(0)
const contractPagination = computed(() => ({ current: contractQuery.pageNo, pageSize: contractQuery.pageSize, total: contractTotal.value, showSizeChanger: false }))
const contractStatusMap: Record<string, { text: string; cls: string }> = {
  DRAFT: { text: '草稿', cls: '' },
  PENDING_APPROVAL: { text: '待审批', cls: 'rm-pill--warning' },
  APPROVED: { text: '已审批', cls: 'rm-pill--info' },
  REJECTED: { text: '已驳回', cls: 'rm-pill--danger' },
  SIGNED: { text: '已签署', cls: 'rm-pill--success' },
  EFFECTIVE: { text: '已生效', cls: 'rm-pill--success' },
  VOID: { text: '已作废', cls: '' }
}
const contractStatusOptions = Object.entries(contractStatusMap).map(([value, item]) => ({ value, label: item.text }))
const contractStatusText = (status?: string) => (status ? contractStatusMap[status]?.text || status : '--')
const contractStatusPill = (status?: string) => (status ? contractStatusMap[status]?.cls || '' : '')
const contractColumns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo' },
  { title: '长者', dataIndex: 'elderName', key: 'elderName' },
  { title: '状态', key: 'status' },
  { title: '签订日期', key: 'signedAt' },
  { title: '到期日期', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate' },
  { title: '到期提醒', key: 'expiry' },
  { title: '操作', key: 'action', width: 100 }
]
const reminderColumns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName' },
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo' },
  { title: '到期日期', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate' },
  { title: '提醒', key: 'expiry' },
  { title: '操作', key: 'action', width: 80 }
]
const expiryPill: Record<ContractExpiry, string> = { EXPIRED: 'rm-pill--danger', EXPIRING: 'rm-pill--warning' }

const reminders = ref<(CrmContractItem & { state: ContractExpiry })[]>([])
const reminderLoading = ref(false)
const reminderById = computed(() => new Map(reminders.value.map((item) => [String(item.id), item.state])))
const reminderState = (contract: CrmContractItem) => reminderById.value.get(String(contract.id))
const reminderCounts = computed(() => ({
  EXPIRED: reminders.value.filter((item) => item.state === 'EXPIRED').length,
  EXPIRING: reminders.value.filter((item) => item.state === 'EXPIRING').length
}))

async function loadContracts() {
  contractLoading.value = true
  try {
    const res = await getContractPage({
      pageNo: contractQuery.pageNo,
      pageSize: contractQuery.pageSize,
      keyword: contractQuery.keyword || undefined,
      status: contractQuery.status,
      elderId: residentId.value
    })
    contracts.value = res.list || []
    contractTotal.value = res.total || 0
  } finally {
    contractLoading.value = false
  }
}

async function loadReminders() {
  reminderLoading.value = true
  try {
    // shortcut: 到期提醒最多取 200 条（PageGuard 上限）；机构内同时到期的合同远少于此
    const res = await getContractPage({ pageNo: 1, pageSize: 200, elderId: residentId.value, expiryReminderTo: contractExpiryReminderTo() })
    const list = res.list || []
    reminders.value = list.flatMap((contract) => {
      const state = contractExpiry(contract, list)
      return state ? [{ ...contract, state }] : []
    })
  } finally {
    reminderLoading.value = false
  }
}

function searchContracts() {
  contractQuery.pageNo = 1
  loadContracts()
}

function onContractTableChange(pagination: { current?: number }) {
  contractQuery.pageNo = pagination.current || 1
  loadContracts()
}

function goElderContract(elderId: Id) {
  router.push({ path: `/elder/detail/${elderId}`, query: { tab: 'contract' } })
}

// ---------- 退住办理 ----------
const EXIT_FETCH_LIMIT = 200
const DISCHARGE_STATUSES = ['IN_PROGRESS', 'PENDING', 'APPROVED', 'REJECTED']
const DEATH_STATUSES = ['REGISTERED', 'CANCELLED']
const departureTypeOptions = [
  { label: '全部类型', value: 'ALL' },
  { label: '退住', value: 'DISCHARGE' },
  { label: '身故', value: 'DEATH' }
]
const dischargeStatusOptions = [
  { label: '办理中', value: 'IN_PROGRESS' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]
const deathStatusOptions = [
  { label: '已登记', value: 'REGISTERED' },
  { label: '已作废', value: 'CANCELLED' }
]

interface ExitRow {
  key: string
  kind: Departure
  elderId: Id
  elderName?: string
  date?: string
  plannedDate?: string
  reason?: string
  timeout: DischargeTimeout | null
  apply?: DischargeApplyItem
  death?: DeathRegisterItem
}

const exitQuery = reactive({
  departureType: 'ALL' as 'ALL' | Departure,
  status: 'IN_PROGRESS',
  elderId: undefined as Id | undefined,
  keyword: '',
  plannedDateRange: undefined as [string, string] | undefined
})
const exitStatusOptions = computed(() => [
  { label: '全部状态', value: '' },
  ...(exitQuery.departureType === 'DEATH' ? [] : dischargeStatusOptions),
  ...(exitQuery.departureType === 'DISCHARGE' ? [] : deathStatusOptions)
])
const exitColumns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName' },
  { title: '离院类型', key: 'kind' },
  { title: '申请/死亡日期', dataIndex: 'date', key: 'date' },
  { title: '计划退住日期', dataIndex: 'plannedDate', key: 'plannedDate' },
  { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
  { title: '进度/状态', key: 'progress' },
  { title: '超时提醒', key: 'timeout' }
]

const inProgress = ref<DischargeApplyItem[]>([])
const timeoutCounts = computed(() => countDischargeTimeouts(inProgress.value))
const exitRows = ref<ExitRow[]>([])
const exitLoading = ref(false)
const exitTruncated = ref(false)
const timeoutFilter = ref<DischargeTimeout | null>(null)
const visibleExitRows = computed(() => (timeoutFilter.value ? exitRows.value.filter((row) => row.timeout === timeoutFilter.value) : exitRows.value))
const selectedKey = ref<string>()
const selectedRow = computed(() => visibleExitRows.value.find((row) => row.key === selectedKey.value) || visibleExitRows.value[0])
const steps = computed(() => (selectedRow.value?.apply ? buildDischargeSteps(selectedRow.value.apply) : []))
const settlement = ref<DischargeSettlementItem | null>(null)
const acting = ref(false)
const stepPill: Record<DischargeStepState, string> = {
  done: 'rm-pill--success',
  current: 'rm-pill--info',
  waiting: 'rm-pill--warning',
  todo: ''
}

const toApplyRow = (apply: DischargeApplyItem): ExitRow => ({
  key: `D-${apply.id}`,
  kind: 'DISCHARGE',
  elderId: apply.elderId,
  elderName: apply.elderName,
  date: apply.applyDate,
  plannedDate: apply.plannedDischargeDate,
  reason: apply.reason,
  timeout: dischargeTimeout(apply),
  apply
})
const toDeathRow = (death: DeathRegisterItem): ExitRow => ({
  key: `X-${death.id}`,
  kind: 'DEATH',
  elderId: death.elderId,
  elderName: death.elderName,
  date: death.deathDate,
  reason: death.cause,
  timeout: null,
  death
})

function rowPill(row: ExitRow) {
  if (row.death) {
    return row.death.status === 'CANCELLED' ? { text: '已作废', cls: '' } : { text: '已登记', cls: 'rm-pill--danger' }
  }
  const current = buildDischargeSteps(row.apply!).find((step) => step.state !== 'done')
  return current ? { text: `${current.title} · ${current.stateText}`, cls: stepPill[current.state] } : { text: '已完成', cls: 'rm-pill--success' }
}

function exitRowClass(row: ExitRow) {
  return [row.timeout ? 'is-timeout' : '', row.key === selectedRow.value?.key ? 'is-active' : ''].join(' ')
}

function stepHint(step: DischargeStep) {
  if (step.key !== 'fee' || step.state !== 'waiting' || !settlement.value) return step.hint
  const s = settlement.value
  return `结算单 ${s.detailNo || s.id}：前台${s.frontdeskApproved === 1 ? '已签' : '未签'}，护理${s.nursingApproved === 1 ? '已签' : '未签'}，待财务退款`
}

async function loadInProgress() {
  // shortcut: 只取最近 200 条待审/已通过申请，在途退住远少于此；数量增长后改为后端按进度过滤
  const [pending, approved] = await Promise.all([
    getDischargeApplyPage({ pageNo: 1, pageSize: EXIT_FETCH_LIMIT, status: 'PENDING', elderId: residentId.value }),
    getDischargeApplyPage({ pageNo: 1, pageSize: EXIT_FETCH_LIMIT, status: 'APPROVED', elderId: residentId.value })
  ])
  inProgress.value = [...(pending.list || []), ...(approved.list || [])].filter(isDischargeInProgress)
}

function exitParams() {
  const [plannedDateFrom, plannedDateTo] = exitQuery.departureType === 'DEATH' ? [] : exitQuery.plannedDateRange || []
  const status = exitQuery.status
  return {
    wantDischarge: exitQuery.departureType !== 'DEATH' && !DEATH_STATUSES.includes(status),
    wantDeath: exitQuery.departureType !== 'DISCHARGE' && !DISCHARGE_STATUSES.includes(status),
    discharge: {
      elderId: exitQuery.elderId,
      keyword: exitQuery.keyword || undefined,
      status: (DISCHARGE_STATUSES.includes(status) && status !== 'IN_PROGRESS' ? status : undefined) as DischargeApplyStatus | undefined,
      plannedDateFrom,
      plannedDateTo
    },
    death: {
      elderId: exitQuery.elderId,
      keyword: exitQuery.keyword || undefined,
      status: (DEATH_STATUSES.includes(status) ? status : undefined) as DeathRegisterStatus | undefined
    }
  }
}

async function loadExitRows() {
  const params = exitParams()
  const page = { pageNo: 1, pageSize: EXIT_FETCH_LIMIT }
  exitLoading.value = true
  try {
    const [applies, deaths] = await Promise.all([
      !params.wantDischarge
        ? Promise.resolve({ list: [], total: 0 })
        : exitQuery.status === 'IN_PROGRESS'
          ? Promise.all([
            getDischargeApplyPage({ ...page, ...params.discharge, status: 'PENDING' }),
            getDischargeApplyPage({ ...page, ...params.discharge, status: 'APPROVED' })
          ]).then(([a, b]) => ({ list: [...(a.list || []), ...(b.list || [])].filter(isDischargeInProgress), total: Math.max(a.total, b.total) }))
          : getDischargeApplyPage({ ...page, ...params.discharge }),
      params.wantDeath ? getDeathRegisterPage({ ...page, ...params.death }) : Promise.resolve({ list: [], total: 0 })
    ])
    exitRows.value = [...(applies.list || []).map(toApplyRow), ...(deaths.list || []).map(toDeathRow)]
    exitTruncated.value = applies.total > EXIT_FETCH_LIMIT || deaths.total > EXIT_FETCH_LIMIT
  } finally {
    exitLoading.value = false
  }
}

function reloadDischarge() {
  return Promise.all([loadInProgress(), loadExitRows()])
}

function onDepartureTypeChange() {
  const allowed = exitStatusOptions.value.map((item) => item.value)
  if (!allowed.includes(exitQuery.status)) exitQuery.status = exitQuery.departureType === 'DISCHARGE' ? 'IN_PROGRESS' : ''
  loadExitRows()
}

function resetExitQuery() {
  Object.assign(exitQuery, { departureType: 'ALL', status: 'IN_PROGRESS', elderId: residentId.value, keyword: '', plannedDateRange: undefined })
  timeoutFilter.value = null
  loadExitRows()
}

function showTimeouts(kind: DischargeTimeout) {
  timeoutFilter.value = kind
  if (exitQuery.status !== 'IN_PROGRESS' || exitQuery.departureType === 'DEATH') {
    Object.assign(exitQuery, { departureType: 'ALL', status: 'IN_PROGRESS' })
    loadExitRows()
  }
}

async function exportExitRows() {
  const params = exitParams()
  if (params.wantDischarge) await exportDischargeApply(params.discharge)
  if (params.wantDeath) await exportDeathRegister(params.death)
}

watch(() => selectedRow.value?.apply, async (apply) => {
  settlement.value = null
  if (!apply || !canFinance.value || nextDischargeAction(apply) !== 'SETTLEMENT') return
  const res = await getDischargeSettlementPage({ pageNo: 1, pageSize: 20, elderId: apply.elderId })
  settlement.value = (res.list || []).find((item) => String(item.dischargeApplyId) === String(apply.id)) || null
})

const nextAction = computed(() => {
  const apply = selectedRow.value?.apply
  const action = apply ? nextDischargeAction(apply) : null
  switch (action) {
    case 'REVIEW':
      return { label: canApproveDischarge.value ? '审批退住申请' : '待部门负责人审批', enabled: canApproveDischarge.value }
    case 'CARE_HANDOVER':
      return { label: '确认护理/医务交接完成', enabled: true }
    case 'ITEM_HANDOVER':
      return { label: '确认物品及房间交接完成', enabled: true }
    case 'SETTLEMENT':
      return { label: canFinance.value ? '前往退住结算' : '待财务确认', enabled: canFinance.value }
    case 'ARCHIVE':
      return { label: '确认档案归档', enabled: true }
    default:
      return { label: '继续退住办理', enabled: false }
  }
})

const reviewOpen = ref(false)
const reviewForm = reactive<DischargeApplyReviewRequest>({ status: 'APPROVED', reviewRemark: '' })
const reviewChecklist = reactive({ proofChecked: true, planChecked: true, financeHandoffReady: true })

async function run(task: () => Promise<unknown>, successText: string) {
  acting.value = true
  try {
    await task()
    message.success(successText)
    await reloadDischarge()
  } finally {
    acting.value = false
  }
}

function goFinance(path: string, apply: DischargeApplyItem) {
  router.push({
    path,
    query: { elderId: String(apply.elderId), elderName: apply.elderName || '', dischargeApplyId: String(apply.id), openCreate: '1' }
  })
}

function continueDischarge() {
  const apply = selectedRow.value?.apply
  const action = apply ? nextDischargeAction(apply) : null
  if (!apply || !action) return
  if (action === 'REVIEW') {
    Object.assign(reviewForm, { status: 'APPROVED', reviewRemark: '' })
    Object.assign(reviewChecklist, { proofChecked: true, planChecked: true, financeHandoffReady: true })
    reviewOpen.value = true
    return
  }
  if (action === 'SETTLEMENT') {
    goFinance('/finance/discharge/settlement', apply)
    return
  }
  Modal.confirm({
    title: nextAction.value.label,
    content: `确认 ${apply.elderName || ''} 的该步骤已办理完成？确认后将记录办理人与时间。`,
    onOk: () => run(() => completeDischargeStep(apply.id, action), '已记录')
  })
}

async function submitReview() {
  const apply = selectedRow.value?.apply
  if (!apply) return
  const approved = reviewForm.status === 'APPROVED'
  if (approved && !(reviewChecklist.proofChecked && reviewChecklist.planChecked && reviewChecklist.financeHandoffReady)) {
    message.warning('通过前请完成审核清单确认')
    return
  }
  if (!approved && !reviewForm.reviewRemark?.trim()) {
    message.warning('驳回时请填写审批备注')
    return
  }
  const notes = approved ? ['资料已核验', '计划日期已确认', '已移交交接与退住结算'] : []
  const reviewRemark = [...notes, reviewForm.reviewRemark?.trim()].filter(Boolean).join('；') || undefined
  await run(() => reviewDischargeApply(apply.id, { status: reviewForm.status, reviewRemark }), approved ? '已通过' : '已驳回')
  reviewOpen.value = false
}

function removeApply(apply: DischargeApplyItem) {
  run(() => deleteDischargeApply(apply.id), apply.status === 'PENDING' ? '已取消申请' : '已删除')
}

function cancelDeath(death: DeathRegisterItem) {
  run(() => cancelDeathRegister(death.id, { remark: '前台作废操作' }), '身故登记已作废')
}

function removeDeath(death: DeathRegisterItem) {
  run(() => deleteDeathRegister(death.id), '已删除')
}

// ---------- 新建退住 / 身故登记 抽屉 ----------
const createOpen = ref(false)
const createMode = ref<Departure>('DISCHARGE')
const submitting = ref(false)
const editingDeathId = ref<Id>()
const applyFormRef = ref<FormInstance>()
const deathFormRef = ref<FormInstance>()
const proofUploading = ref(false)
const applyForm = reactive<DischargeApplyCreateRequest>({ elderId: '' as Id, plannedDischargeDate: '', reason: '', proofFileUrl: '' })
const deathForm = reactive<DeathRegisterCreateRequest>({
  elderId: '' as Id,
  deathDate: '',
  deathTime: undefined,
  place: '',
  cause: '',
  certificateNo: '',
  reportedBy: '',
  reportedTime: undefined,
  remark: ''
})
const applyRules: Record<string, Rule[]> = {
  elderId: [{ required: true, message: '请选择长者' }],
  plannedDischargeDate: [{ required: true, message: '请选择计划退住日期' }],
  reason: [{ required: true, whitespace: true, message: '请输入申请原因' }]
}
const deathRules: Record<string, Rule[]> = {
  elderId: [{ required: true, message: '请选择长者' }],
  deathDate: [{ required: true, message: '请选择死亡日期' }]
}
const createTitle = computed(() => (editingDeathId.value ? '更正身故登记' : createMode.value === 'DEATH' ? '身故登记' : '新建退住申请'))

function openCreate(mode: Departure) {
  const elderId = residentId.value || ('' as Id)
  if (residentId.value) ensureSelectedElder(residentId.value)
  editingDeathId.value = undefined
  createMode.value = mode
  Object.assign(applyForm, { elderId, plannedDischargeDate: dayjs().format('YYYY-MM-DD'), reason: '', proofFileUrl: '' })
  Object.assign(deathForm, {
    elderId,
    deathDate: '',
    deathTime: undefined,
    place: '',
    cause: '',
    certificateNo: '',
    reportedBy: '',
    reportedTime: undefined,
    remark: ''
  })
  createOpen.value = true
}

function openEditDeath(death: DeathRegisterItem) {
  ensureSelectedElder(death.elderId, death.elderName)
  editingDeathId.value = death.id
  createMode.value = 'DEATH'
  Object.assign(deathForm, {
    elderId: death.elderId,
    deathDate: death.deathDate || '',
    deathTime: death.deathTime,
    place: death.place || '',
    cause: death.cause || '',
    certificateNo: death.certificateNo || '',
    reportedBy: death.reportedBy || '',
    reportedTime: death.reportedTime,
    remark: death.remark || ''
  })
  createOpen.value = true
}

async function beforeUploadProof(file: File) {
  proofUploading.value = true
  try {
    const uploaded = await uploadElderFile(file, 'elder-discharge-apply-proof')
    if (!uploaded?.fileUrl) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    applyForm.proofFileUrl = uploaded.fileUrl
    message.success('证明附件上传成功')
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    proofUploading.value = false
  }
  return false
}

function fileLabel(url?: string, fallback = '') {
  if (!url) return fallback
  return decodeURIComponent(String(url).split('/').pop() || url)
}

async function submitCreate() {
  const isDeath = createMode.value === 'DEATH'
  await (isDeath ? deathFormRef.value : applyFormRef.value)?.validate()
  if (submitting.value) return
  submitting.value = true
  try {
    if (!isDeath) {
      await createDischargeApply({ ...applyForm, reason: applyForm.reason.trim() })
      message.success('退住申请已提交')
    } else if (editingDeathId.value) {
      const { deathDate, deathTime, place, cause, certificateNo, reportedBy, reportedTime, remark } = deathForm
      await updateDeathRegister(editingDeathId.value, { deathDate, deathTime, place, cause, certificateNo, reportedBy, reportedTime, remark })
      message.success('身故登记已更正')
    } else {
      await createDeathRegister({ ...deathForm })
      message.success('身故登记成功')
    }
    createOpen.value = false
    activeTab.value = 'discharge'
    if (isDeath && !editingDeathId.value) Object.assign(exitQuery, { departureType: 'DEATH', status: '' })
    else if (!isDeath) Object.assign(exitQuery, { departureType: 'ALL', status: 'IN_PROGRESS' })
    await reloadDischarge()
  } finally {
    submitting.value = false
  }
}

// ---------- 路由参数：tab / elderId(residentId) / create=1 / departure=DEATH ----------
const contractsLoaded = ref(false)

function applyRouteQuery() {
  const tab = String(route.query.tab || '')
  const death = String(route.query.departure || '').toUpperCase() === 'DEATH'
  activeTab.value = (['fee', 'contract', 'discharge'] as string[]).includes(tab)
    ? (tab as Tab)
    : residentId.value || death ? 'discharge' : 'fee'
  if (residentId.value) ensureSelectedElder(residentId.value)
  Object.assign(exitQuery, death ? { departureType: 'DEATH', status: '' } : { departureType: 'ALL', status: 'IN_PROGRESS' })
  exitQuery.elderId = residentId.value
  // departure=DEATH 只把列表切到身故；带 create=1 才打开登记抽屉
  if (canManageDischarge.value && String(route.query.create || '') === '1') {
    openCreate(death ? 'DEATH' : 'DISCHARGE')
  }
}

function loadAll() {
  contractsLoaded.value = false
  if (canFinance.value) {
    loadSummary()
    loadBills()
  }
  if (canManageDischarge.value) reloadDischarge()
  if (activeTab.value === 'contract') loadContractTab()
}

function loadContractTab() {
  if (!canReadContracts.value || contractsLoaded.value) return
  contractsLoaded.value = true
  loadContracts()
  loadReminders()
}

watch(activeTab, (tab) => {
  if (tab === 'contract') loadContractTab()
})

watch(() => route.query, () => {
  if (route.path !== '/elder/status-change/discharge-settlement') return
  applyRouteQuery()
  loadAll()
})

onMounted(() => {
  applyRouteQuery()
  loadAll()
})
</script>

<style scoped>
.discharge-meta { margin-bottom: 8px; padding: 10px 12px; border-radius: 8px; background: #f5f8f7; color: var(--rm-muted); font-size: 12px; }
.exit-table :deep(.ant-table-row) { cursor: pointer; }
.exit-table :deep(.ant-table-row.is-timeout > td) { background: #fff8e8; }
.exit-table :deep(.ant-table-row.is-active > td) { background: var(--rm-green-soft); }
.step-index {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  flex: none;
  border-radius: 50%;
  font-size: 12px;
  background: #eff2f1;
  color: var(--rm-muted);
}
.step-index.is-done { background: var(--rm-green-soft); color: var(--rm-green); }
.step-index.is-current { background: #e8f0fc; color: var(--rm-info); }
.step-index.is-waiting { background: #fff5e1; color: var(--rm-warning); }
</style>
