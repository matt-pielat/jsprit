. .\serialization.ps1

$csvPath = "D:\results.csv"
$data = @{}
$csvData = [System.Collections.ArrayList]::new()

foreach ($benchmark in $allBenchmarks) {
    $benchmarkName = Split-Path -Path $benchmark.path -Leaf
    $problemDirectory = "$($benchmark.path)\Problems"
    $problemIds = Get-ChildItem $problemDirectory | Select-Object -ExpandProperty BaseName | Sort-Object

    foreach ($problemId in $problemIds) {
        $dataItem = @{}
        $data[$problemId] = $dataItem

        $csvDataItem = [PSCustomObject]@{id = $problemId}
        $csvData.Add($csvDataItem) | Out-Null

        foreach ($solutionType in $allSolutionTypes) {
            $solutionDirectory = "$($benchmark.path)\Solutions\${solutionType}"
            if (-not (Test-Path $solutionDirectory)) {
                continue
            }

            $solutionFiles = Get-ChildItem $solutionDirectory | Where-Object { $_.Name.StartsWith($problemId) } | Sort-Object
            foreach ($solutionFile in $solutionFiles) {
                $solution = Read-SolutionFile -FilePath $solutionFile.FullName -SolutionType $solutionType -ExternalFormat $benchmark.externalSolutionFormat

                if (-not $solution) {
                    Write-Warning "Solution ${solutionFile} not parsed."
                    continue
                }
                if (-not $solution.Cost) {
                    Write-Warning "Solution ${solutionFile} has no cost."
                }

                $key = $solutionFile.BaseName.Replace($problemId, $solutionType)
                $dataItem[$key + " cost"] = $solution.Cost
                $dataItem[$key + " k"] = $solution.Routes.Count
            }
        }
    }
}

$allPropertyKeys = $data.Values.Keys | Select-Object -Unique | Sort-Object
foreach ($solutionType in $allSolutionTypes) {
    $propertyKeys = $allPropertyKeys | Where-Object { $_.StartsWith($solutionType) }

    foreach ($csvDataItem in $csvData) {
        foreach ($propertyKey in $propertyKeys) {
            $csvDataItem | Add-Member $propertyKey $data[$csvDataItem.id][$propertyKey]
        }
    }
}

$culture = [System.Globalization.CultureInfo]::InvariantCulture
[System.Threading.Thread]::CurrentThread.CurrentCulture = $culture

$csvData | Export-Csv -Delimiter ',' -Path $csvPath -NoTypeInformation 