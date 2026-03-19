package dao;

import java.sql.DriverManager;
import java.sql.*;
import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import modelo.Usuario;

public class UsuarioDAO {
    private Connection con;
    
    public UsuarioDAO(Connection con){
        this.con = con;
    }

    public void insertar(Usuario u) {
        String sql = "INSERT INTO usuario(nombre, apellido, telefono, correo, password) VALUES (?, ?, ?, ?, ?)";

        try {
            System.out.println("🔄 Insertando: " + u.getNombre());

            Conexion cn = new Conexion();
            Connection con = cn.conectar();

            if (con == null) {
                System.err.println("❌ Conexion retorna NULL");
                throw new RuntimeException("Fallo conexión");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getApellido());
                ps.setString(3, u.getTelefono());
                ps.setString(4, u.getCorreo());
                ps.setString(5, u.getContrasena());

                int filas = ps.executeUpdate();
                System.out.println("✅ Insertadas " + filas + " filas");
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Usuario validarUsuario(String correo, String contrasena) {
    // Buscamos al usuario que coincida con ambos datos
    String sql = "SELECT * FROM usuarios WHERE correo = ? AND contrasena = ?";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, correo);
        ps.setString(2, contrasena);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            
            Usuario u = new Usuario();
            u.setId(rs.getInt("id_usuario"));
            u.setNombre(rs.getString("nombre"));
            u.setCorreo(rs.getString("correo"));
            return u; 
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; 
}

}
