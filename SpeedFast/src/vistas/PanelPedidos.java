package vistas;

import DAO.PedidoDAO;
import Modelo.Pedido;
import util.Mensajes;
import util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class PanelPedidos extends JPanel {

    private static final String TODOS = "TODOS";

    private final PedidoDAO dao = new PedidoDAO();
    private final Runnable alCambiar;

    private final JTextField txtDireccion = new JTextField(18);
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"COMIDA", "ENCOMIENDA", "EXPRESS"});
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"PENDIENTE", "EN_REPARTO", "ENTREGADO"});
    private final JComboBox<String> cbFiltroEstado = new JComboBox<>(new String[]{TODOS, "PENDIENTE", "EN_REPARTO", "ENTREGADO"});
    private final JComboBox<String> cbFiltroTipo = new JComboBox<>(new String[]{TODOS, "COMIDA", "ENCOMIENDA", "EXPRESS"});

    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Dirección", "Tipo", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public PanelPedidos(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Dirección:"));
        form.add(txtDireccion);
        form.add(new JLabel("Tipo:"));
        form.add(cbTipo);
        form.add(new JLabel("Estado:"));
        form.add(cbEstado);
        JButton btnGuardar = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar seleccionado");
        JButton btnEliminar = new JButton("Eliminar seleccionado");
        JButton btnLimpiar = new JButton("Limpiar");
        form.add(btnGuardar);
        form.add(btnActualizar);
        form.add(btnEliminar);
        form.add(btnLimpiar);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros (opcionales)"));
        filtros.add(new JLabel("Estado:"));
        filtros.add(cbFiltroEstado);
        filtros.add(new JLabel("Tipo:"));
        filtros.add(cbFiltroTipo);
        JButton btnQuitarFiltros = new JButton("Quitar filtros");
        filtros.add(btnQuitarFiltros);

        JPanel norte = new JPanel(new GridLayout(2, 1));
        norte.add(form);
        norte.add(filtros);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Al seleccionar una fila, se cargan sus datos en el formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int f = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && f >= 0) {
                txtDireccion.setText(modelo.getValueAt(f, 1).toString());
                cbTipo.setSelectedItem(modelo.getValueAt(f, 2));
                cbEstado.setSelectedItem(modelo.getValueAt(f, 3));
            }
        });

        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        cbFiltroEstado.addActionListener(e -> refrescar());
        cbFiltroTipo.addActionListener(e -> refrescar());
        btnQuitarFiltros.addActionListener(e -> {
            cbFiltroEstado.setSelectedItem(TODOS); // dispara refrescar()
            cbFiltroTipo.setSelectedItem(TODOS);
        });
    }

    private void guardar() {
        try {
            Pedido p = new Pedido(Validador.direccion(txtDireccion.getText()),
                    (String) cbTipo.getSelectedItem(), (String) cbEstado.getSelectedItem());
            dao.create(p);
            limpiar();
            alCambiar.run();
            Mensajes.exito(this, "Pedido registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            Mensajes.aviso(this, ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "registrar el pedido", ex);
        }
    }

    private void actualizar() {
        try {
            int fila = tabla.getSelectedRow();
            if (fila < 0) throw new IllegalArgumentException("Seleccione un pedido de la tabla para actualizar.");
            Pedido p = new Pedido((int) modelo.getValueAt(fila, 0), Validador.direccion(txtDireccion.getText()),
                    (String) cbTipo.getSelectedItem(), (String) cbEstado.getSelectedItem());
            if (dao.update(p)) {
                limpiar();
                alCambiar.run();
                Mensajes.exito(this, "Pedido actualizado correctamente.");
            } else {
                Mensajes.error(this, "El pedido ya no existe.");
                alCambiar.run();
            }
        } catch (IllegalArgumentException ex) {
            Mensajes.aviso(this, ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "actualizar el pedido", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.aviso(this, "Seleccione un pedido de la tabla para eliminar.");
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        if (!Mensajes.confirmar(this, "¿Eliminar el pedido " + id + "?")) return;
        try {
            dao.delete(id);
            limpiar();
            alCambiar.run();
            Mensajes.exito(this, "Pedido eliminado.");
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "eliminar el pedido", ex);
        }
    }

    private void limpiar() {
        txtDireccion.setText("");
        cbTipo.setSelectedIndex(0);
        cbEstado.setSelectedIndex(0);
        tabla.clearSelection();
    }

    public void refrescar() {
        try {
            String estado = TODOS.equals(cbFiltroEstado.getSelectedItem()) ? null : (String) cbFiltroEstado.getSelectedItem();
            String tipo = TODOS.equals(cbFiltroTipo.getSelectedItem()) ? null : (String) cbFiltroTipo.getSelectedItem();
            modelo.setRowCount(0);
            for (Pedido p : dao.readFiltrado(estado, tipo)) {
                modelo.addRow(new Object[]{p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
            }
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar los pedidos", ex);
        }
    }
}
