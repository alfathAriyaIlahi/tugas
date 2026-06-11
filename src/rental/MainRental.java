package rental;

import com.sun.net.httpserver.*;
import java.net.*;
import java.util.*;

public class MainRental {

    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8080), 0);

        s.createContext("/login", e -> Auth.handleLogin(e));
        s.createContext("/logout", e -> Auth.handleLogout(e));
        s.createContext("/style.css", e -> Pelanggan.sendFile(e, "style.css"));

        s.createContext("/", e -> {
            if (!Auth.isLoggedIn(e)) { Pelanggan.redirect(e, "/login"); return; }
            if (!"/".equals(e.getRequestURI().getPath()) && !"/index.html".equals(e.getRequestURI().getPath())) {
                e.sendResponseHeaders(404, -1);
                e.close();
                return;
            }
            Pelanggan.send(e, Dashboard.buildDashboard());
        });

        route(s, "/pelanggan.html", "pelanggan", "<!--DATA_PELANGGAN-->");
        route(s, "/petugas.html", "petugas", "<!--DATA_PETUGAS-->");
        route(s, "/jenis.html", "jenis_komputer", "<!--DATA_JENIS-->");
        route(s, "/komputer.html", "komputer", "<!--DATA_KOMPUTER-->");
        route(s, "/rental.html", "rental", "<!--DATA_RENTAL-->");
        route(s, "/pembayaran.html", "pembayaran", "<!--DATA_PEMBAYARAN-->");

        s.createContext("/hapus", e -> {
            if (!Auth.isLoggedIn(e)) { Pelanggan.redirect(e, "/login"); return; }
            Map<String,String> q = Pelanggan.parseQuery(e.getRequestURI().getQuery());
            String table = q.get("table");
            String id = q.get("id");
            String back = q.get("back");
            if (table != null && id != null) Petugas.delete(table, id);
            Pelanggan.redirect(e, back != null ? back : "/");
        });

        s.start();
        System.out.println("http://localhost:8080");
    }

    static void route(HttpServer s, String path, String table, String placeholder) {
        s.createContext(path, e -> {
            if (!Auth.isLoggedIn(e)) { Pelanggan.redirect(e, "/login"); return; }
            if ("POST".equalsIgnoreCase(e.getRequestMethod())) {
                Map<String,String> p = Pelanggan.parse(new String(e.getRequestBody().readAllBytes()));
                String actionId = p.remove("id");
                if (actionId != null && !actionId.trim().isEmpty()) {
                    Database.update(table, actionId, p);
                } else {
                    Database.insert(table, p);
                }
                Pelanggan.redirect(e, path);
                return;
            }
            String data = Komputer.selectHTML(table, path);
            String html = JenisKomputer.render(path.replace("/", ""), placeholder, data);
            String script = JenisKomputer.generateRowDataJs(table);
            html = html.replace("</body>", script + "\n</body>");
            Pelanggan.send(e, html);
        });
    }
}