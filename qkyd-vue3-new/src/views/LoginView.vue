<template>
  <div class="login-page">
    <img class="login-bg-media" src="/login-bg.webp" alt="" aria-hidden="true" decoding="async" fetchpriority="low" />
    <div class="login-art">
      <h1 class="brand-title">
        <picture class="brand-logo-wrap">
          <source srcset="/logo-qkyd-wide.webp" type="image/webp" />
          <img
            src="/logo-qkyd-wide.png"
            alt="耆康云盾健康监测平台"
            class="brand-logo"
            decoding="async"
            fetchpriority="high"
          />
        </picture>
        <span class="brand-text">耆康云盾健康监测平台</span>
      </h1>
      <p>整合设备数据、AI 风险识别与告警处置，帮助照护团队更快发现问题。</p>
      <ul>
        <li>多源健康数据实时监测</li>
        <li>异常事件可视化处置闭环</li>
        <li>AI 辅助风险评估与趋势分析</li>
      </ul>
    </div>

    <div class="login-card panel">
      <h2>欢迎登录</h2>
      <p class="login-tip">请输入账号信息进入管理台</p>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            size="large"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item v-if="captchaEnabled" label="验证码" prop="code">
          <div class="captcha-row">
            <el-input v-model="loginForm.code" size="large" placeholder="请输入验证码" @keyup.enter="handleLogin" />
            <img v-if="captcha" :src="captcha" alt="验证码" class="captcha-image" @click="refreshCaptcha" />
          </div>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="submitting" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const submitting = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  code: '',
  uuid: ''
})

const captchaEnabled = ref(true)
const captcha = ref('')

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  try {
    const res = await userStore.fetchCaptcha()
    captcha.value = userStore.captcha
    captchaEnabled.value = userStore.captchaEnabled
    loginForm.uuid = res.uuid || ''
    if (!captchaEnabled.value) {
      loginForm.code = ''
      loginForm.uuid = ''
    }
  } catch (error) {
    ElMessage.error('验证码获取失败，请稍后重试')
  }
}

const handleLogin = async () => {
  try {
    submitting.value = true
    await loginFormRef.value?.validate()
    await userStore.handleLogin(loginForm)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    await router.replace('/')
  } catch (error) {
    await refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1.1fr 460px;
  gap: 28px;
  align-items: center;
  padding: 24px;
  background-color: #ffffff; /* 基础深蓝色背景底色 */
}

.login-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at top left, rgba(80, 127, 92, 0.28), transparent 38%),
    linear-gradient(135deg, rgba(12, 36, 44, 0.72), rgba(54, 96, 78, 0.4));
  z-index: 0;
}

.login-bg-media {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  opacity: 0.4;
  pointer-events: none;
  z-index: 0;
  transform: scale(1.02);
}

.login-art {
  position: relative;
  z-index: 1;
  color: #f4f9ff;
  width: fit-content;
  max-width: 560px;
  padding: 16px 18px;
  background: rgba(9, 22, 33, 0.48);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.22);

  h1 {
    margin-bottom: 12px;
    font-size: 40px;
    line-height: 1.2;
    text-shadow: 0 4px 18px rgba(0, 0, 0, 0.45);
  }

  .brand-title {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .brand-logo-wrap {
    display: flex;
    flex: 0 0 auto;
  }

  .brand-logo {
    width: clamp(180px, 21vw, 280px);
    height: auto;
    max-width: 100%;
    object-fit: contain;
    display: block;
    filter: drop-shadow(0 10px 24px rgba(0, 0, 0, 0.28));
  }

  .brand-text {
    display: none;
  }

  p {
    max-width: 500px;
    color: rgba(244, 249, 255, 0.92);
    margin-bottom: 18px;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.38);
  }

  ul {
    padding-left: 20px;
    color: rgba(244, 249, 255, 0.9);
    line-height: 1.9;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
  }
}

.login-card {
  position: relative;
  z-index: 1;
  padding: 28px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.login-tip {
  color: var(--text-sub);
  margin: 4px 0 16px;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 108px;
  gap: 10px;
}

.captcha-image {
  width: 108px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--line-soft);
  cursor: pointer;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 1080px) {
  .login-page {
    grid-template-columns: 1fr;
    gap: 18px;
    padding: 14px;
  }

  .login-art {
    h1 {
      font-size: 30px;
    }
  }
}
</style>
