#!/bin/bash
# Genereer een self-signed key in een Java Keystore

PASSWD=d

if [ -z "$1" ]
then
	PASSWD=`head /dev/urandom | tr -dc A-Za-z0-9 | head -c 16 ;`
else
	PASSWD="$1"
fi

echo "$PASSWD"

keytool -genkey -keyalg RSA -alias selfsigned -keystore ~/.mfsrv/keystore.jks -storepass "$PASSWD" -validity 360 -keysize 4096
