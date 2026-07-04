<template>
  <div class="panorama-container" ref="containerRef">
    <div class="scene-atmosphere"></div>

    <div class="scene-toolbar">
      <div class="toolbar-left">
        <a-button v-if="currentLevel !== 'PARK'" class="tech-btn" @click="goUpLevel">返回上级</a-button>
        <a-button class="tech-btn" @click="toggleCruise">{{ cruiseEnabled ? '暂停巡航' : '自动巡航' }}</a-button>
      </div>

      <div class="toolbar-center">
        <span class="toolbar-eyebrow">{{ currentLevelLabel }}视角</span>
        <strong>{{ breadcrumbText }}</strong>
        <small>{{ focusTitle }} · {{ focusStatus }} · {{ toolbarHint }}</small>
      </div>

      <div class="toolbar-right">
        <a-button class="tech-btn" @click="resetCamera">复位</a-button>
        <a-button class="tech-btn" @click="toggleFullscreen">{{ fullscreenActive ? '退出全屏' : '全屏' }}</a-button>
      </div>
    </div>

    <div class="scene-hud">
      <div class="scene-legend">
        <div class="legend-chip status-occupied">在住</div>
        <div class="legend-chip status-empty">空床</div>
        <div class="legend-chip status-warning">维护 / 外出</div>
        <div class="legend-chip status-ai">重点关注</div>
        <div class="legend-chip status-alert">异常告警</div>
      </div>
    </div>

    <div
      v-show="tooltip.visible"
      class="tech-tooltip"
      :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }"
    >
      <div class="tt-header">
        <strong>{{ tooltip.title }}</strong>
        <small>{{ tooltip.badge }}</small>
      </div>
      <div class="tt-body">
        <div v-for="row in tooltip.rows" :key="row.label" class="tt-row">
          <span>{{ row.label }}</span>
          <strong :class="row.tone">{{ row.value }}</strong>
        </div>
      </div>
      <div class="tt-tip">{{ tooltip.tip }}</div>
    </div>

    <div ref="canvasRef" class="canvas-wrapper"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DObject, CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js'
import gsap from 'gsap'

type PanoramaScopeLevel = 'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM'
type PanoramaScope = {
  level: PanoramaScopeLevel
  building: string
  floor: string
  room: string
}

type RoomScene = {
  roomNo: string
  occupiedBeds: number
  totalBeds: number
  beds: any[]
}

type TooltipRow = {
  label: string
  value: string
  tone?: string
}

const props = defineProps<{
  buildings: string[]
  floors: string[]
  roomLookup: Map<string, RoomScene[]>
  scope?: PanoramaScope
}>()

const emit = defineEmits<{
  (e: 'click-room', room: RoomScene): void
  (e: 'click-bed', bed: any): void
  (e: 'scope-change', scope: PanoramaScope): void
}>()

const containerRef = ref<HTMLElement | null>(null)
const canvasRef = ref<HTMLElement | null>(null)
const currentLevel = ref<PanoramaScopeLevel>('PARK')
const selectedBuilding = ref('')
const selectedFloor = ref('')
const selectedRoom = ref('')
const cruiseEnabled = ref(true)
const fullscreenActive = ref(false)
const focusTitle = ref('园区总览')
const focusStatus = ref('等待交互')
const sceneRenderingEnabled = ref(true)
const tooltip = ref({
  visible: false,
  x: 0,
  y: 0,
  title: '',
  badge: '',
  rows: [] as TooltipRow[],
  tip: ''
})

const scene = shallowRef(new THREE.Scene())
const camera = shallowRef<THREE.PerspectiveCamera | null>(null)
const renderer = shallowRef<THREE.WebGLRenderer | null>(null)
const cssRenderer = shallowRef<CSS2DRenderer | null>(null)
const controls = shallowRef<OrbitControls | null>(null)

const objectsMap = new Map<string, THREE.Object3D>()
const interactableObjects: THREE.Object3D[] = []
const animatedBeds: Array<{ mesh: THREE.Mesh; type: 'pulse' | 'blink' | 'sleep' | 'ai'; baseIntensity: number }> = []
let particles: THREE.Points | null = null
let animationFrameId = 0
let rebuildSceneTimer = 0
let lastPointerMoveAt = 0
let hoveredObj: THREE.Object3D | null = null
let pointerDownPos: { x: number; y: number } | null = null
const pickPosition = new THREE.Vector2()
const raycaster = new THREE.Raycaster()

const currentScope = computed<PanoramaScope>(() => ({
  level: currentLevel.value,
  building: selectedBuilding.value,
  floor: selectedFloor.value,
  room: selectedRoom.value
}))

const breadcrumbText = computed(() => {
  let text = '园区全景'
  if (selectedBuilding.value) text += ` / ${selectedBuilding.value}`
  if (selectedFloor.value) text += ` / ${selectedFloor.value}`
  if (selectedRoom.value) text += ` / ${selectedRoom.value}`
  return text
})

const toolbarHint = computed(() => {
  if (currentLevel.value === 'ROOM') return '再次点击房间可打开业务详情，点击床位可查看守护信息'
  if (currentLevel.value === 'FLOOR') return '楼层已展开，可继续选择房间或床位'
  if (currentLevel.value === 'BUILDING') return '楼栋已展开，可进入楼层鸟瞰视角'
  return '先看楼栋群组，再逐级钻取到楼层、房间与床位'
})

const currentLevelLabel = computed(() => {
  if (currentLevel.value === 'PARK') return '园区'
  if (currentLevel.value === 'BUILDING') return '楼栋'
  if (currentLevel.value === 'FLOOR') return '楼层'
  return '房间'
})

// 床位状态语义配色（与全局养老绿主题一致）：
// 在住=品牌绿 / 平稳在住=浅一档的绿 / 关注=蓝紫 / 维护·外出=琥珀 / 异常=红 / 空床=暖灰
const materials = {
  buildingShell: new THREE.MeshStandardMaterial({ color: 0xf0f3ec, transparent: true, opacity: 0.98, roughness: 0.74, metalness: 0.06 }),
  buildingCap: new THREE.MeshStandardMaterial({ color: 0xe2e8dc, transparent: true, opacity: 1, roughness: 0.62, metalness: 0.04 }),
  buildingHitbox: new THREE.MeshBasicMaterial({ color: 0x2e8a72, transparent: true, opacity: 0.01, side: THREE.DoubleSide }),
  floor: new THREE.MeshStandardMaterial({ color: 0xf9fbf5, transparent: true, opacity: 0.98, roughness: 0.8, metalness: 0.02 }),
  roomNeutral: new THREE.MeshStandardMaterial({ color: 0xf4f0e6, transparent: true, opacity: 0.98, roughness: 0.84, metalness: 0.02 }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0xe4f1ea, emissive: 0xdcede6, emissiveIntensity: 0.08, transparent: true, opacity: 0.98 }),
  hover: new THREE.MeshStandardMaterial({ color: 0xe8f3ef, emissive: 0xd6eae2, emissiveIntensity: 0.16, transparent: true, opacity: 0.98 }),
  bedNormal: new THREE.MeshStandardMaterial({ color: 0xccd4cc, emissive: 0xe7ece5, emissiveIntensity: 0.08, metalness: 0.12, roughness: 0.44 }),
  bedOccupied: new THREE.MeshStandardMaterial({ color: 0x2e8a72, emissive: 0x6cc3a6, emissiveIntensity: 0.3, metalness: 0.08, roughness: 0.3 }),
  bedSleep: new THREE.MeshStandardMaterial({ color: 0x55a98c, emissive: 0x8fd4ba, emissiveIntensity: 0.22, metalness: 0.06, roughness: 0.3 }),
  bedAi: new THREE.MeshStandardMaterial({ color: 0x6b79d8, emissive: 0xa4adf0, emissiveIntensity: 0.28, metalness: 0.08, roughness: 0.34 }),
  bedWarning: new THREE.MeshStandardMaterial({ color: 0xde9b3d, emissive: 0xf3c37e, emissiveIntensity: 0.24, metalness: 0.06, roughness: 0.3 }),
  bedAlert: new THREE.MeshStandardMaterial({ color: 0xc9504b, emissive: 0xef8f88, emissiveIntensity: 0.36, metalness: 0.06, roughness: 0.28 }),
  bedOffline: new THREE.MeshStandardMaterial({ color: 0xc7cdc6, emissive: 0xdde2da, emissiveIntensity: 0.08, metalness: 0.08, roughness: 0.42 })
}

function emitScopeChange(scope: PanoramaScope) {
  emit('scope-change', { ...scope })
}

function applyScope(scope: PanoramaScope, options?: { emit?: boolean; animate?: boolean }) {
  currentLevel.value = scope.level
  selectedBuilding.value = scope.building || ''
  selectedFloor.value = scope.floor || ''
  selectedRoom.value = scope.room || ''
  updateVisibility()

  if (options?.emit) {
    emitScopeChange(scope)
  }
  if (options?.animate !== false) {
    focusScope(scope)
  }
}

function createLabel(text: string, className: string) {
  const div = document.createElement('div')
  div.className = `scene-label ${className}`
  div.textContent = text
  return new CSS2DObject(div)
}

function resolveBedVisualState(bed: any): 'normal' | 'occupied' | 'sleep' | 'ai' | 'warning' | 'alert' | 'offline' {
  const abnormalCount = Number(bed.abnormalVital24hCount || 0)
  if (bed.status === 0 || bed.occupancySource === 'FROZEN') return 'offline'
  if (bed.riskLevel === 'HIGH' || abnormalCount > 0) return 'alert'
  if (bed.status === 3 || bed.occupancySource === 'MAINTENANCE' || bed.occupancySource === 'CLEANING') return 'warning'
  if (bed.riskLevel === 'MEDIUM' || bed.riskSource) return 'ai'
  if (bed.riskLevel === 'LOW' && bed.elderId) return 'sleep'
  if (bed.elderId) return 'occupied'
  return 'normal'
}

function materialForState(state: ReturnType<typeof resolveBedVisualState>) {
  if (state === 'occupied') return materials.bedOccupied.clone()
  if (state === 'sleep') return materials.bedSleep.clone()
  if (state === 'ai') return materials.bedAi.clone()
  if (state === 'warning') return materials.bedWarning.clone()
  if (state === 'alert') return materials.bedAlert.clone()
  if (state === 'offline') return materials.bedOffline.clone()
  return materials.bedNormal.clone()
}

function roomMaterialForData(room: RoomScene) {
  const ratio = room.totalBeds ? room.occupiedBeds / room.totalBeds : 0
  const alertBeds = room.beds.filter((bed) => String(bed.riskLevel || '') === 'HIGH' || Number(bed.abnormalVital24hCount || 0) > 0).length
  const concernBeds = room.beds.filter((bed) => String(bed.riskLevel || '') === 'MEDIUM').length
  const material = materials.roomNeutral.clone()
  if (alertBeds > 0) {
    // 有高风险/异常床位的房间：地面泛红，一眼即见
    material.color = new THREE.Color(0xf9ebe9)
    material.emissive = new THREE.Color(0xf2c7c2)
    material.emissiveIntensity = 0.07
    return material
  }
  if (concernBeds > 0) {
    material.color = new THREE.Color(0xeef0fa)
    material.emissive = new THREE.Color(0xd4d9f4)
    material.emissiveIntensity = 0.06
    return material
  }
  if (ratio >= 1) {
    material.color = new THREE.Color(0xe8f3ec)
    material.emissive = new THREE.Color(0xd0ecdb)
    material.emissiveIntensity = 0.05
    return material
  }
  if (ratio === 0) {
    material.color = new THREE.Color(0xf3f5f0)
    return material
  }
  material.color = new THREE.Color(0xf4f0e6)
  material.emissive = new THREE.Color(0xfaf3e6)
  material.emissiveIntensity = 0.03
  return material
}

// 建筑改为"微缩沙盘"手法：单体圆角体量 + 层间凹槽 + 内嵌暖窗 + 出挑屋檐
function createBuildingMass(totalFloors: number) {
  const group = new THREE.Group()
  const width = 10
  const depth = 10
  const floorHeight = 1.72
  const bodyHeight = totalFloors * floorHeight

  const body = new THREE.Mesh(
    new RoundedBoxGeometry(width, bodyHeight, depth, 3, 0.42),
    materials.buildingShell.clone()
  )
  body.position.y = bodyHeight / 2
  body.castShadow = true
  body.receiveShadow = true
  group.add(body)

  const grooveMaterial = new THREE.MeshStandardMaterial({ color: 0xdde3d4, roughness: 0.8, metalness: 0.02 })
  const windowMaterial = new THREE.MeshStandardMaterial({
    color: 0xf6ecd2,
    emissive: 0xefdcae,
    emissiveIntensity: 0.28,
    roughness: 0.35,
    metalness: 0.03
  })
  const frameMaterial = new THREE.MeshStandardMaterial({ color: 0xe7ebdf, roughness: 0.6, metalness: 0.02 })

  const windowsPerSide = 4
  const winW = 1.16
  const winH = 0.76
  const winGeo = new RoundedBoxGeometry(winW, winH, 0.1, 2, 0.1)
  const frameGeo = new RoundedBoxGeometry(winW + 0.18, winH + 0.18, 0.05, 2, 0.08)

  for (let level = 0; level < totalFloors; level += 1) {
    const centerY = level * floorHeight + floorHeight / 2

    // 层间细凹槽，远看有清晰楼层节奏
    if (level > 0) {
      const groove = new THREE.Mesh(new THREE.BoxGeometry(width + 0.04, 0.07, depth + 0.04), grooveMaterial)
      groove.position.y = level * floorHeight
      group.add(groove)
    }

    // 四立面窗洞：框 + 暖光窗芯，间隔亮灯更自然
    for (let w = 0; w < windowsPerSide; w += 1) {
      const offset = (w - (windowsPerSide - 1) / 2) * (width / windowsPerSide)
      const lit = (level * 7 + w * 3) % 5 !== 0
      const winMat = lit ? windowMaterial : new THREE.MeshStandardMaterial({ color: 0xcfd8e2, roughness: 0.3, metalness: 0.08 })
      ;[-1, 1].forEach((direction) => {
        const frameZ = new THREE.Mesh(frameGeo, frameMaterial)
        frameZ.position.set(offset, centerY, direction * (depth / 2 + 0.02))
        group.add(frameZ)
        const winZ = new THREE.Mesh(winGeo, winMat)
        winZ.position.set(offset, centerY, direction * (depth / 2 + 0.06))
        group.add(winZ)

        const frameX = new THREE.Mesh(frameGeo, frameMaterial)
        frameX.rotation.y = Math.PI / 2
        frameX.position.set(direction * (width / 2 + 0.02), centerY, offset)
        group.add(frameX)
        const winX = new THREE.Mesh(winGeo, winMat)
        winX.rotation.y = Math.PI / 2
        winX.position.set(direction * (width / 2 + 0.06), centerY, offset)
        group.add(winX)
      })
    }
  }

  // 出挑屋檐 + 屋顶设备间，轮廓不再是平顶盒子
  const eave = new THREE.Mesh(new RoundedBoxGeometry(width + 1.1, 0.34, depth + 1.1, 2, 0.16), materials.buildingCap.clone())
  eave.position.y = bodyHeight + 0.17
  eave.castShadow = true
  group.add(eave)

  const penthouse = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.34, 0.72, depth * 0.3, 2, 0.14),
    new THREE.MeshStandardMaterial({ color: 0xe9eee2, roughness: 0.7, metalness: 0.03 })
  )
  penthouse.position.set(-width * 0.2, bodyHeight + 0.34 + 0.36, -depth * 0.18)
  penthouse.castShadow = true
  group.add(penthouse)

  const roofGarden = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.4, 0.24, depth * 0.42, 2, 0.12),
    new THREE.MeshStandardMaterial({ color: 0x9dc3a2, roughness: 0.9, metalness: 0.01 })
  )
  roofGarden.position.set(width * 0.2, bodyHeight + 0.34 + 0.12, depth * 0.16)
  group.add(roofGarden)

  return group
}

// 简洁风格化树木：圆角树冠 + 圆柱树干，撑起园区绿意
function createTree(scale = 1) {
  const tree = new THREE.Group()
  const trunk = new THREE.Mesh(
    new THREE.CylinderGeometry(0.14 * scale, 0.2 * scale, 1.1 * scale, 8),
    new THREE.MeshStandardMaterial({ color: 0x9a7b58, roughness: 0.9, metalness: 0.01 })
  )
  trunk.position.y = 0.55 * scale
  trunk.castShadow = true
  tree.add(trunk)

  const foliageMaterial = new THREE.MeshStandardMaterial({ color: 0x7fb08a, roughness: 0.85, metalness: 0.01 })
  const crown = new THREE.Mesh(new THREE.SphereGeometry(1.05 * scale, 14, 12), foliageMaterial)
  crown.position.y = 1.9 * scale
  crown.scale.y = 1.12
  crown.castShadow = true
  tree.add(crown)

  const crownSide = new THREE.Mesh(new THREE.SphereGeometry(0.62 * scale, 12, 10), foliageMaterial.clone())
  ;(crownSide.material as THREE.MeshStandardMaterial).color = new THREE.Color(0x92bd97)
  crownSide.position.set(0.62 * scale, 1.5 * scale, 0.24 * scale)
  crownSide.castShadow = true
  tree.add(crownSide)

  return tree
}

function createBedUnit(bed: any, width: number, depth: number) {
  const state = resolveBedVisualState(bed)
  const group = new THREE.Group()
  const frameHeight = 0.18

  const frameMaterial = new THREE.MeshStandardMaterial({ color: 0xeaeee6, metalness: 0.12, roughness: 0.38 })
  const frameRailGeo = new THREE.BoxGeometry(width, frameHeight, 0.08)
  const sideRailGeo = new THREE.BoxGeometry(0.08, frameHeight, depth - 0.18)
  ;[-1, 1].forEach((direction) => {
    const rail = new THREE.Mesh(frameRailGeo, frameMaterial)
    rail.position.set(0, frameHeight / 2, direction * (depth / 2 - 0.04))
    group.add(rail)
  })
  ;[-1, 1].forEach((direction) => {
    const rail = new THREE.Mesh(sideRailGeo, frameMaterial)
    rail.position.set(direction * (width / 2 - 0.04), frameHeight / 2, 0)
    group.add(rail)
  })

  ;[-1, 1].forEach((xDirection) => {
    ;[-1, 1].forEach((zDirection) => {
      const leg = new THREE.Mesh(
        new THREE.CylinderGeometry(0.035, 0.05, 0.24, 10),
        new THREE.MeshStandardMaterial({ color: 0xcdd5cb, metalness: 0.18, roughness: 0.36 })
      )
      leg.position.set(xDirection * (width / 2 - 0.12), 0.12, zDirection * (depth / 2 - 0.12))
      group.add(leg)
    })
  })

  const mattressMaterial = materialForState(state)
  const mattress = new THREE.Mesh(new RoundedBoxGeometry(width * 0.8, 0.14, depth * 0.68, 2, 0.05), mattressMaterial)
  mattress.position.y = frameHeight + 0.07
  mattress.castShadow = true
  mattress.userData = { type: 'bed', bed, originalMat: mattressMaterial, state }
  group.add(mattress)
  interactableObjects.push(mattress)

  const pillow = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.42, 0.09, depth * 0.16, 2, 0.04),
    new THREE.MeshStandardMaterial({ color: 0xfdfefb, roughness: 0.62, metalness: 0.02 })
  )
  pillow.position.set(0, frameHeight + 0.16, -depth * 0.2)
  group.add(pillow)

  const blanket = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.76, 0.08, depth * 0.34, 2, 0.04),
    new THREE.MeshStandardMaterial({
      color: state === 'alert' ? 0xf6d4d1 : state === 'ai' ? 0xdde1f8 : state === 'warning' ? 0xf6e4c4 : 0xdfeade,
      roughness: 0.7,
      metalness: 0.02
    })
  )
  blanket.position.set(0, frameHeight + 0.14, depth * 0.08)
  group.add(blanket)

  const headboard = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.84, 0.34, 0.1, 2, 0.05),
    new THREE.MeshStandardMaterial({ color: 0xd3c9b4, metalness: 0.08, roughness: 0.4 })
  )
  headboard.position.set(0, 0.3, -depth * 0.34)
  headboard.castShadow = true
  group.add(headboard)

  const tailboard = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.72, 0.2, 0.08, 2, 0.04),
    new THREE.MeshStandardMaterial({ color: 0xddd5c3, metalness: 0.08, roughness: 0.42 })
  )
  tailboard.position.set(0, 0.2, depth * 0.33)
  group.add(tailboard)

  // 床头小柜替代原先的输液杆式监护屏，只在有长者的床位出现，柜上一块小监护屏
  if (bed.elderId) {
    const bedside = new THREE.Group()
    const cabinet = new THREE.Mesh(
      new RoundedBoxGeometry(0.22, 0.26, 0.22, 2, 0.04),
      new THREE.MeshStandardMaterial({ color: 0xe3dcc9, roughness: 0.6, metalness: 0.03 })
    )
    cabinet.position.y = 0.13
    cabinet.castShadow = true
    bedside.add(cabinet)
    const monitorScreen = new THREE.Mesh(
      new RoundedBoxGeometry(0.16, 0.1, 0.03, 2, 0.02),
      new THREE.MeshStandardMaterial({
        color: state === 'alert' ? 0x2f1820 : 0x18271f,
        emissive: state === 'alert' ? 0xff7d78 : state === 'ai' ? 0xa4adf0 : 0x63d6b8,
        emissiveIntensity: state === 'alert' ? 0.35 : 0.16,
        roughness: 0.34,
        metalness: 0.18
      })
    )
    monitorScreen.position.set(0, 0.32, 0)
    monitorScreen.rotation.x = -0.28
    bedside.add(monitorScreen)
    bedside.position.set(width / 2 + 0.16, 0, -depth * 0.2)
    group.add(bedside)
  }

  // 地面光环只保留在需要注意的床位（异常/维护），平稳床位保持干净
  if (state === 'alert' || state === 'warning') {
    const halo = new THREE.Mesh(
      new THREE.RingGeometry(Math.min(width, depth) * 0.38, Math.min(width, depth) * 0.52, 32),
      new THREE.MeshBasicMaterial({ color: mattressMaterial.color, transparent: true, opacity: state === 'alert' ? 0.26 : 0.14, side: THREE.DoubleSide })
    )
    halo.rotation.x = -Math.PI / 2
    halo.position.y = 0.03
    halo.userData.isHalo = true
    group.add(halo)
    mattress.userData.halo = halo
  }

  // 只让异常/维护床位有动效，平稳床位静止——整体更安静耐看
  if (state === 'alert') animatedBeds.push({ mesh: mattress, type: 'pulse', baseIntensity: 0.42 })
  else if (state === 'warning') animatedBeds.push({ mesh: mattress, type: 'blink', baseIntensity: 0.28 })

  return group
}

function buildAtmosphere() {
  if (!scene.value) return

  const campusBase = new THREE.Mesh(
    new THREE.CylinderGeometry(78, 84, 1.4, 64),
    new THREE.MeshStandardMaterial({
      color: 0xf1f4ec,
      roughness: 0.86,
      metalness: 0.02
    })
  )
  campusBase.position.y = -0.7
  campusBase.receiveShadow = true
  campusBase.userData.isGenerated = true
  scene.value.add(campusBase)

  // 草坪环带：园区外围一圈柔和的绿意，呼应养老院庭院
  const lawnRing = new THREE.Mesh(
    new THREE.RingGeometry(34, 74, 96),
    new THREE.MeshStandardMaterial({ color: 0xdcead8, roughness: 0.92, metalness: 0.01, transparent: true, opacity: 0.85, side: THREE.DoubleSide })
  )
  lawnRing.rotation.x = -Math.PI / 2
  lawnRing.position.y = 0.015
  lawnRing.receiveShadow = true
  lawnRing.userData.isGenerated = true
  scene.value.add(lawnRing)

  const gridHelper = new THREE.GridHelper(180, 28, 0xd3ddcd, 0xe6ece0)
  gridHelper.position.y = 0.02
  gridHelper.material.transparent = true
  ;(gridHelper.material as THREE.Material).opacity = 0.22
  gridHelper.userData.isGenerated = true
  scene.value.add(gridHelper)

  // 园区步道环：暖色混凝土质感，替代原先的发光科技环
  const pathRing = new THREE.Mesh(
    new THREE.RingGeometry(27.5, 30.5, 96),
    new THREE.MeshStandardMaterial({ color: 0xe9e2d2, roughness: 0.95, metalness: 0.01, transparent: true, opacity: 0.9, side: THREE.DoubleSide })
  )
  pathRing.rotation.x = -Math.PI / 2
  pathRing.position.y = 0.03
  pathRing.receiveShadow = true
  pathRing.userData.isGenerated = true
  scene.value.add(pathRing)

  // 园区树木：确定性伪随机分布在草坪带上，营造疗养花园氛围
  const treeCount = 16
  for (let i = 0; i < treeCount; i += 1) {
    const angle = (i / treeCount) * Math.PI * 2 + (i % 3) * 0.14
    const radius = 36 + ((i * 37) % 24)
    const scale = 0.85 + ((i * 13) % 10) / 22
    const tree = createTree(scale)
    tree.position.set(Math.cos(angle) * radius, 0, Math.sin(angle) * radius)
    tree.rotation.y = i * 1.7
    tree.userData.isGenerated = true
    scene.value.add(tree)
  }

  // 不再使用漂浮粒子（噪点感重），保留静谧沙盘氛围
  particles = null
}

function initScene() {
  if (!canvasRef.value) return

  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight

  scene.value = new THREE.Scene()
  scene.value.background = new THREE.Color(0xf5f7f1)
  scene.value.fog = new THREE.FogExp2(0xf5f7f1, 0.0022)

  camera.value = new THREE.PerspectiveCamera(40, width / height, 0.1, 1600)
  camera.value.position.set(50, 38, 62)

  renderer.value = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.value.setSize(width, height)
  renderer.value.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.value.setClearColor(0xf5f7f1, 1)
  renderer.value.outputColorSpace = THREE.SRGBColorSpace
  renderer.value.toneMapping = THREE.ACESFilmicToneMapping
  renderer.value.toneMappingExposure = 0.98
  // 柔和阴影：让楼栋与地面产生真实体积感
  renderer.value.shadowMap.enabled = true
  renderer.value.shadowMap.type = THREE.PCFSoftShadowMap
  canvasRef.value.appendChild(renderer.value.domElement)

  cssRenderer.value = new CSS2DRenderer()
  cssRenderer.value.setSize(width, height)
  cssRenderer.value.domElement.style.position = 'absolute'
  cssRenderer.value.domElement.style.top = '0'
  cssRenderer.value.domElement.style.pointerEvents = 'none'
  canvasRef.value.appendChild(cssRenderer.value.domElement)

  controls.value = new OrbitControls(camera.value, renderer.value.domElement)
  controls.value.enableDamping = true
  controls.value.dampingFactor = 0.04
  controls.value.minDistance = 10
  controls.value.maxDistance = 220
  controls.value.maxPolarAngle = Math.PI / 2 - 0.06
  controls.value.autoRotate = true
  controls.value.autoRotateSpeed = 0.09
  controls.value.target.set(0, 6, 0)

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.72)
  scene.value.add(ambientLight)

  // 天空-地面半球光：暖白天光 + 草地反光，比纯环境光更柔和自然
  const hemiLight = new THREE.HemisphereLight(0xfdfff8, 0xd7e2cf, 0.55)
  scene.value.add(hemiLight)

  const topLight = new THREE.DirectionalLight(0xfffbf2, 1.12)
  topLight.position.set(40, 90, 40)
  topLight.castShadow = true
  topLight.shadow.mapSize.set(2048, 2048)
  topLight.shadow.camera.left = -90
  topLight.shadow.camera.right = 90
  topLight.shadow.camera.top = 90
  topLight.shadow.camera.bottom = -90
  topLight.shadow.camera.far = 260
  topLight.shadow.bias = -0.0006
  topLight.shadow.radius = 4
  scene.value.add(topLight)

  const fillLight = new THREE.DirectionalLight(0xd6e8da, 0.4)
  fillLight.position.set(-56, 28, -46)
  scene.value.add(fillLight)

  const rimLight = new THREE.PointLight(0xbcd9c8, 0.72, 240, 2)
  rimLight.position.set(0, 18, -20)
  scene.value.add(rimLight)

  buildAtmosphere()

  renderer.value.domElement.addEventListener('pointermove', onPointerMove)
  renderer.value.domElement.addEventListener('pointerdown', onPointerDown)
  renderer.value.domElement.addEventListener('click', onClick)
  window.addEventListener('resize', onWindowResize)

  buildSceneData()
  animate()
}

function clearScene() {
  interactableObjects.length = 0
  animatedBeds.length = 0
  objectsMap.clear()
  hoveredObj = null
  tooltip.value.visible = false
  const removable = scene.value.children.filter((child) => child.userData.isGenerated)
  removable.forEach((child) => {
    child.traverse((node) => {
      const mesh = node as THREE.Mesh
      if (mesh.geometry) mesh.geometry.dispose?.()
      const material = mesh.material
      if (Array.isArray(material)) material.forEach((item) => item.dispose?.())
      else material?.dispose?.()
    })
    scene.value.remove(child)
  })
  buildAtmosphere()
}

function buildSceneData() {
  clearScene()
  if (!props.buildings.length) return

  const parkGroup = new THREE.Group()
  parkGroup.userData.isGenerated = true
  scene.value.add(parkGroup)

  const cols = Math.ceil(Math.sqrt(props.buildings.length))
  const spacing = 26

  props.buildings.forEach((buildingName, buildingIndex) => {
    const buildingGroup = new THREE.Group()
    buildingGroup.userData = { type: 'building', name: buildingName }

    const row = Math.floor(buildingIndex / cols)
    const col = buildingIndex % cols
    buildingGroup.position.set(
      (col - (cols - 1) / 2) * spacing,
      0,
      (row - (Math.ceil(props.buildings.length / cols) - 1) / 2) * spacing
    )

    const buildingFloors = props.floors.filter((floor) => props.roomLookup.has(`${buildingName}@@${floor}`))
    const totalFloors = buildingFloors.length || 1

    // 浅色基座平台 + 周边绿植带，替代原先突兀的深色圆柱
    const plinth = new THREE.Mesh(
      new RoundedBoxGeometry(11.6, 0.8, 11.6, 2, 0.28),
      new THREE.MeshStandardMaterial({ color: 0xeae5d7, roughness: 0.88, metalness: 0.02 })
    )
    plinth.position.y = 0.9
    plinth.receiveShadow = true
    buildingGroup.add(plinth)

    const plinthLawn = new THREE.Mesh(
      new RoundedBoxGeometry(13.2, 0.36, 13.2, 2, 0.18),
      new THREE.MeshStandardMaterial({ color: 0xc4dcc0, roughness: 0.94, metalness: 0.01 })
    )
    plinthLawn.position.y = 0.5
    plinthLawn.receiveShadow = true
    buildingGroup.add(plinthLawn)

    const buildingMass = createBuildingMass(totalFloors)
    buildingMass.position.y = 1.3
    buildingMass.userData.type = 'buildingMass'
    buildingGroup.add(buildingMass)

    buildingFloors.forEach((floorName, floorIndex) => {
      const floorGroup = new THREE.Group()
      const floorYIndex = totalFloors - 1 - floorIndex
      floorGroup.position.y = floorYIndex * 4.4 + 1.4
      floorGroup.userData = { type: 'floor', building: buildingName, name: floorName, floorYIndex }

      const rooms = props.roomLookup.get(`${buildingName}@@${floorName}`) || []
      const roomColumns = Math.ceil(Math.sqrt(Math.max(rooms.length, 1)))
      const roomSize = 4.5
      const roomGap = 0.84
      const slabWidth = roomColumns * (roomSize + roomGap) + 1.4
      const slabDepth = Math.ceil(Math.max(rooms.length, 1) / roomColumns) * (roomSize + roomGap) + 1.4

      const floorSlab = new THREE.Mesh(new THREE.BoxGeometry(slabWidth, 0.36, slabDepth), materials.floor.clone())
      floorSlab.position.y = 0.18
      floorSlab.receiveShadow = true
      floorSlab.userData = { type: 'floorSlab', building: buildingName, floor: floorName, originalMat: materials.floor.clone() }
      floorGroup.add(floorSlab)
      interactableObjects.push(floorSlab)
      objectsMap.set(`floor_${buildingName}_${floorName}`, floorGroup)

      const corridor = new THREE.Mesh(
        new THREE.BoxGeometry(Math.max(3.6, slabWidth * 0.24), 0.04, slabDepth - 1.2),
        new THREE.MeshStandardMaterial({ color: 0xefeee6, roughness: 0.92, metalness: 0.02 })
      )
      corridor.position.y = 0.21
      floorGroup.add(corridor)

      const nurseStation = new THREE.Group()
      nurseStation.position.set(0, 0.32, 0)
      const stationBase = new THREE.Mesh(
        new THREE.CylinderGeometry(1.3, 1.45, 0.42, 28),
        new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.5, metalness: 0.04 })
      )
      const stationTop = new THREE.Mesh(
        new THREE.CylinderGeometry(1.05, 1.15, 0.16, 28),
        new THREE.MeshStandardMaterial({ color: 0xd9e8dc, roughness: 0.32, metalness: 0.08 })
      )
      stationTop.position.y = 0.24
      nurseStation.add(stationBase)
      nurseStation.add(stationTop)
      floorGroup.add(nurseStation)

      const stationLabel = createLabel('护理站', 'station-label')
      stationLabel.position.set(0, 1.05, 0)
      stationLabel.visible = true
      floorGroup.add(stationLabel)

      const slabEdge = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.BoxGeometry(slabWidth, 0.36, slabDepth)),
        new THREE.LineBasicMaterial({ color: 0xb8cbe0, transparent: true, opacity: 0.28 })
      )
      slabEdge.position.y = 0.18
      floorGroup.add(slabEdge)

      rooms.forEach((roomItem, roomIndex) => {
        const roomGroup = new THREE.Group()
        roomGroup.userData = { type: 'room', building: buildingName, floor: floorName, room: roomItem }

        const r = Math.floor(roomIndex / roomColumns)
        const c = roomIndex % roomColumns
        roomGroup.position.set(
          (c - (roomColumns - 1) / 2) * (roomSize + roomGap),
          0.38,
          (r - (Math.ceil(Math.max(rooms.length, 1) / roomColumns) - 1) / 2) * (roomSize + roomGap)
        )

        const roomMaterial = roomMaterialForData(roomItem)
        const roomTile = new THREE.Mesh(new THREE.BoxGeometry(roomSize, 0.18, roomSize), roomMaterial)
        roomTile.position.y = 0.09
        roomTile.receiveShadow = true
        roomTile.userData = { type: 'roomTile', ref: roomGroup, data: roomItem, floorName, originalMat: roomMaterial }
        roomGroup.add(roomTile)
        interactableObjects.push(roomTile)

        const roomCarpet = new THREE.Mesh(
          new THREE.BoxGeometry(roomSize - 0.42, 0.02, roomSize - 0.42),
          new THREE.MeshStandardMaterial({
            color: roomItem.occupiedBeds ? 0xf8f4ed : 0xf3f7fb,
            roughness: 0.92,
            metalness: 0.01
          })
        )
        roomCarpet.position.y = 0.2
        roomGroup.add(roomCarpet)

        const wallMaterial = new THREE.MeshStandardMaterial({ color: 0xfbfaf3, roughness: 0.86, metalness: 0.02 })
        const wallHeight = 1.08
        const wallThickness = 0.08
        const sideWallDepth = roomSize - 0.32
        const rearWall = new THREE.Mesh(new THREE.BoxGeometry(roomSize, wallHeight, wallThickness), wallMaterial)
        rearWall.position.set(0, wallHeight / 2 + 0.09, -roomSize / 2 + wallThickness / 2)
        roomGroup.add(rearWall)
        ;[-1, 1].forEach((direction) => {
          const sideWall = new THREE.Mesh(new THREE.BoxGeometry(wallThickness, wallHeight, sideWallDepth), wallMaterial)
          sideWall.position.set(direction * (roomSize / 2 - wallThickness / 2), wallHeight / 2 + 0.09, 0)
          roomGroup.add(sideWall)
        })
        const frontWallWidth = (roomSize - 1.36) / 2
        ;[-1, 1].forEach((direction) => {
          const frontWall = new THREE.Mesh(new THREE.BoxGeometry(frontWallWidth, wallHeight, wallThickness), wallMaterial)
          frontWall.position.set(direction * (0.68 + frontWallWidth / 2), wallHeight / 2 + 0.09, roomSize / 2 - wallThickness / 2)
          roomGroup.add(frontWall)
        })
        const doorLintel = new THREE.Mesh(
          new THREE.BoxGeometry(1.28, 0.14, wallThickness),
          new THREE.MeshStandardMaterial({ color: 0xd9e5f3, roughness: 0.42, metalness: 0.06 })
        )
        doorLintel.position.set(0, wallHeight + 0.02, roomSize / 2 - wallThickness / 2)
        roomGroup.add(doorLintel)

        const windowBand = new THREE.Mesh(
          new THREE.BoxGeometry(roomSize * 0.56, 0.22, 0.02),
          new THREE.MeshStandardMaterial({
            color: 0xd7ebff,
            emissive: 0xbfe4ff,
            emissiveIntensity: 0.12,
            roughness: 0.12,
            metalness: 0.06,
            transparent: true,
            opacity: 0.84
          })
        )
        windowBand.position.set(0, wallHeight * 0.72, -roomSize / 2 + 0.05)
        roomGroup.add(windowBand)

        const roomLight = new THREE.Mesh(
          new THREE.CylinderGeometry(0.36, 0.36, 0.04, 18),
          new THREE.MeshStandardMaterial({
            color: 0xffffff,
            emissive: roomItem.occupiedBeds ? 0xf8f6d8 : 0xe7eef8,
            emissiveIntensity: roomItem.occupiedBeds ? 0.16 : 0.08,
            roughness: 0.22,
            metalness: 0.02
          })
        )
        roomLight.position.set(0, wallHeight + 0.08, 0)
        roomGroup.add(roomLight)

        if (roomItem.beds.some((bed: any) => String(bed.riskLevel || '') === 'HIGH')) {
          const beacon = new THREE.Mesh(
            new THREE.CylinderGeometry(0.08, 0.12, 0.12, 16),
            new THREE.MeshStandardMaterial({
              color: 0xffe1e6,
              emissive: 0xff6d89,
              emissiveIntensity: 0.4,
              roughness: 0.24,
              metalness: 0.02
            })
          )
          beacon.position.set(roomSize / 2 - 0.4, wallHeight + 0.12, -roomSize / 2 + 0.4)
          roomGroup.add(beacon)
        }

        const roomEdges = new THREE.LineSegments(
          new THREE.EdgesGeometry(new THREE.BoxGeometry(roomSize, 0.18, roomSize)),
          new THREE.LineBasicMaterial({ color: roomItem.occupiedBeds >= roomItem.totalBeds ? 0x5fae97 : 0x5c6f69, transparent: true, opacity: 0.24 })
        )
        roomEdges.position.y = 0.09
        roomGroup.add(roomEdges)

        const bedCount = roomItem.beds.length
        if (bedCount) {
          const bedColumns = Math.ceil(Math.sqrt(bedCount))
          const bedWidth = (roomSize - 0.8) / bedColumns
          const bedDepth = Math.min(bedWidth * 1.46, (roomSize - 0.8) / Math.ceil(bedCount / bedColumns))
          roomItem.beds.forEach((bed: any, bedIndex: number) => {
            const bedUnit = createBedUnit(bed, bedWidth * 0.88, bedDepth * 0.8)
            const rowIndex = Math.floor(bedIndex / bedColumns)
            const colIndex = bedIndex % bedColumns
            bedUnit.position.set(
              (colIndex - (bedColumns - 1) / 2) * bedWidth,
              0.22,
              (rowIndex - (Math.ceil(bedCount / bedColumns) - 1) / 2) * bedDepth
            )
            roomGroup.add(bedUnit)
          })
        }

        const roomLabel = createLabel(`${roomItem.roomNo} · ${roomItem.occupiedBeds}/${roomItem.totalBeds}`, 'room-label')
        roomLabel.position.set(0, 1.76, 0)
        roomLabel.visible = false
        roomGroup.add(roomLabel)

        floorGroup.add(roomGroup)
        objectsMap.set(`room_${buildingName}_${floorName}_${roomItem.roomNo}`, roomGroup)
      })

      const floorLabel = createLabel(floorName, 'floor-label')
      floorLabel.position.set(slabWidth / 2 + 1.2, 0.1, 0)
      floorLabel.visible = false
      floorGroup.add(floorLabel)

      buildingGroup.add(floorGroup)
    })

    const hitboxHeight = Math.max(totalFloors * 2.1, 3.8)
    const hitbox = new THREE.Mesh(new THREE.BoxGeometry(12, hitboxHeight, 12), materials.buildingHitbox.clone())
    hitbox.position.y = hitboxHeight / 2 + 1.4
    hitbox.userData = {
      type: 'buildingHitbox',
      name: buildingName,
      ref: buildingGroup,
      totalFloors,
      totalRooms: buildingFloors.reduce((sum, floor) => sum + (props.roomLookup.get(`${buildingName}@@${floor}`)?.length || 0), 0)
    }
    buildingGroup.add(hitbox)
    interactableObjects.push(hitbox)

    const hitboxEdges = new THREE.LineSegments(
      new THREE.EdgesGeometry(new THREE.BoxGeometry(12, hitboxHeight, 12)),
      new THREE.LineBasicMaterial({ color: 0x5fae97, transparent: true, opacity: 0.36 })
    )
    hitboxEdges.position.y = hitboxHeight / 2 + 1.4
    buildingGroup.add(hitboxEdges)

    const buildingLabel = createLabel(buildingName, 'building-label')
    buildingLabel.position.set(0, hitboxHeight + 3.2, 0)
    buildingGroup.add(buildingLabel)

    parkGroup.add(buildingGroup)
    objectsMap.set(`building_${buildingName}`, buildingGroup)
  })

  updateVisibility()
}

function updateVisibility() {
  if (!controls.value) return
  controls.value.autoRotate = currentLevel.value === 'PARK' && cruiseEnabled.value

  objectsMap.forEach((obj, key) => {
    if (!key.startsWith('building_')) return
    const buildingName = obj.userData.name
    const hitbox = obj.children.find((child) => child.userData.type === 'buildingHitbox')
    const buildingLabel = obj.children.find((child) => child instanceof CSS2DObject && child.element.className.includes('building-label'))
    const buildingMass = obj.children.find((child) => child.userData.type === 'buildingMass')

    if (currentLevel.value === 'PARK') {
      obj.visible = true
      if (hitbox) hitbox.visible = true
      if (buildingLabel) buildingLabel.visible = true
      if (buildingMass) buildingMass.visible = true
      obj.children.forEach((child) => {
        if (child.userData.type === 'floor') {
          child.visible = false
          gsap.to(child.position, { y: child.userData.originalY ?? child.position.y, duration: 0.45 })
        }
      })
      return
    }

    if (buildingName !== selectedBuilding.value) {
      obj.visible = false
      return
    }

    obj.visible = true
    if (hitbox) hitbox.visible = false
    if (buildingLabel) buildingLabel.visible = false
    if (buildingMass) buildingMass.visible = false

    obj.children.forEach((child) => {
      if (child.userData.type !== 'floor') return
      child.userData.originalY = child.userData.originalY ?? child.position.y

      if (currentLevel.value === 'BUILDING') {
        child.visible = true
        gsap.to(child.position, { y: child.userData.floorYIndex * 10.2 + 1.4, duration: 0.82, ease: 'power2.out' })
        child.children.forEach((subChild) => {
          if (subChild.userData.type === 'room') {
            subChild.visible = true
            const roomLabel = subChild.children.find((roomChild) => roomChild instanceof CSS2DObject && roomChild.element.className.includes('room-label'))
            if (roomLabel) roomLabel.visible = true
          }
          if (subChild instanceof CSS2DObject && subChild.element.className.includes('floor-label')) subChild.visible = false
        })
        return
      }

      if (child.userData.name === selectedFloor.value) {
        child.visible = true
        gsap.to(child.position, { y: 1.4, duration: 0.82, ease: 'power2.out' })
        child.children.forEach((subChild) => {
          if (subChild.userData.type === 'room') {
            const isCurrentRoom = subChild.userData.room.roomNo === selectedRoom.value
            subChild.visible = currentLevel.value !== 'ROOM' || isCurrentRoom
            const roomLabel = subChild.children.find((roomChild) => roomChild instanceof CSS2DObject && roomChild.element.className.includes('room-label'))
            if (roomLabel) roomLabel.visible = currentLevel.value !== 'ROOM'
          }
          if (subChild instanceof CSS2DObject && subChild.element.className.includes('floor-label')) subChild.visible = currentLevel.value === 'FLOOR'
        })
      } else {
        child.visible = false
      }
    })
  })
}

function setTooltipPosition(event: PointerEvent) {
  if (!containerRef.value) return
  const rect = containerRef.value.getBoundingClientRect()
  const desiredX = event.clientX - rect.left + 18
  const desiredY = event.clientY - rect.top + 18
  tooltip.value.x = Math.min(Math.max(desiredX, 18), rect.width - 250)
  tooltip.value.y = Math.min(Math.max(desiredY, 18), rect.height - 180)
}

function updateTooltip(event: PointerEvent, obj: THREE.Object3D) {
  tooltip.value.visible = true
  setTooltipPosition(event)

  if (obj.userData.type === 'buildingHitbox') {
    tooltip.value.title = obj.userData.name
    tooltip.value.badge = '楼栋'
    tooltip.value.rows = [
      { label: '楼层数', value: `${obj.userData.totalFloors} 层`, tone: 'cyan' },
      { label: '总房间', value: `${obj.userData.totalRooms} 间` }
    ]
    tooltip.value.tip = '点击展开楼栋内部结构'
    focusTitle.value = obj.userData.name
    focusStatus.value = '楼栋级观察'
    return
  }

  if (obj.userData.type === 'floorSlab') {
    tooltip.value.title = `${obj.userData.building} / ${obj.userData.floor}`
    tooltip.value.badge = '楼层'
    tooltip.value.rows = [
      { label: '视图模式', value: '楼层爆炸视图', tone: 'cyan' },
      { label: '下一步', value: '选择房间或床位' }
    ]
    tooltip.value.tip = '点击进入楼层鸟瞰视角'
    focusTitle.value = `${obj.userData.building} ${obj.userData.floor}`
    focusStatus.value = '楼层级观察'
    return
  }

  if (obj.userData.type === 'roomTile') {
    const roomData = obj.userData.data
    tooltip.value.title = `房间 ${roomData.roomNo || '-'}`
    tooltip.value.badge = '房间'
    tooltip.value.rows = [
      { label: '容量', value: `${roomData.totalBeds} 床` },
      { label: '入住', value: `${roomData.occupiedBeds}/${roomData.totalBeds}`, tone: roomData.occupiedBeds >= roomData.totalBeds ? 'cyan' : 'green' }
    ]
    tooltip.value.tip = currentLevel.value === 'ROOM' ? '再次点击打开房间详情' : '点击进入房间视角'
    focusTitle.value = `房间 ${roomData.roomNo || '-'}`
    focusStatus.value = `${roomData.occupiedBeds}/${roomData.totalBeds} 床位占用`
    return
  }

  if (obj.userData.type === 'bed') {
    const bed = obj.userData.bed
    tooltip.value.title = `床位 ${bed.bedNo || '-'}`
    tooltip.value.badge = bed.elderId ? '守护中' : '空床'
    tooltip.value.rows = [
      { label: '状态', value: bed.elderId ? (bed.elderName || '在住长者') : '空床待命' },
      { label: '风险', value: bed.riskLabel || '守护稳定', tone: bed.riskLevel === 'HIGH' ? 'red' : bed.riskLevel === 'MEDIUM' ? 'purple' : bed.riskLevel === 'LOW' ? 'blue' : 'cyan' },
      { label: '24h异常', value: `${bed.abnormalVital24hCount || 0} 次` }
    ]
    tooltip.value.tip = '点击查看业务详情'
    focusTitle.value = `${bed.roomNo || '-'} / ${bed.bedNo || '-'}`
    focusStatus.value = bed.riskLabel || (bed.elderId ? '长者守护中' : '空床待命')
    return
  }

  tooltip.value.visible = false
}

function restoreHoverObject(obj: THREE.Object3D) {
  if (obj.userData.type === 'buildingHitbox' || obj.userData.type === 'floorSlab') {
    ;(obj as THREE.Mesh).material = obj.userData.originalMat || materials.buildingHitbox
  }
  if (obj.userData.type === 'roomTile') {
    ;(obj as THREE.Mesh).material = obj.userData.originalMat || materials.roomNeutral
  }
  if (obj.userData.type === 'bed') {
    ;(obj as THREE.Mesh).material = obj.userData.originalMat
    ;(obj as THREE.Mesh).scale.set(1, 1, 1)
    if (obj.userData.halo) {
      const haloMaterial = (obj.userData.halo as THREE.Mesh).material as THREE.MeshBasicMaterial
      haloMaterial.opacity = obj.userData.state === 'alert' ? 0.24 : 0.12
    }
  }
}

function onPointerMove(event: PointerEvent) {
  if (!canvasRef.value || !camera.value) return
  const now = performance.now()
  if (now - lastPointerMoveAt < 32) return
  lastPointerMoveAt = now

  const rect = canvasRef.value.getBoundingClientRect()
  pickPosition.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  pickPosition.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(pickPosition, camera.value)
  const intersects = raycaster.intersectObjects(interactableObjects, false).filter((hit) => hit.object.visible)

  if (hoveredObj && hoveredObj !== intersects[0]?.object) {
    restoreHoverObject(hoveredObj)
    hoveredObj = null
  }

  if (!intersects.length) {
    tooltip.value.visible = false
    if (renderer.value) renderer.value.domElement.style.cursor = 'default'
    return
  }

  const obj = intersects[0].object
  updateTooltip(event, obj)

  if (obj !== hoveredObj) {
    if (obj.userData.type === 'buildingHitbox' || obj.userData.type === 'floorSlab') {
      obj.userData.originalMat = (obj as THREE.Mesh).material
      ;(obj as THREE.Mesh).material = materials.hover
    }
    if (obj.userData.type === 'roomTile') {
      ;(obj as THREE.Mesh).material = materials.roomHover
    }
    if (obj.userData.type === 'bed') {
      ;(obj as THREE.Mesh).scale.set(1.06, 1.06, 1.06)
      if (obj.userData.halo) {
        const haloMaterial = (obj.userData.halo as THREE.Mesh).material as THREE.MeshBasicMaterial
        haloMaterial.opacity = 0.3
      }
    }
    hoveredObj = obj
  }

  renderer.value!.domElement.style.cursor = 'pointer'
}

function onPointerDown(event: PointerEvent) {
  pointerDownPos = { x: event.clientX, y: event.clientY }
}

function onClick(event: MouseEvent) {
  // 拖拽旋转视角结束时也会触发 click，位移超过阈值则忽略，避免误返回上级
  if (pointerDownPos) {
    const moved = Math.abs(event.clientX - pointerDownPos.x) + Math.abs(event.clientY - pointerDownPos.y)
    pointerDownPos = null
    if (moved > 6) return
  }
  if (!hoveredObj) {
    goUpLevel()
    return
  }

  if (hoveredObj.userData.type === 'buildingHitbox') {
    applyScope({
      level: 'BUILDING',
      building: hoveredObj.userData.name,
      floor: '',
      room: ''
    }, { emit: true })
    return
  }

  if (hoveredObj.userData.type === 'floorSlab') {
    applyScope({
      level: 'FLOOR',
      building: hoveredObj.userData.building,
      floor: hoveredObj.userData.floor,
      room: ''
    }, { emit: true })
    return
  }

  if (hoveredObj.userData.type === 'roomTile') {
    const roomNo = hoveredObj.userData.ref.userData.room.roomNo
    if (currentLevel.value === 'ROOM') {
      emit('click-room', hoveredObj.userData.data)
      return
    }
    applyScope({
      level: 'ROOM',
      building: selectedBuilding.value,
      floor: selectedFloor.value,
      room: roomNo
    }, { emit: true })
    return
  }

  if (hoveredObj.userData.type === 'bed') {
    emit('click-bed', hoveredObj.userData.bed)
  }
}

function zoomToObject(obj: THREE.Object3D, topDown = false, overrideDistance?: number) {
  if (!camera.value || !controls.value) return
  const box = new THREE.Box3().setFromObject(obj)
  const center = box.getCenter(new THREE.Vector3())
  const size = box.getSize(new THREE.Vector3())
  const maxDim = Math.max(size.x, size.y, size.z)
  const fov = camera.value.fov * (Math.PI / 180)
  const cameraDistance = overrideDistance || Math.abs(maxDim / 2 / Math.tan(fov / 2)) * 2
  const offsetX = topDown ? 0 : cameraDistance * 0.46
  const offsetZ = topDown ? cameraDistance * 0.18 : cameraDistance

  gsap.to(camera.value.position, {
    x: center.x + offsetX,
    y: topDown ? center.y + cameraDistance * 0.92 : center.y + size.y / 2 + 10,
    z: center.z + offsetZ,
    duration: 1.18,
    ease: 'power3.inOut'
  })
  gsap.to(controls.value.target, {
    x: center.x,
    y: center.y,
    z: center.z,
    duration: 1.18,
    ease: 'power3.inOut'
  })
}

function focusScope(scope: PanoramaScope) {
  if (!camera.value || !controls.value) return
  if (scope.level === 'PARK') {
    gsap.to(camera.value.position, { x: 50, y: 38, z: 62, duration: 1.1, ease: 'power3.out' })
    gsap.to(controls.value.target, { x: 0, y: 6, z: 0, duration: 1.1, ease: 'power3.out' })
    focusTitle.value = '园区总览'
    focusStatus.value = '等待交互'
    return
  }
  if (scope.level === 'BUILDING') {
    const buildingGroup = objectsMap.get(`building_${scope.building}`)
    if (buildingGroup) zoomToObject(buildingGroup, false, 20)
    return
  }
  if (scope.level === 'FLOOR') {
    const floorGroup = objectsMap.get(`floor_${scope.building}_${scope.floor}`)
    if (floorGroup) zoomToObject(floorGroup, true, 18)
    return
  }
  const roomGroup = objectsMap.get(`room_${scope.building}_${scope.floor}_${scope.room}`)
  if (roomGroup) zoomToObject(roomGroup, false, 16)
}

function goUpLevel() {
  if (currentLevel.value === 'ROOM') {
    applyScope({
      level: 'FLOOR',
      building: selectedBuilding.value,
      floor: selectedFloor.value,
      room: ''
    }, { emit: true })
    return
  }

  if (currentLevel.value === 'FLOOR') {
    applyScope({
      level: 'BUILDING',
      building: selectedBuilding.value,
      floor: '',
      room: ''
    }, { emit: true })
    return
  }

  if (currentLevel.value === 'BUILDING') {
    applyScope({ level: 'PARK', building: '', floor: '', room: '' }, { emit: true })
  }
}

function resetCamera() {
  cruiseEnabled.value = true
  if (selectedBuilding.value && selectedFloor.value) {
    applyScope({ level: 'FLOOR', building: selectedBuilding.value, floor: selectedFloor.value, room: '' }, { emit: true })
    return
  }
  if (selectedBuilding.value) {
    applyScope({ level: 'BUILDING', building: selectedBuilding.value, floor: '', room: '' }, { emit: true })
    return
  }
  applyScope({ level: 'PARK', building: '', floor: '', room: '' }, { emit: true })
}

function toggleCruise() {
  cruiseEnabled.value = !cruiseEnabled.value
  updateVisibility()
}

async function toggleFullscreen() {
  if (!containerRef.value) return
  if (!document.fullscreenElement) {
    await containerRef.value.requestFullscreen?.().catch(() => {})
    fullscreenActive.value = Boolean(document.fullscreenElement)
    return
  }
  await document.exitFullscreen?.().catch(() => {})
  fullscreenActive.value = Boolean(document.fullscreenElement)
}

function onWindowResize() {
  if (!canvasRef.value || !camera.value || !renderer.value || !cssRenderer.value) return
  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight
  camera.value.aspect = width / height
  camera.value.updateProjectionMatrix()
  renderer.value.setSize(width, height)
  cssRenderer.value.setSize(width, height)
}

function scheduleBuildSceneData() {
  if (rebuildSceneTimer) window.clearTimeout(rebuildSceneTimer)
  rebuildSceneTimer = window.setTimeout(() => {
    rebuildSceneTimer = 0
    buildSceneData()
    focusScope(currentScope.value)
  }, 80)
}

function handleVisibilityChange() {
  sceneRenderingEnabled.value = !document.hidden
  if (sceneRenderingEnabled.value && !animationFrameId) animate()
}

function syncFullscreenState() {
  fullscreenActive.value = Boolean(document.fullscreenElement)
}

function animate() {
  animationFrameId = requestAnimationFrame(animate)
  if (!sceneRenderingEnabled.value) return
  const time = Date.now() * 0.0011
  controls.value?.update()

  animatedBeds.forEach((item) => {
    const material = item.mesh.material as THREE.MeshStandardMaterial
    if (item.type === 'pulse') {
      // 异常床位：缓慢呼吸式脉动，醒目但不刺眼
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 3.4) + 1) * 0.13
      if (item.mesh.userData.halo) {
        const haloMaterial = (item.mesh.userData.halo as THREE.Mesh).material as THREE.MeshBasicMaterial
        haloMaterial.opacity = 0.18 + (Math.sin(time * 3.4) + 1) * 0.07
      }
    } else if (item.type === 'blink') {
      // 维护/外出：柔和渐变代替生硬闪烁
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 2.2) + 1) * 0.08
    } else {
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 2.2) + 1) * 0.05
    }
  })

  renderer.value?.render(scene.value, camera.value!)
  cssRenderer.value?.render(scene.value, camera.value!)
}

watch(() => props.buildings, scheduleBuildSceneData, { deep: true })
watch(() => props.floors, scheduleBuildSceneData, { deep: true })
watch(() => props.roomLookup, scheduleBuildSceneData, { deep: true })
watch(
  () => props.scope,
  (scope) => {
    if (!scope) return
    const changed = [
      scope.level !== currentLevel.value,
      scope.building !== selectedBuilding.value,
      scope.floor !== selectedFloor.value,
      scope.room !== selectedRoom.value
    ].some(Boolean)
    if (!changed) return
    applyScope(scope, { emit: false })
  },
  { deep: true }
)

onMounted(() => {
  sceneRenderingEnabled.value = !document.hidden
  initScene()
  if (props.scope) {
    applyScope(props.scope, { emit: false })
  } else {
    focusScope(currentScope.value)
  }
  document.addEventListener('visibilitychange', handleVisibilityChange)
  document.addEventListener('fullscreenchange', syncFullscreenState)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
  animationFrameId = 0
  if (rebuildSceneTimer) {
    window.clearTimeout(rebuildSceneTimer)
    rebuildSceneTimer = 0
  }
  renderer.value?.domElement.removeEventListener('pointermove', onPointerMove)
  renderer.value?.domElement.removeEventListener('pointerdown', onPointerDown)
  renderer.value?.domElement.removeEventListener('click', onClick)
  window.removeEventListener('resize', onWindowResize)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.removeEventListener('fullscreenchange', syncFullscreenState)
  controls.value?.dispose()
  renderer.value?.dispose()
})
</script>

<style scoped>
.panorama-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  border-radius: 26px;
  background:
    radial-gradient(circle at 12% 12%, rgba(46, 138, 114, 0.1), transparent 28%),
    radial-gradient(circle at 82% 0%, rgba(229, 138, 58, 0.08), transparent 30%),
    linear-gradient(180deg, #f9faf5 0%, #eff3ea 100%);
  box-shadow:
    inset 0 0 90px rgba(46, 138, 114, 0.05),
    inset 0 0 30px rgba(229, 138, 58, 0.03);
}

.canvas-wrapper {
  width: 100%;
  height: 100%;
}

.scene-atmosphere {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.02), rgba(231, 238, 227, 0.12) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(232, 239, 229, 0.12));
}

.scene-toolbar,
.scene-hud {
  position: absolute;
  left: 50%;
  bottom: 18px;
  z-index: 3;
  transform: translateX(-50%);
}

.scene-toolbar {
  top: 16px;
}

.scene-hud {
  width: auto;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-center,
.scene-legend {
  padding: 12px 16px;
  border-radius: 18px;
  border: 1px solid rgba(187, 217, 207, 0.5);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 28px rgba(34, 51, 46, 0.1);
}

.toolbar-center {
  display: grid;
  gap: 4px;
  min-width: 320px;
  text-align: center;
}

.toolbar-center strong {
  color: var(--ink);
}

.toolbar-center small,
.toolbar-eyebrow {
  color: var(--muted-2);
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.tech-btn {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(187, 217, 207, 0.55);
  color: var(--ink-soft);
  border-radius: 14px;
}

.tech-btn:hover {
  border-color: rgba(var(--primary-rgb), 0.4);
  color: var(--primary-strong);
  box-shadow: 0 10px 24px rgba(var(--primary-rgb), 0.14);
}

.scene-legend {
  display: flex;
  align-items: center;
  gap: 12px;
}

.legend-chip {
  padding: 0;
  border-radius: 999px;
  border: 0;
  background: transparent;
  font-size: 12px;
  color: var(--muted);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.legend-chip::before {
  content: '';
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.status-occupied::before { background: #2e8a72; }
.status-empty::before { background: #c7cdc6; }
.status-warning::before { background: #de9b3d; }
.status-ai::before { background: #6b79d8; }
.status-alert::before { background: #c9504b; }

.tech-tooltip {
  position: absolute;
  z-index: 8;
  width: 230px;
  border-radius: 18px;
  border: 1px solid rgba(187, 217, 207, 0.55);
  background: rgba(255, 255, 255, 0.96);
  color: var(--ink-soft);
  backdrop-filter: blur(10px);
  box-shadow: 0 16px 28px rgba(34, 51, 46, 0.12);
  padding: 14px;
  pointer-events: none;
}

.tt-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(187, 217, 207, 0.4);
  margin-bottom: 8px;
}

.tt-header strong {
  font-size: 14px;
}

.tt-header small {
  font-size: 11px;
  color: var(--muted-2);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.tt-body {
  display: grid;
  gap: 6px;
}

.tt-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
}

.tt-row span {
  color: var(--muted-2);
}

.tt-row strong {
  color: var(--ink);
}

.tt-row strong.cyan { color: var(--primary); }
.tt-row strong.green { color: var(--success); }
.tt-row strong.blue { color: var(--info); }
.tt-row strong.purple { color: #6b79d8; }
.tt-row strong.red { color: var(--danger); }

.tt-tip {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(187, 217, 207, 0.55);
  color: var(--muted-2);
  font-size: 12px;
}

:global(.scene-label) {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(187, 217, 207, 0.55);
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink-soft);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 26px rgba(34, 51, 46, 0.1);
  white-space: nowrap;
}

@media (max-width: 960px) {
  .scene-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-center {
    min-width: 0;
  }

  .scene-hud {
    position: static;
    transform: none;
    padding: 0 18px 18px;
  }

  .scene-legend {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
