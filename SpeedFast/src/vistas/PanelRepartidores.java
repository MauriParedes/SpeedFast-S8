package vistas;

import DAO.RepartidorDAO;
import Modelo.Repartidor;
import util.Mensajes;
import util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class PanelRepartidores extends JPanel {

    private final RepartidorDAO dao = new RepartidorDAO();
    private final Runnable alCambiar; // avisa a la ventana para refrescar otras pestañas/combos

    private final JTextField txtNombre = new JTextField(22);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public PanelRepartidores(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        JButton btnGuardar = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar seleccionado");
        JButton btnEliminar = new JButton("Eliminar seleccionado");
        JButton btnLimpiar = new JButton("Limpiar");
        form.add(btnGuardar);
        form.add(btnActualizar);
        form.add(btnEliminar);
        form.add(btnLimpiar);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Al seleccionar una fila, se carga su valor en el formulario para editarlo
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            }
        });

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private void guardar() {
        try {
            String nombre = Validador.nombre(txtNombre.getText());
            dao.create(new Repartidor(nombre));
            limpiar();
            alCambiar.run();
            Mensajes.exito(this, "Repartidor registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            Mensajes.aviso(this, ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "registrar el repartidor", ex);
        }
    }

    private void actualizar() {
        try {
            int fila = Validador.seleccionado(tabla.getSelectedRow() >= 0 ? tabla.getSelectedRow() : null, "un repartidor de la tabla");
            int id = (int) modelo.getValueAt(fila, 0);
            String nombre = Validador.nombre(txtNombre.getText());
            if (dao.update(new Repartidor(id, nombre))) {
                limpiar();
                alCambiar.run();
                Mensajes.exito(this, "Repartidor actualizado correctamente.");
            } else {
                Mensajes.error(this, "El repartidor ya no existe.");
                alCambiar.run();
            }
        } catch (IllegalArgumentException ex) {
            Mensajes.aviso(this, ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "actualizar el repartidor", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.aviso(this, "Seleccione un repartidor de la tabla para eliminar.");
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        if (!Mensajes.confirmar(this, "¿Eliminar al repartidor " + modelo.getValueAt(fila, 1) + "?")) return;
        try {
            dao.delete(id);
            limpiar();
            alCambiar.run();
            Mensajes.exito(this, "Repartidor eliminado.");
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "eliminar el repartidor", ex);
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        tabla.clearSelection();
    }

    public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Repartidor r : dao.readAll()) {
                modelo.addRow(new Object[]{r.getId(), r.getNombre()});
            }
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar los repartidores", ex);
        }
    }
}
