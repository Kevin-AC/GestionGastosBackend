package servlet;  // ← Ajusta tu paquete

import dao.TransaccionDAO;
import modelo.Transaccion;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/ListaGastosServlet")
public class ListaGastosServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(200);
            return;
        }
        
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection con = null;
        
        try {
            System.out.println("🔥 DEBUG: Servlet iniciado");
            
             String idUsuarioStr = request.getParameter("idUsuario");
                if (idUsuarioStr == null || idUsuarioStr.isEmpty()) {
                    response.setStatus(400);
                    out.print("{\"error\":\"Falta idUsuario\"}");
                    return;
                }

            
            int usuarioId = Integer.parseInt(idUsuarioStr);
            System.out.println("👤 Usuario solicitado: " + usuarioId);
            
            conexion.Conexion conexion = new conexion.Conexion();
            con = conexion.conectar();
            TransaccionDAO dao = new TransaccionDAO(con);
            
           
            List<Transaccion> gastos = dao.listarGastos(usuarioId);
            
            System.out.println("📊 DEBUG: Gastos encontrados: " + gastos.size());
            
            out.print("[");
            for (int i = 0; i < gastos.size(); i++) {
                Transaccion t = gastos.get(i);
                out.print("{\"idTransaccion\":" + t.getIdTransaccion() + 
                         ",\"descripcion\":\"" + escapeJson(t.getDescripcion()) + "\"" +
                         ",\"monto\":" + t.getMonto() + 
                         ",\"fecha\":\"" + t.getFecha() + "\"" +
                         ",\"idUsuario\":" + t.getIdUsuario() + 
                         ",\"categoria_id\":" + t.getCategoria_id() + "}");
                if (i < gastos.size() - 1) out.print(",");
            }
            out.print("]");
            
        } catch (Exception e) {
            System.out.println("💥 DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
            out.flush();
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
