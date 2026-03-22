const fs = require('fs');
const path = require('path');

const targetPath = path.join(__dirname, 'src/views/AiWorkbenchView.vue');
let content = fs.readFileSync(targetPath, 'utf-8');

const oldLayout = `      <div class="visual-dashboard">
        <div class="metrics-panel">
          <div class="metric-item panel">
            <div class="metric-info">
              <span class="metric-label">分析轮次</span>
              <strong class="metric-value">{{ aiMessageCount }}</strong>
            </div>
            <div class="metric-icon"><el-icon><ChatDotRound /></el-icon></div>
          </div>
          <div class="metric-item panel">
            <div class="metric-info">
              <span class="metric-label">结构化结果</span>
              <strong class="metric-value">{{ structuredInsightCount }}</strong>
            </div>
            <div class="metric-icon"><el-icon><Grid /></el-icon></div>
          </div>
          <div class="metric-item panel danger-soft">
            <div class="metric-info">
              <span class="metric-label">高风险判断</span>
              <strong class="metric-value">{{ highRiskCount }}</strong>
            </div>
            <div class="metric-icon"><el-icon><Warning /></el-icon></div>
          </div>
          <div class="metric-item panel success-soft">
            <div class="metric-info">
              <span class="metric-label">待执行动作</span>
              <strong class="metric-value">{{ pendingActionCount }}</strong>
            </div>
            <div class="metric-icon"><el-icon><VideoPlay /></el-icon></div>
          </div>
        </div>

        <div class="chart-panel panel">
          <div class="panel-header">
            <div>
              <p class="section-eyebrow">风险结构监控</p>
              <h4>风险等级分布</h4>
            </div>
          </div>
          <div id="riskChart" class="dashboard-chart"></div>
        </div>

        <div class="chart-panel panel">
          <div class="panel-header">
            <div>
              <p class="section-eyebrow">分析效能趋势</p>
              <h4>今日处理量趋势</h4>
            </div>
          </div>
          <div id="trendChart" class="dashboard-chart"></div>
        </div>
      </div>

      <div class="workbench-layout">
        <section class="conversation-panel panel">`;

const newLayout = `      <div class="workbench-split-layout">
        <aside class="workbench-left-pane">
          <div class="metrics-panel">
            <div class="metric-item panel">
              <div class="metric-info">
                <span class="metric-label">分析轮次</span>
                <strong class="metric-value">{{ aiMessageCount }}</strong>
              </div>
              <div class="metric-icon"><el-icon><ChatDotRound /></el-icon></div>
            </div>
            <div class="metric-item panel">
              <div class="metric-info">
                <span class="metric-label">结构化结果</span>
                <strong class="metric-value">{{ structuredInsightCount }}</strong>
              </div>
              <div class="metric-icon"><el-icon><Grid /></el-icon></div>
            </div>
            <div class="metric-item panel danger-soft">
              <div class="metric-info">
                <span class="metric-label">高风险判断</span>
                <strong class="metric-value">{{ highRiskCount }}</strong>
              </div>
              <div class="metric-icon"><el-icon><Warning /></el-icon></div>
            </div>
            <div class="metric-item panel success-soft">
              <div class="metric-info">
                <span class="metric-label">待执行动作</span>
                <strong class="metric-value">{{ pendingActionCount }}</strong>
              </div>
              <div class="metric-icon"><el-icon><VideoPlay /></el-icon></div>
            </div>
          </div>

          <div class="chart-panel panel">
            <div class="panel-header">
              <div>
                <p class="section-eyebrow">风险结构监控</p>
                <h4>风险等级分布</h4>
              </div>
            </div>
            <div id="riskChart" class="dashboard-chart"></div>
          </div>

          <div class="chart-panel panel">
            <div class="panel-header">
              <div>
                <p class="section-eyebrow">分析效能趋势</p>
                <h4>今日处理量趋势</h4>
              </div>
            </div>
            <div id="trendChart" class="dashboard-chart"></div>
          </div>
        </aside>

        <main class="workbench-center-pane">
          <section class="conversation-panel panel">`;

content = content.replace(oldLayout, newLayout);

content = content.replace(
`        </section>
      </div>

      <template #aside>`,
`        </section>
        </main>
      </div>

      <template #aside>`
);

const cssReplacements = [
  [
    `.panel,
.inner-panel {
  border: 1px solid var(--ai-border);
  background: var(--ai-card);
  border-radius: 20px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}`,
    `.panel,
.inner-panel {
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}`
  ],
  [
    `.panel,
.inner-panel {
  border: 1px solid var(--ai-border);
  background: var(--ai-card);
  border-radius: 18px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}`,
    `.panel,
.inner-panel {
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}`
  ],
  [
    `.metric-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  border-radius: 14px;
  background: var(--ai-card);
  border: 1px solid var(--ai-border);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.03);
}`,
    `.metric-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 6px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
}`
  ],
  [
    `.msg-bubble {
  padding: 16px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid var(--ai-border);
  min-width: 220px;
}`,
    `.msg-bubble {
  padding: 16px;
  border-radius: 6px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  min-width: 220px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}`
  ],
  [
    `.pill-input-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  padding: 12px;
  border-radius: 20px;
  border: 1px solid var(--ai-border);
  background: #fff;
}`,
    `.pill-input-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}`
  ],
  [
    `.mode-chip,
.entity-chip,
.action-tile,
.chip-btn {
  border: 1px solid var(--ai-border);
  background: rgba(255, 255, 255, 0.95);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}`,
    `.mode-chip,
.entity-chip,
.action-tile,
.chip-btn {
  border: 1px solid rgba(0,0,0,0.1);
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}`
  ],
  [
    `.visual-dashboard {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}`,
    `.workbench-split-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
  align-items: stretch;
}
.workbench-left-pane {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.workbench-center-pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
}`
  ],
  [
    `.metrics-panel {
  display: grid;
  grid-template-rows: repeat(4, 1fr);
  gap: 12px;
}`,
    `.metrics-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}`
  ],
  [
    `.chart-panel {
  display: flex;
  flex-direction: column;
  padding: 16px;
}`,
    `.chart-panel {
  display: flex;
  flex-direction: column;
  padding: 16px;
  min-height: 240px;
}`
  ],
  [
    `.insight-block {
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
}`,
    `.insight-block {
  padding: 14px;
  border-radius: 6px;
  background: #f8fafc;
  border: 1px solid rgba(0,0,0,0.04);
}`
  ],
  [
    `.msg-avatar {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--ai-primary-soft);
  color: var(--ai-primary);
  flex: 0 0 auto;
}`,
    `.msg-avatar {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--ai-primary-soft);
  color: var(--ai-primary);
  flex: 0 0 auto;
}`
  ],
  [
    `.conversation-panel,
.aside-card {
  padding: 18px;
}`,
    `.conversation-panel,
.aside-card {
  padding: 24px;
  height: 100%;
}`
  ]
];

cssReplacements.forEach(([oldStr, newStr]) => {
  if (content.includes(oldStr)) {
    content = content.replace(oldStr, newStr);
  }
});

const mediaQOld = `@media (max-width: 1280px) {
  .visual-dashboard {
    grid-template-columns: minmax(0, 1fr);
  }

  .metrics-panel {
    grid-template-rows: auto;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .mode-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}`;

const mediaQNew = `@media (max-width: 1280px) {
  .workbench-split-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .metrics-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .mode-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}`;

if (content.includes(mediaQOld)) {
  content = content.replace(mediaQOld, mediaQNew);
}

fs.writeFileSync(targetPath, content, 'utf-8');
console.log('Patch complete!');
