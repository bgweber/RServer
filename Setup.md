# RServer Installation 

First, install the required software listed below. 
<br>RServer has only been tested on Windows 7 (64-bit).

### Download files: 

\> mkdir C:\RServerGit
<br>\> cd C:\RServerGit
<br>\> git clone https://github.com/bgweber/RServer

**Build:**

Set up your [Java Path](https://github.com/bgweber/RServer/blob/master/Setup.md#setting-up-java) 

\> C:\RServerGit\RServer\Compile.bat 

**Run:**

On windows 8, run as administrator. 

\> C:\RServerGit\bin\Run.bat 

**Test:** [http://localhost/RServer](http://localhost/RServer) 

## Required Software

Git: https://git-scm.com/downloads 
<br>Java (64-bit JDK): http://www.oracle.com/technetwork/java/javase/downloads/ 
<br>R: https://cran.r-project.org/bin/windows/base/
<br>Pandoc: https://github.com/jgm/pandoc/releases/latest  
Miktek: http://miktex.org/download      
Visual 2012/2015 C++ Redistributables for WAMP (both 32-bit and 64-bit): 
- https://www.microsoft.com/en-us/download/details.aspx?id=30679 
- https://www.microsoft.com/en-us/download/confirmation.aspx?id=48145 

WAMP: https://sourceforge.net/projects/wampserver/ 


## Setting Up Java

You'll need to add Java to the system path so that the javac and jar commands work. 

Open: Control Panel -> System and Security -> System
<br>Select: Advanced System settings -> Environment Variables -> System Variables 

Append your java location to the end. For example ";C:\Program Files\Java\jdk1.8.0_77\bin"

To Test, run “CMD” then type javac at the console.

![alt tag](https://github.com/bgweber/RServer/blob/master/JavaPath.png)

