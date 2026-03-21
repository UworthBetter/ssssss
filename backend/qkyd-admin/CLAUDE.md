# qkyd-admin 妯″潡

[鏍圭洰褰昡(../CLAUDE.md) > **qkyd-admin**

---

## 鍙樻洿璁板綍 (Changelog)

| 鏃ユ湡 | 鐗堟湰 | 鍙樻洿鍐呭 |
|------|------|----------|
| 2026-02-01 14:18:39 | v3.8.7 | 鍒濆鍖栨ā鍧楁枃妗?|

---

## 妯″潡鑱岃矗

qkyd-admin 鏄暣涓簲鐢ㄧ殑 Web 鏈嶅姟鍏ュ彛妯″潡锛岃礋璐ｏ細

1. **搴旂敤鍚姩**锛氬寘鍚?`QkydApplication` 涓诲惎鍔ㄧ被
2. **鎺у埗鍣ㄥ眰**锛氱郴缁熺鐞嗐€佺洃鎺с€侀€氱敤鍔熻兘鐨?REST API
3. **閰嶇疆绠＄悊**锛歋wagger銆佸簲鐢ㄩ厤缃殑鍔犺浇

---

## 鍏ュ彛涓庡惎鍔?

### 涓诲惎鍔ㄧ被
```java
// 鏂囦欢: src/main/java/com/qkyd/QkydApplication.java
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class QkydApplication {
    public static void main(String[] args) {
        SpringApplication.run(QkydApplication.class, args);
    }
}
```

### 鍚姩鍙傛暟
- **榛樿绔彛**: 8098
- **涓婁笅鏂囪矾寰?*: `/`
- **閰嶇疆鏂囦欢**: `application.yml`

---

## 瀵瑰鎺ュ彛

### 鎺у埗鍣ㄥ垎绫?

#### 1. 绯荤粺绠＄悊 (system)
| 璺緞 | 鎺у埗鍣?| 鍔熻兘 |
|------|--------|------|
| `/system/user` | `SysUserController` | 鐢ㄦ埛绠＄悊 |
| `/system/role` | `SysRoleController` | 瑙掕壊绠＄悊 |
| `/system/menu` | `SysMenuController` | 鑿滃崟绠＄悊 |
| `/system/dept` | `SysDeptController` | 閮ㄩ棬绠＄悊 |
| `/system/post` | `SysPostController` | 宀椾綅绠＄悊 |
| `/system/dict/*` | `SysDictTypeController` | 瀛楀吀绠＄悊 |
| `/system/config` | `SysConfigController` | 鍙傛暟閰嶇疆 |
| `/system/notice` | `SysNoticeController` | 閫氱煡鍏憡 |
| `/system/login` | `SysLoginController` | 鐧诲綍璁よ瘉 |
| `/system/register` | `SysRegisterController` | 鐢ㄦ埛娉ㄥ唽 |

#### 2. 绯荤粺鐩戞帶 (monitor)
| 璺緞 | 鎺у埗鍣?| 鍔熻兘 |
|------|--------|------|
| `/monitor/online` | `SysUserOnlineController` | 鍦ㄧ嚎鐢ㄦ埛 |
| `/monitor/logininfor` | `SysLogininforController` | 鐧诲綍鏃ュ織 |
| `/monitor/operlog` | `SysOperlogController` | 鎿嶄綔鏃ュ織 |
| `/monitor/server` | `ServerController` | 鏈嶅姟鐩戞帶 |
| `/monitor/cache` | `CacheController` | 缂撳瓨鐩戞帶 |

#### 3. 閫氱敤鍔熻兘 (common)
| 璺緞 | 鎺у埗鍣?| 鍔熻兘 |
|------|--------|------|
| `/common/captcha` | `CaptchaController` | 楠岃瘉鐮?|
| `/common/upload` | `CommonController` | 鏂囦欢涓婁紶 |

#### 4. 娴嬭瘯宸ュ叿 (tool)
| 璺緞 | 鎺у埗鍣?| 鍔熻兘 |
|------|--------|------|
| `/tool/test` | `TestController` | 娴嬭瘯鎺ュ彛 |

---

## 鍏抽敭渚濊禆涓庨厤缃?

### Maven 渚濊禆
```xml
<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>

<!-- MySQL 椹卞姩 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- 渚濊禆鐨勪笟鍔℃ā鍧?-->
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-framework</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-system</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-quartz</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-health</artifactId>
</dependency>
<dependency>
    <groupId>com.qkyd</groupId>
    <artifactId>qkyd-ai</artifactId>
</dependency>
```

### 閰嶇疆鏂囦欢
- **涓婚厤缃?*: `src/main/resources/application.yml`
- **鏁版嵁婧愰厤缃?*: `src/main/resources/application-druid.yml`
- **MyBatis 閰嶇疆**: `src/main/resources/mybatis/mybatis-config.xml`
- **鏃ュ織閰嶇疆**: `src/main/resources/logback.xml`

---

## 鏁版嵁妯″瀷

鏈ā鍧椾笉鐩存帴瀹氫箟鏁版嵁妯″瀷锛屾墍鏈夊疄浣撶被鍦ㄤ笟鍔℃ā鍧椾腑瀹氫箟锛?

| 鏁版嵁绫诲瀷 | 鏉ユ簮妯″潡 |
|---------|----------|
| 绯荤粺鐢ㄦ埛銆佽鑹层€佽彍鍗?| `qkyd-system` |
| 鍋ュ悍鏁版嵁銆佽澶囦俊鎭?| `qkyd-health` |
| AI 鍒嗘瀽缁撴灉 | `qkyd-ai` |

---

## 娴嬭瘯涓庤川閲?

### 鍗曞厓娴嬭瘯
- 娴嬭瘯鐩綍锛歚src/test/java/`
- 杩愯鍛戒护锛歚mvn test -Dtest=*ControllerTest`

### API 鏂囨。
- **Swagger UI**: http://localhost:8098/doc.html
- **OpenAPI JSON**: http://localhost:8098/v3/api-docs

---

## 甯歌闂 (FAQ)

### Q1: 濡備綍淇敼榛樿绔彛锛?
缂栬緫 `application.yml`锛?
```yaml
server:
  port: 8098  # 淇敼涓虹洰鏍囩鍙?
```

### Q2: 濡備綍娣诲姞鏂扮殑鎺у埗鍣紵
1. 鍦?`src/main/java/com/qkyd/web/controller/` 涓嬪垱寤烘帶鍒跺櫒绫?
2. 娣诲姞 `@RestController` 鍜?`@RequestMapping` 娉ㄨВ
3. 娉ㄥ叆涓氬姟 Service锛堟潵鑷叾浠栨ā鍧楋級

### Q3: 濡備綍閰嶇疆璺ㄥ煙锛?
宸查€氳繃 `ResourcesConfig` 閰嶇疆璺ㄥ煙锛岄粯璁ゅ厑璁告墍鏈夋潵婧愩€?

---

## 鐩稿叧鏂囦欢娓呭崟

```
qkyd-admin/
鈹溾攢鈹€ src/main/java/com/qkyd/
鈹?  鈹溾攢鈹€ QkydApplication.java              # 鍚姩绫?
鈹?  鈹溾攢鈹€ QkydServletInitializer.java       # Servlet 鍒濆鍖?
鈹?  鈹斺攢鈹€ web/
鈹?      鈹溾攢鈹€ controller/
鈹?      鈹?  鈹溾攢鈹€ common/                    # 閫氱敤鎺у埗鍣?
鈹?      鈹?  鈹溾攢鈹€ monitor/                   # 鐩戞帶鎺у埗鍣?
鈹?      鈹?  鈹溾攢鈹€ system/                    # 绯荤粺绠＄悊鎺у埗鍣?
鈹?      鈹?  鈹斺攢鈹€ tool/                      # 宸ュ叿鎺у埗鍣?
鈹?      鈹斺攢鈹€ core/config/SwaggerConfig.java # Swagger 閰嶇疆
鈹溾攢鈹€ src/main/resources/
鈹?  鈹溾攢鈹€ application.yml                    # 涓婚厤缃?
鈹?  鈹溾攢鈹€ application-druid.yml              # 鏁版嵁婧愰厤缃?
鈹?  鈹溾攢鈹€ mybatis/mybatis-config.xml         # MyBatis 閰嶇疆
鈹?  鈹斺攢鈹€ logback.xml                        # 鏃ュ織閰嶇疆
鈹斺攢鈹€ pom.xml                                # Maven 閰嶇疆
```

---

**鏈€鍚庢洿鏂?*: 2026-02-01 14:18:39


