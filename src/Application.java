import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {

	public static void main(String[] args) {
		try {
			Connection con = DatabaseUtils.getConnection();//Create connection
			//operations to execute for database here 
			
			if(con != null) {
				System.out.println("Connected to the Database successfully");
			}
		}
		catch(SQLException e){
			System.out.println("SQL Error: "+e.getMessage());
			e.printStackTrace();
		}

	}
	public void mainMenu() {
		System.out.println("Press 1 to Add data");
		System.out.println("Press 2 to edit data");
		System.out.println("Press 3 to delete data");
	}

}
