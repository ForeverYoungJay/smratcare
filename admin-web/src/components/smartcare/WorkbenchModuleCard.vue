<template>
  <section class="workbench-module-card card-elevated">
    <div class="workbench-module-card__head">
      <div>
        <span v-if="eyebrow" class="section-kicker">{{ eyebrow }}</span>
        <h3>{{ title }}</h3>
      </div>
      <div v-if="$slots.extra" class="workbench-module-card__extra">
        <slot name="extra" />
      </div>
    </div>
    <div class="workbench-module-card__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  eyebrow?: string
  title: string
}>()
</script>

<style scoped>
.workbench-module-card {
  padding: 18px;
}

.workbench-module-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.workbench-module-card__head h3 {
  margin: 4px 0 0;
  font-size: 18px;
  color: var(--ink);
}

.workbench-module-card__extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.workbench-module-card__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>

