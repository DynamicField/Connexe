@echo off
REM Recycled from https://github.com/ChuechTeam/Domotique/blob/main/docs/javadoc.bat
echo Generating Javadoc documentation...
pushd %~dp0
call gradlew.bat javadoc
if %ERRORLEVEL% neq 0 (
    echo Failed to generate Javadoc documentation.
    pause
    popd
    exit /b %ERRORLEVEL%
)
echo Javadocs have been generated at %CD%\build\docs\javadoc
echo Opening Javadoc in browser...
start "" "build\docs\javadoc\index.html"
echo Done.
popd