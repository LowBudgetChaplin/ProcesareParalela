public final class Student {
    private final String prenume;
    private final String nume;
    private final double varsta;
    private final int notaCurs;
    /**
     * Whether the student is currently enrolled, or has already completed the
     * course.
     */
    private final boolean esteInregistrat;

    public Student(final String prenume, final String nume,
            final double varsta, final int nota,
            final boolean esteInregistrat) {
        this.prenume = prenume;
        this.nume = nume;
        this.varsta = varsta;
        this.notaCurs = nota;
        this.esteInregistrat = esteInregistrat;
    }

    public String getPrenume() {
        return prenume;
    }

    public String getNume() {
        return nume;
    }

    public double getVarsta() {
        return varsta;
    }

    public int getNota() {
        return notaCurs;
    }

    public boolean verificaEsteInregistrat() {
        return esteInregistrat;
    }
}
