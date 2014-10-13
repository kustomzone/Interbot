#!/bin/sh
# Executes a package supplied by the server.
# First downloads and unpacks the package.
# Next the package run.sh script is executed.
# $1: the URL of the package (supplied by server).
# $2: the name of the package (supplied by server).

LOG_FILE="$HOME/interbot/logs/execute.log"
DOWNLOADS_DIR="$HOME/interbot/downloads"
echo '******************************' >> $LOG_FILE
date >> $LOG_FILE
echo $1 >> $LOG_FILE
cd $DOWNLOADS_DIR
wget -q $1
gunzip $2.tar.gz
tar xf $2.tar
rm $2.tar
$2/run.sh >> $LOG_FILE
rm -rf $2
