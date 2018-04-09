#
sudo gcc -std=c++11 -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux rtlsdr-handler.cpp rtlsdrDevice.cpp -o librtlsdr-wrapper.so -lstdc++ -lrtlsdr

