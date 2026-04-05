Write-Host "Cleaning generated folders for portable copy..."

$root = Split-Path -Parent $PSScriptRoot
$paths = @(
  "$root\frontend\node_modules",
  "$root\frontend\dist",
  "$root\backend\target",
  "$root\.idea",
  "$root\.vscode"
)

foreach ($p in $paths) {
  if (Test-Path $p) {
    Remove-Item -Recurse -Force $p
    Write-Host "Deleted: $p"
  } else {
    Write-Host "Skip (not found): $p"
  }
}

Write-Host "Done."

