. .\commons.ps1

$problemIds = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Problems" -File | Select-Object -ExpandProperty BaseName
$solutionDir = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Solutions\GarridoRiff" -File
$csvOutputPath = "${dataRoot}\heuristic_usage.tsv"

$orderingHeuristicIds = @()
$constructiveHeuristicIds = @()
$repairingHeuristicIds = @()

$data = @()

foreach ($problemId in $problemIds) {
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

        $dataItem = @{Id = "$($solutionFile.BaseName)" }
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

$keys = , "Id" + $orderingHeuristicIds + $constructiveHeuristicIds + $repairingHeuristicIds
$data | Select-Object $keys | Export-Csv -Delimiter "`t" -Path $csvOutputPath -NoTypeInformation 