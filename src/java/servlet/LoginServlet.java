package servlet;

import conexion.Conexion;
import dao.UsuarioDAO;
import modelo.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        Connection con = null;
        try {
            // Leer posible body JSON
            String body = null;
            String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();
            if (contentType.contains("application/json")) {
                body = new BufferedReader(request.getReader()).lines().collect(Collectors.joining());
            }

            // Extraer identificador y contraseña (JSON o form params)
            String identificador = null;
            String contrasena = null;
           

            if (body != null && !body.isEmpty()) {
                identificador = extractJsonField(body, "identificador");
                if (identificador == null) identificador = extractJsonField(body, "correo");
                contrasena = extractJsonField(body, "password");
                if (contrasena == null) contrasena = extractJsonField(body, "contrasena");
                 System.out.println("🔍 identificador extraído: [" + identificador + "]");
                 System.out.println("🔍 contrasena extraída: [" + contrasena + "]");
            } else {
                // Si no hay JSON, error
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.println("{\"success\":false,\"message\":\"No se recibió JSON\"}");
                }
                return;
            }


            // Conectar y validar
            Conexion cn = new Conexion();
            con = cn.conectar();
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try (PrintWriter out = response.getWriter()) {
                    out.println("{\"success\":false,\"message\":\"Error de conexión a la base de datos\"}");
                }
                return;
            }

            UsuarioDAO dao = new UsuarioDAO(con);

            // Usar validarUsuario para comprobar identificador + contraseña en una sola consulta
            Usuario usuario = dao.validarUsuario(identificador, contrasena);

            if (usuario == null) {
                // Credenciales inválidas
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                try (PrintWriter out = response.getWriter()) {
                    out.println("{\"success\":false,\"message\":\"Credenciales inválidas\"}");
                }
                return;
            }

            // Login correcto: crear sesión y devolver datos mínimos
            HttpSession session = request.getSession(true);
            session.setAttribute("idUsuario", usuario.getId());
            session.setAttribute("nombreUsuario", usuario.getNombre());
            session.setAttribute("apellidoUsuario", usuario.getApellido());
            session.setAttribute("isAdmin", Boolean.FALSE);
            
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"message\":\"Login exitoso\",\"user\":{")
               .append("\"idUsuario\":").append(usuario.getId()).append(",")
               .append("\"id\":").append(usuario.getId()).append(",")
               .append("\"nombre\":\"").append(escape(usuario.getNombre())).append("\",")
               .append("\"apellido\":\"").append(escape(usuario.getApellido())).append("\",")
               .append("\"correo\":\"").append(escape(usuario.getCorreo())).append("\"}}");

            try (PrintWriter out = response.getWriter()) {
                out.println(json.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("{\"success\":false,\"message\":\"Error interno del servidor\"}");
            }
        } finally {
            try { if (con != null && !con.isClosed()) con.close(); } catch (Exception ignored) {}
        }
    }

        // extraer campos de JSON sin librería
        private String extractJsonField(String json, String field) {
        System.out.println("🔍🔍 Buscando '" + field + "' en JSON: " + json);

        if (json == null || json.trim().isEmpty()) {
            System.out.println("🔍🔍 JSON vacío o null");
            return null;
        }

        // Buscar exactamente el campo
        String keyPattern = "\"" + field + "\":";
        int idx = json.indexOf(keyPattern);
        System.out.println("🔍🔍 keyPattern encontrada en posición: " + idx);

        if (idx == -1) {
            System.out.println("🔍🔍 Campo '" + field + "' NO encontrado");
            return null;
        }

        int start = idx + keyPattern.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start >= json.length() || json.charAt(start) != '"') {
            System.out.println("🔍🔍 No es string value");
            return null;
        }

        start++; // saltar "
        int end = json.indexOf("\"", start);
        if (end == -1) {
            System.out.println("🔍🔍 No se encontró cierre de comillas");
            return null;
        }

        String resultado = json.substring(start, end);
        System.out.println("🔍🔍 RESULTADO para '" + field + "': [" + resultado + "]");
        return resultado;
    }


    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    @Override
    public String getServletInfo() {
        return "Servlet para login de usuarios";
    }
}