#
sudo gcc -std=c++11 -std=gnu++11 -shared -fPIC -I /usr/lib/jvm/java-8-oracle/include -I /usr/lib/jvm/java-8-oracle/include/linux *.cpp -lstdc++ -o libwav-wrapper.so -lsndfile

