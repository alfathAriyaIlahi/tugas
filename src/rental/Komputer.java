package rental;

import java.sql.*;

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

    static String selectHTML(String table, String backPath) {
        String sql;
        switch (table) {
            case "komputer":
                sql = "SELECT k.idKomputer, k.merk, j.namaJenis, j.hargaPerJam " +
                      "FROM komputer k JOIN jenis_komputer j ON k.idJenis = j.idJenis";
                break;
            case "rental":
                sql = "SELECT r.idRental, r.idPelanggan, r.idKomputer, r.idPetugas, r.lamaSewa, " +
                      "(r.lamaSewa * j.hargaPerJam) AS totalTagihan " +
                      "FROM rental r " +
                      "JOIN komputer k ON r.idKomputer = k.idKomputer " +
                      "JOIN jenis_komputer j ON k.idJenis = j.idJenis";
                break;
            case "pembayaran":
                sql = "SELECT p.idPembayaran, p.idRental, " +
                      "(r.lamaSewa * j.hargaPerJam) AS totalTagihan, " +
                      "p.bayar, " +
                      "(p.bayar - (r.lamaSewa * j.hargaPerJam)) AS kembalian " +
                      "FROM pembayaran p " +
                      "JOIN rental r ON p.idRental = r.idRental " +
                      "JOIN komputer k ON r.idKomputer = k.idKomputer " +
                      "JOIN jenis_komputer j ON k.idJenis = j.idJenis";
                break;
            default:
                sql = "SELECT * FROM " + table;
                break;
        }

        StringBuilder sb = new StringBuilder();
        try (Connection c = Database.conn();
             ResultSet rs = c.createStatement().executeQuery(sql)) {

            int nomor = 1;
            while (rs.next()) {
                String rowId = rs.getString(1);
                sb.append("<tr>");
                sb.append("<td>").append(nomor++).append("</td>");
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sb.append("<td>").append(rs.getString(i)).append("</td>");
                }
                sb.append("<td>");
                sb.append("<button class=\"btn-edit\" onclick=\"editRow('").append(rowId).append("')\">Edit</button>");
                sb.append("<a class=\"btn-hapus\" href=\"/hapus?table=").append(table)
                  .append("&id=").append(rowId)
                  .append("&back=").append(backPath)
                  .append("\" onclick=\"return confirm('Yakin hapus data ini?')\">");
                sb.append("Hapus</a>");
                sb.append("</td>");
                sb.append("</tr>");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sb.toString();
    }
}
