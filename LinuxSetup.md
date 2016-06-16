Set up a AWS AMI Machine

Once complete, browse to http://machineurl/RServer

sudo yum -y install git
sudo yum -y install httpd
sudo service httpd start 
sudo yum -y install php php-mysql
sudo yum -y install php-ldap.x86_64
sudo service httpd restart
sudo rpm -Uvh http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
sudo yum -y install R 
sudo yum -y install libcurl-devel
sudo yum -y install unixODBC-devel
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u60-b27/jdk-8u60-linux-x64.rpm" 
sudo yum -y localinstall jdk-8u60-linux-x64.rpm
export JAVA_HOME=/usr/java/jdk1.8.0_60/
export JRE_HOME=/usr/java/jdk1.8.0_60/jre/
export PATH=$PATH:/usr/java/jdk1.8.0_60/bin:/usr/java/jdk1.8.0_60/jre/bin 
sudo R CMD javareconf
sudo yum -y update
sudo yum -y install git
curl -sSL https://s3.amazonaws.com/download.fpcomplete.com/centos/6/fpco.repo | sudo tee /etc/yum.repos.d/fpco.repo
sudo yum -y install stack
git clone https://github.com/jgm/pandoc
cd pandoc
git submodule update --init
stack setup 
stack install
cd ..  
export PATH=$PATH:/home/ec2-user/.local/bin
sudo ln -s /home/ec2-user/.local/bin/pandoc /usr/bin/pandoc

wget https://github.com/bgweber/RServer/raw/master/RServ/RServ.tar.gz
tar xzf RServ.tar.gz 
cd RServ 
sudo nohup ./run.sh & 
