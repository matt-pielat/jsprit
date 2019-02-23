using module ".\commons.psm1"

function Validate-Benchmark
{
    param(
        [string]$Directory, 
        [string]$ProblemFormat
    )

    $problemsDir = "$Directory\Problems"
    $solutionsDir = "$Directory\Solutions"

    $problemFiles = Get-ChildItem $problemsDir
    foreach ($problemFile in $problemFiles)
    {
        if ($ProblemFormat -eq "Tsplib95") {
            $vrp = $problemFile | Select-Object -ExpandProperty FullName | Read-AugeratFormat
        }
        elseif ($ProblemFormat -eq "Solomon") {
            $vrp = $problemFile | Select-Object -ExpandProperty FullName | Read-SolomonFormat
        }
        
        $problemId = $problemFile | Select-Object -ExpandProperty BaseName
        
        $jspritSolutions = Get-ChildItem "$solutionsDir\jsprit" | Where-Object { $_.Name.StartsWith(($problemId)) }
        $garridoRiffSolutions = Get-ChildItem "$solutionsDir\GarridoRiff" | Where-Object { $_.Name.StartsWith(($problemId)) }
        $allSolutions = $jspritSolutions + $garridoRiffSolutions | Sort-Object

        #TODO minimal cost

        foreach ($solutionFile in $allSolutions)
        {
            $solution = $solutionFile | Select-Object -ExpandProperty FullName | Read-XmlSolutionFile
            Validate-Solution -Vrp $vrp -Solution $solution -MinSolutionCost 0.0
        }
    }
}

function Validate-Solution {
    param (
        [VrpDefinition]$Vrp,
        [VrpSolution]$Solution,
        [double]$MinSolutionCost
    )

    $solutionCost = 0

    foreach ($route in $Solution.Routes)
    {
        $satisfiedDemand = 0
        $prevNode = $Vrp.Depot
        $prevDepartureTime = $Vrp.Depot.WindowStart

        foreach ($nodeId in $route.CustomerIds)
        {
            $node = $Vrp.CustomersById[[int]$nodeId]
            $distance = $Vrp.GetDistance($prevNode, $node)
            $solutionCost += $distance
            $arrivalTime = $prevDepartureTime + $distance
            $departureTime = $node.GetDepartureTime($arrivalTime)
            if ($arrivalTime -gt $node.WindowEnd)
            {
                Write-Error "${SolutionPath}: arrived too late ($departureTime) at $($node.Id)"
                return
            }
            $satisfiedDemand += $node.Demand

            $prevNode = $node
            $prevDepartureTime = $departureTime
        }

        $distance = $Vrp.GetDistance($prevNode, $Vrp.Depot)
        $solutionCost += $distance
        $arrivalTime = $prevDepartureTime + $distance
        if ($arrivalTime -gt $Vrp.Depot.WindowEnd)
        {
            Write-Error "${SolutionPath}: arrived too late ($arrivalTime) at the depot from $($prevNode.Id)"
            return
        }

        if ($satisfiedDemand -gt $Vrp.Capacity)
        {
            Write-Error "${SolutionPath}: capacity exceeded ($satisfiedDemand) on route $($route.node[0].Id) - $($prevNode.Id)"
            return
        }
    }

    if ($MinSolutionCost -and ($MinSolutionCost -gt $solutionCost))
    {
        Write-Error "${SolutionPath}: capacity exceeded ($satisfiedDemand) on route $($route.node[0].Id) - $($prevNode.Id)"
        return
    }
}

Validate-Benchmark -Directory "D:\Google Drive\Magisterka\data\Set E (Christofides and Eilon, 1969)" -ProblemFormat Tsplib95
Validate-Benchmark -Directory "D:\Google Drive\Magisterka\data\Solomon" -ProblemFormat Tsplib95
Validate-Benchmark -Directory "D:\Google Drive\Magisterka\data\Uchoa et al. (2014)" -ProblemFormat Tsplib95
Validate-Benchmark -Directory "D:\Google Drive\Magisterka\data\VrpTestCasesGenerator" -ProblemFormat Solomon