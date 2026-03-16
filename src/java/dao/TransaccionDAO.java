
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.Transaccion;

public class TransaccionDAO {

   private Connection con;

    public TransaccionDAO(Connection con) {
        this.con = con;
    }

    //  1. INSERTAR TRANSACCION 
    public boolean insertar(Transaccion t) {
        String sql = "INSERT INTO transacciones (monto, descripcion, fecha, usuario_id, categoria_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, t.getMonto());
            ps.setString(2, t.getDescripcion());
            ps.setString(3, t.getFecha());
            ps.setInt(4, t.getIdUsuario());
            ps.setInt(5, t.getCategoria_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //  2. LISTAR TODAS POR USUARIO 
    public List<Transaccion> listarPorUsuario(int usuarioId) {
        List<Transaccion> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacciones WHERE usuario_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(extraerTransaccion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // -------- 3. LISTAR SOLO GASTOS --------
    public List<Transaccion> listarGastos(int usuarioId) {
        List<Transaccion> lista = new ArrayList<>();
        // El JOIN es clave para saber qué categoría es tipo 'Gasto'
        String sql = "SELECT t.* FROM transacciones t " +
                     "JOIN categorias c ON t.categoria_id = c.id_categoria " +
                     "WHERE t.usuario_id = ? AND c.tipo = 'Gasto'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(extraerTransaccion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 4. LISTAR SOLO INGRESOS 
    public List<Transaccion> listarIngresos(int usuarioId) {
        List<Transaccion> lista = new ArrayList<>();
        String sql = "SELECT t.* FROM transacciones t " +
                     "JOIN categorias c ON t.categoria_id = c.id_categoria " +
                     "WHERE t.usuario_id = ? AND c.tipo = 'Ingreso'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(extraerTransaccion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    
    // Este método para evitar escribir monto encada lista
    private Transaccion extraerTransaccion(ResultSet rs) throws SQLException {
        Transaccion t = new Transaccion();
        t.setIdTransaccion(rs.getInt("id_transaccion"));
        t.setMonto(rs.getDouble("monto"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setFecha(rs.getString("fecha"));
        t.setIdUsuario(rs.getInt("usuario_id"));
        t.setCategoria_id(rs.getInt("categoria_id"));
        return t;
    }
}