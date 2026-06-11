package rental;

import java.sql.*;

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

    static String formatRupiah(long amount) {
        String raw = String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = raw.length() - 1; i >= 0; i--) {
            sb.insert(0, raw.charAt(i));
            count++;
            if (count % 3 == 0 && i > 0) sb.insert(0, '.');
        }
        return "Rp " + sb;
    }

    static String buildRentalOptions() {
        String sql = "SELECT r.idRental, pl.nama, (r.lamaSewa * j.hargaPerJam) AS tagihan " +
                     "FROM rental r " +
                     "JOIN pelanggan pl ON r.idPelanggan = pl.idPelanggan " +
                     "JOIN komputer k ON r.idKomputer = k.idKomputer " +
                     "JOIN jenis_komputer j ON k.idJenis = j.idJenis " +
                     "LEFT JOIN pembayaran pb ON r.idRental = pb.idRental " +
                     "WHERE pb.idPembayaran IS NULL";
        StringBuilder sb = new StringBuilder();
        try (Connection c = Database.conn();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString(1);
                String nama = rs.getString(2);
                long tagihan = rs.getLong(3);
                sb.append("<option value=\"").append(id).append("\">")
                  .append(id).append(" - ").append(nama)
                  .append(" (Rp ").append(formatRupiah(tagihan).replace("Rp ", "")).append(")")
                  .append("</option>");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sb.toString();
    }
}
