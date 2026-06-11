package rental;

import java.io.*;
import java.nio.file.*;
import java.sql.*;

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
        System.out.println("Jenis ID: " + idJenis + " | Nama: " + namaJenis + " | Harga: Rp " + hargaPerJam + "/jam");
    }

    static final String FRONT = "frontend/";

    static String render(String file, String key, String data) throws IOException {
        String c = Files.readString(Path.of(FRONT + file));
        c = c.replace(key, data);
        c = c.replace("<!--OPT_PELANGGAN-->", buildOptions("SELECT idPelanggan, nama FROM pelanggan"));
        c = c.replace("<!--OPT_PETUGAS-->", buildOptions("SELECT idPetugas, namaPetugas FROM petugas"));
        c = c.replace("<!--OPT_JENIS-->", buildOptions("SELECT idJenis, namaJenis FROM jenis_komputer"));
        c = c.replace("<!--OPT_KOMPUTER-->", buildOptions("SELECT idKomputer, merk FROM komputer"));
        c = c.replace("<!--OPT_RENTAL-->", Rental.buildRentalOptions());
        return c;
    }

    static String buildOptions(String sql) {
        StringBuilder sb = new StringBuilder();
        try (Connection c = Database.conn();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString(1);
                String label = rs.getString(2);
                sb.append("<option value=\"").append(id).append("\">")
                  .append(id).append(" - ").append(label)
                  .append("</option>");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sb.toString();
    }

    static String generateRowDataJs(String table) {
        StringBuilder sb = new StringBuilder("<script>\nconst rowData = {\n");
        String idCol = Pembayaran.getIdColumn(table);
        try (Connection c = Database.conn();
             ResultSet rs = c.createStatement().executeQuery("SELECT * FROM " + table)) {
            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String id = rs.getString(idCol);
                sb.append("  \"").append(id).append("\": {");
                for (int i = 1; i <= colCount; i++) {
                    String colName = rs.getMetaData().getColumnName(i);
                    String val = rs.getString(i);
                    if (val == null) val = "";
                    val = val.replace("\\", "\\\\").replace("\"", "\\\"");
                    val = val.replace("\n", "\\n").replace("\r", "");
                    sb.append("\"").append(colName).append("\":\"").append(val).append("\"");
                    if (i < colCount) sb.append(",");
                }
                sb.append("},\n");
            }
        } catch (Exception e) { e.printStackTrace(); }
        sb.append("};\n");
        sb.append("function editRow(id) {\n");
        sb.append("  document.getElementById('formId').value = id;\n");
        sb.append("  const data = rowData[id];\n");
        sb.append("  for (let key in data) {\n");
        sb.append("    let el = document.getElementById(key);\n");
        sb.append("    if (el) el.value = data[key];\n");
        sb.append("  }\n");
        sb.append("  let btn = document.querySelector('.btn-submit');\n");
        sb.append("  if(btn) btn.textContent = 'Update Data';\n");
        sb.append("  window.scrollTo({ top: 0, behavior: 'smooth' });\n");
        sb.append("}\n");
        sb.append("</script>");
        return sb.toString();
    }
}
