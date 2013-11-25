#!/usr/bin/env bash

# RabbitMQ APT-Repository hinzufügen
wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
apt-key add rabbitmq-signing-key-public.asc
rm rabbitmq-signing-key-public.asc
echo "deb http://www.rabbitmq.com/debian/ testing main" >> /etc/apt/sources.list

apt-get update

# Allgemeine Pakete installieren
apt-get install -y unzip vim git

# Java installieren
apt-get install -y openjdk-7-jdk openjdk-7-source openjdk-7-demo openjdk-7-doc openjdk-7-jre-headless openjdk-7-jre-lib 

# Apache installieren
apt-get install -y apache2

# Tomcat 7 installieren(256MB max heap statt 128MB konfigurieren)
apt-get install -y tomcat7 tomcat7-admin
sed -e 's/128/256/g' -i /etc/default/tomcat7
service tomcat7 restart

# RabbitMQ installieren und Management- und Stomp-Plugins aktivieren
apt-get install -y rabbitmq-server
cat >> /etc/profile <<EOF
export PATH=/usr/lib/rabbitmq/bin:$PATH
EOF
source /etc/profile
rabbitmq-plugins enable rabbitmq_management rabbitmq_stomp
invoke-rc.d rabbitmq-server stop
invoke-rc.d rabbitmq-server start

# Merkur-Server deployen
echo "Deploye Merkur-Server Webapplikation"
cd /vagrant/dist
unzip -d /var/lib/tomcat7/webapps merkur.zip merkur-server-0.1.war

# Merkur-Client deployen
echo "Deploye Merkur-Client Webapplikation"
unzip -d /home/vagrant/ merkur.zip merkur-client.zip
cd /home/vagrant
unzip merkur-client.zip
rm merkur-client.zip
rm -rf /var/www
ln -fs /home/vagrant/dist /var/www

echo "Fertig!"