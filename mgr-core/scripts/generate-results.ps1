$scriptPath = ".\Start-Runner.ps1"

$timeLimit = 30000
$runsPerProblem = 10

function Run-Benchmark
{
    param(
        [string]$Directory, 
        [string]$ProblemFormat
    )

    $problemsDir = "$Directory\Problems"
    $solutionsDir = "$Directory\Solutions"
    $logDir = "$Directory\Logs"

    $serializableDate = (Get-Date).ToString('yyMMdd-hhmmss');
    $logFilePath = "${logDir}\${serializableDate}.log"

    $problemFiles = Get-ChildItem $problemsDir
    foreach ($problemFile in $problemFiles)
    {
        for ($i = 0; $i -lt $runsPerProblem; $i++)
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
                    -TimeLimit $timeLimit `
            }
    
            $solutionFilePath = "$solutionsDir\GarridoRiff\${problemId}_r${i}.sol"
            if (-not (Test-Path -Path $solutionFilePath))
            {
                & "$scriptPath" `
                    -ProblemPath "$problemFilePath" `
                    -LogPath "$logFilePath" `
                    -SolutionPath "$solutionFilePath" `
                    -ProblemFormat $ProblemFormat `
                    -Algorithm GarridoRiff `
                    -TimeLimit $timeLimit `
            }
        }
    }
}

# Run-Benchmark -Directory "Set E (Christofides and Eilon, 1969)" -ProblemFormat Tsplib95
# Run-Benchmark -Directory "Uchoa et al. (2014)" -ProblemFormat Tsplib95
# Run-Benchmark -Directory "VrpTestCasesGenerator" -ProblemFormat Tsplib95
# Run-Benchmark -Directory "Solomon" -ProblemFormat Solomon