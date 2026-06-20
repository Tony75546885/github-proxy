#!/bin/sh
# Gradle wrapper script — downloads Gradle if not cached
set -e

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

DIRNAME=$(dirname "$0")
CLASSPATH="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"

# Determine the Java command to use
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Use the Gradle wrapper JAR if present, otherwise download
WRAPPER_JAR="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.14.2/gradle/wrapper/gradle-wrapper.jar"
    echo "Downloading Gradle wrapper JAR..."
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
    else
        echo "ERROR: Cannot download wrapper JAR. Install curl or wget." >&2
        exit 1
    fi
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain "$@"
