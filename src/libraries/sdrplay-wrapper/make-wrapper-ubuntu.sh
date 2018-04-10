#
sudo gcc -std=c++11 -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux sdrplayDevice.cpp sdrplay-handler.cpp  -o libsdrplay-wrapper.so -lstdc++ -lmirsdrapi-rsp

