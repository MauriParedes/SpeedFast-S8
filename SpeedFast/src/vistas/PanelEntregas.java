package vistas;

import DAO.EntregaDAO;
import DAO.PedidoDAO;
import DAO.RepartidorDAO;
import Modelo.Entrega;
import Modelo.Pedido;
import Modelo.Repartidor;
import util.Mensajes;
import util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class PanelEntregas extends JPanel {

    private static final String TODOS = "-- Todos --";

    private final EntregaDAO dao = new EntregaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final Runnable alCambiar;

    // Formulario
    private final JComboBox<Pedido> cbPedido = new JComboBox<>();
    private final JComboBox<Repartidor> cbRepartidor = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtHora = new JTextField(8);

    // Filtros (el elemento TODOS es un String; los demás son Pedido / Repartidor)
    private final JComboBox<Object> cbFiltroPedido = new JComboBox<>();
    private final JComboBox<Object> cbFiltroRepartidor = new JComboBox<>();
    private boolean cargandoCombos = false; // evita refrescos en cascada mientras se recargan los combos

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Pedido", "Repartidor", "Fecha", "Hora"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final java.util.List<Entrega> entregasMostradas = new java.util.ArrayList<>();

    public PanelEntregas(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new GridLayout(2, 4, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Datos de la entrega"));
        form.add(new JLabel("Pedido:"));
        form.add(cbPedido);
        form.add(new JLabel("Repartidor:"));
        form.add(cbRepartidor);
        form.add(new JLabel("Fecha (AAAA-MM-DD):"));
        form.add(txtFecha);
        form.add(new JLabel("Hora (HH:MM o HH:MM:SS):"));
        form.add(txtHora);
        limpiarCamposFechaHora();

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRegistrar = new JButton("Registrar entrega");
        JButton btnActualizar = new JButton("Actualizar seleccionada");
        JButton btnEliminar = new JButton("Eliminar seleccionada");
        JButton btnLimpiar = new JButton("Limpiar");
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.setBorder(BorderFactory.createTitledBorder("Listar entregas por..."));
        filtros.add(new JLabel("Pedido:"));
        filtros.add(cbFiltroPedido);
        filtros.add(new JLabel("Repartidor:"));
        filtros.add(cbFiltroRepartidor);
        JButton btnQuitarFiltros = new JButton("Quitar filtros");
        filtros.add(btnQuitarFiltros);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.add(form);
        norte.add(botones);
        norte.add(filtros);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Al seleccionar una fila, se cargan sus datos en el formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int f = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && f >= 0 && f < entregasMostradas.size()) {
                Entrega en = entregasMostradas.get(f);
                seleccionarPorId(cbPedido, en.getIdPedido());
                seleccionarPorId(cbRepartidor, en.getIdRepartidor());
                txtFecha.setText(String.valueOf(en.getFecha()));
                txtHora.setText(String.valueOf(en.getHora()));
            }
        });

        btnRegistrar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        cbFiltroPedido.addActionListener(e -> { if (!cargandoCombos) cargarTabla(); });
        cbFiltroRepartidor.addActionListener(e -> { if (!cargandoCombos) cargarTabla(); });
        btnQuitarFiltros.addActionListener(e -> {
            cargandoCombos = true;
            cbFiltroPedido.setSelectedIndex(0);
            cbFiltroRepartidor.setSelectedIndex(0);
            cargandoCombos = false;
            cargarTabla();
        });
    }

    private Entrega leerFormulario(int id) {
        Pedido p = Validador.seleccionado((Pedido) cbPedido.getSelectedItem(), "un pedido (cree uno antes si no hay)");
        Repartidor r = Validador.seleccionado((Repartidor) cbRepartidor.getSelectedItem(), "un repartidor (cree uno antes si no hay)");
        return new Entrega(id, p.getId(), r.getId(), Validador.fecha(txtFecha.getText()), Validador.hora(txtHora.getText()));
    }

    private void guardar() {
        try {
            dao.create(leerFormulario(0));
            limpiar();
            alCambiar.run();
            Mensajes.exito(this, "Entrega registrada correctamente.");
        } catch (IllegalArgumentException ex) {
            Mensajes.aviso(this, ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "registrar la entrega", ex);
        }
    }

    private void actualizar() {
        try {
            int fila = tabla.getSelectedRow();
            if (fila < 0) throw new IllegalArgumentException("Seleccione una entrega de la tabla para actualizar.");
            Entrega en = leerFormulario((int) modelo.getValueAt(fila, 0));
            if (dao.update(en)) {
                limpiar();
                alCambiar.run();
                Mensajes.exito(this, "Entrega actualizada correctamente.");
            } else {
                Mensajes.error(this, "La entrega ya no existe.");
                alCambiar.run();
            }
        } catch (IllegalArgumentException ex) {
            Mensajes.aviso(this, ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "actualizar la entrega", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.aviso(this, "Seleccione una entrega de la tabla para eliminar.");
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        if (!Mensajes.confirmar(this, "¿Eliminar la entrega " + id + "?")) return;
        try {
            dao.delete(id);
            limpiar();
            alCambiar.run();
            Mensajes.exito(this, "Entrega eliminada.");
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "eliminar la entrega", ex);
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        limpiarCamposFechaHora();
    }

    private void limpiarCamposFechaHora() {
        txtFecha.setText(LocalDate.now().toString());
        txtHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private <T> void seleccionarPorId(JComboBox<T> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object o = combo.getItemAt(i);
            int itemId = (o instanceof Pedido) ? ((Pedido) o).getId() : ((Repartidor) o).getId();
            if (itemId == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private int idSeleccionado(JComboBox<Object> combo) {
        Object o = combo.getSelectedItem();
        return (o instanceof Pedido) ? ((Pedido) o).getId() : (o instanceof Repartidor) ? ((Repartidor) o).getId() : -1;
    }

    public void refrescarCombos() {
        cargandoCombos = true;
        try {
            Pedido pSel = (Pedido) cbPedido.getSelectedItem();
            Repartidor rSel = (Repartidor) cbRepartidor.getSelectedItem();
            int fp = idSeleccionado(cbFiltroPedido), fr = idSeleccionado(cbFiltroRepartidor);

            java.util.List<Pedido> pedidos = pedidoDAO.readAll();
            java.util.List<Repartidor> repartidores = repartidorDAO.readAll();

            cbPedido.removeAllItems();
            cbFiltroPedido.removeAllItems();
            cbFiltroPedido.addItem(TODOS);
            for (Pedido p : pedidos) { cbPedido.addItem(p); cbFiltroPedido.addItem(p); }

            cbRepartidor.removeAllItems();
            cbFiltroRepartidor.removeAllItems();
            cbFiltroRepartidor.addItem(TODOS);
            for (Repartidor r : repartidores) { cbRepartidor.addItem(r); cbFiltroRepartidor.addItem(r); }

            if (pSel != null) seleccionarPorId(cbPedido, pSel.getId());
            if (rSel != null) seleccionarPorId(cbRepartidor, rSel.getId());
            for (int i = 1; i < cbFiltroPedido.getItemCount(); i++)
                if (((Pedido) cbFiltroPedido.getItemAt(i)).getId() == fp) cbFiltroPedido.setSelectedIndex(i);
            for (int i = 1; i < cbFiltroRepartidor.getItemCount(); i++)
                if (((Repartidor) cbFiltroRepartidor.getItemAt(i)).getId() == fr) cbFiltroRepartidor.setSelectedIndex(i);
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar pedidos y repartidores en los combos", ex);
        } finally {
            cargandoCombos = false;
        }
    }

    public void cargarTabla() {
        try {
            int idPedido = idSeleccionado(cbFiltroPedido);
            int idRep = idSeleccionado(cbFiltroRepartidor);
            java.util.List<Entrega> lista;
            if (idPedido > 0) {
                lista = dao.readByPedido(idPedido);
                if (idRep > 0) lista.removeIf(e -> e.getIdRepartidor() != idRep); // ambos filtros activos
            } else if (idRep > 0) {
                lista = dao.readByRepartidor(idRep);
            } else {
                lista = dao.readAll();
            }
            modelo.setRowCount(0);
            entregasMostradas.clear();
            for (Entrega e : lista) {
                entregasMostradas.add(e);
                modelo.addRow(new Object[]{e.getId(),
                        e.getDescripcionPedido(), e.getIdRepartidor() + " - " + e.getNombreRepartidor(),
                        e.getFecha(), e.getHora()});
            }
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar las entregas", ex);
        }
    }

    public void refrescar() {
        refrescarCombos();
        cargarTabla();
    }
}
