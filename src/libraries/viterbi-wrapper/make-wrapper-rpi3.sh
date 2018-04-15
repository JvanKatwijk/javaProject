#
sudo gcc -shared -fPIC -O3 -DNEON_AVAILABLE -mcpu=cortex-a53 -mfloat-abi=hard -mfpu=neon-fp-armv8 -mneon-for-64bits -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux spiral-neon.c viterbi-768.cpp viterbi-wrapper.cpp -o libviterbi-wrapper.so -lstdc++

