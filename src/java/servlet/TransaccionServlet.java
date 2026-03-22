package servlet;

import dao.TransaccionDAO;
import conexion.Conexion;
import modelo.Transaccion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "TransaccionServlet", urlPatterns = {"/TransaccionServlet"})
public class TransaccionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CORS para React
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json;charset=UTF-8");

        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(200);
            return;
        }

        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            System.out.println("📦 BODY: " + body);

            String accion       = extractJsonField(body, "accion");
            String idUsuarioStr = extractJsonField(body, "idUsuario");
           // String montoStr     = extractJsonField(body, "monto");
            //String categoriaStr = extractJsonField(body, "categoria_id");
            String idTransaccionStr = extractJsonField(body, "idTransaccion");
           // String descripcion  = extractJsonField(body, "descripcion");
            //String fecha        = extractJsonField(body, "fecha");

            if (accion == null) accion = "insertar";

            if (idUsuarioStr == null) {
                response.setStatus(400);
                sendJson(response, false, "Falta idUsuario");
                return;
            }
            int idUsuario = Integer.parseInt(idUsuarioStr);
            int idTransaccion = idTransaccionStr != null ? Integer.parseInt(idTransaccionStr) : 0;

            Connection con = new Conexion().conectar();
            TransaccionDAO dao = new TransaccionDAO(con);

            Transaccion t = new Transaccion();
            boolean resultado = false;
            String mensaje = "";
            
            

        

            if ("insertar".equals(accion) || "actualizar".equals(accion)) {
                String montoStr     = extractJsonField(body, "monto");
                String categoriaStr = extractJsonField(body, "categoria_id");
                String descripcion  = extractJsonField(body, "descripcion");
                String fecha        = extractJsonField(body, "fecha");

                if (montoStr == null || categoriaStr == null) {
                    response.setStatus(400);
                    sendJson(response, false, "Faltan monto o categoria_id para " + accion);
                    return;
                }

                double monto     = Double.parseDouble(montoStr);
                int categoria_id = Integer.parseInt(categoriaStr);

                t = new Transaccion();
                t.setIdUsuario(idUsuario);
                t.setMonto(monto);
                t.setCategoria_id(categoria_id);
                t.setDescripcion(descripcion != null ? descripcion : "");
                t.setFecha(fecha != null ? fecha : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

                if ("insertar".equals(accion)) {
                    resultado = dao.insertar(t);
                    mensaje = "Gasto guardado";
                } else if ("actualizar".equals(accion)) {
                    t.setIdTransaccion(idTransaccion);
                    resultado = dao.actualizar(t);
                    mensaje = "Gasto actualizado";
                }

            } else if ("eliminar".equals(accion)) {
                if (idTransaccionStr == null) {
                    response.setStatus(400);
                    sendJson(response, false, "Falta idTransaccion");
                    return;
                }
                resultado = dao.eliminar(idTransaccion, idUsuario);
                mensaje = "Gasto eliminado correctamente";

            } else {
                response.setStatus(400);
                sendJson(response, false, "Acción no válida");
                return;
            }

            sendJson(response, resultado, mensaje);
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            sendJson(response, false, e.getMessage());
        }
     }
    

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        doPost(request, response);
    }

    private String extractJsonField(String json, String field) {
        if (json == null || field == null) return null;

        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;

        int colon = json.indexOf(":", idx);
        if (colon == -1) return null;

        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        char c = json.charAt(start);

        if (c == '"') {
            int end = json.indexOf("\"", start + 1);
            if (end == -1) return null;
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() &&
                   json.charAt(end) != ',' &&
                   json.charAt(end) != '}' &&
                   json.charAt(end) != ']') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }

    private void sendJson(HttpServletResponse resp, boolean success, String msg) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(String.format("{\"success\":%s,\"message\":\"%s\"}",
                success,
                msg.replace("\"", "\\\"")
        ));
        out.flush();
    }
}
