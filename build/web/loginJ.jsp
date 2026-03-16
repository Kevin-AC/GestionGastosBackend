<%-- 
    Document   : loginJ
    Created on : 12/03/2026, 8:15:43 p. m.
    Author     : USUARIO
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Iniciar Sesion!</h1>
        <form action="LoginServlet" method="post">

        Correo:
        <input type="text" name="correo">

        <br><br>

        Contraseña:
        <input type="password" name="password">

<br><br>

<input type="submit" value="Ingresar">

</form>
    </body>
</html>
