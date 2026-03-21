# Server Sync

Example (systemd):

```powershell
powershell -ExecutionPolicy Bypass -File .\backend\scripts\sync_server.ps1 `
  -ServerHost 1.2.3.4 -ServerUser root -ServerPort 22 `
  -RemoteAppDir /opt/qkyd -RestartMode systemd -ServiceName qkyd-admin
```

Example (docker compose + seed):

```powershell
powershell -ExecutionPolicy Bypass -File .\backend\scripts\sync_server.ps1 `
  -ServerHost 1.2.3.4 -ServerUser root -ServerPort 22 `
  -RemoteAppDir /opt/qkyd -RestartMode docker -ServiceName qkyd-admin -SeedDatabase
```

Notes:
- `backend/application-prod.yml` uses environment variables for DB/Redis/Token/OpenAI.
- Set server env vars (`MYSQL_*`, `REDIS_*`, `TOKEN_SECRET`, etc.) before restart.
