<template>
  <div class="fv">
    <section class="rm-card fv-tree">
      <div class="rm-card__head"><span class="rm-card__title">区域与摄像头</span></div>
      <a-input-search v-model:value="keyword" placeholder="搜索摄像头名称" allow-clear @search="loadCameras" />
      <div v-for="group in groups" :key="group.name" class="fv-group">
        <b>{{ group.name }} · {{ group.cameras.length }} 路</b>
        <button v-for="camera in group.cameras" :key="String(camera.id)" type="button"
          :class="{ 'is-active': selected.includes(String(camera.id)) }" @click="toggle(String(camera.id))">
          {{ camera.cameraName }}<small v-if="!camera.enabled">（停用）</small>
        </button>
      </div>
      <a-empty v-if="!groups.length" description="还没有登记摄像头" />
    </section>

    <section class="rm-card fv-wall">
      <div class="rm-card__head">
        <span class="rm-card__title">公共区域视频监控 <small class="rm-card__sub">最多同时查看 4 路</small></span>
        <a class="rm-link" @click="router.push('/fire/control-center?tab=access')">申请录像调阅 →</a>
      </div>
      <div class="fv-tiles">
        <div v-for="tile in tiles" :key="tile.id" class="fv-tile">
          <span class="fv-tile__name">{{ tile.name }}</span>
          <video v-if="tile.stream?.configured && tile.stream.url" :src="tile.stream.url" autoplay muted playsinline />
          <div v-else class="fv-tile__placeholder">{{ tile.stream?.message || '加载中…' }}</div>
        </div>
        <div v-for="n in Math.max(0, 4 - tiles.length)" :key="`empty-${n}`" class="fv-tile fv-tile--empty">从左侧选择摄像头</div>
      </div>
      <p class="fo-muted">实时画面通过视频平台接口接入（后端 FireVideoLiveGateway）；未接入时显示占位说明。录像查看须申请并经主管批准。</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getVideoCameras, type VideoCamera } from '../../../api/logisticsVideo'
import { getFireLiveStream, type FireLiveStream } from '../../../api/fireOps'
import '../../elder/residentUi.css'

const router = useRouter()
const keyword = ref('')
const cameras = ref<VideoCamera[]>([])
const selected = ref<string[]>([])
const streams = ref<Record<string, FireLiveStream>>({})

// 按位置第一段（楼栋）分组
const groups = computed(() => {
  const map = new Map<string, VideoCamera[]>()
  for (const camera of cameras.value) {
    const name = (camera.location || '未分区').split(/[·\s/-]/)[0] || '未分区'
    map.set(name, [...(map.get(name) || []), camera])
  }
  return [...map.entries()].map(([name, list]) => ({ name, cameras: list }))
})
const tiles = computed(() => selected.value.map((id) => ({
  id, name: cameras.value.find((c) => String(c.id) === id)?.cameraName || id, stream: streams.value[id]
})))

function toggle(id: string) {
  selected.value = selected.value.includes(id) ? selected.value.filter((x) => x !== id) : [...selected.value, id].slice(-4)
}

async function loadCameras() {
  cameras.value = (await getVideoCameras({ pageNo: 1, pageSize: 200, keyword: keyword.value || undefined })).list || []
  if (!selected.value.length) selected.value = cameras.value.filter((c) => c.enabled).slice(0, 4).map((c) => String(c.id))
}

watch(selected, async (ids) => {
  for (const id of ids) {
    if (!streams.value[id]) streams.value = { ...streams.value, [id]: await getFireLiveStream(id).catch(() => ({ configured: false, message: '画面获取失败' })) }
  }
})

onMounted(loadCameras)
</script>

<style scoped>
.fv { display: grid; grid-template-columns: 240px minmax(0, 1fr); gap: 16px; }
.fv-tree { display: grid; gap: 10px; align-content: start; }
.fv-group { display: grid; gap: 4px; }
.fv-group b { font-size: 13px; color: #33433a; }
.fv-group button { padding: 4px 10px; border: 0; border-radius: 6px; background: none; text-align: left; color: #4f6258; cursor: pointer; }
.fv-group button.is-active { background: #e8f6ef; color: #1b6e52; font-weight: 600; }
.fv-tiles { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.fv-tile { position: relative; aspect-ratio: 16 / 9; overflow: hidden; border-radius: 10px; background: #e9efec; }
.fv-tile video { width: 100%; height: 100%; object-fit: cover; }
.fv-tile__name { position: absolute; left: 8px; bottom: 8px; z-index: 1; padding: 2px 8px; border-radius: 6px; background: rgba(20, 50, 40, .75); color: #fff; font-size: 12px; }
.fv-tile__placeholder, .fv-tile--empty { display: grid; height: 100%; place-items: center; padding: 12px; color: #6b7a74; font-size: 13px; text-align: center; }
.fo-muted { margin: 10px 0 0; color: #8a968f; font-size: 12px; }
@media (max-width: 900px) { .fv { grid-template-columns: 1fr; } }
</style>
