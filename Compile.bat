cd src
"C:\Program Files\Java\jdk1.8.0_25\bin"\javac -cp ../lib/javax.mail.jar;../lib/sigar.jar;../lib/jna.jar;../lib/jna-plat.jar;. com/ea/rserver/*.java
"C:\Program Files\Java\jdk1.8.0_25\bin"\jar cf rserver.jar com/ea/rserver/*.class
cd ..
cd ..
mkdir bin
xcopy /Y RServer\lib\* bin\* 
xcopy /Y RServer\src\*.jar bin\*.jar 
pause 
