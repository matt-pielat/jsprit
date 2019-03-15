$dataRootDirectory = "D:\Google Drive\Magisterka\data"

class Node {
    [int]$Id
    [double]$CoordX
    [double]$CoordY
    [double]$WindowStart = 0
    [double]$WindowEnd = [double]::PositiveInfinity
    [double]$ServiceTime = 0
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
    [double[,]]$DistanceMatrix

    [double]GetDistance([Node]$A, [Node]$B) {
        if ($this.DistanceMatrix) {
            return $this.DistanceMatrix[$A.Id, $B.Id]        
        }

        $distanceSquared = ($A.CoordX - $B.CoordX) * ($A.CoordX - $B.CoordX) + `
            ($A.CoordY - $B.CoordY) * ($A.CoordY - $B.CoordY)
        return [System.Math]::Sqrt($distanceSquared)
    }

    [double]GetSolutionCost([VrpSolution]$Solution) {
        [double]$cost = 0

        foreach ($route in $Solution.Routes) {
            $prevNode = $this.Depot
            foreach ($nodeId in $route.CustomerIds) {
                $currNode = $this.CustomersById[$nodeId]
                $cost += $this.GetDistance($prevNode, $currNode)
                $prevNode = $currNode
            }
            $cost += $this.GetDistance($prevNode, $this.Depot)
        }

        return $cost
    }
}

class Route {
    [int[]]$CustomerIds = @()
}

class VrpSolution {
    [double]$Cost
    [Route[]]$Routes = @()
}

enum ProblemFormat {
    Tsplib95
    Solomon
}

enum SolutionFormat {
    None
    Xml
    Plain
    Uchoa
}

$allSolutionTypes = @(
    "optimal",
    "best",
    "jsprit",
    "GarridoRiff",
    "Bakala"
)

$allBenchmarks = @(
    @{ 
        path = "${dataRootDirectory}\Set E (Christofides and Eilon, 1969)"; 
        problemFormat = [ProblemFormat]::Tsplib95; 
        externalSolutionFormat = [SolutionFormat]::Plain
    },
    @{ 
        path = "${dataRootDirectory}\Solomon"; 
        problemFormat = [ProblemFormat]::Solomon;
        externalSolutionFormat = [SolutionFormat]::Plain
    },
    @{ 
        path = "${dataRootDirectory}\Uchoa et al. (2014)"; 
        problemFormat = [ProblemFormat]::Tsplib95;
        externalSolutionFormat = [SolutionFormat]::Uchoa
    },
    @{ 
        path = "${dataRootDirectory}\VrpTestCasesGenerator"; 
        problemFormat = [ProblemFormat]::Tsplib95;
        externalSolutionFormat = [SolutionFormat]::None
    }
)

