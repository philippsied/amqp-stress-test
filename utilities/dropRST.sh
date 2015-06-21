#!/bin/bash

iptables=/sbin/iptables


function printhelp {
	echo -e "\n usage: $0 -i|r <interface> <dest_ipaddr> <dest_port>"
	echo -e " -i\tInstall the rule"
	echo -e " -r\tRemove the rule"
	echo -e "\n i.e. to install the rule to block outgoing TCP RST of eth0, send to 192.168.178.114:5672 use"
	echo -e "\n\t$0 -i eth0 192.168.178.114 5672\n"
}

if [ ! $# -eq 4 ]; then
	printhelp
	exit 1
fi

interface=$2
dipaddress=$3
dport=$4

while getopts "ir" opt; do
	case "$opt" in
	i)
		echo "$iptables -I OUTPUT -o $interface -p tcp -d $dipaddress --dport $dport --tcp-flags RST RST -j DROP"
		$iptables -I OUTPUT -o $interface -p tcp -d $dipaddress --dport $dport --tcp-flags RST RST -j DROP
		exit 0
		;;
	r)
		echo "$iptables -I OUTPUT -o $interface -p tcp -d $dipaddress --dport $dport --tcp-flags RST RST -j DROP"		
		$iptables -D OUTPUT -o $interface -p tcp -d $dipaddress --dport $dport --tcp-flags RST RST -j DROP
		exit 0
		;;
	*)	printhelp
		;;
	esac
done


