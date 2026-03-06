@REM Maven Wrapper script for Windows
@REM
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper JAR...
  powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%' -UseBasicParsing"
)

if not exist "%WRAPPER_JAR%" (
  echo ERROR: Could not find or download maven-wrapper.jar
  exit /b 1
)

if not "%JAVA_HOME%"=="" (
  set "JAVA_EXE=%JAVA_HOME%\bin\java"
) else (
  set "JAVA_EXE=java"
)

"%JAVA_EXE%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR:~0,-1%" "-cp" "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal & exit /b %ERRORLEVEL%
