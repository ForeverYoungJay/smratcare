export type PrintableColumn = {
  key: string
  title: string
}

export function printTableReport(params: {
  title: string
  subtitle?: string
  columns: PrintableColumn[]
  rows: Array<Record<string, any>>
}) {
  const popup = window.open('', '_blank')
  if (!popup) {
    throw new Error('请允许弹窗后重试打印')
  }
  const columns = params.columns || []
  const rows = params.rows || []
  const headers = columns.map(item => `<th>${escapeHtml(item.title)}</th>`).join('')
  const body = rows.map(row => {
    const cells = columns.map(col => `<td>${escapeHtml(String(row[col.key] ?? ''))}</td>`).join('')
    return `<tr>${cells}</tr>`
  }).join('')
  const html = `
    <html>
      <head>
        <meta charset="utf-8">
        <title>${escapeHtml(params.title || '报表打印')}</title>
      </head>
      <body>
        <h3>${escapeHtml(params.title || '报表打印')}</h3>
        ${params.subtitle ? `<p>${escapeHtml(params.subtitle)}</p>` : ''}
        <table border="1" cellspacing="0" cellpadding="6">
          <thead><tr>${headers}</tr></thead>
          <tbody>${body}</tbody>
        </table>
      </body>
    </html>
  `
  popup.document.write(html)
  popup.document.close()
  popup.focus()
  popup.print()
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
