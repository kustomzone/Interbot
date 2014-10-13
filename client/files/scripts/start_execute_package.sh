#!/bin/sh
# Starts execution of a package supplied by the server.
# $1: the URL of the package (supplied by server).
# $2: the name of the package (supplied by server).

"$HOME"/interbot/scripts/execute_package.sh "$1" "$2" &
