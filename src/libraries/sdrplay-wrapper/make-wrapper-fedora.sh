#
sudo gcc -std=c++11 -shared -fPIC  -O3 -I /home/jan/jdk1.8.0_151/include -I /home/jan/jdk1.8.0_151/include/linux sdrplayDevice.cpp sdrplay-handler.cpp  -o libsdrplay-wrapper.so -lstdc++ -lmirsdrapi-rsp

