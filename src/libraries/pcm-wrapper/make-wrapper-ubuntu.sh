#
sudo g++ -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux pa-writer.cpp  utils_PCMwrapper.cpp -o lib-pcmwrapper.so -lstdc++ -lportaudio

