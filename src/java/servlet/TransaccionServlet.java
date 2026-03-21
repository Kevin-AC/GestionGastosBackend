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
import java.io.BufferedReader;
import java.util.stream.Collectors;

@WebServlet(name = "TransaccionServlet", urlPatterns = {"/TransaccionServlet"})
public class TransaccionServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            BufferedReader reader = request.getReader();
            String body = reader.lines().collect(Collectors.joining());
            System.out.println("BODY: " + body);

        try {
            // 1. Obtener la acción que el usuario quiere realizar
            String accion = extractJsonField(body, "accion");
                if (accion == null) {
                    accion = "insertar";
                }
            // 2. Conectar a la base de datos (Tu lógica actual)
            Conexion cn = new Conexion();
            Connection con = cn.conectar();
            TransaccionDAO dao = new TransaccionDAO(con);

            boolean resultado = false;
            String mensaje = "";

            // 3. Lógica según la acción
            if (accion.equals("insertar") || accion.equals("actualizar")) {

            // Capturamos los datos del JSON
            String montoStr = extractJsonField(body, "monto");
            String descripcion = extractJsonField(body, "descripcion");
            String fecha = extractJsonField(body, "fecha");
            String idUsuarioStr = extractJsonField(body, "idUsuario");
            String idCategoriaStr = extractJsonField(body, "categoria_id");


            if (montoStr == null || idUsuarioStr == null || idCategoriaStr == null) {
                    response.setStatus(400);
                    return;
                }

                double monto = Double.parseDouble(montoStr);
                int idUsuario = Integer.parseInt(idUsuarioStr);
                int idCategoria = Integer.parseInt(idCategoriaStr);

                Transaccion t = new Transaccion();
                t.setMonto(monto);
                t.setDescripcion(descripcion);
                t.setFecha(fecha);
                t.setIdUsuario(idUsuario);
                t.setCategoria_id(idCategoria);

                if (accion.equals("insertar")) {

                    resultado = dao.insertar(t);
                    mensaje = "Transacción guardada con éxito";

                } else if (accion.equals("actualizar")) {

                    String idT = extractJsonField(body, "idTransaccion");

                    if (idT == null) {
                        response.setStatus(400);
                        return;
                    }

                    int idTransaccion = Integer.parseInt(idT);
                    t.setIdTransaccion(idTransaccion);

                    resultado = dao.actualizar(t);
                    mensaje = "Transacción actualizada con éxito";
                }
            } else if (accion.equals("eliminar")) {

                int idTransaccion = Integer.parseInt(extractJsonField(body, "idTransaccion"));
                int idUsuario = Integer.parseInt(extractJsonField(body, "idUsuario"));

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
    private String extractJsonField(String json, String field) {
        try {
            if (json == null) return null;

            String key = "\"" + field + "\"";
            int idx = json.indexOf(key);
            if (idx == -1) return null;

            int colon = json.indexOf(":", idx);
            if (colon == -1) return null;

            int start = colon + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

            char c = json.charAt(start);

            if (c == '\"') {
                int end = json.indexOf("\"", start + 1);
                if (end == -1) return null;
                return json.substring(start + 1, end);
            } else {
                int end = start;
                while (end < json.length() &&
                       json.charAt(end) != ',' &&
                       json.charAt(end) != '}' &&
                       !Character.isWhitespace(json.charAt(end))) {
                    end++;
                }
                return json.substring(start, end).trim();
            }

        } catch (Exception e) {
            return null;
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
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        processRequest(request, response);
    }

}
