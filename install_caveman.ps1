$wc = New-Object System.Net.WebClient
$wc.Encoding = [System.Text.Encoding]::UTF8
$script = $wc.DownloadString('https://raw.githubusercontent.com/JuliusBrussee/caveman/main/install.ps1')
$tmp = [System.IO.Path]::GetTempFileName() + '.ps1'
[System.IO.File]::WriteAllText($tmp, $script, [System.Text.Encoding]::UTF8)
& powershell -ExecutionPolicy Bypass -File $tmp -Only gemini,antigravity -NoHooks -NoMcpShrink
Remove-Item $tmp -Force
