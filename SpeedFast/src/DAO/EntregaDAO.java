package DAO;

import Conexion.ConexionDB;
import Modelo.Entrega;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntregaDAO {

    private static final String SELECT_DETALLE =
            "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, " +
            "       CONCAT(p.id, ' - ', p.direccion) AS desc_pedido, r.nombre AS nombre_repartidor " +
            "FROM entregas e " +
            "LEFT JOIN pedidos p ON p.id = e.id_pedido " +
            "LEFT JOIN repartidores r ON r.id = e.id_repartidor ";

    public boolean create(Entrega e) throws SQLException {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getIdPedido());
            ps.setInt(2, e.getIdRepartidor());
            ps.setDate(3, e.getFecha());
            ps.setTime(4, e.getHora());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Entrega> readAll() throws SQLException {
        return consultar(SELECT_DETALLE + "ORDER BY e.id", null);
    }
    public List<Entrega> readByPedido(int idPedido) throws SQLException {
        return consultar(SELECT_DETALLE + "WHERE e.id_pedido = ? ORDER BY e.id", idPedido);
    }

    public List<Entrega> readByRepartidor(int idRepartidor) throws SQLException {
        return consultar(SELECT_DETALLE + "WHERE e.id_repartidor = ? ORDER BY e.id", idRepartidor);
    }

    public boolean update(Entrega e) throws SQLException {
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getIdPedido());
            ps.setInt(2, e.getIdRepartidor());
            ps.setDate(3, e.getFecha());
            ps.setTime(4, e.getHora());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM entregas WHERE id = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Entrega> consultar(String sql, Integer parametro) throws SQLException {
        List<Entrega> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Entrega e = new Entrega(
                            rs.getInt("id"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_repartidor"),
                            rs.getDate("fecha"),
                            rs.getTime("hora"));
                    e.setDescripcionPedido(rs.getString("desc_pedido"));
                    e.setNombreRepartidor(rs.getString("nombre_repartidor"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }
}
