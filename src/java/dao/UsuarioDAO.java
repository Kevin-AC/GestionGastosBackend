package dao;
import java.sql.DriverManager;
import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import modelo.Usuario;


public class UsuarioDAO {
    
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
        public boolean validarUsuario(String correo, String contrasena) {
    return false;  // Temporal
}


}
