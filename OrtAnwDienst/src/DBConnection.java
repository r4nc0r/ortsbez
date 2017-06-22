
import fu.keys.LSIClassCentreDB;
import fu.util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by samue on 22.06.2017.
 */
public class DBConnection {
    private static String DBparams;
    private static String SQLStatement;


    public DBConnection(String dbparams, String sqlstatement){
        this.DBparams =dbparams;
        this.SQLStatement = sqlstatement;
    }

    public void getDBData() {
        Connection connection = null;
        Statement statement;
        ResultSet resultSet;
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

                cnt++;
            }

        } catch (Exception e) {
            System.out.println("Error processing DB queries: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

