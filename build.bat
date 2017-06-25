@echo off
set JAVAC=%JAVA_HOME%\bin\javac.exe
set JAR=%JAVA_HOME%\bin\jar.exe
set SOURCES=src
set BASEPACKAGE=net

for /f %%f in ('dir /b snmp4j*.jar') do set "SNMP4J=%%~nxf"

set JAVAC_ARGS=-source "1.7" -target "1.7" -d . -cp ".;%SNMP4J%"

echo Compiling java files...

"%JAVAC%" %JAVAC_ARGS% %SOURCES%\*.java

echo Create MANIFEST...

echo Class-Path: %SNMP4J% > MANIFEST

echo Packaging JAR...

"%JAR%" cvfm SnmpHelper.jar MANIFEST %BASEPACKAGE%

echo Cleaning up...

del MANIFEST
rmdir /Q /S %BASEPACKAGE%

echo All done.
