<template>
  <section class="business-query" :class="{ 'business-query--collapsed': compact && !expanded }">
    <a-form v-bind="$attrs" :model="model" layout="inline" class="business-query__form" @finish="$emit('finish', $event)">
      <div ref="fields" class="business-query__fields"><QueryFields><slot /></QueryFields></div>
      <div class="business-query__actions">
        <a-button v-if="compact && fieldCount > 2" type="link" :aria-expanded="expanded" @click="expanded = !expanded">{{ expanded ? '收起筛选' : '更多筛选' }}</a-button>
        <slot name="actions" />
      </div>
    </a-form>
  </section>
</template>
<script setup lang="ts">
import { defineComponent, inject, onMounted, onUpdated, ref } from 'vue'
import { useRoute } from 'vue-router'
import { commonQueryFields } from './queryFields'
defineOptions({ inheritAttrs: false })
defineProps<{ model?: Record<string, any> }>()
defineEmits<{ (event: 'finish', value: unknown): void }>()
const compact = inject('business-page-layout', false)
const route = useRoute()
const QueryFields = defineComponent({ setup: (_, { slots }) => () => compact ? commonQueryFields(slots.default?.() || [], route?.path || '') : slots.default?.() })
const expanded = ref(false)
const fields = ref<HTMLElement>()
const fieldCount = ref(0)
function countFields() { fieldCount.value = fields.value?.children.length || 0 }
onMounted(countFields)
onUpdated(countFields)
</script>
<style scoped>
.business-query { padding:12px 14px; border:1px solid var(--border); border-radius:8px; background:var(--surface); }
.business-query__form { display:flex; align-items:flex-start; flex-wrap:wrap; gap:12px; padding:0; margin:0; }
.business-query__fields { display:flex; align-items:center; flex-wrap:wrap; flex:1 1 400px; gap:12px; min-width:0; }
.business-query__actions { display:flex; align-items:center; flex-wrap:wrap; gap:8px; margin-inline-start:auto; }
.business-query :deep(.ant-form-item) { margin:0; }
.business-query--collapsed .business-query__fields > :deep(:nth-child(n + 3)) { display:none; }
@media(max-width:700px) {
  .business-query__fields { flex-basis:100%; }
  .business-query__fields > * { width:100%; min-width:0; }
  .business-query__actions { width:100%; justify-content:flex-end; }
  .business-query :deep(.ant-picker), .business-query :deep(.ant-select), .business-query :deep(.ant-input) { max-width:100%; }
}
</style>
