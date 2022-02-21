import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

interface CakeRepositoryInterface {
  public Map<Integer, CakeModel> GetAllCake() throws SQLException;

  public boolean InsertCake(Map<Integer, CakeModel> value) throws SQLException;

  public void UpdateCake(Map<Integer, CakeModel> value) throws SQLException;
}

public class UasJava extends JFrame implements ActionListener {
  JPanel jPanel;
  JMenuBar menuBar;
  JMenu menu;
  JMenuItem insertMenu;
  JMenuItem list;

  UasJava() {
    this.initGuiMain();
  }

  private void initGuiMain() {
    this.jPanel = new JPanel();
    this.menuBar = new JMenuBar();
    this.menu = new JMenu("Menu");
    this.insertMenu = new JMenuItem("Insert");
    this.list = new JMenuItem("List");
    this.insertMenu.addActionListener(this);
    this.list.addActionListener(this);
    this.menu.add(insertMenu);
    this.menu.add(list);
    this.menuBar.add(this.menu);
  }

  public static void main(String[] args) throws SQLException {
    CakeRepositoryInterface cakeRepositoryInterface = new CakeRepository();
    String[] columns = {
      "Kode Bahan",
      "Nama Bahan",
      "Satuan",
      "Jenis Bahan",
      "Harga",
      "Jumlah",
    };
    String[][] data = new String[10][6];
    Map<Integer, CakeModel> results = cakeRepositoryInterface.GetAllCake();
    for (int i = 0; i < results.size(); i++) {
      data[i][0] = results.get(i).getKodeBahan();
      data[i][1] = results.get(i).getNamaBahan();
      data[i][2] = results.get(i).getJenisBahan();
      data[i][3] = results.get(i).getSatuan();
      data[i][4] = results.get(i).getHarga() + "";
      data[i][5] = results.get(i).getJumlah() + "";
    }
    JTable jTable = new JTable(data, columns);
    jTable.setBounds(200, 200, 700, 700);
    JScrollPane sp = new JScrollPane(jTable);
    UasJava uasJava = new UasJava();
    uasJava.add(jTable);
    uasJava.add(sp);
    uasJava.setSize(700, 700);
    uasJava.setVisible(true);
    uasJava.getContentPane().setBackground(Color.WHITE);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == insertMenu) {
      InsertComponent insertPanel = new InsertComponent();
      this.removeAll();
      this.jPanel.add(insertPanel.getInsertPanel());
    }
    if (e.getSource() == list) {
      this.initGuiMain();
    }
  }
}

class InsertComponent implements ActionListener {
  JTextField namaBahan;
  JFormattedTextField jumlah, harga;
  JButton submitInsert;
  JComboBox satuan, jenisBahan;
  JPanel panel;

  InsertComponent() {
    this.initGui();
  }

  private void initGui() {
    this.panel = new JPanel();
    NumberFormat numberFormat = NumberFormat.getInstance();
    NumberFormatter intFormater = new NumberFormatter(numberFormat);
    intFormater.setValueClass(Integer.class);
    intFormater.setMinimum(0);
    intFormater.setMaximum(Integer.MAX_VALUE);
    String[] satuanValue = { "Kg", "Butir", "Botol", "Buah" };
    String[] jenisBahanValue = { "Baku", "Dekorasi" };
    this.satuan = new JComboBox(satuanValue);
    this.jenisBahan = new JComboBox(jenisBahanValue);
    this.satuan.setBounds(50, 50, 90, 20);
    this.jenisBahan.setBounds(50, 50, 90, 20);
    this.namaBahan = new JTextField("Nama Bahan");
    this.jumlah = new JFormattedTextField(intFormater);
    this.harga = new JFormattedTextField(intFormater);
    this.jumlah.setText("Jumlah");
    this.harga.setText("Harga");
    this.submitInsert = new JButton();
    this.submitInsert.setText("Insert");
    this.panel.add(namaBahan);
    this.panel.add(jenisBahan);
    this.panel.add(namaBahan);
    this.panel.add(jumlah);
    this.panel.add(harga);
    this.panel.add(this.submitInsert);
    this.panel.setSize(700, 700);
    this.panel.setVisible(true);
    this.panel.setLayout(new GridLayout(5, 5));
    this.submitInsert.addActionListener(this);
  }

  public JPanel getInsertPanel() {
    return this.panel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Map<Integer, CakeModel> value = new HashMap<Integer, CakeModel>();
    CakeModel model = new CakeModel(
      1,
      "",
      namaBahan.getText(),
      satuan.getSelectedItem().toString(),
      jenisBahan.getSelectedItem().toString(),
      Integer.parseInt(harga.getValue().toString()),
      Integer.parseInt(jumlah.getValue().toString())
    );
    value.put(model.getKey(), model);
    CakeRepositoryInterface cakeRepositoryInterface = new CakeRepository();
    try {
      var insert = cakeRepositoryInterface.InsertCake(value);
      if (insert) {
        JOptionPane.showMessageDialog(null, "Success");
      }
    } catch (SQLException exception) {
      JOptionPane.showMessageDialog(null, exception.getMessage());
    }
  }
}

class DatabaseConnection {
  private static Connection conn = null;
  static final String url = "jdbc:mysql://localhost:3306/thecake";

  static {
    String user = "root";
    String password = "root";
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      conn = DriverManager.getConnection(url, user, password);
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }

  public static Connection getConnection() {
    return conn;
  }
}

class CakeRepository implements CakeRepositoryInterface {
  static Connection con = DatabaseConnection.getConnection();

  @Override
  public Map<Integer, CakeModel> GetAllCake() throws SQLException {
    Statement stmt = con.createStatement();
    Map<Integer, CakeModel> list = new HashMap<Integer, CakeModel>();
    String query = "SELECT * FROM produk";
    ResultSet result = stmt.executeQuery(query);
    int i = 0;
    try {
      while (result.next()) {
        String kodeBahan = result.getString("KodeBahan");
        String namaBahan = result.getString("NamaBahan");
        String satuan = result.getString("Satuan");
        String jenisBahan = result.getString("JenisBahan");
        int harga = result.getInt("Harga");
        int jumlah = result.getInt("Jumlah");
        CakeModel cakeModel = new CakeModel(
          i,
          kodeBahan,
          namaBahan,
          satuan,
          jenisBahan,
          harga,
          jumlah
        );
        list.put(cakeModel.getKey(), cakeModel);
        i++;
      }
      return list;
    } catch (SQLException e) {
      throw e;
    }
  }

  @Override
  public boolean InsertCake(Map<Integer, CakeModel> value) throws SQLException {
    Statement stmt = con.createStatement();
    ResultSet lastValue = stmt.executeQuery("SELECT KodeBahan FROM produk");
    if (lastValue.last()) {
      String kodeBahan =
        this.generateKodeBahan(lastValue.getString("KodeBahan"));
      String query =
        "INSERT INTO produk (`KodeBahan`, `NamaBahan`, `Satuan`, `JenisBahan`, `Harga`, `Jumlah`) VALUES ('%s','%s','%s','%s',%d,%d)";
      query =
        String.format(
          query,
          kodeBahan,
          value.get(1).getNamaBahan(),
          value.get(1).getSatuan(),
          value.get(1).getJenisBahan(),
          value.get(1).getHarga(),
          value.get(1).getJumlah()
        );
      try {
        return stmt.execute(query);
      } catch (SQLException e) {
        throw e;
      }
    }
    return false;
  }

  @Override
  public void UpdateCake(Map<Integer, CakeModel> value) throws SQLException {
    Statement stmt = con.createStatement();
    String query = "DELETE FROM product WHERE = %d";
    query = String.format(query, value.get(1).getKodeBahan());
    try {
      stmt.execute(query);
    } catch (SQLException e) {
      throw e;
    }
  }

  private String generateKodeBahan(String kodeBahan) {
    String extract = kodeBahan.substring(1);
    int numberCode = Integer.parseInt(extract);
    return "A" + numberCode++;
  }
}

class CakeModel {
  private Integer key;
  private String kodeBahan;
  private String namaBahan;
  private String satuan;
  private String jenisBahan;
  private int harga;
  private int jumlah;

  CakeModel(
    Integer key,
    String kodeBahan,
    String namaBahan,
    String satuan,
    String jenisBahan,
    int harga,
    int jumlah
  ) {
    this.key = key;
    this.kodeBahan = kodeBahan;
    this.namaBahan = namaBahan;
    this.satuan = satuan;
    this.jenisBahan = jenisBahan;
    this.harga = harga;
    this.jumlah = jumlah;
  }

  public Integer getKey() {
    return this.key;
  }

  public String getKodeBahan() {
    return this.kodeBahan;
  }

  public String getNamaBahan() {
    return this.namaBahan;
  }

  public String getSatuan() {
    return this.satuan;
  }

  public String getJenisBahan() {
    return this.jenisBahan;
  }

  public int getHarga() {
    return this.harga;
  }

  public int getJumlah() {
    return this.jumlah;
  }
}
