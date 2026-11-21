import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { CSS2DObject, CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import { FLOOR_HEIGHT, OVERLAY_EMPTY, type PlacedBuilding } from './twinModel'

export type TwinLayer = 'buildings' | 'roads' | 'vegetation' | 'labels'
export type TwinView = 'reset' | 'top'

export interface SceneBuilding extends PlacedBuilding {
  title: string
  sub?: string
}

export interface TwinSceneOptions {
  modelUrl: string | null
  /** 程序化场景按它生成楼栋；有 GLB 时只用 key（= 节点名）和标签 */
  buildings: SceneBuilding[]
  onSelect: (nodeKey: string | null) => void
}

export interface TwinSceneHandle {
  nodeKeys: string[]
  highlight: (nodeKey: string | null) => void
  focus: (nodeKey: string) => void
  setView: (view: TwinView) => void
  setLayer: (layer: TwinLayer, visible: boolean) => void
  /** 屋顶着色：key → 颜色；null 恢复原色 */
  setOverlay: (colors: Record<string, number> | null) => void
  /** 标签副标题（如「入住 82%」） */
  setLabelSub: (subs: Record<string, string>) => void
  dispose: () => void
}

const COLORS = {
  wall: 0xf4ede2, roof: 0xb9553d, plinth: 0xe4ded2, ground: 0xa8c592, plaza: 0xe8e3d6, road: 0xd7d1c3,
  curb: 0xf2eee6, conceptRoof: 0xc4b2a8, water: 0x7fb0bf, hill: 0x95ae8f, trunk: 0x7a5a3c, highlight: 0x1f8a74, horizon: 0xf3f1ea
}
const HOME = { position: new THREE.Vector3(160, 135, 185), target: new THREE.Vector3(0, 0, 10) }

export function webglAvailable(): boolean {
  try {
    const canvas = document.createElement('canvas')
    return Boolean(window.WebGLRenderingContext && (canvas.getContext('webgl2') || canvas.getContext('webgl')))
  } catch {
    return false
  }
}

/** 一个「层高 × 5m」的立面单元：白底 + 两扇窗 + 层间线；材质颜色再乘上去 */
function facadeTexture(): THREE.CanvasTexture {
  const c = document.createElement('canvas')
  c.width = 128
  c.height = 96
  const g = c.getContext('2d')!
  g.fillStyle = '#ffffff'
  g.fillRect(0, 0, 128, 96)
  g.fillStyle = '#e6ddd0'
  g.fillRect(0, 88, 128, 8)
  for (const x of [18, 74]) {
    const grad = g.createLinearGradient(0, 22, 0, 70)
    grad.addColorStop(0, '#7f9aa8')
    grad.addColorStop(1, '#a9bec8')
    g.fillStyle = grad
    g.fillRect(x, 22, 36, 48)
    g.fillStyle = '#f7f3ec'
    g.fillRect(x + 17, 22, 2, 48)
    g.fillRect(x - 2, 70, 40, 4)
  }
  const tex = new THREE.CanvasTexture(c)
  tex.colorSpace = THREE.SRGBColorSpace
  tex.wrapS = tex.wrapT = THREE.RepeatWrapping
  tex.anisotropy = 4
  return tex
}

function skyTexture(): THREE.CanvasTexture {
  const c = document.createElement('canvas')
  c.width = 2
  c.height = 256
  const g = c.getContext('2d')!
  const grad = g.createLinearGradient(0, 0, 0, 256)
  grad.addColorStop(0, '#bcd9e2')
  grad.addColorStop(0.55, '#e3eeee')
  grad.addColorStop(1, '#f3f1ea')
  g.fillStyle = grad
  g.fillRect(0, 0, 2, 256)
  const tex = new THREE.CanvasTexture(c)
  tex.colorSpace = THREE.SRGBColorSpace
  return tex
}

/** 程序化楼栋：台基 + 带窗立面的墙体 + 双坡屋顶。概念占位楼半透明，和主数据楼栋区分开 */
function buildBuilding(b: SceneBuilding, facade: THREE.Texture): THREE.Group {
  const group = new THREE.Group()
  const { w, d, height } = b
  const floors = Math.max(1, Math.round(height / FLOOR_HEIGHT))
  // 概念占位：半透明、屋顶去饱和，一眼能和主数据楼栋区分
  const look = { transparent: !b.real, opacity: b.real ? 1 : 0.5 }
  const faceMat = (span: number) => {
    const map = facade.clone()
    map.needsUpdate = true
    map.repeat.set(Math.max(1, Math.round(span / 5)), floors)
    return new THREE.MeshStandardMaterial({ color: COLORS.wall, map, roughness: 0.85, ...look })
  }
  const capMat = new THREE.MeshStandardMaterial({ color: COLORS.wall, roughness: 0.9, ...look })
  // BoxGeometry 面顺序：+x -x +y -y +z -z
  const body = new THREE.Mesh(new THREE.BoxGeometry(w, height, d), [faceMat(d), faceMat(d), capMat, capMat, faceMat(w), faceMat(w)])
  body.position.y = height / 2 + 0.6
  const plinth = new THREE.Mesh(new THREE.BoxGeometry(w + 5, 0.6, d + 5), new THREE.MeshStandardMaterial({ color: COLORS.plinth, roughness: 1 }))
  plinth.position.y = 0.3
  const ridge = Math.min(w, d) * 0.32
  const span = Math.min(w, d) / 2 + 1.2
  const shape = new THREE.Shape([new THREE.Vector2(-span, 0), new THREE.Vector2(span, 0), new THREE.Vector2(0, ridge)])
  const length = Math.max(w, d) + 2.4
  const roofGeo = new THREE.ExtrudeGeometry(shape, { depth: length, bevelEnabled: false })
  roofGeo.translate(0, 0, -length / 2)
  const roof = new THREE.Mesh(roofGeo, new THREE.MeshStandardMaterial({ color: b.real ? COLORS.roof : COLORS.conceptRoof, roughness: 0.7, ...look }))
  roof.position.y = height + 0.6
  roof.userData.part = 'roof'
  if (w > d) roof.rotation.y = Math.PI / 2
  for (const mesh of [body, roof, plinth]) { mesh.castShadow = b.real; mesh.receiveShadow = true }
  group.add(plinth, body, roof)
  return group
}

function flat(w: number, d: number, color: number, x: number, y: number, z: number): THREE.Mesh {
  const mesh = new THREE.Mesh(new THREE.PlaneGeometry(w, d), new THREE.MeshStandardMaterial({ color, roughness: 1 }))
  mesh.rotation.x = -Math.PI / 2
  mesh.position.set(x, y, z)
  mesh.receiveShadow = true
  return mesh
}

/** 环境：地面、广场、道路（带路缘）、水体、远山、两色实例化树木（都是示意，不对应真实位置）；extent = 南侧额外楼栋行占用的进深 */
function environment(layers: Record<TwinLayer, THREE.Group>, extent: number) {
  const ground = new THREE.Mesh(new THREE.CircleGeometry(520, 64), new THREE.MeshStandardMaterial({ color: COLORS.ground, roughness: 1 }))
  ground.rotation.x = -Math.PI / 2
  ground.receiveShadow = true
  layers.buildings.add(ground, flat(250, 160 + extent, COLORS.plaza, 0, 0.02, extent / 2))
  const roads: number[][] = [[0, 22, 230, 8], [0, -26, 230, 8], [-112, extent / 2, 8, 170 + extent], [112, extent / 2, 8, 170 + extent], [0, 92, 300, 10]]
  roads.forEach(([x, z, w, d]) => layers.roads.add(flat(w + 1.6, d + 1.6, COLORS.curb, x, 0.04, z), flat(w, d, COLORS.road, x, 0.06, z)))
  const water = new THREE.Mesh(new THREE.CircleGeometry(28, 48), new THREE.MeshStandardMaterial({ color: COLORS.water, roughness: 0.15, metalness: 0.2 }))
  water.rotation.x = -Math.PI / 2
  water.scale.set(1.4, 1, 1)
  water.position.set(-165, 0.08, 40)
  layers.vegetation.add(water)
  const hillMat = new THREE.MeshStandardMaterial({ color: COLORS.hill, flatShading: true, roughness: 1 })
  ;[[-230, -340, 110, 46], [-80, -380, 120, 64], [80, -360, 100, 42], [240, -330, 95, 54]].forEach(([x, z, r, h]) => {
    const hill = new THREE.Mesh(new THREE.ConeGeometry(r, h, 8), hillMat)
    hill.position.set(x, h / 2, z)
    layers.vegetation.add(hill)
  })

  const count = 140
  const trunks = new THREE.InstancedMesh(new THREE.CylinderGeometry(0.5, 0.7, 4, 6), new THREE.MeshStandardMaterial({ color: COLORS.trunk }), count)
  const crowns = new THREE.InstancedMesh(new THREE.IcosahedronGeometry(3.4, 0), new THREE.MeshStandardMaterial({ flatShading: true, roughness: 0.9 }), count)
  const m = new THREE.Matrix4()
  const greens = [new THREE.Color(0x5b8c55), new THREE.Color(0x6f9e5c), new THREE.Color(0x4d7a4f)]
  let seed = 7
  const rand = () => ((seed = (seed * 9301 + 49297) % 233280) / 233280)
  for (let i = 0; i < count; i++) {
    const edge = i % 4
    const t = rand() * 2 - 1
    const x = edge < 2 ? t * 130 : (edge === 2 ? -124 : 124) + rand() * 8
    const z = edge < 2 ? (edge === 0 ? 80 + extent : -84) + rand() * 6 : t * 90 + extent / 2
    const s = 0.8 + rand() * 0.6
    m.compose(new THREE.Vector3(x, 2 * s, z), new THREE.Quaternion(), new THREE.Vector3(s, s, s))
    trunks.setMatrixAt(i, m)
    m.compose(new THREE.Vector3(x, 6.4 * s, z), new THREE.Quaternion().setFromEuler(new THREE.Euler(0, rand() * 3, 0)), new THREE.Vector3(s, s * 1.15, s))
    crowns.setMatrixAt(i, m)
    crowns.setColorAt(i, greens[i % 3])
  }
  trunks.castShadow = crowns.castShadow = true
  layers.vegetation.add(trunks, crowns)
}

function meshMaterials(mesh: THREE.Mesh): THREE.MeshStandardMaterial[] {
  const list = Array.isArray(mesh.material) ? mesh.material : [mesh.material]
  return list.filter((m): m is THREE.MeshStandardMaterial => m instanceof THREE.MeshStandardMaterial)
}

export async function createTwinScene(container: HTMLElement, options: TwinSceneOptions): Promise<TwinSceneHandle> {
  if (!webglAvailable()) throw new Error('WEBGL_UNSUPPORTED')
  // 先加载模型再建渲染器：加载失败时不留下半初始化的 WebGL 资源
  const gltf = options.modelUrl ? await new GLTFLoader().loadAsync(options.modelUrl) : null
  const renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.05
  container.appendChild(renderer.domElement)
  const labelRenderer = new CSS2DRenderer()
  labelRenderer.domElement.className = 'twin-label-layer'
  container.appendChild(labelRenderer.domElement)

  const scene = new THREE.Scene()
  const sky = skyTexture()
  scene.background = sky
  scene.fog = new THREE.Fog(COLORS.horizon, 380, 760)
  const camera = new THREE.PerspectiveCamera(42, 1, 1, 2400)
  camera.position.copy(HOME.position)
  const controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.maxPolarAngle = Math.PI / 2.15
  controls.minDistance = 30
  controls.maxDistance = 560
  controls.target.copy(HOME.target)

  scene.add(new THREE.HemisphereLight(0xf4f8ff, 0x8a9a7a, 1.25))
  const sun = new THREE.DirectionalLight(0xfff3e0, 2.1)
  sun.position.set(-140, 220, 120)
  sun.castShadow = true
  sun.shadow.mapSize.set(2048, 2048)
  sun.shadow.bias = -0.0004
  Object.assign(sun.shadow.camera, { left: -220, right: 220, top: 220, bottom: -220, far: 700 })
  scene.add(sun)

  const layers = { buildings: new THREE.Group(), roads: new THREE.Group(), vegetation: new THREE.Group(), labels: new THREE.Group() }
  Object.values(layers).forEach((g) => scene.add(g))
  const pickables = new Map<string, THREE.Object3D>()
  const facade = facadeTexture()
  const infos = new Map(options.buildings.map((b) => [b.key, b]))

  if (gltf) {
    // 有已发布 GLB 时只认节点名等于 modelNodeKey 的对象为可点击楼栋
    layers.buildings.add(gltf.scene)
    gltf.scene.traverse((o) => { if (infos.has(o.name)) pickables.set(o.name, o) })
  } else {
    environment(layers, Math.max(0, ...options.buildings.map((b) => b.z - 60)))
    for (const b of options.buildings) {
      const building = buildBuilding(b, facade)
      building.position.set(b.x, 0, b.z)
      building.rotation.y = b.rot ?? 0
      building.name = b.key
      layers.buildings.add(building)
      pickables.set(b.key, building)
    }
  }
  const subEls = new Map<string, HTMLElement>()
  for (const [key, obj] of pickables) {
    const info = infos.get(key)!
    const div = document.createElement('div')
    div.className = 'twin-label' + (info.real ? ' twin-label--real' : '')
    const title = document.createElement('b')
    title.textContent = info.title
    const sub = document.createElement('span')
    sub.textContent = info.sub ?? ''
    div.append(title, sub)
    subEls.set(key, sub)
    const label = new CSS2DObject(div)
    const box = new THREE.Box3().setFromObject(obj)
    label.position.set((box.min.x + box.max.x) / 2, box.max.y + 3, (box.min.z + box.max.z) / 2)
    layers.labels.add(label)
  }

  // 选中环：贴地的青绿圆环，呼吸动画
  const ring = new THREE.Mesh(new THREE.RingGeometry(1, 1.12, 64), new THREE.MeshBasicMaterial({ color: COLORS.highlight, transparent: true, depthWrite: false }))
  ring.rotation.x = -Math.PI / 2
  ring.visible = false
  scene.add(ring)

  // ---------- 选择 / 高亮 / 着色 / 聚焦 ----------
  let selected: string | null = null
  let hovered: string | null = null
  // 程序化楼栋每栋自带材质；GLB 里材质可能共享，复制一份再改色。记下原色，选中时把颜色推向品牌青绿
  const tealColor = new THREE.Color(COLORS.highlight)
  const replacedMaterials = new Set<THREE.Material>()
  for (const obj of pickables.values()) {
    obj.traverse((o) => {
      const mesh = o as THREE.Mesh
      if (!mesh.isMesh) return
      const own = meshMaterials(mesh).map((mat) => {
        if (gltf) replacedMaterials.add(mat)
        const copy = gltf ? mat.clone() : mat
        copy.userData.baseColor = copy.color.clone()
        return copy
      })
      if (gltf && own.length) mesh.material = Array.isArray(mesh.material) ? own : own[0]
    })
  }
  const tint = (key: string | null, amount: number) => {
    if (!key) return
    pickables.get(key)?.traverse((o) => {
      const mesh = o as THREE.Mesh
      if (!mesh.isMesh) return
      for (const mat of meshMaterials(mesh)) {
        const base = mat.userData.baseColor as THREE.Color | undefined
        if (base) mat.color.lerpColors(base, tealColor, amount)
      }
    })
  }
  const repaint = () => {
    for (const key of pickables.keys()) tint(key, 0)
    tint(hovered, 0.22)
    tint(selected, 0.45)
  }
  const placeRing = () => {
    const obj = selected ? pickables.get(selected) : null
    ring.visible = Boolean(obj)
    if (!obj) return
    const box = new THREE.Box3().setFromObject(obj)
    const c = box.getCenter(new THREE.Vector3())
    const r = Math.max(box.max.x - box.min.x, box.max.z - box.min.z) / 2 + 6
    ring.position.set(c.x, 0.15, c.z)
    ring.scale.set(r, r, 1)
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
    const now = performance.now()
    if (flight) {
      const t = Math.min(1, (now - flight.start) / 800)
      const k = t < 0.5 ? 2 * t * t : 1 - (-2 * t + 2) ** 2 / 2
      camera.position.lerpVectors(flight.from, flight.to, k)
      controls.target.lerpVectors(flight.fromT, flight.toT, k)
      if (t === 1) flight = null
    }
    if (ring.visible) (ring.material as THREE.MeshBasicMaterial).opacity = 0.55 + Math.sin(now / 320) * 0.3
    controls.update()
    renderer.render(scene, camera)
    labelRenderer.render(scene, camera)
  }
  loop()

  const handle: TwinSceneHandle = {
    nodeKeys: [...pickables.keys()],
    highlight(key) { selected = key && pickables.has(key) ? key : null; repaint(); placeRing() },
    focus(key) {
      const obj = pickables.get(key)
      if (!obj) return
      const center = new THREE.Box3().setFromObject(obj).getCenter(new THREE.Vector3())
      flyTo(center.clone().add(new THREE.Vector3(62, 70, 82)), center)
    },
    setView(view) {
      if (view === 'top') flyTo(new THREE.Vector3(0.01, 320, 20.01), HOME.target.clone())
      else flyTo(HOME.position.clone(), HOME.target.clone())
    },
    setLayer(layer, visible) { layers[layer].visible = visible },
    setOverlay(colors) {
      for (const [key, obj] of pickables) {
        obj.traverse((o) => {
          if (o.userData.part !== 'roof') return
          const original = infos.get(key)?.real === false ? COLORS.conceptRoof : COLORS.roof
          for (const mat of meshMaterials(o as THREE.Mesh)) mat.userData.baseColor = new THREE.Color(colors ? colors[key] ?? OVERLAY_EMPTY : original)
        })
      }
      repaint()
    },
    setLabelSub(subs) {
      for (const [key, el] of subEls) el.textContent = subs[key] ?? infos.get(key)?.sub ?? ''
    },
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
      facade.dispose()
      sky.dispose()
      renderer.dispose()
      renderer.forceContextLoss()
      renderer.domElement.remove()
      labelRenderer.domElement.remove()
    }
  }
  return handle
}
