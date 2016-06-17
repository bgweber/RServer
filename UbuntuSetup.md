sudo apt-get install git

sudo apt-get install apache2 mysql-server php5-mysql php5 libapache2-mod-php5 php5-mcrypt

sudo apt-get install r-base-core 
 
sudo apt-get install unixODBC-dev

wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u60-b27/jdk-8u60-linux-x64.tar.gz" 

tar xvzf jdk-8u60-linux-x64.tar.gz

sudo mv jdk1.8.0_60 /usr/java/

export JAVA_HOME=/usr/java/jdk1.8.0_60/ 

export JRE_HOME=/usr/java/jdk1.8.0_60/jre/ 

export PATH=$PATH:/usr/java/jdk1.8.0_60/bin:/usr/java/jdk1.8.0_60/jre/bin 

sudo R CMD javareconf 

sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 575159689BEFB442

echo 'deb http://download.fpcomplete.com/ubuntu trusty main'|sudo tee /etc/apt/sources.list.d/fpco.list

sudo apt-get update && sudo apt-get install stack -y

git clone https://github.com/jgm/pandoc 

cd pandoc 

git submodule update --init 

stack setup 

stack install 

cd ..

cp .stack-work/install/x86_64-linux/lts-6.1/7.10.3/bin/pandoc /usr/bin

wget https://github.com/bgweber/RServer/raw/master/RServ/RServ.tar.gz 

tar xzf RServ.tar.gz 

cd RServ 

sudo nohup ./run.sh &
