. .\serialization.ps1

$csvPath = "D:\results.csv"
$data = @()
$keys = @{}

foreach ($benchmark in $allBenchmarks) {
    $benchmarkName = Split-Path -Path $benchmark.path -Leaf
    $problemDirectory = "$($benchmark.path)\Problems"
    $problemFiles = Get-ChildItem $problemDirectory

    foreach ($problemFile in $problemFiles) {
        $problemId = $problemFile.BaseName
        $problemObject = $problemFile.FullName | Read-ProblemFile -ProblemFormat $benchmark.problemFormat

        $dataItem = [PSCustomObject]@{
            id = $problemId
            "is best optimal" = $false
        }
        $data += $dataItem

        foreach ($solutionType in $allSolutionTypes) {
            $solutionDirectory = "$($benchmark.path)\Solutions\${solutionType}"
            if (-not (Test-Path $solutionDirectory)) {
                continue
            }

            $solutionFiles = Get-ChildItem $solutionDirectory | Where-Object { $_.Name.StartsWith($problemId) }
            
            foreach ($solutionFile in $solutionFiles) {
                $solutionObject = $solutionFile.FullName | Read-SolutionFile -SolutionType $solutionType -ExternalFormat $benchmark.externalSolutionFormat

                if (-not $solutionObject) {
                    Write-Warning "Solution ${solutionFile} not parsed."
                    continue
                }

                if ($solutionType -eq "optimal") {
                    $keyBase = $solutionFile.BaseName.Replace($problemId, "best")
                    $dataItem."is best optimal" = $true
                }
                else {
                    $keyBase = $solutionFile.BaseName.Replace($problemId, $solutionType)
                }

                $costKey = "cost " + $keyBase
                $dataItem | Add-Member $costKey $problemObject.GetSolutionCost($solutionObject)
                $keys[$costKey] = $null

                $kKey = "k " + $keyBase
                $dataItem | Add-Member $kKey $solutionObject.Routes.Count
                $keys[$kKey] = $null
            }
        }
    }
}

$keys = $keys.Keys | Sort-Object
$keys = "id", "is best optimal" + $keys | Select-Object -Unique

$culture = [System.Globalization.CultureInfo]::InvariantCulture
[System.Threading.Thread]::CurrentThread.CurrentCulture = $culture

$data | Select-Object $keys | Export-Csv -Delimiter ',' -Path $csvPath -NoTypeInformation 