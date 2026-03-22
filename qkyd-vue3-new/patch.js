const fs = require('fs');

function patchFile(filepath) {
  let code = fs.readFileSync(filepath, 'utf8');

  // 1. Remove the floating value cards from top left
  const removeFloatingRegex = /(<!-- ================= 0\.5 顶部左上角：价值卡片 ================= -->\r?\n\s*<div class="hud-value-container">)[\s\S]*?(<\/div>\r?\n\s*<\/div>\r?\n)/;
  code = code.replace(removeFloatingRegex, '');

  // 2. Insert into the left sidebar
  const insertSidebarRegex = /(<!-- ================= 2\. 左侧：全息渐变翼 ================= -->\r?\n\s*<div class="hud-side-panel hud-left fade-left">\r?\n)/;
  const newCardsHtml = `      <!-- 价值卡片 (整合进左侧面板避免重叠) -->
      <div class="hud-value-cards">
        <div class="value-card">
          <div class="vc-title" title="今日异常对象">今日异常对象</div>
          <div class="vc-value digital-font">{{ valueCardsData.abnormalObjects }}<span class="vc-unit">人</span></div>
        </div>
        <div class="value-card">
          <div class="vc-title" title="高风险预警">高风险预警</div>
          <div class="vc-value digital-font text-danger">{{ valueCardsData.highRisk }}<span class="vc-unit">次</span></div>
        </div>
        <div class="value-card">
          <div class="vc-title" title="平均响应时长">平均响应时长</div>
          <div class="vc-value digital-font text-warning">{{ valueCardsData.avgResponseTime }}<span class="vc-unit">min</span></div>
        </div>
        <div class="value-card">
          <div class="vc-title" title="已闭环占比">已闭环占比</div>
          <div class="vc-value digital-font text-success">{{ valueCardsData.closedRatio }}<span class="vc-unit">%</span></div>
        </div>
      </div>

`;
  if(!code.match(insertSidebarRegex)) console.log("Failed to insert into sidebar");
  code = code.replace(insertSidebarRegex, '$1' + newCardsHtml);

  // 3. Update side panel widths
  code = code.replace(
    /(\.hud-side-panel \{[\s\S]*?width: )280px(;)/g,
    '$1320px$2'
  );
  code = code.replace(
    /(\.hud-left \{ left: 0; padding-right: )40px(; \})/g,
    '$120px$2'
  );
  code = code.replace(
    /(\.hud-right \{ right: 0; padding-left: )40px(; \})/g,
    '$120px$2'
  );
  code = code.replace(
    /(\.hud-bottom-center \{[\s\S]*?left: )280px(; right: )280px(;)/g,
    '$1320px$2320px$3'
  );

  // 4. Update the CSS for the cards to display as 2x2 grid
  const cssRegex = /(\/\* 价值大屏卡片 \*\/[\s\S]*?\.hud-value-cards \{[\s\S]*?\}[\s\S]*?\.value-card \{[\s\S]*?\}[\s\S]*?\.value-card:hover \{[\s\S]*?\}[\s\S]*?\.vc-title \{[\s\S]*?\}[\s\S]*?\.vc-value \{[\s\S]*?\}[\s\S]*?\.vc-unit \{[\s\S]*?\})/;
  
  const newCss = `/* 价值大屏卡片 (改为侧边栏内部网格布局) */
.hud-value-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  pointer-events: auto;
  flex-shrink: 0;
  margin-bottom: 24px;
}
.value-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  padding: 10px 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  width: 100%;
  transition: all 0.3s;
}
.value-card:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
}
.vc-title { font-size: 11px; color: #64748b; margin-bottom: 4px; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.vc-value { font-size: 20px; font-weight: bold; color: #0ea5e9; display: flex; align-items: baseline; gap: 4px; line-height: 1; }
.vc-unit { font-size: 11px; font-weight: normal; color: #94a3b8; font-family: sans-serif; }`;

  if(!code.match(cssRegex)) console.log("Failed to match css block");
  code = code.replace(cssRegex, newCss);

  // Remove the old hud-value-container CSS
  code = code.replace(/\/\* 顶部左侧：价值卡片容器 \*\/[\s\S]*?\.hud-value-container \{[\s\S]*?\}\s*/, '');

  // Update media query logic
  code = code.replace(
    /\s*\.hud-value-container \{ .*? \}\r?\n/,
    '\n'
  );
  code = code.replace(
    /\.hud-value-cards \{ flex-direction: row; flex-wrap: wrap; justify-content: center; \}/,
    '.hud-value-cards { display: flex; flex-direction: row; flex-wrap: wrap; justify-content: center; margin-bottom: 16px; }'
  );

  fs.writeFileSync(filepath, code);
  console.log('Patch complete.');
}

patchFile('src/views/DashboardView.vue');
