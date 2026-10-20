// 把 Vditor 运行时资源同步到 public/vditor/dist，使编辑器预览完全离线可用
// （Vditor 默认从 unpkg CDN 拉 lute/highlight 等资源，本地开发无外网时会白屏）
// Vditor 组件里通过 cdn: '/vditor' 指向该目录。
import { cpSync, existsSync, mkdirSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const src = resolve(root, 'node_modules/vditor/dist')
const dest = resolve(root, 'public/vditor/dist')

if (!existsSync(src)) {
  console.error('[vditor] 未找到 node_modules/vditor，请先执行 npm install')
  process.exit(1)
}

// 只同步编辑器运行时真正需要的子目录，避免把 20M+ 的 mermaid/mathjax 全量拷进来
const needed = [
  'js/lute', // Markdown 引擎（必需）
  'js/highlight.js', // 预览区代码高亮
  'js/i18n', // 中文文案
  'css', // 内容主题
  'images',
]

// 注意：这里刻意不做整目录删除（rmSync recursive），只做覆盖式拷贝——
// 一是静态资源覆盖即等价，二是避免在受保护环境里触发批量删除拦截。
for (const dir of needed) {
  const from = resolve(src, dir)
  if (!existsSync(from)) {
    console.warn(`[vditor] 跳过不存在的资源：${dir}`)
    continue
  }
  const to = resolve(dest, dir)
  mkdirSync(dirname(to), { recursive: true })
  cpSync(from, to, { recursive: true, force: true })
  console.log(`[vditor] 已同步 ${dir} -> public/vditor/dist/${dir}`)
}
console.log('[vditor] 完成')
