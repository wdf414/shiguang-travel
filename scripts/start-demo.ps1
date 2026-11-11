param(
    [switch]$NoPrompt
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

function Step($message) {
    Write-Host ""
    Write-Host "[INFO] $message" -ForegroundColor Cyan
}

function Ok($message) {
    Write-Host "[OK]   $message" -ForegroundColor Green
}

function Warn($message) {
    Write-Host "[WARN] $message" -ForegroundColor Yellow
}

function Fail($message) {
    Write-Host ""
    Write-Host "[ERROR] $message" -ForegroundColor Red
    if (-not $NoPrompt) {
        Read-Host "按 Enter 退出"
    }
    exit 1
}

function Find-Npm {
    $cmd = Get-Command npm.cmd -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $candidates = @(
        "$env:ProgramFiles\nodejs\npm.cmd",
        "$env:LOCALAPPDATA\Programs\nodejs\npm.cmd"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) { return $candidate }
    }

    $wingetRoot = Join-Path $env:LOCALAPPDATA "Microsoft\WinGet\Packages"
    $nodePackage = Get-ChildItem $wingetRoot -Directory -Filter "OpenJS.NodeJS.LTS_*" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($nodePackage) {
        $found = Get-ChildItem $nodePackage.FullName -Recurse -Filter "npm.cmd" -ErrorAction SilentlyContinue |
            Select-Object -First 1
        if ($found) { return $found.FullName }
    }
    return $null
}

function Wait-Url($url, $seconds) {
    for ($i = 0; $i -lt $seconds; $i++) {
        Start-Sleep -Seconds 1
        try {
            $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return $true
            }
        } catch {
            # 服务还没起来，继续等待
        }
    }
    return $false
}

# ------------------------------------------------------------
# 1. 检查 .env
# ------------------------------------------------------------
$envFile = Join-Path $Root "travel-backend\.env"
$envExample = Join-Path $Root "travel-backend\.env.example"

if (-not (Test-Path $envFile)) {
    Copy-Item $envExample $envFile
    Warn "首次启动：已生成 travel-backend\.env"
    Warn "请填写 DB_PASSWORD、JWT_SECRET、AI_API_KEY，保存后重新双击本脚本。"
    Start-Process notepad.exe $envFile
    if (-not $NoPrompt) {
        Read-Host "填好后按 Enter 退出，然后重新双击启动"
    }
    exit 0
}

$envMap = @{}
Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    if ($_ -match '^\s*([^#=]+?)\s*=\s*(.*)$') {
        $envMap[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$dbPass = $envMap["DB_PASSWORD"]
if ([string]::IsNullOrWhiteSpace($dbPass) -or $dbPass -match '请填写|你的MySQL密码') {
    Warn "DB_PASSWORD 还没有填写。"
    Start-Process notepad.exe $envFile
    if (-not $NoPrompt) {
        Read-Host "填好后按 Enter 退出，然后重新双击启动"
    }
    exit 0
}

$jwt = $envMap["JWT_SECRET"]
if ([string]::IsNullOrWhiteSpace($jwt) -or $jwt -match '请替换|至少32') {
    $bytes = New-Object byte[] 48
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $newJwt = [Convert]::ToBase64String($bytes)
    $content = Get-Content $envFile -Raw -Encoding UTF8
    $content = [regex]::Replace($content, '(?m)^JWT_SECRET\s*=.*$', "JWT_SECRET=$newJwt")
    Set-Content -Path $envFile -Value $content -Encoding UTF8
    Ok "已自动生成 JWT_SECRET"
}

$aiKey = $envMap["AI_API_KEY"]
if ([string]::IsNullOrWhiteSpace($aiKey) -or $aiKey -match '请填写|your-deepseek') {
    Warn "未配置 AI_API_KEY，AI 将使用本地兜底；不影响网页和业务功能。"
}

$npmExe = Find-Npm
if (-not $npmExe) {
    Fail "未找到 npm。请安装 Node.js 18+，或重新打开终端后再试。"
}
$nodeDir = Split-Path -Parent $npmExe

# ------------------------------------------------------------
# 2. 检查 MySQL 与数据库
# ------------------------------------------------------------
Step "检查 MySQL..."
$mysqlService = Get-Service -Name mysql -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -ne "Running") {
    Warn "MySQL 服务未启动，尝试启动..."
    try {
        Start-Service mysql -ErrorAction Stop
        Ok "MySQL 服务已启动"
    } catch {
        Warn "无法自动启动 MySQL，请手动启动服务 mysql"
    }
}

$mysqlHost = "127.0.0.1"
$mysqlPort = 3306
$dbName = "lvyou"
$dbUser = "root"
if ($envMap["DB_USERNAME"]) { $dbUser = $envMap["DB_USERNAME"] }
if ($envMap["DB_URL"] -match 'jdbc:mysql://([^:/]+)(?::(\d+))?/([^?]+)') {
    $mysqlHost = $matches[1]
    if ($matches[2]) { $mysqlPort = [int]$matches[2] }
    $dbName = $matches[3]
}

$mysqlExe = $null
$mysqlCommand = Get-Command mysql.exe -ErrorAction SilentlyContinue
if ($mysqlCommand) {
    $mysqlExe = $mysqlCommand.Source
} else {
    $candidates = @(
        "E:\mysql-8.0.44-winx64\mysql-8.0.44-winx64\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            $mysqlExe = $candidate
            break
        }
    }
}

if ($mysqlExe) {
    $env:MYSQL_PWD = $dbPass
    try {
        $dbExists = & $mysqlExe "--host=$mysqlHost" "--port=$mysqlPort" "--user=$dbUser" "--silent" "--skip-column-names" `
            "-e" "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$dbName';" 2>$null
    } catch {
        $dbExists = $null
    }
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue

    if (-not $dbExists) {
        Warn "未检测到数据库 $dbName。"
        if (-not $NoPrompt) {
            $answer = Read-Host "是否现在导入 database\lvyou_full.sql？这会重建 $dbName 数据库 (Y/N)"
        } else {
            $answer = "N"
        }
        if ($answer -eq "Y" -or $answer -eq "y") {
            $sql = Join-Path $Root "database\lvyou_full.sql"
            $env:MYSQL_PWD = $dbPass
            $importCommand = "`"$mysqlExe`" --host=$mysqlHost --port=$mysqlPort --user=$dbUser --default-character-set=utf8mb4 < `"$sql`""
            cmd /c $importCommand
            $importCode = $LASTEXITCODE
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
            if ($importCode -ne 0) {
                Fail "数据库导入失败，请检查 MySQL 密码、服务状态和 root 权限。"
            }
            Ok "数据库导入完成"
        } else {
            Warn "跳过数据库导入。若数据库不存在，后端启动后查询数据会失败。"
        }
    } else {
        Ok "数据库 $dbName 已存在"
    }
} else {
    Warn "未找到 mysql.exe，跳过数据库检查。请确认已导入 $dbName 数据库。"
}

# ------------------------------------------------------------
# 3. 检查并启动后端
# ------------------------------------------------------------
$backendHealth = "http://127.0.0.1:8080/api/ai/health"
$backendRunning = $false
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    try {
        $health = Invoke-RestMethod -Uri $backendHealth -TimeoutSec 2
        if ($health.code -eq 200) { $backendRunning = $true }
    } catch {
        $backendRunning = $false
    }
    if (-not $backendRunning) {
        Fail "8080 端口被其他程序占用，请关闭占用程序后重试。"
    }
}

$jar = Join-Path $Root "travel-backend\target\travel-backend-1.0.0.jar"
if (-not $backendRunning -and -not (Test-Path $jar)) {
    Step "未找到后端 jar，正在构建（首次可能需要几分钟）..."
    Push-Location (Join-Path $Root "travel-backend")
    & .\mvnw.cmd -q -DskipTests package
    $buildCode = $LASTEXITCODE
    Pop-Location
    if ($buildCode -ne 0 -or -not (Test-Path $jar)) {
        Fail "后端构建失败，请查看上方日志。"
    }
    Ok "后端构建完成"
}

if (-not $backendRunning) {
    Step "启动后端..."
    $backendCommand = "`$Host.UI.RawUI.WindowTitle='拾光旅行-后端'; Set-Location '$Root\travel-backend'; java -jar target\travel-backend-1.0.0.jar"
    Start-Process powershell.exe -ArgumentList @("-NoExit", "-Command", $backendCommand) | Out-Null
    if (-not (Wait-Url $backendHealth 90)) {
        Fail "后端启动失败。请查看“拾光旅行-后端”窗口的最后几行日志。"
    }
    Ok "后端已启动"
} else {
    Ok "后端已经在运行"
}

# ------------------------------------------------------------
# 4. 检查并启动前端
# ------------------------------------------------------------
$frontendUrl = "http://localhost:3000"
$frontendRunning = $false
if (Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue) {
    try {
        $response = Invoke-WebRequest -Uri $frontendUrl -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) { $frontendRunning = $true }
    } catch {
        $frontendRunning = $false
    }
    if (-not $frontendRunning) {
        Fail "3000 端口被其他程序占用，请关闭占用程序后重试。"
    }
}

if (-not $frontendRunning) {
    if (-not (Test-Path (Join-Path $Root "travel-frontend\node_modules"))) {
        Step "首次启动，正在安装前端依赖..."
        Push-Location (Join-Path $Root "travel-frontend")
        $oldPath = $env:Path
        $env:Path = "$nodeDir;$env:Path"
        & $npmExe install --no-audit --no-fund
        $npmCode = $LASTEXITCODE
        $env:Path = $oldPath
        Pop-Location
        if ($npmCode -ne 0) {
            Fail "前端依赖安装失败，请检查网络后重试。"
        }
        Ok "前端依赖安装完成"
    }

    Step "启动前端..."
    $frontendCommand = "`$Host.UI.RawUI.WindowTitle='拾光旅行-前端'; `$env:Path = '$nodeDir;' + `$env:Path; Set-Location '$Root\travel-frontend'; & '$npmExe' run dev"
    Start-Process powershell.exe -ArgumentList @("-NoExit", "-Command", $frontendCommand) | Out-Null
    if (-not (Wait-Url $frontendUrl 60)) {
        Fail "前端启动失败。请查看“拾光旅行-前端”窗口的日志。"
    }
    Ok "前端已启动"
} else {
    Ok "前端已经在运行"
}

# ------------------------------------------------------------
# 5. 打开浏览器
# ------------------------------------------------------------
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  拾光旅行已启动" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "  前台：    http://localhost:3000"
Write-Host "  后端：    http://localhost:8080/api/ai/health"
Write-Host "  管理员：  13800138000 / 123456"
Write-Host "  普通用户：13800138001 / 123456"
Write-Host ""
Write-Host "  后端和前端窗口请保持打开；关闭本窗口不会停止服务。" -ForegroundColor DarkGray

Start-Process $frontendUrl

if (-not $NoPrompt) {
    Read-Host "按 Enter 关闭本启动窗口"
}

