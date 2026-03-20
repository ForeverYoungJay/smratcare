<template>
  <div class="panorama-container" ref="containerRef">
    <div class="ui-panel">
      <a-space align="center">
        <a-button v-if="currentLevel !== 'PARK'" type="primary" @click="goUpLevel">返回上级</a-button>
        <a-button @click="resetCamera">重置视角</a-button>
        <a-tag color="blue" style="margin-left: 12px">{{ breadcrumbText }}</a-tag>
      </a-space>
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
const currentLevel = ref<'PARK' | 'BUILDING' | 'FLOOR'>('PARK')
const selectedBuilding = ref<string>('')
const selectedFloor = ref<string>('')

const breadcrumbText = computed(() => {
  let text = '园区全景'
  if (selectedBuilding.value) text += ` > ${selectedBuilding.value}`
  if (selectedFloor.value) text += ` > ${selectedFloor.value}`
  return text
})

// Three.js instances
const scene = shallowRef(new THREE.Scene())
const camera = shallowRef<THREE.PerspectiveCamera | null>(null)
const renderer = shallowRef<THREE.WebGLRenderer | null>(null)
const cssRenderer = shallowRef<CSS2DRenderer | null>(null)
const controls = shallowRef<OrbitControls | null>(null)

let animationFrameId = 0
const pickPosition = { x: 0, y: 0 }
const raycaster = new THREE.Raycaster()
let hoveredObj: THREE.Object3D | null = null

const materials = {
  building: new THREE.MeshStandardMaterial({ color: 0x3b82f6, transparent: true, opacity: 0.8, roughness: 0.2, metalness: 0.1 }),
  floor: new THREE.MeshStandardMaterial({ color: 0xe2e8f0, transparent: true, opacity: 0.9 }),
  room: new THREE.MeshStandardMaterial({ color: 0xffffff, transparent: true, opacity: 1.0, roughness: 0.8 }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0x60a5fa }),
  hover: new THREE.MeshStandardMaterial({ color: 0x2563eb, emissive: 0x1e3a8a })
}

const objectsMap = new Map<string, THREE.Object3D>() // key -> mesh or group
const interactableObjects: THREE.Object3D[] = []

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
  renderer.value.setClearColor(0xf1f5f9, 1)
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
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.value.add(ambientLight)
  const dirLight = new THREE.DirectionalLight(0xffffff, 0.8)
  dirLight.position.set(50, 100, 50)
  scene.value.add(dirLight)

  // Ground
  const gridHelper = new THREE.GridHelper(200, 50, 0xcbd5e1, 0xe2e8f0)
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
  div.style.padding = '4px 8px'
  div.style.background = 'rgba(255,255,255,0.9)'
  div.style.color = '#1e293b'
  div.style.borderRadius = '4px'
  div.style.fontSize = '12px'
  div.style.fontWeight = 'bold'
  div.style.pointerEvents = 'none'
  div.style.boxShadow = '0 2px 4px rgba(0,0,0,0.1)'
  return new CSS2DObject(div)
}

function createBedCard(bed: any, roomNo: string) {
  const div = document.createElement('div')
  div.className = 'bed-info-card'
  
  const isAlert = bed.riskLevel === 'HIGH' || bed.status === 0 || bed.abnormalVital24hCount > 0
  if (isAlert) div.classList.add('is-alert')
  
  let statusText = '静卧'
  if (!bed.elderId) statusText = '空闲'
  else if (bed.status === 2) statusText = '维修'
  else if (bed.status === 3) statusText = '清洁中'
  else if (isAlert) statusText = bed.riskLabel || '正在告警'
  
  const heartRate = bed.elderId ? (Math.floor(Math.random() * 20) + 60) : '--' // Mock or use real
  const breathRate = bed.elderId ? (Math.floor(Math.random() * 5) + 16) : '--'

  div.innerHTML = `
    <div class="bed-card-header">
      <div class="bed-card-title">
        <span class="bed-icon">💡</span>
        <span class="elder-name">${bed.elderName || '空床'}</span>
        <span class="room-bed-no">— ${roomNo} — ${bed.bedNo}</span>
      </div>
      <div class="bed-card-more">•••</div>
    </div>
    <div class="bed-card-body">
      <div class="status-graphic">
        <div class="graphic-bed ${bed.elderId ? 'occupied' : 'empty'} ${isAlert ? 'alert' : ''}"></div>
        <div class="graphic-wave"></div>
      </div>
      <div class="status-text ${isAlert ? 'text-alert' : ''}">${statusText}</div>
    </div>
    <div class="bed-card-footer">
      <div class="vitals">
        <span class="vital-item">❤️ ${heartRate}</span>
        <span class="vital-item">🫁 ${breathRate}</span>
        <span class="vital-item wave-anim">||||||||||</span>
      </div>
      <div class="status-badge ${isAlert ? 'badge-alert' : (bed.elderId ? 'badge-normal' : 'badge-idle')}">${bed.elderId ? (isAlert ? '异常' : '静卧') : '空闲'}</div>
    </div>
  `
  
  const cssObj = new CSS2DObject(div)
  // Position it slightly above the bed mesh
  cssObj.position.set(0, 1.5, 0)
  return cssObj
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
        roomMesh.userData = { type: 'roomTile', ref: rGroup }
        rGroup.add(roomMesh)
        interactableObjects.push(roomMesh)

        // Render beds inside the room
        const numBeds = roomItem.beds.length
        if (numBeds > 0) {
          const bCols = Math.ceil(Math.sqrt(numBeds))
          const bedSizeX = (roomSize - 0.4) / bCols
          const bedSizeZ = Math.min(bedSizeX * 1.5, (roomSize - 0.4) / Math.ceil(numBeds / bCols))
          roomItem.beds.forEach((bed: any, bIndex: number) => {
               const bGeo = new THREE.BoxGeometry(bedSizeX * 0.8, 0.4, bedSizeZ * 0.8)
               // Determine bed color
               let bedColor = 0xcbd5e1 // Idle
               if (bed.elderId) bedColor = 0x22c55e // occupied
               else if (bed.status === 2) bedColor = 0xf97316 // maintenance
               else if (bed.status === 3) bedColor = 0x06b6d4 // cleaning
               else if (bed.status === 0) bedColor = 0xef4444 // locked
               else if (String(bed.bedNo || '').endsWith('R')) bedColor = 0x3b82f6 // reserved

               const bMat = new THREE.MeshStandardMaterial({ color: bedColor })
               const bMesh = new THREE.Mesh(bGeo, bMat)
               bMesh.userData = { type: 'bed', bed: bed }

               const rbb = Math.floor(bIndex / bCols)
               const rbc = bIndex % bCols
               const bx = (rbc - (bCols - 1) / 2) * bedSizeX
               const bz = (rbb - Math.ceil(numBeds / bCols - 1) / 2) * bedSizeZ
               bMesh.position.set(bx, 0.4, bz)

               // Attach Bed Info Card
               const bedCard = createBedCard(bed, roomItem.roomNo)
               bedCard.visible = false // Hidden by default, show on FLOOR level
               bedCard.userData = { type: 'bedCard' }
               bMesh.add(bedCard)

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
    hitMesh.userData = { type: 'buildingHitbox', name: buildingName, ref: bGroup }
    bGroup.add(hitMesh)
    interactableObjects.push(hitMesh)
    
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
      } else if (currentLevel.value === 'FLOOR') {
        if (bName === selectedBuilding.value) {
          obj.visible = true
          if (hitbox) hitbox.visible = false
          if (bLabel) bLabel.visible = false
          obj.children.forEach((c) => {
            if (c.userData.type === 'floor') {
              if (c.userData.name === selectedFloor.value) {
                c.visible = true
                gsap.to(c.position, { y: 0, duration: 0.8, ease: "power2.out" })
                // Hide room label, show bed cards
                c.children.forEach(cc => {
                  if (cc.userData.type === 'room') {
                    const rl = cc.children.find(ccc => ccc instanceof CSS2DObject && ccc.element.className.includes('room-label'))
                    if (rl) rl.visible = false

                    cc.children.forEach(roomChild => {
                      if (roomChild.userData.type === 'roomTile') return
                      if (roomChild.userData.type === 'bed') {
                        const bc = roomChild.children.find(bedChild => bedChild.userData?.type === 'bedCard')
                        if (bc) bc.visible = true
                      }
                    })
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
      hoveredObj.position.y -= 0.1 // Reset bed hover bump
    }
    hoveredObj = null
  }

  if (hits.length > 0) {
    const obj = hits[0].object
    if (obj !== hoveredObj) {
      if (obj.userData.type === 'buildingHitbox' || obj.userData.type === 'floorSlab') {
        obj.userData.originalMat = (obj as THREE.Mesh).material
        ;(obj as THREE.Mesh).material = materials.hover
      } else if (obj.userData.type === 'roomTile') {
        ;(obj as THREE.Mesh).material = materials.roomHover
      } else if (obj.userData.type === 'bed') {
        obj.position.y += 0.1 // Bump up bed on hover
      }
      hoveredObj = obj
    }
    renderer.value!.domElement.style.cursor = 'pointer'
  } else {
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
      emit('click-room', hoveredObj.userData.ref.userData.room)
    } else if (hoveredObj.userData.type === 'bed') {
      emit('click-bed', hoveredObj.userData.bed)
    }
  }
}

function zoomToObject(obj: THREE.Object3D, topDown = false) {
  if (!camera.value || !controls.value) return
  const box = new THREE.Box3().setFromObject(obj)
  const center = box.getCenter(new THREE.Vector3())
  const size = box.getSize(new THREE.Vector3())
  
  const maxDim = Math.max(size.x, size.y, size.z)
  const fov = camera.value.fov * (Math.PI / 180)
  let cameraZ = Math.abs(maxDim / 2 / Math.tan(fov / 2)) * 1.5

  gsap.to(camera.value.position, {
    x: topDown ? center.x : center.x + cameraZ * 0.5,
    y: topDown ? center.y + cameraZ : center.y + size.y / 2 + 10,
    z: topDown ? center.z : center.z + cameraZ,
    duration: 1.0,
    ease: 'power3.inOut'
  })
  gsap.to(controls.value.target, {
    x: center.x,
    y: center.y,
    z: center.z,
    duration: 1.0,
    ease: 'power3.inOut'
  })
}

function goUpLevel() {
  if (currentLevel.value === 'FLOOR') {
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
  background: #f1f5f9;
}
.canvas-wrapper {
  width: 100%;
  height: 100%;
}
.ui-panel {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.85);
  padding: 8px 16px;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  backdrop-filter: blur(4px);
}

:deep(.bed-info-card) {
  width: 240px;
  background: linear-gradient(145deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 12px;
  color: #f8fafc;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
  pointer-events: none; /* Let clicks pass through to the 3D mesh */
  transition: transform 0.2s ease, border-color 0.2s ease;
  transform-origin: bottom center;
}

:deep(.bed-info-card.is-alert) {
  border-color: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

:deep(.bed-info-card .bed-card-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #334155;
  padding-bottom: 8px;
  margin-bottom: 12px;
}

:deep(.bed-info-card .bed-card-title) {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

:deep(.bed-info-card .elder-name) {
  font-weight: 600;
  color: #e2e8f0;
}

:deep(.bed-info-card .room-bed-no) {
  color: #94a3b8;
  font-size: 11px;
}

:deep(.bed-info-card .bed-card-more) {
  color: #64748b;
  letter-spacing: 2px;
  font-weight: bold;
}

:deep(.bed-info-card .bed-card-body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80px;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 19px,
    rgba(51, 65, 85, 0.3) 19px,
    rgba(51, 65, 85, 0.3) 20px
  ), repeating-linear-gradient(
    90deg,
    transparent,
    transparent 19px,
    rgba(51, 65, 85, 0.3) 19px,
    rgba(51, 65, 85, 0.3) 20px
  );
  border-radius: 6px;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

:deep(.bed-info-card .status-graphic) {
  position: relative;
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.bed-info-card .graphic-bed) {
  width: 60px;
  height: 30px;
  border-bottom: 4px solid #475569;
  border-left: 4px solid #475569;
  border-right: 4px solid #475569;
  border-radius: 2px 2px 0 0;
  position: relative;
}
:deep(.bed-info-card .graphic-bed::after) {
  content: '';
  position: absolute;
  bottom: 0px;
  left: 4px;
  width: 44px;
  height: 12px;
  background: #3b82f6; /* occupied */
  border-radius: 2px;
}
:deep(.bed-info-card .graphic-bed.empty::after) {
  background: #64748b;
}
:deep(.bed-info-card .graphic-bed.alert::after) {
  background: #ef4444;
}

:deep(.bed-info-card .status-text) {
  font-size: 12px;
  font-weight: bold;
  margin-top: 8px;
  color: #cbd5e1;
}
:deep(.bed-info-card .status-text.text-alert) {
  color: #f87171;
}

:deep(.bed-info-card .bed-card-footer) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.bed-info-card .vitals) {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: #cbd5e1;
}

:deep(.bed-info-card .vital-item.wave-anim) {
  color: #3b82f6;
  font-weight: bold;
  letter-spacing: 1px;
  opacity: 0.8;
}

:deep(.bed-info-card .status-badge) {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}
:deep(.bed-info-card .badge-idle) {
  background: #334155;
  color: #94a3b8;
}
:deep(.bed-info-card .badge-normal) {
  background: #1e3a8a;
  color: #93c5fd;
}
:deep(.bed-info-card .badge-alert) {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.5);
}
</style>
