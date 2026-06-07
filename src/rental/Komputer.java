package rental;

public class Komputer {
    private String idKomputer;
    private String merk;
    private JenisKomputer jenis;

    public Komputer(String idKomputer, String merk, JenisKomputer jenis) {
        this.idKomputer = idKomputer;
        this.merk = merk;
        this.jenis = jenis;
    }

    public String getIdKomputer() { return idKomputer; }
    public void setIdKomputer(String idKomputer) { this.idKomputer = idKomputer; }
    public String getMerk() { return merk; }
    public void setMerk(String merk) { this.merk = merk; }
    public JenisKomputer getJenis() { return jenis; }
    public void setJenis(JenisKomputer jenis) { this.jenis = jenis; }

    public void display() {
        System.out.print("Komputer ID: " + idKomputer + " | Merk: " + merk + " | ");
        if (jenis != null) jenis.display();
    }
}
