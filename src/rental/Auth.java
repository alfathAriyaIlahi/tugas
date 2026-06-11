package rental;

import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Auth {
    static final String USERNAME = "Admin";
    static final String PASSWORD = "Admin123";
    static final Set<String> sessions = Collections.synchronizedSet(new HashSet<>());

    static String getSessionId(HttpExchange e) {
        String cookies = e.getRequestHeaders().getFirst("Cookie");
        if (cookies == null) return null;
        for (String c : cookies.split(";")) {
            String trimmed = c.trim();
            if (trimmed.startsWith("session=")) {
                return trimmed.substring("session=".length());
            }
        }
        return null;
    }

    static boolean isLoggedIn(HttpExchange e) {
        String sessionId = getSessionId(e);
        return sessionId != null && sessions.contains(sessionId);
    }

    static void handleLogin(HttpExchange e) throws IOException {
        if ("POST".equalsIgnoreCase(e.getRequestMethod())) {
            Map<String,String> p = Pelanggan.parse(new String(e.getRequestBody().readAllBytes()));
            String user = p.getOrDefault("username", "");
            String pass = p.getOrDefault("password", "");
            if (USERNAME.equals(user) && PASSWORD.equals(pass)) {
                String sessionId = UUID.randomUUID().toString();
                sessions.add(sessionId);
                e.getResponseHeaders().add("Set-Cookie", "session=" + sessionId + "; Path=/");
                Pelanggan.redirect(e, "/");
            } else {
                String html = Files.readString(Path.of(JenisKomputer.FRONT + "login.html"));
                html = html.replace("<!--LOGIN_ERROR-->",
                    "<div class=\"login-error\">&#9888;&#65039; Username atau password salah!</div>");
                Pelanggan.send(e, html);
            }
            return;
        }
        String html = Files.readString(Path.of(JenisKomputer.FRONT + "login.html"));
        html = html.replace("<!--LOGIN_ERROR-->", "");
        Pelanggan.send(e, html);
    }

    static void handleLogout(HttpExchange e) throws IOException {
        String sessionId = getSessionId(e);
        if (sessionId != null) sessions.remove(sessionId);
        e.getResponseHeaders().add("Set-Cookie", "session=; Path=/; Max-Age=0");
        Pelanggan.redirect(e, "/login");
    }
}
