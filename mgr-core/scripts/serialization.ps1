. .\commons.ps1

function Read-SolutionFile {
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath,

        [string]
        $SolutionType,

        [SolutionFormat]
        $ExternalFormat
    )

    $format = [SolutionFormat]::None
    switch -regex ($SolutionType) {
        "jsprit|GarridoRiff" {
            $format = [SolutionFormat]::Xml
        }
        "Bakala" {
            $format = [SolutionFormat]::Plain
        }
        "optimal|best" {
            $format = $ExternalFormat
        }
    }
    
    switch ($format) {
        Xml {
            $solution = Read-XmlSolutionFile -FilePath $FilePath
        }
        Plain {
            $solution = Read-AugeratSolutionFile -FilePath $FilePath
        }
        Uchoa {
            try {
                $solution = Read-UchoaSolutionFile -FilePath $FilePath
            }
            catch {
                $solution = Read-AugeratSolutionFile -FilePath $FilePath
            }
        }
        Default {
            Write-Error "Solution format ${Format} is not supported."
            return
        }
    }

    return $solution
}

function Read-XmlSolutionFile {
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath
    )

    $xmlDoc = New-Object System.Xml.XmlDocument
    $xmlDoc.Load($FilePath)

    $solution = New-Object VrpSolution

    foreach ($route in $xmlDoc.solution.routes.route)
    {
        $newRoute = New-Object Route
        foreach ($nodeId in $route.node)
        {
            $newRoute.CustomerIds += $nodeId
        }
        $solution.Routes += $newRoute
    }
    $solution.Cost = $xmlDoc.solution.cost

    return $solution
}

function Read-AugeratSolutionFile {
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath
    )

    $solution = New-Object VrpSolution

    foreach ($line in Get-Content $FilePath) {
        if ($line -match "^\s*[Rr]oute") {
            $newRoute = New-Object Route
            $newRoute.CustomerIds = $line -creplace "^.*:", "" | 
                ForEach-Object { $_.Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries) } | 
                ForEach-Object { [int]::parse($_) }
            $solution.Routes += $newRoute
        }
        elseif ($line -match "^\s*[Cc]ost") {
            $line -match "[0-9]+((,|\.)[0-9]+)?" | Out-Null
            $solution.Cost = $matches[0]
        }
    }

    return $solution
}

function Read-UchoaSolutionFile {
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath
    )

    $solution = New-Object VrpSolution
    $k = 0

    $i = 0
    foreach ($line in Get-Content $FilePath) {
        $i += 1
        if ($i -eq 1) {
            $solution.Cost = [double]::parse($line)
        }
        elseif ($i -eq 2) {
            $k = [int]::parse($line)
        }
        elseif ($i -ge 5 -and $i -lt $k + 5) {
            $newRoute = New-Object Route
            $line -match "0 (([1-9][0-9]* )+)0 *$" | Out-Null
            $nodes = $matches[1].Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries) | 
                ForEach-Object { [int]::parse($_) }
            $newRoute.CustomerIds = $nodes
            $solution.Routes += $newRoute
        }
    }

    return $solution
}

function Read-ProblemFile {
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath,

        [ProblemFormat]
        $ProblemFormat
    )    

    switch ($ProblemFormat) {
        Tsplib95 {
            return Read-Tsplib95ProblemFile -FilePath $FilePath
        }
        Solomon {
            return Read-SolomonProblemFile -FilePath $FilePath
        }
        Default {
            Write-Error "Solution format ${Format} is not supported."
            return $null
        }
    }
}

function Read-Tsplib95ProblemFile {
    param (
        [Parameter(ValueFromPipeline)]
        [string]
        $FilePath
    )

    $readEdgeWeightSection = $false
    $readDemandSection = $false
    $readNodeCoordSection = $false
    $dimension = 0

    $x = 0
    $y = 0

    $vrp = New-Object VrpDefinition

    foreach ($line in Get-Content $FilePath) {
        if ($readEdgeWeightSection) {
            if ($line -notmatch "^[ \t]*([0-9]+(\.[0-9]+)?[ \t]+)*[0-9]+(\.[0-9]+)?[ \t]*$") {
                $readEdgeWeightSection = $false
                continue
            }

            $tokens = $line.Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries)

            if ($edgeWeightFormat -eq "LOWER_ROW") {

                foreach ($token in $tokens) {
                    if ($y -ge $x) { $x += 1; $y = 0 }

                    $vrp.DistanceMatrix[$x, $y] = $vrp.DistanceMatrix[$y, $x] = [double]$token
                    $y++
                }           
            }
            elseif ($edgeWeightFormat -eq "FULL_MATRIX") {
                foreach ($token in $tokens) {
                    if ($y -ge $dimension) { $x += 1; $y = 0 }
                    if ($y -eq $x -and $token -ne 0) {
                        Write-Error "Invalid value"
                    }

                    $vrp.DistanceMatrix[$x, $y] = [double]$token
                    $y++
                }
            }
            else {
                Write-Error "Unsupported edge weight format"
                return
            }
            continue
        }
        if ($readDemandSection) {
            if ($line -notmatch "^[ \t]*[0-9]+[ \t]+[0-9]+[ \t]*$") {
                $readDemandSection = $false
                continue
            }
            $tokens = $line.Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries)

            $id = [int]$tokens[0] - 1
            if ($id -eq 0) {
                $vrp.Depot.Demand = [int]$tokens[1]
            }
            else {
                $vrp.CustomersById[$id].Demand = [int]$tokens[1]
            }
            continue
        }
        if ($readNodeCoordSection) {
            if ($line -notmatch "^[ \t]*[0-9]+([ \t]+[0-9]+(\.[0-9]+)?){2}[ \t]*$") {
                $readNodeCoordSection = $false
                continue
            }
            $tokens = $line.Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries)

            $id = [int]$tokens[0] - 1
            if ($id -eq 0) {
                $vrp.Depot.CoordX = [double]$tokens[1]
                $vrp.Depot.CoordY = [double]$tokens[2]
            }
            else {
                $vrp.CustomersById[$id].CoordX = [double]$tokens[1]
                $vrp.CustomersById[$id].CoordY = [double]$tokens[2]
            }
            continue
        }
        if ($line -match "DIMENSION") {
            $dimension = [int]$line.Substring($line.IndexOf(':') + 1).Trim()

            $vrp.Depot = New-Object Node
            $vrp.Depot.Id = 0

            for ($i = 1; $i -lt $dimension; $i++) {
                $node = New-Object Node
                $node.Id = $i

                $vrp.CustomersById.Add($i, $node)
            }
        }
        if ($line -match "EDGE_WEIGHT_FORMAT") {
            $edgeWeightFormat = $line.Substring($line.IndexOf(':') + 1).Trim().ToUpper()
        }
        if ($line -match "EDGE_WEIGHT_TYPE") {
            $edgeWeightType = $line.Substring($line.IndexOf(':') + 1).Trim().ToUpper()
            if ($edgeWeightType -eq "EXPLICIT") {
                $vrp.DistanceMatrix = New-Object 'double[,]' $dimension,$dimension 
            }
        }
        if ($line -match "CAPACITY") {
            $vrp.Capacity = [int]$line.Substring($line.IndexOf(':') + 1).Trim()
        }
        if ($line -match "EDGE_WEIGHT_SECTION") {
            $readEdgeWeightSection = $true
        }
        if ($line -match "DEMAND_SECTION") {
            $readDemandSection = $true
        }
        if ($line -match "NODE_COORD_SECTION") {
            $readNodeCoordSection = $true
        }
    }
    return $vrp
}

function Read-SolomonProblemFile {
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
            $tokens = $line.Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries)

            $vrp.Capacity = [int]$tokens[1]

            $readVehicleInfo = $false
            continue
        }
        if ($readCustomerInfo) {
            if ($line -notmatch "^[ \t]*[0-9]+([ \t]+[0-9]+){6}[ \t]*$") {
                $readCustomerInfo = $false
                continue
            }

            $tokens = $line.Split(" `t", [System.StringSplitOptions]::RemoveEmptyEntries)

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
        if ($line -match "VEHICLE") {
            $readVehicleInfo = $true
            $linesToSkip = 1
            continue
        }
        if ($line -match "CUSTOMER") {
            $readCustomerInfo = $true
            $linesToSkip = 2
            continue
        }
    }
    return $vrp
}