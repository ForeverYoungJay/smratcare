function l(t){return(t==null?"":String(t)).replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;").replaceAll("'","&#39;")}function f(t){const{title:o,subtitle:a,columns:i,rows:p}=t,r=(p||[]).map(e=>`<tr>${i.map(c=>`<td>${l((e==null?void 0:e[c.key])??"")}</td>`).join("")}</tr>`).join(""),s=(t.signatures||[]).filter(Boolean),d=s.length?`<div class="signatures">${s.map(e=>`<div class="sig-item">${l(e)}：________________</div>`).join("")}</div>`:"",n=window.open("","_blank");return n?(n.document.write(`
    <html>
      <head>
        <title>${l(o)}</title>
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
        <h1>${l(o)}</h1>
        <div class="sub">${l(a||"")}</div>
        <table>
          <thead><tr>${i.map(e=>`<th>${l(e.title)}</th>`).join("")}</tr></thead>
          <tbody>${r}</tbody>
        </table>
        ${d}
      </body>
    </html>
  `),n.document.close(),n.focus(),n.print(),!0):!1}function m(t){if(!f(t))throw new Error("请允许弹窗后重试打印")}export{f as o,m as p};
