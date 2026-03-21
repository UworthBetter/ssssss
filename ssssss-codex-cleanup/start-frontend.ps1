$ErrorActionPreference = "Stop"

$nodeDir = "C:\Program Files\nodejs"
if (-not (Test-Path (Join-Path $nodeDir "node.exe"))) {
    throw "Node.js was not found at $nodeDir"
}

$env:Path = "$nodeDir;$env:Path"
$projectDir = Join-Path $PSScriptRoot "qkyd-vue3-new"

Set-Location $projectDir
& npm.cmd run dev
