
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;


public class Conexion {
    public Connection conectar(){

        Connection con = null;
        

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

        con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/gestor_gastos_finan",
            "root",
            "12345678"
        );

        System.out.println("Conexion exitosa");

    }catch(Exception e){
        e.printStackTrace();
    }

    return con;
    }
    
}
