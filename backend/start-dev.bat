@echo off
set JAVA_HOME=%USERPROFILE%\.jdks\graalvm-jdk-21.0.7
"%JAVA_HOME%\bin\java" -jar target\campus-platform-1.0.0.jar --spring.profiles.active=dev
pause
