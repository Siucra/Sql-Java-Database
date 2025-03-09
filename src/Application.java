import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;



public class Application {
	static Scanner input = new Scanner(System.in);

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
		//while true
		System.out.println("-".repeat(16));
		System.out.println("Press 1 to Add data");
		System.out.println("Press 2 to edit data");
		System.out.println("Press 3 to delete data");
		System.out.println("Press 4 to create new table");
		System.out.println("Press 5 to Add column to table");
		System.out.println("Press 6 to exit program");
		System.out.println("-".repeat(16));
		
		switch(input.nextInt()) {
			case 5:{
				createNewTable();
				break;
			}
			default:{
				System.out.println("Invalid choice. Please enter a valid number.");
				break;
			}
		}
	}
	public void createNewTable() {
		System.out.println("Enter the name of the table: ");
		String tableName = input.nextLine();
		
		System.out.println("How many columns would you like to add to table "+tableName+" ?");
		int columnAmount = input.nextInt();
		
		//used to construct and use multiple strings at once
		StringBuilder sql = new StringBuilder("CREATE TABLE "+ tableName + "(");
		
		for(int i =0; i<columnAmount; i++) {
			System.out.println("Enter column name: ");
			String columnName = input.nextLine();
			
			System.out.println("Enter data type for " + columnName + ": ");
			String dataType = input.nextLine();
			
			System.out.println("Is this column a Primary Key? (yes/no)");
			String isPrimaryKey = input.nextLine().toLowerCase();
			
			System.out.println("Can this column be NULL? (yes=s/no)");
			String nullColumn = input.nextLine().toLowerCase();
					
			//append used to connect strings in StringBuilder
			sql.append(columnName).append(" ").append(dataType);
			
			//append NOT NULL if the column cannot be null
			if(nullColumn.equals("no")) {
				sql.append(" NOT NULL");
			}
			
			//append PRIMARY KEY if user chose yes
			if(isPrimaryKey.equals("yes")) {
				sql.append(" PRIMARY KEY");
			}
			
			
			//add a comma if its not the last column
			if(i<columnAmount - 1) {
				sql.append(", ");
			}
		}
		
		//end of table syntax
		sql.append(");");
		
	}

}
