# Launcher isolado para o cliente de teste do Lote B.
# Le (somente leitura) a definicao de versao e bibliotecas do RPGMODPACK; grava apenas em teste_lote_b\client_env.
param([string]$User = "TesteLoteB", [switch]$PrepareOnly, [switch]$Join)
$ErrorActionPreference = "Stop"
$mc      = "$env:APPDATA\.minecraft"
$verDir  = "$mc\versions\RPGMODPACK"
$base    = "C:\Users\Matt\Desktop\modpacknovo\caos-arcana-mod\run\teste_lote_b"
$client  = "$base\client_env"
$natives = "$client\natives"
$java    = "C:\Program Files\Java\jdk-17\bin\java.exe"

New-Item -ItemType Directory -Force -Path $client, $natives, "$client\mods" | Out-Null
Get-ChildItem "$verDir\natives" -File | Where-Object { $_.Extension -eq '.dll' } | ForEach-Object {
    $dst = Join-Path $natives $_.Name
    if (-not (Test-Path $dst)) { Copy-Item $_.FullName $dst }
}

$j = Get-Content "$verDir\RPGMODPACK.json" -Raw | ConvertFrom-Json
$cp = New-Object System.Collections.Generic.List[string]
$missing = @()
foreach ($lib in $j.libraries) {
    $ok = $true
    if ($lib.rules) {
        $ok = $false
        foreach ($r in $lib.rules) {
            $match = (-not $r.os) -or ($r.os.name -eq 'windows')
            if ($match) { $ok = ($r.action -eq 'allow') }
        }
    }
    if (-not $ok) { continue }
    $paths = @()
    if ($lib.artifact.path) { $paths += $lib.artifact.path }
    elseif ($lib.downloads.artifact.path) { $paths += "libraries/" + $lib.downloads.artifact.path }
    else {
        $p = $lib.name.Split(':'); $g = $p[0].Replace('.', '/')
        $file = if ($p.Count -ge 4) { "$($p[1])-$($p[2])-$($p[3]).jar" } else { "$($p[1])-$($p[2]).jar" }
        $paths += "libraries/$g/$($p[1])/$($p[2])/$file"
    }
    foreach ($rel in $paths) {
        if ($rel -match 'natives-(linux|macos|osx)|natives-windows-(arm64|x86)') { continue }
        if (-not $rel.StartsWith('libraries/')) { $rel = 'libraries/' + $rel }
        $full = Join-Path $mc $rel
        if (Test-Path $full) { if (-not $cp.Contains($full)) { $cp.Add($full) } } else { $missing += $rel }
    }
}
$cp.Add("$verDir\RPGMODPACK.jar")
if ($missing.Count) { Write-Host "Bibliotecas ausentes (ignoradas):"; $missing | ForEach-Object { Write-Host "  $_" } }
Write-Host "Classpath: $($cp.Count) entradas"

$uuid = [guid]::NewGuid().ToString("N")
$argFile = "$client\launch_args.txt"
$args = @(
  "-Xms1G", "-Xmx3G", "-Xss1M",
  "-Djava.library.path=$natives", "-Djna.tmpdir=$natives",
  "-Dorg.lwjgl.system.SharedLibraryExtractPath=$natives", "-Dio.netty.native.workdir=$natives",
  "-Dminecraft.launcher.brand=caos-teste-lote-b",
  "-cp", ($cp -join ';'),
  $j.mainClass,
  "--username", $User, "--version", "RPGMODPACK", "--gameDir", $client,
  "--assetsDir", "$mc\assets", "--assetIndex", $j.assetIndex.id,
  "--uuid", $uuid, "--accessToken", "0", "--userType", "legacy", "--versionType", "release",
  "--userProperties", "{}", "--clientId", "00000000-0000-0000-0000-000000000000"
)
if ($Join) { $args += @("--quickPlayMultiplayer", "localhost:25565") }
($args | ForEach-Object { '"' + ($_ -replace '\\', '\\') + '"' }) -join "`n" | Set-Content -Path $argFile -Encoding ASCII
if ($PrepareOnly) { Write-Host "Preparado: $argFile"; exit 0 }
Set-Location $client
& $java "@$argFile"
