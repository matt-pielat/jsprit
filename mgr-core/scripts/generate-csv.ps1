. .\serialization.ps1

$csvPath = "D:\results.csv"
$data = @()
$keys = @{}

foreach ($benchmark in $allBenchmarks) {
    $benchmarkName = Split-Path -Path $benchmark.path -Leaf
    $problemDirectory = "$($benchmark.path)\Problems"

    $problemFiles = Get-ChildItem $problemDirectory | Select-Object -Property `
        BaseName, `
        FullName, `
        @{Name = "SortPriority"; Expression = { $_.Name -match "[Nn](\d+)\D*[Kk](\d+)"; [int]::Parse($matches[1]); [int]::Parse($matches[2]) }}

    $advancedSort = $problemFiles | Test-All { $_.SortPriority -eq $null }
    if ($advancedSort) {
        $problemFiles = $problemFiles | Sort-Object -Property @{Expression = { $_.SortPriority[1] }}, @{Expression = { $_.SortPriority[2] }}
    }
    else {
        $problemFiles = $problemFiles | Sort-Object -Property BaseName
    }

    foreach ($problemFile in $problemFiles) {
        $problemId = $problemFile.BaseName
        $problemObject = $problemFile.FullName | Read-ProblemFile

        $dataItem = [PSCustomObject]@{
            id = $problemId
            "matrix based distance" = $null -ne $problemObject.DistanceMatrix
            "asymmetric transport" = $problemObject.TransportAsymmetry
            "time windows" = $problemObject.TimeWindows
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
                $solutionObject = $solutionFile.FullName | Read-SolutionFile
                
                if (-not $solutionObject) {
                    Write-Warning "Solution $($solutionFile.FullName) not parsed."
                    continue
                }

                if (-not $problemObject.ValidateSolution($solutionObject)) {
                    Write-Error "Invalid solution $($solutionFile.FullName)"
                }

                if ($solutionType -eq "optimal") {
                    $keyBase = $solutionFile.BaseName.Replace($problemId, "best")
                    $dataItem."is best optimal" = $true
                }
                else {
                    $keyBase = $solutionFile.BaseName.Replace($problemId, $solutionType)
                }

                $solutionCost = $problemObject.GetSolutionCost($solutionObject)
                if ($solutionCost -eq 0) {
                    Write-Error "Cost unknown for $($solutionFile.FullName)"
                }

                $costKey = "cost " + $keyBase
                $dataItem | Add-Member $costKey $solutionCost
                $keys[$costKey] = $null

                $kKey = "k " + $keyBase
                $dataItem | Add-Member $kKey $solutionObject.Routes.Count
                $keys[$kKey] = $null
            }
        }
    }
}

$keys = $keys.Keys | Sort-Object
$keys = "id", "matrix based distance", "asymmetric transport", "time windows", "is best optimal" + $keys | Select-Object -Unique

$culture = [System.Globalization.CultureInfo]::InvariantCulture
[System.Threading.Thread]::CurrentThread.CurrentCulture = $culture

$data | Select-Object $keys | Export-Csv -Delimiter ',' -Path $csvPath -NoTypeInformation 