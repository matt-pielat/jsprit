param(
    [string]$InputDir,
    [bool]$TimeWindows,
    [string]$JspritOutDir,
    [string]$GarridoRiffOutDir,
    [string]$LogDir,
    [long]$TimePerRun = 999999999,
    [int]$IterationsPerRun = 2000000000,
    [int]$RunsPerProblem = 10,
    [int]$PopulationSize = 10,
    [int]$OffspringSize = 5,
    [int]$ChromosomeSize = 5
)

"InputDir: " + $InputDir
"TimeWindows: " + $TimeWindows
"JspritOutDir: " + $JspritOutDir
"GarridoRiffOutDir: " + $GarridoRiffOutDir
"LogDir: " + $LogDir
"TimePerRun: " + $TimePerRun
"IterationsPerRun: " + $IterationsPerRun
"RunsPerProblem: " + $RunsPerProblem
"PopulationSize: " + $PopulationSize
"OffspringSize: " + $OffspringSize
"ChromosomeSize: " + $ChromosomeSize

$jarPath = "$PSScriptRoot\..\output\mgr-benchmark.jar"

if ($TimeWindows) { $timeWindowsArg = "--timeWindows" }
if ($JspritOutDir) { $jspritOutDirArg = "--jspritOutDir" }
if ($GarridoRiffOutDir) { $garridoRiffOutDirArg = "--garridoRiffOutDir" }

java -jar "$jarPath" `
    --inputDir "$InputDir" `
    $timeWindowsArg `
    $jspritOutDirArg "$JspritOutDir" `
    $garridoRiffOutDirArg "$GarridoRiffOutDir" `
    --logDir "$LogDir" `
    --timePerRun $TimePerRun `
    --iterationsPerRun $IterationsPerRun `
    --runsPerProblem $RunsPerProblem `
    --populationSize $PopulationSize `
    --offspringSize $OffspringSize `
    --chromosomeSize $ChromosomeSize