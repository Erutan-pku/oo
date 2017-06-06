package library;

import java.text.*;
import java.util.*;

import dataBase.DataBase;
import library.Book.BookState;

public class BookList {
	private DataBase dataBase = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private HashMap<String, Book> booklist = new HashMap<String, Book>();
	BookList(DataBase db) {
		try {
			dataBase = db;
			Vector<Vector<String>> bookTable = db.getTable("book");
			for (Vector<String> bookinfor : bookTable) {
				Date date = sdf.parse(bookinfor.get(4));
				BookState state = BookState.values()[Integer.valueOf(bookinfor.get(3))];
				Book book = new Book(bookinfor.get(0), bookinfor.get(1), bookinfor.get(2), date, bookinfor.get(5), state);
				booklist.put(bookinfor.get(0), book);
			}
		} catch (NumberFormatException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("BookList init error!");;
		}
	}
	public boolean addBook(String bookID, String bookName, String bookInformation) {
		if (booklist.containsKey(bookID))
			return false;
		Book book_t = new Book(bookID, bookName, bookInformation);
		booklist.put(bookID, book_t);
		
		Vector<String> SQL_type = book_t.getSqlFormat();
		dataBase.InsertBook(SQL_type.get(0), SQL_type.get(1), SQL_type.get(2), SQL_type.get(3), SQL_type.get(4), SQL_type.get(5));
		
		return true;
	}
	public boolean removeBook(String bookID) {
		if (!booklist.containsKey(bookID))
			return false;
		booklist.remove(bookID);
		dataBase.removeData("book", "BookID", "'"+bookID+"'");
		return true;
	}
	public Book searchBookID(String bookID) {
		return booklist.getOrDefault(bookID, null);
	}
	public Vector<Book> searchBooksName(String bookName) {
		Vector<Book> ret = new Vector<>();
		for (String key : booklist.keySet()) {
			Book book_t = booklist.get(key);
			if (book_t.getName().indexOf(bookName) == -1)
				continue;
			ret.add(book_t);
		}
		return ret;
	}
	public Vector<Book> searchUserBooks(String UserID) {
		Vector<Book> ret = new Vector<>();
		Vector<String> BookIDList = dataBase.getUserBorrowedList("'"+UserID+"'");
		for (String bookID : BookIDList)
			ret.add(booklist.get(bookID));
		return ret;
	}
	public Vector<Book> searchDueBooks(String UserID) {
		Date date_now = new Date();
		Vector<Book> ret = new Vector<>();
		Vector<String> BookIDList = dataBase.getUserBorrowedList("'"+UserID+"'");
		for (String bookID : BookIDList) {
			Book book = booklist.get(bookID);
			
			Date date_then = book.getReturnTime();
			long intervalMilli = date_then.getTime() - date_now.getTime();
		    int delta_days = (int) (intervalMilli / (24 * 60 * 60 * 1000));
		    if (delta_days > 30) 
		    	continue;
		    
			ret.add(book);
		}
		return ret;
	}
	public Vector<Book> getAllBooks() {
		Vector<Book> ret = new Vector<Book>();
		for (String BookID : booklist.keySet())
			ret.add(booklist.get(BookID));
		return ret;
	}}
