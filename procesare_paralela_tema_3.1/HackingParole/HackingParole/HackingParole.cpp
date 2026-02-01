#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <algorithm>

#include <omp.h>
#include "sha1.h"

static inline void toLowerInPlace(std::string& s) {
    std::transform(s.begin(), s.end(), s.begin(),
        [](unsigned char c) { return (char)std::tolower(c); });
}

int main(int argc, char** argv) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0]
            << " <passwords_file> <target_hash_hex> [salt]\n"
            << "Ex: " << argv[0]
            << " 10-million-password-list-top-1000000.txt 5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8 parallel\n";
        return 2;
    }

    const std::string passwordsFile = argv[1];
    std::string targetHash = argv[2];
    toLowerInPlace(targetHash);

    const std::string salt = (argc >= 4) ? argv[3] : "parallel";

    std::ifstream fin(passwordsFile.c_str());
    if (!fin) {
        return 2;
    }

    std::vector<std::string> passwords;
    passwords.reserve(1000000);

    std::string line;
    while (std::getline(fin, line)) {
        if (!line.empty() && line.back() == '\r') line.pop_back();
        passwords.push_back(line);
    }
    fin.close();

    volatile int found = 0;
    long bestIndex = -1;
    std::string bestPassword;

    const long n = (long)passwords.size();


#pragma omp parallel
    {
        std::string candidate;
        std::string h1;
        std::string hFinal;

#pragma omp for schedule(dynamic, 2048)
        for (long i = 0; i < n; i++) {

#pragma omp flush(found)
            if (found) continue;

            // hashFinal = SHA1( SHA1(salt + parola) )
            candidate = salt;
            candidate += passwords[i];

            h1 = sha1(candidate);
            hFinal = sha1(h1);
            toLowerInPlace(hFinal);

            if (hFinal == targetHash) {
#pragma omp critical
                {
                    if (bestIndex == -1 || i < bestIndex) {
                        bestIndex = i;
                        bestPassword = passwords[i];
                        found = 1;
#pragma omp flush(found)
                    }
                }
            }
        }
    }

    if (bestIndex >= 0) {
        std::cout << "Gasit\n";
        std::cout << "Password: " << bestPassword << "\n";
        std::cout << "Index (0-based): " << bestIndex << "\n";
        std::cout << "Index (1-based): " << (bestIndex + 1) << "\n";
        return 0;
    }
    else {
        std::cout << "Nu exista\n";
        return 1;
    }
}
