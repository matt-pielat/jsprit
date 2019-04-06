
$algorithms = @("GarridoRiff", "jsprit")
$problems = @("C101", "R101", "M30", "M31", "X-n322", "X-n327")
$scriptPath = $PSScriptRoot + "\convergence.py"

foreach ($problem in $problems) {
    $outputPath = "D:\Magisterka\convergence\${problem}.tsv"
    $params = $scriptPath, $outputPath

    foreach ($algorithm in $algorithms) {
        $inputPaths = Get-ChildItem "D:\Google Drive\Magisterka\data\Convergence\Solutions\${algorithm}" | 
            Where-Object { $_.BaseName.StartsWith($problem) } | 
            Select-Object -ExpandProperty FullName
        $params += $inputPaths
    }

    & python.exe  @params
}