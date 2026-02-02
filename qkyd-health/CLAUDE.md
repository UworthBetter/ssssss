# qkyd-health 模块

[根目录](../CLAUDE.md) > **qkyd-health**

---

## 变更记录 (Changelog)

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 初始化模块文档 |

---

## 模块职责

qkyd-health 是健康数据管理模块，负责：

1. **设备管理**：智能穿戴设备的绑定、状态监控
2. **健康数据采集**：心率、血氧、体温、血压、步数、位置等
3. **数据服务**：为前端和 AI 模块提供健康数据查询接口
4. **AI 服务集成**：调用 AI 服务进行健康数据分析
5. **电子围栏**：位置监控和围栏告警

---

## 入口与启动

### 配置类
```java
// 文件: src/main/java/com/qkyd/health/config/AiServiceProperties.java
@ConfigurationProperties(prefix = "ueit.ai")
public class AiServiceProperties {
    // AI 服务配置属性
}

// 文件: src/main/java/com/qkyd/health/config/AsyncConfig.java
@Configuration
public class AsyncConfig {
    // 异步任务配置
}

// 文件: src/main/java/com/qkyd/health/config/RestTemplateConfig.java
@Configuration
public class RestTemplateConfig {
    // HTTP 客户端配置
}
```

---

## 对外接口

### REST API

#### 健康数据控制器
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/health/blood` | `UeitBloodController` | 血压数据管理 |
| `/health/heartRate` | `UeitHeartRateController` | 心率数据管理 |
| `/health/spo2` | `UeitSpo2Controller` | 血氧数据管理 |
| `/health/temp` | `UeitTempController` | 体温数据管理 |
| `/health/steps` | `UeitStepsController` | 步数数据管理 |
| `/health/location` | `UeitLocationController` | 位置数据管理 |
| `/health/exception` | `UeitExceptionController` | 异常数据管理 |
| `/health/deviceInfo` | `UeitDeviceInfoController` | 设备信息管理 |
| `/health/deviceInfoExt` | `UeitDeviceInfoExtendController` | 设备扩展信息 |
| `/health/fence` | `UeitFenceController` | 电子围栏管理 |
| `/health/watch` | `WatchController` | 手表数据接口 |

#### 数据服务控制器
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/data/index` | `IndexController` | 首页数据统计 |
| `/data/data` | `DataController` | 数据查询接口 |

#### AI 模拟控制器
| 路径 | 控制器 | 功能 |
|------|--------|------|
| `/ai/mock` | `HealthMockController` | AI 健康检查模拟 |

---

## 关键依赖与配置

### Maven 依赖
```xml
<!-- 依赖的通用工具 -->
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-common</artifactId>
</dependency>
```

---

## 数据模型

### 实体类 (domain)
| 类名 | 表名 | 功能 |
|------|------|------|
| `UeitBlood` | `qkyd_blood` | 血压数据 |
| `UeitHeartRate` | `qkyd_heart_rate` | 心率数据 |
| `UeitSpo2` | `qkyd_spo2` | 血氧数据 |
| `UeitTemp` | `qkyd_temp` | 体温数据 |
| `UeitSteps` | `qkyd_steps` | 步数数据 |
| `UeitLocation` | `qkyd_location` | 位置数据 |
| `UeitException` | `qkyd_exception` | 异常记录 |
| `UeitDeviceInfo` | `qkyd_device_info` | 设备信息 |
| `UeitDeviceInfoExtend` | `qkyd_device_info_extend` | 设备扩展信息 |
| `UeitFence` | `qkyd_fence` | 电子围栏 |
| `AiHealthRecord` | `qkyd_ai_health_record` | AI 健康记录 |

### DTO (数据传输对象)
| 类名 | 功能 |
|------|------|
| `RealTimeData` | 实时数据 |
| `RealTimeTracking` | 实时跟踪数据 |
| `UserDevice` | 用户设备信息 |
| `WatchDto` | 手表数据传输对象 |
| `WatchBNDto` | 手表 B&N 数据 |
| `ExceptionDataDto` | 异常数据 |
| `LatLng` | 经纬度 |
| `AgeSexGroupCountDto` | 年龄性别分组统计 |
| `AiHealthCheckRequest` | AI 健康检查请求 |
| `AiHealthCheckResponse` | AI 健康检查响应 |
| `VitalSignData` | 生命体征数据 |
| `HeartRateAnomaly` | 心率异常数据 |
| `MockUploadRequest` | 模拟上传请求 |

---

## 核心服务

### 健康数据服务
```java
// 血压服务
public interface IUeitBloodService extends IService<UeitBlood> {}

// 心率服务
public interface IUeitHeartRateService extends IService<UeitHeartRate> {}

// 血氧服务
public interface IUeitSpo2Service extends IService<UeitSpo2> {}

// 体温服务
public interface IUeitTempService extends IService<UeitTemp> {}

// 步数服务
public interface IUeitStepsService extends IService<UeitSteps> {}

// 位置服务
public interface IUeitLocationService extends IService<UeitLocation> {}

// 异常服务
public interface IUeitExceptionService extends IService<UeitException> {}

// 设备信息服务
public interface IUeitDeviceInfoService extends IService<UeitDeviceInfo> {}

// 电子围栏服务
public interface IUeitFenceService extends IService<UeitFence> {}
```

### 手表服务
```java
// 文件: src/main/java/com/qkyd/health/service/WatchService.java
public interface WatchService {
    // 手表数据相关业务
}

// 文件: src/main/java/com/qkyd/health/service/WatchBNService.java
public interface WatchBNService {
    // 手表 B&N 协议相关业务
}
```

### AI 服务客户端
```java
// 文件: src/main/java/com/qkyd/health/service/ai/AiServiceClient.java
@Service
public class AiServiceClient {
    // 调用 AI 服务进行健康分析
}
```

### 数据服务
```java
// 文件: src/main/java/com/qkyd/health/service/DataService.java
public interface DataService {
    // 数据查询和统计服务
}

// 文件: src/main/java/com/qkyd/health/service/IndexService.java
public interface IndexService {
    // 首页数据统计服务
}
```

### 健康数据服务
```java
// 文件: src/main/java/com/qkyd/health/service/ai/HealthDataService.java
public interface HealthDataService {
    // 健康数据处理服务
}
```

---

## 数据访问层

### Mapper 接口
| Mapper | 功能 |
|--------|------|
| `UeitBloodMapper` | 血压数据访问 |
| `UeitHeartRateMapper` | 心率数据访问 |
| `UeitSpo2Mapper` | 血氧数据访问 |
| `UeitTempMapper` | 体温数据访问 |
| `UeitStepsMapper` | 步数数据访问 |
| `UeitLocationMapper` | 位置数据访问 |
| `UeitExceptionMapper` | 异常数据访问 |
| `UeitDeviceInfoMapper` | 设备信息访问 |
| `UeitDeviceInfoExtendMapper` | 设备扩展信息访问 |
| `UeitFenceMapper` | 电子围栏数据访问 |
| `AiHealthRecordMapper` | AI 健康记录访问 |

---

## 工具类

### 签名工具
```java
// 文件: src/main/java/com/qkyd/health/utils/ParamsSignUtils.java
public class ParamsSignUtils {
    // 参数签名工具类
}
```

---

## 测试与质量

### 单元测试
- 测试目录：`src/test/java/com/qkyd/health/`
- 运行命令：`mvn test -Dtest=*ServiceTest`

### API 测试
- Swagger UI: http://localhost:8098/doc.html
- 测试端点：`/health/*`, `/data/*`

---

## 常见问题 (FAQ)

### Q1: 如何添加新的健康指标类型？
1. 创建对应的实体类（继承基础字段）
2. 创建 Mapper、Service、Controller
3. 添加 MyBatis XML 映射文件
4. 配置前端路由和 API

### Q2: 如何处理设备数据上报？
- 通过 `WatchController` 或 `WatchBNController` 接收设备数据
- 数据验证后存入对应健康指标表
- 触发 AI 分析（如需要）

### Q3: 电子围栏如何工作？
1. 用户设置围栏（中心点、半径）
2. 定期检查设备位置是否超出围栏
3. 超出时生成异常记录并推送告警

---

## 相关文件清单

```
qkyd-health/
├── src/main/java/com/qkyd/health/
│   ├── config/
│   │   ├── AiServiceProperties.java
│   │   ├── AsyncConfig.java
│   │   └── RestTemplateConfig.java
│   ├── controller/
│   │   ├── UeitBloodController.java
│   │   ├── UeitHeartRateController.java
│   │   ├── UeitSpo2Controller.java
│   │   ├── UeitTempController.java
│   │   ├── UeitStepsController.java
│   │   ├── UeitLocationController.java
│   │   ├── UeitExceptionController.java
│   │   ├── UeitDeviceInfoController.java
│   │   ├── UeitDeviceInfoExtendController.java
│   │   ├── UeitFenceController.java
│   │   ├── WatchController.java
│   │   ├── data/
│   │   │   ├── DataController.java
│   │   │   └── IndexController.java
│   │   └── ai/
│   │       └── HealthMockController.java
│   ├── service/
│   │   ├── IUeitBloodService.java
│   │   ├── IUeitHeartRateService.java
│   │   ├── IUeitSpo2Service.java
│   │   ├── IUeitTempService.java
│   │   ├── IUeitStepsService.java
│   │   ├── IUeitLocationService.java
│   │   ├── IUeitExceptionService.java
│   │   ├── IUeitDeviceInfoService.java
│   │   ├── IUeitDeviceInfoExtendService.java
│   │   ├── IUeitFenceService.java
│   │   ├── WatchService.java
│   │   ├── WatchBNService.java
│   │   ├── DataService.java
│   │   ├── IndexService.java
│   │   ├── ai/
│   │   │   ├── AiServiceClient.java
│   │   │   └── HealthDataService.java
│   │   └── impl/
│   │       ├── UeitBloodServiceImpl.java
│   │       ├── UeitHeartRateServiceImpl.java
│   │       ├── UeitSpo2ServiceImpl.java
│   │       ├── UeitTempServiceImpl.java
│   │       ├── UeitStepsServiceImpl.java
│   │       ├── UeitLocationServiceImpl.java
│   │       ├── UeitExceptionServiceImpl.java
│   │       ├── UeitDeviceInfoServiceImpl.java
│   │       ├── UeitDeviceInfoExtendServiceImpl.java
│   │       ├── UeitFenceServiceImpl.java
│   │       ├── WatchServiceImpl.java
│   │       ├── WatchBNServiceImpl.java
│   │       ├── DataServiceImpl.java
│   │       └── IndexServiceImpl.java
│   ├── mapper/
│   │   ├── UeitBloodMapper.java
│   │   ├── UeitHeartRateMapper.java
│   │   ├── UeitSpo2Mapper.java
│   │   ├── UeitTempMapper.java
│   │   ├── UeitStepsMapper.java
│   │   ├── UeitLocationMapper.java
│   │   ├── UeitExceptionMapper.java
│   │   ├── UeitDeviceInfoMapper.java
│   │   ├── UeitDeviceInfoExtendMapper.java
│   │   ├── UeitFenceMapper.java
│   │   └── AiHealthRecordMapper.java
│   ├── domain/
│   │   ├── UeitBlood.java
│   │   ├── UeitHeartRate.java
│   │   ├── UeitSpo2.java
│   │   ├── UeitTemp.java
│   │   ├── UeitSteps.java
│   │   ├── UeitLocation.java
│   │   ├── UeitException.java
│   │   ├── UeitDeviceInfo.java
│   │   ├── UeitDeviceInfoExtend.java
│   │   ├── UeitFence.java
│   │   ├── AiHealthRecord.java
│   │   └── dto/
│   ├── utils/
│   │   └── ParamsSignUtils.java
│   └── resources/mapper/health/
│       ├── UeitBloodMapper.xml
│       ├── UeitHeartRateMapper.xml
│       ├── UeitSpo2Mapper.xml
│       ├── UeitTempMapper.xml
│       ├── UeitStepsMapper.xml
│       ├── UeitLocationMapper.xml
│       ├── UeitExceptionMapper.xml
│       ├── UeitDeviceInfoMapper.xml
│       ├── UeitDeviceInfoExtendMapper.xml
│       ├── UeitFenceMapper.xml
│       └── AiHealthRecordMapper.xml
└── pom.xml
```

---

**最后更新**: 2026-02-01 14:18:39
