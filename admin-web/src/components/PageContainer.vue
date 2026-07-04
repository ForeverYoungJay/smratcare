<template>
  <div class="page-container page-shell" :class="[`page-shell--${mode}`]">
    <section class="page-head" :class="{ 'page-head--showcase': mode === 'showcase' }">
      <div class="page-head-accent"></div>
      <div class="page-head-glow" v-if="mode === 'showcase'"></div>
      <div class="page-head-main">
        <div class="page-title-wrap">
          <div class="page-kicker" v-if="kicker">
            <span class="page-kicker-chip">{{ kicker }}</span>
          </div>
          <div class="page-title-row">
            <h1 class="page-title">{{ title }}</h1>
            <slot name="badge" />
          </div>
          <p class="page-subtitle" v-if="subTitle || subTitleAlias">{{ subTitle || subTitleAlias }}</p>
        </div>
        <div class="page-head-extra" v-if="$slots.extra">
          <slot name="extra" />
        </div>
      </div>
      <div class="page-head-foot" v-if="$slots.meta || $slots.stats">
        <div class="page-head-meta" v-if="$slots.meta">
          <slot name="meta" />
        </div>
        <div class="page-head-stats" v-if="$slots.stats">
          <slot name="stats" />
        </div>
      </div>
    </section>
    <section class="page-body">
      <slot />
    </section>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{ title: string; subTitle?: string; subTitleAlias?: string; mode?: 'workspace' | 'showcase'; kicker?: string }>(), {
  mode: 'workspace'
})
const subTitleAlias = props.subTitleAlias
const mode = props.mode
const kicker = props.kicker
</script>

<style scoped>
.page-shell {
  gap: 18px;
}

.page-head {
  position: relative;
  padding: 22px 24px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: var(--shadow-xs);
  overflow: hidden;
}

.page-head-accent {
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: linear-gradient(180deg, #2e8a72 0%, #21705f 100%);
  border-radius: 999px;
}

.page-head--showcase {
  background:
    radial-gradient(320px 220px at 100% 0%, rgba(33, 112, 95, 0.08), transparent 68%),
    linear-gradient(135deg, #ffffff 0%, #fbfcf9 60%, #f4f8f4 100%);
}

.page-head--showcase::before,
.page-head--showcase::after {
  content: '';
  position: absolute;
  pointer-events: none;
  border-radius: 50%;
}

.page-head--showcase::before {
  inset: auto auto -80px -60px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(46, 138, 114, 0.1) 0%, rgba(46, 138, 114, 0) 70%);
}

.page-head--showcase::after {
  top: -80px;
  right: -50px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(229, 138, 58, 0.1) 0%, rgba(229, 138, 58, 0) 70%);
}

.page-head-glow {
  position: absolute;
  right: -88px;
  bottom: -96px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(33, 112, 95, 0.12) 0%, rgba(33, 112, 95, 0) 72%);
  pointer-events: none;
}

.page-head-main {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.page-title-wrap {
  min-width: 0;
  display: grid;
  gap: 10px;
}

.page-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.page-kicker {
  display: flex;
  align-items: center;
}

.page-kicker-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-strong);
  font-size: 12px;
  letter-spacing: 0.06em;
  font-weight: 700;
}

.page-title {
  margin: 0;
  font-size: 26px;
  line-height: 1.12;
  letter-spacing: 0.01em;
  color: var(--ink);
}

.page-subtitle {
  margin: 0;
  max-width: 760px;
  font-size: 14px;
  color: var(--muted);
  line-height: 1.7;
}

.page-head-extra {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
  align-self: stretch;
}

.page-head-extra :deep(.ant-space) {
  justify-content: flex-end;
}

.page-head-extra :deep(.ant-btn),
.page-head-extra :deep(.ant-select-selector),
.page-head-extra :deep(.ant-input-affix-wrapper) {
  border-radius: 10px;
}

.page-head-foot {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--border-soft);
}

.page-head-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-head-stats {
  min-width: 0;
  flex: 1 1 320px;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-shell--workspace .page-title {
  font-size: 24px;
}

.page-shell--workspace .page-head {
  background: #ffffff;
}

.page-shell--workspace .page-head-accent {
  background: linear-gradient(180deg, #2e8a72 0%, #21705f 100%);
}

.page-shell--showcase .page-title {
  font-size: 30px;
}

.page-shell--workspace .page-subtitle {
  max-width: 620px;
  font-size: 13px;
}

.page-shell--workspace .page-kicker-chip {
  background: var(--primary-soft);
  color: var(--primary-strong);
}

.page-head-meta :deep(.ant-tag),
.page-head-meta :deep(.ant-space-item > .soft-pill),
.page-head-meta :deep(.ant-space-item > .selection-pill) {
  margin-inline-end: 0;
}

@media (max-width: 992px) {
  .page-head {
    padding: 18px;
  }

  .page-head-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-head-extra {
    justify-content: flex-start;
    width: 100%;
    align-self: auto;
  }

  .page-head-extra :deep(.ant-space) {
    justify-content: flex-start;
  }

  .page-head-foot {
    flex-direction: column;
  }

  .page-title {
    font-size: 24px;
  }

  .page-shell--workspace .page-title {
    font-size: 22px;
  }
}
</style>
