param(
  [Parameter(Mandatory = $true)] [string]$ServerHost,
  [Parameter(Mandatory = $true)] [string]$ServerUser,
  [int]$ServerPort = 22,
  [string]$RemoteAppDir = '/opt/qkyd',
  [ValidateSet('systemd','docker')] [string]$RestartMode = 'systemd',
  [string]$ServiceName = 'qkyd-admin',
  [switch]$SkipBuild,
  [switch]$SeedDatabase
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$adminTarget = Join-Path $root 'qkyd-admin\target'
$jarPath = Join-Path $adminTarget 'qkyd-admin.jar'
$prodConfig = Join-Path $root 'application-prod.yml'
$seedSql = Join-Path $root 'sql\demo_seed_complete.sql'

Write-Host "[1/5] Prepare artifacts..." -ForegroundColor Cyan
if (-not $SkipBuild) {
  Push-Location $root
  try {
    mvn -pl qkyd-admin -am -DskipTests clean package
  }
  finally {
    Pop-Location
  }
}

if (-not (Test-Path $jarPath)) {
  throw "Jar not found: $jarPath"
}
if (-not (Test-Path $prodConfig)) {
  throw "Config not found: $prodConfig"
}

Write-Host "[2/5] Ensure remote directories..." -ForegroundColor Cyan
ssh -p $ServerPort "$ServerUser@$ServerHost" "mkdir -p $RemoteAppDir/config $RemoteAppDir/sql $RemoteAppDir/bin"

Write-Host "[3/5] Upload jar + config..." -ForegroundColor Cyan
scp -P $ServerPort "$jarPath" "$ServerUser@$ServerHost`:$RemoteAppDir/qkyd-admin.jar"
scp -P $ServerPort "$prodConfig" "$ServerUser@$ServerHost`:$RemoteAppDir/config/application.yml"
if (Test-Path $seedSql) {
  scp -P $ServerPort "$seedSql" "$ServerUser@$ServerHost`:$RemoteAppDir/sql/demo_seed_complete.sql"
}

Write-Host "[4/5] Restart service..." -ForegroundColor Cyan
if ($RestartMode -eq 'docker') {
  ssh -p $ServerPort "$ServerUser@$ServerHost" "cd $RemoteAppDir && docker compose up -d --force-recreate $ServiceName"
} else {
  ssh -p $ServerPort "$ServerUser@$ServerHost" "sudo systemctl daemon-reload && sudo systemctl restart $ServiceName && sudo systemctl status $ServiceName --no-pager -n 30"
}

Write-Host "[5/5] Optional DB seed..." -ForegroundColor Cyan
if ($SeedDatabase) {
  $remoteSeed = "$RemoteAppDir/sql/demo_seed_complete.sql"
  $seedCmd = @"
if [ -f '$remoteSeed' ]; then
  mysql -h "\${MYSQL_HOST:-127.0.0.1}" -P "\${MYSQL_PORT:-3306}" -u"\${MYSQL_USER:-root}" -p"\${MYSQL_PASSWORD:-123456}" "\${MYSQL_DB:-qkyd_jkpt}" < '$remoteSeed'
fi
"@
  ssh -p $ServerPort "$ServerUser@$ServerHost" $seedCmd
}

Write-Host "Done. Backend config and service are synced." -ForegroundColor Green
