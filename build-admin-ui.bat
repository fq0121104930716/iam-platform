@echo off
REM Build admin UI and copy to BFF server resources

echo Building IAM Admin UI...

REM Navigate to admin-ui directory
cd /d %~dp0iam-admin-ui

REM Install dependencies
echo Installing dependencies...
call npm install

REM Build for production
echo Building admin UI...
call npm run build

REM Copy dist folder to BFF server resources
echo Copying build output to BFF server...
if exist ..\iam-bff-server\src\main\resources\static\admin-ui rmdir /s /q ..\iam-bff-server\src\main\resources\static\admin-ui
mkdir ..\iam-bff-server\src\main\resources\static
xcopy /e /i /y dist ..\iam-bff-server\src\main\resources\static\admin-ui

echo.
echo Admin UI build complete!
echo Files copied to: iam-bff-server\src\main\resources\static\admin-ui

pause
