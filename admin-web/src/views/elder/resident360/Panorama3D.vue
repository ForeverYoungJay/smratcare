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
        <div class="legend-chip status-warning">外出</div>
        <div class="legend-chip status-ai">观察</div>
        <div class="legend-chip status-alert">异常</div>
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

const materials = {
  buildingShell: new THREE.MeshStandardMaterial({ color: 0xe9f1fb, transparent: true, opacity: 0.98, roughness: 0.74, metalness: 0.06 }),
  buildingCap: new THREE.MeshStandardMaterial({ color: 0xdbe8f7, transparent: true, opacity: 1, roughness: 0.62, metalness: 0.04 }),
  buildingHitbox: new THREE.MeshBasicMaterial({ color: 0x7da0ff, transparent: true, opacity: 0.01, side: THREE.DoubleSide }),
  floor: new THREE.MeshStandardMaterial({ color: 0xf7fbff, transparent: true, opacity: 0.98, roughness: 0.8, metalness: 0.02 }),
  roomNeutral: new THREE.MeshStandardMaterial({ color: 0xf6efe4, transparent: true, opacity: 0.98, roughness: 0.84, metalness: 0.02 }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0xe3efff, emissive: 0xe8f3ff, emissiveIntensity: 0.08, transparent: true, opacity: 0.98 }),
  hover: new THREE.MeshStandardMaterial({ color: 0xe7f2ff, emissive: 0xdcecff, emissiveIntensity: 0.16, transparent: true, opacity: 0.98 }),
  bedNormal: new THREE.MeshStandardMaterial({ color: 0xc9d2dc, emissive: 0xe5ebf1, emissiveIntensity: 0.08, metalness: 0.12, roughness: 0.44 }),
  bedOccupied: new THREE.MeshStandardMaterial({ color: 0x58c392, emissive: 0x81e3b7, emissiveIntensity: 0.3, metalness: 0.08, roughness: 0.3 }),
  bedSleep: new THREE.MeshStandardMaterial({ color: 0xf2b14c, emissive: 0xffd18a, emissiveIntensity: 0.3, metalness: 0.06, roughness: 0.3 }),
  bedAi: new THREE.MeshStandardMaterial({ color: 0x8b78e8, emissive: 0xb4a6ff, emissiveIntensity: 0.28, metalness: 0.08, roughness: 0.34 }),
  bedWarning: new THREE.MeshStandardMaterial({ color: 0xf2b14c, emissive: 0xffd18a, emissiveIntensity: 0.24, metalness: 0.06, roughness: 0.3 }),
  bedAlert: new THREE.MeshStandardMaterial({ color: 0xef6f7c, emissive: 0xff9faa, emissiveIntensity: 0.36, metalness: 0.06, roughness: 0.28 }),
  bedOffline: new THREE.MeshStandardMaterial({ color: 0xc9d2dc, emissive: 0xdce5ee, emissiveIntensity: 0.08, metalness: 0.08, roughness: 0.42 })
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
  const concernBeds = room.beds.filter((bed) => ['HIGH', 'MEDIUM'].includes(String(bed.riskLevel || '')) || Number(bed.abnormalVital24hCount || 0) > 0).length
  const material = materials.roomNeutral.clone()
  if (concernBeds > 0) {
    material.color = new THREE.Color(0xf3eefe)
    material.emissive = new THREE.Color(0xded6ff)
    material.emissiveIntensity = 0.06
    return material
  }
  if (ratio >= 1) {
    material.color = new THREE.Color(0xe6f5ee)
    material.emissive = new THREE.Color(0xcff0dd)
    material.emissiveIntensity = 0.05
    return material
  }
  if (ratio === 0) {
    material.color = new THREE.Color(0xf2f5f9)
    return material
  }
  material.color = new THREE.Color(0xf6efe4)
  material.emissive = new THREE.Color(0xfff5ea)
  material.emissiveIntensity = 0.03
  return material
}

function createBuildingMass(totalFloors: number) {
  const group = new THREE.Group()
  const width = 10
  const depth = 10
  const floorHeight = 1.75
  for (let index = 0; index < totalFloors; index += 1) {
    const shell = new THREE.Mesh(
      new THREE.BoxGeometry(width - index * 0.18, floorHeight, depth - index * 0.18),
      materials.buildingShell.clone()
    )
    shell.position.y = floorHeight / 2 + index * (floorHeight * 0.96)
    group.add(shell)
  }
  const roof = new THREE.Mesh(new THREE.BoxGeometry(width + 0.6, 0.4, depth + 0.6), materials.buildingCap.clone())
  roof.position.y = totalFloors * (floorHeight * 0.96) + 0.3
  group.add(roof)
  return group
}

function createBedUnit(bed: any, width: number, depth: number) {
  const state = resolveBedVisualState(bed)
  const group = new THREE.Group()
  const frameHeight = 0.2

  const base = new THREE.Mesh(
    new THREE.BoxGeometry(width, frameHeight, depth),
    new THREE.MeshStandardMaterial({ color: 0xeaf0f6, metalness: 0.06, roughness: 0.54 })
  )
  base.position.y = frameHeight / 2
  group.add(base)

  const mattressMaterial = materialForState(state)
  const mattress = new THREE.Mesh(new THREE.BoxGeometry(width * 0.82, 0.14, depth * 0.7), mattressMaterial)
  mattress.position.y = frameHeight + 0.08
  mattress.userData = { type: 'bed', bed, originalMat: mattressMaterial, state }
  group.add(mattress)
  interactableObjects.push(mattress)

  const headboard = new THREE.Mesh(
    new THREE.BoxGeometry(width * 0.84, 0.3, 0.08),
    new THREE.MeshStandardMaterial({ color: 0xc8d7ea, metalness: 0.08, roughness: 0.3 })
  )
  headboard.position.set(0, 0.3, -depth * 0.34)
  group.add(headboard)

  const halo = new THREE.Mesh(
    new THREE.RingGeometry(Math.min(width, depth) * 0.38, Math.min(width, depth) * 0.52, 32),
    new THREE.MeshBasicMaterial({ color: mattressMaterial.color, transparent: true, opacity: state === 'alert' ? 0.24 : 0.12, side: THREE.DoubleSide })
  )
  halo.rotation.x = -Math.PI / 2
  halo.position.y = 0.03
  halo.userData.isHalo = true
  group.add(halo)
  mattress.userData.halo = halo

  if (state === 'alert') animatedBeds.push({ mesh: mattress, type: 'pulse', baseIntensity: 0.42 })
  else if (state === 'ai') animatedBeds.push({ mesh: mattress, type: 'ai', baseIntensity: 0.24 })
  else if (state === 'warning') animatedBeds.push({ mesh: mattress, type: 'blink', baseIntensity: 0.28 })
  else if (state === 'sleep') animatedBeds.push({ mesh: mattress, type: 'sleep', baseIntensity: 0.26 })

  return group
}

function buildAtmosphere() {
  if (!scene.value) return

  const campusBase = new THREE.Mesh(
    new THREE.CylinderGeometry(78, 84, 1.4, 64),
    new THREE.MeshStandardMaterial({
      color: 0xf2f7fd,
      roughness: 0.86,
      metalness: 0.02
    })
  )
  campusBase.position.y = -0.7
  campusBase.userData.isGenerated = true
  scene.value.add(campusBase)

  const gridHelper = new THREE.GridHelper(180, 28, 0xd5e3f3, 0xe6eef7)
  gridHelper.position.y = 0.02
  gridHelper.material.transparent = true
  ;(gridHelper.material as THREE.Material).opacity = 0.24
  gridHelper.userData.isGenerated = true
  scene.value.add(gridHelper)

  const ringGeo = new THREE.RingGeometry(28, 31, 96)
  const ringMat = new THREE.MeshBasicMaterial({ color: 0x7da0ff, transparent: true, opacity: 0.05, side: THREE.DoubleSide })
  const ringMesh = new THREE.Mesh(ringGeo, ringMat)
  ringMesh.rotation.x = -Math.PI / 2
  ringMesh.position.y = 0.04
  ringMesh.userData.isGenerated = true
  scene.value.add(ringMesh)

  const particleGeometry = new THREE.BufferGeometry()
  const particleCount = 240
  const positions = new Float32Array(particleCount * 3)
  for (let i = 0; i < particleCount; i += 1) {
    positions[i * 3] = (Math.random() - 0.5) * 144
    positions[i * 3 + 1] = Math.random() * 22 + 5
    positions[i * 3 + 2] = (Math.random() - 0.5) * 144
  }
  particleGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const particleMaterial = new THREE.PointsMaterial({ size: 0.42, color: 0x8fb3ff, transparent: true, opacity: 0.16 })
  particles = new THREE.Points(particleGeometry, particleMaterial)
  particles.userData.isGenerated = true
  scene.value.add(particles)
}

function initScene() {
  if (!canvasRef.value) return

  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight

  scene.value = new THREE.Scene()
  scene.value.background = new THREE.Color(0xf3f8fe)
  scene.value.fog = new THREE.FogExp2(0xf3f8fe, 0.0022)

  camera.value = new THREE.PerspectiveCamera(40, width / height, 0.1, 1600)
  camera.value.position.set(50, 38, 62)

  renderer.value = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.value.setSize(width, height)
  renderer.value.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.value.setClearColor(0xf3f8fe, 1)
  renderer.value.outputColorSpace = THREE.SRGBColorSpace
  renderer.value.toneMapping = THREE.ACESFilmicToneMapping
  renderer.value.toneMappingExposure = 0.96
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
  controls.value.autoRotateSpeed = 0.14
  controls.value.target.set(0, 6, 0)

  const ambientLight = new THREE.AmbientLight(0xffffff, 1)
  scene.value.add(ambientLight)

  const topLight = new THREE.DirectionalLight(0xffffff, 1.18)
  topLight.position.set(40, 90, 40)
  scene.value.add(topLight)

  const fillLight = new THREE.DirectionalLight(0xb7cbff, 0.4)
  fillLight.position.set(-56, 28, -46)
  scene.value.add(fillLight)

  const rimLight = new THREE.PointLight(0xb2c6ff, 0.72, 240, 2)
  rimLight.position.set(0, 18, -20)
  scene.value.add(rimLight)

  buildAtmosphere()

  renderer.value.domElement.addEventListener('pointermove', onPointerMove)
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

    const plinth = new THREE.Mesh(
      new THREE.CylinderGeometry(7.8, 8.8, 1.4, 18),
      new THREE.MeshStandardMaterial({ color: 0x0f1b29, roughness: 0.72, metalness: 0.06 })
    )
    plinth.position.y = 0.6
    buildingGroup.add(plinth)

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
      floorSlab.userData = { type: 'floorSlab', building: buildingName, floor: floorName, originalMat: materials.floor.clone() }
      floorGroup.add(floorSlab)
      interactableObjects.push(floorSlab)
      objectsMap.set(`floor_${buildingName}_${floorName}`, floorGroup)

      const corridor = new THREE.Mesh(
        new THREE.BoxGeometry(Math.max(3.6, slabWidth * 0.24), 0.04, slabDepth - 1.2),
        new THREE.MeshStandardMaterial({ color: 0xe8eef6, roughness: 0.92, metalness: 0.02 })
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
        new THREE.MeshStandardMaterial({ color: 0xdce8f6, roughness: 0.32, metalness: 0.08 })
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
        roomTile.userData = { type: 'roomTile', ref: roomGroup, data: roomItem, floorName, originalMat: roomMaterial }
        roomGroup.add(roomTile)
        interactableObjects.push(roomTile)

        const wallMaterial = new THREE.MeshStandardMaterial({ color: 0xf8fbff, roughness: 0.86, metalness: 0.02 })
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

        const roomEdges = new THREE.LineSegments(
          new THREE.EdgesGeometry(new THREE.BoxGeometry(roomSize, 0.18, roomSize)),
          new THREE.LineBasicMaterial({ color: roomItem.occupiedBeds >= roomItem.totalBeds ? 0x8ad1dc : 0x546778, transparent: true, opacity: 0.24 })
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
      new THREE.LineBasicMaterial({ color: 0x8ad1dc, transparent: true, opacity: 0.36 })
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

function onClick() {
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
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 5.2) + 1) * 0.14
    } else if (item.type === 'ai') {
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 3.2) + 1) * 0.08
    } else if (item.type === 'blink') {
      material.emissiveIntensity = Math.sin(time * 6.4) > 0 ? item.baseIntensity + 0.14 : item.baseIntensity * 0.32
    } else {
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 2.8) + 1) * 0.06
    }
  })

  if (particles) {
    particles.rotation.y += 0.0006
    particles.position.y = Math.sin(time * 0.7) * 0.4 + 2
  }

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
  min-height: 760px;
  overflow: hidden;
  border-radius: 28px;
  background:
    radial-gradient(circle at 12% 12%, rgba(141, 194, 255, 0.14), transparent 28%),
    radial-gradient(circle at 82% 0%, rgba(123, 210, 184, 0.12), transparent 30%),
    linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%);
  box-shadow:
    inset 0 0 90px rgba(141, 194, 255, 0.05),
    inset 0 0 30px rgba(123, 210, 184, 0.04);
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
    radial-gradient(circle at center, rgba(255, 255, 255, 0.02), rgba(224, 235, 247, 0.12) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(225, 236, 248, 0.12));
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
  border: 1px solid rgba(171, 191, 214, 0.22);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 28px rgba(114, 135, 162, 0.12);
}

.toolbar-center {
  display: grid;
  gap: 4px;
  min-width: 320px;
  text-align: center;
}

.toolbar-center strong {
  color: #1f2d41;
}

.toolbar-center small,
.toolbar-eyebrow {
  color: #7d8ea5;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.tech-btn {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(171, 191, 214, 0.24);
  color: #41536b;
  border-radius: 14px;
}

.tech-btn:hover {
  border-color: rgba(111, 149, 255, 0.32);
  color: #22324b;
  box-shadow: 0 10px 24px rgba(111, 149, 255, 0.12);
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
  color: #50627a;
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

.status-occupied::before { background: #4fc291; }
.status-empty::before { background: #c6d0dc; }
.status-warning::before { background: #f2b14c; }
.status-ai::before { background: #8b78e8; }
.status-alert::before { background: #ef6f7c; }

.tech-tooltip {
  position: absolute;
  z-index: 8;
  width: 230px;
  border-radius: 18px;
  border: 1px solid rgba(171, 191, 214, 0.24);
  background: rgba(255, 255, 255, 0.96);
  color: #33455d;
  backdrop-filter: blur(10px);
  box-shadow: 0 16px 28px rgba(116, 137, 164, 0.14);
  padding: 14px;
  pointer-events: none;
}

.tt-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(166, 199, 219, 0.12);
  margin-bottom: 8px;
}

.tt-header strong {
  font-size: 14px;
}

.tt-header small {
  font-size: 11px;
  color: #7d8ea5;
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
  color: #7d8ea5;
}

.tt-row strong {
  color: #22324b;
}

.tt-row strong.cyan { color: #5a8dff; }
.tt-row strong.green { color: #2f9b6c; }
.tt-row strong.blue { color: #5a8dff; }
.tt-row strong.purple { color: #6b56d5; }
.tt-row strong.red { color: #d65164; }

.tt-tip {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(171, 191, 214, 0.24);
  color: #7d8ea5;
  font-size: 12px;
}

:global(.scene-label) {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(171, 191, 214, 0.22);
  background: rgba(255, 255, 255, 0.92);
  color: #354760;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 26px rgba(114, 135, 162, 0.12);
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
