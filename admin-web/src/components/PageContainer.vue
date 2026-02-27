<template>
  <div class="page-container page-shell">
    <section class="page-head card-elevated">
      <div class="page-head-main">
        <div class="page-title-wrap">
          <h1 class="page-title">{{ title }}</h1>
          <p class="page-subtitle" v-if="subTitle">{{ subTitle }}</p>
        </div>
        <div class="page-head-extra">
          <slot name="extra" />
        </div>
      </div>
      <div class="page-head-stats" v-if="$slots.stats">
        <slot name="stats" />
      </div>
    </section>
    <section class="page-body">
      <slot />
    </section>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; subTitle?: string }>()
</script>

<style scoped>
.page-shell {
  gap: 18px;
}

.page-head {
  position: relative;
  overflow: hidden;
  padding: 18px 20px;
}

.page-head::before {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(57, 133, 255, 0.2) 0%, rgba(57, 133, 255, 0) 72%);
  right: -100px;
  top: -200px;
  pointer-events: none;
}

.page-head-main {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.page-title-wrap {
  min-width: 0;
}

.page-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: 0.4px;
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.page-head-extra {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.page-head-stats {
  position: relative;
  margin-top: 14px;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 992px) {
  .page-head-main {
    flex-direction: column;
  }

  .page-head-extra {
    justify-content: flex-start;
    width: 100%;
  }
}
</style>
