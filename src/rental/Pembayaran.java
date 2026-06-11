package rental;

import java.sql.*;

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

    static String getIdColumn(String table) {
        switch (table) {
            case "pelanggan": return "idPelanggan";
            case "petugas": return "idPetugas";
            case "jenis_komputer": return "idJenis";
            case "komputer": return "idKomputer";
            case "rental": return "idRental";
            case "pembayaran": return "idPembayaran";
            default: return "id";
        }
    }

    static String getIdPrefix(String table) {
        switch (table) {
            case "pelanggan": return "P";
            case "petugas": return "PT";
            case "jenis_komputer": return "J";
            case "komputer": return "K";
            case "rental": return "R";
            case "pembayaran": return "PB";
            default: return "X";
        }
    }

    static String generateId(Connection c, String table, String idCol, String prefix) throws SQLException {
        String sql = "SELECT " + idCol + " FROM " + table + " ORDER BY " + idCol + " DESC LIMIT 1";
        ResultSet rs = c.createStatement().executeQuery(sql);
        int next = 1;
        if (rs.next()) {
            String lastId = rs.getString(1);
            String numPart = lastId.replaceAll("[^0-9]", "");
            if (!numPart.isEmpty()) {
                next = Integer.parseInt(numPart) + 1;
            }
        }
        return prefix + String.format("%03d", next);
    }
}
