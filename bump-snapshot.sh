#!/bin/sh
set -e

echo "Updating version to next snapshot"
mvn versions:set -DnextSnapshot -DgenerateBackupPoms=false