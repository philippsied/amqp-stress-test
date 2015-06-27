#!/bin/bash

# Run this script on a fresh Ubuntu Server 14.04.2 LTS, amd64
# Linux Kernel 3.16.0-30-generic instance
# 
# This script requires enabled root access, so setup a root password, unless it already exists.
#  'sudo passwd root' 
# 

ethinterface=eth0
ipconfigpath=/etc/network/interfaces
ipaddress=192.168.178.153
ipsuffix=24

dlurl=https://www.rabbitmq.com/releases/rabbitmq-server/v3.5.1
debfile=rabbitmq-server_3.5.1-1_all.deb

rbusername=testc
rbuserpw=testp
rbadminname=admin
rbadminpw=Passw0rd#1
rbguestpw=JvMw2dL_u
rbmqport=5672
rbmqmngtport=15672


# Ensure to have root access
if [ "$(whoami)" != "root" ]; then
   echo "Please run this as root 'sudo $0'."
   exit 1
fi

echo "Start setting up test environment"

# Set static ip address
su -c "echo -e '\n# Set static private ip address' >> $ipconfigpath"
su -c "echo 'iface $ethinterface inet static' >> $ipconfigpath"
su -c "echo 'address $ipaddress/$ipsuffix' >> $ipconfigpath"
ifdown -a && ifup -a


# Get deb-package of rabbitmq
wget $dlurl/$debfile

# Check for return code of wget
if [ $? != 0 ]; then
   echo "Error when executing command: 'wget'"
   exit 1
fi

# Renew package cache
apt-get -q update

# Install deb-package
dpkg -i $debfile
apt-get -fqy install

# Create new user for tests
rabbitmqctl add_user $rbusername $rbuserpw
rabbitmqctl set_permissions $rbusername ".*" ".*" ".*"

# Create new administrator
rabbitmqctl add_user $rbadminname $rbadminpw
rabbitmqctl set_user_tags $rbadminname administrator
rabbitmqctl set_permissions $rbadminname ".*" ".*" ".*"

# Degrade guest
rabbitmqctl set_user_tags guest
rabbitmqctl change_password guest $rbguestpw

# Enable Management webinterface
rabbitmq-plugins enable rabbitmq_management

# Install glances
apt-get install -qy python-pip build-essential python-dev
pip install -q glances bottle

echo -e "\n\n--------------------------------------------------"
echo -e "Setup Done\n"
echo "Server is listening on: $ipaddress:$rbmqport"
echo "Management interface is available on: $ipaddress:$rbmqmngtport"
echo "RabbitMQ adminstrator: $rbadminname|$rbadminpw"
echo "RabbitMQ user: $rbusername|$rbuserpw"
echo "Glances can be locally run with: 'sudo glances'"


exit 0

