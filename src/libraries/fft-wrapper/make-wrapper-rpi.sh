
 sudo gcc -shared -fPIC  -O3 -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux -lstdc++ -lfftw3f fftHandler.cpp -o libfft-wrapper.so

