@echo off
setlocal

echo ========================================================
echo Database Migration Tool (ueit_jkpt -^> qkyd_jkpt)
echo ========================================================
echo.

set DB_USER=root
set DB_PASS=123456
set OLD_DB=ueit_jkpt
set NEW_DB=qkyd_jkpt
set BACKUP_FILE=ueit_jkpt_backup.sql
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin

echo [STEP 1] Checking if mysqldump and mysql are available...
if not exist "%MYSQL_BIN%\mysqldump.exe" (
    echo Error: mysqldump.exe not found at "%MYSQL_BIN%"
    pause
    exit /b 1
)

echo [STEP 2] Backing up old database (%OLD_DB%)...
"%MYSQL_BIN%\mysqldump" -u%DB_USER% -p%DB_PASS% --routines --events --triggers %OLD_DB% > %BACKUP_FILE%
if %errorlevel% neq 0 (
    echo Error: Backup failed. Check if database exists and password is correct.
    pause
    exit /b 1
)
echo Backup successful: %BACKUP_FILE%

echo [STEP 3] Creating new database (%NEW_DB%)...
"%MYSQL_BIN%\mysql" -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %NEW_DB% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if %errorlevel% neq 0 (
    echo Error: Failed to create new database.
    pause
    exit /b 1
)
echo Database created.

echo [STEP 4] Importing data into new database...
"%MYSQL_BIN%\mysql" -u%DB_USER% -p%DB_PASS% %NEW_DB% < %BACKUP_FILE%
if %errorlevel% neq 0 (
    echo Error: Import failed.
    pause
    exit /b 1
)
echo Import successful.

echo.
echo ========================================================
echo Migration Complete! 
echo Please verify that the backend starts successfully.
echo ========================================================
pause
