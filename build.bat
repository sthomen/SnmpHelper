@echo off
set JAVAC=%JAVA_HOME%\bin\javac.exe
set JAR=%JAVA_HOME%\bin\jar.exe

for /f %%f in ('dir /b snmp4j*.jar') do set "SNMP4J=%%~nxf"

set JAVAC_ARGS=-sourcepath src -d . -cp ".;%SNMP4J%"

echo Compiling java files...

"%JAVAC%" %JAVAC_ARGS% src\SnmpHelper.java

echo Create MANIFEST...

echo Class-Path: %SNMP4J% > MANIFEST

echo Packaging JAR...

"%JAR%" cvfm SnmpHelper.jar MANIFEST *.class

echo Cleaning up...

del MANIFEST
del *.class

echo All done.
