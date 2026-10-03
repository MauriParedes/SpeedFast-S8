package util;

import javax.swing.*;
import java.awt.Component;
import java.sql.SQLException;

public final class Mensajes {

    private Mensajes() {}

    public static void exito(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String msg) {
        return JOptionPane.showConfirmDialog(padre, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void errorSQL(Component padre, String accion, SQLException ex) {
        error(padre, "No se pudo " + accion + ".\n" + traducir(ex));
    }

    public static String traducir(SQLException ex) {
        switch (ex.getErrorCode()) {
            case 1451: return "El registro está siendo usado por otros datos (por ejemplo, tiene entregas asociadas).\n"
                            + "Elimine primero los registros relacionados.";
            case 1452: return "El pedido o repartidor seleccionado ya no existe.";
            case 1045: return "Usuario o contraseña de MySQL incorrectos (revise ConexionDB).";
            case 1049: return "La base de datos 'speedfast_db' no existe. Ejecute el script schema.sql.";
            default:
                if (ex.getSQLState() != null && ex.getSQLState().startsWith("08")) {
                    return "No hay conexión con MySQL. Verifique que el servidor esté encendido (localhost:3306).";
                }
                return "Detalle técnico: " + ex.getMessage();
        }
    }
}
