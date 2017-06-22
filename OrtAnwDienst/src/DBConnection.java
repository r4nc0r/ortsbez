
import fu.keys.LSIClassCentreDB;
import fu.util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class DBConnection {
    private static String DBparams;
    private static String SQLStatement;
    private static ArrayList<String> result;

    public DBConnection(String dbparams, String sqlstatement){
        this.DBparams =dbparams;
        this.SQLStatement = sqlstatement;
    }

    public ArrayList<String> getDBData() {
        Connection connection = null;
        Statement statement;
        ResultSet resultSet;
        result = new ArrayList<String>();
        try {
            DBUtil.parseDBparams(this.DBparams);
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);
            LSIClassCentreDB.initFromDB(connection);
        } catch (Exception e) {
            System.out.println("Error initialising DB access: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        try {
            statement = connection.createStatement();
            statement.setFetchSize(1000);
            resultSet = statement.executeQuery(String.format(this.SQLStatement));
            int cnt = 0;
            while (resultSet.next()) {

                result.add(resultSet.getString(1));
                cnt++;
            }

        } catch (Exception e) {
            System.out.println("Error processing DB queries: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        return result;
    }
}

