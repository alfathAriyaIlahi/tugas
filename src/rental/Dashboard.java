package rental;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class Dashboard {

    static String buildDashboard() {
        long totalPendapatan = 0;
        int totalTransaksi = 0;
        int totalLunas = 0;
        int totalBelum = 0;
        StringBuilder rows = new StringBuilder();

        String sql = "SELECT r.idRental, pl.nama AS namaPelanggan, " +
                     "k.merk AS merkKomputer, j.namaJenis, " +
                     "pt.namaPetugas, r.lamaSewa, " +
                     "(r.lamaSewa * j.hargaPerJam) AS totalTagihan, " +
                     "pb.bayar " +
                     "FROM rental r " +
                     "JOIN pelanggan pl ON r.idPelanggan = pl.idPelanggan " +
                     "JOIN komputer k ON r.idKomputer = k.idKomputer " +
                     "JOIN jenis_komputer j ON k.idJenis = j.idJenis " +
                     "JOIN petugas pt ON r.idPetugas = pt.idPetugas " +
                     "LEFT JOIN pembayaran pb ON r.idRental = pb.idRental";

        try (Connection c = Database.conn();
             ResultSet rs = c.createStatement().executeQuery(sql)) {

            int nomor = 1;
            while (rs.next()) {
                totalTransaksi++;
                String idRental = rs.getString("idRental");
                String nama = rs.getString("namaPelanggan");
                String merk = rs.getString("merkKomputer");
                String jenis = rs.getString("namaJenis");
                String petugas = rs.getString("namaPetugas");
                int lamaSewa = rs.getInt("lamaSewa");
                long tagihan = rs.getLong("totalTagihan");
                long bayar = rs.getLong("bayar");
                boolean sudahBayar = !rs.wasNull() && bayar > 0;

                if (sudahBayar) {
                    totalLunas++;
                    totalPendapatan += tagihan;
                } else {
                    totalBelum++;
                }

                String statusBadge = sudahBayar
                        ? "<span class=\"badge badge-lunas\">Lunas</span>"
                        : "<span class=\"badge badge-belum\">Belum Bayar</span>";
                String uangMasuk = sudahBayar
                        ? "<span class=\"money-positive\">" + Rental.formatRupiah(tagihan) + "</span>"
                        : "<span class=\"money-neutral\">-</span>";

                rows.append("<tr>");
                rows.append("<td>").append(nomor++).append("</td>");
                rows.append("<td>").append(idRental).append("</td>");
                rows.append("<td>").append(nama).append("</td>");
                rows.append("<td>").append(merk).append("</td>");
                rows.append("<td>").append(jenis).append("</td>");
                rows.append("<td>").append(petugas).append("</td>");
                rows.append("<td>").append(lamaSewa).append(" jam</td>");
                rows.append("<td>").append(Rental.formatRupiah(tagihan)).append("</td>");
                rows.append("<td>").append(uangMasuk).append("</td>");
                rows.append("<td>").append(statusBadge).append("</td>");
                rows.append("</tr>");
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            String html = Files.readString(Path.of(JenisKomputer.FRONT + "index.html"));
            html = html.replace("<!--TOTAL_PENDAPATAN-->", Rental.formatRupiah(totalPendapatan));
            html = html.replace("<!--TOTAL_TRANSAKSI-->", String.valueOf(totalTransaksi));
            html = html.replace("<!--TOTAL_LUNAS-->", String.valueOf(totalLunas));
            html = html.replace("<!--TOTAL_BELUM-->", String.valueOf(totalBelum));
            html = html.replace("<!--DATA_DASHBOARD-->", rows.toString());
            return html;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "<h1>Error loading dashboard</h1>";
        }
    }
}
