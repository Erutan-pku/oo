import java.io.*;
import java.util.Date;
import java.util.Vector;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import dataBase.DataBase;
import library.Book;
import library.Library;
import library.User;

public class WebSever extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Library library;
	static Log log = new Log("OOLibrary_log");
	static DataBase dataBase = new DataBase(null);
	static HTMLDecoder htmlDecoder = new HTMLDecoder();
	static {
		library = new Library(dataBase);
	}
	/**
     * get information from request.
     * @param request
     * @param inforKey
     * @return
     * @throws Exception
     */
    private static JSONObject getInformation(HttpServletRequest request, String[] inforKey, JSONObject ret) {
    	try {
    		JSONObject Data = new JSONObject();
        	for (String key : inforKey)
        		Data.accumulate(key, request.getParameter(key));
    		log.writeLine(Data.toString());
    		return Data;
		} catch (Exception e) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "ParameterCannotLoad");
			return null;
		}
    }
	private static User checkLogin(HttpServletRequest request, JSONObject ret, boolean checkKeeper) {
		String UserID = (String) request.getSession().getAttribute("userID");
		if (UserID.equals("#NULL")) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "needLogin");
			return null;
		}
		User user = library.userList.getUser(UserID);
		if (user == null) {
			ret.accumulate("isSuccess", "true");
			ret.accumulate("error", "wrongUserIDSession");
			return null;
		}
		if (checkKeeper && !user.isKeeper) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NotKeeper");
			return null;
		}
		return user;
	}

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
        out.println("Hello World!");
        out.println("<br>");
        out.println("Contacts: erutan.pkuicst@gmail.com");
        out.flush();
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    		 throws ServletException, IOException {
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	HttpSession httpSession = request.getSession();
    	if (httpSession.getAttribute("userID") == null)
    		httpSession.setAttribute("userID", "#NULL");
    	System.out.println(httpSession.getAttribute("userID"));
    	JSONObject ret = new JSONObject();
    	String Type = "TypeCannotGet";
    	String IP = request.getRemoteAddr();
		try {
			Type = request.getParameter("type");
		} catch (Exception e){}
		log.writeLine(new Date()+ "\n" + IP + "\t" + httpSession.getAttribute("userID") + "\t" + Type);
		/**
		 **User-control
		 * login - if not registered, automatically do so.
		 * logout
		 * checkBookID
		 * checkBookList
		 * checkBookName
		 * checkBorrowedBookList_me
		 * renewBook
		 * 
		 **Keeper-control
		 * punishUser
		 * recoverUser
		 * addBook
		 * removeBook
		 * checkBorrowedBookList_user
		 * borrowBook
		 * returnBook
		 * 
		 **Administrator-control
		 * MakeKeeper - turn a user to keeper 
		 * RemoveKeeper
		 * 
		 **Page-control
		 * checklog
		 * 
		 */
		
		if (Type.equals("login")) {
			login(request, ret);
		} else if (Type.equals("logout")) {
			logout(request, ret);
		} else if (Type.equals("checklog")) {
			checklog(request, ret);
		} else if (Type.equals("makeKeeper")) {
			changeKeeper(request, ret, IP, true);
		} else if (Type.equals("removeKeeper")) {
			changeKeeper(request, ret, IP, false);
		} else if (Type.equals("punishUser")) {
			changeRight(request, ret, false);
		} else if (Type.equals("recoverUser")) {
			changeRight(request, ret, true);
		} else if (Type.equals("addBook")) {
			addBook(request, ret);
		} else if (Type.equals("removeBook")) {
			removeBook(request, ret);
		} else if (Type.equals("checkBookID")) {
			checkBookID(request, ret);
		} else if (Type.equals("checkBookList")) {
			checkBookList(request, ret);
		} else if (Type.equals("checkBookName")) {
			checkBookName(request, ret);
		} else if (Type.equals("checkBorrowedBookList_me")) {
			checkBorrowedBookList_me(request, ret);
		} else if (Type.equals("checkBorrowedBookList_me_due")) {
			checkBorrowedBookList_me_due(request, ret);
		} else if (Type.equals("checkBorrowedBookList_user")) {
			checkBorrowedBookList_user(request, ret);
		} else if (Type.equals("borrowBook")) {
			borrowBook(request, ret);
		} else if (Type.equals("renewBook")) {
			renewBook(request, ret);
		} else if (Type.equals("returnBook")) {
			returnBook(request, ret);
		} else {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NoSuchType:" + Type);
		}
		
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		log.writeLine(ret.toString());
		log.writeLine("");
		PrintWriter out = response.getWriter();
		out.print(ret);
		out.flush(); out.close();
	}
    
    /**
     * 登录
     * @param request
     * @param ret
     * @param IP
     */
    public void login(HttpServletRequest request, JSONObject ret) {
    	String[] inforKey = {"ID", "password"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String ID 		= Data.getString("ID");
    	String password = Data.getString("password");
    	
    	if (!library.userList.checkAccount(ID, password)) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongPassword");
			return;
    	}
    	
    	if (!library.userList.checkHas(ID))
    		ret.accumulate("register", "true");
    	library.userList.loginOrRegister(ID);
    	
    	request.getSession().setAttribute("userID", ID);
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 退出
     * @param request
     * @param ret
     * @param IP
     */
    public void logout(HttpServletRequest request, JSONObject ret) {
		log.writeLine(new JSONObject().toString());
		User user = checkLogin(request, ret, false);
    	if (user == null) return;

		request.getSession().setAttribute("userID", "#NULL");
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 检查登录
     * @param request
     * @param ret
     * @param IP
     */
    public void checklog(HttpServletRequest request, JSONObject ret) {
    	JSONObject Data = new JSONObject();
		log.writeLine(Data.toString());
		String userID = (String) request.getSession().getAttribute("userID");
		if (userID.equals("#NULL")) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NoRecord");
			return;
		}
		User user = library.userList.getUser(userID);
		if (user == null) {
			ret.accumulate("isSuccess", "true");
			ret.accumulate("error", "wrongUserIDSession");
			return;
		}
		
    	ret.accumulate("isSuccess", "true");
    	ret.accumulate("uniformID", user.userID);
    	ret.accumulate("hasRight", user.hasRight);
    	ret.accumulate("isKeeper", user.isKeeper);
    }
    /**
     * 指定管理员权限
     * @param request
     * @param ret
     * @param IP
     */
    public void changeKeeper(HttpServletRequest request, JSONObject ret, String IP, boolean value) {
    	String[] inforKey = {"ID", "superPassword"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String ID 		= Data.getString("ID");
    	String superPassword = Data.getString("superPassword");
    	
    	if (!library.userList.checkSuperPassword(superPassword, IP)) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongSuperPasswordIP");
			return;
    	}
    	
    	boolean isSuccess = library.userList.changKeeper(ID, value);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "changeKeeperFail");
			return;
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 变更用户权限
     * @param request
     * @param ret
     */
    public void changeRight(HttpServletRequest request, JSONObject ret, boolean value) {
    	User user = checkLogin(request, ret, true);
    	if (user == null) return;
    	
    	String[] inforKey = {"changedID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String changedID = Data.getString("changedID");
    	
    	boolean isSuccess = library.userList.changeRight(changedID, value);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "changeRightFail");
			return;
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 添加图书
     * @param request
     * @param ret
     */
    public void addBook(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, true);
    	if (user == null) return;
		
    	String[] inforKey = {"bookID", "bookName", "bookInfor"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID	= Data.getString("bookID");
    	String bookName = Data.getString("bookName");
    	String bookInfor= Data.getString("bookInfor");
    	
    	boolean isSuccess = library.bookList.addBook(bookID, bookName, bookInfor);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongBookInfor");
			return;
    	}
   
    	ret.accumulate("isSuccess", "true");
    	ret.accumulate("addedBookID", bookID);
    }
    /**
     * 删除图书
     * @param request
     * @param ret
     */
    public void removeBook(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, true);
    	if (user == null) return;
		
    	String[] inforKey = {"bookID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID 		= Data.getString("bookID");
    	
    	boolean isSuccess = library.bookList.removeBook(bookID);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongBookInfor");
			return;
    	}
    	
    	ret.accumulate("isSuccess", "true");
    	ret.accumulate("removedBookID", bookID);
    }
    /**
     * 查看所有图书
     * @param request
     * @param ret
     */
    public void checkBookList(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, false);
    	if (user == null) return;
		
    	Vector<Book> books= library.bookList.getAllBooks();
    	ret.accumulate("booknum", books.size());
    	for (Book book : books) {
    		ret.accumulate("book", book.toJson());
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 查询指定ID的图书
     * @param request
     * @param ret
     */
    public void checkBookID(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, false);
    	if (user == null) return;
		
    	String[] inforKey = {"bookID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID	= Data.getString("bookID");
    	
    	Book book = library.bookList.searchBookID(bookID);
    	
    	if (book == null) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongBookID");
			return;
    	}
   
    	ret.accumulate("book", book.toJson());
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 查询指定名字的图书，支持模糊匹配（目前只做了子串）
     * @param request
     * @param ret
     */
    public void checkBookName(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, false);
    	if (user == null) return;
		
    	String[] inforKey = {"bookName"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookName	= Data.getString("bookName");
    	
    	Vector<Book> books= library.bookList.searchBooksName(bookName);
    	ret.accumulate("booknum", books.size());
    	for (Book book : books) {
    		ret.accumulate("book", book.toJson());
    	}
    	
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 查询自己借的快到期的书
     * @param request
     * @param ret
     */
    public void checkBorrowedBookList_me_due(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, false);
    	if (user == null) return;
		
    	Vector<Book> books= library.bookList.searchDueBooks(user.userID);
    	ret.accumulate("booknum", books.size());
    	for (Book book : books) {
    		ret.accumulate("book", book.toJson());
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 查询自己借的书
     * @param request
     * @param ret
     */
    public void checkBorrowedBookList_me(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, false);
    	if (user == null) return;
		
    	Vector<Book> books= library.bookList.searchUserBooks(user.userID);
    	ret.accumulate("booknum", books.size());
    	for (Book book : books) {
    		ret.accumulate("book", book.toJson());
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 查询指定用户借出的图书
     * @param request
     * @param ret
     */
    public void checkBorrowedBookList_user(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, true);
    	if (user == null) return;
		
    	String[] inforKey = {"checkUserID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String checkUserID	= Data.getString("checkUserID");
    	
    	Vector<Book> books= library.bookList.searchUserBooks(checkUserID);
    	ret.accumulate("booknum", books.size());
    	for (Book book : books) {
    		ret.accumulate("book", book.toJson());
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 借书
     * @param request
     * @param ret
     */
    public void borrowBook(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, true);
    	if (user == null) return;
		
    	String[] inforKey = {"bookID", "borrowedUserID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID			= Data.getString("bookID");
    	String borrowedUserID	= Data.getString("borrowedUserID");
    	
    	Book book = library.bookList.searchBookID(bookID);
    	if (book == null) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongBookID");
			return;
    	}
    	User borrowedUser = library.userList.getUser(borrowedUserID);
    	if (borrowedUser == null) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "borrowedUserIDNotFound");
			return;
    	}
    	if (!borrowedUser.hasRight) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "noRight");
			return;
    	}
    	
    	boolean isSuccess = book.setBorrowed(dataBase, borrowedUserID);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "borrowFailed");
			return;
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 续借
     * @param request
     * @param ret
     */
    public void renewBook(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, false);
    	if (user == null) return;
		
    	String[] inforKey = {"bookID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID	= Data.getString("bookID");
    	
    	Book book = library.bookList.searchBookID(bookID);
    	if (book == null) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongBookID");
			return;
    	}
    	if (!user.hasRight) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "noRight");
			return;
    	}
    	
    	boolean isSuccess = book.setRenewed(dataBase, user.userID);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "renewFailed");
			return;
    	}
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 还书
     * @param request
     * @param ret
     */
    public void returnBook(HttpServletRequest request, JSONObject ret) {
    	User user = checkLogin(request, ret, true);
    	if (user == null) return;
		
    	String[] inforKey = {"bookID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID			= Data.getString("bookID");
    	
    	Book book = library.bookList.searchBookID(bookID);
    	if (book == null) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongBookID");
			return;
    	}
    	
    	boolean isSuccess = book.setReturned(dataBase);
    	if (!isSuccess) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "returnFailed");
			return;
    	}
    	ret.accumulate("isSuccess", "true");
    }
}