<template>
  <div class="panorama-container">
    <div class="ui-panel">
      <a-space align="center">
        <a-button v-if="store.currentLevel !== 'PARK'" type="primary" @click="store.goUpLevel()">返回上级</a-button>
        <a-button @click="resetCamera">重置视角</a-button>
        <a-tag color="blue" style="margin-left: 12px">{{ breadcrumbText }}</a-tag>
      </a-space>
    </div>

    <!-- TresJS Canvas -->
    <TresCanvas clear-color="#f1f5f9" window-size>
      <TresPerspectiveCamera
        ref="cameraRef"
        :position="[40, 40, 60]"
        :look-at="[0, 0, 0]"
      />
      <OrbitControls 
        ref="controlsRef" 
        :enable-damping="true" 
        :damping-factor="0.05" 
        :max-polar-angle="1.52" 
      />
      
      <TresAmbientLight :intensity="0.6" />
      <TresDirectionalLight :position="[50, 100, 50]" :intensity="0.8" />
      <TresGridHelper :args="[200, 50, 0xcbd5e1, 0xe2e8f0]" />

      <!-- Buildings -->
      <TresGroup v-for="b in parsedBuildings" :key="b.name" :position="b.position">
        
        <!-- Building Hitbox (for click zoom at PARK level) -->
        <TresMesh
           v-if="store.currentLevel === 'PARK'"
           :position="b.hitboxPosition"
           @click="clickBuilding(b)"
           @pointer-enter="hoverBuilding = b.name"
           @pointer-leave="hoverBuilding = ''"
        >
          <TresBoxGeometry :args="b.hitboxSize" />
          <TresMeshStandardMaterial 
            :color="hoverBuilding === b.name ? '#2563eb' : '#3b82f6'" 
            :transparent="true" 
            :opacity="0.8" 
          />
        </TresMesh>

        <!-- Floors Loop -->
        <TresGroup 
           v-for="f in b.floors" 
           :key="f.name" 
           :position-y="floorYOffsets[`${b.name}_${f.name}`] ?? (f.floorYIndex * 3)"
           v-show="store.currentLevel === 'PARK' || (store.selectedBuilding === b.name && (store.currentLevel === 'BUILDING' || store.selectedFloor === f.name))"
        >
          <!-- Floor Slab Mesh -->
          <TresMesh
            :position="[0, 0.2, 0]"
            @click="clickFloor(b, f)"
            @pointer-enter="hoverFloor = f.name"
            @pointer-leave="hoverFloor = ''"
          >
            <TresBoxGeometry :args="[f.slabWidth, 0.4, f.slabDepth]" />
            <TresMeshStandardMaterial 
              :color="hoverFloor === f.name ? '#2563eb' : '#e2e8f0'" 
              :transparent="true" 
              :opacity="0.9" 
            />
          </TresMesh>

          <!-- Rooms Loop -->
          <TresGroup 
            v-for="r in f.rooms" 
            :key="r.roomNo" 
            :position="[r.x, 0.4, r.z]"
            v-show="store.currentLevel !== 'ROOM' || store.selectedRoom === r.roomNo"
          >
             <!-- Room Tile Mesh -->
             <TresMesh 
               :position="[0, 0.1, 0]" 
               @click="clickRoom(b, f, r)" 
               @pointer-enter="hoverRoom = r.roomNo" 
               @pointer-leave="hoverRoom = ''"
             >
               <TresBoxGeometry :args="[r.size, 0.2, r.size]" />
               <TresMeshStandardMaterial :color="hoverRoom === r.roomNo ? '#60a5fa' : '#ffffff'" />
             </TresMesh>

             <!-- Beds Loop -->
             <TresGroup v-for="bed in r.beds" :key="bed.id" :position="[bed.x, 0.4, bed.z]">
                <TresMesh 
                   @click.stop="clickBed(bed.raw)" 
                   @pointer-enter="hoverBed = bed.id" 
                   @pointer-leave="hoverBed = ''"
                   :scale="hoverBed === bed.id ? [1.1, 1.1, 1.1] : [1, 1, 1]"
                >
                   <TresBoxGeometry :args="[bed.sizeX, 0.4, bed.sizeZ]" />
                   <TresMeshStandardMaterial 
                      :color="bed.color" 
                      :emissive="bed.animType ? bed.emissiveColor : '#000000'" 
                      :emissiveIntensity="getBedEmissiveIntensity(bed.animType, time)"
                   />
                </TresMesh>
             </TresGroup>
          </TresGroup>
        </TresGroup>
      </TresGroup>

    </TresCanvas>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive, onMounted, onBeforeUnmount } from 'vue'
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import { usePanoramaStore } from '../../../store/panorama3d'
import gsap from 'gsap'

const props = defineProps<{
  buildings: string[]
  floors: string[]
  roomLookup: Map<string, any[]>
}>()

const emit = defineEmits(['click-room', 'click-bed'])
const store = usePanoramaStore()

// State for hovers
const hoverBuilding = ref('')
const hoverFloor = ref('')
const hoverRoom = ref('')
const hoverBed = ref('')

const breadcrumbText = computed(() => {
  let text = '园区全景'
  if (store.selectedBuilding) text += ` > ${store.selectedBuilding}`
  if (store.selectedFloor) text += ` > ${store.selectedFloor}`
  if (store.selectedRoom) text += ` > 房间 ${store.selectedRoom}`
  return text
})

// Time loop for animations (pulsing and blinking beds)
const time = ref(0)
let animationFrameId = 0

function animateLoop() {
  time.value = performance.now() * 0.001
  animationFrameId = requestAnimationFrame(animateLoop)
}

onMounted(() => {
  animateLoop()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationFrameId)
})

function getBedEmissiveIntensity(animType: string | null, t: number) {
  if (animType === 'pulse') return (Math.sin(t * 2) + 1) / 2 * 0.8
  if (animType === 'blink') return Math.sin(t * 3) > 0 ? 0.6 : 0
  return 0
}

// Compute the massive static layout structure so Vue template is simple
const parsedBuildings = computed(() => {
  const numBuildings = props.buildings.length
  if (numBuildings === 0) return []
  const cols = Math.ceil(Math.sqrt(numBuildings))
  const spacing = 20
  
  return props.buildings.map((buildingName, bIndex) => {
    const row = Math.floor(bIndex / cols)
    const col = bIndex % cols
    const bx = (col - (cols - 1) / 2) * spacing
    const bz = (row - Math.ceil(numBuildings / cols - 1) / 2) * spacing

    const bFloors = props.floors.filter(f => props.roomLookup.has(`${buildingName}@@${f}`))
    const floorHeight = 3
    const totalFloors = bFloors.length

    const floors = bFloors.map((floorName, fIndex) => {
      const floorYIndex = totalFloors - 1 - fIndex
      const roomsRaw = props.roomLookup.get(`${buildingName}@@${floorName}`) || []
      
      const numRooms = roomsRaw.length
      const rCols = Math.ceil(Math.sqrt(numRooms > 0 ? numRooms : 1))
      const roomSize = 3
      const roomGap = 0.5
      const slabWidth = rCols * (roomSize + roomGap) + 1
      const slabDepth = Math.ceil(numRooms / rCols) * (roomSize + roomGap) + 1
      
      const rooms = roomsRaw.map((roomItem: any, rIndex: number) => {
        const rr = Math.floor(rIndex / rCols)
        const rc = rIndex % rCols
        const rx = (rc - (rCols - 1) / 2) * (roomSize + roomGap)
        const rz = (rr - Math.ceil(numRooms / rCols - 1) / 2) * (roomSize + roomGap)
        
        const numBeds = roomItem.beds.length
        let beds: any[] = []
        if (numBeds > 0) {
          const bCols = Math.ceil(Math.sqrt(numBeds))
          const bedSizeX = (roomSize - 0.4) / bCols
          const bedSizeZ = Math.min(bedSizeX * 1.5, (roomSize - 0.4) / Math.ceil(numBeds / bCols))
          beds = roomItem.beds.map((bed: any, bIndex: number) => {
             const rbb = Math.floor(bIndex / bCols)
             const rbc = bIndex % bCols
             const bedX = (rbc - (bCols - 1) / 2) * bedSizeX
             const bedZ = (rbb - Math.ceil(numBeds / bCols - 1) / 2) * bedSizeZ
             
             let bedColor = '#22c55e' // idle (green)
             let animType = null
             let emissiveColor = '#000000'

             const riskLevel = bed.riskLevel || ''
             const bStatus = bed.status || 0
             const isAlert = riskLevel === 'HIGH' || bStatus === 0 || bed.abnormalVital24hCount > 0

             if (isAlert) {
                  animType = 'pulse'
                  emissiveColor = '#ef4444'
                  bedColor = '#ef4444'
             } else if (riskLevel === 'MEDIUM') {
                  animType = 'blink'
                  emissiveColor = '#f97316'
                  bedColor = '#f97316'
             } else if (bed.elderId) {
                  bedColor = '#ef4444'
             } else if (bStatus === 2) {
                  bedColor = '#9ca3af'
             } else if (bStatus === 3) {
                  bedColor = '#eab308'
             } else if (String(bed.bedNo || '').endsWith('R')) {
                  bedColor = '#3b82f6'
             }

             return {
               id: bed.id || `${rIndex}-${bIndex}`,
               x: bedX, z: bedZ,
               sizeX: bedSizeX * 0.8, sizeZ: bedSizeZ * 0.8,
               color: bedColor,
               animType, emissiveColor,
               raw: bed
             }
          })
        }
        
        return { roomNo: roomItem.roomNo, x: rx, z: rz, size: roomSize, beds, raw: roomItem }
      })
      
      return { 
        name: floorName, 
        floorYIndex, 
        slabWidth, slabDepth, 
        rooms,
        center: [bx, floorYIndex * floorHeight, bz]
      }
    })

    const totalHeight = Math.max(totalFloors * floorHeight, floorHeight)
    return {
      name: buildingName,
      position: [bx, 0, bz],
      height: totalHeight,
      hitboxPosition: [0, totalHeight / 2, 0],
      hitboxSize: [10, totalHeight, 10],
      floors,
      totalFloors
    }
  })
})

// Camera & Transitions logic
const cameraRef = ref<any>(null)
const controlsRef = ref<any>(null)
const floorYOffsets = reactive<Record<string, number>>({})

// Watch the store level to handle floor expanding/collapsing using GSAP
watch(() => store.currentLevel, (newVal) => {
   parsedBuildings.value.forEach(b => {
     b.floors.forEach(f => {
       const key = `${b.name}_${f.name}`
       let targetY = f.floorYIndex * 3 // default packed state
       if (newVal === 'BUILDING' && store.selectedBuilding === b.name) {
           targetY = f.floorYIndex * 8 // exploded view
       } else if ((newVal === 'FLOOR' || newVal === 'ROOM') && store.selectedBuilding === b.name && store.selectedFloor === f.name) {
           targetY = 0 // dropped to floor view
       }
       // Only animate if value exists, otherwise set it immediately on init
       if (floorYOffsets[key] === undefined) {
          floorYOffsets[key] = targetY
       } else {
          gsap.to(floorYOffsets, { [key]: targetY, duration: 0.8, ease: "power2.out" })
       }
     })
   })
}, { immediate: true })

function zoomTo(targetCenter: number[], size: number[], topDown = false, overrideZ?: number) {
  if (!cameraRef.value || !controlsRef.value) return
  const cam = cameraRef.value?.instance || cameraRef.value;
  const ctrl = controlsRef.value?.instance || controlsRef.value?.controls || controlsRef.value;
  
  const [cx, cy, cz] = targetCenter
  const [sx, sy, sz] = size
  const maxDim = Math.max(sx, sy, sz)
  const fov = 45 * (Math.PI / 180)
  let cameraZ = overrideZ || (Math.abs(maxDim / 2 / Math.tan(fov / 2)) * 1.5)

  gsap.to(cam.position, {
    x: topDown ? cx : cx + cameraZ * 0.5,
    y: topDown ? cy + cameraZ : cy + sy / 2 + (overrideZ ? 5 : 10),
    z: topDown ? cz : cz + cameraZ,
    duration: 1.0,
    ease: 'power3.inOut'
  })
  
  gsap.to(ctrl.target, {
    x: cx,
    y: cy,
    z: cz,
    duration: 1.0,
    ease: 'power3.inOut'
  })
}

function clickBuilding(b: any) {
  store.setLevel('BUILDING')
  store.selectedBuilding = b.name
  zoomTo([b.position[0], b.height / 2, b.position[2]], b.hitboxSize)
}

function clickFloor(b: any, f: any) {
  store.setLevel('FLOOR')
  store.selectedFloor = f.name
  const targetY = store.currentLevel === 'BUILDING' ? f.floorYIndex * 8 : 0 
  zoomTo([b.position[0], targetY, b.position[2]], [f.slabWidth, 1, f.slabDepth], true)
}

function clickRoom(b: any, f: any, r: any) {
  if (store.currentLevel !== 'ROOM') {
    store.setLevel('ROOM')
    store.selectedRoom = r.roomNo
    zoomTo([b.position[0] + r.x, 0, b.position[2] + r.z], [r.size, 1, r.size], false, 20)
  }
}

function clickBed(bedData: any) {
  emit('click-bed', bedData)
}

function resetCamera() {
  store.resetCamera()
  const cam = cameraRef.value?.instance || cameraRef.value;
  const ctrl = controlsRef.value?.instance || controlsRef.value?.controls || controlsRef.value;
  if (!cam || !ctrl) return
  gsap.to(cam.position, { x: 40, y: 40, z: 60, duration: 1 })
  gsap.to(ctrl.target, { x: 0, y: 0, z: 0, duration: 1 })
}
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
