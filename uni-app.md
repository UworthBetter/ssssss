
## 🎯 小程序开发规划

### 整体架构设计

```
┌─────────────────────────────────────────────────────┐
│                    用户端架构                        │
└─────────────────────────────────────────────────────┘

┌──────────────────┐    ┌──────────────────┐
│   微信小程序     │    │   H5移动端       │
│  (uni-app开发)   │    │  (Vue3开发)      │
└────────┬─────────┘    └────────┬─────────┘
         │                        │
         └──────────┬─────────────┘
                    ↓
┌──────────────────────────────────────┐
│      后端API层 (新增模块)             │
│  - ueit-app (小程序专用模块)         │
│  - ueit-miniprogram (小程序API)      │
└──────────────┬───────────────────────┘
               ↓
┌──────────────────────────────────────┐
│       现有后端服务层                 │
│  - ueit-health (健康数据)           │
│  - ueit-system (用户权限)           │
└──────────────┬───────────────────────┘
               ↓
┌──────────────────────────────────────┐
│        数据存储层 (MySQL)            │
│  - 现有表 + 新增用户端相关表         │
└──────────────────────────────────────┘
```

### 分阶段开发计划（建议4-6周）

#### 📅 第一阶段：基础架构与用户体系（第1-2周）

**目标：搭建小程序基础框架和用户认证体系**

**任务清单：**

```
Week 1 后端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 创建ueit-app模块
  └─ 搭建Spring Boot启动模块
  └─ 配置微信小程序SDK

□ 用户表设计
  └─ ueit_user（终端用户表）
  └─ ueit_family（家属关系表）
  └─ ueit_user_device（用户设备关系）

□ 微信小程序登录
  └─ /api/app/login (code2session)
  └─ /api/app/getPhoneNumber
  └─ JWT Token生成

□ 设备管理API
  └─ 绑定设备
  └─ 解绑设备
  └─ 设备状态查询
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```
Week 2 前端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 小程序基础框架
  └─ uni-app项目搭建
  └─ 配置微信小程序
  └─ 网络请求封装
  └─ Token管理

□ 用户模块页面
  └─ 登录页
  └─ 我的页面
  └─ 个人信息设置
  └─ 设备管理页
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**数据库设计：**

```sql
-- 终端用户表
CREATE TABLE ueit_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wechat_openid VARCHAR(100) UNIQUE NOT NULL,
    wechat_unionid VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    phone VARCHAR(20),
    gender CHAR(1),
    birthday DATE,
    address VARCHAR(200),
    emergency_contact VARCHAR(50),
    emergency_phone VARCHAR(20),
    health_score INT DEFAULT 80,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (wechat_openid)
) COMMENT='终端用户表';

-- 家属关系表
CREATE TABLE ueit_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_user_id BIGINT NOT NULL COMMENT '家属用户ID',
    relation_type VARCHAR(20) COMMENT '关系类型:parent,child,spouse,other',
    relation_desc VARCHAR(50) COMMENT '关系描述',
    is_primary TINYINT DEFAULT 0 COMMENT '是否主要联系人',
    can_alarm TINYINT DEFAULT 1 COMMENT '是否接收告警',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_family (family_user_id)
) COMMENT='家属关系表';

-- 用户设备关系表（已有ueit_device_info，需扩展）
CREATE TABLE ueit_user_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    device_name VARCHAR(100),
    is_primary TINYINT DEFAULT 0 COMMENT '是否主设备',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    unbind_time DATETIME,
    status CHAR(1) DEFAULT '1' COMMENT '1使用中 0已解绑',
    INDEX idx_user (user_id),
    INDEX idx_device (device_id)
) COMMENT='用户设备关系表';
```

---

#### 📅 第二阶段：健康数据展示（第3周）

**目标：实现健康数据的可视化展示**

**任务清单：**

```
Week 3 后端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 数据查询API（复用DataServiceImpl）
  └─ /api/app/health/blood (血压)
  └─ /api/app/health/heartRate (心率)
  └─ /api/app/health/spo2 (血氧)
  └─ /api/app/health/temperature (体温)
  └─ /api/app/health/steps (步数)

□ 数据统计API
  └─ 今日/本周/本月数据汇总
  └─ 数据趋势分析
  └─ 异常数据统计
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```
Week 3 前端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 健康数据展示页面
  └─ 健康首页（数据概览）
  └─ 血压详情页（带图表）
  └─ 心率详情页（带图表）
  └─ 血氧详情页（带图表）
  └─ 体温详情页（带图表）
  └─ 运动数据页（步数）
  
□ 图表组件
  └─ 折线图（趋势）
  └─ 柱状图（对比）
  └─ 仪表盘（指标）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**页面结构示例：**

```
健康首页
├─ 头部：用户信息 + 健康评分
├─ 实时数据卡片（最新一次）
│  ├─ 血压：120/80 mmHg
│  ├─ 心率：75 bpm
│  ├─ 血氧：98%
│  ├─ 体温：36.5℃
│  └─ 步数：5000步
├─ 数据趋势图表
│  ├─ 血压7天趋势
│  └─ 心率24小时分布
└─ 异常告警列表
   └─ 最近异常记录
```

---

#### 📅 第三阶段：定位与安全功能（第4周）

**目标：实现定位追踪、电子围栏、告警通知**

**任务清单：**

```
Week 4 后端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 定位相关API
  └─ /api/app/location/realtime (实时定位)
  └─ /api/app/location/history (历史轨迹)
  └─ /api/app/location/path (轨迹回放)
  └─ /api/app/fence/list (电子围栏列表)
  └─ /api/app/fence/add (添加围栏)
  
□ 告警通知API
  └─ /api/app/alarm/list (告警列表)
  └─ /api/app/alarm/detail (告警详情)
  └─ 微信模板消息推送
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```
Week 4 前端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 定位功能页面
  └─ 实时定位页（地图展示）
  └─ 历史轨迹页（轨迹回放）
  └─ 电子围栏管理页
  
□ 告警功能页面
  └─ 告警列表页
  └─ 告警详情页
  └─ 告警设置页
  
□ 地图集成
  └─ 高德地图SDK
  └─ 腾讯地图SDK
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**微信模板消息配置：**

```json
{
  "template_id": "告警通知模板",
  "data": {
    "first": {
      "value": "您的家人发生告警",
      "color": "#173177"
    },
    "keyword1": {
      "value": "跌倒告警",
      "color": "#FF0000"
    },
    "keyword2": {
      "value": "2024-01-21 15:30",
      "color": "#173177"
    },
    "keyword3": {
      "value": "人民公园",
      "color": "#173177"
    },
    "remark": {
      "value": "请及时关注",
      "color": "#173177"
    }
  }
}
```

---

#### 📅 第四阶段：家属端与高级功能（第5周）

**目标：实现家属端功能和AI健康分析**

**任务清单：**

```
Week 5 后端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 家属端API
  └─ /api/app/family/list (家属列表)
  └─ /api/app/family/add (添加家属)
  └─ /api/app/family/remove (移除家属)
  └─ /api/app/family/health (家属健康数据)
  
□ AI分析API（集成三大算法）
  └─ /api/app/ai/healthReport (健康报告)
  └─ /api/app/ai/riskPrediction (风险预测)
  └─ /api/app/ai/advice (健康建议)
  
□ 报告生成API
  └─ /api/app/report/generate (生成报告)
  └─ /api/app/report/list (报告列表)
  └─ /api/app/report/detail (报告详情)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```
Week 5 前端开发
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 家属端页面
  └─ 家属列表页
  └─ 添加家属页
  └─ 家属健康数据页
  
□ AI分析页面
  └─ 健康报告页
  └─ 风险评估页
  └─ 健康建议页
  
□ 报告管理页面
  └─ 报告列表页
  └─ 报告详情页（PDF预览）
  └─ 报告分享页
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

#### 📅 第五阶段：测试优化与部署（第6周）

**目标：测试、优化、部署上线**

**任务清单：**

```
Week 6 测试与优化
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 功能测试
  └─ 用户流程测试
  └─ 数据准确性测试
  └─ 接口性能测试
  
□ 兼容性测试
  └─ iOS/Android测试
  └─ 不同尺寸屏幕测试
  
□ 性能优化
  └─ 接口响应优化
  └─ 页面加载优化
  └─ 数据缓存优化
  
□ 安全加固
  └─ 接口鉴权增强
  └─ 数据加密
  └─ 防刷机制
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

```
Week 6 部署与上线
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
□ 后端部署
  └─ 服务器配置
  └─ 数据库部署
  └─ Nginx配置
  └─ HTTPS配置
  
□ 小程序上线
  └─ 微信小程序提交审核
  └─ 小程序发布
  └─ 运营准备
  
□ 文档整理
  └─ API文档
  └─ 用户手册
  └─ 运维文档
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 💡 技术选型建议

### 后端技术栈

| 技术 | 版本 | 用途 | 说明 |
|------|------|------|------|
| **Spring Boot** | 2.5.15 | 基础框架 | 复用现有技术栈 |
| **MyBatis** | 3.x | 数据持久化 | 复用现有技术 |
| **JWT** | 0.9.1 | Token认证 | 小程序登录 |
| **WxJava** | 4.6.0 | 微信SDK | 小程序API |
| **Redis** | 6.x | 缓存 | Token、数据缓存 |
| **WebSocket** | - | 实时推送 | 实时告警 |

### 前端技术栈

| 技术 | 版本 | 用途 | 说明 |
|------|------|------|------|
| **uni-app** | 3.x | 跨平台框架 | 一次开发，多端运行 |
| **Vue3** | 3.x | 前端框架 | 现代化开发 |
| **TypeScript** | 5.x | 类型检查 | 提高代码质量 |
| **uCharts** | 2.x | 图表库 | 数据可视化 |
| **地图SDK** | - | 地图功能 | 高德/腾讯地图 |
| **ECharts** | 5.x | 备选图表 | 复杂图表 |

---

## 📊 核心功能模块详细设计

### 1. 用户认证模块

```typescript
// 登录流程
微信小程序端              后端服务
    │                      │
    ├─wx.login()           │
    │获取code              │
    ├───────────────────>  │
    │                      ├─调用微信API
    │                      │(code2session)
    │                      │
    │                      ├─获取openid/session_key
    │                      │
    │                      ├─查询/创建用户
    │                      │
    │                      ├─生成JWT Token
    │                      │
    │<─────────────────────┤
    │返回Token+用户信息     │
    │                      │
    ├─存储Token            │
    │后续请求带Token        │
```

**API接口：**

```java
@RestController
@RequestMapping("/api/app")
public class AppAuthController {
    
    @PostMapping("/login")
    public AjaxResult login(@RequestBody WxLoginDto dto) {
        // 1. code换取openid
        String openid = wxService.getOpenid(dto.getCode());
        
        // 2. 查询或创建用户
        UeitUser user = userService.getByOpenid(openid);
        if (user == null) {
            user = userService.create(openid);
        }
        
        // 3. 生成Token
        String token = JwtUtil.createToken(user.getUserId());
        
        return AjaxResult.success(Map.of(
            "token", token,
            "userInfo", user
        ));
    }
    
    @PostMapping("/getPhoneNumber")
    public AjaxResult getPhoneNumber(@RequestBody PhoneDto dto) {
        // 解密手机号
        String phone = wxService.decryptPhone(
            dto.getCode(), 
            dto.getEncryptedData(), 
            dto.getIv()
        );
        
        // 绑定手机号
        userService.bindPhone(userId, phone);
        
        return AjaxResult.success();
    }
}
```

### 2. 健康数据展示模块

```typescript
// 页面结构示例
<template>
  <view class="health-home">
    <!-- 顶部用户信息 -->
    <view class="user-header">
      <image :src="userInfo.avatar" mode="aspectFill" />
      <view class="user-info">
        <text class="nickname">{{ userInfo.nickname }}</text>
        <text class="health-score">健康评分：{{ userInfo.healthScore }}</text>
      </view>
    </view>
    
    <!-- 实时数据卡片 -->
    <view class="realtime-data">
      <view class="data-card" @tap="goDetail('blood')">
        <text class="label">血压</text>
        <text class="value">{{ latestData.blood }}</text>
        <text class="unit">mmHg</text>
      </view>
      <view class="data-card" @tap="goDetail('heartRate')">
        <text class="label">心率</text>
        <text class="value">{{ latestData.heartRate }}</text>
        <text class="unit">bpm</text>
      </view>
      <!-- 更多卡片... -->
    </view>
    
    <!-- 趋势图表 -->
    <view class="trend-chart">
      <qiun-ucharts 
        type="line" 
        :opts="chartOpts" 
        :chartData="chartData" 
      />
    </view>
    
    <!-- 异常告警 -->
    <view class="alarm-list">
      <view class="alarm-item" v-for="alarm in alarms">
        <text class="alarm-type">{{ alarm.type }}</text>
        <text class="alarm-time">{{ alarm.time }}</text>
      </view>
    </view>
  </view>
</template>
```

### 3. 定位与安全模块

**实时定位：**

```typescript
// 实时定位页面
<template>
  <view class="location-page">
    <map 
      :longitude="location.longitude"
      :latitude="location.latitude"
      :markers="markers"
      :polyline="polyline"
      style="width:100%;height:500rpx"
    >
      <marker 
        :longitude="location.longitude"
        :latitude="location.latitude"
        iconPath="/static/marker.png"
      />
    </map>
    
    <view class="location-info">
      <text>定位时间：{{ location.updateTime }}</text>
      <text>定位方式：{{ location.typeName }}</text>
      <text>位置：{{ location.address }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      location: {},
      timer: null
    }
  },
  onLoad() {
    this.startRealtimeLocation();
  },
  methods: {
    async startRealtimeLocation() {
      // 每30秒更新一次
      this.timer = setInterval(async () => {
        const res = await this.$api.get('/api/app/location/realtime', {
          userId: this.userId
        });
        this.location = res.data;
      }, 30000);
    }
  }
}
</script>
```

### 4. AI健康分析模块

**健康报告生成：**

```java
@Service
public class AiHealthService {
    
    @Autowired
    private DataService dataService;
    
    /**
     * 生成健康报告
     */
    public HealthReport generateReport(Long userId) {
        HealthReport report = new HealthReport();
        
        // 1. 获取健康数据
        Date now = new Date();
        Date startMonth = DateUtil.addMonths(now, -1);
        
        List bloodData = dataService.getDataBoard(
            userId, "blood", startMonth, now
        );
        List heartRateData = dataService.getDataBoard(
            userId, "heartRate", startMonth, now
        );
        // ... 其他数据
        
        // 2. 岐黄预处理（数据质量评估）
        PreprocessResult preprocess = qihuangAlgorithm.preprocess(
            bloodData, heartRateData
        );
        
        // 3. 扁鹊模型（风险预测）
        RiskPrediction risk = bianqueAlgorithm.predict(
            userId, bloodData, heartRateData
        );
        
        // 4. 嘉庆算法（跌倒检测分析）
        FallAnalysis fallAnalysis = jiaqingAlgorithm.analyze(userId);
        
        // 5. 生成报告
        report.setUserId(userId);
        report.setPreprocessResult(preprocess);
        report.setRiskPrediction(risk);
        report.setFallAnalysis(fallAnalysis);
        report.setHealthScore(calculateHealthScore(risk));
        report.setAdvice(generateAdvice(risk));
        report.setCreateTime(now);
        
        return report;
    }
}
```

---

## 🎨 小程序页面结构

```
pages/
├── index/                  # 首页（健康数据概览）
│   ├── index.vue
│   └── components/
│       ├── HealthCard.vue      # 健康数据卡片
│       └── TrendChart.vue      # 趋势图表
│
├── health/                 # 健康数据详情
│   ├── blood.vue           # 血压详情
│   ├── heart-rate.vue      # 心率详情
│   ├── spo2.vue            # 血氧详情
│   ├── temperature.vue     # 体温详情
│   └── steps.vue           # 步数详情
│
├── location/               # 定位功能
│   ├── realtime.vue        # 实时定位
│   ├── history.vue         # 历史轨迹
│   └── fence.vue           # 电子围栏
│
├── alarm/                  # 告警功能
│   ├── list.vue            # 告警列表
│   ├── detail.vue          # 告警详情
│   └── setting.vue         # 告警设置
│
├── family/                 # 家属功能
│   ├── list.vue            # 家属列表
│   ├── add.vue             # 添加家属
│   └── health.vue          # 家属健康
│
├── report/                 # 健康报告
│   ├── list.vue            # 报告列表
│   ├── detail.vue          # 报告详情
│   └── generate.vue        # 生成报告
│
├── device/                 # 设备管理
│   ├── list.vue            # 设备列表
│   ├── bind.vue            # 绑定设备
│   └── unbind.vue          # 解绑设备
│
├── user/                   # 用户中心
│   ├── profile.vue         # 个人信息
│   ├── settings.vue        # 设置
│   └── about.vue           # 关于
│
└── login/                  # 登录
    ├── index.vue           # 登录页
    └── phone.vue           # 手机号绑定
```

---

## 📝 总结

### 项目现状
✅ **技术栈成熟**：RuoYi + Spring Boot + Vue3  
✅ **数据采集完整**：7类健康数据支持  
✅ **设备管理完善**：多种手表型号支持  
⚠️ **缺少用户端**：只有管理后台  
⚠️ **认证单一**：没有终端用户体系  

### 开发优势
- 复用现有后端服务，开发周期短
- 健康数据API已完备，前端调用简单
- 技术栈统一，团队学习成本低

### 核心功能
1. 用户认证（微信登录）
2. 健康数据展示（图表化）
3. 定位追踪（实时+历史）
4. 告警通知（模板消息）
5. 家属关联（关系管理）
6. AI健康分析（三大算法）
7. 健康报告（自动生成）

### 建议工期
**6周**可完成核心功能并上线

你对这个规划有什么想法或需要调整的地方吗？我可以针对某个模块进一步细化设计。