<template>
  <div :class="['ai-command-center', 'bento-lively-theme', `theme-${settingsForm.theme}`]">
    <!-- 活泼的环境光背景 (颜色随主题变量动态变化) -->
    <div class="ambient-background">
      <div class="glow-orb orb-pink"></div>
      <div class="glow-orb orb-purple"></div>
      <div class="glow-orb orb-cyan"></div>
      <div class="glow-orb orb-yellow"></div>
    </div>

    <div class="workbench-layout">
      <!-- 左侧：Bento 活力看板 -->
      <aside class="bento-sidebar">
        <div class="bento-header glass-card slide-up" style="animation-delay: 0.1s;">
          <div class="brand-title">
            <div class="logo-box pulse-logo">
              <!-- 统一使用 3D 机器人形象 -->
              <img :src="bot3dImage" alt="Bot" class="avatar-image bot-img-3d" />
            </div>
            <!-- 动态绑定助理名称 -->
            <span class="gradient-text">{{ settingsForm.botName }} AI 助理</span>
          </div>
          <span class="system-status"><span class="dot pulse-green"></span> 状态极佳</span>
        </div>

        <!-- 核心指标 -->
        <div class="bento-grid">
          <div class="bento-item glass-card stat-card card-blue slide-up" style="animation-delay: 0.2s;">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <span class="label">守护长者</span>
              <span class="value">342<span class="unit">位</span></span>
            </div>
          </div>
          <div class="bento-item glass-card stat-card card-purple slide-up" style="animation-delay: 0.3s;">
            <div class="stat-icon"><el-icon><DataLine /></el-icon></div>
            <div class="stat-info">
              <span class="label">今日处理</span>
              <span class="value">12.5<span class="unit">万条</span></span>
            </div>
          </div>
          <!-- 告警卡片 -->
          <div class="bento-item glass-card alert-card card-red slide-up" style="animation-delay: 0.4s;">
            <div class="alert-header">
              <el-icon class="alert-icon"><WarningFilled /></el-icon>
              <span>需关注长者</span>
            </div>
            <div class="alert-value">3 <span class="unit">人</span></div>
            <div class="alert-wave"></div>
          </div>
        </div>

        <!-- 智能建议 Prompt (自动横向切换轮播图版) -->
        <div class="bento-item glass-card prompt-guide slide-up" style="animation-delay: 0.5s;">
          <h4 class="guide-title"><el-icon><MagicStick /></el-icon> 你可以这样问{{ settingsForm.botName }}：</h4>
          
          <el-carousel class="guide-carousel" height="72px" indicator-position="outside" :interval="3500" arrow="never">
            <el-carousel-item>
              <button class="guide-btn" @click="askCopilot('请给我一份今天的健康巡检建议')">
                <span class="icon-wrap bg-blue"><el-icon><Calendar /></el-icon></span>
                <span class="text">今日健康巡检建议</span>
              </button>
            </el-carousel-item>
            <el-carousel-item>
              <button class="guide-btn" @click="askCopilot('帮我分析张爷爷近期的睡眠质量')">
                <span class="icon-wrap bg-purple"><el-icon><Moon /></el-icon></span>
                <span class="text">长者睡眠质量分析</span>
              </button>
            </el-carousel-item>
            <el-carousel-item>
              <button class="guide-btn" @click="askCopilot('预测下周可能的心血管高危人员')">
                <span class="icon-wrap bg-orange"><el-icon><DataAnalysis /></el-icon></span>
                <span class="text">心血管风险预测</span>
              </button>
            </el-carousel-item>
            <el-carousel-item>
              <button class="guide-btn" @click="askCopilot('导出昨晚所有心率异常的长者名单')">
                <span class="icon-wrap bg-green"><el-icon><Download /></el-icon></span>
                <span class="text">异常数据一键导出</span>
              </button>
            </el-carousel-item>
          </el-carousel>
        </div>
      </aside>

      <!-- 右侧：AI 交互核心舱 -->
      <main class="copilot-main glass-card slide-up" style="animation-delay: 0.2s;">
        
        <!-- 右侧对话舱专属头部 -->
        <header class="copilot-header">
          <div class="header-bot-info">
            <div class="bot-avatar-glow">
              <img :src="bot3dImage" alt="Bot" class="bot-avatar-img bot-img-3d" />
            </div>
            <div class="bot-text">
              <h3>{{ settingsForm.botName }}</h3>
              <span class="status"><span class="dot"></span> 医疗级 AI 引擎运行中</span>
            </div>
          </div>
          <div class="header-actions">
            <!-- 下拉菜单 -->
            <el-dropdown trigger="click" @command="handleHeaderCommand">
              <el-button circle plain size="small"><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="clear"><el-icon><Delete /></el-icon> 清除当前对话</el-dropdown-item>
                  <el-dropdown-item command="settings"><el-icon><Setting /></el-icon> 助理偏好设置</el-dropdown-item>
                  <el-dropdown-item command="exportChat" divided><el-icon><Document /></el-icon> 导出对话记录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </header>

        <div class="chat-viewport" ref="chatScroll">
          <transition-group name="chat-flow" tag="div" class="chat-list">
            
            <div v-for="msg in messages" :key="msg.id" :class="['message-box', msg.role]">
              <!-- 头像 -->
              <div class="msg-avatar" :class="msg.role">
                <img v-if="msg.role === 'ai'" :src="bot3dImage" class="avatar-image bot-img-3d" alt="Bot" />
                <el-icon v-else><Avatar /></el-icon>
              </div>

              <!-- 气泡内容 -->
              <div class="msg-content-wrapper">
                <div class="msg-bubble" :class="{ 'rich-media': msg.type !== 'text' }">
                  <!-- 纯文本 -->
                  <div v-if="msg.type === 'text'" v-html="msg.content" class="md-text"></div>
                  
                  <!-- 数据表格 widget -->
                  <div v-if="msg.type === 'table'" class="widget-box">
                    <div class="widget-header">
                      <el-icon><List /></el-icon> {{ settingsForm.botName }}为您整理的名单：
                    </div>
                    <el-table :data="msg.data" size="small" class="glass-table" :row-class-name="tableRowClassName">
                      <el-table-column prop="name" label="姓名" width="90" />
                      <el-table-column prop="room" label="房间号" width="100" />
                      <el-table-column prop="issue" label="异常类型" />
                      <el-table-column prop="time" label="发生时间" width="140" />
                    </el-table>
                  </div>

                  <!-- 可视化图表 widget -->
                  <div v-if="msg.type === 'chart'" class="widget-box">
                    <div class="widget-header">
                      <el-icon><TrendCharts /></el-icon> 相关的健康体征走势：
                    </div>
                    <div :id="'chart-' + msg.id" class="echarts-container"></div>
                  </div>
                </div>

                <!-- 交互按钮 -->
                <transition name="fade">
                  <div v-if="msg.role === 'ai' && msg.actions && msg.actions.length" class="action-chips">
                    <button 
                      v-for="(action, idx) in msg.actions" 
                      :key="idx" 
                      class="chip-btn"
                      @click="handleAction(action, msg)">
                      {{ action }}
                    </button>
                  </div>
                </transition>
              </div>
            </div>

            <!-- 思考状态 -->
            <div v-if="isTyping" key="typing" class="message-box ai">
              <div class="msg-avatar ai">
                <img :src="bot3dImage" class="avatar-image bot-img-3d" alt="Bot" />
              </div>
              <div class="msg-content-wrapper">
                <div class="msg-bubble typing">
                  <span class="typing-text">{{ settingsForm.botName }}正在努力思考</span>
                  <div class="dot-flashing"></div>
                </div>
              </div>
            </div>
          </transition-group>
        </div>

        <!-- 悬浮胶囊输入舱及全息 3D 机器人 -->
        <div class="floating-input-zone">
          <div class="input-center-wrapper">
            
            <!-- 🌟 全新无框的 3D 全息悬浮机器人 🌟 -->
            <div class="dynamic-mascot-zone" :class="{ 'is-thinking': isTyping }">
              <!-- 交互提示气泡 -->
              <transition name="fade-bounce">
                <div v-if="isTyping" class="mascot-bubble">
                  <el-icon class="is-loading"><Loading /></el-icon> 高速运算中...
                </div>
                <div v-else-if="isInputFocused" class="mascot-bubble">
                  <el-icon><Microphone /></el-icon> 正在聆听指令...
                </div>
              </transition>
              
              <!-- 机器人本体 (真正的 3D 悬浮效果) -->
              <div class="mascot-body" @click="pokeMascot">
                <!-- 透明 3D 图像 -->
                <div class="hologram-model">
                  <img :src="bot3dImage" alt="3D Bot" />
                  <div class="scan-line"></div>
                </div>
                <!-- 底部能量投影环 (模拟3D底座视角) -->
                <div class="energy-ring ring-inner"></div>
                <div class="energy-ring ring-outer"></div>
                <div class="holo-base-glow"></div>
              </div>
            </div>

            <div class="pill-input-box" :class="{ 'focused': isInputFocused }">
              <el-input
                v-model="inputText"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 3 }"
                resize="none"
                :placeholder="`呼叫${settingsForm.botName}... (支持自然语言对话)`"
                @focus="isInputFocused = true"
                @blur="isInputFocused = false"
                @keyup.enter.exact.prevent="sendMessage"
              />
              <button class="send-action-btn" :class="{ 'ready': inputText.trim().length > 0 }" :disabled="isTyping" @click="sendMessage">
                <el-icon v-if="!isTyping"><Position /></el-icon>
                <el-icon v-else class="spin"><Loading /></el-icon>
              </button>
            </div>
          </div>
          
          <div class="sec-footer">
            <el-icon><Lock /></el-icon> 医疗级数据加密保护 · {{ settingsForm.botName }} 全息驱动引擎
          </div>
        </div>
      </main>
    </div>

    <!-- 助理偏好设置抽屉 -->
    <el-drawer
      v-model="showSettingsDrawer"
      title="助理偏好设置"
      direction="rtl"
      size="380px"
      class="glass-drawer"
      :show-close="true"
    >
      <div class="drawer-settings-content">
        <el-form label-position="top" :model="settingsForm">
          <el-form-item label="助理名称" class="setting-item">
            <el-input v-model="settingsForm.botName" placeholder="给助理起个名字" clearable>
              <template #prefix><el-icon><Service /></el-icon></template>
            </el-input>
            <div class="form-tip">自定义名称不仅修改全局称呼，还会改变助理的性格！</div>
          </el-form-item>

          <el-form-item label="回答语气风格" class="setting-item">
            <el-radio-group v-model="settingsForm.tone" class="custom-radio-group">
              <el-radio-button label="professional">专业严谨</el-radio-button>
              <el-radio-button label="lively">活泼亲切</el-radio-button>
              <el-radio-button label="concise">简明扼要</el-radio-button>
            </el-radio-group>
            <div class="form-tip">将作为底层 Prompt 深度介入大语言模型的回复。</div>
          </el-form-item>

          <el-form-item label="自动语音播报" class="setting-item flex-row-item">
            <div class="item-text">
              <span>语音朗读回答</span>
              <div class="form-tip no-margin">开启后将调用浏览器 TTS 自动播放</div>
            </div>
            <el-switch v-model="settingsForm.autoSpeak" active-color="var(--ai-primary)" />
          </el-form-item>

          <el-form-item label="主题颜色偏好" class="setting-item">
            <div class="theme-color-picker">
              <div class="color-dot pink" :class="{ 'active': settingsForm.theme === 'pink' }" @click="settingsForm.theme = 'pink'"></div>
              <div class="color-dot purple" :class="{ 'active': settingsForm.theme === 'purple' }" @click="settingsForm.theme = 'purple'"></div>
              <div class="color-dot blue" :class="{ 'active': settingsForm.theme === 'blue' }" @click="settingsForm.theme = 'blue'"></div>
              <div class="color-dot green" :class="{ 'active': settingsForm.theme === 'green' }" @click="settingsForm.theme = 'green'"></div>
            </div>
            <div class="form-tip" style="margin-top:12px;">立即改变全局发光体、全息投影和光晕的颜色。</div>
          </el-form-item>
        </el-form>

        <div class="drawer-footer-actions">
          <el-button round class="cancel-btn" @click="showSettingsDrawer = false">完成设置</el-button>
          <el-button type="primary" round class="save-btn" @click="saveSettings">测试语音并保存</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Monitor, User, DataLine, WarningFilled, MagicStick, Search, Document, Cpu, Position, Loading, Lock, Avatar, Calendar, Moon, DataAnalysis, Download, List, TrendCharts, MoreFilled, Delete, Setting, Service, Microphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chatAi } from '@/api/ai' 
import * as echarts from 'echarts'

interface ChatMessage {
  id: number
  role: 'user' | 'ai'
  type: 'text' | 'table' | 'chart'
  content: string
  data?: any
  actions?: string[]
}

const inputText = ref('')
const isTyping = ref(false)
const isInputFocused = ref(false)
const chatScroll = ref<HTMLElement | null>(null)

// 选用一张高质量的去背景 3D 机器人资源
const bot3dImage = 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Robot.png'

// --- 助理偏好设置状态 ---
const showSettingsDrawer = ref(false)
const settingsForm = ref({
  botName: '小豆',
  tone: 'lively',
  autoSpeak: false,
  theme: 'purple'
})

const getAvatarUrl = () => bot3dImage

// 语音朗读
const speakText = (text: string) => {
  if (!settingsForm.value.autoSpeak) return
  window.speechSynthesis.cancel() 
  const plainText = text.replace(/<[^>]+>/g, '').replace(/&[^;]+;/g, '')
  const utterance = new SpeechSynthesisUtterance(plainText)
  utterance.lang = 'zh-CN'
  utterance.rate = settingsForm.value.tone === 'lively' ? 1.1 : 1.0 
  utterance.pitch = settingsForm.value.tone === 'lively' ? 1.2 : 1.0 
  window.speechSynthesis.speak(utterance)
}

const saveSettings = () => {
  showSettingsDrawer.value = false
  ElMessage.success(`设置已更新！现在的助理是 ${settingsForm.value.botName} 啦 ✨`)
  const tempAutoSpeak = settingsForm.value.autoSpeak; settingsForm.value.autoSpeak = true;
  speakText(`设置已保存，全息引擎加载完毕。我是您的管家${settingsForm.value.botName}`);
  settingsForm.value.autoSpeak = tempAutoSpeak;
}

// “戳一戳” 动态机器人互动功能
const pokeMascot = () => {
  if (isTyping.value) return;
  const phrases = [
    `哎呀，别戳${settingsForm.value.botName}啦！✨`, 
    `${settingsForm.value.botName}时刻准备为您服务！`, 
    `今天也是元气满满的一天哦~ 🚀`, 
    `全院健康数据都在我的监测中！🛡️`
  ];
  const phrase = phrases[Math.floor(Math.random() * phrases.length)];
  messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: phrase });
  speakText(phrase);
  scrollToBottom();
}

// 初始消息
const getInitialMessage = (): ChatMessage => ({
  id: 1, role: 'ai', type: 'text',
  content: `您好！我是您的专属 3D 智能助理 <strong style="color:var(--ai-primary)">${settingsForm.value.botName}</strong> 🤖<br/>您可以随时在底部的输入框向我发送指令！`
})

const messages = ref<ChatMessage[]>([ getInitialMessage() ])

const handleHeaderCommand = (command: string) => {
  if (command === 'clear') {
    messages.value = [ getInitialMessage() ]
    ElMessage.success('对话记录已清空 🧹')
  } else if (command === 'settings') {
    showSettingsDrawer.value = true 
  } else if (command === 'exportChat') {
    ElMessage.info('对话导出功能将在下一版本开放 📄')
  }
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return
  
  messages.value.push({ id: Date.now(), role: 'user', type: 'text', content: text })
  inputText.value = ''
  isTyping.value = true
  scrollToBottom()

  try {
    let promptModifier = ''
    if (settingsForm.value.tone === 'professional') promptModifier = '\n(系统指令: 请务必使用非常专业、严谨、客观的医疗语气回答)'
    if (settingsForm.value.tone === 'lively') promptModifier = '\n(系统指令: 请务必使用非常活泼、亲切、带有emoji的可爱语气回答，就像跟长辈说话一样)'
    if (settingsForm.value.tone === 'concise') promptModifier = '\n(系统指令: 请务必极其简明扼要，一句话直击重点，不要说废话)'

    const res = await chatAi(text + promptModifier)
    let replyText = typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2)
    replyText = replyText.replace(/\n/g, '<br/>')

    let type: 'text' | 'table' | 'chart' = 'text'
    let data: any = null
    let actions: string[] = []

    if (text.includes('心率') || text.includes('名单') || text.includes('导出')) {
      type = 'table'
      data = [{ name: '张建国', room: 'A楼-302', issue: '心动过缓 (45bpm)', time: '02:15 - 02:40' }, { name: '李桂英', room: 'B楼-105', issue: '心率飙升 (110bpm)', time: '04:22 - 04:30' }]
      actions = ['导出 Excel 名单', '一键下发巡检工单']
    } else if (text.includes('分析') || text.includes('趋势') || text.includes('睡眠') || text.includes('预测')) {
      type = 'chart'
      actions = ['调整设备监测频率', '生成报告并下载']
    } else if (text.includes('报告')) {
      actions = ['生成报告并下载']
    }

    const msgId = Date.now()
    messages.value.push({ id: msgId, role: 'ai', type: type, content: replyText, data: data, actions: actions })

    speakText(replyText)

    if (type === 'chart') {
      nextTick(() => renderChart(`chart-${msgId}`))
    }

  } catch (error: any) {
    messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `呜呜，${settingsForm.value.botName}的服务器断开连接啦 🔌<br/><span style="color: #ef4444;font-size:13px;">(${error.message || '网络错误'})</span>` })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const askCopilot = (text: string) => {
  inputText.value = text
  sendMessage()
}

// 导出相关...
const exportToCsv = (tableData: any[]) => {
  if (!tableData || tableData.length === 0) return ElMessage.warning('数据为空')
  const headers = ['姓名', '房间号', '异常类型', '发生时间']; const keys = ['name', 'room', 'issue', 'time']; const csvRows = [headers.join(',')]
  tableData.forEach(row => csvRows.push(keys.map(key => `"${row[key] || ''}"`).join(',')))
  const url = URL.createObjectURL(new Blob(['\uFEFF' + csvRows.join('\n')], { type: 'text/csv;charset=utf-8;' }))
  const link = document.createElement('a'); link.href = url; link.download = `DataExport_${Date.now()}.csv`; link.click(); URL.revokeObjectURL(url)
}
const exportToWord = () => {
  const url = URL.createObjectURL(new Blob([`<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'><title>报告</title></head><body><h1 style="text-align: center; color: ${settingsForm.value.theme === 'purple' ? '#d946ef' : '#3b82f6'};">全局健康风险分析报告</h1><p><strong>生成时间：</strong>${new Date().toLocaleString()}</p></body></html>`], { type: 'application/msword;charset=utf-8' }))
  const link = document.createElement('a'); link.href = url; link.download = `${settingsForm.value.botName}整理的报告_${Date.now()}.doc`; link.click(); URL.revokeObjectURL(url)
}
const handleAction = (action: string, msg?: ChatMessage) => {
  if (action === '导出 Excel 名单') { exportToCsv(msg?.data); return messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `没问题！${settingsForm.value.botName}已经把 Excel 名单下载到您的电脑啦 📁` }), speakText('已经把名单下载到您的电脑啦'), scrollToBottom() }
  if (action === '生成报告并下载') { exportToWord(); return messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `报告整理完毕！${settingsForm.value.botName}已经将 Word 文档发送给您啦 📝` }), speakText('报告整理完毕，已发送给您'), scrollToBottom() }
  ElMessage.success(`指令下发成功: ${action}`)
  messages.value.push({ id: Date.now(), role: 'ai', type: 'text', content: `系统已执行任务：<b>${action}</b>。${settingsForm.value.botName}持续为您守护！` })
  speakText(`系统已执行任务：${action}`)
  scrollToBottom()
}

const renderChart = (domId: string) => {
  const dom = document.getElementById(domId)
  if (dom) {
    const myChart = echarts.init(dom)
    const chartColor = settingsForm.value.theme === 'purple' ? '#d946ef' : (settingsForm.value.theme === 'pink' ? '#f43f5e' : (settingsForm.value.theme === 'green' ? '#10b981' : '#3b82f6'));
    
    myChart.setOption({
      grid: { left: '2%', right: '2%', bottom: '5%', top: '15%', containLabel: true },
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(255, 255, 255, 0.95)', borderColor: 'rgba(255,255,255,0.5)', padding: 12, textStyle: { color: '#1e293b' }, extraCssText: 'box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); border-radius: 12px;' },
      legend: { data: ['收缩压', '舒张压'], icon: 'circle', top: 0, right: 0 },
      xAxis: { type: 'category', data: ['一', '二', '三', '四', '五', '六', '日'], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#64748b' } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: 'rgba(0,0,0,0.04)' } }, axisLabel: { color: '#64748b' } },
      series: [
        { name: '收缩压', type: 'line', data: [120, 122, 145, 125, 118, 121, 120], smooth: true, lineStyle: { color: '#f43f5e', width: 4 }, itemStyle: { color: '#f43f5e', borderWidth: 3, borderColor: '#fff' }, symbolSize: 10 },
        { name: '舒张压', type: 'line', data: [80, 82, 95, 85, 78, 80, 81], smooth: true, lineStyle: { color: chartColor, width: 4 }, itemStyle: { color: chartColor, borderWidth: 3, borderColor: '#fff' }, symbolSize: 10, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: chartColor + '33' }, { offset: 1, color: chartColor + '00' }]) } }
      ]
    })
  }
}
const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => rowIndex % 2 === 1 ? 'glass-row-striped' : ''
const scrollToBottom = () => nextTick(() => { if (chatScroll.value) { chatScroll.value.scrollTop = chatScroll.value.scrollHeight } })
</script>

<style scoped lang="scss">
/* --- 真实注入功能的 CSS 变量主题切换 --- */
.bento-lively-theme {
  --ai-primary: #d946ef;
  --ai-primary-grad: linear-gradient(135deg, #d946ef 0%, #8b5cf6 100%);
  --ai-glow: rgba(217, 70, 239, 0.3);

  &.theme-pink { --ai-primary: #f43f5e; --ai-primary-grad: linear-gradient(135deg, #f43f5e 0%, #fb7185 100%); --ai-glow: rgba(244, 63, 94, 0.3); }
  &.theme-blue { --ai-primary: #3b82f6; --ai-primary-grad: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%); --ai-glow: rgba(59, 130, 246, 0.3); }
  &.theme-green { --ai-primary: #10b981; --ai-primary-grad: linear-gradient(135deg, #34d399 0%, #10b981 100%); --ai-glow: rgba(16, 185, 129, 0.3); }
}

$bg-color: #f8fafc;
$glass-bg: rgba(255, 255, 255, 0.7);
$glass-border: rgba(255, 255, 255, 0.9);
$glass-shadow: 0 10px 40px 0 rgba(148, 163, 184, 0.15);
$blue-grad: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
$purple-grad: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
$orange-grad: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
$green-grad: linear-gradient(135deg, #34d399 0%, #10b981 100%);
$text-main: #1e293b;
$text-secondary: #475569;
$text-muted: #94a3b8;
$radius-xl: 24px;
$radius-lg: 16px;

/* --- 全局头像控制，去背景纯净 3D --- */
.bot-img-3d {
  background: transparent !important;
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

.bento-lively-theme {
  position: relative; padding: 24px; background: $bg-color; height: calc(100vh - 84px); overflow: hidden;
  font-family: "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  
  .ambient-background {
    position: absolute; top: 0; left: 0; width: 100%; height: 100%; overflow: hidden; pointer-events: none; z-index: 0;
    .glow-orb { position: absolute; border-radius: 50%; filter: blur(90px); opacity: 0.6; animation: float 12s infinite ease-in-out alternate; transition: background 0.5s ease; }
    .orb-pink { width: 450px; height: 450px; background: #fce7f3; top: -100px; left: -100px; }
    .orb-purple { width: 500px; height: 500px; background: var(--ai-glow); bottom: -200px; right: -100px; animation-delay: -3s; }
    .orb-cyan { width: 350px; height: 350px; background: #e0f2fe; top: 50%; left: 40%; animation-delay: -6s; opacity: 0.5; }
    .orb-yellow { width: 300px; height: 300px; background: #fef3c7; top: 10%; right: 20%; animation-delay: -2s; opacity: 0.4; }
  }

  .workbench-layout { position: relative; z-index: 1; display: flex; gap: 24px; height: 100%; }

  .glass-card {
    background: $glass-bg; backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px);
    border: 1px solid $glass-border; border-radius: $radius-xl; box-shadow: $glass-shadow;
  }

  .bento-sidebar {
    width: 350px; display: flex; flex-direction: column; gap: 20px;
    
    .bento-header {
      padding: 16px 24px; display: flex; justify-content: space-between; align-items: center;
      .brand-title { display: flex; align-items: center; gap: 12px; font-weight: 800; font-size: 20px; letter-spacing: -0.5px;
        .pulse-logo { 
          width: 46px; height: 46px; background: rgba(255,255,255,0.5); padding: 4px; border-radius: 14px; 
          display: flex; justify-content: center; align-items: center; box-shadow: 0 4px 12px rgba(0,0,0,0.05);
          .avatar-image { width: 100%; height: 100%; object-fit: contain; transition: all 0.5s ease; }
        }
        .gradient-text { background: var(--ai-primary-grad); -webkit-background-clip: text; color: transparent; transition: all 0.5s ease; }
      }
      .system-status { font-size: 13px; color: #10b981; display: flex; align-items: center; font-weight: 600;
        .dot { width: 8px; height: 8px; background: #10b981; border-radius: 50%; margin-right: 6px; }
        .pulse-green { animation: pulseGreen 2s infinite; }
      }
    }

    .bento-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
      .bento-item { padding: 20px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); cursor: default; border: none;
        &:hover { transform: translateY(-4px); box-shadow: 0 15px 30px rgba(0,0,0,0.08); }
      }
      .card-blue { background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%); }
      .card-purple { background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%); }
      .card-red { grid-column: span 2; position: relative; overflow: hidden; background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%); }

      .stat-card {
        display: flex; flex-direction: column; gap: 12px;
        .stat-icon { width: 44px; height: 44px; border-radius: 14px; display: flex; justify-content: center; align-items: center; font-size: 22px; background: #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.05); color: var(--ai-primary); transition: color 0.5s ease; }
        .stat-info { display: flex; flex-direction: column; .label { font-size: 14px; color: $text-secondary; font-weight: 600; } .value { font-size: 26px; font-weight: 800; color: $text-main; margin-top: 4px; .unit { font-size: 13px; color: $text-muted; margin-left: 4px; font-weight: 500; } } }
      }
      
      .alert-card {
        .alert-header { display: flex; align-items: center; gap: 8px; color: #ef4444; font-weight: 700; font-size: 15px; .alert-icon { font-size: 20px; } }
        .alert-value { font-size: 36px; font-weight: 800; color: #b91c1c; margin-top: 8px; .unit { font-size: 16px; font-weight: 600; } }
        .alert-wave { position: absolute; right: -20px; bottom: -20px; width: 120px; height: 120px; background: radial-gradient(circle, rgba(239,68,68,0.15) 0%, transparent 70%); border-radius: 50%; animation: pulseRed 3s infinite; }
      }
    }

    /* --- 美化：将推荐 Prompt 框改为黄色调主题及横向轮播 --- */
    .prompt-guide {
      flex: 1; padding: 24px; display: flex; flex-direction: column; overflow: hidden;
      background: linear-gradient(135deg, rgba(255, 251, 235, 0.9) 0%, rgba(254, 243, 199, 0.8) 100%);
      border: 1px solid rgba(251, 191, 36, 0.4);
      box-shadow: 0 10px 30px rgba(245, 158, 11, 0.1);

      .guide-title { 
        margin: 0 0 16px 0; font-size: 16px; color: #92400e; display: flex; align-items: center; gap: 8px; font-weight: 800; 
        .el-icon { color: #f59e0b; font-size: 20px; filter: drop-shadow(0 2px 4px rgba(245, 158, 11, 0.3)); }
      }
      
      .guide-carousel {
        width: 100%;
        border-radius: 16px;
        
        /* 定制化黄色主题的轮播指示器 */
        :deep(.el-carousel__indicators--outside) {
          margin-top: 4px;
        }
        :deep(.el-carousel__indicator button) {
          background-color: #fcd34d;
          height: 4px;
          border-radius: 4px;
          transition: all 0.3s;
          opacity: 0.6;
        }
        :deep(.el-carousel__indicator.is-active button) {
          background-color: #f59e0b;
          width: 20px;
          opacity: 1;
        }
      }

      .guide-btn {
        width: 100%; height: 100%; box-sizing: border-box; display: flex; align-items: center; gap: 14px; padding: 0 20px; border-radius: 16px;
        background: rgba(255, 255, 255, 0.7); border: 1px solid rgba(253, 230, 138, 0.6); cursor: pointer; transition: 0.3s;
        &:hover { background: #fff; border-color: #fbbf24; box-shadow: 0 8px 20px rgba(245, 158, 11, 0.15); transform: translateY(-3px); }
        .icon-wrap { width: 36px; height: 36px; border-radius: 10px; color: #fff; display: flex; justify-content: center; align-items: center; font-size: 18px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
        .bg-blue { background: $blue-grad; } .bg-purple { background: $purple-grad; } .bg-orange { background: $orange-grad; } .bg-green { background: $green-grad; }
        .text { font-size: 15px; font-weight: 700; color: #78350f; }
      }
    }
  }

  .copilot-main {
    flex: 1; display: flex; flex-direction: column; position: relative; overflow: hidden;
    
    .copilot-header {
      padding: 16px 28px; border-bottom: 1px solid rgba(0,0,0,0.05); display: flex; align-items: center; justify-content: space-between; background: rgba(255,255,255,0.3);
      .header-bot-info {
        display: flex; align-items: center; gap: 14px;
        .bot-avatar-glow {
          width: 52px; height: 52px; border-radius: 16px; padding: 2px;
          background: rgba(255,255,255,0.5); transition: all 0.5s ease;
          box-shadow: 0 4px 12px var(--ai-glow);
          display: flex; justify-content: center; align-items: center;
          .bot-avatar-img { width: 90%; height: 90%; object-fit: contain; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1)); }
        }
        .bot-text {
          display: flex; flex-direction: column; gap: 4px;
          h3 { margin: 0; font-size: 16px; font-weight: 800; color: $text-main; transition: color 0.5s ease; }
          .status { font-size: 12px; color: #10b981; display: flex; align-items: center; font-weight: 600;}
          .dot { width: 6px; height: 6px; background: #10b981; border-radius: 50%; margin-right: 6px; animation: pulseGreen 2s infinite; }
        }
      }
      .header-actions {
        .el-button { &:hover { color: var(--ai-primary); border-color: var(--ai-primary); background: var(--ai-glow); } }
      }
    }

    .chat-viewport {
      flex: 1; padding: 30px 40px 40px; overflow-y: auto;
      &::-webkit-scrollbar { display: none; } 
      .chat-list { padding-bottom: 140px; }

      .message-box {
        display: flex; gap: 16px; margin-bottom: 32px;
        
        .msg-avatar {
          width: 46px; height: 46px; flex-shrink: 0; border-radius: 14px; display: flex; justify-content: center; align-items: center; font-weight: bold; font-size: 20px; box-shadow: 0 4px 10px rgba(0,0,0,0.05);
          &.ai { background: rgba(255,255,255,0.6); padding: 4px; }
          &.user { background: #fff; color: $text-secondary; font-size: 16px; border: 1px solid #e2e8f0;}
          .avatar-image { width: 100%; height: 100%; object-fit: contain; }
        }

        &.user { flex-direction: row-reverse; .msg-bubble { background: #fff; color: $text-main; border: 1px solid #e2e8f0; border-radius: 20px 4px 20px 20px; box-shadow: 0 8px 20px rgba(148,163,184,0.1); } }
        &.ai { .msg-bubble { background: rgba(255,255,255,0.8); color: $text-main; border: 1px solid #fff; border-radius: 4px 20px 20px 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); } }

        .msg-content-wrapper {
          max-width: 80%; display: flex; flex-direction: column; align-items: flex-start;
          
          .msg-bubble {
            padding: 16px 20px; font-size: 15px; line-height: 1.6; letter-spacing: 0.2px;
            :deep(strong) { color: var(--ai-primary); font-weight: 700; transition: color 0.5s ease; }
            &.rich-media { padding: 12px; background: transparent; border: none; box-shadow: none; width: 100%; }

            .widget-box {
              background: #fff; border: 1px solid #e2e8f0; border-radius: $radius-lg; padding: 20px; box-shadow: 0 8px 25px rgba(148,163,184,0.1); width: 100%;
              .widget-header { display: flex; align-items: center; gap: 8px; font-weight: 800; font-size: 16px; color: $text-main; margin-bottom: 20px; .el-icon { color: var(--ai-primary); font-size: 20px; transition: color 0.5s ease;} }
              
              :deep(.glass-table) {
                border-radius: 12px; overflow: hidden; border: 1px solid #f1f5f9;
                th.el-table__cell { background: #f8fafc; color: $text-secondary; font-weight: 700; border-bottom: none; padding: 12px 0; }
                td.el-table__cell { border-bottom: 1px solid #f1f5f9; padding: 14px 0; color: $text-main; font-weight: 500; }
                .glass-row-striped { background: #f8fafc; }
              }
              .echarts-container { height: 280px; width: 100%; min-width: 500px; }
            }
          }

          .action-chips {
            display: flex; flex-wrap: wrap; gap: 12px; margin-top: 16px;
            .chip-btn {
              padding: 10px 20px; border-radius: 100px; background: #fff; border: 1px solid #e2e8f0; color: var(--ai-primary); font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 0 4px 10px rgba(0,0,0,0.02);
              &:hover { background: var(--ai-primary-grad); color: #fff; border-color: transparent; box-shadow: 0 6px 15px var(--ai-glow); transform: translateY(-2px); }
            }
          }
        }
      }

      .typing {
        padding: 16px 24px !important; display: flex; align-items: center; gap: 12px;
        .typing-text { font-size: 14px; color: var(--ai-primary); font-weight: 600; }
        .dot-flashing {
          position: relative; width: 6px; height: 6px; border-radius: 5px; background-color: var(--ai-primary); color: var(--ai-primary); animation: dotFlashing 1s infinite linear alternate; animation-delay: .5s;
          &::before, &::after { content: ''; display: inline-block; position: absolute; top: 0; width: 6px; height: 6px; border-radius: 5px; background-color: var(--ai-primary); color: var(--ai-primary); animation: dotFlashing 1s infinite alternate; }
          &::before { left: -12px; animation-delay: 0s; }
          &::after { left: 12px; animation-delay: 1s; }
        }
      }
    }

    .floating-input-zone {
      position: absolute; bottom: 0; left: 0; width: 100%; padding: 0 40px 30px 40px;
      background: linear-gradient(to top, rgba(248,250,252,0.95) 40%, transparent); pointer-events: none;
      display: flex; flex-direction: column; align-items: center;
      
      .input-center-wrapper {
        position: relative; width: 100%; max-width: 850px; display: flex; flex-direction: column; align-items: center;
      }

      /* 🌟 无框 3D 悬浮机器人 🌟 */
      /* 已将小豆整体移至右侧，避免遮挡左侧的 AI 聊天记录 */
      .dynamic-mascot-zone {
        position: absolute; bottom: 100%; right: 40px; display: flex; flex-direction: column; align-items: center; z-index: 10; pointer-events: none; margin-bottom: -20px;
        
        .mascot-bubble {
          background: var(--ai-primary-grad); color: #fff; padding: 6px 14px; 
          border-radius: 12px 12px 0 12px; /* 调整气泡小尾巴到右下角 */
          font-size: 13px; font-weight: 600; box-shadow: 0 4px 15px var(--ai-glow); margin-bottom: 20px; display: flex; align-items: center; gap: 6px; animation: floatBubble 3s ease-in-out infinite; 
          transform-origin: bottom right; /* 调整动画缩放基点 */
        }
        
        .mascot-body {
          position: relative; width: 85px; height: 85px; animation: floatMascot 4s ease-in-out infinite; pointer-events: auto; cursor: pointer; transition: transform 0.2s;
          &:active { transform: scale(0.95); }
          
          /* 纯透明 3D 悬浮模型 */
          .hologram-model {
            width: 100%; height: 100%; position: relative; z-index: 5;
            display: flex; justify-content: center; align-items: center;
            img { 
              width: 90%; height: 90%; object-fit: contain; opacity: 0.95;
              filter: drop-shadow(0 0 10px var(--ai-primary)); /* 3D 全息发光描边 */
              transition: all 0.5s; 
            }
            .scan-line { position: absolute; top: 0; left: 10%; width: 80%; height: 2px; background: #fff; box-shadow: 0 0 10px #fff, 0 0 20px var(--ai-primary); z-index: 6; animation: scanLine 2s linear infinite; border-radius: 50%; opacity: 0.8; }
          }
          
          /* 3D 透视底座能量环 */
          .energy-ring {
            position: absolute; left: 50%; top: 95%; transform: translate(-50%, -50%) rotateX(75deg); 
            border-radius: 50%; border: 2px solid var(--ai-primary); z-index: 1; transition: border-color 0.5s;
            &.ring-inner { width: 80px; height: 80px; animation: spinRing 3s linear infinite; border-top-color: transparent; border-bottom-color: transparent; }
            &.ring-outer { width: 110px; height: 110px; animation: spinRing 5s linear infinite reverse; border-left-color: transparent; border-right-color: transparent; opacity: 0.4; }
          }
          .holo-base-glow {
            position: absolute; left: 50%; top: 95%; transform: translate(-50%, -50%) rotateX(75deg);
            width: 60px; height: 60px; background: radial-gradient(ellipse at center, var(--ai-primary) 0%, transparent 60%);
            border-radius: 50%; opacity: 0.6; z-index: 0; animation: pulseBase 2s infinite alternate;
          }
        }
        
        &.is-thinking .mascot-body .energy-ring { border-width: 3px; animation-duration: 1.5s; }
      }

      .pill-input-box {
        pointer-events: auto; width: 100%; background: rgba(255,255,255,0.95); backdrop-filter: blur(12px);
        border: 1px solid #e2e8f0; border-radius: 36px; padding: 10px 10px 10px 24px; box-shadow: 0 12px 40px -10px rgba(148,163,184,0.2);
        display: flex; align-items: center; transition: all 0.3s ease; position: relative; z-index: 5;
        &.focused { border-color: var(--ai-primary); box-shadow: 0 15px 50px -10px var(--ai-glow); transform: translateY(-2px); }
        
        :deep(.el-textarea__inner) { background: transparent; border: none; box-shadow: none !important; padding: 12px 0; font-size: 16px; color: $text-main; font-weight: 500; resize: none; &::placeholder { color: #cbd5e1; font-weight: normal; } }
        
        .send-action-btn {
          width: 48px; height: 48px; border-radius: 50%; border: none; background: #f1f5f9; color: #94a3b8; flex-shrink: 0; display: flex; justify-content: center; align-items: center; font-size: 22px; cursor: not-allowed; transition: 0.3s; margin-left: 12px;
          &.ready { background: var(--ai-primary-grad); color: #fff; cursor: pointer; box-shadow: 0 6px 16px var(--ai-glow); &:hover { transform: scale(1.05); } }
          .spin { animation: spin 1s linear infinite; }
        }
      }
      
      .sec-footer { margin-top: 16px; font-size: 13px; color: $text-muted; font-weight: 600; pointer-events: auto; display: flex; align-items: center; gap: 6px; letter-spacing: 0.5px; }
    }
  }
}

/* --- 抽屉侧边栏样式 --- */
.glass-drawer {
  :deep(.el-drawer) { background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(20px); border-left: 1px solid rgba(255, 255, 255, 0.8); }
  :deep(.el-drawer__header) { margin-bottom: 0; padding: 20px 24px; border-bottom: 1px solid #f1f5f9; font-weight: 800; color: $text-main; font-size: 18px; }
  .drawer-settings-content {
    padding: 24px; height: 100%; display: flex; flex-direction: column;
    .setting-item {
      margin-bottom: 24px;
      :deep(.el-form-item__label) { font-weight: 700; color: $text-main; padding-bottom: 8px; font-size: 15px; }
      .form-tip { font-size: 12px; color: $text-muted; margin-top: 8px; line-height: 1.4; }
      .no-margin { margin-top: 2px; }
      .custom-radio-group {
        width: 100%; display: flex;
        :deep(.el-radio-button) {
          flex: 1;
          .el-radio-button__inner { width: 100%; border-radius: 8px; border: 1px solid #e2e8f0; margin-right: 8px; background: #f8fafc; color: $text-secondary; box-shadow: none; transition: all 0.3s;}
          &:last-child .el-radio-button__inner { margin-right: 0; }
          &.is-active .el-radio-button__inner { background: var(--ai-glow); color: var(--ai-primary); border-color: var(--ai-primary); font-weight: bold; }
        }
      }
      .theme-color-picker {
        display: flex; gap: 16px; margin-top: 8px;
        .color-dot {
          width: 32px; height: 32px; border-radius: 50%; cursor: pointer; transition: 0.2s; position: relative;
          &.pink { background: #fce7f3; border: 2px solid #fbcfe8; } &.purple { background: #f3e8ff; border: 2px solid #e9d5ff; } &.blue { background: #e0f2fe; border: 2px solid #bae6fd; } &.green { background: #dcfce3; border: 2px solid #bbf7d0; }
          &:hover { transform: scale(1.1); }
          &.active { transform: scale(1.15); border-color: var(--ai-primary); box-shadow: 0 4px 12px var(--ai-glow); &::after { content: ''; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 12px; height: 12px; background: var(--ai-primary); border-radius: 50%; } }
        }
      }
    }
    .flex-row-item { :deep(.el-form-item__content) { display: flex; justify-content: space-between; align-items: center; width: 100%; } .item-text { display: flex; flex-direction: column; font-weight: 700; color: $text-main; font-size: 15px; } }
    .drawer-footer-actions {
      margin-top: auto; display: flex; gap: 16px; padding-top: 24px; border-top: 1px solid #f1f5f9;
      .el-button { flex: 1; height: 44px; font-size: 15px; font-weight: bold; transition: all 0.3s;}
      .cancel-btn { background: #f1f5f9; border: none; color: $text-secondary; &:hover { background: #e2e8f0; } }
      .save-btn { background: var(--ai-primary-grad); border: none; color: white; box-shadow: 0 4px 15px var(--ai-glow); &:hover { opacity: 0.9; transform: translateY(-1px); box-shadow: 0 6px 20px var(--ai-glow); } }
    }
  }
}

/* 动画定义 */
.slide-up { animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) both; }
.chat-flow-enter-active { transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.chat-flow-enter-from { opacity: 0; transform: translateY(20px) scale(0.98); }
.fade-enter-active { transition: opacity 0.5s ease 0.4s; }
.fade-enter-from { opacity: 0; }
.fade-bounce-enter-active { animation: bounceIn 0.5s; }
.fade-bounce-leave-active { animation: bounceIn 0.3s reverse; }

@keyframes slideUpFade { 0% { opacity: 0; transform: translateY(30px); } 100% { opacity: 1; transform: translateY(0); } }
@keyframes bounceIn { 0% { opacity: 0; transform: scale(0.8) translateY(10px); } 60% { opacity: 1; transform: scale(1.05) translateY(-2px); } 100% { opacity: 1; transform: scale(1) translateY(0); } }
@keyframes pulseGreen { 0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); } 70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); } 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); } }
@keyframes pulseRed { 0% { opacity: 0.6; transform: scale(1); } 50% { opacity: 1; transform: scale(1.2); } 100% { opacity: 0.6; transform: scale(1); } }
@keyframes dotFlashing { 0% { background-color: var(--ai-primary); } 50%, 100% { background-color: var(--ai-glow); } }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes float { 0% { transform: translate(0, 0); } 100% { transform: translate(40px, 30px); } }
@keyframes floatMascot { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
@keyframes floatBubble { 0%, 100% { transform: translateY(0) scale(1); } 50% { transform: translateY(-4px) scale(1.02); } }
@keyframes scanLine { 0% { top: 0%; opacity: 0; } 10% { opacity: 1; } 90% { opacity: 1; } 100% { top: 100%; opacity: 0; } }
@keyframes spinRing { 0% { transform: translate(-50%, -50%) rotateX(75deg) rotateZ(0deg); } 100% { transform: translate(-50%, -50%) rotateX(75deg) rotateZ(360deg); } }
@keyframes pulseBase { 0% { opacity: 0.4; transform: translate(-50%, -50%) rotateX(75deg) scale(0.9); } 100% { opacity: 0.8; transform: translate(-50%, -50%) rotateX(75deg) scale(1.1); } }
</style>