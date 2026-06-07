package rental;

public class JenisKomputer {
    private String idJenis;
    private String namaJenis;
    private int hargaPerJam;

    public JenisKomputer(String idJenis, String namaJenis, int hargaPerJam) {
        this.idJenis = idJenis;
        this.namaJenis = namaJenis;
        this.hargaPerJam = hargaPerJam;
    }

    public String getIdJenis() { return idJenis; }
    public void setIdJenis(String idJenis) { this.idJenis = idJenis; }
    public String getNamaJenis() { return namaJenis; }
    public void setNamaJenis(String namaJenis) { this.namaJenis = namaJenis; }
    public int getHargaPerJam() { return hargaPerJam; }
    public void setHargaPerJam(int hargaPerJam) { this.hargaPerJam = hargaPerJam; }

    public void display() {
        System.out.println("Jenis Komputer ID: " + idJenis + " | Nama: " + namaJenis + " | Harga/Jam: Rp " + hargaPerJam);
    }
}
