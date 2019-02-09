$scriptPath = ".\Start-Runner.ps1"

$iterationsPerRun = 2000000000
$timePerRunInMs = 30000
$runsPerProblem = 10

# $iterationsPerRun = 2000
# $timePerRunInMs = 999999999
# $runsPerProblem = 10

function Run-Benchmark
{
    param([string]$Directory, [bool]$TimeWindows)

    $problemsDir = ".\$Directory\Problems"

    $solutionsDir = ".\$Directory\Solutions"
    $jspritSolutionsDir = "$solutionsDir\jsprit"
    $garridoRiffSolutionsDir = "$solutionsDir\Pielat"

    $logDir = ".\$Directory\Logs"

    & "$scriptPath" `
        -InputDir "$problemsDir" `
        -TimeWindows $TimeWindows `
        -JspritOutDir "$jspritSolutionsDir" `
        -GarridoRiffOutDir "$garridoRiffSolutionsDir" `
        -LogDir "$logDir" `
        -TimePerRun $timePerRunInMs `
        -IterationsPerRun $iterationsPerRun `
        -RunsPerProblem $runsPerProblem
}

# Run-Benchmark -Directory "Set E (Christofides and Eilon, 1969)" -TimeWindows $false
# Run-Benchmark -Directory "Uchoa et al. (2014)" -TimeWindows $false
Run-Benchmark -Directory "VrpTestCasesGenerator" -TimeWindows $false
Run-Benchmark -Directory "Solomon" -TimeWindows $true