package rental;

import java.sql.*;
import java.util.*;

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

    static void delete(String table, String id) {
        try (Connection c = Database.conn()) {
            switch (table) {
                case "pelanggan":
                    cascade(c, "pembayaran", "idRental", "SELECT idRental FROM rental WHERE idPelanggan=?", id);
                    cascadeDirect(c, "rental", "idPelanggan", id);
                    break;
                case "petugas":
                    cascade(c, "pembayaran", "idRental", "SELECT idRental FROM rental WHERE idPetugas=?", id);
                    cascadeDirect(c, "rental", "idPetugas", id);
                    break;
                case "jenis_komputer":
                    for (String kid : getIds(c, "SELECT idKomputer FROM komputer WHERE idJenis=?", id)) {
                        cascade(c, "pembayaran", "idRental", "SELECT idRental FROM rental WHERE idKomputer=?", kid);
                        cascadeDirect(c, "rental", "idKomputer", kid);
                    }
                    cascadeDirect(c, "komputer", "idJenis", id);
                    break;
                case "komputer":
                    cascade(c, "pembayaran", "idRental", "SELECT idRental FROM rental WHERE idKomputer=?", id);
                    cascadeDirect(c, "rental", "idKomputer", id);
                    break;
                case "rental":
                    cascadeDirect(c, "pembayaran", "idRental", id);
                    break;
            }
            String idCol = Pembayaran.getIdColumn(table);
            PreparedStatement st = c.prepareStatement("DELETE FROM " + table + " WHERE " + idCol + "=?");
            st.setString(1, id);
            st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void cascadeDirect(Connection c, String table, String col, String val) throws SQLException {
        PreparedStatement st = c.prepareStatement("DELETE FROM " + table + " WHERE " + col + "=?");
        st.setString(1, val);
        st.executeUpdate();
    }

    static void cascade(Connection c, String delTable, String delCol, String selectSql, String val) throws SQLException {
        for (String childId : getIds(c, selectSql, val)) {
            cascadeDirect(c, delTable, delCol, childId);
        }
    }

    static java.util.List<String> getIds(Connection c, String sql, String val) throws SQLException {
        java.util.List<String> ids = new java.util.ArrayList<>();
        PreparedStatement st = c.prepareStatement(sql);
        st.setString(1, val);
        ResultSet rs = st.executeQuery();
        while (rs.next()) ids.add(rs.getString(1));
        return ids;
    }
}
