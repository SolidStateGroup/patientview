#!/usr/bin/env bash

#wget https://alioth.debian.org/scm/loggerhead/pkg-postgresql/postgresql-common/trunk/download/head:/apt.postgresql.org.s-20130224224205-px3qyst90b3xp8zj-1/apt.postgresql.org.sh
#chmod u+x apt.postgresql.org.sh

whoami

if [ ! -d /etc/postgresql/9.3 ]; then
  sudo /home/vagrant/fhirbase/apt.postgresql.org.sh
fi

sudo apt-get install -y curl build-essential git libpq-dev postgresql-9.3 postgresql-contrib-9.3 postgresql-server-dev-9.3

sudo su postgres -c 'createuser -s root'
sudo su postgres -c 'createuser -s vagrant'

sudo cp /home/vagrant/fhirbase/postgresql.conf /etc/postgresql/9.3/main/postgresql.conf
sudo cp /home/vagrant/fhirbase/pg_hba.conf /etc/postgresql/9.3/main/pg_hba.conf
sudo service postgresql restart

if [ ! -d /tmp/pgtap ]; then
  echo y | sudo perl -MCPAN -e 'install TAP::Parser::SourceHandler::pgTAP' || echo 'CPAN :( - hope it will work'
  git clone https://github.com/theory/pgtap.git /tmp/pgtap
  cd /tmp/pgtap
  make
  sudo make install
  sudo make installcheck
  sudo service postgresql restart
  echo y | sudo perl -MCPAN -e 'install TAP::Parser::SourceHandler::pgTAP' || echo 'CPAN :( - hope it will work'
fi

# only for vagrant
#sudo su -l vagrant -c 'cd /home/vagrant/fhirbase  && ./runme -c cfg/config.sh.vagrant test'
sudo su -l vagrant -c 'psql template1 -c "CREATE DATABASE fhirbase"'
sudo su -l vagrant -c 'cd /home/vagrant/fhirbase && ./runme -c cfg/config.sh.vagrant install fhirbase'
