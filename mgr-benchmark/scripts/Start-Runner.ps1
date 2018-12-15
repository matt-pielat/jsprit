param(
    [string]$InputDir,
    [bool]$TimeWindows,
    [string]$JspritOutputDir,
    [string]$GarridoRiffOutputDir,
    [string]$LogDir,
    [long]$TimeLimitInMs,
    [int]$RunsPerProblem
)

# Example usage:
# .\Start-Runner.ps1 -InputDir "D:\VRP Benchmarks\Set A - Augerat\Problems" -TimeWindows $false -JspritOutputDir "D:\VRP Benchmarks\Set A - Augerat\Solutions jsprit" -GarridoRiffOutputDir "D:\VRP Benchmarks\Set A - Augerat\Solutions algorithm" -LogDir "D:\VRP Benchmarks\Set A - Augerat\Logs" -TimeLimitInMs 5000 -RunsPerProblem 10

$jarPath = "../output/mgr-benchmark.jar"

$timeWindowsArg = ""
if ($TimeWindows)
{
    $timeWindowsArg = "-tw"
}

& java -jar "$jarPath" -i "$InputDir" $timeWindowsArg -j "$JspritOutputDir" -gr "$GarridoRiffOutputDir" -l "$LogDir" -t $TimeLimitInMs -r $RunsPerProblem