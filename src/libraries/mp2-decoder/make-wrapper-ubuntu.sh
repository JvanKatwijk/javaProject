#
sudo gcc -shared -fPIC -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux -lstdc++ kjmp2.cpp -o libmp2-wrapper.so

