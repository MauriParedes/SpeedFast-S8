package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Entrega conexiones JDBC a la base de datos MySQL "speedfast_db".
 * Ajuste USER y PASS según su instalación local de MySQL.
 */
public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/speedfast_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";  // Usuario por defecto
    private static final String PASS = "admin"; // Contraseña configurada en su MySQL

    /**
     * Abre una nueva conexión. Quien la recibe debe cerrarla (los DAO usan try-with-resources).
     * @throws SQLException si el servidor no responde o las credenciales son incorrectas
     */
    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC de MySQL no encontrado: agregue lib/mysql-connector-j al proyecto.", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
