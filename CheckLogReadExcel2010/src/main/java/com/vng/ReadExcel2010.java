package com.vng;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.poi.xssf.usermodel.*;
import java.util.regex.*;

/**
 * Hello world!
 *
 */
public class ReadExcel2010 {

    public static Connection con;
    private static String tableName, tableColumn;
    private static Integer nOfCol = 0;
    private static Integer[] col;
    private static String[] type;

    public static Properties LoadProperties() {
        // Load file properties
        Properties prop = new Properties();
        InputStream input = null;

        try {
            String filename = "data_loader.properties";
            input = new FileInputStream(filename);

            if (input == null) {
                System.out.println("Sorry, unable to find " + filename);
                return null;
            }
            prop.load(input);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            return prop;
        }
    }

    public static boolean ConnectToMySQL() {
        Properties prop = LoadProperties();
        if (prop == null) {
            return false;
        }
        try {
            String driverName = "org.gjt.mm.mysql.Driver"; // MySQL MM JDBC driver
            Class.forName(driverName);

            // Create a connection to the database
            String serverName = prop.getProperty("db.host") + ":" + prop.getProperty("db.port");
            String mydatabase = prop.getProperty("db.name");
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = prop.getProperty("db.user");
            String password = prop.getProperty("db.password");
            tableName = prop.getProperty("db.table.name");
            tableColumn = prop.getProperty("db.table.columns");
            Pattern p = Pattern.compile("#([0-9]+)#");
            Matcher m = p.matcher(tableColumn);
            while (m.find()) {
                nOfCol++;
            }
            col = new Integer[nOfCol];
            type = new String[nOfCol];
            p = Pattern.compile("#[0-9]+#");
            m = p.matcher(tableColumn);
            for (int i = 0; i < nOfCol; i++) {
                m.find();
                col[i] = Integer.parseInt(m.group(0).substring(1, m.group(0).length() - 1));
            }
            tableColumn += ";";
            p = Pattern.compile("#[^#;]+;");
            m = p.matcher(tableColumn);
            for (int i = 0; i < nOfCol; i++) {
                if (m.find()) {
                    type[i] = m.group(0).substring(1, m.group(0).length() - 1);
                }
            }
            con = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
            return false;
        } catch (SQLException e) {
            System.out.println("Connect failed");
            return false;
        }

        System.out.println("Connect success");
        return true;
    }

    public static void main(String[] args) {
        ConnectToMySQL();
        String[] X = {"A", "B", "C", "D", "E", "F", "G"};
        // insert into action_map values (0,'enum','dichuyen trong toa do');

        File excel = new File("FV-Thao tác các loại Action.xlsx");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(excel);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XSSFWorkbook wb = null;
        try {
            wb = new XSSFWorkbook(fis);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XSSFSheet ws = wb.getSheet("Sheet1");
        int rowNum = ws.getLastRowNum() + 1;
        XSSFCell[] data = new XSSFCell[nOfCol];
        for (int i = 2; i < rowNum; i++) {
            XSSFRow row = ws.getRow(i);
            for (int j = 0; j < nOfCol; j++) {
                data[j] = row.getCell(col[j]);
                if (data[j] == null || data[j].toString().equals("")) {
                    for (int k = i;; k--) {
                        XSSFRow findRow = ws.getRow(k);
                        XSSFCell findCell = findRow.getCell(col[j]);
                        if (((findCell != null) && (!findCell.toString().equals("")))) {
                            data[j] = findCell;
                            break;
                        }

                    }
                } 
            }
            String insert = "";
            for (int k = 0; k < nOfCol; k++) {
                if (type[k].equals("int")) {
                    insert += Integer.parseInt(data[k].getRawValue());
                } else {
                    insert = insert + "'" + data[k].getStringCellValue() + "'";
                }
                if (k < nOfCol - 1) {
                    insert += ",";
                }
            }
            try {
                String sql = "INSERT INTO " + tableName + " VALUES (" + insert + ");";
                Statement st = con.createStatement();
                st.execute(sql);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
