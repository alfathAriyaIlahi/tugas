package rental;

public class Pembayaran {
    private String idPembayaran;
    private Rental rental;
    private int bayar;

    public Pembayaran(String idPembayaran, Rental rental, int bayar) {
        this.idPembayaran = idPembayaran;
        this.rental = rental;
        this.bayar = bayar;
    }

    public int hitungKembalian() {
        if (rental != null) {
            return bayar - rental.hitungTotal();
        }
        return 0;
    }

    public void display() {
        System.out.println("================================");
        System.out.println("Data Pembayaran ID: " + idPembayaran);
        if (rental != null) {
            System.out.println("Rental ID: " + rental.getIdRental());
            System.out.println("Total Tagihan: Rp " + rental.hitungTotal());
        }
        System.out.println("Jumlah Bayar: Rp " + bayar);
        System.out.println("Kembalian: Rp " + hitungKembalian());
        System.out.println("================================");
    }
    
    public String getIdPembayaran() { return idPembayaran; }
    public Rental getRental() { return rental; }
    public int getBayar() { return bayar; }
}
