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
        <div class="legend-chip status-empty">空床</div>
        <div class="legend-chip status-occupied">有人</div>
        <div class="legend-chip status-nursing">护理中</div>
        <div class="legend-chip status-cleaning">清洁中</div>
        <div class="legend-chip status-reserved">待入住</div>
        <div class="legend-chip status-alert">告警</div>
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

// 床态语义配色（数字孪生暗色主题，对应看板六态）：
// 空床=薄荷绿 / 有人=天蓝 / 护理中=青蓝 / 清洁中=琥珀 / 待入住=紫 / 告警=红
type BedStatusKey = 'empty' | 'occupied' | 'nursing' | 'cleaning' | 'reserved' | 'alert'

const STATUS_META: Record<BedStatusKey, { text: string; hex: string; glow: number; base: number }> = {
  empty: { text: '空床', hex: '#34d39a', glow: 0x34d39a, base: 0x123a30 },
  occupied: { text: '有人', hex: '#4c9dff', glow: 0x4c9dff, base: 0x13294d },
  nursing: { text: '护理中', hex: '#26c6e0', glow: 0x26c6e0, base: 0x123a45 },
  cleaning: { text: '清洁中', hex: '#f2a13d', glow: 0xf2a13d, base: 0x3a2d13 },
  reserved: { text: '待入住', hex: '#a97bf5', glow: 0xa97bf5, base: 0x2a2450 },
  alert: { text: '告警', hex: '#ff5d6c', glow: 0xff5d6c, base: 0x431922 }
}

// 单床状态归一：告警 > 清洁/维护 > 待入住 > 护理 > 有人 > 空床
function bedStatusKey(bed: any): BedStatusKey {
  const abnormal = Number(bed?.abnormalVital24hCount || 0)
  const source = String(bed?.occupancySource || '')
  if (bed?.riskLevel === 'HIGH' || abnormal > 0) return 'alert'
  if (bed?.status === 0 || source === 'MAINTENANCE' || source === 'CLEANING' || source === 'FROZEN') return 'cleaning'
  if (!bed?.elderId && source === 'RESERVATION') return 'reserved'
  if (bed?.elderId) {
    if (bed?.riskLevel === 'MEDIUM' || bed?.riskSource) return 'nursing'
    return 'occupied'
  }
  return 'empty'
}

const STATUS_PRIORITY: BedStatusKey[] = ['alert', 'cleaning', 'reserved', 'nursing', 'occupied', 'empty']

// 房间状态取其床位中优先级最高者
function roomStatusKey(room: RoomScene): BedStatusKey {
  if (!room.beds?.length) return 'empty'
  const keys = new Set(room.beds.map((bed) => bedStatusKey(bed)))
  return STATUS_PRIORITY.find((key) => keys.has(key)) || 'empty'
}

function makeBedMaterial(key: BedStatusKey) {
  const meta = STATUS_META[key]
  return new THREE.MeshStandardMaterial({
    color: meta.glow,
    emissive: meta.glow,
    emissiveIntensity: key === 'alert' ? 0.55 : key === 'empty' ? 0.16 : 0.36,
    metalness: 0.2,
    roughness: 0.34
  })
}

const materials = {
  buildingShell: new THREE.MeshStandardMaterial({ color: 0x16233d, transparent: true, opacity: 0.96, roughness: 0.5, metalness: 0.3 }),
  buildingCap: new THREE.MeshStandardMaterial({ color: 0x1f3355, transparent: true, opacity: 1, roughness: 0.42, metalness: 0.4 }),
  buildingHitbox: new THREE.MeshBasicMaterial({ color: 0x4c9dff, transparent: true, opacity: 0.01, side: THREE.DoubleSide }),
  floor: new THREE.MeshStandardMaterial({ color: 0x0f1c33, transparent: true, opacity: 0.92, roughness: 0.5, metalness: 0.32 }),
  roomNeutral: new THREE.MeshStandardMaterial({ color: 0x14243f, transparent: true, opacity: 0.94, roughness: 0.46, metalness: 0.3 }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0x1d3358, emissive: 0x2c62b0, emissiveIntensity: 0.35, transparent: true, opacity: 0.96 }),
  hover: new THREE.MeshStandardMaterial({ color: 0x1d3358, emissive: 0x2c62b0, emissiveIntensity: 0.4, transparent: true, opacity: 0.96 })
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

const STATUS_ICON: Record<BedStatusKey, string> = {
  empty: '🛏',
  occupied: '👤',
  nursing: '➕',
  cleaning: '🧹',
  reserved: '🕘',
  alert: '⚠'
}

// 悬浮床态标签（对应看板图上的状态胶囊）：房间号 + 状态文案 + 状态色
function createRoomPill(room: RoomScene, key: BedStatusKey) {
  const meta = STATUS_META[key]
  const div = document.createElement('div')
  div.className = `room-pill status-${key}`
  div.style.setProperty('--pill', meta.hex)
  const icon = document.createElement('span')
  icon.className = 'room-pill__icon'
  icon.textContent = STATUS_ICON[key]
  const body = document.createElement('div')
  body.className = 'room-pill__body'
  const no = document.createElement('strong')
  no.textContent = room.roomNo
  const status = document.createElement('small')
  status.textContent = meta.text
  body.appendChild(no)
  body.appendChild(status)
  div.appendChild(icon)
  div.appendChild(body)
  return new CSS2DObject(div)
}

function materialForStatus(key: BedStatusKey) {
  return makeBedMaterial(key)
}

// 房间地面按状态泛出对应色调的辉光，暗色底上一眼可辨
function roomMaterialForData(room: RoomScene) {
  const key = roomStatusKey(room)
  const meta = STATUS_META[key]
  const material = materials.roomNeutral.clone()
  material.color = new THREE.Color(meta.base)
  material.emissive = new THREE.Color(meta.glow)
  material.emissiveIntensity = key === 'empty' ? 0.05 : key === 'alert' ? 0.22 : 0.12
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

  const grooveMaterial = new THREE.MeshStandardMaterial({ color: 0x0e1b30, roughness: 0.6, metalness: 0.3 })
  const windowMaterial = new THREE.MeshStandardMaterial({
    color: 0x1a3a63,
    emissive: 0x4c9dff,
    emissiveIntensity: 0.5,
    roughness: 0.25,
    metalness: 0.2
  })
  const frameMaterial = new THREE.MeshStandardMaterial({ color: 0x24395c, roughness: 0.5, metalness: 0.3 })

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
      const winMat = lit ? windowMaterial : new THREE.MeshStandardMaterial({ color: 0x142743, roughness: 0.3, metalness: 0.2 })
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
    new THREE.MeshStandardMaterial({ color: 0x1c2f4d, roughness: 0.55, metalness: 0.3 })
  )
  penthouse.position.set(-width * 0.2, bodyHeight + 0.34 + 0.36, -depth * 0.18)
  penthouse.castShadow = true
  group.add(penthouse)

  const roofGarden = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.4, 0.24, depth * 0.42, 2, 0.12),
    new THREE.MeshStandardMaterial({ color: 0x1f7a63, emissive: 0x2ea884, emissiveIntensity: 0.25, roughness: 0.7, metalness: 0.1 })
  )
  roofGarden.position.set(width * 0.2, bodyHeight + 0.34 + 0.12, depth * 0.16)
  group.add(roofGarden)

  return group
}

function createBedUnit(bed: any, width: number, depth: number) {
  const state = bedStatusKey(bed)
  const meta = STATUS_META[state]
  const group = new THREE.Group()
  const frameHeight = 0.18

  const frameMaterial = new THREE.MeshStandardMaterial({ color: 0x2a4066, metalness: 0.4, roughness: 0.4 })
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
        new THREE.MeshStandardMaterial({ color: 0x243758, metalness: 0.4, roughness: 0.4 })
      )
      leg.position.set(xDirection * (width / 2 - 0.12), 0.12, zDirection * (depth / 2 - 0.12))
      group.add(leg)
    })
  })

  const mattressMaterial = materialForStatus(state)
  const mattress = new THREE.Mesh(new RoundedBoxGeometry(width * 0.8, 0.14, depth * 0.68, 2, 0.05), mattressMaterial)
  mattress.position.y = frameHeight + 0.07
  mattress.castShadow = true
  mattress.userData = { type: 'bed', bed, originalMat: mattressMaterial, state }
  group.add(mattress)
  interactableObjects.push(mattress)

  const pillow = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.42, 0.09, depth * 0.16, 2, 0.04),
    new THREE.MeshStandardMaterial({ color: 0xdce8f7, roughness: 0.6, metalness: 0.04 })
  )
  pillow.position.set(0, frameHeight + 0.16, -depth * 0.2)
  group.add(pillow)

  const blanket = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.76, 0.08, depth * 0.34, 2, 0.04),
    new THREE.MeshStandardMaterial({
      color: meta.glow,
      emissive: meta.glow,
      emissiveIntensity: state === 'empty' ? 0.04 : 0.14,
      roughness: 0.6,
      metalness: 0.05
    })
  )
  blanket.position.set(0, frameHeight + 0.14, depth * 0.08)
  group.add(blanket)

  const headboard = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.84, 0.34, 0.1, 2, 0.05),
    new THREE.MeshStandardMaterial({ color: 0x2b4168, metalness: 0.3, roughness: 0.42 })
  )
  headboard.position.set(0, 0.3, -depth * 0.34)
  headboard.castShadow = true
  group.add(headboard)

  const tailboard = new THREE.Mesh(
    new RoundedBoxGeometry(width * 0.72, 0.2, 0.08, 2, 0.04),
    new THREE.MeshStandardMaterial({ color: 0x243a5e, metalness: 0.3, roughness: 0.44 })
  )
  tailboard.position.set(0, 0.2, depth * 0.33)
  group.add(tailboard)

  // 床头小柜 + 监护屏：只在有长者的床位出现，屏色跟随状态
  if (bed.elderId) {
    const bedside = new THREE.Group()
    const cabinet = new THREE.Mesh(
      new RoundedBoxGeometry(0.22, 0.26, 0.22, 2, 0.04),
      new THREE.MeshStandardMaterial({ color: 0x223350, roughness: 0.5, metalness: 0.2 })
    )
    cabinet.position.y = 0.13
    cabinet.castShadow = true
    bedside.add(cabinet)
    const monitorScreen = new THREE.Mesh(
      new RoundedBoxGeometry(0.16, 0.1, 0.03, 2, 0.02),
      new THREE.MeshStandardMaterial({
        color: 0x0b1626,
        emissive: meta.glow,
        emissiveIntensity: state === 'alert' ? 0.5 : 0.28,
        roughness: 0.34,
        metalness: 0.2
      })
    )
    monitorScreen.position.set(0, 0.32, 0)
    monitorScreen.rotation.x = -0.28
    bedside.add(monitorScreen)
    bedside.position.set(width / 2 + 0.16, 0, -depth * 0.2)
    group.add(bedside)
  }

  // 地面光环：告警强、其余弱，空床不加光环
  if (state !== 'empty') {
    const halo = new THREE.Mesh(
      new THREE.RingGeometry(Math.min(width, depth) * 0.38, Math.min(width, depth) * 0.52, 32),
      new THREE.MeshBasicMaterial({ color: meta.glow, transparent: true, opacity: state === 'alert' ? 0.34 : 0.16, side: THREE.DoubleSide })
    )
    halo.rotation.x = -Math.PI / 2
    halo.position.y = 0.03
    halo.userData.isHalo = true
    group.add(halo)
    mattress.userData.halo = halo
  }

  // 告警脉动、清洁/护理柔和呼吸，其余静止
  if (state === 'alert') animatedBeds.push({ mesh: mattress, type: 'pulse', baseIntensity: 0.55 })
  else if (state === 'cleaning' || state === 'nursing' || state === 'reserved') animatedBeds.push({ mesh: mattress, type: 'blink', baseIntensity: 0.34 })

  return group
}

function buildAtmosphere() {
  if (!scene.value) return

  // 深色基座平台：数字孪生底盘
  const campusBase = new THREE.Mesh(
    new THREE.CylinderGeometry(78, 84, 1.4, 64),
    new THREE.MeshStandardMaterial({ color: 0x0a1424, roughness: 0.7, metalness: 0.28 })
  )
  campusBase.position.y = -0.7
  campusBase.receiveShadow = true
  campusBase.userData.isGenerated = true
  scene.value.add(campusBase)

  // 内圈发光地台，营造舞台聚光
  const stageDisc = new THREE.Mesh(
    new THREE.CircleGeometry(40, 96),
    new THREE.MeshStandardMaterial({ color: 0x11213c, emissive: 0x18365f, emissiveIntensity: 0.3, roughness: 0.55, metalness: 0.2, transparent: true, opacity: 0.95 })
  )
  stageDisc.rotation.x = -Math.PI / 2
  stageDisc.position.y = 0.01
  stageDisc.receiveShadow = true
  stageDisc.userData.isGenerated = true
  scene.value.add(stageDisc)

  // 霓虹网格地面
  const gridHelper = new THREE.GridHelper(200, 40, 0x3d6fb0, 0x1c3355)
  gridHelper.position.y = 0.02
  gridHelper.material.transparent = true
  ;(gridHelper.material as THREE.Material).opacity = 0.4
  gridHelper.userData.isGenerated = true
  scene.value.add(gridHelper)

  // 发光科技环
  const pathRing = new THREE.Mesh(
    new THREE.RingGeometry(27.5, 29.2, 128),
    new THREE.MeshBasicMaterial({ color: 0x4c9dff, transparent: true, opacity: 0.5, side: THREE.DoubleSide })
  )
  pathRing.rotation.x = -Math.PI / 2
  pathRing.position.y = 0.04
  pathRing.userData.isGenerated = true
  scene.value.add(pathRing)

  const outerRing = new THREE.Mesh(
    new THREE.RingGeometry(38.5, 39.4, 128),
    new THREE.MeshBasicMaterial({ color: 0x26c6e0, transparent: true, opacity: 0.28, side: THREE.DoubleSide })
  )
  outerRing.rotation.x = -Math.PI / 2
  outerRing.position.y = 0.04
  outerRing.userData.isGenerated = true
  scene.value.add(outerRing)

  particles = null
}

function initScene() {
  if (!canvasRef.value) return

  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight

  scene.value = new THREE.Scene()
  scene.value.background = new THREE.Color(0x070f1d)
  scene.value.fog = new THREE.FogExp2(0x070f1d, 0.0026)

  camera.value = new THREE.PerspectiveCamera(40, width / height, 0.1, 1600)
  camera.value.position.set(50, 38, 62)

  renderer.value = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.value.setSize(width, height)
  renderer.value.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.value.setClearColor(0x070f1d, 1)
  renderer.value.outputColorSpace = THREE.SRGBColorSpace
  renderer.value.toneMapping = THREE.ACESFilmicToneMapping
  renderer.value.toneMappingExposure = 1.05
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

  const ambientLight = new THREE.AmbientLight(0x6f93c8, 0.65)
  scene.value.add(ambientLight)

  // 天空-地面半球光：冷蓝天光 + 深底反光
  const hemiLight = new THREE.HemisphereLight(0x9fc4ff, 0x0a1424, 0.5)
  scene.value.add(hemiLight)

  const topLight = new THREE.DirectionalLight(0xdcecff, 1.0)
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

  const fillLight = new THREE.DirectionalLight(0x3f6bb0, 0.5)
  fillLight.position.set(-56, 28, -46)
  scene.value.add(fillLight)

  const rimLight = new THREE.PointLight(0x4c9dff, 1.1, 260, 2)
  rimLight.position.set(0, 20, -20)
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
        new THREE.MeshStandardMaterial({ color: 0x0f2038, roughness: 0.6, metalness: 0.25, emissive: 0x18365f, emissiveIntensity: 0.18 })
      )
      corridor.position.y = 0.21
      floorGroup.add(corridor)

      const nurseStation = new THREE.Group()
      nurseStation.position.set(0, 0.32, 0)
      const stationBase = new THREE.Mesh(
        new THREE.CylinderGeometry(1.3, 1.45, 0.42, 28),
        new THREE.MeshStandardMaterial({ color: 0x16294a, roughness: 0.4, metalness: 0.3, emissive: 0x2c62b0, emissiveIntensity: 0.25 })
      )
      const stationTop = new THREE.Mesh(
        new THREE.CylinderGeometry(1.05, 1.15, 0.16, 28),
        new THREE.MeshStandardMaterial({ color: 0x1f7a63, emissive: 0x2ea884, emissiveIntensity: 0.4, roughness: 0.3, metalness: 0.2 })
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
        new THREE.LineBasicMaterial({ color: 0x4c9dff, transparent: true, opacity: 0.4 })
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

        const roomStatus = roomStatusKey(roomItem)
        const roomStatusMeta = STATUS_META[roomStatus]

        const roomCarpet = new THREE.Mesh(
          new THREE.BoxGeometry(roomSize - 0.42, 0.02, roomSize - 0.42),
          new THREE.MeshStandardMaterial({
            color: roomStatusMeta.base,
            emissive: roomStatusMeta.glow,
            emissiveIntensity: roomStatus === 'empty' ? 0.03 : 0.1,
            roughness: 0.7,
            metalness: 0.15
          })
        )
        roomCarpet.position.y = 0.2
        roomGroup.add(roomCarpet)

        const wallMaterial = new THREE.MeshStandardMaterial({ color: 0x1a2c49, roughness: 0.5, metalness: 0.3, transparent: true, opacity: 0.72 })
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
          new THREE.MeshStandardMaterial({ color: 0x24395c, emissive: roomStatusMeta.glow, emissiveIntensity: 0.35, roughness: 0.4, metalness: 0.3 })
        )
        doorLintel.position.set(0, wallHeight + 0.02, roomSize / 2 - wallThickness / 2)
        roomGroup.add(doorLintel)

        const windowBand = new THREE.Mesh(
          new THREE.BoxGeometry(roomSize * 0.56, 0.22, 0.02),
          new THREE.MeshStandardMaterial({
            color: 0x1a3a63,
            emissive: 0x6fb6ff,
            emissiveIntensity: 0.35,
            roughness: 0.12,
            metalness: 0.1,
            transparent: true,
            opacity: 0.9
          })
        )
        windowBand.position.set(0, wallHeight * 0.72, -roomSize / 2 + 0.05)
        roomGroup.add(windowBand)

        const roomLight = new THREE.Mesh(
          new THREE.CylinderGeometry(0.36, 0.36, 0.04, 18),
          new THREE.MeshStandardMaterial({
            color: 0x0b1626,
            emissive: roomStatusMeta.glow,
            emissiveIntensity: roomStatus === 'empty' ? 0.12 : 0.3,
            roughness: 0.22,
            metalness: 0.1
          })
        )
        roomLight.position.set(0, wallHeight + 0.08, 0)
        roomGroup.add(roomLight)

        const roomEdges = new THREE.LineSegments(
          new THREE.EdgesGeometry(new THREE.BoxGeometry(roomSize, 0.18, roomSize)),
          new THREE.LineBasicMaterial({ color: roomStatusMeta.glow, transparent: true, opacity: 0.45 })
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

        const roomLabel = createRoomPill(roomItem, roomStatus)
        roomLabel.position.set(0, 1.9, 0)
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
            const roomLabel = subChild.children.find((roomChild) => roomChild instanceof CSS2DObject && roomChild.element.className.includes('room-pill'))
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
            const roomLabel = subChild.children.find((roomChild) => roomChild instanceof CSS2DObject && roomChild.element.className.includes('room-pill'))
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
      haloMaterial.opacity = obj.userData.state === 'alert' ? 0.34 : 0.16
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
    radial-gradient(circle at 16% 8%, rgba(76, 157, 255, 0.16), transparent 34%),
    radial-gradient(circle at 84% 4%, rgba(38, 198, 224, 0.12), transparent 32%),
    linear-gradient(180deg, #0b1526 0%, #060d1a 100%);
  box-shadow:
    inset 0 0 120px rgba(6, 13, 26, 0.6),
    inset 0 0 40px rgba(76, 157, 255, 0.06);
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
    radial-gradient(circle at 50% 120%, rgba(76, 157, 255, 0.12), transparent 55%),
    linear-gradient(180deg, rgba(8, 16, 30, 0), rgba(4, 9, 18, 0.55) 100%);
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
  border: 1px solid rgba(90, 140, 210, 0.3);
  background: rgba(14, 26, 46, 0.78);
  backdrop-filter: blur(12px);
  box-shadow: 0 14px 34px rgba(2, 8, 20, 0.5);
}

.toolbar-center {
  display: grid;
  gap: 4px;
  min-width: 320px;
  text-align: center;
}

.toolbar-center strong {
  color: #eaf2ff;
}

.toolbar-center small,
.toolbar-eyebrow {
  color: #8ba2c8;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.tech-btn {
  background: rgba(24, 42, 70, 0.85);
  border: 1px solid rgba(90, 140, 210, 0.35);
  color: #cfe0f7;
  border-radius: 14px;
}

.tech-btn:hover {
  border-color: rgba(76, 157, 255, 0.6);
  color: #eaf2ff;
  box-shadow: 0 10px 24px rgba(76, 157, 255, 0.24);
}

.scene-legend {
  display: flex;
  align-items: center;
  gap: 14px;
}

.legend-chip {
  padding: 0;
  border-radius: 999px;
  border: 0;
  background: transparent;
  font-size: 12px;
  color: #c3d4ee;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.legend-chip::before {
  content: '';
  width: 10px;
  height: 10px;
  border-radius: 999px;
  box-shadow: 0 0 8px currentColor;
}

.status-empty::before { background: #34d39a; color: #34d39a; }
.status-occupied::before { background: #4c9dff; color: #4c9dff; }
.status-nursing::before { background: #26c6e0; color: #26c6e0; }
.status-cleaning::before { background: #f2a13d; color: #f2a13d; }
.status-reserved::before { background: #a97bf5; color: #a97bf5; }
.status-alert::before { background: #ff5d6c; color: #ff5d6c; }

.tech-tooltip {
  position: absolute;
  z-index: 8;
  width: 230px;
  border-radius: 18px;
  border: 1px solid rgba(90, 140, 210, 0.35);
  background: rgba(12, 24, 44, 0.94);
  color: #d7e4f7;
  backdrop-filter: blur(12px);
  box-shadow: 0 18px 38px rgba(2, 8, 20, 0.6);
  padding: 14px;
  pointer-events: none;
}

.tt-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(90, 140, 210, 0.28);
  margin-bottom: 8px;
}

.tt-header strong {
  font-size: 14px;
  color: #eaf2ff;
}

.tt-header small {
  font-size: 11px;
  color: #8ba2c8;
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
  color: #8ba2c8;
}

.tt-row strong {
  color: #eaf2ff;
}

.tt-row strong.cyan { color: #26c6e0; }
.tt-row strong.green { color: #34d39a; }
.tt-row strong.blue { color: #4c9dff; }
.tt-row strong.purple { color: #a97bf5; }
.tt-row strong.red { color: #ff5d6c; }

.tt-tip {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(90, 140, 210, 0.35);
  color: #8ba2c8;
  font-size: 12px;
}

:global(.scene-label) {
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid rgba(90, 140, 210, 0.4);
  background: rgba(14, 26, 46, 0.82);
  color: #d7e4f7;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(2, 8, 20, 0.5);
  white-space: nowrap;
}

:global(.scene-label.station-label) {
  border-color: rgba(46, 168, 132, 0.6);
  color: #7ff0cf;
  box-shadow: 0 0 18px rgba(46, 168, 132, 0.4);
}

:global(.scene-label.building-label) {
  font-size: 13px;
  letter-spacing: 0.1em;
  color: #eaf2ff;
}

/* 悬浮床态胶囊：对应看板图中每间房上方的状态标签 */
:global(.room-pill) {
  --pill: #4c9dff;
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 6px;
  border-radius: 999px;
  background: rgba(9, 18, 34, 0.9);
  border: 1px solid color-mix(in srgb, var(--pill) 65%, transparent);
  box-shadow: 0 8px 22px rgba(2, 8, 20, 0.55), 0 0 16px color-mix(in srgb, var(--pill) 40%, transparent);
  backdrop-filter: blur(8px);
  white-space: nowrap;
  transform: translateY(-2px);
}

:global(.room-pill::after) {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -6px;
  width: 2px;
  height: 6px;
  transform: translateX(-50%);
  background: var(--pill);
  opacity: 0.7;
}

:global(.room-pill__icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
  background: color-mix(in srgb, var(--pill) 26%, rgba(9, 18, 34, 0.6));
  color: #fff;
}

:global(.room-pill__body) {
  display: grid;
  gap: 1px;
  line-height: 1.05;
}

:global(.room-pill__body strong) {
  font-size: 12px;
  font-weight: 800;
  color: #f2f7ff;
}

:global(.room-pill__body small) {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--pill);
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
