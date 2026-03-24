<template>
  <div class="panorama-container" ref="containerRef">
    <div class="ui-panel">
      <div class="ui-panel-bg"></div>
      <a-space align="center">
        <a-button v-if="currentLevel !== 'PARK'" class="tech-btn" @click="goUpLevel">返回上级</a-button>
        <a-button class="tech-btn" @click="resetCamera">重置视角</a-button>
        <span class="tech-breadcrumb">{{ breadcrumbText }}</span>
      </a-space>
    </div>
    
    <!-- Hover Tooltip -->
    <div v-show="tooltip.visible" class="tech-tooltip" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
      <div class="tt-header">{{ tooltip.title }}</div>
      <div class="tt-body" v-html="tooltip.content"></div>
    </div>

    <div ref="canvasRef" class="canvas-wrapper"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted, onBeforeUnmount, watch, computed } from 'vue'
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
const selectedBuilding = ref<string>('')
const selectedFloor = ref<string>('')
const selectedRoom = ref<string>('')

const tooltip = ref({ visible: false, x: 0, y: 0, title: '', content: '' })

const breadcrumbText = computed(() => {
  let text = '园区全景'
  if (selectedBuilding.value) text += ` > ${selectedBuilding.value}`
  if (selectedFloor.value) text += ` > ${selectedFloor.value}`
  if (selectedRoom.value) text += ` > 房间 ${selectedRoom.value}`
  return text
})

// Three.js instances
const scene = shallowRef(new THREE.Scene())
const camera = shallowRef<THREE.PerspectiveCamera | null>(null)
const renderer = shallowRef<THREE.WebGLRenderer | null>(null)
const cssRenderer = shallowRef<CSS2DRenderer | null>(null)
const controls = shallowRef<OrbitControls | null>(null)

let animationFrameId = 0
const pickPosition = new THREE.Vector2()
const raycaster = new THREE.Raycaster()
let hoveredObj: THREE.Object3D | null = null

const materials = {
  building: new THREE.MeshStandardMaterial({ color: 0x0284c7, transparent: true, opacity: 0.15, roughness: 0.1, metalness: 0.8, side: THREE.DoubleSide }),
  floor: new THREE.MeshStandardMaterial({ color: 0x0f172a, transparent: true, opacity: 0.85, roughness: 0.5 }),
  room: new THREE.MeshStandardMaterial({ color: 0x0ea5e9, transparent: true, opacity: 0.08, depthWrite: false }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0x38bdf8, transparent: true, opacity: 0.25 }),
  hover: new THREE.MeshStandardMaterial({ color: 0x38bdf8, transparent: true, opacity: 0.3, emissive: 0x0ea5e9, emissiveIntensity: 0.6 }),
  bedIdle: new THREE.MeshStandardMaterial({ color: 0x22c55e, emissive: 0x16a34a, emissiveIntensity: 0.5 }), // Green
  bedOccupied: new THREE.MeshStandardMaterial({ color: 0xef4444, emissive: 0xdc2626, emissiveIntensity: 0.5 }), // Red
  bedReserved: new THREE.MeshStandardMaterial({ color: 0x3b82f6, emissive: 0x2563eb, emissiveIntensity: 0.5 }), // Blue
  bedCleaning: new THREE.MeshStandardMaterial({ color: 0xeab308, emissive: 0xca8a04, emissiveIntensity: 0.5 }), // Yellow
  bedMaintenance: new THREE.MeshStandardMaterial({ color: 0x9ca3af, emissive: 0x6b7280, emissiveIntensity: 0.3 }), // Gray
  bedBlink: new THREE.MeshStandardMaterial({ color: 0xf97316, emissive: 0xf97316, emissiveIntensity: 0.9 }), // Orange
  bedPulse: new THREE.MeshStandardMaterial({ color: 0xef4444, emissive: 0xef4444, emissiveIntensity: 1.0 }) // Red pulse
}

const objectsMap = new Map<string, THREE.Object3D>() // key -> mesh or group
const interactableObjects: THREE.Object3D[] = []
const animatedBeds: { mesh: THREE.Mesh, type: 'blink' | 'pulse' }[] = []

function initScene() {
  if (!canvasRef.value) return

  const width = canvasRef.value.clientWidth
  const height = canvasRef.value.clientHeight

  // Camera
  camera.value = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  camera.value.position.set(40, 40, 60)

  // Renderer
  renderer.value = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.value.setSize(width, height)
  renderer.value.setPixelRatio(window.devicePixelRatio)
  renderer.value.setClearColor(0x020617, 1) // Deep space/slate-950
  canvasRef.value.appendChild(renderer.value.domElement)

  // CSS2DRenderer for Labels
  cssRenderer.value = new CSS2DRenderer()
  cssRenderer.value.setSize(width, height)
  cssRenderer.value.domElement.style.position = 'absolute'
  cssRenderer.value.domElement.style.top = '0px'
  cssRenderer.value.domElement.style.pointerEvents = 'none'
  canvasRef.value.appendChild(cssRenderer.value.domElement)

  // Controls
  controls.value = new OrbitControls(camera.value, renderer.value.domElement)
  controls.value.enableDamping = true
  controls.value.dampingFactor = 0.05
  controls.value.maxPolarAngle = Math.PI / 2 - 0.05 // don't go below ground

  // Lighting
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.4)
  scene.value.add(ambientLight)
  
  const dirLight = new THREE.DirectionalLight(0xe0f2fe, 1.2)
  dirLight.position.set(50, 100, 50)
  scene.value.add(dirLight)
  
  const fillLight = new THREE.DirectionalLight(0x38bdf8, 0.5)
  fillLight.position.set(-50, 20, -50)
  scene.value.add(fillLight)

  // Cyber Ground Grid
  const gridHelper = new THREE.GridHelper(300, 60, 0x1e3a8a, 0x0f172a)
  gridHelper.position.y = -0.1
  scene.value.add(gridHelper)

  // Events
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
  const toRemove: THREE.Object3D[] = []
  scene.value.children.forEach(child => {
    if (child.userData.isGenerated) toRemove.push(child)
  })
  toRemove.forEach(c => scene.value.remove(c))
}

function createLabel(text: string, className: string) {
  const div = document.createElement('div')
  div.className = `3d-label ${className}`
  div.textContent = text
  div.style.padding = '4px 10px'
  div.style.background = 'rgba(15, 23, 42, 0.75)'
  div.style.border = '1px solid rgba(56, 189, 248, 0.4)'
  div.style.color = '#e0f2fe'
  div.style.borderRadius = '4px'
  div.style.fontSize = '12px'
  div.style.fontWeight = 'bold'
  div.style.backdropFilter = 'blur(4px)'
  div.style.pointerEvents = 'none'
  div.style.boxShadow = '0 0 10px rgba(2, 132, 199, 0.3)'
  return new CSS2DObject(div)
}

function buildSceneData() {
  clearScene()
  const parkGroup = new THREE.Group()
  parkGroup.userData.isGenerated = true
  scene.value.add(parkGroup)

  const numBuildings = props.buildings.length
  if (numBuildings === 0) return

  const cols = Math.ceil(Math.sqrt(numBuildings))
  const spacing = 20

  props.buildings.forEach((buildingName, bIndex) => {
    const bGroup = new THREE.Group()
    bGroup.userData = { type: 'building', name: buildingName }
    
    // Layout in grid
    const row = Math.floor(bIndex / cols)
    const col = bIndex % cols
    const bx = (col - (cols - 1) / 2) * spacing
    const bz = (row - Math.ceil(numBuildings / cols - 1) / 2) * spacing
    bGroup.position.set(bx, 0, bz)

    // Floors
    const bFloors = props.floors.filter(f => props.roomLookup.has(`${buildingName}@@${f}`))
    const floorHeight = 3
    const totalFloors = bFloors.length
    
    bFloors.forEach((floorName, fIndex) => {
      const fGroup = new THREE.Group()
      const floorYIndex = totalFloors - 1 - fIndex
      fGroup.userData = { type: 'floor', building: buildingName, name: floorName, floorYIndex }
      fGroup.position.y = floorYIndex * floorHeight
      
      const rooms = props.roomLookup.get(`${buildingName}@@${floorName}`) || []
      
      // Compute floor bounding box based on rooms
      const numRooms = rooms.length
      const rCols = Math.ceil(Math.sqrt(numRooms > 0 ? numRooms : 1))
      const roomSize = 3 // Increased room size to fit beds better
      const roomGap = 0.5
      
      // Floor Slab
      const slabWidth = rCols * (roomSize + roomGap) + 1
      const slabDepth = Math.ceil(numRooms / rCols) * (roomSize + roomGap) + 1
      const slabGeo = new THREE.BoxGeometry(slabWidth, 0.4, slabDepth)
      const slabMesh = new THREE.Mesh(slabGeo, materials.floor)
      slabMesh.position.y = 0.2
      slabMesh.userData = { type: 'floorSlab', building: buildingName, floor: floorName }
      fGroup.add(slabMesh)
      interactableObjects.push(slabMesh)
      objectsMap.set(`floor_${buildingName}_${floorName}`, fGroup)

      // Rooms
      rooms.forEach((roomItem, rIndex) => {
        const rGroup = new THREE.Group()
        rGroup.userData = { type: 'room', building: buildingName, floor: floorName, room: roomItem }
        
        const rr = Math.floor(rIndex / rCols)
        const rc = rIndex % rCols
        const rx = (rc - (rCols - 1) / 2) * (roomSize + roomGap)
        const rz = (rr - Math.ceil(numRooms / rCols - 1) / 2) * (roomSize + roomGap)
        rGroup.position.set(rx, 0.4, rz)

        // Room tile (thin slab)
        const rGeo = new THREE.BoxGeometry(roomSize, 0.2, roomSize)
        const roomMesh = new THREE.Mesh(rGeo, materials.room.clone())
        roomMesh.position.y = 0.1
        roomMesh.userData = { type: 'roomTile', ref: rGroup, data: roomItem, floorName }
        rGroup.add(roomMesh)
        interactableObjects.push(roomMesh)

        // Room wireframe
        const rEdges = new THREE.EdgesGeometry(rGeo)
        const rLine = new THREE.LineSegments(rEdges, new THREE.LineBasicMaterial({ color: 0x0ea5e9, opacity: 0.35, transparent: true }))
        rLine.position.y = 0.1
        rGroup.add(rLine)

        // Render beds inside the room
        const numBeds = roomItem.beds.length
        if (numBeds > 0) {
          const bCols = Math.ceil(Math.sqrt(numBeds))
          const bedSizeX = (roomSize - 0.4) / bCols
          const bedSizeZ = Math.min(bedSizeX * 1.5, (roomSize - 0.4) / Math.ceil(numBeds / bCols))
          roomItem.beds.forEach((bed: any, bIndex: number) => {
               const bGeo = new THREE.BoxGeometry(bedSizeX * 0.8, 0.4, bedSizeZ * 0.8)
               // Determine bed color
               let bedMat = materials.bedIdle
               let animType: 'blink' | 'pulse' | null = null

               const riskLevel = bed.riskLevel || ''
               const bStatus = bed.status || 0
               const isAlert = riskLevel === 'HIGH' || bStatus === 0 || bed.abnormalVital24hCount > 0

               if (isAlert) {
                  animType = 'pulse'
                  bedMat = materials.bedPulse.clone()
               } else if (riskLevel === 'MEDIUM') {
                  animType = 'blink'
                  bedMat = materials.bedBlink.clone()
               } else if (bed.elderId) {
                  bedMat = materials.bedOccupied
               } else if (bStatus === 2) {
                  bedMat = materials.bedMaintenance
               } else if (bStatus === 3) {
                  bedMat = materials.bedCleaning
               } else if (String(bed.bedNo || '').endsWith('R')) {
                  bedMat = materials.bedReserved
               }

               const bMesh = new THREE.Mesh(bGeo, bedMat)
               bMesh.userData = { type: 'bed', bed: bed, originalMat: bedMat }
               if (animType) animatedBeds.push({ mesh: bMesh, type: animType })

               const rbb = Math.floor(bIndex / bCols)
               const rbc = bIndex % bCols
               const bx = (rbc - (bCols - 1) / 2) * bedSizeX
               const bz = (rbb - Math.ceil(numBeds / bCols - 1) / 2) * bedSizeZ
               bMesh.position.set(bx, 0.4, bz)

               rGroup.add(bMesh)
               interactableObjects.push(bMesh)
          })
        }
        
        // Add room label (for building view)
        const rLabel = createLabel(`${roomItem.roomNo} (${roomItem.occupiedBeds}/${roomItem.totalBeds})`, 'room-label')
        rLabel.position.set(0, 1.5, 0)
        rLabel.visible = false // hidden initially
        rGroup.add(rLabel)

        fGroup.add(rGroup)
      })

      // Floor Label
      const fLabel = createLabel(floorName, 'floor-label')
      fLabel.position.set(slabWidth/2 + 1, 0, 0)
      fLabel.visible = false
      fGroup.add(fLabel)

      bGroup.add(fGroup)
    })

    // Building Hitbox (for clicking at PARK level)
    const totalHeight = Math.max(totalFloors * floorHeight, floorHeight)
    const hitGeo = new THREE.BoxGeometry(10, totalHeight, 10)
    const hitMesh = new THREE.Mesh(hitGeo, materials.building.clone())
    hitMesh.position.y = totalHeight / 2
    hitMesh.userData = { 
      type: 'buildingHitbox', 
      name: buildingName, 
      ref: bGroup,
      totalFloors,
      totalRooms: bFloors.reduce((acc, f) => acc + (props.roomLookup.get(`${buildingName}@@${f}`)?.length || 0), 0)
    }
    bGroup.add(hitMesh)
    interactableObjects.push(hitMesh)
    
    // Building Wireframe Glow
    const bEdges = new THREE.EdgesGeometry(hitGeo)
    const bLine = new THREE.LineSegments(bEdges, new THREE.LineBasicMaterial({ color: 0x38bdf8, opacity: 0.7, transparent: true }))
    bLine.position.y = totalHeight / 2
    bGroup.add(bLine)
    
    // Building Label
    const bLabel = createLabel(buildingName, 'building-label')
    bLabel.position.set(0, totalHeight + 2, 0)
    bGroup.add(bLabel)

    parkGroup.add(bGroup)
    objectsMap.set(`building_${buildingName}`, bGroup)
  })

  updateVisibility()
}

function updateVisibility() {
  objectsMap.forEach((obj, key) => {
    if (key.startsWith('building_')) {
      const bName = obj.userData.name
      const hitbox = obj.children.find(c => c.userData.type === 'buildingHitbox')
      const bLabel = obj.children.find(c => c instanceof CSS2DObject && c.element.className.includes('building-label'))

      if (currentLevel.value === 'PARK') {
        obj.visible = true
        if (hitbox) hitbox.visible = true
        if (bLabel) bLabel.visible = true
        
        // Reset floor positions
        obj.children.forEach(c => {
          if (c.userData.type === 'floor') {
             c.visible = false
             gsap.to(c.position, { y: c.userData.originalY || c.position.y, duration: 0.5 })
          }
        })
      } else if (currentLevel.value === 'BUILDING') {
        if (bName === selectedBuilding.value) {
          obj.visible = true
          if (hitbox) hitbox.visible = false
          if (bLabel) bLabel.visible = false
          // Expand floors
          obj.children.forEach((c) => {
            if (c.userData.type === 'floor') {
              c.visible = true
              c.userData.originalY = c.userData.originalY ?? c.position.y
              gsap.to(c.position, { y: c.userData.floorYIndex * 8, duration: 0.8, ease: "power2.out" })
              // Show floor labels, hide room labels
              c.children.forEach(cc => {
                if (cc.userData.type === 'room') {
                  const rl = cc.children.find(ccc => ccc instanceof CSS2DObject && ccc.element.className.includes('room-label'))
                  if (rl) rl.visible = true
                  
                  // Hide bed cards
                  cc.children.forEach(roomChild => {
                    if (roomChild.userData.type === 'roomTile') return
                    if (roomChild.userData.type === 'bed') {
                      const bc = roomChild.children.find(bedChild => bedChild.userData?.type === 'bedCard')
                      if (bc) bc.visible = false
                    }
                  })
                }
                if (cc instanceof CSS2DObject && cc.element.className.includes('floor-label')) cc.visible = false
              })
            }
          })
        } else {
          obj.visible = false
        }
      } else if (currentLevel.value === 'FLOOR' || currentLevel.value === 'ROOM') {
        if (bName === selectedBuilding.value) {
          obj.visible = true
          if (hitbox) hitbox.visible = false
          if (bLabel) bLabel.visible = false
          obj.children.forEach((c) => {
            if (c.userData.type === 'floor') {
              if (c.userData.name === selectedFloor.value) {
                c.visible = true
                gsap.to(c.position, { y: 0, duration: 0.8, ease: "power2.out" })
                
                c.children.forEach(cc => {
                  if (cc.userData.type === 'room') {
                    const isRoomSelected = currentLevel.value === 'ROOM' && cc.userData.room.roomNo === selectedRoom.value
                    
                    if (currentLevel.value === 'ROOM' && !isRoomSelected) {
                      // Hide other rooms in Room mode
                      cc.visible = false
                    } else {
                      cc.visible = true
                      const rl = cc.children.find(ccc => ccc instanceof CSS2DObject && ccc.element.className.includes('room-label'))
                      if (rl) rl.visible = currentLevel.value === 'FLOOR' // Show label only in Floor view
                    }
                  }
                  if (cc instanceof CSS2DObject && cc.element.className.includes('floor-label')) cc.visible = false
                })
              } else {
                c.visible = false
              }
            }
          })
        } else {
          obj.visible = false
        }
      }
    }
  })
}

function onPointerMove(event: PointerEvent) {
  if (!canvasRef.value) return
  const rect = canvasRef.value.getBoundingClientRect()
  pickPosition.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  pickPosition.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(pickPosition, camera.value!)
  const intersects = raycaster.intersectObjects(interactableObjects, false)
  const hits = intersects.filter(hit => hit.object.visible)
  
  if (hoveredObj && hoveredObj !== hits[0]?.object) {
    if (hoveredObj.userData.type === 'buildingHitbox' || hoveredObj.userData.type === 'floorSlab') {
      (hoveredObj as THREE.Mesh).material = hoveredObj.userData.originalMat
    } else if (hoveredObj.userData.type === 'roomTile') {
      (hoveredObj as THREE.Mesh).material = materials.room
    } else if (hoveredObj.userData.type === 'bed') {
      (hoveredObj as THREE.Mesh).scale.set(1, 1, 1) // Reset scale
      ;(hoveredObj as THREE.Mesh).material = hoveredObj.userData.originalMat;
    }
    hoveredObj = null
  }

  if (hits.length > 0) {
    const obj = hits[0].object
    
    // Hover Tooltip Logic
    tooltip.value.visible = true
    // Smooth follow cursor with slight offset
    tooltip.value.x = Object.is(tooltip.value.x, 0) ? event.clientX + 15 : tooltip.value.x + (event.clientX + 15 - tooltip.value.x) * 0.5
    tooltip.value.y = Object.is(tooltip.value.y, 0) ? event.clientY + 15 : tooltip.value.y + (event.clientY + 15 - tooltip.value.y) * 0.5

    if (obj.userData.type === 'buildingHitbox') {
      tooltip.value.title = obj.userData.name
      tooltip.value.content = `<div class="tt-row"><span>楼层书：</span><span class="tt-val cyan">${obj.userData.totalFloors}层</span></div>
                               <div class="tt-row"><span>总计房间：</span><span class="tt-val">${obj.userData.totalRooms}间</span></div>
                               <div class="tt-tip">点击展开内部结构</div>`
    } else if (obj.userData.type === 'floorSlab') {
      tooltip.value.title = `${obj.userData.building} - ${obj.userData.floor}`
      tooltip.value.content = `<div class="tt-tip">双击/单机下钻至楼层</div>`
    } else if (obj.userData.type === 'roomTile') {
      const rd = obj.userData.data
      tooltip.value.title = `房间：${rd.roomNo || '-'}`
      tooltip.value.content = `<div class="tt-row"><span>楼栋/楼层：</span><span class="tt-val">${obj.userData.ref.userData.building} / ${obj.userData.floorName}</span></div>
                               <div class="tt-row"><span>入住情况：</span><span class="tt-val ${rd.occupiedBeds >= rd.totalBeds ? 'red' : 'green'}">${rd.occupiedBeds}/${rd.totalBeds} 床</span></div>
                               <div class="tt-tip">点击进入房间内部视角</div>`
    } else if (obj.userData.type === 'bed') {
      const bd = obj.userData.bed
      const occStr = bd.elderId ? `已入住 (${bd.elderName})` : '空闲'
      tooltip.value.title = `床位：${bd.bedNo}`
      tooltip.value.content = `<div class="tt-row"><span>状态：</span><span class="tt-val">${occStr}</span></div>
                               <div class="tt-tip">点击查看详细资产档案</div>`
    } else {
      tooltip.value.visible = false
    }

    // Material Highlights
    if (obj !== hoveredObj) {
      if (obj.userData.type === 'buildingHitbox' || obj.userData.type === 'floorSlab') {
        obj.userData.originalMat = (obj as THREE.Mesh).material
        ;(obj as THREE.Mesh).material = materials.hover
      } else if (obj.userData.type === 'roomTile') {
        ;(obj as THREE.Mesh).material = materials.roomHover
      } else if (obj.userData.type === 'bed') {
        (obj as THREE.Mesh).scale.set(1.2, 1.2, 1.2) // Pronounced scale up
        const isAnim = animatedBeds.some(b => b.mesh === obj)
        if (!isAnim) {
           (obj as THREE.Mesh).material = materials.hover 
        }
      }
      hoveredObj = obj
    }
    renderer.value!.domElement.style.cursor = 'pointer'
  } else {
    tooltip.value.visible = false
    renderer.value!.domElement.style.cursor = 'default'
  }
}

function onClick() {
  if (hoveredObj) {
    if (hoveredObj.userData.type === 'buildingHitbox') {
      selectedBuilding.value = hoveredObj.userData.name
      currentLevel.value = 'BUILDING'
      updateVisibility()
      zoomToObject(hoveredObj.userData.ref)
    } else if (hoveredObj.userData.type === 'floorSlab') {
      selectedFloor.value = hoveredObj.userData.floor
      currentLevel.value = 'FLOOR'
      updateVisibility()
      const floorGroup = objectsMap.get(`floor_${selectedBuilding.value}_${selectedFloor.value}`)
      if (floorGroup) zoomToObject(floorGroup, true)
    } else if (hoveredObj.userData.type === 'roomTile') {
      if (currentLevel.value !== 'ROOM') {
        // Drill down to ROOM
        selectedRoom.value = hoveredObj.userData.ref.userData.room.roomNo
        currentLevel.value = 'ROOM'
        updateVisibility()
        zoomToObject(hoveredObj.userData.ref, false, 20) // Zoom closer
      }
    } else if (hoveredObj.userData.type === 'bed') {
      emit('click-bed', hoveredObj.userData.bed)
    }
  } else {
    // Clicked empty space
    goUpLevel();
  }
}

function zoomToObject(obj: THREE.Object3D, topDown = false, overrideZ?: number) {
  if (!camera.value || !controls.value) return
  const box = new THREE.Box3().setFromObject(obj)
  const center = box.getCenter(new THREE.Vector3())
  const size = box.getSize(new THREE.Vector3())
  
  const maxDim = Math.max(size.x, size.y, size.z)
  const fov = camera.value.fov * (Math.PI / 180)
  let cameraZ = overrideZ || (Math.abs(maxDim / 2 / Math.tan(fov / 2)) * 1.5)
  // Give it a more dynamic tech-angle (offset X and Z)
  const offsetX = topDown ? 0 : cameraZ * 0.4
  const offsetZ = topDown ? 0 : cameraZ

  gsap.to(camera.value.position, {
    x: center.x + offsetX,
    y: topDown ? center.y + cameraZ : center.y + size.y / 2 + (overrideZ ? 8 : 12),
    z: center.z + offsetZ,
    duration: 1.2,
    ease: 'power3.inOut'
  })
  gsap.to(controls.value.target, {
    x: center.x,
    y: center.y,
    z: center.z,
    duration: 1.2,
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
  } else if (currentLevel.value === 'FLOOR') {
    currentLevel.value = 'BUILDING'
    selectedFloor.value = ''
    updateVisibility()
    const bGroup = objectsMap.get(`building_${selectedBuilding.value}`)
    if (bGroup) zoomToObject(bGroup)
  } else if (currentLevel.value === 'BUILDING') {
    currentLevel.value = 'PARK'
    selectedBuilding.value = ''
    updateVisibility()
    resetCamera()
  }
}

function resetCamera() {
  if (currentLevel.value !== 'PARK') {
    currentLevel.value = 'PARK'
    selectedBuilding.value = ''
    selectedFloor.value = ''
    updateVisibility()
  }
  gsap.to(camera.value!.position, { x: 40, y: 40, z: 60, duration: 1 })
  gsap.to(controls.value!.target, { x: 0, y: 0, z: 0, duration: 1 })
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

function animate() {
  animationFrameId = requestAnimationFrame(animate)
  if (controls.value) controls.value.update()
  
  // Bed Animations (Pulse / Blink)
  const time = Date.now() * 0.003
  animatedBeds.forEach(item => {
    const mat = item.mesh.material as THREE.MeshStandardMaterial
    if (item.type === 'pulse') {
      const v = (Math.sin(time * 2) + 1) / 2 // 0 to 1
      mat.emissive.setHex(0xef4444)
      mat.emissiveIntensity = v * 0.8
    } else if (item.type === 'blink') {
      const v = Math.sin(time * 3) > 0 ? 1 : 0
      mat.emissive.setHex(0xf97316)
      mat.emissiveIntensity = v * 0.6
    }
  })

  if (renderer.value && camera.value && scene.value) {
    renderer.value.render(scene.value, camera.value)
    cssRenderer.value?.render(scene.value, camera.value)
  }
}

watch(() => props.buildings, buildSceneData, { deep: true })
watch(() => props.floors, buildSceneData, { deep: true })
watch(() => props.roomLookup, buildSceneData, { deep: true })

onMounted(() => {
  initScene()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
  renderer.value?.domElement.removeEventListener('pointermove', onPointerMove)
  renderer.value?.domElement.removeEventListener('click', onClick)
  window.removeEventListener('resize', onWindowResize)
  renderer.value?.dispose()
})
</script>

<style scoped>
.panorama-container {
  width: 100%;
  height: 600px;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #020617; /* Matches clearColor */
  box-shadow: inset 0 0 100px rgba(2, 132, 199, 0.05); /* subtle inner glow */
}
.canvas-wrapper {
  width: 100%;
  height: 100%;
}

/* Cyber UI Panel */
.ui-panel {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid rgba(56, 189, 248, 0.3);
  box-shadow: 0 4px 20px rgba(0,0,0,0.5), inset 0 0 10px rgba(56, 189, 248, 0.1);
  overflow: hidden;
}
.ui-panel-bg {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
  z-index: -1;
}
.tech-btn {
  background: rgba(2, 132, 199, 0.2);
  border: 1px solid rgba(56, 189, 248, 0.5);
  color: #bae6fd;
  font-weight: 600;
  border-radius: 4px;
  transition: all 0.2s;
}
.tech-btn:hover {
  background: rgba(14, 165, 233, 0.4);
  color: #fff;
  border-color: #7dd3fc;
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.4);
}
.tech-breadcrumb {
  color: #7dd3fc;
  font-weight: 700;
  letter-spacing: 1px;
  font-size: 13px;
  margin-left: 12px;
  text-shadow: 0 0 10px rgba(56, 189, 248, 0.5);
}

/* Hover Tooltip */
.tech-tooltip {
  position: fixed;
  z-index: 100;
  pointer-events: none;
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(56, 189, 248, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6), 0 0 15px rgba(2, 132, 199, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 6px;
  padding: 12px;
  min-width: 180px;
  transform: translate(0, 0); /* Let JS position it */
  font-family: 'Inter', sans-serif;
  transition: opacity 0.2s;
}
.tech-tooltip .tt-header {
  font-size: 14px;
  font-weight: 800;
  color: #e0f2fe;
  border-bottom: 1px solid rgba(56, 189, 248, 0.3);
  padding-bottom: 6px;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.tech-tooltip .tt-body {
  font-size: 12px;
  color: #94a3b8;
}
.tech-tooltip :deep(.tt-row) {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
.tech-tooltip :deep(.tt-val) {
  font-weight: bold;
  color: #f8fafc;
}
.tech-tooltip :deep(.tt-val.cyan) { color: #38bdf8; }
.tech-tooltip :deep(.tt-val.green) { color: #4ade80; }
.tech-tooltip :deep(.tt-val.red) { color: #f87171; }
.tech-tooltip :deep(.tt-tip) {
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed rgba(100, 116, 139, 0.3);
  font-size: 11px;
  color: #0ea5e9;
  font-style: italic;
}
</style>
