. $PSScriptRoot\commons.ps1
. $PSScriptRoot\serialization.ps1

$problemFiles = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Problems" -File
$solutionDir = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Solutions\eh-dvrp" -File
$csvOutputPath = "${dataRoot}\heuristic_usage.tsv"

$orderingHeuristicIds = @()
$constructiveHeuristicIds = @()
$repairingHeuristicIds = @()

$data = @()

function GetDataRows {
    param (
        $ProblemId,
        $RunNo,
        $Vrp,
        $Hus,
        $HeuristicType
    )

    $rows = @()
    $heuristicUsages = $Hus | Select-Object id, @{Name = 'usage count'; Expression = '#text' }

    foreach ($hu in $heuristicUsages) {
        $dataItem = @{
            id = $ProblemId
            "matrix based distance" = $null -ne $Vrp.DistanceMatrix
            "asymmetric transport" = $Vrp.TransportAsymmetry
            "time windows" = $Vrp.TimeWindows
            "run no" = $RunNo
            "heuristic id" = $hu.id
            "heuristic type" = $HeuristicType
            "usage count" = [int]::Parse($hu.'usage count')
        }
        $rows += [PSCustomObject]$dataItem
    }
    
    return $rows

}

foreach ($problemFile in $problemFiles) {
    $vrp = Read-ProblemFile -FilePath $problemFile.FullName
    $problemId = $problemFile.BaseName
    $solutionFiles = Get-ChildItem $solutionDir.FullName | Where-Object { $_.BaseName.StartsWith($problemId) } | Sort-Object
        
    foreach ($solutionFile in $solutionFiles) {
        $xmlDoc = New-Object System.Xml.XmlDocument
        $xmlDoc.Load($solutionFile.FullName)

        $solutionFile.BaseName -match "r(\d)+$" | Out-Null
        $runNo = [int]::Parse($matches[1])

        $orderingHus = $xmlDoc.solution.heuristicUsages.orderingHeuristicUsages.hu
        $data += GetDataRows $problemId $runNo $vrp $orderingHus "ordering"

        $constructiveHus = $xmlDoc.solution.heuristicUsages.constructiveHeuristicUsages.hu
        $data += GetDataRows $problemId $runNo $vrp $constructiveHus "constructive"

        $repairingHus = $xmlDoc.solution.heuristicUsages.repairingHeuristicUsages.hu
        $data += GetDataRows $problemId $runNo $vrp $repairingHus "repairing"
    }
}

$keys = "id", "matrix based distance", "asymmetric transport", "time windows", "run no", "heuristic id", "heuristic type", "usage count"
$data | Select-Object $keys | Export-Csv -Delimiter "`t" -Path $csvOutputPath -NoTypeInformation 