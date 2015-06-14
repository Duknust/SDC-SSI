#!/bin/bash
java -Dconf=/etc/init.d/myintegrit.conf -cp bin/ daemonTh.Main  <&- &pid=$!
echo ${pid} > /home/user/myIntegrit.pid
#echo "PID=$pid"