package rental;

import java.sql.*;
import java.util.*;

public class Database {
    static final String URL = "jdbc:mysql://localhost:3306/db_rental";
    static final String USER = "root";
    static final String PASS = "";

    static Connection conn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    static void insert(String table, Map<String,String> p) {
        try (Connection c = conn()) {
            String idCol = Pembayaran.getIdColumn(table);
            String prefix = Pembayaran.getIdPrefix(table);
            String newId = Pembayaran.generateId(c, table, idCol, prefix);
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

    static void update(String table, String id, Map<String,String> p) {
        try (Connection c = conn()) {
            String idCol = Pembayaran.getIdColumn(table);
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
}
