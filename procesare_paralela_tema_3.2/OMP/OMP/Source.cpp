#include <iostream>
#include <vector>
#include <thread>
#include <atomic>
#include <limits>
#include <algorithm>
#include <omp.h>

using namespace std;

static inline bool esteMaiMic(int a, int b) {
    int rez = 0;

    for (int i = 1; i <= 100; i++) {
        float temp = (float)a / (float)i;
        rez += (int)temp;
    }

    return (a <= b);
}

int getMinimSecvential(const vector<int>& valori) {
    int minim = valori[0];
    for (size_t i = 1; i < valori.size(); i++) {
        int v = valori[i];
        if (esteMaiMic(v, minim)) minim = v;
    }
    return minim;
}

int getMinimThreads(const vector<int>& valori, unsigned nrThreads) {
    if (valori.empty()) return std::numeric_limits<int>::max();
    if (nrThreads == 0) nrThreads = 1;

    atomic<size_t> nextIdx(0);
    vector<int> localMin(nrThreads, std::numeric_limits<int>::max());
    vector<thread> workers;
    workers.reserve(nrThreads);

    auto worker = [&](unsigned tid) {
        int myMin = std::numeric_limits<int>::max();

        const size_t CHUNK = 256;

        while (true) {
            size_t start = nextIdx.fetch_add(CHUNK, std::memory_order_relaxed);
            if (start >= valori.size()) break;

            size_t end = std::min(start + CHUNK, valori.size());
            for (size_t i = start; i < end; i++) {
                int v = valori[i];
                if (esteMaiMic(v, myMin)) myMin = v;
            }
        }

        localMin[tid] = myMin;
        };

    for (unsigned t = 0; t < nrThreads; t++) {
        workers.emplace_back(worker, t);
    }
    for (auto& th : workers) th.join();

    int globalMin = localMin[0];
    for (unsigned t = 1; t < nrThreads; t++) {
        if (esteMaiMic(localMin[t], globalMin)) globalMin = localMin[t];
    }
    return globalMin;
}

int getMinimOmpSharedLock(const vector<int>& valori) {
    if (valori.empty()) return std::numeric_limits<int>::max();

    int globalMin = valori[0];
    omp_lock_t lockMin;
    omp_init_lock(&lockMin);

#pragma omp parallel shared(globalMin, lockMin)
    {
#pragma omp for schedule(dynamic, 256)
        for (long long i = 0; i < (long long)valori.size(); i++) {
            int v = valori[(size_t)i];

            int snapshot = globalMin;
            if (esteMaiMic(v, snapshot)) {
                omp_set_lock(&lockMin);
                if (esteMaiMic(v, globalMin)) globalMin = v;
                omp_unset_lock(&lockMin);
            }
        }
    }

    omp_destroy_lock(&lockMin);
    return globalMin;
}

int getMinimOmpPrivateLocal(const vector<int>& valori) {
    if (valori.empty()) return std::numeric_limits<int>::max();

    int globalMin = valori[0];

#pragma omp parallel shared(globalMin)
    {
        int local = std::numeric_limits<int>::max();

#pragma omp for schedule(dynamic, 256) nowait
        for (long long i = 0; i < (long long)valori.size(); i++) {
            int v = valori[(size_t)i];
            if (esteMaiMic(v, local)) local = v;
        }

#pragma omp critical
        {
            if (esteMaiMic(local, globalMin)) globalMin = local;
        }
    }

    return globalMin;
}

static void benchmark(const char* label, int (*fn)(const vector<int>&), const vector<int>& v) {
    double t0 = omp_get_wtime();
    int m = fn(v);
    double t1 = omp_get_wtime();
    cout << "\n" << label << " | Minim=" << m << " | Timp=" << (t1 - t0) << " s";
}

int main() {
    int nrProcesoare = omp_get_num_procs();
    cout << "Nr procesoare: " << nrProcesoare << "\n";

    omp_set_num_threads(nrProcesoare);

    int nrElemente = (int)1e8;
    vector<int> valori(nrElemente);
    vector<int> valoriSortateDesc(nrElemente);

    cout << "Se genereaza valorile\n";
    srand(1000);
    for (int i = 0; i < nrElemente; i++) {
        valori[i] = rand();
        valoriSortateDesc[i] = nrElemente - i;
    }

    cout << "Start teste\n";

    benchmark("SEQ", getMinimSecvential, valori);

    {
        unsigned nt = (unsigned)max(1, nrProcesoare);
        auto fnThreads = [&](const vector<int>& x) -> int { return getMinimThreads(x, nt); };

        double t0 = omp_get_wtime();
        int m = fnThreads(valori);
        double t1 = omp_get_wtime();
        cout << "\nC++11 threads (" << nt << ") | Minim=" << m << " | Timp=" << (t1 - t0) << " s";
    }

    benchmark("OMP shared+lock", getMinimOmpSharedLock, valori);
    benchmark("OMP private local", getMinimOmpPrivateLocal, valori);

    cout << "\nStart test:\n";

    benchmark("SEQ", getMinimSecvential, valoriSortateDesc);

    {
        unsigned nt = (unsigned)max(1, nrProcesoare);
        auto fnThreads = [&](const vector<int>& x) -> int { return getMinimThreads(x, nt); };

        double t0 = omp_get_wtime();
        int m = fnThreads(valoriSortateDesc);
        double t1 = omp_get_wtime();
        cout << "\nC++11 threads (" << nt << ") | Minim=" << m << " | Timp=" << (t1 - t0) << " s";
    }

    benchmark("OMP shared+lock", getMinimOmpSharedLock, valoriSortateDesc);
    benchmark("OMP private local", getMinimOmpPrivateLocal, valoriSortateDesc);

    cout << "\n";
    return 0;
}