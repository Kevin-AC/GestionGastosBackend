package dao;

import java.sql.*;
import java.sql.Connection;
import modelo.Categoria;

public class CategoriaDAO {

    private Connection con;

    public boolean insertar(Categoria c) {
        String sql = "INSERT INTO categorias (nombre_cat, tipo) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTipo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
