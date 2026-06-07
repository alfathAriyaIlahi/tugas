package rental;

public class Rental {
    private String idRental;
    private Pelanggan pelanggan;
    private Komputer komputer;
    private Petugas petugas;
    private int lamaSewa;

    public Rental(String idRental, Pelanggan pelanggan, Komputer komputer, Petugas petugas, int lamaSewa) {
        this.idRental = idRental;
        this.pelanggan = pelanggan;
        this.komputer = komputer;
        this.petugas = petugas;
        this.lamaSewa = lamaSewa;
    }

    public int hitungTotal() {
        if (komputer != null && komputer.getJenis() != null) {
            return lamaSewa * komputer.getJenis().getHargaPerJam();
        }
        return 0;
    }

    public void display() {
        System.out.println("================================");
        System.out.println("Data Rental ID: " + idRental);
        if (pelanggan != null) pelanggan.display();
        if (komputer != null) komputer.display();
        if (petugas != null) petugas.display();
        System.out.println("Lama Sewa: " + lamaSewa + " Jam");
        System.out.println("Total Bayar: Rp " + hitungTotal());
        System.out.println("================================");
    }
    
    public String getIdRental() { return idRental; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public Komputer getKomputer() { return komputer; }
    public Petugas getPetugas() { return petugas; }
    public int getLamaSewa() { return lamaSewa; }
}
