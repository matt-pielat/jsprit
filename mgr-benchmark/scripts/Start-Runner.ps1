param(
    [string]$InputDir,
    [bool]$TimeWindows,
    [string]$JspritOutDir,
    [string]$GarridoRiffOutDir,
    [string]$LogDir,
    [long]$TimePerRun,
    [int]$RunsPerProblem,
    [int]$PopulationSize,
    [int]$OffspringSize,
    [int]$ChromosomeSize
)

$jarPath = "$PSScriptRoot\..\output\mgr-benchmark.jar"

if ($TimeWindows) { $timeWindowsArg = "--timeWindows" }
if ($JspritOutDir) { $jspritOutDirArg = "--jspritOutDir" }
if ($GarridoRiffOutDir) { $garridoRiffOutDirArg = "--garridoRiffOutDir" }
if ($PopulationSize) { $populationSizeArg = "--populationSize" }
if ($OffspringSize) { $offspringSizeArg = "--offspringSize" }
if ($ChromosomeSize) { $chromosomeSizeArg = "--chromosomeSize" }


java -jar "$jarPath" `
    --inputDir "$InputDir" `
    $timeWindowsArg `
    $jspritOutDirArg "$JspritOutDir" `
    $garridoRiffOutDirArg "$GarridoRiffOutDir" `
    --logDir "$LogDir" `
    --timePerRun $TimePerRun `
    --runsPerProblem $RunsPerProblem `
    $populationSizeArg $PopulationSize `
    $offspringSizeArg $OffspringSize `
    $chromosomeSizeArg $ChromosomeSize