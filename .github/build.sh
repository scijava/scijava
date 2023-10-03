#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-build.sh
mvn -Djavadoc.skip -pl scijava/scijava-taglets,scijava/scijava-ops-indexer -am clean install
sh ci-build.sh
