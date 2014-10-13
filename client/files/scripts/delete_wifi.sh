#!/bin/sh
# Deletes a WiFi connection created by add_wifi.sh.
# $1: the SSID of the WiFi network to delete.

sudo rm /etc/NetworkManager/system-connections/"$1"
