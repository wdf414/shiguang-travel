param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminPhone = "13800138000",
    [string]$AdminPassword = "123456"
)

$ErrorActionPreference = "Stop"
$script:passed = 0
$script:failed = 0

function Invoke-JsonPost {
    param(
        [string]$Url,
        [object]$Body,
        [string]$Token = ""
    )
    $json = $Body | ConvertTo-Json -Depth 8 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $headers = @{ "Content-Type" = "application/json; charset=utf-8" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    return Invoke-RestMethod -Uri $Url -Method Post -Headers $headers -Body $bytes -TimeoutSec 120
}

function Check {
    param([string]$Name, [scriptblock]$Action)
    try {
        & $Action
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "[FAIL] $Name -> $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

Write-Host "Smoke test: $BaseUrl" -ForegroundColor Cyan

Check "AI health" {
    $health = Invoke-RestMethod -Uri "$BaseUrl/api/ai/health" -TimeoutSec 15
    if ($health.code -ne 200) { throw "health code=$($health.code)" }
    if (-not $health.data.enabled) { throw "AI disabled" }
    Write-Host "       configured=$($health.data.configured), model=$($health.data.model)"
}

Check "Normal user login" {
    $login = Invoke-JsonPost "$BaseUrl/api/user/login" @{ phone = "13800138001"; password = "123456" }
    if ($login.code -ne 200 -or -not $login.data.token) { throw "login failed: $($login.msg)" }
}

$token = $null
Check "Admin login" {
    $login = Invoke-JsonPost "$BaseUrl/api/user/login" @{ phone = $AdminPhone; password = $AdminPassword }
    if ($login.code -ne 200 -or $login.data.user.role -ne "ADMIN") { throw "admin login failed" }
    $script:token = $login.data.token
    Write-Host "       admin id=$($login.data.user.id)"
}

Check "AI plan" {
    $plan = Invoke-JsonPost "$BaseUrl/api/ai/plan" @{
        destination = "三亚"
        days = 3
        budget = 2500
        departureCity = ""
        travelStyle = "学生轻旅行"
        preferences = @("海岛", "美食")
        companions = 2
    }
    if ($plan.code -ne 200) { throw "plan code=$($plan.code), msg=$($plan.msg)" }
    if (-not $plan.data.itinerary -or $plan.data.itinerary.Count -lt 1) { throw "empty itinerary" }
    Write-Host "       days=$($plan.data.days), fallback=$($plan.data.meta.fallback), grounded=$($plan.data.meta.groundedProducts)"
}

Check "AI chat" {
    $chat = Invoke-JsonPost "$BaseUrl/api/ai/chat" @{
        message = "三亚三天怎么玩？"
        history = @()
        sessionId = "smoke-test"
    }
    if ($chat.code -ne 200 -or [string]::IsNullOrWhiteSpace($chat.data.reply)) { throw "empty reply" }
    Write-Host "       references=$($chat.data.references.Count)"
}

Check "AI moderation (admin)" {
    if (-not $script:token) { throw "no admin token" }
    $mod = Invoke-JsonPost "$BaseUrl/api/ai/moderate" @{
        contentType = "travel_note"
        title = "三亚攻略"
        content = "加我微信 abc123，低价订酒店，返利 50 元。"
    } $script:token
    if ($mod.code -ne 200 -or -not $mod.data.riskLevel) { throw "moderation failed" }
    Write-Host "       risk=$($mod.data.riskLevel), suggestion=$($mod.data.suggestion)"
}

Check "Moderation requires admin" {
    try {
        $null = Invoke-JsonPost "$BaseUrl/api/ai/moderate" @{
            contentType = "travel_note"
            title = "x"
            content = "x"
        }
        throw "expected 401/403"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -notin @(401, 403)) {
            throw "unexpected status: $($_.Exception.Response.StatusCode.value__)"
        }
    }
}

Write-Host ""
Write-Host "Result: $script:passed passed, $script:failed failed" -ForegroundColor $(if ($script:failed -eq 0) { "Green" } else { "Red" })
if ($script:failed -gt 0) { exit 1 }

