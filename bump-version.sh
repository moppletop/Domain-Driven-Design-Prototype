#!/bin/sh
set -e

echo "Updating version to $1"
mvn versions:set -DnewVersion=$1 -DgenerateBackupPoms=false