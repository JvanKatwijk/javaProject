#
sudo gcc -O3 -shared -fPIC -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux -lstdc++ kjmp2.cpp -o libmp2-wrapper.so

