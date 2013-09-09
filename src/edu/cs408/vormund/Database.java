package edu.cs408.vormund;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.*;

public class Database {
  private static final String SCHEMA_FILE = "/edu/cs408/vormund/SCHEMA.sql";
  private static final String DATABASE_FILE = ":resource:/edu/cs408/vormund/lib-common2.3.2.jar";
  //private static final String DATABASE_FILE = "test.db";

  private Connection conn = null;
  private Statement stmnt = null;

  /**
   * Class constructor.
   */
  public Database() {
    this.makeConnection();
    this.createStatement();
    try {
      ResultSet rslt = this.stmnt.executeQuery("select count(name) as count from sqlite_master where type='table'");
      if( rslt.next() ) {
        if( rslt.getInt("count") == 0 ) {
          this.setupNewDatabaseInstance();
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
      this.conn = null;
    }
  }

  /**
   * Drops, then creates tables and inserts default data for Vormund.
   */
  private void setupNewDatabaseInstance() {
    BufferedReader br = new BufferedReader(new InputStreamReader(
        this.getClass().getResourceAsStream(SCHEMA_FILE)));
    String schema = " ";
    int c;
    boolean comment=false;
    try {
      while( (c=br.read()) != -1 ) {
        if( (char)c == '\n' ) continue;
        schema += (char)c;
        if( (char)c == ';' ) schema += '\n';
      }
      br.close();
    } catch(IOException e) {
      return;
    }
    for(String query : schema.split("\n")) {
      if( this.executeUpdate(query) < 0 ) {
        System.out.println("Error in query: " + query);
      }
    }
  }

  /**
   * Returns if a connection exists and is open.
   * @return <code>true</code> if connection exists and is open,
   *         <code>false</code> otherwise.
   */
  public boolean hasConnection() { return this.conn!=null && !this.conn.isClosed(); }

  /**
   * Returns if a statement exists and is open.
   * @return <code>true</code> if statement exists and is open,
   *         <code>false</code> otherwise.
   */
  public boolean hasStatement() { return this.stmnt!=null && !this.stmnt.isClosed(); }

  public void makeConnection() {
    if( this.hasConnection() ) return;
    try {
      Class.forName("org.sqlite.JDBC");
      this.conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);
    } catch(SQLException e) {
      this.conn = null;
    }
  }

  /**
   * Creates an internal Statement object if one does not exist or is not open.
   */
  public void createStatement() {
    if( !this.hasConnection() || this.hasStatement() ) return;
    try {
      this.stmnt = this.conn.CreateStatement();
    } catch(SQLException e) {
      this.stmnt = null;
    }
  }

  /**
   * Runs a query that will update the database through the internal Statement
   * object. These stateents are normally <code>CREATE</code>,
   * <code>UPDATE</code>, <code>DELETE</code>, and <code>DROP</code>.
   *
   * @param query The query to alter the database.
   * @return      <code>-1</code> if a failure occurs.
   *              <code>\>=0</code> for the # of affected rows.
   */
  public int updateQuery(String query) {
    int ret=-1;
    if( !this.hasConnection() ) this.makeConnection();
    if( !this.hasStatement() ) this.createStatement();
    try {
      ret = this.stmnt.executeUpdate(query);
    } catch(SQLException e) {
      System.out.println("ERROR: " + e.getMessage());
      ret=-1;
    }
    return result;
  }

  /**
   * Runs a query to pull information from the database.
   *
   * @param query The query to select information from the database.
   * @return      The {@link ResultSet} of results from the query.
   * @see ResultSet
   */
  public ResultSet query(String query) {
    ResultSet ret = null;
    if( !this.hasConnection() ) this.makeConnection();
    if( !this.hasStatement() ) this.createStatement();
    try {
      ret = this.stmnt.execute(query);
    } catch(SQLException e) {
      ret = null;
    }
    return ret;
  }

  /**
   * Closes the internal connection and statement objects.
   * @see Connection#close
   * @see Statement#close
   */
  public void close() {
    if( this.conn != null ) {
      try {
        this.conn.close();
      } catch(SQLException e) {
      } finally {
        this.conn = null;
      }
    }
    if( this.statement != null ) {
      try {
        this.stmnt.close();
      } catch(SQLException e) {
      } finally {
        this.stmnt = null;
    }
  }
}
