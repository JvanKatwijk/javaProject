#
sudo gcc -std=c++11 -shared -fPIC  -O3 -I /usr/lib/jvm/java-1.8.0-openjdk/include -I /usr/lib/jvm/java-1.8.0-openjdk/include/linux rtlsdr-handler.cpp rtlsdrDevice.cpp -o librtlsdr-wrapper.so -lstdc++ -lrtlsdr

