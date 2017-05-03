package library;

import dataBase.DataBase;

public class Library {
	DataBase dataBase;
	public BookList bookList = null;
	public Library(DataBase _db) {
		dataBase = _db;
		bookList = new BookList(_db);
	}
}
