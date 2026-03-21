
package dao;

import modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO para operaciones sobre la tabla usuario.
 * Ajusta nombres de columnas si tu esquema es distinto.
 */
public class UsuarioDAO {

    private final Connection con;

    public UsuarioDAO(Connection con) {
        this.con = con;
    }

    /**
     * Inserta un usuario. Devuelve true si se insertó al menos una fila.
     * Lanza SQLException para que el servlet lo maneje.
     */
    public boolean insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, apellido, telefono, correo, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getTelefono());
            ps.setString(4, u.getCorreo());
            ps.setString(5, u.getContrasena()); // en producción guarda hash
            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * Actualiza un usuario. Si contrasena es null o vacía no la actualiza.
     * Devuelve true si se actualizó al menos una fila.
     */
    public boolean actualizar(Usuario u) throws SQLException {
        boolean actualizarPassword = u.getContrasena() != null && !u.getContrasena().isEmpty();
        String sql = "UPDATE usuario SET nombre = ?, correo = ?, telefono = ?" + (actualizarPassword ? ", password = ?" : "") + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, u.getNombre());
            ps.setString(idx++, u.getCorreo());
            ps.setString(idx++, u.getTelefono());
            if (actualizarPassword) {
                ps.setString(idx++, u.getContrasena()); // en producción usar hash
            }
            ps.setInt(idx++, u.getId());
            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * Elimina un usuario por id. Devuelve true si se eliminó al menos una fila.
     */
    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * Obtiene un usuario por id (sin password).
     * Devuelve null si no existe.
     */
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT id, nombre, apellido, telefono, correo FROM usuario WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setTelefono(rs.getString("telefono"));
                    u.setCorreo(rs.getString("correo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Valida usuario por correo o nombre y contraseña.
     * Devuelve Usuario si las credenciales coinciden, null en caso contrario.
     * IMPORTANTE: en producción compara hashes en lugar de texto plano.
     */
    public Usuario validarUsuario(String correoONombre, String contrasena) {
        String sql = "SELECT id, nombre, correo FROM usuario WHERE (correo = ? OR nombre = ?) AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correoONombre);
            ps.setString(2, correoONombre);
            ps.setString(3, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCorreo(rs.getString("correo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}