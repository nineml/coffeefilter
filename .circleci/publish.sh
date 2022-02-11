#!/bin/bash

if [ -z "$CIRCLE_TAG" ]; then
    echo "Only tagged commits are published"
    exit 0
fi

if [ -z "$GPGKEYURI" ]; then
    echo "Environment not configured for publishing"
    exit 1
fi

curl -o secret.gpg $GPGKEYURI
./gradlew -PsonatypeUsername="$SONATYPEUSER" -PsonatypePassword="$SONATYPEPASS" \
          -Psigning.keyId="$SIGNKEY" -Psigning.password="$SIGNPSW" \
          -Psigning.secretKeyRingFile=./secret.gpg \
          publish
rm -f secret.gpg
