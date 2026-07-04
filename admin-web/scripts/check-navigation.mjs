import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import esbuild from 'esbuild'

const ROOT = process.cwd()
const ROUTES_FILE = path.join(ROOT, 'src/router/routes.ts')
const LEGACY_FILE = path.join(ROOT, 'src/router/legacyRedirects.ts')
const MARKETING_FILE = path.join(ROOT, 'src/router/marketingRoutes.ts')
const SRC_DIR = path.join(ROOT, 'src')

// 用 esbuild 把 TS 源码转成 CJS 后在沙箱执行；组件懒加载 () => import(...) 不会被调用，因此无需真实模块。
// requireShim 负责解析被拆分出去的本地路由文件（legacyRedirects / marketingRoutes）。
function loadModule(file, requireShim) {
  if (!fs.existsSync(file)) return {}
  const source = fs.readFileSync(file, 'utf8')
  const { code } = esbuild.transformSync(source, { loader: 'ts', format: 'cjs' })
  const moduleObj = { exports: {} }
  const sandbox = {
    module: moduleObj,
    exports: moduleObj.exports,
    require: requireShim || (() => ({})),
    Promise
  }
  vm.runInNewContext(code, sandbox, { timeout: 3000 })
  return moduleObj.exports || {}
}

function loadRoutes() {
  const legacy = loadModule(LEGACY_FILE)
  const marketing = loadModule(MARKETING_FILE)
  const requireShim = (id) => {
    if (id.includes('legacyRedirects')) return legacy
    if (id.includes('marketingRoutes')) return marketing
    return {}
  }
  return loadModule(ROUTES_FILE, requireShim).routes || []
}

function flattenRoutes(routes, base = '') {
  const output = []
  for (const route of routes) {
    const routePath = String(route.path || '')
    const fullPath = routePath.startsWith('/') ? routePath : `${base}/${routePath}`.replace(/\/+/g, '/')
    output.push(fullPath || '/')
    if (Array.isArray(route.children)) {
      output.push(...flattenRoutes(route.children, fullPath || '/'))
    }
  }
  return output
}

function collectFiles(dir, result = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  for (const entry of entries) {
    if (entry.name === 'node_modules' || entry.name === 'dist') continue
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      collectFiles(full, result)
      continue
    }
    if (!/\.(vue|ts|tsx|js|jsx)$/.test(entry.name)) continue
    if (full.endsWith('src/router/routes.ts')) continue
    result.push(full)
  }
  return result
}

function lineMatches(line, regex) {
  const result = []
  let match
  while ((match = regex.exec(line)) !== null) {
    result.push(match[2])
  }
  return result
}

function extractPathsFromFile(file) {
  const lines = fs.readFileSync(file, 'utf8').split('\n')
  const hits = []
  const patterns = [
    /\brouter\.(?:push|replace)\(\s*(['"`])([^'"`]+)\1/g,
    /\bgo\(\s*(['"`])([^'"`]+)\1/g,
    /\broute\s*:\s*(['"`])([^'"`]+)\1/g,
    /\bhref\s*=\s*(['"`])([^'"`]+)\1/g
  ]

  lines.forEach((line, i) => {
    for (const pattern of patterns) {
      const values = lineMatches(line, pattern)
      values.forEach((value) => {
        if (!value.startsWith('/')) return
        if (value.startsWith('/api/')) return
        if (value.startsWith('//')) return
        hits.push({ path: value, line: i + 1 })
      })
    }
  })
  return hits
}

function normalizePath(input) {
  return input
    .split('#')[0]
    .split('?')[0]
    .replace(/\$\{[^}]+\}/g, 'DYNAMIC')
}

function toRouteRegex(routePath) {
  let pattern = routePath.replace(/[.+?^${}()|[\]\\]/g, '\\$&')
  pattern = pattern.replace(/\/:([^/]+)/g, '/[^/]+')
  pattern = pattern.replace(/\\\*/g, '.*')
  return new RegExp(`^${pattern}$`)
}

const routeList = flattenRoutes(loadRoutes())
const routeRegexList = routeList.map((routePath) => ({ routePath, regex: toRouteRegex(routePath) }))
const files = collectFiles(path.join(SRC_DIR, 'views'))

const unresolved = []
let checked = 0

for (const file of files) {
  const links = extractPathsFromFile(file)
  links.forEach((item) => {
    const normalized = normalizePath(item.path)
    checked += 1
    const hit = routeRegexList.some((route) => route.regex.test(normalized))
    if (!hit) {
      unresolved.push({
        file: path.relative(ROOT, file),
        line: item.line,
        path: item.path
      })
    }
  })
}

console.log(`[navigation-check] routes=${routeList.length} links=${checked} unresolved=${unresolved.length}`)
if (unresolved.length) {
  unresolved
    .sort((a, b) => a.file.localeCompare(b.file) || a.line - b.line)
    .forEach((item) => {
      console.log(` - ${item.file}:${item.line} -> ${item.path}`)
    })
  process.exit(1)
}

