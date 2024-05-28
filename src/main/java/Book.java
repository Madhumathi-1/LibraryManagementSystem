class Book {
	private int id;
	private String title;
	private String author;
	private int available;
	private double price;

	public Book(int id, String title, String author, double price, int available) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.available = available;
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public int getAvailable() {
		return available;
	}

	public double getPrice() {
		return price;
	}
}