#
sudo g++ -shared -fPIC -I /home/jan/jdk1.8.0_151/include -I /home/jan/jdk1.8.0_151/include/linux airspy-handler.cpp airspyDevice.cpp -o libairspy-wrapper.so -lstdc++ -lairspy

