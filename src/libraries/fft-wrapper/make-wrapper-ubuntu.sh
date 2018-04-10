#
sudo gcc -shared -fPIC  -O3 -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux -lstdc++ fftHandler.cpp -o libfft-wrapper.so -lfftw3f

