$benchmarks = @(
    # @{ path = "..\data\Set E (Christofides and Eilon, 1969)" }
    @{ path = "..\data\Solomon" },
    # @{ path = "..\data\Uchoa et al. (2014)" },
    @{ path = "..\data\VrpTestCasesGenerator" }
)

$solutionTypes = @(
    "optimal",
    "best known",
    "jsprit",
    "Pielat",
    "Bakala"
)

$data = @()

foreach ($benchmark in $benchmarks) 
{
    $problemDirectory = "$($benchmark.path)\Problems"

    $problemIds = Get-ChildItem $problemDirectory | Select-Object -ExpandProperty BaseName | Sort-Object
    foreach ($problemId in $problemIds)
    {
        $dataItem = New-Object psobject -Property @{Id = $problemId}
        $data += $dataItem

        foreach ($solutionType in $solutionTypes)
        {
            $solutionDirectory = "$($benchmark.path)\Solutions\${solutionType}"
    
            if (-not (Test-Path $solutionDirectory))
            {
                Write-Output "Skipping $solutionDirectory because it does not exist."
                continue
            }

            $solutions = Get-ChildItem $solutionDirectory | Where-Object { $_.Name.StartsWith(($problemId)) } | Sort-Object
            foreach ($solution in $solutions)
            {
                $xmlDoc = New-Object System.Xml.XmlDocument
                $xmlDoc.Load($solution.FullName)                

                $key = $solution.BaseName.Replace($problemId, $solutionType)
                $value = $xmlDoc.solution.cost

                $dataItem | Add-Member $key $value
            }
        }
    }
}

$data | ConvertTo-Csv -Delimiter ';' -NoTypeInformation | Out-File -FilePath "D:\VRP Benchmarks\results.csv"