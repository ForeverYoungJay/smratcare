<template>
  <a-drawer :open="open" title="执行护理任务" width="480" :mask-closable="false" @close="emit('update:open', false)">
    <template v-if="task">
      <div class="te-elder">
        <strong>{{ task.elderName || '未命名长者' }}</strong>
        <span>{{ task.roomNo || '未分配房间' }}{{ task.careLevel ? ` · ${task.careLevel}` : '' }}</span>
      </div>
      <div class="te-task">
        <div><strong>{{ task.taskName }}</strong><small>计划时间：{{ task.planTime ? dayjs(task.planTime).format('MM-DD HH:mm') : '--' }}</small></div>
        <span class="rm-pill" :class="task.overdueFlag ? 'rm-pill--danger' : 'rm-pill--warning'">{{ task.overdueFlag ? '已超时' : '待执行' }}</span>
      </div>

      <a-form layout="vertical">
        <a-form-item v-if="task.serviceItems?.length" label="护理执行项目（按实际情况勾选）">
          <a-checkbox-group v-model:value="form.checkedItems" class="te-items">
            <a-checkbox v-for="item in task.serviceItems" :key="item" :value="item">{{ item }}</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item label="执行结果" required>
          <a-radio-group v-model:value="form.result" button-style="solid">
            <a-radio-button value="DONE">已完成</a-radio-button>
            <a-radio-button value="PARTIAL">部分完成</a-radio-button>
            <a-radio-button value="NOT_DONE">未完成</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="form.result !== 'DONE'" label="未完成原因" required>
          <a-textarea v-model:value="form.reason" :rows="2" :maxlength="200" show-count placeholder="例如：长者拒绝、外出就医、物资不足" />
        </a-form-item>
        <a-form-item v-if="form.result !== 'NOT_DONE'" label="床位二维码" required extra="当面扫码核对长者与床位，防止未执行先确认">
          <a-input v-model:value="form.bedQrCode" placeholder="请扫码或输入床位二维码" />
        </a-form-item>
        <a-form-item label="服务情况与观察记录">
          <a-textarea v-model:value="form.observation" :rows="3" :maxlength="200" show-count placeholder="精神状态、皮肤、进食等观察到的情况" />
        </a-form-item>
        <a-form-item label="异常情况">
          <a-select v-model:value="form.incidentType" :options="incidentOptions" />
        </a-form-item>
        <a-form-item v-if="form.incidentType" label="风险级别" required>
          <a-radio-group v-model:value="form.severity">
            <a-radio v-for="(meta, key) in INCIDENT_SEVERITY" :key="key" :value="key">{{ meta.label }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="执行人">
          <a-input :value="`${userStore.staffInfo?.realName || userStore.staffInfo?.username || '当前账号'}（当前登录人）`" disabled />
        </a-form-item>
      </a-form>
      <a-alert type="warning" show-icon message="必须在实际服务完成后记录；执行时间以服务器时间为准，系统同时记录执行人和修改日志。选了异常或未完成，会自动生成异常事件并发到护士长待办，在「交接班与安全」跟进。" />
    </template>
    <template #footer>
      <a-space style="float: right">
        <a-button @click="emit('update:open', false)">取消</a-button>
        <a-button @click="saveDraft">保存草稿</a-button>
        <a-button type="primary" :loading="submitting" @click="submit">提交执行记录</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { executeTask } from '../../../api/care'
import { createIncident } from '../../../api/nursing'
import { useUserStore } from '../../../stores/user'
import type { CareTaskItem } from '../../../types'
import { resolveCareError } from '../careError'
import { INCIDENT_SEVERITY, INCIDENT_TYPES } from './nursingModel'
import { buildExecution, type ExecuteForm } from './taskExecution'
import '../../elder/residentUi.css'

const props = defineProps<{ open: boolean; task: CareTaskItem | null }>()
const emit = defineEmits<{ 'update:open': [boolean]; done: [] }>()
const userStore = useUserStore()
const submitting = ref(false)
const incidentOptions = [{ value: '', label: '无异常' }, ...Object.entries(INCIDENT_TYPES).map(([value, label]) => ({ value, label }))]
const blank = (): ExecuteForm => ({ result: 'DONE', reason: '', bedQrCode: '', observation: '', incidentType: '', severity: 'MEDIUM', checkedItems: [] })
const form = reactive<ExecuteForm>(blank())

// 草稿只存本机浏览器，换设备不会带过去
const draftKey = () => `care-exec-draft-${props.task?.taskDailyId}`
watch(() => [props.open, props.task?.taskDailyId], () => {
  if (!props.open || !props.task) return
  let draft: Partial<ExecuteForm> = {}
  try { draft = JSON.parse(localStorage.getItem(draftKey()) || '{}') } catch { /* 无痕模式等读不到草稿 */ }
  Object.assign(form, blank(), draft, { bedQrCode: '' })
})

function saveDraft() {
  try {
    const { bedQrCode: _skip, ...rest } = form
    localStorage.setItem(draftKey(), JSON.stringify(rest))
    message.success('草稿已保存在本机')
  } catch {
    message.warning('浏览器不允许保存草稿')
  }
}

async function submit() {
  const task = props.task
  const staffId = Number(userStore.staffInfo?.id)
  if (!task) return
  if (!staffId) return message.error('当前账号缺少员工档案，无法记录真实执行人')
  const plan = buildExecution(form, task)
  if (plan.error) return message.warning(plan.error)
  submitting.value = true
  try {
    if (plan.execute) await executeTask({ taskDailyId: task.taskDailyId, staffId, ...plan.execute })
  } catch (error) {
    submitting.value = false
    return message.error(resolveCareError(error, '任务执行失败'))
  }
  try {
    if (plan.incident) await createIncident({ ...plan.incident, occurredAt: dayjs().format('YYYY-MM-DDTHH:mm:ss') })
    message.success(plan.incident ? '已记录，并生成异常事件' : '护理任务已记录完成')
  } catch (error) {
    // 执行记录已保存，只是异常没建成：提示去安全页补报，别让人以为全失败而重复执行
    message.warning(`执行已记录，但异常事件上报失败，请到「交接班与安全」补报：${resolveCareError(error, '')}`)
  } finally {
    submitting.value = false
  }
  try { localStorage.removeItem(draftKey()) } catch { /* ignore */ }
  emit('update:open', false)
  emit('done')
}
</script>

<style scoped>
.te-elder { display: grid; gap: 4px; padding: 12px 14px; border: 1px solid #cfe7dc; border-radius: 10px; background: #f1faf5; }
.te-elder span { color: #5f7067; font-size: 12px; }
.te-task { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; margin: 14px 0; padding-bottom: 12px; border-bottom: 1px solid #edf0ed; }
.te-task div { display: grid; gap: 4px; }
.te-task small { color: #7b877e; }
.te-items { display: grid; gap: 8px; width: 100%; }
.te-items :deep(.ant-checkbox-wrapper) { margin-inline-start: 0; padding: 8px 10px; border: 1px solid #e3ebe6; border-radius: 8px; }
</style>
