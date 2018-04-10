#
sudo g++ -shared -fPIC -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux airspy-handler.cpp airspyDevice.cpp -o libairspy-wrapper.so -lstdc++ -lairspy

