
round1: round1.o
	ld -macosx_version_min 12.0.0 -o round1 round1.o -lSystem -syslibroot `xcrun -sdk macosx --show-sdk-path` -e _start -arch arm64 

round1.o: round1.s
	as -o round1.o round1.s
