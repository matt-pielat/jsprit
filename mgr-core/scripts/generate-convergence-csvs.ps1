. .\commons.ps1

$outputDir = "${dataRoot}\convergence csvs"
$scriptPath = $PSScriptRoot + "\convergence.py"

$problemIds = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Problems" -File | Select-Object -ExpandProperty BaseName
$solutionDirs = Get-ChildItem "${dataRoot}\data sets\Cherry picked\Solutions" -Directory

foreach ($solutionDir in $solutionDirs) {
    foreach ($problemId in $problemIds) {
        $outputPath = "${outputDir}\$($solutionDir.BaseName)_${problemId}.tsv"

        $inputFilePaths = Get-ChildItem $solutionDir.FullName | 
            Where-Object { $_.BaseName.StartsWith($problemId) } | 
            Select-Object -ExpandProperty FullName |
            Sort-Object

        $params = @($scriptPath, $outputPath) + $inputFilePaths
        & python.exe  @params
        
    }
}