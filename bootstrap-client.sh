#!/usr/bin/env bash

apt-get update

# Allgemeine Pakete installieren
apt-get install -y unzip vim

# Java installieren
apt-get install -y openjdk-7-jdk openjdk-7-source openjdk-7-demo openjdk-7-doc openjdk-7-jre-headless openjdk-7-jre-lib 

# Merkur-Generator deployen
echo "Deploye Merkur-Generator Applikation"
cd /home/vagrant
unzip /vagrant/dist/merkur.zip merkur-generator-0.1.zip
unzip merkur-generator-0.1.zip
rm merkur-generator-0.1.zip

# 2 Instanzen erstellen
cp -rf merkur-generator-0.1 merkur-gena
cp -rf merkur-generator-0.1 merkur-genb
rm -r merkur-generator-0.1
HOSTNAME=`hostname`
jar -xf merkur-gena/lib/merkur-generator-0.1.jar logback.xml
sed -e "s/Generator/${HOSTNAME}.gena/g" -i logback.xml
jar -uf merkur-gena/lib/merkur-generator-0.1.jar logback.xml
jar -xf merkur-genb/lib/merkur-generator-0.1.jar logback.xml
sed -e "s/Generator/${HOSTNAME}.genb/g" -i logback.xml
jar -uf merkur-genb/lib/merkur-generator-0.1.jar logback.xml
rm logback.xml
chown -R vagrant:vagrant merkur-gen*

sed -e '/exit 0/d' -i /etc/rc.local
cat >> /etc/rc.local <<EOF
su - vagrant -c "/home/vagrant/merkur-gena/bin/merkur-generator --id=gena --delay=500 &"
su - vagrant -c "/home/vagrant/merkur-genb/bin/merkur-generator --id=genb --delay=500 &"
exit 0
EOF
