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
const currentLevel = ref<'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM'>('PARK')
const selectedBuilding = ref<string>('')
const selectedFloor = ref<string>('')
const selectedRoom = ref<string>('')

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
  building: new THREE.MeshStandardMaterial({ color: 0x3b82f6, transparent: true, opacity: 0.8, roughness: 0.2, metalness: 0.1 }),
  floor: new THREE.MeshStandardMaterial({ color: 0xe2e8f0, transparent: true, opacity: 0.9 }),
  room: new THREE.MeshStandardMaterial({ color: 0xffffff, transparent: true, opacity: 1.0, roughness: 0.8 }),
  roomHover: new THREE.MeshStandardMaterial({ color: 0x60a5fa }),
  hover: new THREE.MeshStandardMaterial({ color: 0x2563eb, emissive: 0x1e3a8a }),
  bedIdle: new THREE.MeshStandardMaterial({ color: 0x22c55e }), // Green
  bedOccupied: new THREE.MeshStandardMaterial({ color: 0xef4444 }), // Red
  bedReserved: new THREE.MeshStandardMaterial({ color: 0x3b82f6 }), // Blue
  bedCleaning: new THREE.MeshStandardMaterial({ color: 0xeab308 }), // Yellow
  bedMaintenance: new THREE.MeshStandardMaterial({ color: 0x9ca3af }), // Gray
  bedBlink: new THREE.MeshStandardMaterial({ color: 0xf97316 }), // Orange
  bedPulse: new THREE.MeshStandardMaterial({ color: 0xef4444 }) // Red pulse
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
    if (obj !== hoveredObj) {
      if (obj.userData.type === 'buildingHitbox' || obj.userData.type === 'floorSlab') {
        obj.userData.originalMat = (obj as THREE.Mesh).material
        ;(obj as THREE.Mesh).material = materials.hover
      } else if (obj.userData.type === 'roomTile') {
        ;(obj as THREE.Mesh).material = materials.roomHover
      } else if (obj.userData.type === 'bed') {
        (obj as THREE.Mesh).scale.set(1.1, 1.1, 1.1) // Slight scale up
        // Do not alter material if it's animating so we don't break the animation,
        // but for idle/occupied we can add a slight emissive.
        const isAnim = animatedBeds.some(b => b.mesh === obj)
        if (!isAnim) {
           (obj as THREE.Mesh).material = materials.hover // Use hover material
        }
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

  gsap.to(camera.value.position, {
    x: topDown ? center.x : center.x + cameraZ * 0.5,
    y: topDown ? center.y + cameraZ : center.y + size.y / 2 + (overrideZ ? 5 : 10),
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
</style>
