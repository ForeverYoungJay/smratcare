import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { CSS2DObject, CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import { CONCEPT_HEIGHT, CONCEPT_LAYOUT } from './twinModel'

export type TwinLayer = 'buildings' | 'roads' | 'vegetation' | 'labels'
export type TwinView = 'reset' | 'top'

export interface TwinSceneOptions {
  modelUrl: string | null
  /** modelNodeKey → 标签文字 */
  labels: Record<string, string>
  onSelect: (nodeKey: string | null) => void
}

export interface TwinSceneHandle {
  nodeKeys: string[]
  highlight: (nodeKey: string | null) => void
  focus: (nodeKey: string) => void
  setView: (view: TwinView) => void
  setLayer: (layer: TwinLayer, visible: boolean) => void
  dispose: () => void
}

const COLORS = {
  wall: 0xf1e9dc, roof: 0xc0583f, ground: 0xb7cfa4, road: 0xdcd6c8, water: 0x8fb8c4,
  hill: 0x9db39a, tree: 0x5f8f5a, highlight: 0x21705f, sky: 0xeef3ef
}
const HOME = { position: new THREE.Vector3(150, 130, 170), target: new THREE.Vector3(0, 0, 0) }

export function webglAvailable(): boolean {
  try {
    const canvas = document.createElement('canvas')
    return Boolean(window.WebGLRenderingContext && (canvas.getContext('webgl2') || canvas.getContext('webgl')))
  } catch {
    return false
  }
}

/** 一栋程序化概念楼：浅色墙体 + 红色双坡屋顶；体量统一，不代表实际层数 */
function conceptBuilding(w: number, d: number): THREE.Group {
  const group = new THREE.Group()
  const body = new THREE.Mesh(new THREE.BoxGeometry(w, CONCEPT_HEIGHT, d), new THREE.MeshStandardMaterial({ color: COLORS.wall }))
  body.position.y = CONCEPT_HEIGHT / 2
  const ridge = Math.min(w, d) * 0.35
  const span = Math.min(w, d) / 2 + 1
  const shape = new THREE.Shape([new THREE.Vector2(-span, 0), new THREE.Vector2(span, 0), new THREE.Vector2(0, ridge)])
  const length = Math.max(w, d) + 2
  const roofGeo = new THREE.ExtrudeGeometry(shape, { depth: length, bevelEnabled: false })
  roofGeo.translate(0, 0, -length / 2)
  const roof = new THREE.Mesh(roofGeo, new THREE.MeshStandardMaterial({ color: COLORS.roof }))
  roof.position.y = CONCEPT_HEIGHT
  if (w > d) roof.rotation.y = Math.PI / 2
  for (const mesh of [body, roof]) { mesh.castShadow = true; mesh.receiveShadow = true }
  group.add(body, roof)
  return group
}

function flat(w: number, d: number, color: number, y: number): THREE.Mesh {
  const mesh = new THREE.Mesh(new THREE.PlaneGeometry(w, d), new THREE.MeshStandardMaterial({ color }))
  mesh.rotation.x = -Math.PI / 2
  mesh.position.y = y
  mesh.receiveShadow = true
  return mesh
}

/** 环境：地面、道路、水体、远山、实例化树木（都是示意，不对应真实位置） */
function conceptEnvironment(layers: Record<TwinLayer, THREE.Group>) {
  layers.buildings.add(flat(420, 360, COLORS.ground, 0))
  const roads = [[0, 22, 220, 8], [0, -26, 220, 8], [-100, 0, 8, 150], [100, 0, 8, 150], [0, 90, 280, 10]]
  roads.forEach(([x, z, w, d]) => { const r = flat(w, d, COLORS.road, 0.05); r.position.x = x; r.position.z = z; layers.roads.add(r) })
  const water = new THREE.Mesh(new THREE.CircleGeometry(26, 40), new THREE.MeshStandardMaterial({ color: COLORS.water }))
  water.rotation.x = -Math.PI / 2
  water.position.set(-140, 0.08, 60)
  layers.vegetation.add(water)
  const hillMat = new THREE.MeshStandardMaterial({ color: COLORS.hill, flatShading: true })
  ;[[-150, -230, 60, 70], [-40, -250, 55, 95], [70, -240, 55, 60], [180, -215, 45, 80]].forEach(([x, z, r, h]) => {
    const hill = new THREE.Mesh(new THREE.ConeGeometry(r, h, 7), hillMat)
    hill.position.set(x, h / 2, z)
    layers.vegetation.add(hill)
  })
  const treeGeo = new THREE.ConeGeometry(3, 9, 6)
  const trees = new THREE.InstancedMesh(treeGeo, new THREE.MeshStandardMaterial({ color: COLORS.tree, flatShading: true }), 90)
  const m = new THREE.Matrix4()
  let seed = 7
  const rand = () => ((seed = (seed * 9301 + 49297) % 233280) / 233280)
  for (let i = 0; i < 90; i++) {
    const edge = i % 4
    const t = rand() * 2 - 1
    const x = edge < 2 ? t * 120 : (edge === 2 ? -112 : 112) + rand() * 6
    const z = edge < 2 ? (edge === 0 ? 74 : -78) + rand() * 6 : t * 75
    m.makeTranslation(x, 4.5, z)
    trees.setMatrixAt(i, m)
  }
  trees.castShadow = true
  layers.vegetation.add(trees)
}

export async function createTwinScene(container: HTMLElement, options: TwinSceneOptions): Promise<TwinSceneHandle> {
  if (!webglAvailable()) throw new Error('WEBGL_UNSUPPORTED')
  // 先加载模型再建渲染器：加载失败时不留下半初始化的 WebGL 资源
  const gltf = options.modelUrl ? await new GLTFLoader().loadAsync(options.modelUrl) : null
  const renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  container.appendChild(renderer.domElement)
  const labelRenderer = new CSS2DRenderer()
  labelRenderer.domElement.className = 'twin-label-layer'
  container.appendChild(labelRenderer.domElement)

  const scene = new THREE.Scene()
  scene.background = new THREE.Color(COLORS.sky)
  scene.fog = new THREE.Fog(COLORS.sky, 320, 620)
  const camera = new THREE.PerspectiveCamera(45, 1, 1, 2000)
  camera.position.copy(HOME.position)
  const controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.maxPolarAngle = Math.PI / 2.1
  controls.minDistance = 30
  controls.maxDistance = 520
  controls.target.copy(HOME.target)

  scene.add(new THREE.HemisphereLight(0xffffff, 0x8a9a80, 1.4))
  const sun = new THREE.DirectionalLight(0xffffff, 1.6)
  sun.position.set(120, 200, 80)
  sun.castShadow = true
  sun.shadow.mapSize.set(2048, 2048)
  Object.assign(sun.shadow.camera, { left: -180, right: 180, top: 180, bottom: -180 })
  scene.add(sun)

  const layers = { buildings: new THREE.Group(), roads: new THREE.Group(), vegetation: new THREE.Group(), labels: new THREE.Group() }
  Object.values(layers).forEach((g) => scene.add(g))
  const pickables = new Map<string, THREE.Object3D>()

  if (gltf) {
    // 有已发布 GLB 时只认节点名等于 modelNodeKey 的对象为可点击楼栋
    layers.buildings.add(gltf.scene)
    gltf.scene.traverse((o) => { if (options.labels[o.name]) pickables.set(o.name, o) })
  } else {
    conceptEnvironment(layers)
    for (const [key, f] of Object.entries(CONCEPT_LAYOUT)) {
      if (!options.labels[key]) continue
      const building = conceptBuilding(f.w, f.d)
      building.position.set(f.x, 0, f.z)
      building.rotation.y = f.rot ?? 0
      building.name = key
      layers.buildings.add(building)
      pickables.set(key, building)
    }
  }
  for (const [key, obj] of pickables) {
    const div = document.createElement('div')
    div.className = 'twin-label'
    div.textContent = options.labels[key]
    const label = new CSS2DObject(div)
    const box = new THREE.Box3().setFromObject(obj)
    label.position.set((box.min.x + box.max.x) / 2, box.max.y + 4, (box.min.z + box.max.z) / 2)
    layers.labels.add(label)
  }

  // ---------- 选择 / 高亮 / 聚焦 ----------
  let selected: string | null = null
  let hovered: string | null = null
  // 每栋楼用自己的材质副本（GLB 里材质可能共享），记下原色，选中时把颜色推向品牌青绿
  const tealColor = new THREE.Color(COLORS.highlight)
  const replacedMaterials = new Set<THREE.Material>()
  for (const obj of pickables.values()) {
    obj.traverse((o) => {
      const mesh = o as THREE.Mesh
      if (!(mesh.material instanceof THREE.MeshStandardMaterial)) return
      replacedMaterials.add(mesh.material)
      const own = mesh.material.clone()
      mesh.material = own
      mesh.userData.baseColor = own.color.clone()
    })
  }
  const tint = (key: string | null, amount: number) => {
    if (!key) return
    pickables.get(key)?.traverse((o) => {
      const mesh = o as THREE.Mesh
      const base = mesh.userData.baseColor as THREE.Color | undefined
      if (base && mesh.material instanceof THREE.MeshStandardMaterial) mesh.material.color.lerpColors(base, tealColor, amount)
    })
  }
  const repaint = () => {
    for (const key of pickables.keys()) tint(key, 0)
    tint(hovered, 0.3)
    tint(selected, 0.65)
  }
  let flight: { from: THREE.Vector3; to: THREE.Vector3; fromT: THREE.Vector3; toT: THREE.Vector3; start: number } | null = null
  const flyTo = (position: THREE.Vector3, target: THREE.Vector3) => {
    flight = { from: camera.position.clone(), to: position, fromT: controls.target.clone(), toT: target, start: performance.now() }
  }

  const raycaster = new THREE.Raycaster()
  const pointer = new THREE.Vector2()
  const pick = (event: PointerEvent | MouseEvent): string | null => {
    const rect = renderer.domElement.getBoundingClientRect()
    pointer.set(((event.clientX - rect.left) / rect.width) * 2 - 1, -((event.clientY - rect.top) / rect.height) * 2 + 1)
    raycaster.setFromCamera(pointer, camera)
    for (const hit of raycaster.intersectObjects([...pickables.values()], true)) {
      for (let o: THREE.Object3D | null = hit.object; o; o = o.parent) if (pickables.has(o.name)) return o.name
    }
    return null
  }
  let downAt: { x: number; y: number } | null = null
  const onDown = (e: PointerEvent) => { downAt = { x: e.clientX, y: e.clientY } }
  const onUp = (e: PointerEvent) => {
    // 拖动旋转不算点击
    if (downAt && Math.hypot(e.clientX - downAt.x, e.clientY - downAt.y) < 5) options.onSelect(pick(e))
    downAt = null
  }
  const onMove = (e: PointerEvent) => {
    if (e.buttons) return
    const key = pick(e)
    if (key !== hovered) { hovered = key; renderer.domElement.style.cursor = key ? 'pointer' : ''; repaint() }
  }
  const onDouble = (e: MouseEvent) => { const key = pick(e); if (key) { options.onSelect(key); handle.focus(key) } }
  renderer.domElement.addEventListener('pointerdown', onDown)
  renderer.domElement.addEventListener('pointerup', onUp)
  renderer.domElement.addEventListener('pointermove', onMove)
  renderer.domElement.addEventListener('dblclick', onDouble)

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

  let frame = 0
  const loop = () => {
    frame = requestAnimationFrame(loop)
    if (flight) {
      const t = Math.min(1, (performance.now() - flight.start) / 700)
      const k = t * (2 - t)
      camera.position.lerpVectors(flight.from, flight.to, k)
      controls.target.lerpVectors(flight.fromT, flight.toT, k)
      if (t === 1) flight = null
    }
    controls.update()
    renderer.render(scene, camera)
    labelRenderer.render(scene, camera)
  }
  loop()

  const handle: TwinSceneHandle = {
    nodeKeys: [...pickables.keys()],
    highlight(key) { selected = key && pickables.has(key) ? key : null; repaint() },
    focus(key) {
      const obj = pickables.get(key)
      if (!obj) return
      const center = new THREE.Box3().setFromObject(obj).getCenter(new THREE.Vector3())
      flyTo(center.clone().add(new THREE.Vector3(70, 80, 90)), center)
    },
    setView(view) {
      if (view === 'top') flyTo(new THREE.Vector3(0.01, 300, 0.01), HOME.target.clone())
      else flyTo(HOME.position.clone(), HOME.target.clone())
    },
    setLayer(layer, visible) { layers[layer].visible = visible },
    dispose() {
      cancelAnimationFrame(frame)
      observer.disconnect()
      renderer.domElement.removeEventListener('pointerdown', onDown)
      renderer.domElement.removeEventListener('pointerup', onUp)
      renderer.domElement.removeEventListener('pointermove', onMove)
      renderer.domElement.removeEventListener('dblclick', onDouble)
      controls.dispose()
      scene.traverse((o) => {
        const mesh = o as THREE.Mesh
        mesh.geometry?.dispose()
        const mats = Array.isArray(mesh.material) ? mesh.material : mesh.material ? [mesh.material] : []
        mats.forEach((mat) => {
          Object.values(mat).forEach((v) => { if (v instanceof THREE.Texture) v.dispose() })
          mat.dispose()
        })
        if (o instanceof CSS2DObject) o.element.remove()
      })
      replacedMaterials.forEach((mat) => mat.dispose())
      renderer.dispose()
      renderer.forceContextLoss()
      renderer.domElement.remove()
      labelRenderer.domElement.remove()
    }
  }
  return handle
}
