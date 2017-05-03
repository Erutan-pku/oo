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
	 * Check password using the ultra-user API
	 * Now, always return True.
	 * @param UserID
	 * @param password
	 * @return
	 */
	static boolean checkAccount(String UserID, String password) {
		return true;
	}
    /**
     * Return True only if the superPassword is correct and IP is in special field.
     * Now, only check the superPassword
     * @param password
     * @param ip 
     * @return
     */
	static boolean checkSuperPassword(String superPassword, String IP) {
    	return superPassword.equals("nice_job");
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
	private static String checkLogin(HttpServletRequest request, JSONObject ret) {
		String UserID = (String) request.getSession().getAttribute("userID");
		if (UserID.equals("#NULL")) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "needLogin");
			return null;
		}
		return UserID;
	}
	private static boolean checkKeeper(JSONObject ret, String UserID) {
		String isKeeper = dataBase.getElementField("user", "isKeeper", "uniformID", "'"+UserID+"'");
    	System.out.println(isKeeper);
		if (!isKeeper.equals("1")) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NotKeeper");
			return false;
    	}
    	return true;
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
			changeKeeper(request, ret, IP, "1");
		} else if (Type.equals("removeKeeper")) {
			changeKeeper(request, ret, IP, "0");
		} else if (Type.equals("punishUser")) {
			changeRight(request, ret, "0");
		} else if (Type.equals("recoverUser")) {
			changeRight(request, ret, "1");
		} else if (Type.equals("addBook")) {
			addBook(request, ret);
		} else if (Type.equals("removeBook")) {
			removeBook(request, ret);
		} else if (Type.equals("checkBookID")) {
			checkBookID(request, ret);
		} else if (Type.equals("checkBookList")) {
			checkBookList(request, ret);
		} else if (Type.equals("checkBorrowedBookList_me")) {
			checkBorrowedBookList_me(request, ret);
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
    	
    	if (!checkAccount(ID, password)) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongPassword");
			return;
    	}
    	
    	if (!dataBase.checkElement("user", "uniformID", "'"+ID+"'")) {
    		ret.accumulate("register", "true");
    		dataBase.InsertUser("'"+ID+"'");
    	}
    	
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
		String UserID = (String) request.getSession().getAttribute("userID");
		if (UserID.equals("#NULL")) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NoRecord");
			return;
		}
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
		String UserID = (String) request.getSession().getAttribute("userID");
		if (UserID.equals("#NULL")) {
			ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NoRecord");
			return;
		}
		Vector<String> UserData = dataBase.getData("user", "uniformID", "'"+UserID+"'");
    	ret.accumulate("isSuccess", "true");
    	ret.accumulate("uniformID", UserData.get(0));
    	ret.accumulate("hasRight", UserData.get(1));
    	ret.accumulate("isKeeper", UserData.get(2));
    }
    /**
     * 指定管理员权限
     * @param request
     * @param ret
     * @param IP
     */
    public void changeKeeper(HttpServletRequest request, JSONObject ret, String IP,String value) {
    	String[] inforKey = {"ID", "superPassword"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String ID 		= Data.getString("ID");
    	String superPassword = Data.getString("superPassword");
    	
    	if (!checkSuperPassword(superPassword, IP)) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "WrongSuperPasswordIP");
			return;
    	}
    	
    	if (!dataBase.checkElement("user", "uniformID", "'"+ID+"'")) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NoSuchUser");
			return;
    	}
    	
    	String string_db = dataBase.getElementField("user", "isKeeper", "uniformID", "'"+ID+"'");
    	if (string_db.equals(value)) {
    		ret.accumulate("isSuccess", "false");
    		ret.accumulate("error", "NothingChanged,isKeeper="+string_db);
			return;
    	}
    	dataBase.updateField("user", "uniformID", "'"+ID+"'", "isKeeper", "'"+value+"'");
    	ret.accumulate("isSuccess", "true");
    }
    /**
     * 变更用户权限
     * @param request
     * @param ret
     */
    public void changeRight(HttpServletRequest request, JSONObject ret, String value) {
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
    	
    	boolean isKeeper = checkKeeper(ret, UserID);
    	if (!isKeeper) return;
    	System.out.println(isKeeper);
    	
    	String[] inforKey = {"changedID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String changedID = Data.getString("changedID");
    	
    	if (!dataBase.checkElement("user", "uniformID", "'"+changedID+"'")) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "NoSuchUser");
			return;
    	}
    	System.out.println("checkPoint1");
    	System.out.println("001\t"+ret.toString());
    	System.out.println("checkPoint2");
    	
    	if (dataBase.getElementField("user", "isKeeper", "uniformID", "'"+changedID+"'").equals("1")) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "cannotPunichKeeper");
			return;
    	}
    	System.out.println("002\t"+ret.toString());
    	
    	String string_db = dataBase.getElementField("user", "hasRight", "uniformID", "'"+changedID+"'");
    	if (string_db.equals(value)) {
    		System.out.println(string_db);
    		System.out.println(value);
    		ret.accumulate("isSuccess", "false");
    		ret.accumulate("error", "NothingChanged,hasRight="+string_db);
			return;
    	}
    	System.out.println("003\t"+ret.toString());
    	
    	dataBase.updateField("user", "uniformID", "'"+changedID+"'", "hasRight", "'"+value+"'");
    	ret.accumulate("isSuccess", "true");
    	System.out.println("004\t"+ret.toString());
    }
    /**
     * 添加图书
     * @param request
     * @param ret
     */
    public void addBook(HttpServletRequest request, JSONObject ret) {
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
		
    	String[] inforKey = {"bookID", "bookName", "bookInfor"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID	= Data.getString("bookID");
    	String bookName = Data.getString("bookName");
    	String bookInfor= Data.getString("bookInfor");
    	
    	boolean isKeeper = checkKeeper(ret, UserID);
    	if (!isKeeper) return;
    	
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
		
    	String[] inforKey = {"bookID"};
    	JSONObject Data = getInformation(request, inforKey, ret);
    	if (Data == null) return;
    	String bookID 		= Data.getString("bookID");
    	
    	boolean isKeeper = checkKeeper(ret, UserID);
    	if (!isKeeper) return;
    	
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
		
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
		
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
     * 查询自己借的书
     * @param request
     * @param ret
     */
    public void checkBorrowedBookList_me(HttpServletRequest request, JSONObject ret) {
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
		
    	Vector<Book> books= library.bookList.searchUserBooks(UserID);
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
    	
    	boolean isKeeper = checkKeeper(ret, UserID);
    	if (!isKeeper) return;
		
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
    	
    	boolean isKeeper = checkKeeper(ret, UserID);
    	if (!isKeeper) return;
		
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
    	if (!dataBase.checkElement("user", "uniformID", "'"+borrowedUserID+"'")) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "borrowedUserIDNotFound");
			return;
    	}
    	if (dataBase.getElementField("user", "hasRight", "uniformID", "'"+borrowedUserID+"'").equals("0")) {
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
		
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
    	if (dataBase.getElementField("user", "hasRight", "uniformID", "'"+UserID+"'").equals("0")) {
    		ret.accumulate("isSuccess", "false");
			ret.accumulate("error", "noRight");
			return;
    	}
    	
    	boolean isSuccess = book.setRenewed(dataBase, UserID);
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
    	String UserID = checkLogin(request, ret);
    	if (UserID == null) return;
    	
    	boolean isKeeper = checkKeeper(ret, UserID);
    	if (!isKeeper) return;
		
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