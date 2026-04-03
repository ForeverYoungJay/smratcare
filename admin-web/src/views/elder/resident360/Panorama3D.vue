<template>
  <div class="panorama-container" ref="containerRef">
    <div class="scene-atmosphere"></div>
    <div class="scene-scanlines"></div>

    <div class="scene-toolbar">
      <div class="toolbar-left">
        <a-button v-if="currentLevel !== 'PARK'" class="tech-btn" @click="goUpLevel">返回上级</a-button>
        <a-button class="tech-btn" @click="resetCamera">重置视角</a-button>
      </div>
      <div class="toolbar-center">
        <span class="toolbar-eyebrow">Current Scope</span>
        <strong>{{ breadcrumbText }}</strong>
      </div>
      <div class="toolbar-right">
        <div class="legend-chip status-normal">正常</div>
        <div class="legend-chip status-occupied">在床</div>
        <div class="legend-chip status-sleep">睡眠</div>
        <div class="legend-chip status-alert">告警</div>
        <div class="legend-chip status-offline">离线</div>
      </div>
    </div>

    <div class="scene-hud">
      <div class="hud-panel">
        <span class="hud-label">当前层级</span>
        <strong>{{ currentLevelLabel }}</strong>
      </div>
      <div class="hud-panel">
        <span class="hud-label">聚焦对象</span>
        <strong>{{ focusTitle }}</strong>
      </div>
      <div class="hud-panel">
        <span class="hud-label">运行状态</span>
        <strong>{{ focusStatus }}</strong>
      </div>
    </div>

    <div v-show="tooltip.visible" class="tech-tooltip" :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }">
      <div class="tt-header">{{ tooltip.title }}</div>
      <div class="tt-body" v-html="tooltip.content"></div>
    </div>

    <div ref="canvasRef" class="canvas-wrapper"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import gsap from 'gsap'

const props = defineProps<{
  buildings: string[]
  floors: string[]
  roomLookup: Map<string, any[]>
}>()

const emit = defineEmits(['click-room', 'click-bed'])

const containerRef = ref<HTMLElement | null>(null)
const canvasRef = ref<HTMLElement | null>(null)
const currentLevel = ref<'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM'>('PARK')
const selectedBuilding = ref('')
const selectedFloor = ref('')
const selectedRoom = ref('')
const focusTitle = ref('园区总览')
const focusStatus = ref('等待交互')
const tooltip = ref({ visible: false, x: 0, y: 0, title: '', content: '' })

const breadcrumbText = computed(() => {
  let text = '园区全景'
  if (selectedBuilding.value) text += ` / ${selectedBuilding.value}`
  if (selectedFloor.value) text += ` / ${selectedFloor.value}`
  if (selectedRoom.value) text += ` / 房间 ${selectedRoom.value}`
  return text
})

const currentLevelLabel = computed(() => {
  if (currentLevel.value === 'PARK') return '园区'
  if (currentLevel.value === 'BUILDING') return '楼栋'
  if (currentLevel.value === 'FLOOR') return '楼层'
  return '房间'
})

const scene = shallowRef(new THREE.Scene())
const camera = shallowRef<THREE.PerspectiveCamera | null>(null)
const renderer = shallowRef<THREE.WebGLRenderer | null>(null)
const cssRenderer = shallowRef<CSS2DRenderer | null>(null)
const controls = shallowRef<OrbitControls | null>(null)

const objectsMap = new Map<string, THREE.Object3D>()
const interactableObjects: THREE.Object3D[] = []
const animatedBeds: Array<{ mesh: THREE.Mesh; type: 'pulse' | 'blink' | 'sleep'; baseIntensity: number }> = []
let particles: THREE.Points | null = null
let animationFrameId = 0
let rebuildSceneTimer = 0
let lastPointerMoveAt = 0
const sceneRenderingEnabled = ref(true)
const pickPosition = new THREE.Vector2()
const raycaster = new THREE.Raycaster()
let hoveredObj: THREE.Object3D | null = null

const materials = {
  building: new THREE.MeshStandardMaterial({ color: 0x57d7ff, transparent: true, opacity: 0.1, roughness: 0.15, metalness: 0.86, side: THREE.DoubleSide }),
  floor: new THREE.MeshStandardMaterial({ color: 0x08192f, transparent: true, opacity: 0.95, roughness: 0.42, metalness: 0.42 }),
  room: new THREE.MeshStandardMaterial({ color: 0x163859, transparent: true, opacity: 0.7, roughness: 0.55, metalness: 0.2 }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0x2bcfff, emissive: 0x2bcfff, emissiveIntensity: 0.4, transparent: true, opacity: 0.85 }),
  hover: new THREE.MeshStandardMaterial({ color: 0x63e3ff, emissive: 0x2bcfff, emissiveIntensity: 0.6, transparent: true, opacity: 0.85 }),
  bedNormal: new THREE.MeshStandardMaterial({ color: 0x2bcfff, emissive: 0x2bcfff, emissiveIntensity: 0.36, metalness: 0.48, roughness: 0.28 }),
  bedOccupied: new THREE.MeshStandardMaterial({ color: 0x24cc93, emissive: 0x24cc93, emissiveIntensity: 0.42, metalness: 0.38, roughness: 0.3 }),
  bedSleep: new THREE.MeshStandardMaterial({ color: 0x8f74ff, emissive: 0x8f74ff, emissiveIntensity: 0.44, metalness: 0.36, roughness: 0.32 }),
  bedWarning: new THREE.MeshStandardMaterial({ color: 0xffa556, emissive: 0xffa556, emissiveIntensity: 0.48, metalness: 0.34, roughness: 0.3 }),
  bedAlert: new THREE.MeshStandardMaterial({ color: 0xff5d7c, emissive: 0xff5d7c, emissiveIntensity: 0.76, metalness: 0.26, roughness: 0.28 }),
  bedOffline: new THREE.MeshStandardMaterial({ color: 0x7388a0, emissive: 0x7388a0, emissiveIntensity: 0.18, metalness: 0.2, roughness: 0.5 })
}

function initScene() {
  if (!canvasRef.value) return

  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight

  scene.value = new THREE.Scene()
  scene.value.background = new THREE.Color(0x030b16)
  scene.value.fog = new THREE.FogExp2(0x020811, 0.008)

  camera.value = new THREE.PerspectiveCamera(42, width / height, 0.1, 1600)
  camera.value.position.set(42, 38, 64)

  renderer.value = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.value.setSize(width, height)
  renderer.value.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.value.setClearColor(0x020811, 1)
  renderer.value.outputColorSpace = THREE.SRGBColorSpace
  renderer.value.toneMapping = THREE.ACESFilmicToneMapping
  renderer.value.toneMappingExposure = 1.16
  canvasRef.value.appendChild(renderer.value.domElement)

  cssRenderer.value = new CSS2DRenderer()
  cssRenderer.value.setSize(width, height)
  cssRenderer.value.domElement.style.position = 'absolute'
  cssRenderer.value.domElement.style.top = '0'
  cssRenderer.value.domElement.style.pointerEvents = 'none'
  canvasRef.value.appendChild(cssRenderer.value.domElement)

  controls.value = new OrbitControls(camera.value, renderer.value.domElement)
  controls.value.enableDamping = true
  controls.value.dampingFactor = 0.05
  controls.value.minDistance = 8
  controls.value.maxDistance = 180
  controls.value.maxPolarAngle = Math.PI / 2 - 0.04
  controls.value.autoRotate = true
  controls.value.autoRotateSpeed = 0.18

  const ambientLight = new THREE.AmbientLight(0xd8efff, 0.62)
  scene.value.add(ambientLight)

  const topLight = new THREE.DirectionalLight(0xf0fbff, 1.45)
  topLight.position.set(36, 80, 40)
  scene.value.add(topLight)

  const fillLight = new THREE.DirectionalLight(0x45bfff, 0.65)
  fillLight.position.set(-40, 24, -36)
  scene.value.add(fillLight)

  const rimLight = new THREE.PointLight(0x8f74ff, 1.2, 260, 2)
  rimLight.position.set(0, 20, -24)
  scene.value.add(rimLight)

  buildAtmosphere()

  renderer.value.domElement.addEventListener('pointermove', onPointerMove)
  renderer.value.domElement.addEventListener('click', onClick)
  window.addEventListener('resize', onWindowResize)

  buildSceneData()
  animate()
}

function buildAtmosphere() {
  if (!scene.value) return

  const gridHelper = new THREE.GridHelper(280, 42, 0x163859, 0x0a1526)
  gridHelper.position.y = -0.12
  gridHelper.material.transparent = true
  ;(gridHelper.material as THREE.Material).opacity = 0.38
  gridHelper.userData.isGenerated = true
  scene.value.add(gridHelper)

  const ringGeo = new THREE.RingGeometry(32, 34, 120)
  const ringMat = new THREE.MeshBasicMaterial({ color: 0x2bcfff, transparent: true, opacity: 0.08, side: THREE.DoubleSide })
  const ringMesh = new THREE.Mesh(ringGeo, ringMat)
  ringMesh.rotation.x = -Math.PI / 2
  ringMesh.position.y = -0.08
  ringMesh.userData.isGenerated = true
  scene.value.add(ringMesh)

  const particleGeometry = new THREE.BufferGeometry()
  const particleCount = 320
  const positions = new Float32Array(particleCount * 3)
  for (let i = 0; i < particleCount; i += 1) {
    positions[i * 3] = (Math.random() - 0.5) * 180
    positions[i * 3 + 1] = Math.random() * 28 + 4
    positions[i * 3 + 2] = (Math.random() - 0.5) * 180
  }
  particleGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const particleMaterial = new THREE.PointsMaterial({
    size: 0.7,
    color: 0x57d7ff,
    transparent: true,
    opacity: 0.4
  })
  particles = new THREE.Points(particleGeometry, particleMaterial)
  particles.userData.isGenerated = true
  scene.value.add(particles)
}

function createLabel(text: string, className: string) {
  const div = document.createElement('div')
  div.className = `scene-label ${className}`
  div.textContent = text
  return new CSS2DObject(div)
}

function resolveBedVisualState(bed: any): 'normal' | 'occupied' | 'sleep' | 'warning' | 'alert' | 'offline' {
  const abnormalCount = Number(bed.abnormalVital24hCount || 0)
  if (bed.status === 0 || bed.status === 2) return 'offline'
  if (bed.riskLevel === 'HIGH' || abnormalCount > 0) return 'alert'
  if (bed.riskLevel === 'MEDIUM' || bed.status === 3 || String(bed.bedNo || '').endsWith('R')) return 'warning'
  if (bed.riskLevel === 'LOW' && bed.elderId) return 'sleep'
  if (bed.elderId) return 'occupied'
  return 'normal'
}

function materialForState(state: ReturnType<typeof resolveBedVisualState>) {
  if (state === 'occupied') return materials.bedOccupied.clone()
  if (state === 'sleep') return materials.bedSleep.clone()
  if (state === 'warning') return materials.bedWarning.clone()
  if (state === 'alert') return materials.bedAlert.clone()
  if (state === 'offline') return materials.bedOffline.clone()
  return materials.bedNormal.clone()
}

function createBedUnit(bed: any, width: number, depth: number) {
  const state = resolveBedVisualState(bed)
  const group = new THREE.Group()
  const frameHeight = 0.22

  const base = new THREE.Mesh(
    new THREE.BoxGeometry(width, frameHeight, depth),
    new THREE.MeshStandardMaterial({
      color: 0x10253d,
      metalness: 0.5,
      roughness: 0.42
    })
  )
  base.position.y = frameHeight / 2
  group.add(base)

  const mattressMaterial = materialForState(state)
  const mattress = new THREE.Mesh(new THREE.BoxGeometry(width * 0.82, 0.14, depth * 0.74), mattressMaterial)
  mattress.position.y = frameHeight + 0.08
  mattress.userData = { type: 'bed', bed, originalMat: mattressMaterial, state }
  group.add(mattress)
  interactableObjects.push(mattress)

  const headboard = new THREE.Mesh(
    new THREE.BoxGeometry(width * 0.84, 0.34, 0.08),
    new THREE.MeshStandardMaterial({ color: 0x1f4367, metalness: 0.42, roughness: 0.26 })
  )
  headboard.position.set(0, 0.34, -depth * 0.36)
  group.add(headboard)

  const glow = new THREE.Mesh(
    new THREE.PlaneGeometry(width * 0.96, depth * 0.88),
    new THREE.MeshBasicMaterial({
      color: mattressMaterial.color,
      transparent: true,
      opacity: state === 'alert' ? 0.24 : 0.12
    })
  )
  glow.rotation.x = -Math.PI / 2
  glow.position.y = 0.02
  group.add(glow)

  if (state === 'alert') {
    animatedBeds.push({ mesh: mattress, type: 'pulse', baseIntensity: 0.76 })
  } else if (state === 'warning') {
    animatedBeds.push({ mesh: mattress, type: 'blink', baseIntensity: 0.48 })
  } else if (state === 'sleep') {
    animatedBeds.push({ mesh: mattress, type: 'sleep', baseIntensity: 0.44 })
  }

  return group
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
  const spacing = 24

  props.buildings.forEach((buildingName, buildingIndex) => {
    const buildingGroup = new THREE.Group()
    buildingGroup.userData = { type: 'building', name: buildingName }

    const row = Math.floor(buildingIndex / cols)
    const col = buildingIndex % cols
    buildingGroup.position.set((col - (cols - 1) / 2) * spacing, 0, (row - (Math.ceil(props.buildings.length / cols) - 1) / 2) * spacing)

    const buildingFloors = props.floors.filter((floor) => props.roomLookup.has(`${buildingName}@@${floor}`))
    const floorHeight = 4
    const totalFloors = buildingFloors.length || 1

    buildingFloors.forEach((floorName, floorIndex) => {
      const floorGroup = new THREE.Group()
      const floorYIndex = totalFloors - 1 - floorIndex
      floorGroup.position.y = floorYIndex * floorHeight
      floorGroup.userData = { type: 'floor', building: buildingName, name: floorName, floorYIndex }

      const rooms = props.roomLookup.get(`${buildingName}@@${floorName}`) || []
      const roomColumns = Math.ceil(Math.sqrt(Math.max(rooms.length, 1)))
      const roomSize = 4.4
      const roomGap = 0.8
      const slabWidth = roomColumns * (roomSize + roomGap) + 1.2
      const slabDepth = Math.ceil(Math.max(rooms.length, 1) / roomColumns) * (roomSize + roomGap) + 1.2

      const floorSlab = new THREE.Mesh(new THREE.BoxGeometry(slabWidth, 0.42, slabDepth), materials.floor.clone())
      floorSlab.position.y = 0.18
      floorSlab.userData = { type: 'floorSlab', building: buildingName, floor: floorName, originalMat: materials.floor }
      floorGroup.add(floorSlab)
      interactableObjects.push(floorSlab)
      objectsMap.set(`floor_${buildingName}_${floorName}`, floorGroup)

      const slabEdge = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.BoxGeometry(slabWidth, 0.42, slabDepth)),
        new THREE.LineBasicMaterial({ color: 0x2bcfff, transparent: true, opacity: 0.28 })
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
          0.42,
          (r - (Math.ceil(Math.max(rooms.length, 1) / roomColumns) - 1) / 2) * (roomSize + roomGap)
        )

        const roomTile = new THREE.Mesh(new THREE.BoxGeometry(roomSize, 0.22, roomSize), materials.room.clone())
        roomTile.position.y = 0.11
        roomTile.userData = { type: 'roomTile', ref: roomGroup, data: roomItem, floorName, originalMat: materials.room }
        roomGroup.add(roomTile)
        interactableObjects.push(roomTile)

        const roomEdges = new THREE.LineSegments(
          new THREE.EdgesGeometry(new THREE.BoxGeometry(roomSize, 0.22, roomSize)),
          new THREE.LineBasicMaterial({ color: 0x2bcfff, transparent: true, opacity: 0.26 })
        )
        roomEdges.position.y = 0.11
        roomGroup.add(roomEdges)

        const bedCount = roomItem.beds.length
        if (bedCount) {
          const bedColumns = Math.ceil(Math.sqrt(bedCount))
          const bedWidth = (roomSize - 0.8) / bedColumns
          const bedDepth = Math.min(bedWidth * 1.5, (roomSize - 0.8) / Math.ceil(bedCount / bedColumns))
          roomItem.beds.forEach((bed: any, bedIndex: number) => {
            const bedUnit = createBedUnit(bed, bedWidth * 0.88, bedDepth * 0.82)
            const rowIndex = Math.floor(bedIndex / bedColumns)
            const colIndex = bedIndex % bedColumns
            bedUnit.position.set(
              (colIndex - (bedColumns - 1) / 2) * bedWidth,
              0.26,
              (rowIndex - (Math.ceil(bedCount / bedColumns) - 1) / 2) * bedDepth
            )
            roomGroup.add(bedUnit)
          })
        }

        const roomLabel = createLabel(`${roomItem.roomNo}  ${roomItem.occupiedBeds}/${roomItem.totalBeds}`, 'room-label')
        roomLabel.position.set(0, 1.9, 0)
        roomLabel.visible = false
        roomGroup.add(roomLabel)

        floorGroup.add(roomGroup)
      })

      const floorLabel = createLabel(floorName, 'floor-label')
      floorLabel.position.set(slabWidth / 2 + 1.6, 0.2, 0)
      floorLabel.visible = false
      floorGroup.add(floorLabel)

      buildingGroup.add(floorGroup)
    })

    const hitboxHeight = Math.max(totalFloors * floorHeight, floorHeight)
    const hitbox = new THREE.Mesh(new THREE.BoxGeometry(12, hitboxHeight, 12), materials.building.clone())
    hitbox.position.y = hitboxHeight / 2
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
      new THREE.LineBasicMaterial({ color: 0x57d7ff, transparent: true, opacity: 0.58 })
    )
    hitboxEdges.position.y = hitboxHeight / 2
    buildingGroup.add(hitboxEdges)

    const buildingLabel = createLabel(buildingName, 'building-label')
    buildingLabel.position.set(0, hitboxHeight + 2.2, 0)
    buildingGroup.add(buildingLabel)

    parkGroup.add(buildingGroup)
    objectsMap.set(`building_${buildingName}`, buildingGroup)
  })

  updateVisibility()
}

function updateVisibility() {
  controls.value!.autoRotate = currentLevel.value === 'PARK'
  objectsMap.forEach((obj, key) => {
    if (!key.startsWith('building_')) return
    const buildingName = obj.userData.name
    const hitbox = obj.children.find((child) => child.userData.type === 'buildingHitbox')
    const buildingLabel = obj.children.find((child) => child instanceof CSS2DObject && child.element.className.includes('building-label'))

    if (currentLevel.value === 'PARK') {
      obj.visible = true
      if (hitbox) hitbox.visible = true
      if (buildingLabel) buildingLabel.visible = true
      obj.children.forEach((child) => {
        if (child.userData.type === 'floor') {
          child.visible = false
          gsap.to(child.position, { y: child.userData.originalY ?? child.position.y, duration: 0.5 })
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

    obj.children.forEach((child) => {
      if (child.userData.type !== 'floor') return
      child.userData.originalY = child.userData.originalY ?? child.position.y
      if (currentLevel.value === 'BUILDING') {
        child.visible = true
        gsap.to(child.position, { y: child.userData.floorYIndex * 8.5, duration: 0.85, ease: 'power2.out' })
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
        gsap.to(child.position, { y: 0, duration: 0.85, ease: 'power2.out' })
        child.children.forEach((subChild) => {
          if (subChild.userData.type === 'room') {
            const isCurrentRoom = subChild.userData.room.roomNo === selectedRoom.value
            subChild.visible = currentLevel.value !== 'ROOM' || isCurrentRoom
            const roomLabel = subChild.children.find((roomChild) => roomChild instanceof CSS2DObject && roomChild.element.className.includes('room-label'))
            if (roomLabel) roomLabel.visible = currentLevel.value === 'FLOOR'
          }
          if (subChild instanceof CSS2DObject && subChild.element.className.includes('floor-label')) subChild.visible = false
        })
      } else {
        child.visible = false
      }
    })
  })
}

function updateTooltip(event: PointerEvent, obj: THREE.Object3D) {
  tooltip.value.visible = true
  tooltip.value.x = event.clientX + 18
  tooltip.value.y = event.clientY + 18

  if (obj.userData.type === 'buildingHitbox') {
    tooltip.value.title = obj.userData.name
    tooltip.value.content = `<div class="tt-row"><span>楼层数</span><span class="tt-val cyan">${obj.userData.totalFloors} 层</span></div>
      <div class="tt-row"><span>总房间</span><span class="tt-val">${obj.userData.totalRooms} 间</span></div>
      <div class="tt-tip">点击展开楼栋内部结构</div>`
    focusTitle.value = obj.userData.name
    focusStatus.value = '楼栋级观察'
    return
  }

  if (obj.userData.type === 'floorSlab') {
    tooltip.value.title = `${obj.userData.building} / ${obj.userData.floor}`
    tooltip.value.content = `<div class="tt-row"><span>透视模式</span><span class="tt-val cyan">楼层平铺</span></div>
      <div class="tt-tip">点击进入楼层鸟瞰视图</div>`
    focusTitle.value = `${obj.userData.building} ${obj.userData.floor}`
    focusStatus.value = '楼层级观察'
    return
  }

  if (obj.userData.type === 'roomTile') {
    const roomData = obj.userData.data
    tooltip.value.title = `房间 ${roomData.roomNo || '-'}`
    tooltip.value.content = `<div class="tt-row"><span>容量</span><span class="tt-val">${roomData.totalBeds} 床</span></div>
      <div class="tt-row"><span>入住</span><span class="tt-val ${roomData.occupiedBeds >= roomData.totalBeds ? 'red' : 'green'}">${roomData.occupiedBeds}/${roomData.totalBeds}</span></div>
      <div class="tt-tip">${currentLevel.value === 'ROOM' ? '再次点击打开房间详情' : '点击进入房间视角'}</div>`
    focusTitle.value = `房间 ${roomData.roomNo || '-'}`
    focusStatus.value = `${roomData.occupiedBeds}/${roomData.totalBeds} 床位占用`
    return
  }

  if (obj.userData.type === 'bed') {
    const bed = obj.userData.bed
    const occupiedText = bed.elderId ? `${bed.elderName || '已入住'}` : '空床'
    tooltip.value.title = `床位 ${bed.bedNo || '-'}`
    tooltip.value.content = `<div class="tt-row"><span>状态</span><span class="tt-val">${occupiedText}</span></div>
      <div class="tt-row"><span>风险</span><span class="tt-val ${bed.riskLevel === 'HIGH' ? 'red' : bed.riskLevel === 'MEDIUM' ? 'orange' : 'cyan'}">${bed.riskLabel || '正常'}</span></div>
      <div class="tt-row"><span>24h异常</span><span class="tt-val">${bed.abnormalVital24hCount || 0} 次</span></div>
      <div class="tt-tip">点击查看业务详情</div>`
    focusTitle.value = `${bed.roomNo || '-'} / ${bed.bedNo || '-'}`
    focusStatus.value = bed.riskLabel || (bed.elderId ? '在住监测中' : '床位空闲')
    return
  }

  tooltip.value.visible = false
}

function restoreHoverObject(obj: THREE.Object3D) {
  if (obj.userData.type === 'buildingHitbox' || obj.userData.type === 'floorSlab') {
    ;(obj as THREE.Mesh).material = obj.userData.originalMat || materials.building
  }
  if (obj.userData.type === 'roomTile') {
    ;(obj as THREE.Mesh).material = obj.userData.originalMat || materials.room
  }
  if (obj.userData.type === 'bed') {
    ;(obj as THREE.Mesh).material = obj.userData.originalMat
    ;(obj as THREE.Mesh).scale.set(1, 1, 1)
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
      ;(obj as THREE.Mesh).scale.set(1.14, 1.14, 1.14)
      if (!animatedBeds.some((item) => item.mesh === obj)) {
        ;(obj as THREE.Mesh).material = materials.hover
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
    selectedBuilding.value = hoveredObj.userData.name
    selectedFloor.value = ''
    selectedRoom.value = ''
    currentLevel.value = 'BUILDING'
    updateVisibility()
    zoomToObject(hoveredObj.userData.ref)
    return
  }

  if (hoveredObj.userData.type === 'floorSlab') {
    selectedFloor.value = hoveredObj.userData.floor
    selectedRoom.value = ''
    currentLevel.value = 'FLOOR'
    updateVisibility()
    const floorGroup = objectsMap.get(`floor_${selectedBuilding.value}_${selectedFloor.value}`)
    if (floorGroup) zoomToObject(floorGroup, true)
    return
  }

  if (hoveredObj.userData.type === 'roomTile') {
    if (currentLevel.value === 'ROOM') {
      emit('click-room', hoveredObj.userData.data)
      return
    }
    selectedRoom.value = hoveredObj.userData.ref.userData.room.roomNo
    currentLevel.value = 'ROOM'
    updateVisibility()
    zoomToObject(hoveredObj.userData.ref, false, 18)
    return
  }

  if (hoveredObj.userData.type === 'bed') {
    emit('click-bed', hoveredObj.userData.bed)
  }
}

function zoomToObject(obj: THREE.Object3D, topDown = false, overrideZ?: number) {
  if (!camera.value || !controls.value) return
  const box = new THREE.Box3().setFromObject(obj)
  const center = box.getCenter(new THREE.Vector3())
  const size = box.getSize(new THREE.Vector3())
  const maxDim = Math.max(size.x, size.y, size.z)
  const fov = camera.value.fov * (Math.PI / 180)
  const cameraDistance = overrideZ || Math.abs(maxDim / 2 / Math.tan(fov / 2)) * 1.8
  const offsetX = topDown ? 0 : cameraDistance * 0.42
  const offsetZ = topDown ? 0 : cameraDistance

  gsap.to(camera.value.position, {
    x: center.x + offsetX,
    y: topDown ? center.y + cameraDistance : center.y + size.y / 2 + 10,
    z: center.z + offsetZ,
    duration: 1.24,
    ease: 'power3.inOut'
  })
  gsap.to(controls.value.target, {
    x: center.x,
    y: center.y,
    z: center.z,
    duration: 1.24,
    ease: 'power3.inOut'
  })
}

function goUpLevel() {
  if (currentLevel.value === 'ROOM') {
    currentLevel.value = 'FLOOR'
    selectedRoom.value = ''
    updateVisibility()
    const floorGroup = objectsMap.get(`floor_${selectedBuilding.value}_${selectedFloor.value}`)
    if (floorGroup) zoomToObject(floorGroup, true)
    return
  }

  if (currentLevel.value === 'FLOOR') {
    currentLevel.value = 'BUILDING'
    selectedFloor.value = ''
    updateVisibility()
    const buildingGroup = objectsMap.get(`building_${selectedBuilding.value}`)
    if (buildingGroup) zoomToObject(buildingGroup)
    return
  }

  if (currentLevel.value === 'BUILDING') {
    currentLevel.value = 'PARK'
    selectedBuilding.value = ''
    selectedFloor.value = ''
    updateVisibility()
    resetCamera()
  }
}

function resetCamera() {
  if (!camera.value || !controls.value) return
  currentLevel.value = 'PARK'
  selectedBuilding.value = ''
  selectedFloor.value = ''
  selectedRoom.value = ''
  focusTitle.value = '园区总览'
  focusStatus.value = '等待交互'
  updateVisibility()
  gsap.to(camera.value.position, { x: 42, y: 38, z: 64, duration: 1.1, ease: 'power3.out' })
  gsap.to(controls.value.target, { x: 0, y: 0, z: 0, duration: 1.1, ease: 'power3.out' })
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
  if (rebuildSceneTimer) {
    window.clearTimeout(rebuildSceneTimer)
  }
  rebuildSceneTimer = window.setTimeout(() => {
    rebuildSceneTimer = 0
    buildSceneData()
  }, 80)
}

function handleVisibilityChange() {
  sceneRenderingEnabled.value = !document.hidden
  if (sceneRenderingEnabled.value && !animationFrameId) {
    animate()
  }
}

function animate() {
  animationFrameId = requestAnimationFrame(animate)
  if (!sceneRenderingEnabled.value) return
  const time = Date.now() * 0.0014
  controls.value?.update()

  animatedBeds.forEach((item) => {
    const material = item.mesh.material as THREE.MeshStandardMaterial
    if (item.type === 'pulse') {
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 5) + 1) * 0.3
    } else if (item.type === 'blink') {
      material.emissiveIntensity = Math.sin(time * 7) > 0 ? item.baseIntensity + 0.28 : item.baseIntensity * 0.36
    } else {
      material.emissiveIntensity = item.baseIntensity + (Math.sin(time * 2.8) + 1) * 0.12
    }
  })

  if (particles) {
    particles.rotation.y += 0.0008
    particles.position.y = Math.sin(time * 0.8) * 0.6
  }

  renderer.value?.render(scene.value, camera.value!)
  cssRenderer.value?.render(scene.value, camera.value!)
}

watch(() => props.buildings, scheduleBuildSceneData, { deep: true })
watch(() => props.floors, scheduleBuildSceneData, { deep: true })
watch(() => props.roomLookup, scheduleBuildSceneData, { deep: true })

onMounted(() => {
  sceneRenderingEnabled.value = !document.hidden
  initScene()
  document.addEventListener('visibilitychange', handleVisibilityChange)
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
  controls.value?.dispose()
  renderer.value?.dispose()
})
</script>

<style scoped>
.panorama-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 620px;
  overflow: hidden;
  border-radius: 24px;
  background:
    radial-gradient(circle at 12% 12%, rgba(87, 215, 255, 0.14), transparent 26%),
    radial-gradient(circle at 82% 0%, rgba(143, 116, 255, 0.16), transparent 28%),
    linear-gradient(180deg, #05101d 0%, #030914 100%);
  box-shadow: inset 0 0 120px rgba(43, 207, 255, 0.08);
}

.canvas-wrapper {
  width: 100%;
  height: 100%;
}

.scene-atmosphere,
.scene-scanlines {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.scene-atmosphere {
  background:
    radial-gradient(circle at center, transparent 38%, rgba(3, 10, 20, 0.54) 100%);
}

.scene-scanlines {
  background: repeating-linear-gradient(180deg, rgba(255, 255, 255, 0.02) 0, rgba(255, 255, 255, 0.02) 1px, transparent 1px, transparent 4px);
  mix-blend-mode: screen;
  opacity: 0.16;
}

.scene-toolbar,
.scene-hud {
  position: absolute;
  left: 20px;
  right: 20px;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.scene-toolbar {
  top: 18px;
}

.scene-hud {
  bottom: 18px;
  left: 18px;
  right: auto;
  max-width: calc(100% - 36px);
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right,
.overlay-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-center,
.hud-panel {
  padding: 12px 16px;
  border-radius: 18px;
  border: 1px solid rgba(87, 215, 255, 0.18);
  background: rgba(5, 16, 31, 0.8);
  backdrop-filter: blur(12px);
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.28);
}

.toolbar-center {
  display: grid;
  gap: 4px;
  min-width: 240px;
  text-align: center;
}

.toolbar-eyebrow,
.hud-label {
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #88a9c3;
}

.toolbar-center strong,
.hud-panel strong {
  color: #ebfbff;
}

.tech-btn {
  background: rgba(7, 22, 42, 0.86);
  border: 1px solid rgba(87, 215, 255, 0.22);
  color: #dff6ff;
  border-radius: 14px;
}

.tech-btn:hover {
  border-color: rgba(87, 215, 255, 0.5);
  color: #ffffff;
  box-shadow: 0 0 20px rgba(87, 215, 255, 0.18);
}

.legend-chip {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: rgba(5, 16, 31, 0.76);
  font-size: 12px;
  color: #dff6ff;
}

.status-normal {
  border-color: rgba(87, 215, 255, 0.24);
}

.status-occupied {
  border-color: rgba(62, 232, 181, 0.24);
}

.status-sleep {
  border-color: rgba(143, 116, 255, 0.3);
}

.status-alert {
  border-color: rgba(255, 93, 124, 0.34);
}

.status-offline {
  border-color: rgba(115, 136, 160, 0.3);
}

.tech-tooltip {
  position: fixed;
  z-index: 10;
  min-width: 210px;
  border-radius: 16px;
  border: 1px solid rgba(87, 215, 255, 0.24);
  background: rgba(6, 18, 34, 0.9);
  color: #eaf7ff;
  backdrop-filter: blur(12px);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.42), 0 0 24px rgba(43, 207, 255, 0.14);
  padding: 14px;
  pointer-events: none;
}

.tt-header {
  font-size: 14px;
  font-weight: 700;
  border-bottom: 1px solid rgba(87, 215, 255, 0.16);
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.tt-body {
  font-size: 12px;
  color: #9ebcd4;
}

.tech-tooltip :deep(.tt-row) {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.tech-tooltip :deep(.tt-val) {
  color: #f2fdff;
  font-weight: 700;
}

.tech-tooltip :deep(.tt-val.cyan) {
  color: #57d7ff;
}

.tech-tooltip :deep(.tt-val.green) {
  color: #52f3c4;
}

.tech-tooltip :deep(.tt-val.orange) {
  color: #ffbf74;
}

.tech-tooltip :deep(.tt-val.red) {
  color: #ff8aa0;
}

.tech-tooltip :deep(.tt-tip) {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(134, 169, 196, 0.18);
  color: #57d7ff;
}

:global(.scene-label) {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(87, 215, 255, 0.24);
  background: rgba(6, 18, 34, 0.82);
  color: #eaf7ff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  backdrop-filter: blur(8px);
  box-shadow: 0 0 18px rgba(43, 207, 255, 0.12);
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
    padding: 14px 18px 18px;
  }
}
</style>
