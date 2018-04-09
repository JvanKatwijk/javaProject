#
sudo gcc -shared -fPIC -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux airspy-handler.cpp airspyDevice.cpp -o libairspy-wrapper.so -lstdc++ -lairspy

