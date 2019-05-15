. $PSScriptRoot\commons.ps1
. $PSScriptRoot\serialization.ps1

$scriptPath = $PSScriptRoot + "\vrp_solution_plot.py"
$dataSetDir = "${dataRoot}\data sets"
$rawDataPath = "${dataRoot}\main raw.tsv"
$plotOutputDir = "D:\solution plots"

$allBenchmarks = @(
    # @{ path = "${dataSetDir}\Set E (Christofides and Eilon, 1969)" },
    # @{ path = "${dataSetDir}\Solomon" },
    # @{ path = "${dataSetDir}\Uchoa et al. (2014)" },
    @{ path = "${dataSetDir}\VrpTestCasesGenerator" }
)

$solutionTypes = @(
    "jsprit",
    "eh-dvrp"
)

function Get-NodeString
{
    param(
        [Parameter(ValueFromPipeline)]
        [Node]$Node
    )
    return "$($Node.CoordX) $($Node.CoordY) $($Node.Demand)"
}

function Plot-Solution
{
    param(
        [VrpDefinition]$VrpDefinition,
        [VrpSolution]$VrpSolution,
        [string]$OutputPath
    )

    $graphData = New-TemporaryFile
    $writer = [System.IO.StreamWriter]::new($graphData)
    
    $str = $VrpDefinition.Depot | Get-NodeString
    $writer.WriteLine($str)
    
    foreach ($route in $VrpSolution.Routes) {
        $writer.WriteLine()
        foreach ($nodeId in $route.CustomerIds) {
            $node = $VrpDefinition.CustomersById[$nodeId]
            $writer.WriteLine(($node | Get-NodeString))
        }
    }
    $writer.Close()

    $params = @($scriptPath, $graphData.FullName, $OutputPath)
    & python.exe  @params
}

$rawDataTable = @{}
$rawDataList = Import-Csv -Path $rawDataPath -Delimiter "`t"
$rawDataList | ForEach-Object { $rawDataTable[$_.id] = $_ }

foreach ($benchmark in $allBenchmarks) {
    $benchmarkName = Split-Path -Path $benchmark.path -Leaf
    $problemDirectory = "$($benchmark.path)\Problems"
    $problemFiles = Get-ChildItem $problemDirectory

    foreach ($problemFile in $problemFiles) {
        $problemId = $problemFile.BaseName
        $problemObject = $problemFile.FullName | Read-ProblemFile

        if ([double]::IsNaN($problemObject.Depot.CoordX)) {
            continue
        }

        $dataItem = $rawDataTable[$problemId]

        foreach ($solutionType in $solutionTypes) {
            $bestCost = $dataItem."cost ${solutionType}_r0"
            $bestRunIndex = 0
            for ($i = 1; $i -lt 10; $i += 1) {
                $propertyName = "cost ${solutionType}_r${i}"
                $cost = $dataItem.$propertyName
    
                if (-not $cost) {
                    break
                }
                if ($bestCost -gt $cost) {
                    $bestCost = $cost
                    $bestRunIndex = $i
                }
            }
    
            $solutionPath = "$($benchmark.path)\Solutions\${solutionType}\${problemId}_r${bestRunIndex}.sol"
            $solutionObject = $solutionPath | Read-SolutionFile
            $outputPlotPath = "${plotOutputDir}\${problemId}_${solutionType}.png"
            
            Plot-Solution -VrpDefinition $problemObject -VrpSolution $solutionObject -OutputPath $outputPlotPath
        }
    }
}

# $problemPath = 'D:\Google Drive\Magisterka\data\data sets\VrpTestCasesGenerator\Problems\M31-n323-k28.vrp'
# $solutionPath = 'D:\Google Drive\Magisterka\data\data sets\VrpTestCasesGenerator\Solutions\jsprit\M31-n323-k28_r1.sol'
# $outputPath = 'D:\solution graphs\graph-jsprit.png'

# Plot-Solution -ProblemPath $problemPath -SolutionPath $solutionPath -OutputPath $outputPath