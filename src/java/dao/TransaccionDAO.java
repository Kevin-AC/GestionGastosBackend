
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

    // -------- INSERTAR TRANSACCION --------
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

    // -------- LISTAR TRANSACCIONES POR USUARIO --------
    public List<Transaccion> listarPorUsuario(int usuarioId) {

        List<Transaccion> lista = new ArrayList<>();

        String sql = "SELECT * FROM transacciones WHERE usuario_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Transaccion t = new Transaccion();

                t.setIdTransaccion(rs.getInt("id_transaccion"));
                t.setMonto(rs.getDouble("monto"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setFecha(rs.getString("fecha"));
                t.setIdUsuario(rs.getInt("usuario_id"));
                t.setCategoria_id(rs.getInt("categoria_id"));

                lista.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}