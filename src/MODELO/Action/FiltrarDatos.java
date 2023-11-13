package MODELO.Action;

import DAO.ConexionSQL;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class FiltrarDatos {

    public static void filterAndPopulateTable(JTable jTable, JTextField dniTrabajador, JTextField dniCliente, JTextField montoMinimo, JTextField montoMaximo) {
        DefaultTableModel tableModel = (DefaultTableModel) jTable.getModel();
        tableModel.setRowCount(0);

        ConexionSQL conexionSQL = new ConexionSQL();
        Connection connection = conexionSQL.conexion();

        if (connection != null) {
            try {
                String query = "SELECT * FROM venta WHERE trabajador LIKE ? AND cliente LIKE ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, "%" + dniTrabajador.getText() + "%");
                statement.setString(2, "%" + dniCliente.getText() + "%");

                ResultSet resultSet = statement.executeQuery();

                LinkedList<Object[]> results = new LinkedList<>(); // Lista enlazada para almacenar los resultados

                while (resultSet.next()) {
                    Object[] rowData = new Object[5];
                    rowData[0] = resultSet.getString("idVenta");
                    rowData[1] = resultSet.getString("trabajador");
                    rowData[2] = resultSet.getString("cliente");
                    rowData[3] = resultSet.getDouble("total");
                    rowData[4] = resultSet.getDate("fecha");
                    results.add(rowData); // Almacenar cada fila en la lista enlazada
                }

                double min = montoMinimo.getText().isEmpty() ? 0 : Double.parseDouble(montoMinimo.getText());
                double max = montoMaximo.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(montoMaximo.getText());

                LinkedList<Object[]> filteredResults = new LinkedList<>(); // Lista enlazada para almacenar resultados filtrados

                for (Object[] row : results) {
                    double total = (Double) row[3];
                    if (total >= min && total <= max) {
                        filteredResults.add(row); // Filtrar por rango de monto
                    }
                }

                for (Object[] row : filteredResults) {
                    tableModel.addRow(row);
                }

                jTable.setModel(tableModel);

            } catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage());
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        }
    }

    private static boolean ascending = true;

    public static void setupTableSorting(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Creamos un comparador personalizado para manejar diferentes tipos de datos
        Comparator<Object> customComparator = (Object obj1, Object obj2) -> {
            if (obj1 instanceof Integer && obj2 instanceof Integer) {
                Integer int1 = (Integer) obj1;
                Integer int2 = (Integer) obj2;
                return int1.compareTo(int2);
            } else {
                String str1 = obj1.toString();
                String str2 = obj2.toString();
                return str1.compareToIgnoreCase(str2);
            }
        };

        // Asignamos el comparador personalizado para todas las columnas
        for (int i = 0; i < model.getColumnCount(); i++) {
            sorter.setComparator(i, customComparator);
        }

        // Asignamos el listener para detectar clics en los encabezados
        table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = table.getTableHeader().columnAtPoint(evt.getPoint());
                if (column != -1) {
                    if (ascending) {
                        sorter.setComparator(column, Comparator.reverseOrder());
                        ascending = false;
                    } else {
                        sorter.setComparator(column, customComparator);
                        ascending = true;
                    }
                    sorter.sort();
                }
            }
        });
    }

}
