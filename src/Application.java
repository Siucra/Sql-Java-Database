import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
				Application app = new Application(); //Encapsulation over static methods
				app.mainMenu(con);
				
			}
		}
		catch(SQLException e){
			System.out.println("SQL Error: "+e.getMessage());
			e.printStackTrace();
		}

	}
	public void mainMenu(Connection con) {
		//while true
		System.out.println("-".repeat(16));
		System.out.println("Press 1 to Add data");
		System.out.println("Press 2 to edit data");
		System.out.println("Press 3 to delete data");
		System.out.println("Press 4 to Add column to table");
		System.out.println("Press 5 to create new table");
		System.out.println("Press 6 to exit program");
		System.out.println("-".repeat(16));
		
		int choice = input.nextInt();
		input.nextLine();//consume leftover newline
		switch(choice) {
			case 5:{
				createNewTable(con);
				break;
			}
			default:{
				System.out.println("Invalid choice. Please enter a valid number.");
				break;
			}
		}
	}
	public void createNewTable(Connection con) {
	
		System.out.println("Enter the name of the table: ");
		String tableName = input.nextLine().trim();
		
		//use backticks to handle reserved words and special characters
		tableName = "`" + tableName + "`";
		
		if(tableName.isEmpty()) {//.isEmpty used for String variables
			System.out.println("Invalid input. Please try again.");
			return;
		}
		
		System.out.println("How many columns would you like to add to table "+tableName+" ?");
		if(!input.hasNextInt()) {
			System.out.println("Invalid number of columns. Please enter an integer.");
			input.nextLine(); //clear invalid input
			return;
		}
		
		int columnAmount = input.nextInt();
		input.nextLine();//
		
		//used to construct and use multiple strings at once
		StringBuilder sql = new StringBuilder("CREATE TABLE "+ tableName + "(");
		String primaryKeyColumn = "";
		boolean firstColumn = true;
		
		
		for(int i =0; i<columnAmount; i++) {
			if(!firstColumn) {
				sql.append(", ");
			}
			System.out.println("Enter column name: ");
			String columnName = input.nextLine();
			//use backticks to handle reserved words and special characters
			columnName = "`" + columnName + "`";
			
			System.out.println("Enter data type for " + columnName + ": ");
			String dataType = input.nextLine();
			
			if(dataType.equalsIgnoreCase("String")) {//if the user types String
				while(true) {//loop until a valid input for length is entered
					System.out.println("Enter maximum length of the String (1-255)");
					int length = input.nextInt();
					input.nextLine(); //consume newline
					if(length>=1 && length<=255) {
						dataType = "VARCHAR("+length+")";
						break; //exits loop if length is valid
					}
					System.out.println("Invalid length. Please enter a value between 1-255");
				}
				
					
			}
			//append used to connect strings in StringBuilder
			sql.append(columnName + " " +dataType);
			
			System.out.println("Can this column be NULL? (yes/no)");
			//String nullColumn = input.nextLine().toLowerCase();
			
			//append NOT NULL if the column cannot be null
			if(input.nextLine().trim().equalsIgnoreCase("no")) {
				sql.append(" NOT NULL");
			}
			
			System.out.println("Is this column a Primary Key? (yes/no)");
			//String isPrimaryKey = input.nextLine().toLowerCase();
			
			//append PRIMARY KEY if user chose yes
			if(input.nextLine().trim().equalsIgnoreCase("yes")) {
				if(!primaryKeyColumn.isEmpty()) {
					System.out.println("A primary Key has already been assigned. Only one primary key is allowed.");
				}
				else {
					//primaryKeyColumn +=", ";
					primaryKeyColumn = columnName;//Assign only once
				}
				
				//sql.append(" PRIMARY KEY");
				
			}
			
			
			//add a comma if its not the last column
			if(i<columnAmount - 1) {
				sql.append(", ");
			}
			
		}
		if(!primaryKeyColumn.isEmpty()) {
			sql.append(", PRIMARY KEY(").append(primaryKeyColumn).append(")");
		}	
		
		//end of table syntax
		sql.append(");");
		
		System.out.println("FINAL SQL: "+sql.toString());//DEBUGGING
		
		executeSql(con, sql.toString());
		
	}
	
	public void executeSql(Connection con, String sql) {
		Statement statement = null;
		
		try {
			statement = con.createStatement(); //creates a statment object to send to database
			statement.execute(sql); //execute sql command
			System.out.println("Executed SQL command successfully.");//confirmation message
		}
		catch(SQLException e) {
			System.out.println("SQL Error: "+e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(statement !=null) {
				//close the statement when completed
				try {
					statement.close();
				}
				catch(SQLException e) {
					System.out.println("Error closing the statement: "+e.getMessage());
				}
			}
		}
		
	}

}
