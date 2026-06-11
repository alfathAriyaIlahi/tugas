package rental;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class MainRental {
    static final String URL = "jdbc:mysql://localhost:3306/db_rental";
    static final String USER = "root";
    static final String PASS = "";
    static final String FRONT = "frontend/";

    static Connection conn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8080), 0);

        // Dashboard route (dynamic)
        s.createContext("/", e -> {
            if (!"/".equals(e.getRequestURI().getPath()) && !"/index.html".equals(e.getRequestURI().getPath())) {
                e.sendResponseHeaders(404, -1);
                e.close();
                return;
            }
            String html = buildDashboard();
            send(e, html);
        });
        s.createContext("/style.css", e -> sendFile(e, "style.css"));

        route(s, "/pelanggan.html", "pelanggan", "<!--DATA_PELANGGAN-->");
        route(s, "/petugas.html", "petugas", "<!--DATA_PETUGAS-->");
        route(s, "/jenis.html", "jenis_komputer", "<!--DATA_JENIS-->");
        route(s, "/komputer.html", "komputer", "<!--DATA_KOMPUTER-->");
        route(s, "/rental.html", "rental", "<!--DATA_RENTAL-->");
        route(s, "/pembayaran.html", "pembayaran", "<!--DATA_PEMBAYARAN-->");

        // Delete route
        s.createContext("/hapus", e -> {
            Map<String,String> q = parseQuery(e.getRequestURI().getQuery());
            String table = q.get("table");
            String id = q.get("id");
            String back = q.get("back");
            if (table != null && id != null) {
                delete(table, id);
            }
            redirect(e, back != null ? back : "/");
        });

        s.start();
        System.out.println("http://localhost:8080");
    }

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

        try (Connection c = conn();
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
                        ? "<span class=\"money-positive\">" + formatRupiah(tagihan) + "</span>"
                        : "<span class=\"money-neutral\">-</span>";

                rows.append("<tr>");
                rows.append("<td>").append(nomor++).append("</td>");
                rows.append("<td>").append(idRental).append("</td>");
                rows.append("<td>").append(nama).append("</td>");
                rows.append("<td>").append(merk).append("</td>");
                rows.append("<td>").append(jenis).append("</td>");
                rows.append("<td>").append(petugas).append("</td>");
                rows.append("<td>").append(lamaSewa).append(" jam</td>");
                rows.append("<td>").append(formatRupiah(tagihan)).append("</td>");
                rows.append("<td>").append(uangMasuk).append("</td>");
                rows.append("<td>").append(statusBadge).append("</td>");
                rows.append("</tr>");
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            String html = Files.readString(Path.of(FRONT + "index.html"));
            html = html.replace("<!--TOTAL_PENDAPATAN-->", formatRupiah(totalPendapatan));
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


    static void route(HttpServer s, String path, String table, String placeholder) {
        s.createContext(path, e -> {
            if ("POST".equalsIgnoreCase(e.getRequestMethod())) {
                Map<String,String> p = parse(new String(e.getRequestBody().readAllBytes()));
                String actionId = p.remove("id");
                if (actionId != null && !actionId.trim().isEmpty()) {
                    update(table, actionId, p);
                } else {
                    insert(table, p);
                }
                redirect(e, path);
                return;
            }

            String data = selectHTML(table, path);
            String html = render(path.replace("/", ""), placeholder, data);
            
            String script = generateRowDataJs(table);
            html = html.replace("</body>", script + "\n</body>");
            
            send(e, html);
        });
    }

    static void update(String table, String id, Map<String,String> p) {
        try (Connection c = conn()) {
            String idCol = getIdColumn(table);
            StringBuilder setClause = new StringBuilder();
            for (String k : p.keySet()) {
                setClause.append(k).append("=?,");
            }
            if (setClause.length() > 0) {
                setClause.deleteCharAt(setClause.length() - 1);
            }

            PreparedStatement st = c.prepareStatement(
                "UPDATE " + table + " SET " + setClause + " WHERE " + idCol + "=?"
            );

            int i = 1;
            for (String v : p.values()) st.setString(i++, v);
            st.setString(i, id);
            st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void delete(String table, String id) {
        try (Connection c = conn()) {
            // Hapus data anak terlebih dahulu (foreign key)
            switch (table) {
                case "pelanggan":
                    cascade(c, "pembayaran", "idRental",
                        "SELECT idRental FROM rental WHERE idPelanggan=?", id);
                    cascadeDirect(c, "rental", "idPelanggan", id);
                    break;
                case "petugas":
                    cascade(c, "pembayaran", "idRental",
                        "SELECT idRental FROM rental WHERE idPetugas=?", id);
                    cascadeDirect(c, "rental", "idPetugas", id);
                    break;
                case "jenis_komputer":
                    // jenis -> komputer -> rental -> pembayaran
                    for (String kid : getIds(c, "SELECT idKomputer FROM komputer WHERE idJenis=?", id)) {
                        cascade(c, "pembayaran", "idRental",
                            "SELECT idRental FROM rental WHERE idKomputer=?", kid);
                        cascadeDirect(c, "rental", "idKomputer", kid);
                    }
                    cascadeDirect(c, "komputer", "idJenis", id);
                    break;
                case "komputer":
                    cascade(c, "pembayaran", "idRental",
                        "SELECT idRental FROM rental WHERE idKomputer=?", id);
                    cascadeDirect(c, "rental", "idKomputer", id);
                    break;
                case "rental":
                    cascadeDirect(c, "pembayaran", "idRental", id);
                    break;
            }
            String idCol = getIdColumn(table);
            PreparedStatement st = c.prepareStatement(
                "DELETE FROM " + table + " WHERE " + idCol + "=?");
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

    static void insert(String table, Map<String,String> p) {
        try (Connection c = conn()) {
            // Auto-generate ID
            String idCol = getIdColumn(table);
            String prefix = getIdPrefix(table);
            String newId = generateId(c, table, idCol, prefix);
            p.put(idCol, newId);

            String cols = String.join(",", p.keySet());
            String vals = "?,".repeat(p.size());
            vals = vals.substring(0, vals.length()-1);

            PreparedStatement st = c.prepareStatement(
                "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")"
            );

            int i = 1;
            for (String v : p.values()) st.setString(i++, v);
            st.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

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

    static String generateRowDataJs(String table) {
        StringBuilder sb = new StringBuilder("<script>\nconst rowData = {\n");
        String idCol = getIdColumn(table);
        try (Connection c = conn();
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
        try (Connection c = conn();
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

    static Map<String,String> parseQuery(String query) {
        Map<String,String> m = new HashMap<>();
        if (query == null) return m;
        try {
            for (String s : query.split("&")) {
                String[] x = s.split("=", 2);
                if (x.length == 2)
                    m.put(URLDecoder.decode(x[0], "UTF-8"),
                          URLDecoder.decode(x[1], "UTF-8"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return m;
    }

    static void sendFile(HttpExchange e, String f) throws IOException {
        File file = new File(FRONT + f);
        byte[] b = Files.readAllBytes(file.toPath());
        e.sendResponseHeaders(200, b.length);
        e.getResponseBody().write(b);
        e.close();
    }

    static String render(String file, String key, String data) throws IOException {
        String c = Files.readString(Path.of(FRONT + file));
        c = c.replace(key, data);
        c = c.replace("<!--OPT_PELANGGAN-->", buildOptions("SELECT idPelanggan, nama FROM pelanggan"));
        c = c.replace("<!--OPT_PETUGAS-->", buildOptions("SELECT idPetugas, namaPetugas FROM petugas"));
        c = c.replace("<!--OPT_JENIS-->", buildOptions("SELECT idJenis, namaJenis FROM jenis_komputer"));
        c = c.replace("<!--OPT_KOMPUTER-->", buildOptions("SELECT idKomputer, merk FROM komputer"));
        c = c.replace("<!--OPT_RENTAL-->", buildRentalOptions());
        return c;
    }

    static String buildOptions(String sql) {
        StringBuilder sb = new StringBuilder();
        try (Connection c = conn();
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

    static String buildRentalOptions() {
        String sql = "SELECT r.idRental, pl.nama, (r.lamaSewa * j.hargaPerJam) AS tagihan " +
                     "FROM rental r " +
                     "JOIN pelanggan pl ON r.idPelanggan = pl.idPelanggan " +
                     "JOIN komputer k ON r.idKomputer = k.idKomputer " +
                     "JOIN jenis_komputer j ON k.idJenis = j.idJenis " +
                     "LEFT JOIN pembayaran pb ON r.idRental = pb.idRental " +
                     "WHERE pb.idPembayaran IS NULL";
        StringBuilder sb = new StringBuilder();
        try (Connection c = conn();
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

    static void send(HttpExchange e, String html) throws IOException {
        byte[] b = html.getBytes();
        e.sendResponseHeaders(200, b.length);
        e.getResponseBody().write(b);
        e.close();
    }

    static void redirect(HttpExchange e, String path) throws IOException {
        e.getResponseHeaders().set("Location", path);
        e.sendResponseHeaders(302, -1);
        e.close();
    }

    static Map<String,String> parse(String q) {
        Map<String,String> m = new HashMap<>();
        try {
            for (String s : q.split("&")) {
                String[] x = s.split("=");
                if (x.length==2)
                    m.put(URLDecoder.decode(x[0],"UTF-8"),
                          URLDecoder.decode(x[1],"UTF-8"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return m;
    }
}