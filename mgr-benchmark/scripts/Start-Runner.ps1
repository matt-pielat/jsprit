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

$timeWindowsArg = ""
if ($TimeWindows) { $timeWindowsArg = "--timeWindows" }

$populationSizeArg = ""
if ($PopulationSize) { $populationSizeArg = "--populationSize $PopulationSize" }

$offspringSizeArg = ""
if ($OffspringSize) { $offspringSizeArg = "--offspringSize $OffspringSize" }

$chromosomeSizeArg = ""
if ($ChromosomeSize) { $chromosomeSizeArg = "--chromosomeSize $ChromosomeSize" }

& java -jar "$jarPath" `
    --inputDir "$InputDir" `
    $timeWindowsArg `
    --jspritOutDir "$JspritOutDir" `
    --garridoRiffOutDir "$GarridoRiffOutDir" `
    --logDir "$LogDir" `
    --timePerRun $TimePerRun `
    --runsPerProblem $RunsPerProblem `
    $populationSizeArg `
    $offspringSizeArg `
    $chromosomeSizeArg