$PROJECT_PATH = "D:\04.Android\04.Chuong 4\ClassManagerDemoV2"
$PACKAGE_NAME = "vn.edu.vaa.classmanagerdemo"

$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = "$env:ANDROID_HOME"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator;$env:Path"

cd $PROJECT_PATH

adb wait-for-device

Write-Host "Build nhanh va cai app..."
.\gradlew.bat :app:installDebug

Write-Host "Mo app..."
adb shell monkey -p $PACKAGE_NAME -c android.intent.category.LAUNCHER 1