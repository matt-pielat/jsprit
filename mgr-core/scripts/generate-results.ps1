$scriptPath = ".\Start-Runner.ps1"

$diagnosticMode = $false

function Run-Benchmark
{
    param(
        [string]$Directory, 
        [string]$ProblemFormat,
        [long]$TimeLimit,
        [int]$RunsPerProblem,
        [long]$MinIntermediateCostDelay
    )

    $problemsDir = "$Directory\Problems"
    $solutionsDir = "$Directory\Solutions"
    $logDir = "$Directory\Logs"

    $serializableDate = (Get-Date).ToString('yyMMdd-hhmmss');
    $logFilePath = "${logDir}\${serializableDate}.log"

    $problemFiles = Get-ChildItem $problemsDir
    foreach ($problemFile in $problemFiles)
    {
        for ($i = 0; $i -lt $RunsPerProblem; $i++)
        {
            $problemId = $problemFile | Select-Object -ExpandProperty BaseName
            $problemFilePath = $problemFile | Select-Object -ExpandProperty FullName

            $solutionFilePath = "$solutionsDir\jsprit\${problemId}_r${i}.sol"
            if (-not (Test-Path -Path $solutionFilePath))
            {
                & "$scriptPath" `
                    -ProblemPath "$problemFilePath" `
                    -LogPath "$logFilePath" `
                    -SolutionPath "$solutionFilePath" `
                    -ProblemFormat $ProblemFormat `
                    -Algorithm jsprit `
                    -TimeLimit $TimeLimit `
                    -MinIntermediateCostDelay $MinIntermediateCostDelay `
            }

            if ($diagnosticMode)
            {
                $diagnosticLogFilePath = "${logDir}\diagnostic_${problemId}_${serializableDate}.log"
            }
    
            $solutionFilePath = "$solutionsDir\GarridoRiff\${problemId}_r${i}.sol"
            if (-not (Test-Path -Path $solutionFilePath))
            {
                & "$scriptPath" `
                    -ProblemPath "$problemFilePath" `
                    -LogPath "$logFilePath" `
                    -DiagnosticLogPath "$diagnosticLogFilePath" `
                    -SolutionPath "$solutionFilePath" `
                    -ProblemFormat $ProblemFormat `
                    -Algorithm GarridoRiff `
                    -TimeLimit $TimeLimit `
                    -MinIntermediateCostDelay $MinIntermediateCostDelay `
            }
        }
    }
}

# Run-Benchmark -Directory "D:\Google Drive\Magisterka\data\Uchoa et al. (2014)" -ProblemFormat Tsplib95 -TimeLimit 30000 -RunsPerProblem 10
# Run-Benchmark -Directory "D:\Google Drive\Magisterka\data\VrpTestCasesGenerator" -ProblemFormat Tsplib95 -TimeLimit 30000 -RunsPerProblem 10
# Run-Benchmark -Directory "D:\Google Drive\Magisterka\data\Set E (Christofides and Eilon, 1969)" -ProblemFormat Tsplib95 -TimeLimit 30000 -RunsPerProblem 10
# Run-Benchmark -Directory "D:\Google Drive\Magisterka\data\Solomon" -ProblemFormat Solomon -TimeLimit 30000 -RunsPerProblem 10

Run-Benchmark -Directory "D:\Google Drive\Magisterka\data\Convergence" -ProblemFormat Solomon -TimeLimit 600000 -RunsPerProblem 3 -MinIntermediateCostDelay 500