$benchmarks = @(
    # @{ path = "..\data\Set E (Christofides and Eilon, 1969)" }
    @{ path = "..\data\Solomon" }
    # @{ path = "..\data\Uchoa et al. (2014)" },
    # @{ path = "..\data\VrpTestCasesGenerator" }
)

$solutionTypes = @(
    "jsprit",
    "Pielat"
)

class Node {
    [int]$Id
    [double]$CoordX
    [double]$CoordY
    [double]$WindowStart
    [double]$WindowEnd
    [double]$ServiceTime
    [int]$Demand

    [double]GetDepartureTime([double]$ArrivalTime) {
        $actualArrivalTime = $this.WindowStart, $ArrivalTime | Measure-Object -Maximum | Select-Object -ExpandProperty Maximum
        return $actualArrivalTime + $this.ServiceTime
    }
}

class VrpDefinition {
    [Node]$Depot
    [hashtable]$CustomersById = @{}
    [int]$Capacity

    [double]GetDistance([Node]$A, [Node]$B) {
        $distanceSquared = ($A.CoordX - $B.CoordX) * ($A.CoordX - $B.CoordX) + `
            ($A.CoordY - $B.CoordY) * ($A.CoordY - $B.CoordY)
        return [System.Math]::Sqrt($distanceSquared)
    }
}

function Read-SolomonFormat {
    # Parameter help description
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath
    )

    $readVehicleInfo = $false
    $readCustomerInfo = $false
    $linesToSkip = 0

    $vrp = New-Object VrpDefinition

    foreach ($line in Get-Content $FilePath) {
        if ($linesToSkip -gt 0) {
            $linesToSkip--
            continue
        }
        if ($readVehicleInfo) {
            $tokens = $line.Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)

            $vrp.Capacity = [int]$tokens[1]

            $readVehicleInfo = $false
            continue
        }
        if ($readCustomerInfo) {
            if ($line -notmatch "^ *[0-9]+( +[0-9]+){6} *$") {
                $readCustomerInfo = $false
                continue
            }

            $tokens = $line.Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)

            $node = New-Object Node
            $node.Id = [int]$tokens[0]
            $node.CoordX = [int]$tokens[1]
            $node.CoordY = [int]$tokens[2]
            $node.Demand = [int]$tokens[3]
            $node.WindowStart = [int]$tokens[4]
            $node.WindowEnd = [int]$tokens[5]
            $node.ServiceTime = [int]$tokens[6]

            if ($node.Id -eq 0) {
                $vrp.Depot = $node
            }
            else {
                $vrp.CustomersById.Add($node.Id, $node)
            }

            continue
        }
        if ($line -match "^ *VEHICLE *$") {
            $readVehicleInfo = $true
            $linesToSkip = 1
            continue
        }
        if ($line -match "^ *CUSTOMER *$") {
            $readCustomerInfo = $true
            $linesToSkip = 2
            continue
        }
    }

    return $vrp
}

foreach ($benchmark in $benchmarks) 
{
    $problemDirectory = "$($benchmark.path)\Problems"

    $problemFiles = Get-ChildItem $problemDirectory
    foreach ($problemFile in $problemFiles)
    {
        $problemId = $problemFile | Select-Object -ExpandProperty BaseName
        $vrp = $problemFile | Select-Object -ExpandProperty FullName | Read-SolomonFormat

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

                $solutionCost = 0
                $routeIsValid = $true

                $routes = $xmlDoc.solution.routes
                foreach ($route in $routes.route)
                {
                    $satisfiedDemand = 0
                    $prevNode = $vrp.Depot
                    $prevDepartureTime = $vrp.Depot.WindowStart

                    foreach ($nodeId in $route.node)
                    {
                        $node = $vrp.CustomersById[[int]$nodeId]
                        $arrivalTime = $prevDepartureTime + $vrp.GetDistance($prevNode, $node)
                        $departureTime = $node.GetDepartureTime($arrivalTime)
                        if ($arrivalTime -gt $node.WindowEnd)
                        {
                            Write-Error "$($solution.Name) ($solutionType): arrived too late ($departureTime) at $($node.Id)"
                            $routeIsValid = $false
                            break
                        }
                        $satisfiedDemand += $node.Demand

                        $prevNode = $node
                        $prevDepartureTime = $departureTime
                    }

                    if (-not $routeIsValid)
                    {
                        break                        
                    }

                    $arrivalTime = $prevDepartureTime + $vrp.GetDistance($prevNode, $vrp.Depot)
                    if ($arrivalTime -gt $vrp.Depot.WindowEnd)
                    {
                        Write-Error "$($solution.Name) ($solutionType): arrived too late ($arrivalTime) at the depot from $($prevNode.Id)"
                        $routeIsValid = $false
                        break
                    }

                    if ($satisfiedDemand -gt $vrp.Capacity)
                    {
                        Write-Error "$($solution.Name) ($solutionType): capacity exceeded ($satisfiedDemand) on route $($route.node[0].Id) - $($prevNode.Id)"
                        $routeIsValid = $false
                        break
                    }
                }

                

				# #TODO
                # $key = $solution.BaseName.Replace($problemId, $solutionType)
                # $value = $xmlDoc.solution.cost

                # # $xmlDoc.solution.routes.route[0]
                # $dataItem | Add-Member $key $value
            }
        }
    }
}