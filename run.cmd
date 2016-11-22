@echo off
IF %JAVA_HOME%=="" (
java -jar webinloop.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
) ELSE (
%JAVA_HOME%\bin\java -jar webinloop.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
)
