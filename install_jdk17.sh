#!/usr/bin/bash

if ! command -v sdk &> /dev/null; then
    curl -s "https://get.sdkman.io" | bash
    source "/usr/local/sdkman/bin/sdkman-init.sh"
fi

if ! command -v sdk &> /dev/null; then
    echo "SDKMAN! not initialized. Restart terminal and try again."
    exit 1
fi

JAVA_17_IDENTIFIER="17.0.13-tem"
JAVA_21_IDENTIFIER="21.0.9-tem"

echo "Install OpenJDK 17 ($JAVA_17_IDENTIFIER)? (y/n)"
read -r INSTALL_JAVA_17
if [[ "$INSTALL_JAVA_17" =~ ^[Yy]$ ]]; then
    if sdk install java "$JAVA_17_IDENTIFIER"; then
        sdk default java "$JAVA_17_IDENTIFIER"
        INSTALL_SUCCESS=true
    fi
fi

if [ "$INSTALL_SUCCESS" != true ]; then
    echo "Install OpenJDK 21 ($JAVA_21_IDENTIFIER)? (y/n)"
    read -r INSTALL_JAVA_21
    if [[ "$INSTALL_JAVA_21" =~ ^[Yy]$ ]]; then
        if sdk install java "$JAVA_21_IDENTIFIER"; then
            sdk default java "$JAVA_21_IDENTIFIER"
            INSTALL_SUCCESS=true
        fi
    fi
fi

if [ "$INSTALL_SUCCESS" = true ]; then
    java -version
    echo "JAVA_HOME: $JAVA_HOME"
else
    echo "No suitable JDK installed."
fi
