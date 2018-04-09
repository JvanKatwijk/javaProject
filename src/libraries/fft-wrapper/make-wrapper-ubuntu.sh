#
sudo gcc -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux -lstdc++ fftHandler.cpp -o libfft-wrapper.so -lfftw3f

