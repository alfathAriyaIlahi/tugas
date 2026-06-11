package rental;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Pelanggan {
    private String idPelanggan;
    private String nama;
    private String noHp;

    public Pelanggan(String idPelanggan, String nama, String noHp) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.noHp = noHp;
    }

    public String getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(String idPelanggan) { this.idPelanggan = idPelanggan; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public void display() {
        System.out.println("Pelanggan ID: " + idPelanggan + " | Nama: " + nama + " | No HP: " + noHp);
    }

    // === HTTP utilities ===
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

    static void sendFile(HttpExchange e, String f) throws IOException {
        File file = new File(JenisKomputer.FRONT + f);
        byte[] b = Files.readAllBytes(file.toPath());
        e.sendResponseHeaders(200, b.length);
        e.getResponseBody().write(b);
        e.close();
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
}
