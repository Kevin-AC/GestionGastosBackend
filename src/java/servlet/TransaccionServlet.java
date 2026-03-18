package servlet;

import dao.TransaccionDAO;
import conexion.Conexion;
import java.io.IOException;
import java.io.PrintWriter;
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
            // 1. Obtener la acción que el usuario quiere realizar
            String accion = request.getParameter("accion");
            if (accion == null) {
                accion = "insertar"; // Acción por defecto
            }
            // 2. Conectar a la base de datos (Tu lógica actual)
            Conexion cn = new Conexion();
            Connection con = cn.conectar();
            TransaccionDAO dao = new TransaccionDAO(con);

            boolean resultado = false;
            String mensaje = "";

            // 3. Lógica según la acción
            if (accion.equals("insertar") || accion.equals("actualizar")) {

                // Capturamos los datos del formulario
                double monto = Double.parseDouble(request.getParameter("monto"));
                String descripcion = request.getParameter("descripcion");
                String fecha = request.getParameter("fecha");
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                int idCategoria = Integer.parseInt(request.getParameter("categoria_id"));

                Transaccion t = new Transaccion();
                t.setMonto(monto);
                t.setDescripcion(descripcion);
                t.setFecha(fecha);
                t.setIdUsuario(idUsuario);
                t.setCategoria_id(idCategoria);

                if (accion.equals("insertar")) {
                    resultado = dao.insertar(t);
                    mensaje = "Transacción guardada con éxito";
                } else {
                    // Para actualizar necesitamos el ID de la transacción
                    int idTransaccion = Integer.parseInt(request.getParameter("idTransaccion"));
                    t.setIdTransaccion(idTransaccion);
                    resultado = dao.actualizar(t);
                    mensaje = "Transacción actualizada con éxito";
                }

            } else if (accion.equals("eliminar")) {
                int idTransaccion = Integer.parseInt(request.getParameter("idTransaccion"));
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                resultado = dao.eliminar(idTransaccion, idUsuario);
                mensaje = "Transacción eliminada";
            } else if (accion.equals("ObtenerBalance")){
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                double saldoTotal = dao.obtenerSaldoTotal(idUsuario);
                
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("{\"success\": true, \"saldo\": " + saldoTotal + "}");
                out.flush();
                return;
            }

            // 4. Respuesta en formato JSON
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("{\"success\": " + resultado + ", \"message\": \"" + mensaje + "\"}");
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            // Enviar error al frontend en caso de fallo
            response.setStatus(500);
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
        // **CORS para React**
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        processRequest(request, response);
    }

}
