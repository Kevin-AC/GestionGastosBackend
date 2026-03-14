
package servlet;


import dao.TransaccionDAO;
import conexion.Conexion;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.Transaccion;

@WebServlet(name = "TransaccionServlet", urlPatterns = {"/TransaccionServlet"})
public class TransaccionServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {

            // Obtener datos del formulario
            int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
            int idCategoria = Integer.parseInt(request.getParameter("categoria_id"));
            double monto = Double.parseDouble(request.getParameter("monto"));
            String descripcion = request.getParameter("descripcion");
            String fecha = request.getParameter("fecha");

            // Crear objeto transaccion
            Transaccion t = new Transaccion();

            t.setIdUsuario(idUsuario);
            t.setCategoria_id(idCategoria);
            t.setMonto(monto);
            t.setDescripcion(descripcion);
            t.setFecha(fecha);

            // Conectar a la base de datos
            Conexion cn = new Conexion();
            Connection con = cn.conectar();

            // Guardar en la base de datos
            TransaccionDAO dao = new TransaccionDAO(con);
            dao.insertar(t);

            // Redirigir al formulario
            response.sendRedirect("RegistroTransaccion.jsp");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
