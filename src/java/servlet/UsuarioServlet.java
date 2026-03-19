package servlet;

import dao.UsuarioDAO;
import modelo.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // CORS para React
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        try {
            Usuario u = new Usuario();
            u.setNombre(request.getParameter("nombre"));
            u.setApellido(request.getParameter("apellido"));
            u.setTelefono(request.getParameter("telefono"));
            u.setCorreo(request.getParameter("correo"));
            u.setContrasena(request.getParameter("contrasena"));

           // UsuarioDAO dao = new UsuarioDAO(con);//falta arreglar error aqui
            //dao.insertar(u);

            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("{\"success\": true, \"message\": \"Usuario registrado\"}");
            out.flush();
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("{\"success\": false, \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            out.flush();
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

    @Override
    public String getServletInfo() {
        return "UsuarioServlet";
    }
    
}
