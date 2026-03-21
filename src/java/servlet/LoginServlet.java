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
            } else {
                identificador = request.getParameter("identificador");
                if (identificador == null) identificador = request.getParameter("correo");
                contrasena = request.getParameter("password");
                if (contrasena == null) contrasena = request.getParameter("contrasena");
            }

            if (identificador == null || contrasena == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.println("{\"success\":false,\"message\":\"Faltan credenciales\"}");
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
            // Si manejas roles/admin, setear isAdmin aquí (ejemplo: false por defecto)
            session.setAttribute("isAdmin", Boolean.FALSE);

            try (PrintWriter out = response.getWriter()) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println("{\"success\":true,\"message\":\"Login exitoso\",\"user\":{\"id\":" + usuario.getId()
                        + ",\"nombre\":\"" + escape(usuario.getNombre()) + "\",\"correo\":\"" + escape(usuario.getCorreo()) + "\"}}");
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

    // Extra: método simple para extraer campos de JSON sin librería
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
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && !Character.isWhitespace(json.charAt(end))) end++;
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return null;
        }
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