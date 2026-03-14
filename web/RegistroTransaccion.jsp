

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Registro de transaccion</title>
    </head>
    <body>
         
<h2>Registro de Transacción</h2>

<form action="TransaccionServlet" method="post">

<input type="hidden" name="idUsuario" value="1">

Categoria:
<select name="categoria_id">

<option value="1">Arriendo</option>
<option value="2">Comida</option>
<option value="3">Servicios</option>
<option value="4">Transporte</option>
<option value="5">Entretenimiento</option>
<option value="6">Salario</option>

</select>

<br><br>

Monto:
<input type="text" name="monto">

<br><br>

Descripcion:
<input type="text" name="descripcion">

<br><br>

Fecha:
<input type="date" name="fecha">

<br><br>

<input type="submit" value="Guardar">

</form>
    </body>
</html>
