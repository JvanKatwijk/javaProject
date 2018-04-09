#
sudo gcc -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux pa-writer.cpp  utils_PCMwrapper.cpp -o lib-pcmwrapper.so -lstdc++ -lportaudio

