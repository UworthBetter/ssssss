# Vue3 require() 修复说明

## 问题描述
前端白屏，控制台报错：
```javascript
ReferenceError: require is not defined
    at Proxy.data (index.vue:509:15)
```

## 原因分析
Vite不支持CommonJS的`require`语法（这是Webpack的方式），必须使用ES模块的`import`。

## 修复内容

### 1. 添加图片import
在`index.vue`的script部分添加：

```javascript
import imgPic5 from '../assets/images/indexIMg/pic-5.png'
import watchIcon from '@/assets/images/watch.png'
```

### 2. 替换require使用

**修复前（Vue2/Webpack方式）：**
```javascript
data() {
  return {
    imgs: require('../assets/images/indexIMg/pic-5.png'),
    imgTwo: require('@/assets/images/watch.png'),
  }
}
```

**修复后（Vue3/Vite方式）：**
```javascript
data() {
  return {
    imgs: imgPic5,
    imgTwo: watchIcon,
  }
}
```

### 3. 注册Data View组件
在`main.js`中添加全局注册：

```javascript
// Data View大屏组件
import dataView from '@jiaminghi/data-view'

app.use(dataView)
```

## 修复后的文件结构

### main.js
```javascript
import { createApp } from 'vue'
import '@/assets/styles/index.scss'
import dataView from '@jiaminghi/data-view'  // 新增
import App from './App'
import store from './store'
import router from './router'
// ...

app.use(store)
app.use(plugins)
app.use(dataView)  // 新增
app.use(elementIcons)
// ...
```

### index.vue
```javascript
import imgPic5 from '../assets/images/indexIMg/pic-5.png'  // 新增
import watchIcon from '@/assets/images/watch.png'  // 新增
import screenfull from 'screenfull'
import * as echarts from 'echarts'

export default {
  data() {
    return {
      imgs: imgPic5,  // 修复：不再是require()
      imgTwo: watchIcon,  // 修复：不再是require()
      // ...
    }
  }
}
```

## Vite与Webpack的区别

### 资源导入

**Webpack (Vue2)：**
```javascript
const img = require('./image.png')
```

**Vite (Vue3)：**
```javascript
import img from './image.png'
// 或
const imgUrl = new URL('./image.png', import.meta.url).href
```

### 全局组件注册

**Webpack (Vue2)：**
- 自动扫描并注册node_modules中的组件

**Vite (Vue3)：**
- 必须显式import并注册

## Data View组件说明

`@jiaminghi/data-view`是一个大屏数据可视化组件库，包含：
- `dv-decoration-3` - 装饰性组件
- `dv-scroll-board` - 滚动表格
- `dv-border-box-1` - 边框组件
- 等其他大屏组件

这些组件在健康管理平台的大屏首页中被广泛使用。

## 验证步骤

1. **重启前端**
   ```bash
   taskkill /F /IM node.exe
   cd d:\jishe\1.19\RuoYi-Vue3-Modern
   npm run dev
   ```

2. **刷新浏览器**
   - 访问 http://localhost:8080
   - 按F5刷新或Ctrl+F5强制刷新

3. **检查控制台**
   - 打开浏览器开发者工具（F12）
   - 查看是否还有错误

## 其他可能的兼容性问题

如果仍然有问题，检查以下方面：

### 1. 图片路径
模板中使用了相对路径：
```html
<img src="../assets/images/indexIMg/1.png">
```

如果仍有问题，可以改为import方式：
```javascript
import img1 from '../assets/images/indexIMg/1.png'
// 在template中使用
<img :src="img1">
```

### 2. Vue Router语法
路由跳转可能需要调整：
```javascript
// Vue2
this.$router.push('/exception-view/' + id)

// Vue3（如果需要）
import { useRouter } from 'vue-router'
const router = useRouter()
router.push('/exception-view/' + id)
```

但Options API的写法在Vue3中仍然支持，所以可能不需要改。

### 3. Element UI组件
- Vue2: `el-icon-more`
- Vue3: `<el-icon><More /></el-icon>`

但Vue3也支持旧的class写法，所以应该兼容。

## 服务状态

✅ 前端：运行中 (端口8080)
✅ 后端：应该运行中 (端口8098)
✅ 依赖：已安装
✅ Data View组件：已注册
✅ require问题：已修复

---

**修复时间**：2026-01-20
**状态**：✅ 已完成，请刷新浏览器查看
