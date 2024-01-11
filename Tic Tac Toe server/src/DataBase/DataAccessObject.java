package DataBase;

import DTO.Records;
import DTO.UserData;
import org.apache.derby.jdbc.ClientDriver;
import java.sql.*;
import java.util.ArrayList;

public class DataAccessObject {
    private static String dbURL;
    private static String dbName;
    private static String dbPassword;
    private static Connection con;
    
    public static void start() throws SQLException {
        dbURL = "jdbc:derby://localhost:1527/Tic Tac Toe Game";
        dbName = "java";
        dbPassword = "j1a2v3a4"; 
        DriverManager.registerDriver(new ClientDriver());
        con = DriverManager.getConnection(dbURL, dbName, dbPassword);
    }
    
    public static ArrayList<UserData> getAvailableUser() throws SQLException {
        ArrayList<UserData> availableUsers = new ArrayList<UserData>();
        
        String sqlStat = "select * from UserData WHERE is_Available = true and is_OnGame = false";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        ResultSet rs = pst.executeQuery(sqlStat);
        while (rs.next()) {
            UserData user = new UserData(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                        rs.getLong(5), rs.getBoolean(6), rs.getBoolean(7));
            availableUsers.add(user);
        }
        return availableUsers;
    }
    
    public static int addUser(UserData user) throws SQLException {
        int result = 0;
        String sqlStat = "insert into UserData values(?, ?, ?, ?, 0, false, false)";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        pst.setInt(1, getNextID("UserData"));
        pst.setString(2, user.getName());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPassword());
        result = pst.executeUpdate();
        pst.close();
        return result;
    }
    
    public static UserData getUser(String email, String password) throws SQLException {
       UserData user = null;
       PreparedStatement pst = con.prepareStatement("select * from UserData where email = ? and password=?");
       pst.setString(1, email);
       pst.setString(2, password);
       ResultSet rs = pst.executeQuery();
       if (rs.next()) {
		user = new UserData(rs.getInt(1),rs.getString(2),rs.getString(3),
		rs.getString(4),rs.getLong(7),rs.getBoolean(5),rs.getBoolean(6) );
       }
       pst.close();
       return user ;
        
        
    }
    
    public static int getNextID(String tableName) throws SQLException {
        int id;
        String sqlStat = "select max(id) from ?";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        pst.setString(1, tableName);
        ResultSet rs = pst.executeQuery();
        
        if(rs.next())
            id = rs.getInt(1) + 1;
        else 
            id = 1;
        
        pst.close();
        rs.close();
        return id;
    }
    
    public static int updateScore(UserData user) throws SQLException {
       int result = 0;
        user.updateScore();
        String sqlStat = "update userdata set score = ? where id = ?";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        pst.setLong(1 , user.getScore());
        pst.setInt(2 , user.getId());
        result = pst.executeUpdate();
        pst.close();
        return result;
    }
    
    public static int updateStatus(UserData user) throws SQLException {
        
        int result = 0;
        String sqlStat = "update userdata set is_available = ?, is_ongame = ? where id = ?";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        pst.setBoolean(1 , user.getIs_available());
        pst.setBoolean(2 , user.getIs_onGame());
        pst.setInt(3 , user.getId());
        result = pst.executeUpdate();
        pst.close();
        
        return result;
    }
    
    public static ArrayList<Records> getAllRecords(int id) throws SQLException {
        ArrayList<Records> allRecords = new ArrayList<Records>();
        
        String sqlStat = "select * from Records WHERE userid = ?";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery(sqlStat);
        while (rs.next()) {
            Records record = new Records(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
            allRecords.add(record);
        }
        return allRecords;
    }
    
    public static int addRecord(Records record) throws SQLException {
        int result = 0;
        String sqlStat = "insert into Records values(?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sqlStat);
        pst.setInt(1, getNextID("Records"));
        pst.setString(2, record.getName());
        pst.setString(3, record.getSteps());
        pst.setInt(4, record.getUserId());
        result = pst.executeUpdate();
        pst.close();
        return result;
    }
    
    public static void stop() throws SQLException {
        con.close();
    }
    
}