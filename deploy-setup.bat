@echo off
chcp 65001 >nul
echo ========================================
echo    第一步: 接受服务器主机密钥
echo ========================================
echo.
echo 服务器IP: 212.64.84.112
echo 用户名: root
echo 密码: M)D4k_3udwVN?
echo.
echo 下一步运行 plink 命令连接服务器
echo 首次连接时需要确认主机密钥
echo 请输入 "y" 继续
echo.
pause

plink root@212.64.84.112
