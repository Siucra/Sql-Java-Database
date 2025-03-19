import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.ResultSet;


public class Application {
	static Scanner input = new Scanner(System.in);

	public static void main(String[] args) {
		try {
			Connection con = DatabaseUtils.getConnection();//Create connection
			//ConnectionManager conManager = new ConnectionManager();
			
			
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
	
	public boolean closeConnection() {
		System.out.println("Would you like to close the connection? (YES/NO)");
		switch(input.nextLine().toLowerCase()) {
			case"yes":{
				System.out.println("Connection closed");
				return true;
			}
			case"no":{
				return false;
			}
			default:{
				System.out.println("Invalid choice. Closing connection by default...");
				return true;
			}
		}
	}
	public void mainMenu(Connection con) {
		boolean keepRunning = true;//control variable
		
		while(keepRunning) {//while keepRunning is true, loop mainMenu
			System.out.println("-".repeat(16));
			System.out.println("Press 1 to create new table");
			System.out.println("Press 2 to delete a table");
			System.out.println("Press 3 to add column to a table");
			System.out.println("Press 4 to delete a column from a table");
			System.out.println("Press 5 to edit column from a table");
			System.out.println("Press 6 to add table data");
			System.out.println("Press 7 to edit table data");
			System.out.println("Press 8 to delete table data");
			System.out.println("Press 9 to close connection");
			System.out.println("-".repeat(16));
			
			int choice = input.nextInt();
			input.nextLine();//consume leftover newline
			switch(choice) {
				case 1:{
					createNewTable(con);
					break;
				}
				case 2:{
					deleteTable(con);
					break;
				}
				case 3:{
					addTableColumn(con);
					break;
				}
				case 4:{
					deleteTableColumn(con);
					break;
				}
				case 5:{
					editTableColumn(con);
					break;
				}
				case 6:{
					insertTableData(con);
					break;
				}
				case 9:{
					if(closeConnection()) {
					//returns true
					keepRunning = false;
					}
					break;
				}
		
				default:{
					System.out.println("Invalid choice. Please enter a valid number.");
					break;
				}
			}
		}
	}
	
	public static String getColumnDataType(Connection con, String tableNameFind,String columnFromTable ) {
		String dataType = null;
		
		try {
			DatabaseMetaData metaData = con.getMetaData();
			ResultSet resultSet = metaData.getColumns(null,  null, tableNameFind, columnFromTable);
			if(resultSet.next()) {
				dataType = resultSet.getString("TYPE_NAME");
			}
		}
		catch(SQLException e) {
			System.out.println("Error retrieving column data type: "+e.getMessage());
		}
		return dataType;
	}


	public void insertTableData(Connection con) {
		// search for table, search for column, check if there isnt existing data, INSERT
		System.out.println("Enter table name: ");
		String tableNameFind = input.nextLine().trim();
		
		//checks if input is empty
		if(tableNameFind.isEmpty()) {
			System.out.println("Unable to find this table. Operating aborted.");
			return;
		}
		//checks if table is in the database
		if(!tableExists(con, tableNameFind)) {
			System.out.println("Error: Table '" + tableNameFind + "' does not exist. Please try again.");
			return;
		}
		
		System.out.println("Enter the column name you wish to add data to Table '"+tableNameFind+"'");
		
	
		
		String columnFromTable = input.nextLine().trim();
		
		String columnType = Application.getColumnDataType(con, tableNameFind, columnFromTable);
		System.out.println("The data type of this column is: "+columnType);
		
		if(columnFromTable.isEmpty()) {
			System.out.println("Unable to find this column in table '"+tableNameFind+"'");
			return;
		}
		
		//String columnType = getColumnDataType(con, tableNameFind, columnFromTable);
	}

	public void editTableColumn(Connection con) {
		//search for table, search for column, edit type/name 
		System.out.println("Enter table name: ");
		String tableNameFind = input.nextLine().trim();
		
		if(tableNameFind.isEmpty()) {
			System.out.println("Unable to find this table. Operation aborted");
			return;
		}
		
		if(!tableExists(con, tableNameFind)) {
			System.out.println("Error: Table '" + tableNameFind + "' does not exist. Please try again.");
			return;
		}
		System.out.println("Enter the column name you wish to edit from table '"+tableNameFind+"'");
		String columnFromTable = input.nextLine().trim();
		
		if(columnFromTable.isEmpty()) {
			System.out.println("Unable to find this column in table '"+tableNameFind+ "'");
			return;
		}
		
		System.out.println("Would you like to rename '"+columnFromTable+"' or edit datatype?");
		System.out.println("1 - RENAME");
		System.out.println("2 - EDIT DATATYPE");
		System.out.println("3 - EXIT");
		
		switch(input.nextInt()){
			case 1:{
				System.out.println("Enter a new name for: "+columnFromTable);
				String newColumnName = input.next();
				System.out.println("Are you sure you want to change column name '"+columnFromTable +"' to '"+newColumnName+"' ?");
				System.out.println("(YES/NO)");
				String userConfirmation = input.next();
				
				if(userConfirmation.trim().equalsIgnoreCase("yes")) {
					String sql = "ALTER TABLE "+tableNameFind +" RENAME COLUMN "+columnFromTable+" TO "+newColumnName;
					try(Statement statement = con.createStatement()) {
						statement.executeUpdate(sql);
						System.out.println("Column '"+columnFromTable+"' successfully renamed to: '"+newColumnName+"' in Table '"+tableNameFind+"'.");
						
					}
					catch(SQLException e) {
						System.out.println("Unable to edit column name:"+e.getMessage());
					}
				}
				else if(userConfirmation.trim().equalsIgnoreCase("no")) {
					System.out.println("Rename Operation for '"+columnFromTable+"' Aborted. Returning to main menu...");
					mainMenu(con);
				}
				else {
					System.out.println("Invalid input. Please enter YES or NO.");
				}
				break;
			}
			case 2:{
				System.out.println("Enter the new datatype for the column '"+columnFromTable+ "' (INT/STRING):");
				String columnType = Application.getColumnDataType(con, tableNameFind, columnFromTable);
				System.out.println("The data type of this column is: "+columnType);
				
				String newDataType = input.next().trim();
				
				if(newDataType.equalsIgnoreCase("String")) {//if the user types String
					while(true) {//loop until a valid input for length is entered
						System.out.println("Enter maximum length of the String (1-255)");
						int length = input.nextInt();
						input.nextLine(); //consume newline
						if(length>=1 && length<=255) {
							newDataType = "VARCHAR("+length+")";
							break; //exits loop if length is valid
						}
						System.out.println("Invalid length. Please enter a value between 1-255");
					}	
				}
				System.out.println("Are you sure you want to change the datatype of '"+ columnFromTable+"' to '"+newDataType+"' ?");
				System.out.println("(YES/NO)");
				String userConfirmation = input.next().trim();
				if(userConfirmation.equalsIgnoreCase("yes")) {
			        String sqlChangeDataType = "ALTER TABLE " + tableNameFind + " MODIFY COLUMN " + columnFromTable + " " + newDataType;
					try(Statement statement = con.createStatement()){
						statement.executeUpdate(sqlChangeDataType);
						System.out.println("Datatype successfully changed to "+newDataType+".");//!!
					}
					catch(SQLException e) {
						System.out.println("Unable to edit column data type: "+e.getMessage());
					}
					
				}
				break;
			}
			case 3:{
				System.out.println("Returning to main menu....");
				mainMenu(con);
				break;
			}
		}
	}

	public void deleteTable(Connection con) {
		System.out.println("Enter the table name you wish to drop: ");
		String tableName = input.nextLine().trim();
		
		if(tableName.isEmpty()) {
			System.out.println("Unable to find this table. Operation aborted.");
			return;
		}
		
		//check if table exists before dropping
		String checkTableSql = "SHOW TABLES LIKE '" + tableName + "'";
		
		 try (Statement checkStatement = con.createStatement();
		         ResultSet resultSet = checkStatement.executeQuery(checkTableSql)) {
			 	
			 	   //moves the cursor to the first row of the result set
		        if (!resultSet.next()) { // If no rows are returned, table doesnt exist
		            System.out.println("Error: Table '" + tableName + "' does not exist. Operation aborted.");
		            deleteTable(con);
		            return;
		        }
		 }
		 catch(SQLException e) {
			 System.out.println("SQL ERROR while checking for table's existence: "+e.getMessage());
			 return;
		 }
		String sql = "DROP TABLE IF EXISTS `" +tableName + "`";
		System.out.println("Are you sure you want to drop table: "+tableName + "?");
		System.out.println("YES/NO");
		String userConfirmation = input.next();
		
		if(userConfirmation.trim().equalsIgnoreCase("yes")) {
			
			try(Statement statement = con.createStatement()){
				System.out.println("Executing: " + sql);//debugging, remove later
				statement.executeUpdate(sql);//Data Definition Language command to drop table
				System.out.println("Table '"+tableName + "' dropped successfully.");
			}
			catch(SQLException e) {
				System.out.println("SQL Error: "+e.getMessage());
				e.printStackTrace();
			}
		}
		else if(userConfirmation.trim().equalsIgnoreCase("no")) {
			System.out.println("Drop Operation Aborted. Returning to main menu....");
			mainMenu(con);//call main menu and keep the connection
		}
		else {
			System.out.println("Invalid input. Please enter YES or NO.");
		}
		
	}
	

	public void deleteTableColumn(Connection con) {
		System.out.println("Enter the table name: ");
		String tableNameFind = input.nextLine().trim();
		
		if(tableNameFind.isEmpty()) {
			System.out.println("Unable to find this table. Operation aborted.");
			return;
		}
		
		if(!tableExists(con, tableNameFind)) {
			System.out.println("Error: Table '" + tableNameFind + "' does not exist. Please try again.");
			return;
		}
		
		System.out.println("Enter the column name you wish to drop from table '"+tableNameFind+"'");
		String columnFromTable = input.nextLine().trim();
			
		if(columnFromTable.isEmpty()) {
			System.out.println("Unable to find this column in table '"+tableNameFind+ "'");
			return;
		}
		
		if(ColumnHasPrimaryKey(con, tableNameFind, columnFromTable)) {
			System.out.println("Cannot drop '"+columnFromTable+"' because it is a Primary Key");
			return;
		}
		
		String sql = "ALTER TABLE "+ tableNameFind + " DROP COLUMN "+columnFromTable;
		System.out.println("Are you sure you want to drop column '"+columnFromTable+"' from Table '"+tableNameFind+"' ?");
		System.out.println("YES/NO");
		String userConfirmation = input.next();
		
		if(userConfirmation.trim().equalsIgnoreCase("yes")) {
		
			try(Statement statement = con.createStatement()){
				statement.executeUpdate(sql);
				System.out.println("Column '"+columnFromTable+"' has been dropped successfully from '"+tableNameFind+"'");
			}
			catch(SQLException e) {
				System.out.println("Unable to drop column: "+e.getMessage());
			}
		}
		else if(userConfirmation.trim().equalsIgnoreCase("no")){
		System.out.println("Drop Operation for '"+columnFromTable+"' Aborted. Returning to main menu...");
		mainMenu(con);
		}
		
		else {
			System.out.println("Invalid input. Please enter YES or NO.");
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
					continue;//exits this loop
				}
				else {
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
	
	private boolean tableExists(Connection con, String tableName) {
		
		try(ResultSet rs = con.getMetaData().getTables(null, null, tableName, null)){
			return rs.next();
		}
		catch(SQLException e) {
			System.out.println("SQL Error while checking for table's existence" + e.getMessage());
			return false;
		}
		
	}
	
	private boolean primaryKeyExists(Connection con, String tableName) {
		try(ResultSet rs = con.getMetaData().getPrimaryKeys(null, null, tableName)) {
			return rs.next(); //primary key exists
		}
		catch(SQLException e) {
			System.out.println("Error on locating Primary key" + e.getMessage());
			return false;
		}
		
	}
	
	private boolean ColumnHasPrimaryKey(Connection con, String tableName, String columnName) {
		try(ResultSet rs = con.getMetaData().getPrimaryKeys(null, null, tableName)){
			while(rs.next()) {
				if(rs.getString("COLUMN_NAME").equals(columnName)) {
					return true;
				}
			}
		}
		catch(SQLException e) {
			System.out.println("SQL Error: "+e.getMessage());
		}
		return false;
	}
	
public void addTableColumn(Connection con) {
    System.out.println("Enter the table name you wish to add a column to: ");
    String tableNameFind = input.nextLine().trim();

    if (tableNameFind.isEmpty()) {
        System.out.println("Unable to find this table. Operation aborted.");
        return;
    }

        if (!tableExists(con, tableNameFind)) {
            System.out.println("Error: Table '" + tableNameFind + "' does not exist. Please try again.");
            return;//!!
        }

        System.out.println("How many columns would you like to add to table " + tableNameFind + " ?");
        if (!input.hasNextInt()) {
            System.out.println("Invalid number of columns. Please enter an integer.");
            input.nextLine(); // Clear invalid input
            return;
        }
    

        int columnAmount = input.nextInt();
        input.nextLine(); // Consume the newline character

        StringBuilder sql = new StringBuilder("ALTER TABLE `" + tableNameFind + "` ");
        boolean firstColumn = true;
        boolean primaryKeyAdded = primaryKeyExists(con, tableNameFind);

        for (int i = 0; i < columnAmount; i++) {
            if (!firstColumn) {
                sql.append(", ");
            }
            
          System.out.println("Enter column name: ");
          String columnName = "`" + input.nextLine().trim() + "`";

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

            sql.append("ADD " + columnName + " " + dataType);

            System.out.println("Can this column be NULL? (YES/NO)");
            if (input.nextLine().trim().equalsIgnoreCase("no")) {
                sql.append(" NOT NULL");
            
            
            System.out.println("Is this column a primary key? (YES/NO) ");
            if(input.nextLine().trim().equalsIgnoreCase("yes")) {
            	if(primaryKeyAdded) {
            		System.out.println("A primary Key already exists for this table. Cannot add another primary key.");
            		continue; //skips column
            	}
            	else {
            		sql.append(" PRIMARY KEY");
            		primaryKeyAdded = true;//update boolean
            	}
            }
            firstColumn = false;
        }

        System.out.println("FINAL SQL: " + sql.toString()); // For debugging
        executeSql(con, sql.toString());
        break;
        }
     
}

	
	
	public void executeSql(Connection con, String sql) {
		Statement statement = null;
		
		try {
			statement = con.createStatement(); //creates a statement object to send to database
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
