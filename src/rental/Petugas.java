package rental;

public class Petugas {
    private String idPetugas;
    private String namaPetugas;

    public Petugas(String idPetugas, String namaPetugas) {
        this.idPetugas = idPetugas;
        this.namaPetugas = namaPetugas;
    }

    public String getIdPetugas() { return idPetugas; }
    public void setIdPetugas(String idPetugas) { this.idPetugas = idPetugas; }
    public String getNamaPetugas() { return namaPetugas; }
    public void setNamaPetugas(String namaPetugas) { this.namaPetugas = namaPetugas; }

    public void display() {
        System.out.println("Petugas ID: " + idPetugas + " | Nama: " + namaPetugas);
    }
}
