#!/bin/sh
# Lists all managed connections. This also includes wifi and non-wifi connections.
# The application must manually filter the connection types.
# The result is saved in tmp/network_connections under the Interbot home directory.

CONNECTION_LIST_FILE="$HOME/interbot/tmp/network_connections"
cd /etc/NetworkManager/system-connections/
ls > $CONNECTION_LIST_FILE
