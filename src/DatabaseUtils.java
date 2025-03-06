import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * 1)Register the driver class
 * 2)Create connection
 * 3)Create Statement
 * 4)Execute queries
 * 5)Close Connection
 * 
 * */

public class DatabaseUtils { //Register the driver class

	private static final String URL = "jdbc:mysql://localhost:3306/sqlconnection"; //(2)
	private static final String USER = "root";
	private static final String PASSWORD ="password";
	
	public static Connection getConnection() throws SQLException{
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
	
	
}
