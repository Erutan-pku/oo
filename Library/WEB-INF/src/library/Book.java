package library;

import java.text.SimpleDateFormat;
import java.util.*;

import dataBase.DataBase;
import net.sf.json.JSONObject;

public class Book {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar calendar = Calendar.getInstance();
	
	private String name;
	private String ID;
	public String information;
	public Date returnTime;
	public String borrowedUserID;
	public enum BookState{
		free, borrowed, renewed
	}
	private BookState state;
	 
	public String getName() { return name; }
	public String getID() { return ID; }
	public String getInformation() { return information; }
	public BookState getBookState() { return state; }
	public Date getReturnTime() {
		if (hasBorrowed())
			return returnTime;
		else
			return null;
	}
	public String getBorrowedUserID() {
		if (hasBorrowed())
			 return borrowedUserID;
		else
			return null;
	}
	public Vector<String> getSqlFormat() {
		Vector<String> ret = new Vector<String>();
		String book_t_returnTime = sdf.format(returnTime);
		String book_t_state = String.valueOf(getBookState().ordinal());
		ret.add("'"+getID()+"'");
		ret.add("'"+getName()+"'");
		ret.add("'"+getInformation()+"'");
		ret.add(book_t_state);
		ret.add("'"+book_t_returnTime+"'");
		ret.add("'"+borrowedUserID+"'");
		return ret;
	}
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.accumulate("name", name);
		ret.accumulate("ID", ID);
		ret.accumulate("information", information);
		ret.accumulate("state", state.ordinal());
		if (hasBorrowed()) {
			ret.accumulate("borrowedUserID", borrowedUserID);
			ret.accumulate("returnTime", returnTime.toString());
		}
		return ret;
	}
	
	public boolean hasBorrowed() {
		return getBookState() != BookState.free;
	}
	public boolean setBorrowed(DataBase db, String _borrowedUserID) {
		if (hasBorrowed())
			return false;
		 
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, 6);
		returnTime = calendar.getTime();
		 
		state = BookState.borrowed;
		borrowedUserID = _borrowedUserID;
		 
		db.updateField("book", "BookID", "'"+getID()+"'", "returnTime", "'"+sdf.format(returnTime)+"'");
		db.updateField("book", "BookID", "'"+getID()+"'", "state", String.valueOf(state.ordinal()));
		db.updateField("book", "BookID", "'"+getID()+"'", "borrowedUserID", "'"+borrowedUserID+"'");
		return true;
	 }
	public boolean setRenewed(DataBase db, String userID) {
		if (getBookState() != BookState.borrowed)
			return false;
		if (!borrowedUserID.equals(userID))
			return false;
		state = BookState.renewed;
		
		calendar.setTime(returnTime);
		calendar.add(Calendar.MONTH, 3);
		returnTime = calendar.getTime();
		
		db.updateField("book", "BookID", "'"+getID()+"'", "returnTime", "'"+sdf.format(returnTime)+"'");
		db.updateField("book", "BookID", "'"+getID()+"'", "state", String.valueOf(state.ordinal()));
		return true;
	}
	public boolean setReturned(DataBase db) {
		if (!hasBorrowed())
			return false;
		state = BookState.free;
		
		db.updateField("book", "BookID", "'"+getID()+"'", "state", String.valueOf(state.ordinal()));
		return true;
	}
	
	Book(String _ID, String _name, String _information) {
		this(_ID, _name, _information, new Date(), "", BookState.free);
	}
	Book(String _ID, String _name, String _information, Date _returnTime, String _borrowedUserID, BookState _state) {
		name = _name;
		ID = _ID;
		information = _information;
		state = _state;
		returnTime = _returnTime;
		borrowedUserID = _borrowedUserID;
	}
}
