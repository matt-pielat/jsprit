. $PSScriptRoot\commons.ps1
. $PSScriptRoot\serialization.ps1

$problemFiles = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Problems" -File
$solutionDir = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Solutions\eh-dvrp" -File
$csvOutputPath = "${dataRoot}\heuristic_usage.tsv"

$orderingHeuristicIds = @()
$constructiveHeuristicIds = @()
$repairingHeuristicIds = @()

$data = @()

foreach ($problemFile in $problemFiles) {
    $vrp = Read-ProblemFile -FilePath $problemFile.FullName
    $problemId = $problemFile.BaseName
    $solutionFiles = Get-ChildItem $solutionDir.FullName | Where-Object { $_.BaseName.StartsWith($problemId) } | Sort-Object
        
    foreach ($solutionFile in $solutionFiles) {
        $xmlDoc = New-Object System.Xml.XmlDocument
        $xmlDoc.Load($solutionFile.FullName)

        $orderingHeuristicIds += ($xmlDoc.solution.heuristicUsages.orderingHeuristicUsages.hu | Select-Object -ExpandProperty id)
        $constructiveHeuristicIds += ($xmlDoc.solution.heuristicUsages.constructiveHeuristicUsages.hu | Select-Object -ExpandProperty id)
        $repairingHeuristicIds += ($xmlDoc.solution.heuristicUsages.repairingHeuristicUsages.hu | Select-Object -ExpandProperty id)

        $heuristicUsages = $xmlDoc.solution.heuristicUsages.orderingHeuristicUsages.hu + 
            $xmlDoc.solution.heuristicUsages.constructiveHeuristicUsages.hu +
            $xmlDoc.solution.heuristicUsages.repairingHeuristicUsages.hu | 
            Select-Object id, @{Name = 'usage count'; Expression = '#text' }

        $solutionFile.BaseName -match "r(\d)+$" | Out-Null
        $runNo = [int]::Parse($matches[1])

        $dataItem = @{
            id = "$problemId"
            runNo = $runNo
            "matrix based distance" = $null -ne $problemObject.DistanceMatrix
            "asymmetric transport" = $vrp.TransportAsymmetry
            "time windows" = $vrp.TimeWindows
        }

        foreach ($hu in $heuristicUsages) {
            $propertyName = $hu.id
            $propertyValue = [int]::Parse($hu.'usage count')
            $dataItem.$propertyName = $propertyValue
        }
        $data += [PSCustomObject]$dataItem
    }
}

$orderingHeuristicIds = $orderingHeuristicIds | Sort-Object -Unique
$constructiveHeuristicIds = $constructiveHeuristicIds | Sort-Object -Unique
$repairingHeuristicIds = $repairingHeuristicIds | Sort-Object -Unique

$keys = "id", "runNo", "matrix based distance", "asymmetric transport", "time windows" + $orderingHeuristicIds + $constructiveHeuristicIds + $repairingHeuristicIds
$data | Select-Object $keys | Export-Csv -Delimiter "`t" -Path $csvOutputPath -NoTypeInformation 