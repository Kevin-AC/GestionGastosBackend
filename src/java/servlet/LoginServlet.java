package servlet;

import dao.UsuarioDAO;
import java.io.IOException;

import conexion.Conexion;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.Usuario;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "POST, GET");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type");

    // 1. Conexión a la base de datos
    Conexion cn = new Conexion();
    Connection con = cn.conectar();
    
    // 2. Crear el DAO pasando la conexión (SOLO UNA VEZ)
    UsuarioDAO dao = new UsuarioDAO(con);

    // 3. Obtener parámetros (Asegúrate de que los nombres coincidan con tu HTML/React)
    String correo = request.getParameter("correo");
    String contrasena = request.getParameter("password");

    // 4. Validar (Tu método devuelve un objeto Usuario, no un boolean)
    Usuario usuarioLogueado = dao.validarUsuario(correo, contrasena);

    if (usuarioLogueado != null) {
        // Guardar el ID en la sesión para que el sistema sepa quién eres
        HttpSession session = request.getSession();
        session.setAttribute("idUsuario", usuarioLogueado.getId());
        session.setAttribute("nombreUsuario", usuarioLogueado.getNombre());

        response.sendRedirect("dashboard.jsp");
    } else {
        // Enviar un mensaje simple o redirigir con error
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<script>alert('Correo o contraseña incorrectos'); window.location='login.jsp';</script>");
    }
}

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
