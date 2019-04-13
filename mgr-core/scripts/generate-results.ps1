. $PSScriptRoot\commons.ps1
. $PSScriptRoot\serialization.ps1

$scriptPath = "${PSScriptRoot}\Start-Runner.ps1"
$diagnosticMode = $false

function Run-Benchmark
{
    param(
        [string]$Directory,
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
        $problemId = $problemFile | Select-Object -ExpandProperty BaseName
        $problemFilePath = $problemFile | Select-Object -ExpandProperty FullName
        $problemFileFormat = $problemFilePath | Get-ProblemFileFormat

        for ($i = 0; $i -lt $RunsPerProblem; $i++)
        {
            $solutionFilePath = "$solutionsDir\jsprit\${problemId}_r${i}.sol"
            if (-not (Test-Path -Path $solutionFilePath))
            {
                & "$scriptPath" `
                    -ProblemPath "$problemFilePath" `
                    -LogPath "$logFilePath" `
                    -SolutionPath "$solutionFilePath" `
                    -ProblemFormat $problemFileFormat `
                    -Algorithm jsprit `
                    -TimeLimit $TimeLimit `
                    -MinIntermediateCostDelay $MinIntermediateCostDelay `
            }

            if ($diagnosticMode)
            {
                $diagnosticLogFilePath = "${logDir}\diagnostic_${problemId}_${serializableDate}.log"
            }
    
            $solutionFilePath = "$solutionsDir\eh-dvrp\${problemId}_r${i}.sol"
            if (-not (Test-Path -Path $solutionFilePath))
            {
                & "$scriptPath" `
                    -ProblemPath "$problemFilePath" `
                    -LogPath "$logFilePath" `
                    -DiagnosticLogPath "$diagnosticLogFilePath" `
                    -SolutionPath "$solutionFilePath" `
                    -ProblemFormat $problemFileFormat `
                    -Algorithm GarridoRiff `
                    -TimeLimit $TimeLimit `
                    -MinIntermediateCostDelay $MinIntermediateCostDelay `
            }
        }
    }
}

# Run-Benchmark -Directory "${dataRoot}\data sets\Uchoa et al. (2014)" -TimeLimit 30000 -RunsPerProblem 10
# Run-Benchmark -Directory "${dataRoot}\data sets\VrpTestCasesGenerator" -TimeLimit 30000 -RunsPerProblem 10
# Run-Benchmark -Directory "${dataRoot}\data sets\Set E (Christofides and Eilon, 1969)" -TimeLimit 30000 -RunsPerProblem 10
# Run-Benchmark -Directory "${dataRoot}\data sets\Solomon" -TimeLimit 30000 -RunsPerProblem 10
Run-Benchmark -Directory "${dataRoot}\data sets\Cherry picked" -TimeLimit 600000 -RunsPerProblem 10 -MinIntermediateCostDelay 500