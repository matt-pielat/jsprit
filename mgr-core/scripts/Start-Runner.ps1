param(
    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [string]$ProblemPath,

    [ValidateNotNullOrEmpty()]
    [string]$LogPath,

    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [string]$SolutionPath,

    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [ValidateSet('Solomon','Tsplib95')]
    [string]$ProblemFormat,
    
    [Parameter(Mandatory)]
    [ValidateNotNullOrEmpty()]
    [ValidateSet('jsprit','GarridoRiff')]
    [string]$Algorithm,

    [int]$PopulationSize,

    [int]$OffspringSize,

    [int]$ChromosomeSize,

    [long]$TimeLimit,

    [int]$IterationLimit
)

# .\Start-Runner -ProblemPath "D:\Repos\jsprit\mgr-benchmark\data\Uchoa et al. (2014)\Problems\X-n101-k25.vrp" -SolutionPath "D:\X-n101-k25.sol" -LogPath "D:\X-n101-k25.log" -ProblemFormat Tsplib95 -Algorithm jsprit -IterationLimit 10

$jarPath = Resolve-Path "$PSScriptRoot\..\output\mgr-core.jar"

$jarArgs = @()
$jarArgs += "--problemPath", "`"$ProblemPath`""
if ($LogPath) { $jarArgs += "--logPath", "`"$LogPath`"" }
$jarArgs += "--solutionPath", "`"$SolutionPath`""
$jarArgs += "--problemFormat", "$ProblemFormat"
$jarArgs += "--algorithm", "$Algorithm"
if ($PopulationSize) { $jarArgs += "--populationSize", $PopulationSize }
if ($OffspringSize) { $jarArgs += "--offspringSize", $OffspringSize }
if ($ChromosomeSize) { $jarArgs += "--chromosomeSize", $ChromosomeSize }
if ($TimeLimit) { $jarArgs += "--timeLimit", $TimeLimit }
if ($IterationLimit) { $jarArgs += "--iterationLimit", $IterationLimit }

Invoke-Expression "java -jar $jarPath $jarArgs"