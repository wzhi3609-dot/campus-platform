$env:JAVA_HOME = "$env:USERPROFILE\.jdks\graalvm-jdk-21.0.7"
$logFile = "C:\Users\QQ284\IdeaProjects\campus-platform\backend\backend.log"
& "$env:JAVA_HOME\bin\java" -jar "C:\Users\QQ284\IdeaProjects\campus-platform\backend\target\campus-platform-1.0.0.jar" *>> $logFile
