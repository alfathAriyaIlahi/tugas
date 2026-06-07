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

        s.createContext("/", e -> sendFile(e, "index.html"));
        s.createContext("/style.css", e -> sendFile(e, "style.css"));

        route(s, "/pelanggan.html", "pelanggan", "<!--DATA_PELANGGAN-->");
        route(s, "/petugas.html", "petugas", "<!--DATA_PETUGAS-->");
        route(s, "/jenis.html", "jenis_komputer", "<!--DATA_JENIS-->");
        route(s, "/komputer.html", "komputer", "<!--DATA_KOMPUTER-->");
        route(s, "/rental.html", "rental", "<!--DATA_RENTAL-->");
        route(s, "/pembayaran.html", "pembayaran", "<!--DATA_PEMBAYARAN-->");

        s.start();
        System.out.println("http://localhost:8080");
    }


    static void route(HttpServer s, String path, String table, String placeholder) {
        s.createContext(path, e -> {
            if ("POST".equalsIgnoreCase(e.getRequestMethod())) {
                Map<String,String> p = parse(new String(e.getRequestBody().readAllBytes()));
                insert(table, p);
                redirect(e, path);
                return;
            }

            String data = selectHTML(table);
            String html = render(path.replace("/", ""), placeholder, data);
            send(e, html);
        });
    }

    static void insert(String table, Map<String,String> p) {
        try (Connection c = conn()) {
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

    static String selectHTML(String table) {
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
                sb.append("<tr>");
                sb.append("<td>").append(nomor++).append("</td>");
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sb.append("<td>").append(rs.getString(i)).append("</td>");
                }
                sb.append("</tr>");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sb.toString();
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
        return c.replace(key, data);
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