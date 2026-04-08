<template>
  <div class="page-container page-shell" :class="[`page-shell--${mode}`]">
    <section class="page-head" :class="{ 'page-head--showcase': mode === 'showcase' }">
      <div class="page-head-glow" v-if="mode === 'showcase'"></div>
      <div class="page-head-main">
        <div class="page-title-wrap">
          <div class="page-kicker">{{ mode === 'showcase' ? 'Smart Senior Care SaaS' : '业务工作面' }}</div>
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
      <div class="page-head-meta" v-if="$slots.meta">
        <slot name="meta" />
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
const props = withDefaults(defineProps<{ title: string; subTitle?: string; subTitleAlias?: string; mode?: 'workspace' | 'showcase' }>(), {
  mode: 'workspace'
})
const subTitleAlias = props.subTitleAlias
const mode = props.mode
</script>

<style scoped>
.page-shell {
  gap: 20px;
}

.page-head {
  position: relative;
  padding: 8px 0 4px;
  border-bottom: 1px solid rgba(200, 216, 229, 0.72);
}

.page-head--showcase {
  padding: 24px 26px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background:
    radial-gradient(320px 220px at 100% 0%, rgba(87, 215, 255, 0.12), transparent 70%),
    radial-gradient(240px 180px at 0% 100%, rgba(121, 177, 255, 0.1), transparent 72%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(246, 250, 253, 0.96) 52%, rgba(240, 248, 252, 0.94) 100%);
  box-shadow: var(--shadow-md);
  overflow: hidden;
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
  background: radial-gradient(circle, rgba(87, 215, 255, 0.14) 0%, rgba(87, 215, 255, 0) 70%);
}

.page-head--showcase::after {
  top: -80px;
  right: -50px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(121, 177, 255, 0.16) 0%, rgba(121, 177, 255, 0) 70%);
}

.page-head-glow {
  position: absolute;
  right: -88px;
  bottom: -96px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(19, 108, 181, 0.16) 0%, rgba(19, 108, 181, 0) 72%);
  pointer-events: none;
}

.page-head-main {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title-wrap {
  min-width: 0;
  display: grid;
  gap: 8px;
}

.page-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.page-kicker {
  color: #7f96aa;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-weight: 700;
}

.page-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.08;
  letter-spacing: 0.01em;
  color: #12314d;
}

.page-subtitle {
  margin: 0;
  max-width: 820px;
  font-size: 13px;
  color: #5f7b95;
  line-height: 1.72;
}

.page-head-extra {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.page-head-meta {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.page-head-stats {
  position: relative;
  margin-top: 18px;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-shell--workspace .page-title {
  font-size: 26px;
}

.page-shell--workspace .page-subtitle {
  max-width: 620px;
}

@media (max-width: 992px) {
  .page-head {
    padding: 4px 0 2px;
  }

  .page-head--showcase {
    padding: 18px;
  }

  .page-head-main {
    flex-direction: column;
  }

  .page-head-extra {
    justify-content: flex-start;
    width: 100%;
  }

  .page-title {
    font-size: 24px;
  }
}
</style>
