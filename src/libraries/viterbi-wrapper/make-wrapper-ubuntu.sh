#
sudo g++ -shared -fPIC -O3 -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux -lstdc++ spiral-no-sse.c  viterbi-768.cpp viterbi-wrapper.cpp -o libviterbi-wrapper.so

