import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { resolve } from 'node:path'
import { JSDOM } from 'jsdom'

const output = resolve('dist')
const html = await readFile(resolve(output, 'home.html'), 'utf8')
const document = new JSDOM(html).window.document
const root = document.querySelector('#home-app[data-prerendered]')
assert.ok(root, '正式首页必须包含预渲染内容')
assert.equal(root.firstChild.nodeType, 1, '首个节点必须是新版页面，避免hydration空白节点不匹配')
assert.equal(root.querySelector('h1')?.textContent, '山水有情，颐养有爱')
for (const id of ['about', 'services', 'environment', 'life', 'news']) {
  assert.ok(root.querySelector(`#${id}`), `禁用JavaScript时也应显示${id}内容`)
}
assert.equal(document.querySelector('.seo-home-shell'), null, '不能先显示旧版首页')
assert.equal(root.querySelector('[role="status"]'), null, '正式首页不能遗留开发加载占位')
const hero = root.querySelector('.hero-landscape')
assert.equal(hero?.getAttribute('fetchpriority'), 'high')
assert.equal(document.querySelector('link[rel="preload"][as="image"]')?.getAttribute('href'), hero.getAttribute('src'))

const scripts = [...new Set([...document.querySelectorAll('script[src],link[rel="modulepreload"]')]
  .map(node => node.getAttribute('src') || node.getAttribute('href')))]
let scriptBytes = 0
for (const src of scripts) {
  assert.ok(src.startsWith('/assets/'), '首页脚本应由本站提供')
  scriptBytes += (await readFile(resolve(output, src.slice(1)))).byteLength
}
assert.ok(scriptBytes < 250_000, `首页首批脚本超出250KB预算：${scriptBytes} bytes`)
console.log(`[home-check] 静态内容、首图优先级及脚本预算通过；首批JS ${(scriptBytes / 1024).toFixed(1)} KiB`)
