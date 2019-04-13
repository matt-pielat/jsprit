. $PSScriptRoot\commons.ps1

$inputTsv = "${dataRoot}\main aggregated.tsv"
$scriptPath = $PSScriptRoot + "\vehicle_count_chart.py"
$outputPath = "${dataRoot}\vrptw_vehicle_count"

$params = @($scriptPath, $inputTsv, $outputPath)
& python.exe @params