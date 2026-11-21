<template>
  <section class="scenic-header">
    <div class="scenic-header__banner">
      <img class="scenic-header__image" :src="imageUrl" alt="" aria-hidden="true" />
      <div class="scenic-header__veil" aria-hidden="true"></div>
      <div class="scenic-header__content">
        <div class="scenic-header__copy">
          <span class="scenic-header__eyebrow">{{ eyebrow }}</span>
          <h1>{{ title }}</h1>
          <p>{{ description }}</p>
        </div>
        <slot name="aside" />
      </div>
    </div>
    <div v-if="$slots.default" class="scenic-header__body"><slot /></div>
  </section>
</template>

<script setup lang="ts">
import { visualAssets } from '../../config/visualAssets'

withDefaults(defineProps<{
  eyebrow: string
  title: string
  description: string
  imageUrl?: string
}>(), {
  imageUrl: visualAssets.scenicMountain.url
})
</script>

<style scoped>
.scenic-header {
  display: grid;
  gap: 12px;
  margin-bottom: 18px;
}

.scenic-header__banner {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  min-height: 160px;
  padding: 26px 30px;
  border: 1px solid rgba(180, 164, 127, .2);
  border-radius: 14px;
  background: #f6f2e8;
}

.scenic-header__image,
.scenic-header__veil {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.scenic-header__image { z-index: -2; object-fit: cover; object-position: center 52%; filter: saturate(.78) contrast(.92); }
.scenic-header__veil { z-index: -1; background: linear-gradient(90deg, rgba(255, 253, 247, .98) 0%, rgba(255, 253, 247, .9) 42%, rgba(255, 253, 247, .35) 72%, rgba(255, 253, 247, .5) 100%); }
.scenic-header__content { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; }
.scenic-header__copy { max-width: 700px; }
.scenic-header__eyebrow { color: #18705d; font-size: 13px; font-weight: 700; letter-spacing: .08em; }
.scenic-header h1 { margin: 8px 0 6px; color: #17251f; font-family: "Noto Serif SC", "Songti SC", serif; font-size: clamp(27px, 3vw, 38px); line-height: 1.24; }
.scenic-header p { margin: 0; color: #53615b; font-size: 14px; line-height: 1.8; }
.scenic-header__body { min-width: 0; }

@media (max-width: 768px) {
  .scenic-header__banner { min-height: 0; padding: 20px; }
  .scenic-header__content { display: grid; }
  .scenic-header__veil { background: rgba(255, 253, 247, .9); }
}

@media (prefers-reduced-motion: reduce) {
  .scenic-header * { scroll-behavior: auto; }
}
</style>
