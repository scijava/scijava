#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/main/ci-build.sh

# NB: These two components must be pre-built before the project as a whole,
# because they are used as helpers during the project build itself.
mvn -Djavadoc.skip -pl scijava-taglets,scijava-ops-indexer -am clean install

sh ci-build.sh
