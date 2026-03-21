<template>
  <div class="login-page">
    <div class="login-art">
      <h1>智能健康管理平台</h1>
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
  display: grid;
  grid-template-columns: 1.1fr 460px;
  gap: 28px;
  align-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 10% 20%, rgba(13, 148, 136, 0.22), transparent 34%),
    radial-gradient(circle at 80% 85%, rgba(59, 130, 246, 0.22), transparent 36%),
    #ecf4f7;
}

.login-art {
  color: #104156;
  padding: 0 20px;

  h1 {
    margin-bottom: 12px;
    font-size: 40px;
    line-height: 1.2;
  }

  p {
    max-width: 520px;
    color: #35627a;
    margin-bottom: 18px;
  }

  ul {
    padding-left: 20px;
    color: #35627a;
    line-height: 1.9;
  }
}

.login-card {
  padding: 28px;
  border-radius: 20px;
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
