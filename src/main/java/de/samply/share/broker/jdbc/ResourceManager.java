package de.samply.share.broker.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

/**
 * The Class ResourceManager.
 */
public class ResourceManager {

  private static final DataSource dataSource;

  private static final String JAVA_COMP_ENV = "java:comp/env";

  private static final String JDBC_POSTGRES = "jdbc/postgres/samply.share.broker";

  static {
    try {
      Context initContext = new InitialContext();
      Context context = (Context) initContext.lookup(JAVA_COMP_ENV);
      dataSource = (DataSource) context.lookup(JDBC_POSTGRES);
    } catch (NamingException ex) {
      throw new ExceptionInInitializerError("dataSource not initialized");
    }
  }

  /**
   * Gets the DSL context.
   *
   * @param connection the sql connection
   * @return the DSL context
   */
  public static DSLContext getDslContext(Connection connection) {
    Configuration configuration = new DefaultConfiguration().set(connection)
        .set(SQLDialect.POSTGRES);
    return DSL.using(configuration);
  }

  /**
   * Gets the connection.
   *
   * @return the connection
   * @throws SQLException the SQL exception
   */
  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public static DataSource getDataSource() {
    return dataSource;
  }

}
