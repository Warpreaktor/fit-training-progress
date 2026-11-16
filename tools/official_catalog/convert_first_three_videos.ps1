$ErrorActionPreference = "Stop"

# Одноразовый скрипт конвертации первых 3 официальных видео.
# Требуется установленный ffmpeg и уже скачанные исходные mp4.

$projectRoot = Split-Path -Parent $PSScriptRoot
$assetsRoot = Join-Path $projectRoot "app/src/main/assets/official_catalog/videos"

$items = @(
    @{
        Source = "C:\temp\official-videos\45-degree-hyperextension.mp4"
        Target = Join-Path $assetsRoot "1/demo.mp4"
    },
    @{
        Source = "C:\temp\official-videos\45-degree-bicycle-twisting-crunch-obliques.mp4"
        Target = Join-Path $assetsRoot "2/demo.mp4"
    },
    @{
        Source = "C:\temp\official-videos\45-degree-bicycle-twisting-crunch-abdominals.mp4"
        Target = Join-Path $assetsRoot "3/demo.mp4"
    }
)

foreach ($item in $items) {
    if (-not (Test-Path $item.Source)) {
        throw "Не найден исходный файл: $($item.Source)"
    }

    $targetDirectory = Split-Path -Parent $item.Target
    New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null

    ffmpeg -y `
        -i $item.Source `
        -vf "scale=960:-2" `
        -an `
        -preset medium `
        -crf 30 `
        -movflags +faststart `
        -filter:v "setpts=PTS/2.5" `
        -r 30 `
        -c:v libx264 `
        $item.Target

    if ($LASTEXITCODE -ne 0) {
        throw "ffmpeg завершился с ошибкой для файла: $($item.Source)"
    }

    Write-Host "Готово: $($item.Target)"
