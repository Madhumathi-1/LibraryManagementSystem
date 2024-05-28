import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Management {
	private static final String DB_URL = "jdbc:mysql://localhost/library";
	private static final String DB_USER = "madhumathi";
	private static final String DB_PASSWORD = "mad@1";
	private static final HashMap<String, Boolean> loggedInUsers = new HashMap<>();
	private static final HashSet<String> activeSessions = new HashSet<>();

	public static String currentUser = null;
    
	public static void main(String[] args) {

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Scanner scanner = new Scanner(System.in);
			boolean running = true;
			
			while (running) {
				System.out.println("Library Management System\n------------------------------\nSelect an option:\n----------------------------");
				System.out.println("1. Create a new user");
				System.out.println("2. Login");
				System.out.println("3. Logout");
				System.out.println("4. Display current user");
				System.out.println("5. Add a book");
				System.out.println("6. Category");
				System.out.println("7. Issue book");
				System.out.println("8. Return book");
				System.out.println("9. Display all books");
				System.out.println("10. Remove a book");
				System.out.println("11. Top 5 Users");
				System.out.println("12. Account Management");
				System.out.println("13. Total Books From Library");
				System.out.println("14. Check the Availabilities");
				System.out.println("15. Num of Users In particular book");
				System.out.println("16. Pay Fine or Subscription");
				System.out.println("17. View Usagers With Book ID");
				System.out.println("18. Currently Book issued Usagers");
				System.out.println("19. Exit");

				int choice = scanner.nextInt();
				scanner.nextLine();

				switch (choice) {
				case 1:
					System.out.println("Enter a username:");
					String username = scanner.nextLine();
					
					System.out.println("Enter a password:");
					String password = scanner.nextLine();
//                     password = readPasswordFromConsole();
//                     String hashedPassword = hashPassword(password);  
					createUser(connection, username, password);
					break;
				case 2:
					System.out.println("-------------------------");
					System.out.println("Enter your username:");
					String loginUsername = scanner.nextLine();
					System.out.println("-------------------------");
					System.out.println("Enter your password:");
					String loginPassword = scanner.nextLine();
					boolean isNewUser = !isLoggedIn(connection, loginUsername);

					if (login(connection, loginUsername, loginPassword)) {
						loggedInUsers.put(loginUsername, true);
						activeSessions.contains(loginUsername);
						if (isNewUser) {
							System.out.println("\u001B[32mYou are a new user. Welcome!\u001B[0m");
						} else {
							System.out.println("\u001B[32mLogin successful. Welcome, " + loginUsername + "!\u001B[0m");
							displayDueDate(connection, loginUsername);
						}
					} else {
						System.out.println("\u001B[31mInvalid username or password.\u001B[0m");
					}
					break;

				case 3:
					System.out.println("Enter your username:");
					String logoutUsername = scanner.nextLine();
					if (logout(connection, logoutUsername)) {
						currentUser = logoutUsername;
						System.out.println("User logged out successfully.");
					} else if(logout(connection, logoutUsername)){
						currentUser = logoutUsername;
						System.out.println("There is no user from "+logoutUsername);
					}
					break;

				case 4:
					displayCurrentUser();
//					displayIssuedBooks(connection);
					break;

				case 5:
					System.out.println("Enter your username:");
					String addBookUsername = scanner.nextLine();
					if (isLoggedIn(connection,addBookUsername)) {
						System.out.print("Enter book ID: ");
						int id = scanner.nextInt();
						scanner.nextLine();
						System.out.print("Enter book title: ");
						String title = scanner.nextLine();
						System.out.print("Enter book author: ");
						String author = scanner.nextLine();
						System.out.print("Enter book category: ");
						String category = scanner.nextLine();
						System.out.print("Enter number of available copies: ");
						int availableCopies = scanner.nextInt();
						System.out.print("Enter price :");
						double price = scanner.nextDouble();
						scanner.nextLine();
						Book book = new Book(id, title, author, price, availableCopies);
						Library.addBook(connection, book, category, availableCopies, price);
						System.out.println("Book added successfully!");
					} else {
						System.out.println("Please login first.");
					}
					break;

				case 6:
					System.out.println("Enter your username:");
					String viewCategoryUsername = scanner.nextLine();
					if (isLoggedIn(connection,viewCategoryUsername)) {
						System.out.println("Select a category:");
						System.out.println("1. Fiction");
						System.out.println("2. Non-Fiction");
						int categoryChoice = scanner.nextInt();
						scanner.nextLine();
						Library.displayBooksByCategory(connection, categoryChoice);
					} else {
						System.out.println("Please login first.");
					}
					break;

				case 7:
					System.out.println("Enter your username:");
					String issueBookUsername = scanner.nextLine();
					if (isLoggedIn(connection,issueBookUsername)) {
						System.out.print("Enter the book ID to issue: ");
						int bookId = scanner.nextInt();
						scanner.nextLine();
						Library.issueBook(connection, bookId, issueBookUsername);
					} else {
						System.out.println("Please login first.");
					}
					break;

				case 8:
					System.out.println("Enter your username:");
					String returnBookUsername = scanner.nextLine();
					if (isLoggedIn(connection,returnBookUsername)) {
						System.out.print("Enter the book ID to return: ");

						int bookId = scanner.nextInt();
						scanner.nextLine();
						Library.returnBook(connection, bookId, returnBookUsername);
					} else {
						System.out.println("Please login first.");

					}

					break;

				case 9:
					Library.displayBooks(connection);
					break;

				case 10:
					System.out.println("Enter your username:");
					String removeBookUsername = scanner.nextLine();
					if (isLoggedIn(connection,removeBookUsername)) {
						System.out.print("Enter the book ID to remove: ");
						int bookId = scanner.nextInt();
						scanner.nextLine();
						Library.removeBook(connection, bookId);
					} else {
						System.out.println("Please login first.");
					}
					break;

				case 11:
					System.out.println("Enter your username:");
					String viewCategoryUsername1 = scanner.nextLine();
					;
					if (isLoggedIn(connection,viewCategoryUsername1)) {
						getTopUsersByCategory(connection);
					} else {
						System.out.println("Please login first.");
					}
					break;
				
				case 12:
					Library.manageAccount(connection);
					break;	
					
				case 13:
					Library.getTotalBooks(connection);
					break;
					
				case 14:
					System.out.println("Enter Book_id you want to check: ");
					int bookId = scanner.nextInt();
					Library.checkAvailabilityFromParticularBook(connection, bookId);
					break;
					
				case 15:
					Library.getNumOfUsersPerBook(connection);
					break;
					
				case 16:
					System.out.println("Enter your username:");
					String payUsername = scanner.nextLine();
					if (isLoggedIn(connection,payUsername)) {
						System.out.println("Select an option:");
						System.out.println("1. Pay Fine");
						System.out.println("2. Pay Subscription");
						System.out.println("3. Subscribe");
						System.out.println("4. Unsubscribe");

						int paymentOption = scanner.nextInt();
						scanner.nextLine();
						switch (paymentOption) {
						case 1:
							Library.payFine(connection, payUsername);
							break;
						case 2:
							Library.paySubscription(connection, payUsername);
							break;
						case 4:
							Library.unsubscribe(connection, payUsername);
							break;
						case 3:
							Library.subscribe(connection, payUsername);
							break;
						default:
							System.out.println("Invalid payment option.");
						}
					} else {
						System.out.println("Please login first.");
					}
					break;
				case 17:
					System.out.println("Enter your username:");
					String viewBookUsuagersUsername = scanner.nextLine();
					if (isLoggedIn(connection,viewBookUsuagersUsername)) {
						System.out.print("Enter the book ID to view usagers: ");
						int bookId1 = scanner.nextInt();
						scanner.nextLine();
						Library.viewBookUsuagers(connection, bookId1);
					} else {
						System.out.println("Please login first.");
					}
					break;
				case 18:
					 Library.displayBookUsers(connection);
					 break;
				case 19:
					running = false;
					break;
				default:
					System.out.println("Invalid option. Please try again.");
				}
			}
			connection.close();
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Library Management System is Exits!");
		}
	}

	private static void createUser(Connection connection, String username, String password) throws SQLException {
	    String query = "INSERT INTO users(username, password, logged_in) VALUES (?, ?, 0)";
	    
	    try {
	        PreparedStatement statement = connection.prepareStatement(query);
	        statement.setString(1, username);
	        statement.setString(2, password);
	        statement.executeUpdate();
	        statement.close();
	        
	        System.out.println("\n\u001B[31mUser Created Successfully!\u001B[0m");
	    } catch (SQLIntegrityConstraintViolationException e) {
	        System.out.println("\u001B[31mUsername already exists. Please choose a different username.\u001B[0m");
	    }
	}

	public static boolean login(Connection connection, String username, String password) throws Exception {
		if (isLoggedIn(connection,username)) {
			System.out.println("User already logged in.");
			return true;
		}
		String query = "SELECT * FROM users WHERE username = ? AND password = ?";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, username);
		statement.setString(2, password);
		ResultSet resultSet = statement.executeQuery();
		boolean loginSuccessful = resultSet.next();

		if (loginSuccessful) {
			String updateQuery = "UPDATE users SET logged_in = 1 WHERE username = ?";
			PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
			updateStatement.setString(1, username);
			updateStatement.executeUpdate();
			updateStatement.close();
		}
		resultSet.close();
		statement.close();
		return loginSuccessful;
	}

	private static boolean logout(Connection connection, String username) throws Exception {

		String updateQuery = "UPDATE users SET logged_in = 0, logout = NOW() WHERE username = ?";

		PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
		updateStatement.setString(1, username);
		updateStatement.close();

		if (!isLoggedIn(connection,username)) {
			System.out.println("User logout successfully!");
			return false;
		}

		activeSessions.remove(username);
		loggedInUsers.remove(username);
		return true;
	}

	private static void displayCurrentUser() {
		System.out.println("Currently logged-in users:");
		if (loggedInUsers.isEmpty()) {
			System.out.println("No user currently logged in.");
		} else {
			for (String user : loggedInUsers.keySet()) {
				System.out.println(user);
			}
		}
	}

	private static boolean isLoggedIn(Connection connection,String username) throws SQLException {

		 String query = "SELECT logged_in FROM users WHERE username = ?";
		    PreparedStatement statement = connection.prepareStatement(query);
		    statement.setString(1, username);
		    ResultSet resultSet = statement.executeQuery();
		    
		    if (resultSet.next()) {
		        int loggedIn = resultSet.getInt("logged_in");
		        return loggedIn == 1;
		    }
		    
		    return false;
		

	}

	private static void getTopUsersByCategory(Connection connection) throws Exception {

		Scanner scanner = new Scanner(System.in);

		System.out.println("Select a category:");
		System.out.println("1. Fiction");
		System.out.println("2. Non-Fiction");
		int categoryChoice = scanner.nextInt();
		scanner.nextLine();

		String category = null;

		if (categoryChoice == 1) {
			category = "Fiction";
		} else if (categoryChoice == 2) {
			category = "Non-Fiction";
		} else {
			System.out.println("Invalid category choice.");
		}
		String query = " select u.username, count(b.id) as books_issued from users u inner join issued_books\n"
				+ "ib on u.username = ib.username inner join books b on ib.book_id = b.id where b.category =? group by u.username having books_issued >=0 order by books_issued desc limit 5";
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, category);
			ResultSet resultSet = statement.executeQuery();

			System.out.println("Top Users in " + category + " category:");
			int rank = 1;
			while (resultSet.next()) {
				String username = resultSet.getString("u.username");
				int booksIssued = resultSet.getInt("books_issued");
				System.out.println(rank + ". " + username + " - Books Issued: " + booksIssued);
				rank++;
			}

			resultSet.close();
			scanner.close();
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	private static void displayDueDate(Connection connection, String username) throws SQLException {
		String query = " select b.title,ib.due_date,ib.returned from issued_books ib "
				+ "join books b on ib.book_id = b.id where ib.username= ?";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, username);
		ResultSet resultSet = statement.executeQuery();

		System.out.println("Reminder!!!\nBooks currently issued by " + username + ":");
		System.out.printf("%-20s | %-12s | %-8s\n", "Title", "Due Date", "Status");
		System.out.println("----------------------------------------");

		while (resultSet.next()) {
			String title = resultSet.getString("title");
			LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
			boolean returned = resultSet.getBoolean("returned");

			if (returned) {
				System.out.printf("%-20s | %-12s | Returned\n", title, dueDate);
			} else {
				LocalDate currentDate = LocalDate.now();
				if (currentDate.isAfter(dueDate)) {
					long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
					double fineAmount = calculateFine(daysOverdue);
					System.out.printf("%-20s | %-12s | Overdue by %d days | Fine: $%.2f\n", title, dueDate, daysOverdue,
							fineAmount);
				} else {
					long daysLeft = ChronoUnit.DAYS.between(currentDate, dueDate);
					System.out.printf("%-20s | %-12s | %d days left\n", title, dueDate, daysLeft);
				}
			}
		}
	}

	private static double calculateFine(long daysOverdue) {
		double finePerDay = 10;
		double fine = 0;

		if (daysOverdue <= 0) {
			fine = 0;
		} else if (daysOverdue <= 7) {
			fine = finePerDay * daysOverdue;
		} else {
			fine = finePerDay * 7;
		}

		return fine;
	}
}