<template>
  <PageContainer title="异常规则配置" subTitle="医护健康模块异常高亮与联动规则（可配置）">
    <a-alert type="warning" show-icon style="margin-bottom: 12px" message="当前为前端配置版，后续可同步到后端规则中心。" />

    <a-card :bordered="false" class="card-elevated">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item label="用药高剂量阈值（超出标黄）">
              <a-input-number v-model:value="rules.medicationHighDosageThreshold" :min="1" :max="999" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="任务超时阈值（小时）">
              <a-input-number v-model:value="rules.overdueHoursThreshold" :min="1" :max="168" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item label="异常巡检必须上传照片">
              <a-switch v-model:checked="rules.abnormalInspectionRequirePhoto" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="交接班自动带确认时间">
              <a-switch v-model:checked="rules.handoverAutoFillConfirmTime" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="交接班风险关键词（逗号分隔）">
          <a-input
            v-model:value="rules.handoverRiskKeywordsText"
            placeholder="例如：异常,风险,事故,上报"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="自动联动开关">
          <a-space direction="vertical">
            <a-checkbox v-model:checked="rules.autoCreateNursingLogFromInspection">巡检异常自动生成护理日志</a-checkbox>
            <a-checkbox v-model:checked="rules.autoRaiseTaskFromAbnormal">异常项自动升级为高优先级任务</a-checkbox>
            <a-checkbox v-model:checked="rules.autoCarryResidentContext">跨页自动携带当前长者上下文</a-checkbox>
          </a-space>
        </a-form-item>
      </a-form>

      <a-space>
        <a-button type="primary" @click="saveRules">保存配置</a-button>
        <a-button @click="resetRules">恢复默认</a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false" class="card-elevated" style="margin-top: 12px" title="规则预览">
      <a-space wrap>
        <a-tag color="blue">高剂量阈值：{{ rules.medicationHighDosageThreshold }}</a-tag>
        <a-tag color="orange">超时阈值：{{ rules.overdueHoursThreshold }}h</a-tag>
        <a-tag :color="rules.abnormalInspectionRequirePhoto ? 'red' : 'default'">
          巡检照片必传：{{ rules.abnormalInspectionRequirePhoto ? '是' : '否' }}
        </a-tag>
        <a-tag :color="rules.autoRaiseTaskFromAbnormal ? 'red' : 'default'">
          异常自动升级：{{ rules.autoRaiseTaskFromAbnormal ? '开启' : '关闭' }}
        </a-tag>
      </a-space>
      <div style="margin-top: 8px; color: #64748b">
        风险关键词：{{ normalizedKeywords.length ? normalizedKeywords.join('、') : '未配置' }}
      </div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { resolveMedicalError } from './medicalError'
import { getMedicalAlertRuleConfig, updateMedicalAlertRuleConfig } from '../../api/medicalCare'
import { defaultMedicalAlertRules, normalizeMedicalAlertRules, readMedicalAlertRulesFromStorage, writeMedicalAlertRulesToStorage } from '../../utils/medicalAlertRule'

type RuleConfig = {
  medicationHighDosageThreshold: number
  overdueHoursThreshold: number
  abnormalInspectionRequirePhoto: boolean
  handoverAutoFillConfirmTime: boolean
  autoCreateNursingLogFromInspection: boolean
  autoRaiseTaskFromAbnormal: boolean
  autoCarryResidentContext: boolean
  handoverRiskKeywordsText: string
}

const defaultRules: RuleConfig = toFormRules(defaultMedicalAlertRules)
const rules = reactive<RuleConfig>({ ...defaultRules, ...toFormRules(readMedicalAlertRulesFromStorage()) })

const normalizedKeywords = computed(() =>
  rules.handoverRiskKeywordsText
    .split(/[，,]/g)
    .map((item) => item.trim())
    .filter((item) => !!item)
)

function loadRules() {
  return readMedicalAlertRulesFromStorage()
}

async function saveRules(silent = false) {
  try {
    if (rules.medicationHighDosageThreshold <= 0 || rules.overdueHoursThreshold <= 0) {
      message.error('阈值必须大于0')
      return
    }
    const payload = toApiRules(rules)
    const saved = await updateMedicalAlertRuleConfig(payload)
    writeMedicalAlertRulesToStorage(saved || payload)
    if (!silent) {
      message.success('规则配置已保存')
    }
  } catch (error) {
    message.error(resolveMedicalError(error, '保存失败'))
  }
}

async function resetRules() {
  Object.assign(rules, defaultRules)
  await saveRules(true)
  message.success('已恢复默认规则并同步')
}

async function loadServerRules() {
  try {
    const serverRules = await getMedicalAlertRuleConfig()
    const normalized = normalizeMedicalAlertRules(serverRules)
    writeMedicalAlertRulesToStorage(normalized)
    Object.assign(rules, toFormRules(normalized))
  } catch {
    Object.assign(rules, toFormRules(loadRules()))
  }
}

function toFormRules(apiRules: any): RuleConfig {
  const normalized = normalizeMedicalAlertRules(apiRules)
  return {
    medicationHighDosageThreshold: normalized.medicationHighDosageThreshold,
    overdueHoursThreshold: normalized.overdueHoursThreshold,
    abnormalInspectionRequirePhoto: normalized.abnormalInspectionRequirePhoto === 1,
    handoverAutoFillConfirmTime: normalized.handoverAutoFillConfirmTime === 1,
    autoCreateNursingLogFromInspection: normalized.autoCreateNursingLogFromInspection === 1,
    autoRaiseTaskFromAbnormal: normalized.autoRaiseTaskFromAbnormal === 1,
    autoCarryResidentContext: normalized.autoCarryResidentContext === 1,
    handoverRiskKeywordsText: normalized.handoverRiskKeywords
  }
}

function toApiRules(formRules: RuleConfig) {
  return {
    medicationHighDosageThreshold: formRules.medicationHighDosageThreshold,
    overdueHoursThreshold: formRules.overdueHoursThreshold,
    abnormalInspectionRequirePhoto: formRules.abnormalInspectionRequirePhoto ? 1 : 0,
    handoverAutoFillConfirmTime: formRules.handoverAutoFillConfirmTime ? 1 : 0,
    autoCreateNursingLogFromInspection: formRules.autoCreateNursingLogFromInspection ? 1 : 0,
    autoRaiseTaskFromAbnormal: formRules.autoRaiseTaskFromAbnormal ? 1 : 0,
    autoCarryResidentContext: formRules.autoCarryResidentContext ? 1 : 0,
    handoverRiskKeywords: formRules.handoverRiskKeywordsText
  }
}

onMounted(loadServerRules)
</script>
