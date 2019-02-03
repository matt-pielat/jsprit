$scriptPath = "D:\Repos\jsprit\mgr-benchmark\scripts\Start-Runner.ps1"

$timePerRunInMs = 60000 # 1 minute
$runsPerProblem = 4

function Run-Benchmark
{
    param([int]$populationSize, [int]$offspringSize, [int]$chromosomeSize)

    $problemsDirCvrp = "D:\VRP Benchmarks\Parameter tests\ProblemsCVRP"
    $problemsDirVrptw = "D:\VRP Benchmarks\Parameter tests\ProblemsVRPTW"

    $solutionsDirCvrp = "D:\VRP Benchmarks\Parameter tests\Solutions_CVRP_p${populationSize}o${offspringSize}c${chromosomeSize}"
    $solutionsDirVrptw = "D:\VRP Benchmarks\Parameter tests\Solutions_VRPTW_p${populationSize}o${offspringSize}c${chromosomeSize}"

    $logDir = "D:\VRP Benchmarks\Parameter tests\Logs"

    "POP: ${populationSize} OFF: ${offspringSize} CHR: ${chromosomeSize} (CVRP)"

    & "$scriptPath" `
        -InputDir "$problemsDirCvrp" `
        -TimeWindows $false `
        -GarridoRiffOutDir "$solutionsDirCvrp" `
        -LogDir "$logDir" `
        -TimePerRun $timePerRunInMs `
        -RunsPerProblem $runsPerProblem `
        -PopulationSize $populationSize `
        -OffspringSize $offspringSize `
        -ChromosomeSize $chromosomeSize

    "POP: ${populationSize} OFF: ${offspringSize} CHR: ${chromosomeSize} (VRPTW)"

    & "$scriptPath" `
        -InputDir "$problemsDirVrptw" `
        -TimeWindows $true `
        -GarridoRiffOutDir "$solutionsDirVrptw" `
        -LogDir "$logDir" `
        -TimePerRun $timePerRunInMs `
        -RunsPerProblem $runsPerProblem `
        -PopulationSize $populationSize `
        -OffspringSize $offspringSize `
        -ChromosomeSize $chromosomeSize
}

Run-Benchmark -populationSize 5 -offspringSize 2 -chromosomeSize 5
Run-Benchmark -populationSize 5 -offspringSize 4 -chromosomeSize 5
Run-Benchmark -populationSize 10 -offspringSize 5 -chromosomeSize 5
Run-Benchmark -populationSize 15 -offspringSize 5 -chromosomeSize 5
Run-Benchmark -populationSize 15 -offspringSize 10 -chromosomeSize 5
Run-Benchmark -populationSize 20 -offspringSize 5 -chromosomeSize 5
Run-Benchmark -populationSize 20 -offspringSize 10 -chromosomeSize 5
Run-Benchmark -populationSize 20 -offspringSize 15 -chromosomeSize 5
Run-Benchmark -populationSize 30 -offspringSize 10 -chromosomeSize 5
Run-Benchmark -populationSize 30 -offspringSize 15 -chromosomeSize 5