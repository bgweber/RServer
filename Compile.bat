cd src
javac -cp ../lib/javax.mail.jar;../lib/sigar.jar;../lib/jna.jar;../lib/jna-plat.jar;. com/ea/rserver/*.java
jar cf rserver.jar com/ea/rserver/*.class
cd ..
cd ..
mkdir bin
xcopy /Y RServer\lib\* bin\* 
xcopy /Y RServer\src\*.jar bin\*.jar 
