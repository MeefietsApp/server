#!/bin/bash
# Genereer een gratis key + JKS m.b.v. LetsEncrypt Certbot.
# Waarschuwing: stop (web) services op 80,443/tcp eerst!
# -- (c) 2018 hypothermic <admin@hypothermic.nl>

FQDN=sub.domain.tld
DIR="/home/user/.mfsrv/"
JKS="keystore.jks"

if ! [ -z "$1" ]
then
    if [ ${#1} -gt 5 ]; then
        FQDN="$1"
    else 
        echo "Argument 1 is too short"
        exit 1
    fi
fi

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

if [ -x "$(command -v fuser)" ]; then
    fuser -k 80/tcp >/dev/null 2>&1
else
    kill -9 $(lsof -t -i:3000 -sTCP:LISTEN) >/dev/null 2>&1
fi

echo "Running certbot for $FQDN..."

certbot certonly --standalone -d "$FQDN" --tls-sni-01-port 5001 --http-01-port 80

if [ ! -d "$DIR" ]; then
    mkdir -p "$DIR"
fi

cat "/etc/letsencrypt/live/$FQDN/*.pem" > /tmp/fullcert.pem

openssl pkcs12 -export -out /tmp/fullchain.pkcs12 -in /tmp/fullcert.pem

keytool -genkey -keyalg RSA -alias primarykey -keystore "$DIR$JKS"

keytool -delete -alias primarykey -keystore "$DIR$JKS"

keytool -v -importkeystore -srckeystore /tmp/fullchain.pkcs12 -destkeystore "$DIR$JKS" -deststoretype JKS