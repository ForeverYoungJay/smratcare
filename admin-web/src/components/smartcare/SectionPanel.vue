<template>
  <section class="section-panel card-elevated" :class="{ 'section-panel--dense': dense }">
    <div class="section-panel__head">
      <div class="section-panel__copy">
        <span v-if="eyebrow" class="section-kicker">{{ eyebrow }}</span>
        <h3>{{ title }}</h3>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.extra" class="section-panel__extra">
        <slot name="extra" />
      </div>
    </div>
    <div class="section-panel__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  dense?: boolean
  description?: string
  eyebrow?: string
  title: string
}>()
</script>

<style scoped>
.section-panel {
  padding: 20px;
}

.section-panel--dense {
  padding: 16px;
}

.section-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-panel__copy {
  display: grid;
  gap: 4px;
}

.section-panel__copy h3 {
  margin: 0;
  font-size: 20px;
  color: var(--ink);
}

.section-panel__copy p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.7;
}

.section-panel__extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.section-panel__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
</style>

