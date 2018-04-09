
 sudo gcc -shared -fPIC -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux -lstdc++ -lairspy airspy-handler.cpp airspyDevice.cpp -o libairspy-wrapper.so

