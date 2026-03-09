function safeHtml(value) {
    const text = value == null ? '' : String(value);
    return text
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}
export function openPrintTableReport(params) {
    const { title, subtitle, columns, rows } = params;
    const htmlRows = (rows || [])
        .map((row) => `<tr>${columns.map((col) => `<td>${safeHtml(row?.[col.key] ?? '')}</td>`).join('')}</tr>`)
        .join('');
    const signatures = (params.signatures || []).filter(Boolean);
    const signatureHtml = signatures.length
        ? `<div class="signatures">${signatures
            .map((item) => `<div class="sig-item">${safeHtml(item)}：________________</div>`)
            .join('')}</div>`
        : '';
    const popup = window.open('', '_blank');
    if (!popup) {
        return false;
    }
    popup.document.write(`
    <html>
      <head>
        <title>${safeHtml(title)}</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif; padding: 20px; color: #1f2937; }
          h1 { margin: 0 0 6px; font-size: 20px; }
          .sub { margin: 0 0 12px; color: #6b7280; font-size: 12px; }
          table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          th, td { border: 1px solid #e5e7eb; padding: 8px 10px; text-align: left; font-size: 12px; }
          th { background: #f3f4f6; }
          .signatures { margin-top: 22px; display: flex; gap: 24px; flex-wrap: wrap; }
          .sig-item { min-width: 200px; font-size: 12px; color: #374151; }
        </style>
      </head>
      <body>
        <h1>${safeHtml(title)}</h1>
        <div class="sub">${safeHtml(subtitle || '')}</div>
        <table>
          <thead><tr>${columns.map((col) => `<th>${safeHtml(col.title)}</th>`).join('')}</tr></thead>
          <tbody>${htmlRows}</tbody>
        </table>
        ${signatureHtml}
      </body>
    </html>
  `);
    popup.document.close();
    popup.focus();
    popup.print();
    return true;
}
export function printTableReport(params) {
    const ok = openPrintTableReport(params);
    if (!ok) {
        throw new Error('请允许弹窗后重试打印');
    }
}
