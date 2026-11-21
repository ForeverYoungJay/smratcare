import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DObject, CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import type { TwinFloor, TwinInterior, TwinRoom } from '../../api/digitalTwin'
import { webglAvailable } from './twinScene'

/**
 * 楼栋内部三维：楼栋 = 真实楼层叠放；楼层 = 该层真实房间体块；房间 = 镜头飞入并标出床位与入住长者。
 * 楼层、房间、床位全部来自主数据；体块的平面位置只是按房号顺序排布的示意，不代表实测位置。
 */
export type InteriorPick = { kind: 'floor' | 'room'; key: string }

export interface InteriorSceneHandle {
  showBuilding: (interior: TwinInterior, activeFloorKey?: string | null) => void
  showFloor: (floor: TwinFloor, activeRoomId?: string | null) => void
  showRoom: (room: TwinRoom | null) => void
  refocus: () => void
  dispose: () => void
}

const SLAB = { w: 40, d: 22, h: 1.2, gap: 8 }
const CELL = { w: 11, d: 9, gap: 2.4, wall: 3.2 }
// 房间地面用浅色，床位用深色，两者叠在一起也分得清
const ROOM_TONE = { full: 0xa9c5e8, partial: 0xb5dcc0, vacant: 0xdde3e0, none: 0xeeeeee }
const BED_TONE: Record<number, number> = { 0: 0x9e9e9e, 1: 0x6fbf73, 2: 0x2f6fb3, 3: 0xe39a3b }
const BED_LABEL: Record<number, string> = { 0: '停用', 1: '空闲', 2: '已入住', 3: '维修' }

export function roomTone(r: TwinRoom): keyof typeof ROOM_TONE {
  if (!r.bedCount) return 'none'
  if (r.occupiedBeds >= r.bedCount) return 'full'
  return r.occupiedBeds ? 'partial' : 'vacant'
}

/** 房间体块按房号顺序逐行排布：列数取近似方阵，越多房间越宽 */
export function roomGrid(count: number) {
  const cols = Math.max(1, Math.ceil(Math.sqrt(count * 1.8)))
  return { cols, rows: Math.max(1, Math.ceil(count / cols)) }
}

function label(text: string, sub: string, className: string) {
  const div = document.createElement('div')
  div.className = `twin-label ${className}`
  const b = document.createElement('b')
  b.textContent = text
  const s = document.createElement('span')
  s.textContent = sub
  div.append(b, s)
  return new CSS2DObject(div)
}

export function createInteriorScene(container: HTMLElement, onPick: (pick: InteriorPick) => void): InteriorSceneHandle {
  if (!webglAvailable()) throw new Error('WEBGL_UNSUPPORTED')
  const renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  container.appendChild(renderer.domElement)
  const labelRenderer = new CSS2DRenderer()
  labelRenderer.domElement.className = 'twin-label-layer'
  container.appendChild(labelRenderer.domElement)

  const scene = new THREE.Scene()
  scene.background = new THREE.Color(0xeef2f0)
  const camera = new THREE.PerspectiveCamera(40, 1, 0.5, 2000)
  const controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.maxPolarAngle = Math.PI / 2.1
  scene.add(new THREE.HemisphereLight(0xffffff, 0x9aa59a, 1.6))
  const sun = new THREE.DirectionalLight(0xffffff, 1.6)
  sun.position.set(-60, 120, 80)
  scene.add(sun)
  const ground = new THREE.Mesh(new THREE.CircleGeometry(400, 48), new THREE.MeshStandardMaterial({ color: 0xdfe6e1 }))
  ground.rotation.x = -Math.PI / 2
  ground.position.y = -0.05
  scene.add(ground)

  let content = new THREE.Group()
  scene.add(content)
  const pickables: THREE.Object3D[] = []
  let floorMode: { floor: TwinFloor; cells: Map<string, THREE.Group> } | null = null
  let roomLabels: CSS2DObject[] = []
  let home = { position: new THREE.Vector3(70, 60, 80), target: new THREE.Vector3() }

  const clear = () => {
    scene.remove(content)
    content.traverse((o) => {
      const mesh = o as THREE.Mesh
      mesh.geometry?.dispose()
      const mats = Array.isArray(mesh.material) ? mesh.material : mesh.material ? [mesh.material] : []
      mats.forEach((m) => m.dispose())
      if (o instanceof CSS2DObject) o.element.remove()
    })
    content = new THREE.Group()
    scene.add(content)
    pickables.length = 0
    floorMode = null
    roomLabels = []
  }

  let flight: { from: THREE.Vector3; to: THREE.Vector3; fromT: THREE.Vector3; toT: THREE.Vector3; start: number } | null = null
  const flyTo = (position: THREE.Vector3, target: THREE.Vector3) => {
    flight = { from: camera.position.clone(), to: position, fromT: controls.target.clone(), toT: target, start: performance.now() }
  }
  /** 让一个包围盒完整出现在画面里（斜 45° 俯视） */
  const frame = (box: THREE.Box3, tilt = 1) => {
    const sphere = box.getBoundingSphere(new THREE.Sphere())
    // 取横、竖视角里较窄的一个，窄屏也能把整栋 / 整层装进画面；+8 给楼层标签留位置
    const vfov = (camera.fov * Math.PI) / 180
    const hfov = 2 * Math.atan(Math.tan(vfov / 2) * camera.aspect)
    const dist = Math.max(18, ((sphere.radius + 8) / Math.sin(Math.min(vfov, hfov) / 2)) * 1.05)
    const dir = new THREE.Vector3(0.55, 0.6 * tilt, 0.75).normalize()
    home = { position: sphere.center.clone().addScaledVector(dir, dist), target: sphere.center.clone() }
    flyTo(home.position.clone(), home.target.clone())
  }

  const material = (color: number, opacity = 1) =>
    new THREE.MeshStandardMaterial({ color, transparent: opacity < 1, opacity, roughness: 0.75 })

  const handle: InteriorSceneHandle = {
    showBuilding(interior, activeFloorKey) {
      clear()
      const real = interior.floors.filter((f) => f.key !== 'unassigned')
      const pending = interior.floors.find((f) => f.key === 'unassigned')
      real.forEach((f, i) => {
        const active = f.key === activeFloorKey
        const slab = new THREE.Mesh(new THREE.BoxGeometry(SLAB.w, SLAB.h, SLAB.d), material(active ? 0x1f8a74 : 0xf4ede2))
        slab.position.y = i * SLAB.gap + SLAB.h / 2
        slab.userData.pick = { kind: 'floor', key: f.key } satisfies InteriorPick
        const glass = new THREE.Mesh(new THREE.BoxGeometry(SLAB.w - 1, SLAB.gap - SLAB.h, SLAB.d - 1), material(0xbfd6e0, 0.28))
        glass.position.y = i * SLAB.gap + SLAB.h + (SLAB.gap - SLAB.h) / 2
        glass.userData.pick = slab.userData.pick
        const tag = label(f.floorNo, `${f.roomCount} 间 · ${f.occupiedBeds}/${f.bedCount} 床`, active ? 'twin-label--real' : '')
        tag.position.set(SLAB.w / 2 + 4, i * SLAB.gap + SLAB.gap / 2, 0)
        content.add(slab, glass, tag)
        pickables.push(slab, glass)
      })
      if (pending) {
        const slab = new THREE.Mesh(new THREE.BoxGeometry(14, SLAB.h, 10), material(0xd9c9a3))
        slab.position.set(-SLAB.w / 2 - 14, SLAB.h / 2, 0)
        slab.userData.pick = { kind: 'floor', key: pending.key } satisfies InteriorPick
        const tag = label('待归属', `${pending.roomCount} 间未关联楼层`, '')
        tag.position.set(slab.position.x, 4, 0)
        content.add(slab, tag)
        pickables.push(slab)
      }
      frame(new THREE.Box3().setFromObject(content))
    },

    showFloor(floor, activeRoomId) {
      clear()
      const rooms = floor.rooms
      const { cols, rows } = roomGrid(rooms.length)
      const width = cols * CELL.w + (cols + 1) * CELL.gap
      const depth = rows * CELL.d + (rows + 1) * CELL.gap
      const slab = new THREE.Mesh(new THREE.BoxGeometry(width, SLAB.h, depth), material(0xf4ede2))
      slab.position.y = -SLAB.h / 2
      content.add(slab)
      const cells = new Map<string, THREE.Group>()
      rooms.forEach((r, i) => {
        const cell = new THREE.Group()
        const col = i % cols
        const row = Math.floor(i / cols)
        cell.position.set(-width / 2 + CELL.gap + CELL.w / 2 + col * (CELL.w + CELL.gap), 0, -depth / 2 + CELL.gap + CELL.d / 2 + row * (CELL.d + CELL.gap))
        const pick = { kind: 'room', key: r.roomId } satisfies InteriorPick
        const tile = new THREE.Mesh(new THREE.BoxGeometry(CELL.w, 0.3, CELL.d), material(ROOM_TONE[roomTone(r)]))
        tile.position.y = 0.15
        tile.userData.pick = pick
        const walls = new THREE.Mesh(new THREE.BoxGeometry(CELL.w, CELL.wall, CELL.d), material(r.roomId === activeRoomId ? 0x1f8a74 : 0xffffff, 0.22))
        walls.position.y = CELL.wall / 2 + 0.3
        walls.userData.pick = pick
        const edges = new THREE.LineSegments(new THREE.EdgesGeometry(walls.geometry), new THREE.LineBasicMaterial({ color: r.roomId === activeRoomId ? 0x1f8a74 : 0x8fa39b }))
        edges.position.copy(walls.position)
        cell.add(tile, walls, edges)
        // 床位：沿房间进深摆一排，颜色按业务床位状态
        const n = Math.max(1, r.beds.length)
        r.beds.forEach((b, j) => {
          const bed = new THREE.Mesh(new THREE.BoxGeometry(Math.min(2.4, (CELL.w - 2) / n - 0.5), 0.9, 4.2), material(BED_TONE[b.status] ?? 0xbdbdbd))
          bed.position.set(-CELL.w / 2 + 1 + ((CELL.w - 2) / n) * (j + 0.5), 0.75, -CELL.d / 2 + 2.8)
          bed.userData.pick = pick
          bed.userData.bedIndex = j
          cell.add(bed)
          pickables.push(bed)
        })
        const tag = label(r.roomNo, `${r.occupiedBeds}/${r.bedCount} 床`, r.roomId === activeRoomId ? 'twin-label--real' : '')
        tag.position.set(0, CELL.wall + 1.6, 0)
        cell.add(tag)
        content.add(cell)
        pickables.push(tile, walls)
        cells.set(r.roomId, cell)
      })
      floorMode = { floor, cells }
      frame(new THREE.Box3().setFromObject(content), 1.1)
    },

    showRoom(room) {
      roomLabels.forEach((l) => { l.element.remove(); l.parent?.remove(l) })
      roomLabels = []
      const cell = room && floorMode?.cells.get(room.roomId)
      if (!room || !cell) {
        if (floorMode) frame(new THREE.Box3().setFromObject(content), 1.1)
        return
      }
      // 房间级：每张床头上方标出床号与入住长者（服务端只对有权岗位返回长者信息）
      cell.children.filter((c) => c.userData.bedIndex !== undefined).forEach((bedMesh) => {
        const b = room.beds[bedMesh.userData.bedIndex as number]
        const tag = label(b.bedNo, b.elder ? b.elder.name : BED_LABEL[b.status] ?? '未知', b.elder ? 'twin-label--real' : '')
        tag.position.set(bedMesh.position.x, 1.4, bedMesh.position.z + 2.6)
        cell.add(tag)
        roomLabels.push(tag)
      })
      const center = new THREE.Box3().setFromObject(cell).getCenter(new THREE.Vector3())
      home = { position: center.clone().add(new THREE.Vector3(13, 19, 23)), target: center }
      flyTo(home.position.clone(), home.target.clone())
    },

    refocus() {
      flyTo(home.position.clone(), home.target.clone())
    },

    dispose() {
      cancelAnimationFrame(raf)
      observer.disconnect()
      renderer.domElement.removeEventListener('pointerdown', onDown)
      renderer.domElement.removeEventListener('pointerup', onUp)
      renderer.domElement.removeEventListener('pointermove', onMove)
      clear()
      controls.dispose()
      ground.geometry.dispose()
      ;(ground.material as THREE.Material).dispose()
      renderer.dispose()
      renderer.forceContextLoss()
      renderer.domElement.remove()
      labelRenderer.domElement.remove()
    }
  }

  const raycaster = new THREE.Raycaster()
  const pointer = new THREE.Vector2()
  const pick = (e: PointerEvent): InteriorPick | null => {
    const rect = renderer.domElement.getBoundingClientRect()
    pointer.set(((e.clientX - rect.left) / rect.width) * 2 - 1, -((e.clientY - rect.top) / rect.height) * 2 + 1)
    raycaster.setFromCamera(pointer, camera)
    const hit = raycaster.intersectObjects(pickables, false)[0]
    return (hit?.object.userData.pick as InteriorPick | undefined) ?? null
  }
  let downAt: { x: number; y: number } | null = null
  const onDown = (e: PointerEvent) => { downAt = { x: e.clientX, y: e.clientY } }
  const onUp = (e: PointerEvent) => {
    // 拖动旋转不算点击
    if (downAt && Math.hypot(e.clientX - downAt.x, e.clientY - downAt.y) < 5) {
      const p = pick(e)
      if (p) onPick(p)
    }
    downAt = null
  }
  const onMove = (e: PointerEvent) => {
    if (!e.buttons) renderer.domElement.style.cursor = pick(e) ? 'pointer' : ''
  }
  renderer.domElement.addEventListener('pointerdown', onDown)
  renderer.domElement.addEventListener('pointerup', onUp)
  renderer.domElement.addEventListener('pointermove', onMove)

  const resize = () => {
    const { clientWidth: w, clientHeight: h } = container
    if (!w || !h) return
    renderer.setSize(w, h)
    labelRenderer.setSize(w, h)
    camera.aspect = w / h
    camera.updateProjectionMatrix()
  }
  const observer = new ResizeObserver(resize)
  observer.observe(container)
  resize()
  camera.position.copy(home.position)

  let raf = 0
  const loop = () => {
    raf = requestAnimationFrame(loop)
    if (flight) {
      const t = Math.min(1, (performance.now() - flight.start) / 700)
      const k = t < 0.5 ? 2 * t * t : 1 - (-2 * t + 2) ** 2 / 2
      camera.position.lerpVectors(flight.from, flight.to, k)
      controls.target.lerpVectors(flight.fromT, flight.toT, k)
      if (t === 1) flight = null
    }
    controls.update()
    renderer.render(scene, camera)
    labelRenderer.render(scene, camera)
  }
  loop()
  return handle
}
