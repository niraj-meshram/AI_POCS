@ECHO OFF

SETLOCAL

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if not defined JAVA_HOME goto findJavaFromPath

set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto init

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail

:findJavaFromPath
set JAVA_EXE=java.exe
where java.exe >NUL 2>&1
if %ERRORLEVEL% == 0 goto init

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail

:init
if exist "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" goto execute

echo ERROR: Could not find gradle-wrapper.jar. Please run "gradle wrapper" to generate it.
goto fail

:execute
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
ENDLOCAL
exit /b %ERRORLEVEL%

:fail
ENDLOCAL
exit /b 1