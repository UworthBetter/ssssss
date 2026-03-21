# qkyd-common 妯″潡

[鏍圭洰褰昡(../CLAUDE.md) > **qkyd-common**

---

## 鍙樻洿璁板綍 (Changelog)

| 鏃ユ湡 | 鐗堟湰 | 鍙樻洿鍐呭 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 鍒濆鍖栨ā鍧楁枃妗?|

---

## 妯″潡鑱岃矗

qkyd-common 鏄€氱敤宸ュ叿妯″潡锛屾彁渚涙暣涓」鐩殑鍩虹璁炬柦锛?

1. **鏍稿績鍩虹绫?*锛氭帶鍒跺櫒鍩虹被銆佸疄浣撳熀绫汇€佸垎椤垫敮鎸?
2. **宸ュ叿绫?*锛氬瓧绗︿覆銆佹棩鏈熴€佹枃浠躲€佸姞瀵嗐€丠TTP銆両P 绛?
3. **娉ㄨВ瀹氫箟**锛欵xcel 瀵煎嚭銆佹暟鎹潈闄愩€佹棩蹇椼€侀噸澶嶆彁浜ょ瓑
4. **寮傚父澶勭悊**锛氬叏灞€寮傚父瀹氫箟
5. **甯搁噺鏋氫妇**锛氱郴缁熺骇甯搁噺

---

## 鏍稿績缁勪欢

### 鍩虹绫?(core)

| 绫诲悕 | 鍔熻兘 |
|------|------|
| `BaseController` | 鎺у埗鍣ㄥ熀绫伙紝鎻愪緵閫氱敤鏂规硶 |
| `AjaxResult` | 缁熶竴鍝嶅簲缁撴灉灏佽 |
| `R` | 绠€鍖栧搷搴旂粨鏋滃皝瑁?|
| `BaseEntity` | 瀹炰綋鍩虹被锛屽寘鍚€氱敤瀛楁 |
| `TreeEntity` | 鏍戝舰瀹炰綋鍩虹被 |
| `TreeSelect` | 鏍戝舰閫夋嫨缁撴瀯 |
| `PageDomain` | 鍒嗛〉鍙傛暟灏佽 |
| `TableDataInfo` | 琛ㄦ牸鏁版嵁鍝嶅簲 |
| `TableSupport` | 鍒嗛〉鏀寔宸ュ叿 |
| `RedisCache` | Redis 缂撳瓨灏佽 |

### 鏍稿績瀹炰綋 (core/domain/entity)
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `SysUser` | 绯荤粺鐢ㄦ埛瀹炰綋 |
| `SysRole` | 绯荤粺瑙掕壊瀹炰綋 |
| `SysMenu` | 绯荤粺鑿滃崟瀹炰綋 |
| `SysDept` | 绯荤粺閮ㄩ棬瀹炰綋 |
| `SysDictType` | 瀛楀吀绫诲瀷瀹炰綋 |
| `SysDictData` | 瀛楀吀鏁版嵁瀹炰綋 |

### 鐧诲綍妯″瀷 (core/domain/model)
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `LoginUser` | 鐧诲綍鐢ㄦ埛淇℃伅 |
| `LoginBody` | 鐧诲綍璇锋眰浣?|
| `RegisterBody` | 娉ㄥ唽璇锋眰浣?|

---

## 宸ュ叿绫?(utils)

### 瀛楃涓蹭笌鏂囨湰
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `StringUtils` | 瀛楃涓插伐鍏?|
| `StrFormatter` | 瀛楃涓叉牸寮忓寲 |
| `CharsetKit` | 瀛楃闆嗗伐鍏?|

### 鏃ユ湡鏃堕棿
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `DateUtils` | 鏃ユ湡宸ュ叿 |

### 鍔犲瘑涓庣鍚?
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `Md5Utils` | MD5 鍔犲瘑 |
| `Base64` | Base64 缂栫爜 |

### 鏂囦欢澶勭悊
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `FileUtils` | 鏂囦欢宸ュ叿 |
| `FileUploadUtils` | 鏂囦欢涓婁紶 |
| `FileTypeUtils` | 鏂囦欢绫诲瀷鍒ゆ柇 |
| `ImageUtils` | 鍥剧墖澶勭悊 |
| `MimeTypeUtils` | MIME 绫诲瀷 |

### HTTP 涓?IP
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `HttpUtils` | HTTP 宸ュ叿 |
| `HttpHelper` | HTTP 杈呭姪 |
| `IpUtils` | IP 鍦板潃宸ュ叿 |
| `AddressUtils` | 鍦板潃瑙ｆ瀽 |

### 鍏朵粬
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `ServletUtils` | Servlet 宸ュ叿 |
| `SecurityUtils` | 瀹夊叏宸ュ叿 |
| `DictUtils` | 瀛楀吀宸ュ叿 |
| `MessageUtils` | 娑堟伅宸ュ叿 |
| `LogUtils` | 鏃ュ織宸ュ叿 |
| `ExceptionUtil` | 寮傚父宸ュ叿 |
| `PageUtils` | 鍒嗛〉宸ュ叿 |
| `Arith` | 鏁板璁＄畻 |
| `BeanUtils` | Bean 宸ュ叿 |
| `BeanValidators` | Bean 楠岃瘉 |
| `SpringUtils` | Spring 宸ュ叿 |
| `ReflectUtils` | 鍙嶅皠宸ュ叿 |
| `SqlUtil` | SQL 宸ュ叿 |

### HTML 澶勭悊
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `EscapeUtil` | 杞箟宸ュ叿 |
| `HTMLFilter` | HTML 杩囨护鍣?|

### Excel 澶勭悊
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `ExcelUtil` | Excel 宸ュ叿 |
| `ExcelHandlerAdapter` | Excel 澶勭悊閫傞厤鍣?|

---

## 娉ㄨВ (annotation)

| 娉ㄨВ | 鍔熻兘 |
|------|------|
| `Anonymous` | 鍖垮悕璁块棶锛堟棤闇€鐧诲綍锛?|
| `DataScope` | 鏁版嵁鏉冮檺杩囨护 |
| `DataSource` | 鍔ㄦ€佹暟鎹簮 |
| `Excel` | Excel 瀵煎嚭娉ㄨВ |
| `Excels` | Excel 瀵煎嚭娉ㄨВ锛堝涓級 |
| `Log` | 鎿嶄綔鏃ュ織璁板綍 |
| `RateLimiter` | 闄愭祦娉ㄨВ |
| `RepeatSubmit` | 闃查噸澶嶆彁浜?|

---

## 鏋氫妇 (enums)

| 鏋氫妇 | 鍔熻兘 |
|------|------|
| `BusinessStatus` | 涓氬姟鐘舵€佺爜 |
| `BusinessType` | 涓氬姟绫诲瀷 |
| `DataSourceType` | 鏁版嵁婧愮被鍨?|
| `HttpMethod` | HTTP 鏂规硶 |
| `LimitType` | 闄愭祦绫诲瀷 |
| `OperatorType` | 鎿嶄綔鑰呯被鍨?|
| `UserStatus` | 鐢ㄦ埛鐘舵€?|

---

## 甯搁噺 (constant)

| 甯搁噺绫?| 鍔熻兘 |
|--------|------|
| `Constants` | 閫氱敤甯搁噺 |
| `CacheConstants` | 缂撳瓨甯搁噺 |
| `HttpStatus` | HTTP 鐘舵€佺爜 |
| `GenConstants` | 浠ｇ爜鐢熸垚甯搁噺 |
| `ScheduleConstants` | 瀹氭椂浠诲姟甯搁噺 |
| `UserConstants` | 鐢ㄦ埛甯搁噺 |
| `WatchPushActionType` | 鎵嬭〃鎺ㄩ€佸姩浣滅被鍨?|
| `WatchBNPushActionType` | 鎵嬭〃 B&N 鎺ㄩ€佸姩浣滅被鍨?|

---

## 寮傚父 (exception)

### 鍩虹寮傚父
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `BaseException` | 鍩虹寮傚父 |
| `GlobalException` | 鍏ㄥ眬寮傚父 |
| `ServiceException` | 鏈嶅姟寮傚父 |
| `UtilException` | 宸ュ叿寮傚父 |
| `DemoModeException` | 婕旂ず妯″紡寮傚父 |

### 鐢ㄦ埛寮傚父
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `UserException` | 鐢ㄦ埛寮傚父 |
| `UserNotExistsException` | 鐢ㄦ埛涓嶅瓨鍦?|
| `UserPasswordNotMatchException` | 瀵嗙爜涓嶅尮閰?|
| `UserPasswordRetryLimitExceedException` | 瀵嗙爜閲嶈瘯瓒呴檺 |
| `BlackListException` | 榛戝悕鍗曞紓甯?|
| `CaptchaException` | 楠岃瘉鐮佸紓甯?|
| `CaptchaExpireException` | 楠岃瘉鐮佽繃鏈?|

### 鏂囦欢寮傚父
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `FileException` | 鏂囦欢寮傚父 |
| `FileUploadException` | 鏂囦欢涓婁紶寮傚父 |
| `FileNameLengthLimitExceededException` | 鏂囦欢鍚嶈繃闀?|
| `FileSizeLimitExceededException` | 鏂囦欢澶у皬瓒呴檺 |
| `InvalidExtensionException` | 鏃犳晥鏂囦欢鎵╁睍鍚?|

### 浠诲姟寮傚父
| 绫诲悕 | 鍔熻兘 |
|------|------|
| `TaskException` | 瀹氭椂浠诲姟寮傚父 |

---

## 杩囨护鍣?(filter)

| 绫诲悕 | 鍔熻兘 |
|------|------|
| `XssFilter` | XSS 杩囨护鍣?|
| `XssHttpServletRequestWrapper` | XSS 璇锋眰鍖呰鍣?|
| `RepeatableFilter` | 鍙噸澶嶆彁浜よ繃婊ゅ櫒 |
| `RepeatedlyRequestWrapper` | 閲嶅璇锋眰鍖呰鍣?|
| `PropertyPreExcludeFilter` | 灞炴€ф帓闄よ繃婊ゅ櫒 |

---

## 閰嶇疆绫?(config)

| 绫诲悕 | 鍔熻兘 |
|------|------|
| `QkydConfig` | 鑻ヤ緷閰嶇疆 |

---

## 鐩稿叧鏂囦欢娓呭崟

```
qkyd-common/
鈹溾攢鈹€ src/main/java/com/qkyd/common/
鈹?  鈹溾攢鈹€ annotation/              # 娉ㄨВ瀹氫箟
鈹?  鈹溾攢鈹€ config/                  # 閰嶇疆绫?
鈹?  鈹溾攢鈹€ constant/                # 甯搁噺瀹氫箟
鈹?  鈹溾攢鈹€ core/                    # 鏍稿績绫?
鈹?  鈹?  鈹溾攢鈹€ controller/          # 鎺у埗鍣ㄥ熀绫?
鈹?  鈹?  鈹溾攢鈹€ domain/              # 鏍稿績瀹炰綋
鈹?  鈹?  鈹溾攢鈹€ page/                # 鍒嗛〉鏀寔
鈹?  鈹?  鈹溾攢鈹€ redis/               # Redis 灏佽
鈹?  鈹?  鈹斺攢鈹€ text/                # 鏂囨湰宸ュ叿
鈹?  鈹溾攢鈹€ enums/                   # 鏋氫妇瀹氫箟
鈹?  鈹溾攢鈹€ exception/               # 寮傚父瀹氫箟
鈹?  鈹?  鈹溾攢鈹€ base/                # 鍩虹寮傚父
鈹?  鈹?  鈹溾攢鈹€ file/                # 鏂囦欢寮傚父
鈹?  鈹?  鈹溾攢鈹€ job/                 # 浠诲姟寮傚父
鈹?  鈹?  鈹斺攢鈹€ user/                # 鐢ㄦ埛寮傚父
鈹?  鈹溾攢鈹€ filter/                  # 杩囨护鍣?
鈹?  鈹斺攢鈹€ utils/                   # 宸ュ叿绫?
鈹?      鈹溾攢鈹€ bean/                # Bean 宸ュ叿
鈹?      鈹溾攢鈹€ file/                # 鏂囦欢宸ュ叿
鈹?      鈹溾攢鈹€ html/                # HTML 宸ュ叿
鈹?      鈹溾攢鈹€ http/                # HTTP 宸ュ叿
鈹?      鈹溾攢鈹€ ip/                  # IP 宸ュ叿
鈹?      鈹溾攢鈹€ poi/                 # Excel 宸ュ叿
鈹?      鈹溾攢鈹€ sign/                # 绛惧悕宸ュ叿
鈹?      鈹溾攢鈹€ sql/                 # SQL 宸ュ叿
鈹?      鈹斺攢鈹€ spring/              # Spring 宸ュ叿
鈹斺攢鈹€ pom.xml
```

---

**鏈€鍚庢洿鏂?*: 2026-02-01 14:18:39

