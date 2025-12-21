set -e

echo "=== Detecting Java ==="
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
echo "JAVA_HOME=$JAVA_HOME"
ls -la $JAVA_HOME/include/

echo "=== Preparing Gradle ==="
chmod +x ./gradlew

echo "=== Building JNI library (x86_64) ==="
export JAVA_HOME=$JAVA_HOME
./gradlew configureJNI_linux-x86_64 buildJNI_linux-x86_64 copyJNILib_linux-x86_64 --no-daemon

echo "=== Building JNI library (aarch64) ==="
export JAVA_HOME=$JAVA_HOME
./gradlew configureJNI_linux-aarch64 buildJNI_linux-aarch64 copyJNILib_linux-aarch64 --no-daemon

echo "=== Building Minecraft mod ==="
./gradlew build --no-daemon

echo "=== Verifying native libraries ==="
file build/resources/main/native/linux-x86_64/libdoomjni.so || true
file build/resources/main/native/linux-aarch64/libdoomjni.so || true

echo "=== Verifying JAR contents ==="
unzip -l build/libs/*.jar | grep "native/" || true
