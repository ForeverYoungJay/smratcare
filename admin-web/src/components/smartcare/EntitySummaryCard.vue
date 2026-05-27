<template>
  <button type="button" class="entity-summary-card" @click="$emit('click')">
    <div class="entity-summary-card__avatar">{{ avatarText }}</div>
    <div class="entity-summary-card__content">
      <div class="entity-summary-card__title-row">
        <strong>{{ title }}</strong>
        <slot name="tag" />
      </div>
      <p>{{ subtitle }}</p>
      <div class="entity-summary-card__meta">
        <span v-for="item in meta" :key="item">{{ item }}</span>
      </div>
    </div>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  avatarText: string
  meta: string[]
  subtitle: string
  title: string
}>()

defineEmits<{
  (e: 'click'): void
}>()
</script>

<style scoped>
.entity-summary-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(212, 224, 232, 0.9);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.94);
  text-align: left;
  cursor: pointer;
}

.entity-summary-card__avatar {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(19, 108, 181, 0.16), rgba(30, 138, 143, 0.12));
  color: var(--primary-strong);
  font-size: 18px;
  font-weight: 800;
}

.entity-summary-card__content {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.entity-summary-card__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.entity-summary-card__title-row strong {
  color: var(--ink);
  font-size: 15px;
}

.entity-summary-card__content p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}

.entity-summary-card__meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.entity-summary-card__meta span {
  color: var(--muted);
  font-size: 12px;
}
</style>
