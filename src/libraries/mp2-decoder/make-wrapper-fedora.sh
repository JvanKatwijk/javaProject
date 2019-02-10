#
sudo gcc -O3 -shared -fPIC -I /usr/lib/jvm/java-1.8.0-openjdk/include -I /usr/lib/jvm/java-1.8.0-openjdk/include/linux -lstdc++ kjmp2.cpp -o libmp2-wrapper.so

