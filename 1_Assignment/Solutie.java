import java.util.*;

public final class Solutie {

    /**
     * @param listaStudenti datele tuturor studentilor
     * @return varsta medie a studentilor inregistrati la acest curs
     */
    public double calculSecventialMediaVarsteiStudentilorInregistrati(final Student[] listaStudenti) {
        double suma = 0.0;
        long counter = 0L;
        for (Student student : listaStudenti) {
            if (student.verificaEsteInregistrat()) {
                suma = suma + student.getVarsta();
                counter++;
            }
        }
        if(counter == 0){
            return 0.0;
        }
        else{
            return suma/(double) counter;
        }
    }



    /**
     * @param listaStudenti datele tuturor studentilor
     * @return varsta medie a studentilor inregistrati la acest curs
     */
    public double calculParalel_MediaVarsteiStudentilorInregistrati(final Student[] listaStudenti) {
        int numberOfThreads = Math.max(2, Runtime.getRuntime().availableProcessors());
        Thread[] threads = new Thread[numberOfThreads];
        double[] partialSums = new double[numberOfThreads];
        long[] partialCounts = new long[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            int threadIdx = i;
            int start = (int)((long)listaStudenti.length * threadIdx / numberOfThreads);
            int end   = (int)((long)listaStudenti.length * (threadIdx + 1) / numberOfThreads);

            threads[i] = new Thread(() -> {
                double partialSum = 0.0;
                long partialCount = 0L;
                for (int j = start; j < end; j++) {
                    Student st = listaStudenti[j];
                    if (st.verificaEsteInregistrat()) {
                        partialSum += st.getVarsta();
                        partialCount++;
                    }
                }
                partialSums[threadIdx] = partialSum;
                partialCounts[threadIdx] = partialCount;
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        double totalSum = 0.0;
        long totalCount = 0L;
        for (int i = 0; i < numberOfThreads; i++) {
            totalSum += partialSums[i];
            totalCount += partialCounts[i];
        }

        if(totalCount == 0){
            return 0.0;
        }else{
            return totalSum/(double) totalCount;
        }
    }


    /**
     * @param studentArray datele tuturor studentilor
     * @return cel mai comun prenume
     */
    public String calculSecvential_CelMaiComunPrenumePentruStudentiiNeinregistrati(final Student[] studentArray) {
        HashMap<String, Integer> freq = new HashMap<>();
        for (Student student : studentArray) {
            if (!student.verificaEsteInregistrat()) {
                String prenume = student.getPrenume();
                freq.merge(prenume, 1, Integer::sum);
            }
        }

        String bestName = null;
        int bestCount = -1;
        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestName = entry.getKey();
            }
        }
        return bestName;
    }

    /**
     * @param listaStudenti datele tuturor studentilor
     * @return cel mai comun prenume
     */
    public String calculParalel_CelMaiComunPrenumePentruStudentiiNeinregistrati(final Student[] listaStudenti) {
        int numberOfThreads = Math.max(2, Runtime.getRuntime().availableProcessors());
        Thread[] threads = new Thread[numberOfThreads];

        @SuppressWarnings("unchecked")
        HashMap<String, Integer>[] partials = new HashMap[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            int threadIdx = i;
            int start = (int)((long)listaStudenti.length * threadIdx / numberOfThreads);
            int end   = (int)((long)listaStudenti.length * (threadIdx + 1) / numberOfThreads);

            threads[i] = new Thread(() -> {
                HashMap<String, Integer> local = new HashMap<>();
                for (int j = start; j < end; j++) {
                    Student student = listaStudenti[j];
                    if (!student.verificaEsteInregistrat()) {
                        String prenume = student.getPrenume();
                        local.merge(prenume, 1, Integer::sum);
                    }
                }
                partials[threadIdx] = local;
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        HashMap<String, Integer> merged = new HashMap<>();
        for (int i = 0; i < numberOfThreads; i++) {
            HashMap<String, Integer> loc = partials[i];
            if (loc == null) continue;
            for (Map.Entry<String, Integer> e : loc.entrySet()) {
                String key = e.getKey();
                merged.merge(key, e.getValue(), Integer::sum);
            }
        }

        String bestName = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : merged.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestName = entry.getKey();
            }
        }
        return bestName;
    }


    /**
     * @param listaStudenti Student data for the class.
     * @return Number of failed grades from students older than 20 years old.
     */
    public int calculSecvential_NumarulStudentiNepromovatiCuVarstaPeste20(final Student[] listaStudenti) {
        int counter = 0;
        for (Student student : listaStudenti) {
            if (student.getNota() < 5 && student.getVarsta() > 20.0) {
                counter++;
            }
        }
        return counter;
    }




    /**
     * @param studentArray Student data for the class.
     * @return Number of failed grades from students older than 20 years old.
     */
    public int calculParalel_NumarulStudentiNepromovatiCuVarstaPeste20(final Student[] studentArray) {
        int numberOfThreads = Math.max(2, Runtime.getRuntime().availableProcessors());
        Thread[] threads = new Thread[numberOfThreads];
        int[] partialCounts = new int[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            int threadIdx = i;
            int start = (int)((long)studentArray.length * threadIdx / numberOfThreads);
            int end   = (int)((long)studentArray.length * (threadIdx + 1) / numberOfThreads);

            threads[i] = new Thread(() -> {
                int local = 0;
                for (int j = start; j < end; j++) {
                    Student student = studentArray[j];
                    if (student.getNota() < 5 && student.getVarsta() > 20.0) {
                        local++;
                    }
                }
                partialCounts[threadIdx] = local;
            });
            threads[i].start();
        }

        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

        int total = 0;
        for (int i = 0; i < numberOfThreads; i++) {
            total += partialCounts[i];
        }




        return total;
    }
}