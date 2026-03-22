package servlet;

import conexion.Conexion;
import dao.UsuarioDAO;
import modelo.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        Connection con = null;
        try {
            String accion = request.getParameter("accion");
            String bodyTmp = null;
            if (accion == null || accion.isEmpty()) {
                bodyTmp = new BufferedReader(request.getReader()).lines().collect(Collectors.joining());
                accion = extractJsonField(bodyTmp, "accion");
            }
            if (accion == null || accion.isEmpty()) accion = "insertar";

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

            if ("me".equalsIgnoreCase(accion)) {
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute("idUsuario") == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"No autenticado\"}");
                    }
                    return;
                }
                int id = (Integer) session.getAttribute("idUsuario");
                Usuario u = dao.obtenerPorId(id);
                try (PrintWriter out = response.getWriter()) {
                    if (u != null) {
                        out.println("{\"success\":true,\"user\": {\"id\":" + u.getId() + ",\"nombre\":\"" + escape(u.getNombre()) + "\",\"correo\":\"" + escape(u.getCorreo()) + "\"}}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.println("{\"success\":false,\"message\":\"Usuario no encontrado\"}");
                    }
                }
                return;
            }

            if ("logout".equalsIgnoreCase(accion)) {
                HttpSession session = request.getSession(false);
                if (session != null) session.invalidate();
                try (PrintWriter out = response.getWriter()) {
                    out.println("{\"success\":true,\"message\":\"Sesión cerrada\"}");
                }
                return;
            }

            if ("insertar".equalsIgnoreCase(accion)) {
                String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();
                String body = (bodyTmp != null) ? bodyTmp : (contentType.contains("application/json") ? new BufferedReader(request.getReader()).lines().collect(Collectors.joining()) : null);

                String nombre = null, apellido = null, telefono = null, correo = null, contrasena = null;
                if (body != null && !body.isEmpty()) {
                    nombre = extractJsonField(body, "nombre");
                    apellido = extractJsonField(body, "apellido");
                    telefono = extractJsonField(body, "telefono");
                    correo = extractJsonField(body, "correo");
                    contrasena = extractJsonField(body, "password");
                    if (contrasena == null) contrasena = extractJsonField(body, "contrasena");
                } else {
                    nombre = request.getParameter("nombre");
                    apellido = request.getParameter("apellido");
                    telefono = request.getParameter("telefono");
                    correo = request.getParameter("correo");
                    contrasena = request.getParameter("password");
                    if (contrasena == null) contrasena = request.getParameter("contrasena");
                }

                if (nombre == null || correo == null || contrasena == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"Faltan datos requeridos\"}");
                    }
                    return;
                }

                Usuario u = new Usuario();
                u.setNombre(nombre);
                u.setApellido(apellido);
                u.setTelefono(telefono);
                u.setCorreo(correo);
                u.setContrasena(contrasena); // en producción hashear

                try {
                    boolean creado = dao.insertar(u); // ahora insertar devuelve boolean
                    try (PrintWriter out = response.getWriter()) {
                        if (creado) {
                            response.setStatus(HttpServletResponse.SC_CREATED);
                            out.println("{\"success\":true,\"message\":\"Usuario creado\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            out.println("{\"success\":false,\"message\":\"No se pudo crear usuario\"}");
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"Error al crear usuario\"}");
                    }
                }
                return;
            }

            if ("actualizar".equalsIgnoreCase(accion)) {
                String body = (bodyTmp != null) ? bodyTmp : new BufferedReader(request.getReader()).lines().collect(Collectors.joining());
                String idParam = extractJsonField(body, "idUsuario");
                if (idParam == null) idParam = request.getParameter("idUsuario");
                if (idParam == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"Falta idUsuario\"}");
                    }
                    return;
                }
                int idUsuario = Integer.parseInt(idParam);

                HttpSession session = request.getSession(false);
                Integer idSesion = (session != null && session.getAttribute("idUsuario") instanceof Integer) ? (Integer) session.getAttribute("idUsuario") : null;
                Boolean isAdmin = (session != null && session.getAttribute("isAdmin") instanceof Boolean) ? (Boolean) session.getAttribute("isAdmin") : Boolean.FALSE;
                if (idSesion == null || (!isAdmin && idSesion.intValue() != idUsuario)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"No autorizado\"}");
                    }
                    return;
                }

                String nombre = extractJsonField(body, "nombre");
                String correo = extractJsonField(body, "correo");
                String telefono = extractJsonField(body, "telefono");
                String password = extractJsonField(body, "password");

                Usuario u = new Usuario();
                u.setId(idUsuario);
                u.setNombre(nombre);
                u.setCorreo(correo);
                u.setTelefono(telefono);
                if (password != null && !password.isEmpty()) u.setContrasena(password);

                try {
                    boolean ok = dao.actualizar(u);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":" + ok + ",\"message\":\"" + (ok ? "Actualizado" : "No actualizado") + "\"}");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"Error al actualizar\"}");
                    }
                }
                return;
            }

            if ("eliminar".equalsIgnoreCase(accion)) {
                String idParam = extractJsonField(bodyTmp, "idUsuario");

                if (idParam == null) {
                    idParam = request.getParameter("idUsuario");
                }
                if (idParam == null || idParam.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"Falta idUsuario\"}");
                    }
                    return;
                }
                int idUsuarioEliminar;
                try {
                    idUsuarioEliminar = Integer.parseInt(idParam);
                } catch (NumberFormatException nfe) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"idUsuario inválido\"}");
                    }
                    return;
                }

                HttpSession session = request.getSession(false);
                Integer idSesion = (session != null && session.getAttribute("idUsuario") instanceof Integer) ? (Integer) session.getAttribute("idUsuario") : null;
                Boolean isAdmin = (session != null && session.getAttribute("isAdmin") instanceof Boolean) ? (Boolean) session.getAttribute("isAdmin") : Boolean.FALSE;
                /*
                if (idSesion == null && !isAdmin) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"No autenticado\"}");
                    }
                    return;
                }
                
                if (!isAdmin && idSesion != null && idSesion.intValue() != idUsuarioEliminar) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"No autorizado\"}");
                    }
                    return;
                }
                */
                try {
                    boolean eliminado = dao.eliminarUsuario(idUsuarioEliminar);
                    try (PrintWriter out = response.getWriter()) {
                        if (eliminado) {
                            if (idSesion != null && idSesion.intValue() == idUsuarioEliminar) {
                                HttpSession s = request.getSession(false);
                                if (s != null) s.invalidate();
                            }
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.println("{\"success\":true,\"message\":\"Usuario eliminado\"}");
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.println("{\"success\":false,\"message\":\"Usuario no encontrado\"}");
                        }
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("{\"success\":false,\"message\":\"Error al eliminar usuario\"}");
                    }
                }
                return;
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.println("{\"success\":false,\"message\":\"Acción no soportada\"}");
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
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    @Override
    public String getServletInfo() {
        return "Servlet para gestionar usuarios";
    }
}