import { readFile, writeFile, rm } from 'node:fs/promises'
import { resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import { build } from 'vite'
import vue from '@vitejs/plugin-vue'

const output = resolve('dist')
const renderDir = resolve(output, '.home-render')
const pagePath = resolve(output, 'home.html')
const root = /<div id="home-app">\s*<!--home-content-->[\s\S]*?<!--\/home-content-->\s*<\/div>/

try {
  const page = await readFile(pagePath, 'utf8')
  if (!root.test(page)) throw new Error('首页预渲染标记缺失')
  await build({
    configFile: false,
    plugins: [vue()],
    build: {
      ssr: resolve('src/home-render.ts'),
      outDir: renderDir,
      rollupOptions: { output: { entryFileNames: 'home-render.mjs' } }
    }
  })
  const { renderHome } = await import(pathToFileURL(resolve(renderDir, 'home-render.mjs')).href)
  const html = await renderHome()
  const result = page.replace(root, () => `<div id="home-app" data-prerendered>${html}</div>`)
  await writeFile(pagePath, result)
  console.log('[home] 已预渲染官网，新旧页面无需替换。')
} finally {
  await rm(renderDir, { recursive: true, force: true })
}
