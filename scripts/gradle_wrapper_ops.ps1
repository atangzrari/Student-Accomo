$path = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.4-bin"
if (Test-Path $path) {
    Get-ChildItem $path -Recurse -Force | Format-List -Force
} else {
    Write-Output 'No wrapper dir found'
}
Remove-Item -Recurse -Force $path -ErrorAction Stop
