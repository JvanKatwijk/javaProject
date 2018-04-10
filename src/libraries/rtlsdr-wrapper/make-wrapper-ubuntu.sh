#
sudo gcc -std=c++11 -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux rtlsdr-handler.cpp rtlsdrDevice.cpp -o librtlsdr-wrapper.so -lstdc++ -lrtlsdr

