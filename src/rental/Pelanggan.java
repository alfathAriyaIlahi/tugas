package rental;

public class Pelanggan {
    private String idPelanggan;
    private String nama;
    private String noHp;

    public Pelanggan(String idPelanggan, String nama, String noHp) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.noHp = noHp;
    }

    public String getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(String idPelanggan) { this.idPelanggan = idPelanggan; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public void display() {
        System.out.println("Pelanggan ID: " + idPelanggan + " | Nama: " + nama + " | No HP: " + noHp);
    }
}
