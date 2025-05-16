import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
				case 1 -> createNewTable(con);
				case 2 -> deleteTable(con);
				case 3 -> addTableColumn(con);
				case 4 -> deleteTableColumn(con);
				case 5 -> editTableColumn(con);
				case 6 -> insertTableData(con);
				case 7 -> editTableData(con);
				case 8 -> deleteTableData(con);
				case 9 -> {
					if(closeConnection()) {
						try {
							con.close();
							System.out.println("Database connection closed.");
							
						}
						catch(SQLException e){
							System.out.println("Error closing connection: "+e.getMessage());
						}
						keepRunning = false;
					}
				}
		
				default -> System.out.println("Invalid choice. Please enter a valid number.");
			}
			
		}
	}
	
	
	private void deleteTableData(Connection con) {
		System.out.println("Enter the table name: ");
		String tableName = input.nextLine().trim();
		
		if(tableName.isEmpty()) {
			System.out.println("Unable to find this table. Operation aborted");
			return;
		}
		if (!tableExists(con, tableName)) {
			System.out.println("Error: Table '" + tableName +"' does not exist. Please try again.");
			return;
		}
		
		System.out.println("Enter the column name: ");
		String columnName = input.nextLine().trim();
		
		if(columnName.isEmpty()) {
			System.out.println("Unable to find this table. Operation aborted.");
			return;
		}
		
		System.out.println("Which column should we use to find the row? (condition column) ");
		System.out.println("e.g id, name");
		String conditionColumn = input.nextLine().trim();
		
		if(conditionColumn.isEmpty()) {
			System.out.println("Condition column cannot be empty.");
			return;
		}
		
		if(!columnExists(con, tableName, conditionColumn)) {
			System.out.println("Column '" + conditionColumn + "' does not exist in table '" + tableName +"'.");
		}
		
		System.out.println("Enter the existing value of that column to find the row (condition value)");
		System.out.println("e.g '3' for id, 'bob' for name");
		String conditionValue = input.nextLine().trim();
		
		if(conditionValue.isEmpty()) {
			System.out.println("Condition value cannot be empty");
			return;
		}
		
		String sql = "DELETE FROM "+ tableName + " WHERE " + conditionColumn + "=?";
	
		try(PreparedStatement statement = con.prepareStatement(sql)){
			statement.setString(1, conditionValue);
			int rowsDeleted = statement.executeUpdate();
		
			if(rowsDeleted > 0) {
				System.out.println("Row deleted successfully.");
			}
			else {
				System.out.println("No matching rows found. Nothing deleted.");
			}
		}
		
		
		catch(SQLException e) {
			System.out.println("SQL Error: "+ e.getMessage());
		}
	
	
	}

	public void editTableData(Connection con) {
		System.out.println("Enter the table name: ");
		String tableName = input.nextLine().trim();
		
		if(tableName.isEmpty()) {
			System.out.println("Unable to find this table. Operation aborted.");
			return;	
		}
		
		if(!tableExists(con, tableName)) {
			System.out.println("Error: Table '" + tableName + "' does not exist. Please try again.");
			return;
		}
		
		System.out.println("Enter the column name: ");
		String columnName = input.nextLine().trim();
		
		if(columnName.isEmpty()) {
			System.out.println("Unable to find this column in table '"+tableName+"'");
			return;
		}
		
		System.out.println("Enter the new value: ");
		String newValue = input.nextLine().trim();
	
		
		System.out.println("Which column should we use to find the row? (condition column) ");
		System.out.println("e.g id, name");
		String conditionColumn = input.nextLine().trim();
		
		if(conditionColumn.isEmpty()) {
			System.out.println("Condition column cannot be empty.");
			return;
		}
		
		if(!columnExists(con, tableName, conditionColumn)) {
			System.out.println("Column '" + conditionColumn + "' does not exist in table '" + tableName +"'.");
		}
		
		System.out.println("Enter the existing value of that column to find the row (condition value)");
		System.out.println("e.g '3' for id, 'bob' for name");
		String conditionValue = input.nextLine().trim();
		
		//? used as placeholder in SQL queries for dynamic data insertion and prevents sql injections 
		String sql = "UPDATE " + tableName + " SET " + columnName + "  = ? WHERE " + conditionColumn + " =?";
		
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
	        stmt.setString(1, newValue); //puts newValue into the first ? in the SQL query
	        stmt.setString(2, conditionValue); //puts conditionValue into the second ? to match the correct row
	        int rowsAffected = stmt.executeUpdate();

	      //checks if a row was affected (error catching)
	        if (rowsAffected > 0) {//update was successful
	            System.out.println("Field updated successfully.");
	        } 
	        else {
	            System.out.println("No records updated. Check your condition.");
	        }
	    } 
		catch (SQLException e) {
	        System.out.println("SQL Error: " + e.getMessage());
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
			else {
				System.out.println("No data found for column '"+columnFromTable +"' in table '"+tableNameFind +"'.");
			}
		}
		catch(SQLException e) {
			System.out.println("Error retrieving column data type: "+e.getMessage());
		}
		return dataType;
	}

	public boolean columnExists(Connection con, String tableName, String columnName) {
		try {
			DatabaseMetaData meta = con.getMetaData();
			ResultSet rs = meta.getColumns(null, null, tableName, columnName);
			return rs.next();
		}
		catch(SQLException e) {
			System.out.println("Error checking column: "+e.getMessage());
			return false;
		}
		

	}
	
	public int getColumnMaxLength(Connection con, String tableNameFind, String columnFromTable) {
		int length = 0;
		
		try {
			DatabaseMetaData metaData = con.getMetaData();
			ResultSet resultSet = metaData.getColumns(null, null, tableNameFind, columnFromTable);
			if(resultSet.next()) {
				length = resultSet.getInt("COLUMN_SIZE");
			}
			else {
				System.out.println("No data found for column '" + columnFromTable + "' in table '" + tableNameFind+"'.");
			}
		}
		catch(SQLException e) {
			System.out.println("Error retrieving column max length: "+e.getMessage());
		}
		
		return length;
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
		
		if(columnFromTable.isEmpty()) {
			System.out.println("Unable to find this column in table '"+tableNameFind+"'");
			return;
		}
		
		String columnType = Application.getColumnDataType(con, tableNameFind, columnFromTable);
	
		System.out.println("The data type of this column is: "+columnType);
		int maxLength = getColumnMaxLength(con, tableNameFind, columnFromTable);
		
		System.out.println("Enter info for field (max length "+maxLength+ "): ");
		String fieldInfo = input.nextLine();
		
		if(fieldInfo.length()>maxLength) {
			System.out.println("The data exceeds the maximum length of "+ maxLength + " characters. Please enter valid data.");
			return;
		}

		String sql = "INSERT INTO " + tableNameFind + " (" + columnFromTable + ") VALUES (?)";
		try(PreparedStatement pStatement = con.prepareStatement(sql)){
			pStatement.setString(1, fieldInfo);
			//rowsAffected used for execution feedback
			int rowsAffected = pStatement.executeUpdate();
			if(rowsAffected > 0) {
				System.out.println("Data successfully added to "+ tableNameFind);
				}
			else {
				System.out.println("No data added.");
			}
		}
		catch(SQLException e) {
			System.out.println("SQL Error: "+e.getMessage());
		}

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

		String sql = "CREATE TABLE "+ tableName + "(id INT AUTO_INCREMENT PRIMARY KEY)";
		System.out.println("FINAL SQL: "+sql.toString());//DEBUGGING
		
		executeSql(con, sql);
		System.out.println("Table "+ tableName + " created successfully. Please add columns into it.");
		
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
        
        System.out.println("Enter the column name you wish to add: ");
        String columnName = "`" + input.nextLine().trim() + "`";
        
        System.out.println("Enter datatype for new column (VARCHAR/INT");
        String dataType = input.nextLine().trim();
        
        if(dataType.equalsIgnoreCase("VARCHAR")) {//if the user types String
			while(true) {//loop until a valid input for length is entered
				System.out.println("Enter maximum length for the VARCHAR (1-255)");
				int length = input.nextInt();
				input.nextLine(); //consume newline
				if(length>=1 && length<=255) {
					dataType = "VARCHAR(" + length + ")";
					break; //exits loop if length is valid
				}
				System.out.println("Invalid length. Please enter a value between 1-255");
			}		
        }
 
        	System.out.println("Can this column be NULL ? (YES/NO)");
        	boolean isNullable = input.nextLine().trim().equalsIgnoreCase("yes");
        	String nullSetting = isNullable ? "": " NOT NULL";
        
        StringBuilder sql = new StringBuilder("ALTER TABLE "+ tableNameFind +" ADD COLUMN " + columnName + " "+ dataType + nullSetting);
        
        System.out.println("Should this column be UNIQUE? (YES/NO)");
        boolean isUnique = input.nextLine().trim().equalsIgnoreCase("yes");
        
        if(isUnique) {
        	sql.append(", ADD UNIQUE (" + columnName + ")");
        }

        System.out.println("FINAL SQL: " + sql.toString()); // For debugging
        executeSql(con, sql.toString());
        System.out.println("Column added successfully.");
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
