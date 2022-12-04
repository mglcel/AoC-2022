#include <fstream>
#include <regex>
#include <iostream>

using namespace std;

int main () {
    ifstream file("input.txt");
    if (file.is_open()) {
        int total1 = 0, total2 = 0;
        for (string line; getline(file, line);) {
            regex e ("^(\\d+)-(\\d+),(\\d+)-(\\d+)");
            smatch m;
            regex_search (line, m, e);

            int s[4] = { stoi(m[1]), stoi(m[2]), stoi(m[3]), stoi(m[4]) };
            if ( ( (s[2] >= s[0]) && (s[3] <= s[1]) ) || ( (s[0] >= s[2]) && (s[1] <= s[3]) ) ) {
                total1++; // there is full overlap
                total2++;
            } else if ( ((s[0] <= s[2]) && (s[1] >= s[2])) || ((s[0] > s[2]) && (s[3] >= s[0])) ) {
                total2++; // there is simple overlap
            }
        }
        file.close();
        cout << "total1: " << total1 << endl;
        cout << "total2: " << total2 << endl;
    }
}