import java.util.Random;


public class TestSolutieTema {
	
    final static int REPETARI = 10;
    final static String[] listaPrenume = {"Alina", "Ionut", "Ion", "Alex", "Bianca", "Mihaela"};
    final static String[] listaNume = {"Popescu", "Ionescu", "Alexandru", "Tudor", "Avram", "Mihalache"};

    
    static int getNrProcesoare() {
        return Runtime.getRuntime().availableProcessors();
    }

    static Student[] generareDateStudenti() {
        final int NR_STUDENTI = (int) 3e7;
        final int NR_STUDENTI_CURS = (int) 1e6;

        Student[] studenti = new Student[NR_STUDENTI];
        Random r = new Random(123);

        for (int stud = 0; stud < NR_STUDENTI; stud++) {
            final String prenume = listaPrenume[r.nextInt(listaPrenume.length)];
            final String nume = listaNume[r.nextInt(listaNume.length)];
            final double varsta = r.nextDouble() * 100.0;
            final int nota = 1 + r.nextInt(10);
            final boolean esteInscrisLaCurs = (stud < NR_STUDENTI_CURS);

            studenti[stud] = new Student(prenume, nume, varsta, nota, esteInscrisLaCurs);
        }

        return studenti;
    }
	
    private static double calculMedieVarstaStudentiInscrisi(final int repetari) {
    	
        System.out.println("*********Test: calcul medie varsta studenti");
        
        final Student[] students = generareDateStudenti();
        final Solutie resurse = new Solutie();

        final double ref = resurse.calculSecventialMediaVarsteiStudentilorInregistrati(students);

        final long startSecvential = System.currentTimeMillis();
        for (int r = 0; r < repetari; r++) {
            resurse.calculSecventialMediaVarsteiStudentilorInregistrati(students);
        }
        final long finalSecvential = System.currentTimeMillis();
        
        System.out.println("Durata solutie secventiala: " + (finalSecvential-startSecvential));

        final double calc = resurse.calculParalel_MediaVarsteiStudentilorInregistrati(students);
        final double err = Math.abs(calc - ref);
        final String msg = "Expected " + ref + " but was " + calc + ", err = " + err;
        
        if(err < 1E-5)
        	System.out.println("Test passed");
        else
        	System.out.println("Test FAILED - " + msg);

        final long startParalel = System.currentTimeMillis();
        for (int r = 0; r < repetari; r++) {
            resurse.calculParalel_MediaVarsteiStudentilorInregistrati(students);
        }
        final long finalParalel = System.currentTimeMillis();
        
        System.out.println("Durata solutie paralela: " + (finalParalel-startParalel));

        return (double)(finalSecvential - startSecvential) / (double)(finalParalel - startParalel);
    }

    /*
     * Verifica daca valorile obtinute prin cele 2 metode sunt identice
     */
    public static void testCalculMedieStudentiInscrisiCurs() {
    	calculMedieVarstaStudentiInscrisi(1);
    }

    /*
     * Test performanta versiune paralela vs secventiala
     */
    public static void testPerformantaCalculMedieStudentiInscrisiCurs() {
        final int nrProcesoare = getNrProcesoare();
        final double speedup = calculMedieVarstaStudentiInscrisi(REPETARI);
        String msg = "Solutia paralela trebuie sa ruleze de cel putin 1.2x mai repede ";
        
        System.out.println("Test: " + msg);
        System.out.println("Nr procesoare: " + nrProcesoare);
        
        if(speedup > 1.2)
        	System.out.println("Test PASSED. Factor crestere viteza " + speedup);
        else
        	System.out.println("Test FAILED. Factor crestere viteza " + speedup);
    }

    private static double determinaCelMaiComunPrenumePentruStudentiiNeinregistrati(final int repeats) {
    	
    	final Student[] studenti = generareDateStudenti();
        final Solutie solutie = new Solutie();
        
        System.out.println("*********Test: determina cel mai comun prenume");

        final String ref = solutie.calculSecvential_CelMaiComunPrenumePentruStudentiiNeinregistrati(studenti);

        final long startSecvential = System.currentTimeMillis();
        for (int r = 0; r < repeats; r++) {
            solutie.calculSecvential_CelMaiComunPrenumePentruStudentiiNeinregistrati(studenti);
        }
        final long finalSecvential = System.currentTimeMillis();
        
        System.out.println("Durata solutie secventiala: " + (finalSecvential-startSecvential));

        final String calc = solutie.calculParalel_CelMaiComunPrenumePentruStudentiiNeinregistrati(studenti);
        
        // **********************************************
        if(ref == calc)
        	System.out.println("Test PASSED. Valorile calculate sunt identice ");
        else
        	System.out.println("Test FAILED. Valorile calculate sunt DIFERITE");


        final long startParalel = System.currentTimeMillis();
        for (int r = 0; r < repeats; r++) {
            solutie.calculParalel_CelMaiComunPrenumePentruStudentiiNeinregistrati(studenti);
        }
        final long finalParalel = System.currentTimeMillis();
        
        System.out.println("Durata solutie paralela: " + (finalParalel-startParalel));

        return (double)(finalSecvential - startSecvential) /
        		(double)(finalParalel - startParalel);
    }

    /*
     * Test correctness of mostCommonFirstNameOfInactiveStudentsParallelStream.
     */
    public static void testDeterminaCelMaiComunPrenumePentruStudentiiNeinregistrati() {
        determinaCelMaiComunPrenumePentruStudentiiNeinregistrati(1);
    }

    /*
     * Test performance of mostCommonFirstNameOfInactiveStudentsParallelStream.
     */
    public static void testPerformantaDeterminaCelMaiComunPrenumePentruStudentiiNeinregistrati() {
        final int nrProcesoare = getNrProcesoare();
        final double speedup = determinaCelMaiComunPrenumePentruStudentiiNeinregistrati(REPETARI);
        final double expectedSpeedup = (double)nrProcesoare * 0.5;
        String msg = "crestere viteza executie cu cel putin " + expectedSpeedup + "x ";
        
        System.out.println("Test: " + msg);
        System.out.println("Nr procesoare: " + nrProcesoare);
        
        if(speedup >= expectedSpeedup)
        	System.out.println("Test PASSED. Factor crestere viteza " + speedup);
        else
        	System.out.println("Test FAILED. Factor crestere viteza " + speedup);

    }

    private static double calculNumarStudentiNepromovati(final int repeats) {
        final Student[] students = generareDateStudenti();
        final Solutie solutie = new Solutie();
        
        System.out.println("*********Test: determina numarul studentilor peste 20 de ani care nu au promovat");

        final int ref = solutie.calculSecvential_NumarulStudentiNepromovatiCuVarstaPeste20(students);

        final long startSecvential = System.currentTimeMillis();
        for (int r = 0; r < repeats; r++) {
            solutie.calculSecvential_NumarulStudentiNepromovatiCuVarstaPeste20(students);
        }
        final long finalSecvential = System.currentTimeMillis();
        
        System.out.println("Durata solutie paralela: " + (finalSecvential-startSecvential));

        final int calc = solutie.calculParalel_NumarulStudentiNepromovatiCuVarstaPeste20(students);
        
        // **********************************************
        if(ref == calc)
        	System.out.println("Test PASSED. Valorile calculate sunt identice ");
        else
        	System.out.println("Test FAILED. Valorile calculate sunt DIFERITE");


        final long startParalel = System.currentTimeMillis();
        for (int r = 0; r < repeats; r++) {
            solutie.calculParalel_NumarulStudentiNepromovatiCuVarstaPeste20(students);
        }
        final long finalParalel = System.currentTimeMillis();
        
        System.out.println("Durata solutie paralela: " + (finalParalel-startParalel));

        return (double)(finalSecvential - startSecvential) / (double)(finalParalel - startParalel);
    }

    /*
     * Test correctness of countNumberOfFailedStudentsOlderThan20ParallelStream.
     */
    public static void testDeterminaNumarStudentiNepromovati() {
        calculNumarStudentiNepromovati(1);
    }

    /*
     * Test performance of countNumberOfFailedStudentsOlderThan20ParallelStream.
     */
    public static void testPerformantaDeterminaNumarStudentiNepromovati() {
        final int nrProcesoare = getNrProcesoare();
        final double speedup = calculNumarStudentiNepromovati(REPETARI);
        String msg = "Solutia paralela trebuie sa ruleze de cel putin 1.2x mai repede ";
        
        System.out.println("Test: " + msg);
        System.out.println("Nr procesoare: " + nrProcesoare);
        
        if(speedup >= 1.2)
        	System.out.println("Test PASSED. Factor crestere viteza " + speedup);
        else
        	System.out.println("Test FAILED. Factor crestere viteza " + speedup);

    }
    
    public static void main(String[] args) {
    	System.out.println("Start...");
    	testCalculMedieStudentiInscrisiCurs();
    	testPerformantaCalculMedieStudentiInscrisiCurs();
    	
    	testDeterminaCelMaiComunPrenumePentruStudentiiNeinregistrati();
    	testPerformantaDeterminaCelMaiComunPrenumePentruStudentiiNeinregistrati();
    	
    	testDeterminaNumarStudentiNepromovati();
    	testPerformantaDeterminaNumarStudentiNepromovati();
    	System.out.println("Sfarsit implementare");
    }

}
