package dev.krocon.shop.ep011.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

/** Folge 011 (Problem): ResultSet-Mapping von Hand, Spalte für Spalte. */
@Repository("ep011OrderDao")
public class OrderDao {

  private final DataSource dataSource;

  public OrderDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<JdbcOrder> findAll() throws SQLException {
    return query("total");
  }

  /** Wie im Video: Tippfehler im Spaltennamen – der Compiler merkt nichts. */
  public List<JdbcOrder> findAllWithTypo() throws SQLException {
    return query("totl");
  }

  private List<JdbcOrder> query(String totalColumn) throws SQLException {
    // DataSourceUtils: dieselbe Verbindung wie die laufende Transaktion
    Connection con = DataSourceUtils.getConnection(dataSource);
    List<JdbcOrder> result = new ArrayList<>();
    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {
      while (rs.next()) {
        JdbcOrder o = new JdbcOrder();
        o.setId(rs.getLong("id"));
        o.setTotal(rs.getBigDecimal(totalColumn));
        o.setCustomerId(rs.getLong("customer_id"));   // nur eine Zahl
        result.add(o);
      }
    } finally {
      DataSourceUtils.releaseConnection(con, dataSource);
    }
    return result;   // … und das für jede Tabelle
  }
}
