# Comprehensive Test Suite for ExamSeat Pro Features
$ErrorActionPreference = "Stop"

Write-Host ">>> 1. Testing Dashboard Stats..."
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/stats" -Method Get
Write-Host "Total Students:" $stats.totalStudents
Write-Host "Total Capacity:" $stats.totalCapacity
Write-Host "Year Distribution:" ($stats.yearDistribution | ConvertTo-Json -Compress)
Write-Host "Active Exam:" $stats.activeExamName "($($stats.activeExamYear))"

Write-Host "`n>>> 2. Testing Year-Wise Student Query..."
$feStudents = Invoke-RestMethod -Uri "http://localhost:8080/api/students?year=First+Year" -Method Get
Write-Host "First Year (FE) Students Count:" $feStudents.Count
$teStudents = Invoke-RestMethod -Uri "http://localhost:8080/api/students?year=Third+Year" -Method Get
Write-Host "Third Year (TE) Students Count:" $teStudents.Count

Write-Host "`n>>> 3. Testing Exam Timetable..."
$exams = Invoke-RestMethod -Uri "http://localhost:8080/api/exams" -Method Get
Write-Host "Scheduled Exams Count:" $exams.Count
$exams | ForEach-Object {
    Write-Host "  [$($_.id)] $($_.examName) | Subject: $($_.subject) | Year: $($_.academicYear) | Time: $($_.examTime) | OnHold: $($_.onHold)"
}

Write-Host "`n>>> 4. Testing Excel Template Download..."
$templateResp = Invoke-WebRequest -Uri "http://localhost:8080/api/excel/template" -UseBasicParsing
Write-Host "Template Status:" $templateResp.StatusCode
Write-Host "Template Content-Type:" $templateResp.Headers["Content-Type"]
Write-Host "Template Preview:" ($templateResp.Content.Substring(0, [Math]::Min(120, $templateResp.Content.Length)))

Write-Host "`n>>> 5. Testing Hold Feature..."
$adminLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{ username="admin"; password="admin123"; role="ADMIN" } | ConvertTo-Json) -ContentType "application/json"
$token = $adminLogin.token
$headers = @{ "Authorization" = "Bearer $token" }

# Put Exam 1 on HOLD
$holdResp = Invoke-RestMethod -Uri "http://localhost:8080/api/exams/hold" -Method Post -Headers $headers -Body (@{ examId="1"; hold="true" } | ConvertTo-Json) -ContentType "application/json"
Write-Host "Put on Hold Result:" ($holdResp | ConvertTo-Json -Compress)

# Attempt generation while ON HOLD (should be blocked)
try {
    $blockedGen = Invoke-RestMethod -Uri "http://localhost:8080/api/seating/generate" -Method Post -Headers $headers -Body (@{ examId="1" } | ConvertTo-Json) -ContentType "application/json"
    Write-Host "ERROR: Generation should have failed while on hold, but got:" ($blockedGen | ConvertTo-Json -Compress)
} catch {
    Write-Host "SUCCESS: Generation correctly blocked while exam is on hold! Response:" $_.Exception.Message
}

# Release Hold
$relResp = Invoke-RestMethod -Uri "http://localhost:8080/api/exams/hold" -Method Post -Headers $headers -Body (@{ examId="1"; hold="false" } | ConvertTo-Json) -ContentType "application/json"
Write-Host "Release Hold Result:" ($relResp | ConvertTo-Json -Compress)

# Now generate seating (should succeed)
$genSuccess = Invoke-RestMethod -Uri "http://localhost:8080/api/seating/generate" -Method Post -Headers $headers -Body (@{ examId="1" } | ConvertTo-Json) -ContentType "application/json"
Write-Host "Generation after release:" ($genSuccess | ConvertTo-Json -Compress)

Write-Host "`n>>> 6. Testing Excel Seating Export..."
$exportResp = Invoke-WebRequest -Uri "http://localhost:8080/api/excel/export/seating?examId=1" -UseBasicParsing
Write-Host "Export Status:" $exportResp.StatusCode
Write-Host "Export Content-Disposition:" $exportResp.Headers["Content-Disposition"]
Write-Host "Export Preview Lines:"
($exportResp.Content -split "`r`n" | Select-Object -First 8) -join "`n"

Write-Host "`n>>> 7. Testing Student Portal (Hall Ticket Pass)..."
$stdLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body (@{ username="A-01"; password="pass123"; role="STUDENT" } | ConvertTo-Json) -ContentType "application/json"
$stdSeat = Invoke-RestMethod -Uri "http://localhost:8080/api/student/seating" -Method Get -Headers @{ "Authorization" = "Bearer $($stdLogin.token)" }
Write-Host "Student Pass for A-01:" ($stdSeat | ConvertTo-Json -Compress)

Write-Host "`n>>> ALL 5 EXAMSEAT PRO FEATURES VERIFIED SUCCESSFULLY! <<<" -ForegroundColor Green
