Set up a AWS AMI Machine

Once complete, browse to http://machineurl/RServer

sudo yum -y install git
<br>sudo yum -y install httpd
<br>sudo service httpd start 
<br>sudo yum -y install php php-mysql
<br>sudo yum -y install php-ldap.x86_64
<br>sudo service httpd restart
<br>sudo rpm -Uvh http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
<br>sudo yum -y install R 
<br>sudo yum -y install libcurl-devel
<br>sudo yum -y install unixODBC-devel
<br>wget --no-check-certificate -c --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u151-b12/e758a0de34e24606bca991d704f6dcbf/jdk-8u151-linux-x64.tar.gz
<br>sudo yum -y localinstall jdk-8u151-linux-x64.rpm
<br>export JAVA_HOME=/usr/java/jdk1.8.0_151/
<br>export JRE_HOME=/usr/java/jdk1.8.0_151/jre/
<br>export PATH=$PATH:/usr/java/jdk1.8.0_151/bin:/usr/java/jdk1.8.0_151/jre/bin 
<br>sudo R CMD javareconf
<br>sudo yum -y update
<br>sudo yum -y install git
<br>curl -sSL https://s3.amazonaws.com/download.fpcomplete.com/centos/6/fpco.repo | sudo tee /etc/yum.repos.d/fpco.repo
<br>sudo yum -y install stack
<br>git clone https://github.com/jgm/pandoc
<br>cd pandoc
<br>git submodule update --init
<br>stack setup 
<br>stack install
<br>cd ..  
<br>export PATH=$PATH:/home/ec2-user/.local/bin
<br>sudo ln -s /home/ec2-user/.local/bin/pandoc /usr/bin/pandoc
<br>
<br>wget https://github.com/bgweber/RServer/raw/master/RServ/RServ.tar.gz
<br>tar xzf RServ.tar.gz 
<br>cd RServ 
<br>sudo nohup ./run.sh & 
