#
sudo gcc -shared -fPIC -I /usr/lib/jvm/java-8-openjdk-amd64/include -I /usr/lib/jvm/java-8-openjdk-amd64/include/linux -lstdc++ aac-wrapper.cpp -o libfaad-wrapper.so -lfaad

