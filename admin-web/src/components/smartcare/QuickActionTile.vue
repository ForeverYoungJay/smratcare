<template>
  <button type="button" class="quick-action-tile" @click="$emit('click')">
    <div class="quick-action-tile__icon">{{ icon }}</div>
    <div class="quick-action-tile__copy">
      <strong>{{ title }}</strong>
      <span>{{ description }}</span>
    </div>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  description: string
  icon: string
  title: string
}>()

defineEmits<{
  (e: 'click'): void
}>()
</script>

<style scoped>
.quick-action-tile {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(213, 224, 233, 0.9);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 250, 252, 0.95));
  text-align: left;
  cursor: pointer;
}

.quick-action-tile__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: rgba(19, 108, 181, 0.1);
  color: var(--primary-strong);
  font-size: 20px;
}

.quick-action-tile__copy {
  display: grid;
  gap: 4px;
}

.quick-action-tile__copy strong {
  color: var(--ink);
  font-size: 14px;
}

.quick-action-tile__copy span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}
</style>

