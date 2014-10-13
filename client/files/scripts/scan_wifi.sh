#!/bin/sh
# Executes a WiFi scan to identify nearby access points.
# The scan results are stored in the tmp/wifi_scan file under the Interbot home directory.

WIFI_SCAN_FILE="$HOME/interbot/tmp/wifi_scan"
nmcli dev wifi > $WIFI_SCAN_FILE
