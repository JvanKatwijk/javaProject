#
sudo gcc -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux sdrplayDevice.cpp  -o libsdrplay-wrapper.so -lstdc++ -lmirsdrapi-rsp

