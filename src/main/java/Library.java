import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Scanner;
import java.sql.Timestamp;

//direct imply in issuebook() method
//return book that returned point 
//View Book Usagers add count users with this method

class Library {

	public static void addBook(Connection connection, Book book, String category, int available, double price)
			throws Exception {
		String checkQuery = "SELECT id, available FROM books WHERE title = ? AND author = ?";
		PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
		checkStatement.setString(1, book.getTitle());
		checkStatement.setString(2, book.getAuthor());
		ResultSet resultSet = checkStatement.executeQuery();

		if (resultSet.next()) {
			int id = resultSet.getInt("id");
			int existingAvailable = resultSet.getInt("available");

			int updatedAvailable = existingAvailable + available;

			String updateQuery = "UPDATE books SET available = ? WHERE id = ?";
			PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
			updateStatement.setInt(1, updatedAvailable);
			updateStatement.setInt(2, id);
			updateStatement.executeUpdate();
			updateStatement.close();
		} else {
			String insertQuery = "INSERT INTO books (id, title, author, category, available, price) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
			insertStatement.setInt(1, book.getId());
			insertStatement.setString(2, book.getTitle());
			insertStatement.setString(3, book.getAuthor());
			insertStatement.setString(4, category);
			insertStatement.setInt(5, available);
			insertStatement.setDouble(6, price);
			insertStatement.executeUpdate();
			insertStatement.close();
		}

		resultSet.close();
		checkStatement.close();
	}

	public static void displayBooksByCategory(Connection connection, int categoryChoice) throws Exception {
		String category = getCategoryFromChoice(categoryChoice);

		String query = "SELECT * FROM books WHERE category = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, category);
		ResultSet resultSet = statement.executeQuery();

		System.out.println("Books in the category '" + category + "':");
		System.out.println("---------------------------------------");
		System.out.printf("%-5s | %-20s | %-25s\n", "ID", "Title", "Author");
		System.out.println("---------------------------------------");
		while (resultSet.next()) {
			int id = resultSet.getInt("id");
			String title = resultSet.getString("title");
			String author = resultSet.getString("author");
			System.out.printf("%-5s | %-20s | %-25s\n", id, title, author);
		}
		System.out.println("---------------------------------------");

		resultSet.close();
		statement.close();
	}

	private static String getCategoryFromChoice(int choice) {
		switch (choice) {
		case 1:
			return "Fiction";
		case 2:
			return "Non-Fiction";
		default:
			return "Invalid Option";

		}
	}

	public static void issueBook(Connection connection, int bookId, String username) throws Exception {

		String checkAvailabilityQuery = "SELECT available, DATE_ADD(NOW(), INTERVAL 14 DAY) AS due_date FROM books WHERE id = ?";
		String insertIssueQuery = "INSERT INTO issued_books (book_id, username, issue_date, return_date, due_date) VALUES (?, ?, NOW(), NULL, ?)";
		String updateAvailabilityQuery = "UPDATE books SET available = available - 1 WHERE id = ?";

		try (PreparedStatement checkAvailabilityStatement = connection.prepareStatement(checkAvailabilityQuery);
				PreparedStatement insertIssueStatement = connection.prepareStatement(insertIssueQuery);
				PreparedStatement updateAvailabilityStatement = connection.prepareStatement(updateAvailabilityQuery)) {

			checkAvailabilityStatement.setInt(1, bookId);

			ResultSet availabilityResult = checkAvailabilityStatement.executeQuery();

			if (availabilityResult.next()) {

				int availableCopies = availabilityResult.getInt("available");

				if (availableCopies > 0) {

					LocalDateTime dueDate = availabilityResult.getTimestamp("due_date").toLocalDateTime();

					insertIssueStatement.setInt(1, bookId);
					insertIssueStatement.setString(2, username);
					insertIssueStatement.setTimestamp(3, Timestamp.valueOf(dueDate));
					insertIssueStatement.executeUpdate();

					updateAvailabilityStatement.setInt(1, bookId);
					updateAvailabilityStatement.executeUpdate();

					System.out.println("Book issued successfully!");
					System.out.println("Due date: " + dueDate);
				} else {
					System.out.println("No available copies of the book.");
				}
			} else {
				System.out.println("Invalid book ID.");
			}
		}
	}

	public static void returnBook(Connection connection, int bookId, String username) throws Exception {
		String updateQuery = "UPDATE issued_books SET return_date = ? WHERE book_id = ? AND username = ?";
		LocalDateTime currentTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = currentTime.format(formatter);

		PreparedStatement statement = connection.prepareStatement(updateQuery);
		statement.setString(1, formattedDateTime);
		statement.setInt(2, bookId);
		statement.setString(3, username);
		statement.executeUpdate();
		statement.close();
		System.out.println("Book returned successfully!");

	}

	public static void displayBooks(Connection connection) throws Exception {
		String query = "SELECT * FROM books";
		PreparedStatement statement = connection.prepareStatement(query);
		ResultSet resultSet = statement.executeQuery();

		System.out.println("Books:");
		System.out.println("");
		System.out.printf("%-5s | %-20s | %-20s | %-10s | %7s\n", "ID", "Title", "Author", "Category", "Price");
		System.out.println("----------------------------------------------------------------------------");
		while (resultSet.next()) {
			int id = resultSet.getInt("id");
			String title = resultSet.getString("title");
			String author = resultSet.getString("author");
			String category = resultSet.getString("category");
			double price = resultSet.getDouble("price");
			System.out.printf("%-5s | %-20s | %-20s | %-10s | %9s\n", id, title, author, category, price);
		}
		System.out.println("");

		resultSet.close();
		statement.close();
	}

	public static void removeBook(Connection connection, int bookId) throws Exception {
		String query = "DELETE FROM books WHERE id = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, bookId);
		statement.executeUpdate();
		statement.close();
		System.out.println("Book removed successfully!");
	}

	public static void manageAccount(Connection connection) throws Exception {
		Scanner scanner = new Scanner(System.in);
		boolean accountManagement = true;

		while (accountManagement) {
			System.out.println("Account Management\nSelect an option:");
			System.out.println("1. Change password");
			System.out.println("2. Delete account");
			System.out.println("3. Show all users");
			System.out.println("4. Go back");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.println("Enter your username:");
				String username = scanner.nextLine();
				System.out.println("Enter your current password:");
				String currentPassword = scanner.nextLine();
				System.out.println("Enter your new password:");
				String newPassword = scanner.nextLine();
				try {
					if (Management.login(connection, username, currentPassword)) {
						changePassword(connection, username, newPassword);
						System.out.println("Password changed successfully.");
					} else {
						System.out.println("Invalid username or password.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				System.out.println("Enter your username:");
				String deleteUsername = scanner.nextLine();
				System.out.println("Enter your password:");
				String deletePassword = scanner.nextLine();
				try {
					if (Management.login(connection, deleteUsername, deletePassword)) {
						deleteUser(connection, deleteUsername);
						Management.currentUser = deleteUsername;
						System.out.println("Account deleted successfully.");
//						accountManagement = false;
					} else {
						System.out.println("Invalid username or password.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 4:
				accountManagement = false;
				break;
			case 3:
				displayAllUsers(connection);
				break;
			default:
				System.out.println("Invalid option. Please try again.");
			}
			scanner.close();
		}
	}

	public static void changePassword(Connection connection, String username, String newPassword) throws Exception {

		String updateQuery = "UPDATE users SET password = ? WHERE username = ?";

		PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
		updateStatement.setString(1, newPassword);
		updateStatement.setString(2, username);
		updateStatement.executeUpdate();
		updateStatement.close();
	}

	public static void deleteUser(Connection connection, String username) throws Exception {

		String deleteQuery = "DELETE FROM users WHERE username = ?";

		PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
		deleteStatement.setString(1, username);
		deleteStatement.executeUpdate();
		deleteStatement.close();
	}

	public static void displayAllUsers(Connection connection) {

		String query = "SELECT * FROM users";

		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery(query);
			System.out.println("All users:");
			while (rs.next()) {
				String username = rs.getString("username");
				System.out.println(username);
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	public static void getTotalBooks(Connection connection) throws Exception {
		String query = "SELECT SUM(available) AS total_books FROM books";

		PreparedStatement statement = connection.prepareStatement(query);
		ResultSet resultSet = statement.executeQuery();

		System.out.println("Total Books from Library:");
		while (resultSet.next()) {
			double totalBooksDecimal = resultSet.getDouble("total_books");
			int totalBooks = (int) totalBooksDecimal;
			System.out.println(totalBooks);
		}

		resultSet.close();
		statement.close();
	}

//	public static void checkAvailability(Connection connection) throws SQLException {
//		String query = "SELECT available, title FROM books";
//		PreparedStatement statement = connection.prepareStatement(query);
//		ResultSet resultSet = statement.executeQuery();
//
//		System.out.println("Check Availability");
//		System.out.printf("%-23s | %-10s%n", "Title of the Book", "Availability");
//		System.out.println("---------------------------------------");
//
//		while (resultSet.next()) {
//			String title = resultSet.getString("title");
//			String availability = resultSet.getString("available");
//			System.out.printf("%-23s | %-10s%n", title, availability);
//		}
//
//		resultSet.close();
//	}
	public static void checkAvailabilityFromParticularBook(Connection connection, int bookId) throws SQLException{

		String query = "SELECT title,available FROM books WHERE id = ?";

			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, bookId);
			ResultSet resultSet = statement.executeQuery();

			System.out.println("Check Availability");
			System.out.printf("%-23s | %-10s%n", "Title of the Book", "Availability");
			System.out.println("---------------------------------------");

			while (resultSet.next()) {
				String title = resultSet.getString("title");
				String availability = resultSet.getString("available");
				System.out.printf("%-23s | %-10s%n", title, availability);
			}

			resultSet.close();

	}

	public static void viewBookUsuagers(Connection connection, int bookId1) throws SQLException {
		String query = "SELECT u.username, ib.issue_date FROM users u "
				+ "INNER JOIN issued_books ib ON u.username = ib.username " + "WHERE ib.book_id = ?";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, bookId1);statement.setInt(1, bookId1);
		ResultSet resultSet = statement.executeQuery();

		System.out.println("Usagers of Book ID " + bookId1 + ":");
		System.out.printf("%-20s | %-12s\n", "Username", "Issue Date");
		System.out.println("----------------------------------------");

		while (resultSet.next()) {
			String username = resultSet.getString("u.username");
			Date issueDate = resultSet.getDate("ib.issue_date");

			System.out.printf("%-20s | %-12s\n", username, issueDate);
		}

		resultSet.close();
		statement.close();
	}

	public static void getNumOfUsersPerBook(Connection connection) throws SQLException {

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter ID to see the users' data from the list:");
		int bookId = sc.nextInt();
		sc.nextLine();

		System.out.println("Choose an option:");
		System.out.println("1. View count of users");
		System.out.println("2. View usernames");

		int option = sc.nextInt();

		if (option == 1) {
			int numOfUsers = getCountOfUsers(connection, bookId);
			System.out.println("Users count is " + numOfUsers);
		} else if (option == 2) {
			String usernames = getUsernames(connection, bookId);
			System.out.println("Usernames: " + usernames);
		} else {
			System.out.println("Invalid option.");
		}
		sc.close();
	}

	public static int getCountOfUsers(Connection connection, int bookId) throws SQLException {

		String query = "SELECT COUNT(DISTINCT i.username) AS num_users " + "FROM issued_books i "
				+ "JOIN books b ON i.book_id = b.id " + "WHERE b.id = ?";

		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, bookId);
			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				return rs.getInt("num_users");
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return 0;
	}


	public static String getUsernames(Connection connection, int bookId) throws SQLException {

		String query = "SELECT GROUP_CONCAT(DISTINCT i.username SEPARATOR ' and ') AS usernames "
				+ "FROM issued_books i " + "JOIN books b ON i.book_id = b.id " + "WHERE b.id = ?";
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, bookId);
			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				return rs.getString("usernames");
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		return "";
	}

	// SUBSCRIPTION OPTION
	public static void payFine(Connection connection, String username) throws SQLException {
		String query = "SELECT * FROM issued_books WHERE username = ? AND returned = 0 AND due_date < CURDATE()";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, username);
		ResultSet resultSet = statement.executeQuery();

		if (resultSet.next()) {
			String updateQuery = "UPDATE issued_books SET returned = 1, paid = 1  WHERE username = ? AND returned = 0 AND due_date < CURDATE()";
			PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
			updateStatement.setString(1, username);
			updateStatement.executeUpdate();

			System.out.println("Fine paid successfully.");
		} else {
			System.out.println("No fine to pay.");
		}

		resultSet.close();
		statement.close();
	}

	public static void paySubscription(Connection connection, String username) throws SQLException {
		String query = "UPDATE users SET subscription_status = ?, subscription_paid = 1 WHERE username = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, "Premium Subscriber");
		statement.setString(2, username);
		int rowsAffected = statement.executeUpdate();
		if (rowsAffected > 0) {
			System.out.println("Subscription paid successfully. Subscription status updated to 'Premium Subscriber'.");
		} else {
			System.out.println("No subscription amount to pay.");
		}
		statement.close();
	}

	public static void subscribe(Connection connection, String username) throws SQLException {

		String query = "UPDATE users SET subscription_status ='Subscribed', subscription_paid=0 WHERE username= ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, username);
		int rowsAffected = statement.executeUpdate();
		if (rowsAffected > 0) {
			System.out.println("Subscription paid successfully. Subscription status updated to 'Subscribed'");
		} else {
			System.out.println("Non subscription amount to pay.");
		}
		statement.close();
	}

	public static void unsubscribe(Connection connection, String username) throws SQLException {
		String query = "UPDATE users SET subscription_status = 'Unsubscribed', subscription_paid = 0 WHERE username = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, username);
		int rowsAffected = statement.executeUpdate();
		if (rowsAffected > 0) {
			System.out.println("Subscription canceled successfully. Subscription status updated to 'Unsubscribed'.");
		} else {
			System.out.println("User not found.");
		}
		statement.close();
	}

	// SHOWS THE BOOK USERS

	public static void displayBookUsers(Connection connection) throws SQLException {
		int currentPage = 1;
		int USERS_PER_PAGE = 10;
		Scanner scanner = new Scanner(System.in);

		boolean running = true;
		while (running) {

			String query = "SELECT due_date, book_id FROM issued_books WHERE paid = 0 LIMIT "
					+ (currentPage - 1) * USERS_PER_PAGE + ", " + USERS_PER_PAGE;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			System.out.println("Books currently issued:");
			System.out.println("Book ID\t\t| Due Date\t| Status");
			System.out.println("----------------------------------------");

			int userCount = 0;
			while (resultSet.next()) {
				LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
				String bookName = resultSet.getString("book_id");

				long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
				System.out.println(bookName + "\t\t| " + dueDate + "\t| " + daysLeft + " days left");

				userCount++;
			}
			if (userCount < USERS_PER_PAGE) {
				System.out.println("No more users to display.");
				break;
			}

			System.out.println("\nOptions:");
			System.out.println("1. Show next " + USERS_PER_PAGE + " users");
			System.out.println("2. Show previous " + USERS_PER_PAGE + " users");
			System.out.println("3. Exit");
			System.out.print("Enter your choice: ");
			int choice = scanner.nextInt();

			switch (choice) {
			case 1:
				currentPage++;
				break;
			case 2:
				currentPage--;
				if (currentPage < 1) {
					currentPage = 1;
				}
				break;
			case 3:
				running = false;
				break;
			default:
				System.out.println("Invalid Choice. Please Try Again..");
			}
			resultSet.close();
			statement.close();
		}
		scanner.close();
	}

}