#!/bin/sh
# Creates a new WiFi connection.
# Only WPA security is supported.
# $1: the SSID of the WiFi network.
# $2: the password.

sudo /usr/share/checkbox/scripts/create_connection wifi -S wpa -K "$2" "$1"
