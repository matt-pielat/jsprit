. $PSScriptRoot\commons.ps1
. $PSScriptRoot\serialization.ps1

$dataSetDir = "${dataRoot}\data sets"
$inputTsv = "${dataRoot}\main aggregated.tsv"
$chartDir = "${dataRoot}\bar charts"
$scriptPath = $PSScriptRoot + "\bar_chart.py"

function CreateBarChart {
    param (
        $ProblemFiles,
        $MinN,
        $MaxN,
        $OutputPath
    )

    $problems = $ProblemFiles

    if ($MinN -or $MaxN) {
        $problems = $problems | 
            Select-Object *, @{Name = "temp"; Expression = { $_.Name -match "[Nn](\d+)\D*[Kk](\d+)"; [int]::Parse($matches[1]); [int]::Parse($matches[2]) }} |
            Select-Object *, @{Name = "N"; Expression = { $_.temp[1] }}, @{Name = "K"; Expression = { $_.temp[2] }} -ExcludeProperty "temp"

        if ($MinN) {
            $problems = $problems | Where-Object { $_.N -ge $MinN }
        }
        if ($MaxN) {
            $problems = $problems | Where-Object { $_.N -lt $MaxN }
        }
    }

    $problemIds = $problems | Sort-Object -Property N, K, BaseName | Select-Object -ExpandProperty BaseName

    $params = @($scriptPath, $inputTsv, $OutputPath) + $problemIds
    & python.exe  @params
}

$cvrpFiles = (Get-ChildItem "${dataSetDir}\Set E (Christofides and Eilon, 1969)\Problems") + (Get-ChildItem "${dataSetDir}\Uchoa et al. (2014)\Problems")
$acvrpFiles = Get-ChildItem "${dataSetDir}\VrpTestCasesGenerator\Problems"
$vrptwFiles = Get-ChildItem "${dataSetDir}\Solomon\Problems"

CreateBarChart -ProblemFiles $cvrpFiles -OutputPath "${chartDir}\cvrp1.png" -MaxN 170
CreateBarChart -ProblemFiles $cvrpFiles -OutputPath "${chartDir}\cvrp2.png" -MinN 170 -MaxN 300
CreateBarChart -ProblemFiles $cvrpFiles -OutputPath "${chartDir}\cvrpbig1.png" -MinN 300 -MaxN 540
CreateBarChart -ProblemFiles $cvrpFiles -OutputPath "${chartDir}\cvrpbig2.png" -MinN 540
CreateBarChart -ProblemFiles $acvrpFiles -OutputPath "${chartDir}\map.png"
CreateBarChart -ProblemFiles $vrptwFiles -OutputPath "${chartDir}\vrptw.png"