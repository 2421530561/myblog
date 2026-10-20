#!/bin/sh
export JAVA_HOME='C:\Program Files\Java\jdk-21.0.12.1'
MH='C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0'
exec "$JAVA_HOME/bin/java.exe" -classpath "$MH\\boot\\plexus-classworlds-2.11.0.jar" \
  "-Dclassworlds.conf=$MH\\bin\\m2.conf" \
  "-Dmaven.home=$MH" \
  "-Dmaven.multiModuleProjectDirectory=D:\\uncompress\\myblog" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
