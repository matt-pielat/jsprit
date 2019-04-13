. $PSScriptRoot\commons.ps1

$tsvDir = "${dataRoot}\convergence tsvs"
$plotDir = "${dataRoot}\convergence plots"
$scriptPath = $PSScriptRoot + "\convergence_plot.py"

$problemIds = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Problems" -File | Select-Object -ExpandProperty BaseName
$solutionTypes = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Solutions" -Directory | Select-Object -ExpandProperty BaseName

$labels = @{
    "eh-dvrp" = "EH-DVRP"
    "jsprit" = "jSprit"
}

foreach ($problemId in $problemIds) {
    $inputParams = @()

    foreach ($solutionType in $solutionTypes) {
        $label = $labels[$solutionType]
        if (-not $label) {
            Write-Warning "Label not found for type $solutionType"
            $label = "???"
        }

        $inputParams += $label
        $inputParams += "${tsvDir}\${solutionType}_${problemId}.tsv"
    }

    $outputPath = "${plotDir}\${problemId}.png"

    $params = @($scriptPath, $outputPath) + $inputParams
    & python.exe  @params
}