package vistas;

import javax.swing.*;


public class VentanaPrincipal extends JFrame {

    private final PanelRepartidores panelRepartidores;
    private final PanelPedidos panelPedidos;
    private final PanelEntregas panelEntregas;

    public VentanaPrincipal() {
        setTitle("SpeedFast - Gestión de Pedidos y Entregas");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Runnable refrescarTodo = this::refrescarTodo;
        panelRepartidores = new PanelRepartidores(refrescarTodo);
        panelPedidos = new PanelPedidos(refrescarTodo);
        panelEntregas = new PanelEntregas(refrescarTodo);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Repartidores", panelRepartidores);
        tabs.addTab("Pedidos", panelPedidos);
        tabs.addTab("Entregas", panelEntregas);
        add(tabs);

        refrescarTodo(); // carga inicial
    }

    private void refrescarTodo() {
        panelRepartidores.refrescar();
        panelPedidos.refrescar();
        panelEntregas.refrescar();
    }
}
