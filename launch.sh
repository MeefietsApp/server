#!/usr/bin/env bash
/usr/bin/script -q -c "/usr/bin/screen ${*}" /dev/null
/usr/bin/setfacl -R -m u:mfsrv:rwx /home/mfsrv/.mfsrv/
/usr/bin/nohup java -jar mfsrv-*.jar -Xms16M -Xmn32M -Xmx768M </dev/null >/dev/null 2>&1 &
