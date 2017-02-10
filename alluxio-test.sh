#!/usr/bin/env bash
java -cp target/testAlluxio-1.0.0-SNAPSHOT.jar:target/alluxio-core-client-1.5.0-SNAPSHOT.jar alluxio.test.TestAlluxio "$@"
