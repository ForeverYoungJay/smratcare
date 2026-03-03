<template>
  <div class="flow-guard">
    <div class="flow-head">
      <div class="flow-title">{{ title }}</div>
      <a-tag :color="stageColor">{{ stageText }}</a-tag>
    </div>
    <div class="flow-subtitle" v-if="subject">{{ subject }}</div>
    <div class="flow-steps">
      <span
        v-for="(item, idx) in steps"
        :key="item"
        class="flow-step"
        :class="{
          done: idx < currentIndex,
          current: idx === currentIndex
        }"
      >
        {{ idx + 1 }}. {{ item }}
      </span>
    </div>
    <div v-if="normalizedBlockers.length" class="blocker-panel">
      <div class="blocker-title">当前阻塞</div>
      <div v-for="item in normalizedBlockers" :key="`${item.code || '-'}-${item.text}`" class="blocker-item">
        <span class="blocker-code" v-if="item.code">{{ item.code }}</span>
        <span class="blocker-text">{{ item.text }}</span>
        <a-button
          v-if="item.actionLabel"
          size="small"
          type="link"
          @click="emit('action', item)"
        >
          {{ item.actionLabel }}
        </a-button>
      </div>
    </div>
    <a-alert
      v-else-if="hint"
      type="success"
      show-icon
      :message="hint"
      style="margin-top: 8px"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type GuardBlocker = string | {
  code?: string
  text: string
  actionLabel?: string
  actionKey?: string
}

const emit = defineEmits<{
  action: [item: { code?: string; text: string; actionLabel?: string; actionKey?: string }]
}>()

const props = withDefaults(defineProps<{
  title?: string
  subject?: string
  stageText: string
  stageColor?: string
  steps: string[]
  currentIndex: number
  blockers?: GuardBlocker[]
  hint?: string
}>(), {
  title: '流程状态',
  stageColor: 'default',
  blockers: () => []
})

const normalizedBlockers = computed(() =>
  (props.blockers || []).map((item) =>
    typeof item === 'string' ? { text: item } : item
  )
)
</script>

<style scoped>
.flow-guard {
  background: linear-gradient(120deg, #f7fbff 0%, #fffef5 100%);
  border: 1px solid #e6edf5;
  border-radius: 10px;
  padding: 12px;
}

.flow-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.flow-title {
  font-weight: 600;
  color: #24486b;
}

.flow-subtitle {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.flow-steps {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.flow-step {
  padding: 4px 10px;
  border-radius: 999px;
  background: #f0f2f5;
  color: #666;
  font-size: 12px;
}

.flow-step.done {
  background: #e8f9ef;
  color: #1f7a43;
}

.flow-step.current {
  background: #e6f7ff;
  color: #096dd9;
  font-weight: 600;
}

.blocker-panel {
  margin-top: 8px;
  border: 1px solid #ffe1b8;
  background: #fff7e8;
  border-radius: 8px;
  padding: 8px 10px;
}

.blocker-title {
  font-size: 12px;
  color: #ad6800;
  font-weight: 600;
  margin-bottom: 4px;
}

.blocker-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #613400;
}

.blocker-code {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  height: 18px;
  border-radius: 999px;
  background: #ffe7ba;
  font-size: 11px;
  color: #ad6800;
  font-weight: 600;
}

.blocker-text {
  flex: 1;
}
</style>
