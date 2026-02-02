# 健康管理平台 - 云服务器部署指南

## 服务器信息
- **实例ID**: lhins-nty01vai
- **公网IP**: 212.64.84.112
- **地域**: ap-shanghai
- **系统**: OpenCloudOS Linux
- **Docker版本**: 26.1.3

## 快速部署(推荐)

### 前置要求

1. **安装PuTTY工具**
   - 下载地址: https://www.chiark.greenend.org.uk/~sgtatham/putty/
   - 安装后确保PuTTY安装目录在系统PATH环境变量中
   - 验证安装: 在命令行执行 `where pscp` 和 `where plink`

2. **构建项目**
   ```powershell
   cd d:\jishe\1.19
   mvn clean package -DskipTests
   ```

### 一键部署

运行自动化部署脚本:
```powershell
cd d:\jishe\1.19
deploy-to-cloud.bat
```

脚本会自动完成以下操作:
1. 检查PuTTY工具
2. 上传后端jar包到服务器
3. 上传配置文件
4. 创建Docker容器并启动服务
5. 验证服务健康状态

**注意**: 脚本已配置密码,无需手动输入。

### 验证部署

部署完成后,访问以下地址验证:
- **后端API**: http://212.64.84.112:8080
- **健康检查**: http://212.64.84.112:8080/actuator/health

如果返回 `{"status":"UP"}`,说明服务启动成功。

---

## 手动部署方案

### 方案一: 手动上传部署

#### 1. 上传后端jar包

在本地Windows PowerShell中执行:

```powershell
# 进入项目目录
cd d:\jishe\1.19

# 上传后端jar包
scp qkyd-admin\target\qkyd-admin.jar root@212.64.84.112:/root/qkyd/backend/

# 上传配置文件
scp application-prod.yml root@212.64.84.112:/root/qkyd/

# 上传docker-compose.yml
scp dist/docker-compose.yml root@212.64.84.112:/root/qkyd/
```

**注意**: 执行SCP命令时需要输入服务器root密码。

#### 2. 登录服务器

```bash
ssh root@212.64.84.112
```

#### 3. 配置数据库

编辑配置文件:
```bash
vi /root/qkyd/application-prod.yml
```

修改数据库连接信息:
```yaml
spring:
  datasource:
    url: jdbc:mysql://your_mysql_host:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_mysql_user
    password: your_mysql_password
```

**数据库选项**:
- **选项A**: 使用腾讯云MySQL(需要先在控制台创建MySQL实例)
- **选项B**: 在服务器上使用Docker运行MySQL:

```bash
docker run --name qkyd-mysql \
  -e MYSQL_ROOT_PASSWORD=your_secure_password \
  -e MYSQL_DATABASE=ry-vue \
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql \
  -d mysql:8.0

# 等待MySQL启动(约30秒)
sleep 30

# 导入数据库(需要先上传sql文件到服务器)
docker exec -i qkyd-mysql mysql -uroot -pyour_secure_password ry-vue < /path/to/ry-vue.sql
```

#### 4. 启动后端服务

```bash
cd /root/qkyd
docker-compose up -d
```

#### 5. 检查服务状态

```bash
# 查看容器状态
docker ps

# 查看后端日志
docker logs qkyd-backend

# 实时查看日志
docker logs -f qkyd-backend
```

#### 6. 验证部署

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 从外部访问
curl http://212.64.84.112:8080/actuator/health
```

如果返回 `{"status":"UP"}`,说明服务启动成功。

### 方案二: 使用SFTP工具上传

如果不熟悉命令行,可以使用图形化工具:

#### 推荐工具
- **WinSCP**: https://winscp.net/
- **FileZilla**: https://filezilla-project.org/
- **MobaXterm**: https://mobaxterm.mobatek.net/

#### 使用WinSCP步骤
1. 下载并安装WinSCP
2. 新建会话:
   - 协议: SFTP
   - 主机名: 212.64.84.112
   - 端口: 22
   - 用户名: root
   - 密码: 你的服务器密码
3. 连接后,上传以下文件到服务器:
   - `d:\jishe\1.19\qkyd-admin\target\qkyd-admin.jar` -> `/root/qkyd/backend/`
   - `d:\jishe\1.19\application-prod.yml` -> `/root/qkyd/`
   - `d:\jishe\1.19\dist\docker-compose.yml` -> `/root/qkyd/`
4. 上传完成后,使用WinSCP的终端或PuTTY登录服务器,执行方案一的步骤3-6。

## 常用运维命令

### 查看服务状态
```bash
docker-compose ps
```

### 查看日志
```bash
# 实时查看后端日志
docker-compose logs -f backend

# 查看最近100行日志
docker logs --tail 100 qkyd-backend
```

### 重启服务
```bash
# 重启所有服务
docker-compose restart

# 只重启后端
docker restart qkyd-backend
```

### 停止服务
```bash
docker-compose down
```

### 更新代码
```bash
# 1. 上传新的jar包
# 2. 重启容器
docker-compose restart backend
```

### 进入容器
```bash
docker exec -it qkyd-backend bash
```

## 故障排查

### 容器无法启动
```bash
# 查看容器日志
docker logs qkyd-backend

# 检查容器状态
docker ps -a
```

### 端口被占用
```bash
# 查看端口占用
netstat -tlnp

# 停止占用端口的进程
kill -9 <PID>
```

### 数据库连接失败
1. 检查数据库服务是否运行
2. 确认数据库连接配置是否正确
3. 检查防火墙规则
4. 验证数据库用户权限

## 访问地址

部署成功后,可以通过以下地址访问:

- **后端API**: http://212.64.84.112:8080
- **健康检查**: http://212.64.84.112:8080/actuator/health
- **API文档**: http://212.64.84.112:8080/doc.html (如果配置了Swagger)

## 前端部署(可选)

前端项目位于 `RuoYi-Vue3-Modern` 目录下,部署步骤:

### 在本地构建前端
```powershell
cd d:\jishe\1.19\RuoYi-Vue3-Modern
npm install --registry=https://registry.npmmirror.com
npm run build:prod
```

### 上传前端文件
```powershell
scp -r RuoYi-Vue3-Modern\dist root@212.64.84.112:/root/qkyd/frontend/
```

### 使用Nginx部署前端
```bash
docker run -d --name qkyd-frontend \
  -p 80:80 \
  -v /root/qkyd/frontend/dist:/usr/share/nginx/html \
  -v /root/qkyd/docker/nginx.conf:/etc/nginx/conf.d/default.conf:ro \
  nginx:1.25-alpine
```

## 下一步

1. **配置数据库**: 根据你的数据库配置修改application-prod.yml
2. **导入数据**: 导入项目中的SQL文件到数据库
3. **启动后端**: 执行docker-compose up -d
4. **测试访问**: 访问http://212.64.84.112:8080/actuator/health验证
5. **部署前端**: 按照前端部署步骤部署前端(可选)

## 注意事项

1. 确保服务器防火墙已开放8080和80端口
2. 修改application-prod.yml中的数据库连接信息
3. 首次部署需要导入数据库数据
4. 生产环境建议修改默认密码和密钥
5. 定期备份重要数据

## 技术支持

如果遇到问题,请提供以下信息:
- 容器状态: `docker-compose ps`
- 错误日志: `docker logs qkyd-backend`
- 系统版本: `cat /etc/os-release`
- Docker版本: `docker --version`

---

**最后更新**: 2026-01-29
**版本**: v1.0
