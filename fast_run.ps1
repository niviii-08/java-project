# Fast build and run
if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory -Path out
$files = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
& javac -cp "lib/mysql-connector-java.jar" -d out $files
if ($LASTEXITCODE -eq 0) {
    & java -cp "out;lib/mysql-connector-java.jar" com.pathfinder.Main
} else {
    Write-Error "Compilation failed"
}
