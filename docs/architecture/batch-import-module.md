# 批量数据导入模块架构设计文档

**版本**: 1.0
**作者**: Architecture Agent
**日期**: 2026-02-05
**状态**: 设计阶段

---

## 目录

- [1. 模块概述](#1-模块概述)
- [2. 需求分析](#2-需求分析)
- [3. 架构设计](#3-架构设计)
- [4. 分层架构](#4-分层架构)
- [5. API接口设计](#5-api接口设计)
- [6. 数据模型设计](#6-数据模型设计)
- [7. 类设计说明](#7-类设计说明)
- [8. 时序图](#8-时序图)
- [9. 技术实现要点](#9-技术实现要点)
- [10. 性能优化策略](#10-性能优化策略)

---

## 1. 模块概述

### 1.1 模块简介

批量数据导入模块是健康管理系统的核心功能之一，用于支持管理员通过Excel/CSV文件批量导入健康数据、服务对象信息和设备信息，提升数据录入效率。

### 1.2 模块目标

- 支持.xlsx、.xls、.csv三种文件格式的批量导入
- 实现严格的数据验证机制，确保数据质量
- 记录完整的导入历史，支持追溯和撤销
- 提供友好的错误提示和导入结果反馈
- 支持大文件导入，保证系统性能

### 1.3 技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| 后端框架 | Spring Boot | 2.7+ | 基础框架 |
| ORM框架 | MyBatis-Plus | 3.5+ | 数据持久化 |
| Excel解析 | Apache POI | 5.2.5 | Excel文件处理 |
| CSV解析 | Apache Commons CSV | 1.10.0 | CSV文件处理 |
| 前端框架 | Vue 3 | 3.x | UI框架 |
| UI组件库 | Element Plus | 2.x | 前端组件 |
| 文档工具 | Swagger | 3.x | API文档 |

---

## 2. 需求分析

### 2.1 功能需求

#### 2.1.1 文件解析
- 支持.xlsx格式（Excel 2007+）
- 支持.xls格式（Excel 97-2003）
- 支持.csv格式（UTF-8编码）
- 支持多种分隔符（逗号、分号、制表符）
- 自动识别表头和数据行
- 处理空值、日期、数字等数据类型

#### 2.1.2 数据验证
- 必填字段验证
- 数据格式验证（手机号、数值范围等）
- 数据类型验证
- 重复数据检测（基于主键）
- 外键关联验证（device_id、user_id存在性）

#### 2.1.3 数据导入
- 支持三种导入类型：
  - 健康数据（health_data）
  - 服务对象（patient）
  - 设备信息（device）
- 批量插入优化性能
- 事务控制（全部成功或全部失败）
- 支持重复数据处理（跳过/覆盖/终止）

#### 2.1.4 导入历史
- 记录每次导入的详细信息
- 查询导入历史列表（分页、筛选）
- 查看导入详情（成功行数、失败行数、错误信息）
- 删除导入记录（可选）

#### 2.1.5 模板下载
- 提供标准导入模板
- 包含字段说明和示例数据
- 支持Excel和CSV格式

### 2.2 非功能需求

| 需求项 | 指标 | 说明 |
|-------|------|------|
| 性能 | <30s/万条 | 1万条数据导入时间 |
| 性能 | <5min/10万条 | 10万条数据导入时间 |
| 内存 | <500MB | 导入过程内存占用 |
| 准确率 | 100% | 数据验证准确率 |
| 并发 | 支持 | 多用户同时导入 |
| 文件大小 | ≤10MB | 单文件大小限制 |

---

## 3. 架构设计

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          表现层 (Presentation)                    │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  批量导入页面    │  │  模板下载页面    │  │  导入历史页面    │  │
│  │  batch-import/  │  │  template/      │  │  history/       │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↕ HTTP/REST
┌─────────────────────────────────────────────────────────────────┐
│                         控制层 (Controller)                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              BatchImportController                         │ │
│  │  - uploadFile()       上传并导入文件                         │ │
│  │  - downloadTemplate()  下载导入模板                          │ │
│  │  - getImportHistory() 查询导入历史                          │ │
│  │  - getImportDetail()  查询导入详情                          │ │
│  │  - deleteRecord()     删除导入记录                          │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↕ Service调用
┌─────────────────────────────────────────────────────────────────┐
│                          业务层 (Service)                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐  ┌─────────────────────────────────┐  │
│  │ IExcelParseService   │  │ IImportRecordService            │  │
│  │ - parseExcel()       │  │ - getImportHistory()            │  │
│  │ - parseCSV()         │  │ - saveImportRecord()            │  │
│  │ - validateData()     │  │ - deleteImportRecord()         │  │
│  │ - importData()       │  │ - updateImportStatus()         │  │
│  └─────────────────────┘  └─────────────────────────────────┘  │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              ExcelParseServiceImpl                        │  │
│  │  核心业务逻辑：                                             │  │
│  │  1. 文件解析（Excel/CSV）                                  │  │
│  │  2. 数据验证（必填、格式、重复）                            │  │
│  │  3. 数据转换（DTO → Entity）                                │  │
│  │  4. 批量导入（事务控制）                                    │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↕ Mapper调用
┌─────────────────────────────────────────────────────────────────┐
│                         持久层 (Mapper)                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐  ┌─────────────────────────────────┐  │
│  │ ImportRecordMapper   │  │ HealthDataMapper              │  │
│  │ - selectList()       │  │ - insertBatch()               │  │
│  │ - selectById()       │  │ - selectByUserAndTime()       │  │
│  │ - insert()           │  │                               │  │
│  │ - deleteById()       │  │ PatientMapper                 │  │
│  │ - updateById()       │  │ - insertBatch()               │  │
│  └─────────────────────┘  │ - selectByPhone()              │  │
│                           │                               │  │
│                           │ DeviceMapper                  │  │
│                           │ - insertBatch()                │  │
│                           │ - selectByDeviceId()           │  │
│                           └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↕ SQL
┌─────────────────────────────────────────────────────────────────┐
│                         数据层 (Database)                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │health_import_   │  │  health_data    │  │    patient      │  │
│  │    record       │  │                 │  │                 │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
│                                                                   │
│  ┌─────────────────┐                                              │
│  │    device       │                                              │
│  └─────────────────┘                                              │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 模块依赖关系

```
batch-import-module
├── Controller层
│   └── BatchImportController
│       └── 依赖 → Service层
├── Service层
│   ├── IExcelParseService
│   │   └── 依赖 → Mapper层 (HealthData, Patient, Device)
│   └── IImportRecordService
│       └── 依赖 → Mapper层 (ImportRecord)
├── Mapper层
│   ├── ImportRecordMapper
│   ├── HealthDataMapper
│   ├── PatientMapper
│   └── DeviceMapper
├── Entity层
│   ├── ImportRecord
│   ├── HealthData
│   ├── Patient
│   └── Device
└── DTO/VO层
    ├── BatchImportDTO
    ├── ImportResultDTO
    ├── ImportHistoryVO
    └── ImportDetailVO
```

---

## 4. 分层架构

### 4.1 Controller层职责

**职责范围**：
- 接收HTTP请求（上传、查询、删除）
- 参数校验（文件类型、大小）
- 调用Service层处理业务逻辑
- 返回统一响应结果
- 异常处理

**核心类**：
- `BatchImportController`

### 4.2 Service层职责

**职责范围**：
- 文件解析（Excel/CSV）
- 数据验证（格式、重复、关联）
- 数据转换（DTO → Entity）
- 批量导入（事务控制）
- 导入历史管理

**核心类**：
- `IExcelParseService` (接口)
- `ExcelParseServiceImpl` (实现)
- `IImportRecordService` (接口)
- `ImportRecordServiceImpl` (实现)

### 4.3 Mapper层职责

**职责范围**：
- 数据库CRUD操作
- 批量插入
- 复杂查询（历史记录、重复检测）

**核心类**：
- `ImportRecordMapper`
- `HealthDataMapper` (已存在)
- `PatientMapper` (已存在)
- `DeviceMapper` (已存在)

---

## 5. API接口设计

### 5.1 接口清单

| 接口编号 | 方法 | 路径 | 功能描述 | 权限标识 |
|---------|------|------|---------|---------|
| API-01 | POST | /health/batch/import | 上传并导入文件 | health:batch:import |
| API-02 | GET | /health/batch/template/{importType} | 下载导入模板 | health:batch:download |
| API-03 | GET | /health/batch/history | 查询导入历史列表 | health:batch:list |
| API-04 | GET | /health/batch/history/{id} | 查询导入详情 | health:batch:query |
| API-05 | DELETE | /health/batch/history/{id} | 删除导入记录 | health:batch:remove |
| API-06 | GET | /health/batch/types | 获取导入类型列表 | health:batch:list |

### 5.2 接口详细说明

#### API-01: 上传并导入文件

**请求信息**：
```
POST /health/batch/import
Content-Type: multipart/form-data
```

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | File | 是 | 导入文件（.xlsx/.xls/.csv） |
| importType | String | 是 | 导入类型：health/patient/device |
| duplicateAction | String | 否 | 重复数据处理：skip/cover/abort（默认skip） |

**响应示例**：
```json
{
  "code": 200,
  "msg": "导入成功",
  "data": {
    "success": true,
    "message": "导入成功，共1000条，成功995条，失败5条",
    "totalRows": 1000,
    "successRows": 995,
    "failRows": 5,
    "importId": 123,
    "errors": [
      {
        "row": 10,
        "column": "user_id",
        "value": "",
        "message": "user_id不能为空"
      }
    ]
  }
}
```

---

#### API-02: 下载导入模板

**请求信息**：
```
GET /health/batch/template/{importType}
```

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| importType | String | 是 | 导入类型：health/patient/device |
| format | String | 否 | 模板格式：xlsx/csv（默认xlsx） |

**响应**：文件流（application/octet-stream）

---

#### API-03: 查询导入历史列表

**请求信息**：
```
GET /health/batch/history
```

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |
| importType | String | 否 | 导入类型筛选 |
| status | String | 否 | 状态筛选：success/failed/processing |
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd HH:mm:ss） |

**响应示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 123,
      "importType": "health",
      "fileName": "健康数据_20260205.xlsx",
      "totalRows": 1000,
      "successRows": 995,
      "failRows": 5,
      "status": "success",
      "errorMsg": null,
      "importTime": "2026-02-05 21:30:00",
      "userName": "张三"
    }
  ],
  "total": 25
}
```

---

#### API-04: 查询导入详情

**请求信息**：
```
GET /health/batch/history/{id}
```

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 导入记录ID |

**响应示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "id": 123,
    "importType": "health",
    "fileName": "健康数据_20260205.xlsx",
    "totalRows": 1000,
    "successRows": 995,
    "failRows": 5,
    "status": "success",
    "errorMsg": null,
    "importTime": "2026-02-05 21:30:00",
    "userName": "张三",
    "errors": [
      {
        "row": 10,
        "column": "user_id",
        "value": "",
        "message": "user_id不能为空"
      },
      {
        "row": 25,
        "column": "heart_rate",
        "value": "abc",
        "message": "heart_rate必须是数字"
      }
    ],
    "importDetails": [
      {
        "tableName": "health_data",
        "insertCount": 995
      }
    ]
  }
}
```

---

#### API-05: 删除导入记录

**请求信息**：
```
DELETE /health/batch/history/{id}
```

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 导入记录ID |

**响应示例**：
```json
{
  "code": 200,
  "msg": "删除成功"
}
```

---

#### API-06: 获取导入类型列表

**请求信息**：
```
GET /health/batch/types
```

**响应示例**：
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "code": "health",
      "name": "健康数据",
      "description": "导入健康测量数据（心率、血压、血氧等）",
      "templateColumns": [
        {"field": "user_id", "name": "用户ID", "required": true},
        {"field": "device_id", "name": "设备ID", "required": true},
        {"field": "heart_rate", "name": "心率", "required": false},
        {"field": "blood_pressure_systolic", "name": "收缩压", "required": false},
        {"field": "blood_pressure_diastolic", "name": "舒张压", "required": false},
        {"field": "blood_oxygen", "name": "血氧", "required": false},
        {"field": "body_temperature", "name": "体温", "required": false},
        {"field": "steps", "name": "步数", "required": false},
        {"field": "record_time", "name": "记录时间", "required": true}
      ]
    },
    {
      "code": "patient",
      "name": "服务对象",
      "description": "导入服务对象基本信息",
      "templateColumns": [
        {"field": "name", "name": "姓名", "required": true},
        {"field": "phone", "name": "手机号", "required": true},
        {"field": "age", "name": "年龄", "required": false},
        {"field": "gender", "name": "性别", "required": false},
        {"field": "address", "name": "地址", "required": false},
        {"field": "health_status", "name": "健康状况", "required": false}
      ]
    },
    {
      "code": "device",
      "name": "设备信息",
      "description": "导入设备基本信息",
      "templateColumns": [
        {"field": "device_id", "name": "设备ID", "required": true},
        {"field": "device_type", "name": "设备类型", "required": true},
        {"field": "device_name", "name": "设备名称", "required": false},
        {"field": "device_model", "name": "设备型号", "required": false},
        {"field": "manufacturer", "name": "制造商", "required": false}
      ]
    }
  ]
}
```

---

## 6. 数据模型设计

### 6.1 数据库表设计

#### health_import_record（导入记录表）

| 字段名 | 类型 | 长度 | 必填 | 说明 | 索引 |
|-------|------|------|------|------|------|
| id | BIGINT | - | 是 | 主键 | PK |
| user_id | BIGINT | - | 是 | 操作用户ID | IDX |
| import_type | VARCHAR | 20 | 是 | 导入类型：health/patient/device | IDX |
| file_name | VARCHAR | 200 | 是 | 原始文件名 | - |
| file_size | BIGINT | - | 否 | 文件大小（字节） | - |
| total_rows | INT | - | 是 | 总行数 | - |
| success_rows | INT | - | 是 | 成功行数 | - |
| fail_rows | INT | - | 是 | 失败行数 | - |
| status | VARCHAR | 20 | 是 | 状态：success/failed/processing | - |
| error_msg | TEXT | - | 否 | 错误信息（JSON） | - |
| duplicate_action | VARCHAR | 20 | 否 | 重复数据处理：skip/cover/abort | - |
| import_time | DATETIME | - | 是 | 导入时间 | IDX |
| create_time | DATETIME | - | 是 | 创建时间 | IDX |
| create_by | VARCHAR | 64 | 否 | 创建人 | - |
| update_time | DATETIME | - | 否 | 更新时间 | - |
| update_by | VARCHAR | 64 | 否 | 更新人 | - |
| remark | VARCHAR | 500 | 否 | 备注 | - |

**索引设计**：
```sql
CREATE INDEX idx_user_id ON health_import_record(user_id);
CREATE INDEX idx_import_type ON health_import_record(import_type);
CREATE INDEX idx_import_time ON health_import_record(import_time);
CREATE INDEX idx_create_time ON health_import_record(create_time);
```

---

### 6.2 DTO对象设计

#### BatchImportDTO（批量导入请求DTO）

```java
package com.qkyd.heal.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("批量导入请求DTO")
public class BatchImportDTO {

    @ApiModelProperty("导入类型：health/patient/device")
    private String importType;

    @ApiModelProperty("重复数据处理方式：skip/cover/abort")
    private String duplicateAction;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;
}
```

---

#### ImportResultDTO（导入结果DTO）

```java
package com.qkyd.heal.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("导入结果DTO")
public class ImportResultDTO {

    @ApiModelProperty("是否成功")
    private Boolean success;

    @ApiModelProperty("提示信息")
    private String message;

    @ApiModelProperty("总行数")
    private Integer totalRows;

    @ApiModelProperty("成功行数")
    private Integer successRows;

    @ApiModelProperty("失败行数")
    private Integer failRows;

    @ApiModelProperty("导入记录ID")
    private Long importId;

    @ApiModelProperty("错误详情列表")
    private List<ErrorDetail> errors;

    @Data
    public static class ErrorDetail {
        @ApiModelProperty("行号")
        private Integer row;

        @ApiModelProperty("列名")
        private String column;

        @ApiModelProperty("错误值")
        private String value;

        @ApiModelProperty("错误信息")
        private String message;
    }
}
```

---

#### ImportHistoryQueryDTO（导入历史查询DTO）

```java
package com.qkyd.heal.domain.dto;

import com.qkyd.common.core.page.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("导入历史查询DTO")
public class ImportHistoryQueryDTO extends PageQuery {

    @ApiModelProperty("导入类型：health/patient/device")
    private String importType;

    @ApiModelProperty("状态：success/failed/processing")
    private String status;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;
}
```

---

### 6.3 VO对象设计

#### ImportHistoryVO（导入历史VO）

```java
package com.qkyd.heal.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("导入历史VO")
public class ImportHistoryVO {

    @ApiModelProperty("导入记录ID")
    private Long id;

    @ApiModelProperty("导入类型：health/patient/device")
    private String importType;

    @ApiModelProperty("导入类型名称")
    private String importTypeName;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件大小（KB）")
    private String fileSizeKB;

    @ApiModelProperty("总行数")
    private Integer totalRows;

    @ApiModelProperty("成功行数")
    private Integer successRows;

    @ApiModelProperty("失败行数")
    private Integer failRows;

    @ApiModelProperty("成功率")
    private Double successRate;

    @ApiModelProperty("状态：success/failed/processing")
    private String status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("重复数据处理方式")
    private String duplicateAction;

    @ApiModelProperty("导入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    @ApiModelProperty("操作人ID")
    private Long userId;

    @ApiModelProperty("操作人姓名")
    private String userName;
}
```

---

#### ImportDetailVO（导入详情VO）

```java
package com.qkyd.heal.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("导入详情VO")
public class ImportDetailVO {

    @ApiModelProperty("导入记录ID")
    private Long id;

    @ApiModelProperty("导入类型：health/patient/device")
    private String importType;

    @ApiModelProperty("导入类型名称")
    private String importTypeName;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件大小（KB）")
    private String fileSizeKB;

    @ApiModelProperty("总行数")
    private Integer totalRows;

    @ApiModelProperty("成功行数")
    private Integer successRows;

    @ApiModelProperty("失败行数")
    private Integer failRows;

    @ApiModelProperty("状态：success/failed/processing")
    private String status;

    @ApiModelProperty("错误信息")
    private List<ImportResultDTO.ErrorDetail> errors;

    @ApiModelProperty("导入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    @ApiModelProperty("操作人姓名")
    private String userName;

    @ApiModelProperty("重复数据处理方式")
    private String duplicateAction;

    @ApiModelProperty("导入明细")
    private List<ImportDetailItemVO> importDetails;

    @Data
    public static class ImportDetailItemVO {
        @ApiModelProperty("表名")
        private String tableName;

        @ApiModelProperty("表名显示")
        private String tableNameDisplay;

        @ApiModelProperty("插入数量")
        private Integer insertCount;
    }
}
```

---

#### ImportTypeVO（导入类型VO）

```java
package com.qkyd.heal.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("导入类型VO")
public class ImportTypeVO {

    @ApiModelProperty("类型代码")
    private String code;

    @ApiModelProperty("类型名称")
    private String name;

    @ApiModelProperty("类型描述")
    private String description;

    @ApiModelProperty("模板列定义")
    private List<TemplateColumnVO> templateColumns;

    @Data
    public static class TemplateColumnVO {
        @ApiModelProperty("字段名")
        private String field;

        @ApiModelProperty("字段中文名")
        private String name;

        @ApiModelProperty("是否必填")
        private Boolean required;

        @ApiModelProperty("字段类型")
        private String type;

        @ApiModelProperty("字段说明")
        private String description;

        @ApiModelProperty("示例值")
        private String example;
    }
}
```

---

### 6.4 Entity对象设计

#### ImportRecord（导入记录实体）

```java
package com.qkyd.heal.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.qkyd.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_import_record")
@ApiModel("导入记录实体")
public class ImportRecord extends BaseEntity {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("操作用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("导入类型：health/patient/device")
    @TableField("import_type")
    private String importType;

    @ApiModelProperty("原始文件名")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty("文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    @ApiModelProperty("总行数")
    @TableField("total_rows")
    private Integer totalRows;

    @ApiModelProperty("成功行数")
    @TableField("success_rows")
    private Integer successRows;

    @ApiModelProperty("失败行数")
    @TableField("fail_rows")
    private Integer failRows;

    @ApiModelProperty("状态：success/failed/processing")
    @TableField("status")
    private String status;

    @ApiModelProperty("错误信息（JSON）")
    @TableField("error_msg")
    private String errorMsg;

    @ApiModelProperty("重复数据处理方式")
    @TableField("duplicate_action")
    private String duplicateAction;

    @ApiModelProperty("导入时间")
    @TableField("import_time")
    private Date importTime;
}
```

---

## 7. 类设计说明

### 7.1 Controller层

#### BatchImportController

**职责**：批量导入控制器，处理所有导入相关的HTTP请求

**核心方法**：

```java
@RestController
@RequestMapping("/health/batch")
@Api(tags = "批量导入管理")
public class BatchImportController {

    @Autowired
    private IExcelParseService excelParseService;

    @Autowired
    private IImportRecordService importRecordService;

    /**
     * 上传并导入文件
     */
    @PostMapping("/import")
    @ApiOperation("上传并导入文件")
    @PreAuthorize("@ss.hasPermi('health:batch:import')")
    public AjaxResult importFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("importType") String importType,
            @RequestParam(value = "duplicateAction", defaultValue = "skip") String duplicateAction
    ) {
        // 参数校验
        // 调用Service层
        // 返回结果
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template/{importType}")
    @ApiOperation("下载导入模板")
    public void downloadTemplate(
            @PathVariable("importType") String importType,
            @RequestParam(value = "format", defaultValue = "xlsx") String format,
            HttpServletResponse response
    ) {
        // 根据类型生成模板
        // 返回文件流
    }

    /**
     * 查询导入历史列表
     */
    @GetMapping("/history")
    @ApiOperation("查询导入历史列表")
    @PreAuthorize("@ss.hasPermi('health:batch:list')")
    public TableDataInfo list(ImportHistoryQueryDTO queryDTO) {
        // 分页查询
        // 返回结果
    }

    /**
     * 查询导入详情
     */
    @GetMapping("/history/{id}")
    @ApiOperation("查询导入详情")
    @PreAuthorize("@ss.hasPermi('health:batch:query')")
    public AjaxResult getDetail(@PathVariable("id") Long id) {
        // 查询详情
        // 返回结果
    }

    /**
     * 删除导入记录
     */
    @DeleteMapping("/history/{id}")
    @ApiOperation("删除导入记录")
    @PreAuthorize("@ss.hasPermi('health:batch:remove')")
    public AjaxResult delete(@PathVariable("id") Long id) {
        // 删除记录
        // 返回结果
    }

    /**
     * 获取导入类型列表
     */
    @GetMapping("/types")
    @ApiOperation("获取导入类型列表")
    public AjaxResult getImportTypes() {
        // 返回类型列表
    }
}
```

---

### 7.2 Service层

#### IExcelParseService接口

```java
public interface IExcelParseService {

    /**
     * 解析Excel文件
     * @param file Excel文件
     * @param importType 导入类型
     * @return 解析结果（List<Map>）
     */
    List<Map<String, Object>> parseExcel(MultipartFile file, String importType);

    /**
     * 解析CSV文件
     * @param file CSV文件
     * @param importType 导入类型
     * @return 解析结果（List<Map>）
     */
    List<Map<String, Object>> parseCSV(MultipartFile file, String importType);

    /**
     * 验证数据
     * @param dataList 数据列表
     * @param importType 导入类型
     * @param duplicateAction 重复数据处理方式
     * @return 验证结果
     */
    ValidateResult validateData(List<Map<String, Object>> dataList, String importType, String duplicateAction);

    /**
     * 导入数据
     * @param validData 验证通过的数据
     * @param importType 导入类型
     * @return 导入结果
     */
    ImportResultDTO importData(List<Map<String, Object>> validData, String importType);
}
```

---

#### ExcelParseServiceImpl实现

```java
@Service
public class ExcelParseServiceImpl implements IExcelParseService {

    @Autowired
    private HealthDataMapper healthDataMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private IImportRecordService importRecordService;

    // Excel解析实现
    @Override
    public List<Map<String, Object>> parseExcel(MultipartFile file, String importType) {
        // 1. 文件格式校验
        // 2. 使用Apache POI解析
        // 3. 读取表头
        // 4. 读取数据行
        // 5. 数据类型转换
        // 6. 返回List<Map>
    }

    // CSV解析实现
    @Override
    public List<Map<String, Object>> parseCSV(MultipartFile file, String importType) {
        // 1. 文件格式校验
        // 2. 使用Commons CSV解析
        // 3. 处理编码和分隔符
        // 4. 读取表头和数据
        // 5. 返回List<Map>
    }

    // 数据验证实现
    @Override
    public ValidateResult validateData(List<Map<String, Object>> dataList, String importType, String duplicateAction) {
        // 1. 必填字段验证
        // 2. 数据格式验证
        // 3. 数据范围验证
        // 4. 重复数据检测
        // 5. 外键关联验证
        // 6. 返回验证结果
    }

    // 数据导入实现
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultDTO importData(List<Map<String, Object>> validData, String importType) {
        // 1. 数据转换（Map → Entity）
        // 2. 批量插入
        // 3. 记录导入历史
        // 4. 返回导入结果
    }
}
```

---

#### IImportRecordService接口

```java
public interface IImportRecordService extends IService<ImportRecord> {

    /**
     * 保存导入记录
     */
    void saveImportRecord(ImportRecord record);

    /**
     * 查询导入历史列表
     */
    List<ImportHistoryVO> getImportHistory(ImportHistoryQueryDTO queryDTO);

    /**
     * 查询导入详情
     */
    ImportDetailVO getImportDetail(Long id);

    /**
     * 删除导入记录
     */
    void deleteImportRecord(Long id);

    /**
     * 更新导入状态
     */
    void updateImportStatus(Long id, String status, String errorMsg);
}
```

---

#### ImportRecordServiceImpl实现

```java
@Service
public class ImportRecordServiceImpl extends ServiceImpl<ImportRecordMapper, ImportRecord>
        implements IImportRecordService {

    @Autowired
    private ImportRecordMapper importRecordMapper;

    // 保存导入记录
    @Override
    public void saveImportRecord(ImportRecord record) {
        importRecordMapper.insert(record);
    }

    // 查询导入历史列表
    @Override
    public List<ImportHistoryVO> getImportHistory(ImportHistoryQueryDTO queryDTO) {
        // 分页查询
        // Entity → VO转换
        // 返回列表
    }

    // 查询导入详情
    @Override
    public ImportDetailVO getImportDetail(Long id) {
        // 查询记录
        // 解析错误信息
        // 返回详情
    }

    // 删除导入记录
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImportRecord(Long id) {
        // 删除记录
        // （可选）同时删除导入的数据
    }

    // 更新导入状态
    @Override
    public void updateImportStatus(Long id, String status, String errorMsg) {
        // 更新状态和错误信息
    }
}
```

---

### 7.3 Mapper层

#### ImportRecordMapper

```java
@Mapper
public interface ImportRecordMapper extends BaseMapper<ImportRecord> {

    /**
     * 查询导入历史列表（分页）
     */
    List<ImportHistoryVO> selectImportHistoryList(
            @Param("queryDTO") ImportHistoryQueryDTO queryDTO,
            @Param("page") Page<ImportRecord> page
    );

    /**
     * 查询导入详情
     */
    ImportDetailVO selectImportDetail(@Param("id") Long id);
}
```

---

#### ImportRecordMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.qkyd.heal.mapper.ImportRecordMapper">

    <!-- 查询导入历史列表 -->
    <select id="selectImportHistoryList" resultType="com.qkyd.heal.domain.vo.ImportHistoryVO">
        SELECT
            id,
            import_type,
            CASE import_type
                WHEN 'health' THEN '健康数据'
                WHEN 'patient' THEN '服务对象'
                WHEN 'device' THEN '设备信息'
            END AS import_type_name,
            file_name,
            ROUND(file_size / 1024.0, 2) AS file_size_kb,
            total_rows,
            success_rows,
            fail_rows,
            ROUND(success_rows * 100.0 / total_rows, 2) AS success_rate,
            status,
            CASE status
                WHEN 'success' THEN '成功'
                WHEN 'failed' THEN '失败'
                WHEN 'processing' THEN '处理中'
            END AS status_name,
            duplicate_action,
            import_time,
            user_id,
            create_by AS user_name
        FROM health_import_record
        <where>
            <if test="queryDTO.importType != null and queryDTO.importType != ''">
                AND import_type = #{queryDTO.importType}
            </if>
            <if test="queryDTO.status != null and queryDTO.status != ''">
                AND status = #{queryDTO.status}
            </if>
            <if test="queryDTO.startTime != null and queryDTO.startTime != ''">
                AND import_time >= #{queryDTO.startTime}
            </if>
            <if test="queryDTO.endTime != null and queryDTO.endTime != ''">
                AND import_time &lt;= #{queryDTO.endTime}
            </if>
        </where>
        ORDER BY import_time DESC
    </select>

    <!-- 查询导入详情 -->
    <select id="selectImportDetail" resultType="com.qkyd.heal.domain.vo.ImportDetailVO">
        SELECT
            id,
            import_type,
            CASE import_type
                WHEN 'health' THEN '健康数据'
                WHEN 'patient' THEN '服务对象'
                WHEN 'device' THEN '设备信息'
            END AS import_type_name,
            file_name,
            ROUND(file_size / 1024.0, 2) AS file_size_kb,
            total_rows,
            success_rows,
            fail_rows,
            status,
            error_msg,
            duplicate_action,
            import_time,
            create_by AS user_name
        FROM health_import_record
        WHERE id = #{id}
    </select>

</mapper>
```

---

## 8. 时序图

### 8.1 批量导入时序图

```
用户          前端页面        Controller        Service          Mapper          数据库
 │             │                │                │                │                │
 ├─上传文件───>│                │                │                │                │
 │             │─POST /import──>│                │                │                │
 │             │                │─参数校验──────>│                │                │
 │             │                │<─通过─────────│                │                │
 │             │                │                │─解析Excel─────>│                │
 │             │                │                │<─数据列表─────│                │
 │             │                │                │─验证数据─────>│                │
 │             │                │                │<─验证结果─────│                │
 │             │                │                │─转换实体──────>│                │
 │             │                │                │                │─批量插入─────>│
 │             │                │                │                │<─成功─────────│
 │             │                │                │─保存导入记录──>│                │
 │             │                │                │                │─insert──────>│
 │             │                │                │                │<─成功─────────│
 │             │                │<─导入结果─────│                │                │
 │             │<─结果展示──────│                │                │                │
 │<─显示结果───│                │                │                │                │
```

---

### 8.2 查询导入历史时序图

```
用户          前端页面        Controller        Service          Mapper          数据库
 │             │                │                │                │                │
 ├─查询历史───>│                │                │                │                │
 │             │─GET /history─>│                │                │                │
 │             │                │─查询参数─────>│                │                │
 │             │                │                │─查询记录──────>│                │
 │             │                │                │                │─SELECT──────>│
 │             │                │                │                │<─数据列表─────│
 │             │                │                │<─Entity列表───│                │
 │             │                │<─VO列表───────│                │                │
 │             │<─表格渲染─────│                │                │                │
 │<─显示列表───│                │                │                │                │
```

---

## 9. 技术实现要点

### 9.1 Excel解析

**使用Apache POI实现流式读取**：

```java
// XSSF（.xlsx）流式读取
try (InputStream is = file.getInputStream();
     XSSFWorkbook workbook = new XSSFWorkbook(is)) {

    XSSFSheet sheet = workbook.getSheetAt(0);

    // 读取表头
    XSSFRow headerRow = sheet.getRow(0);
    List<String> headers = new ArrayList<>();
    for (Cell cell : headerRow) {
        headers.add(cell.getStringCellValue());
    }

    // 读取数据行
    List<Map<String, Object>> dataList = new ArrayList<>();
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        XSSFRow row = sheet.getRow(i);
        if (row == null) continue;

        Map<String, Object> rowData = new HashMap<>();
        for (int j = 0; j < headers.size(); j++) {
            Cell cell = row.getCell(j);
            String header = headers.get(j);
            Object value = getCellValue(cell);
            rowData.put(header, value);
        }
        dataList.add(rowData);
    }
}
```

**单元格值获取**：

```java
private Object getCellValue(Cell cell) {
    if (cell == null) {
        return null;
    }

    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue().trim();
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        case BOOLEAN:
            return cell.getBooleanCellValue();
        case FORMULA:
            return cell.getCellFormula();
        default:
            return null;
    }
}
```

---

### 9.2 CSV解析

**使用Apache Commons CSV**：

```java
private List<Map<String, Object>> parseCSV(MultipartFile file, String importType) throws IOException {

    CSVFormat format = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim();

    try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
         CSVParser parser = CSVParser.parse(reader, format)) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (CSVRecord record : parser.getRecords()) {
            Map<String, Object> rowMap = new HashMap<>();
            record.toMap().forEach((k, v) -> rowMap.put(k, convertValue(v)));
            dataList.add(rowMap);
        }
        return dataList;
    }
}
```

---

### 9.3 数据验证

**验证规则配置**：

```java
// 健康数据验证规则
private static final Map<String, ValidationRule> HEALTH_VALIDATION_RULES = Map.of(
    "user_id", new ValidationRule(true, "required", null, null),
    "device_id", new ValidationRule(true, "required", null, null),
    "heart_rate", new ValidationRule(false, "numeric", "0", "300"),
    "blood_pressure_systolic", new ValidationRule(false, "numeric", "50", "250"),
    "blood_pressure_diastolic", new ValidationRule(false, "numeric", "30", "150"),
    "blood_oxygen", new ValidationRule(false, "numeric", "70", "100"),
    "body_temperature", new ValidationRule(false, "numeric", "35.0", "42.0"),
    "record_time", new ValidationRule(true, "datetime", null, null)
);
```

**验证逻辑**：

```java
private ValidateResult validateData(
        List<Map<String, Object>> dataList,
        String importType,
        String duplicateAction) {

    ValidateResult result = new ValidateResult();
    List<Map<String, Object>> validData = new ArrayList<>();
    List<ErrorDetail> errors = new ArrayList<>();

    Map<String, ValidationRule> rules = getValidationRules(importType);

    for (int i = 0; i < dataList.size(); i++) {
        Map<String, Object> row = dataList.get(i);
        int rowNum = i + 2; // Excel行号（表头是第1行）

        boolean rowValid = true;

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            ValidationRule rule = rules.get(column);

            if (rule == null) continue;

            // 必填验证
            if (rule.isRequired() && isEmpty(value)) {
                errors.add(createError(rowNum, column, value, "不能为空"));
                rowValid = false;
                continue;
            }

            // 数据类型验证
            if (!isEmpty(value)) {
                if ("numeric".equals(rule.getType()) && !isNumeric(value)) {
                    errors.add(createError(rowNum, column, value, "必须是数字"));
                    rowValid = false;
                }
                else if ("datetime".equals(rule.getType()) && !isDatetime(value)) {
                    errors.add(createError(rowNum, column, value, "日期格式错误"));
                    rowValid = false;
                }
            }

            // 范围验证
            if (!isEmpty(value) && rule.getMin() != null && rule.getMax() != null) {
                double numValue = parseDouble(value);
                if (numValue < parseDouble(rule.getMin()) || numValue > parseDouble(rule.getMax())) {
                    errors.add(createError(rowNum, column, value,
                        String.format("超出范围[%s, %s]", rule.getMin(), rule.getMax())));
                    rowValid = false;
                }
            }
        }

        // 重复数据检测
        if (rowValid) {
            boolean isDuplicate = checkDuplicate(row, importType, duplicateAction);
            if (isDuplicate) {
                errors.add(createError(rowNum, "", "", "数据已存在"));
                rowValid = false;
            }
        }

        if (rowValid) {
            validData.add(row);
        }
    }

    result.setValidData(validData);
    result.setErrors(errors);
    return result;
}
```

---

### 9.4 批量插入

**MyBatis-Plus批量插入**：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public ImportResultDTO importData(List<Map<String, Object>> validData, String importType) {

    ImportResultDTO result = new ImportResultDTO();
    int successCount = 0;

    try {
        switch (importType) {
            case "health":
                List<HealthData> healthDataList = convertToHealthData(validData);
                successCount = insertBatch(healthDataList, healthDataMapper);
                break;
            case "patient":
                List<Patient> patientList = convertToPatient(validData);
                successCount = insertBatch(patientList, patientMapper);
                break;
            case "device":
                List<Device> deviceList = convertToDevice(validData);
                successCount = insertBatch(deviceList, deviceMapper);
                break;
        }

        result.setSuccess(true);
        result.setMessage("导入成功");
        result.setSuccessRows(successCount);

    } catch (Exception e) {
        result.setSuccess(false);
        result.setMessage("导入失败：" + e.getMessage());
        throw e;
    }

    return result;
}

// 通用批量插入方法
private <T> int insertBatch(List<T> list, BaseMapper<T> mapper) {
    if (list.isEmpty()) {
        return 0;
    }

    // 分批插入（每批1000条）
    int batchSize = 1000;
    int total = 0;

    for (int i = 0; i < list.size(); i += batchSize) {
        int end = Math.min(i + batchSize, list.size());
        List<T> subList = list.subList(i, end);
        for (T entity : subList) {
            mapper.insert(entity);
            total++;
        }
    }

    return total;
}
```

---

## 10. 性能优化策略

### 10.1 解析优化

| 优化策略 | 说明 | 预期效果 |
|---------|------|---------|
| 流式读取 | 使用XSSF和SAX模式，避免一次性加载全部数据 | 内存占用降低70% |
| 并行解析 | 大文件使用多线程分片解析 | 解析速度提升40% |
| 字符池 | 复用字符串对象，减少GC | GC次数减少50% |

---

### 10.2 数据库优化

| 优化策略 | 说明 | 预期效果 |
|---------|------|---------|
| 批量插入 | 使用MyBatis批量插入，每批1000条 | 插入速度提升5-10倍 |
| 索引优化 | 为查询字段建立复合索引 | 查询速度提升3-5倍 |
| 事务控制 | 合理设置事务边界，减少锁等待 | 并发性能提升30% |
| 连接池 | 使用HikariCP连接池 | 连接获取效率提升50% |

---

### 10.3 缓存优化

| 缓存对象 | 缓存策略 | TTL | 说明 |
|---------|---------|-----|------|
| 导入模板 | 本地缓存 | 24h | 减少重复生成 |
| 导入类型列表 | Redis缓存 | 1h | 减少数据库查询 |
| 验证规则 | 本地缓存 | 启动时加载 | 减少反射调用 |

---

### 10.4 异步处理

**大文件异步导入**：

```java
@Async("taskExecutor")
public CompletableFuture<ImportResultDTO> asyncImport(
        MultipartFile file,
        String importType,
        String duplicateAction,
        Long userId) {

    // 1. 保存导入记录（状态：processing）
    ImportRecord record = createImportRecord(file, importType, userId);
    importRecordService.saveImportRecord(record);

    try {
        // 2. 执行导入
        ImportResultDTO result = importFile(file, importType, duplicateAction);

        // 3. 更新导入记录
        importRecordService.updateImportStatus(
            record.getId(),
            result.getSuccess() ? "success" : "failed",
            null
        );

        return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
        // 4. 更新失败状态
        importRecordService.updateImportStatus(
            record.getId(),
            "failed",
            e.getMessage()
        );
        throw e;
    }
}
```

---

## 附录

### A. 文件清单

```
后端文件:
├── qkyd-health/src/main/java/com/qkyd/heal/
│   ├── controller/
│   │   └── BatchImportController.java
│   ├── domain/
│   │   ├── ImportRecord.java
│   │   ├── dto/
│   │   │   ├── BatchImportDTO.java
│   │   │   ├── ImportResultDTO.java
│   │   │   └── ImportHistoryQueryDTO.java
│   │   └── vo/
│   │       ├── ImportHistoryVO.java
│   │       ├── ImportDetailVO.java
│   │       └── ImportTypeVO.java
│   ├── mapper/
│   │   ├── ImportRecordMapper.java
│   │   └── xml/
│   │       └── ImportRecordMapper.xml
│   ├── service/
│   │   ├── IExcelParseService.java
│   │   ├── IImportRecordService.java
│   │   └── impl/
│   │       ├── ExcelParseServiceImpl.java
│   │       └── ImportRecordServiceImpl.java
│   └── config/
│       └── AsyncTaskConfig.java
└── sql/
    └── health_import_record.sql

前端文件:
├── RuoYi-Vue3-Modern/src/
│   ├── views/health/batch-import/
│   │   └── index.vue
│   └── api/health/
│       └── batch.js
```

---

### B. 参考资料

- [Apache POI官方文档](https://poi.apache.org/)
- [Apache Commons CSV官方文档](https://commons.apache.org/proper/commons-csv/)
- [MyBatis-Plus官方文档](https://baomidou.com/)
- [Spring Boot异步任务](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)
- [Element Plus Upload组件](https://element-plus.org/zh-CN/component/upload.html)

---

**文档结束**
