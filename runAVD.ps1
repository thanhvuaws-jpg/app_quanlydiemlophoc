$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = "$env:ANDROID_HOME"
$env:Path = "$env:ANDROID_HOME\emulator;$env:ANDROID_HOME\platform-tools;$env:Path"

Start-Process emulator -ArgumentList "-avd Pixel_5"

Start-Sleep -Seconds 10

adb devices