# RServer Installation 

First, install the required software listed below. 
<br>RServer has only been tested on Windows 7 (64-bit).

### Download files: 

\> mkdir C:\RServerGit
<br>\> cd C:\RServerGit
<br>\> git clone https://github.com/bgweber/RServer

**Build:**

\> C:\RServerGit\RServer\Compile.bat 

**Run:**

\> C:\RServerGit\bin\Run.bat 

**Test:** [http://localhost/RServer](http://localhost/RServer) 

## Required Software

Git: https://git-scm.com/downloads 
<br>Java (64-bit JDK): http://www.oracle.com/technetwork/java/javase/downloads/ 
<br>R: https://cran.r-project.org/bin/windows/base/
<br>Visual 2012 C++ Redistributable: https://www.microsoft.com/en-us/download/details.aspx?id=30679 
<br>WAMP: https://sourceforge.net/projects/wampserver/ 

**For RMarkdown Reports**

Pandoc: https://github.com/jgm/pandoc/releases/latest  
Miktek: http://miktex.org/download      


## Setting Up Java

You'll need to add Java to the system path so that the javac and jar commands work. 

Open: Control Panel -> System and Security -> System
Select: Advanced System settings -> Environment Variables -> System Variables 

Append your java location to the end, For example ";C:\Program Files\Java\jdk1.8.0_77\bin"

To Test, run “CMD” then type javac at the console.

