#
sudo g++ -shared -fPIC -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux -lstdc++ aac-wrapper.cpp -o libfaad-wrapper.so -lfaad

