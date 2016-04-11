xcopy /F /Y ..\RServer\Update.bat ..\*.bat 
java -cp javax.mail.jar;sigar.jar;jna.jar;jna-plat.jar;rserver.jar com.ea.rserver.RServer 
