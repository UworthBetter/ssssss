@echo off
setlocal

echo ========================================================
echo Database Table Renaming Tool (ueit_ -^> qkyd_)
echo ========================================================
echo.

set DB_USER=root
set DB_PASS=123456
set DB_NAME=qkyd_jkpt
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin

echo [STEP 1] Renaming tables in %DB_NAME%...

"%MYSQL_BIN%\mysql" -u%DB_USER% -p%DB_PASS% %DB_NAME% -e "RENAME TABLE ueit_blood TO qkyd_blood; RENAME TABLE ueit_device_info TO qkyd_device_info; RENAME TABLE ueit_device_info_extend TO qkyd_device_info_extend; RENAME TABLE ueit_exception TO qkyd_exception; RENAME TABLE ueit_fence TO qkyd_fence; RENAME TABLE ueit_heart_rate TO qkyd_heart_rate; RENAME TABLE ueit_location TO qkyd_location; RENAME TABLE ueit_spo2 TO qkyd_spo2; RENAME TABLE ueit_steps TO qkyd_steps; RENAME TABLE ueit_temp TO qkyd_temp;"

if %errorlevel% neq 0 (
    echo Error: Failed to rename tables.
    pause
    exit /b 1
)

echo Table renaming successful.
echo.
echo ========================================================
echo Table Renaming Complete!
echo ========================================================
pause
