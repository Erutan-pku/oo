package library;


import java.util.HashMap;
import java.util.Vector;

import dataBase.DataBase;

public class UserList {
	private DataBase dataBase = null;
	private HashMap<String, User> userList = new HashMap<>();
	UserList(DataBase db) {
		try {
			dataBase = db;
			Vector<Vector<String>> userTable = db.getTable("user");
			for (Vector<String> userinfor : userTable) {
				String userID = userinfor.get(0);
				boolean hasRight = userinfor.get(1).equals('1');
				boolean isKeeper = userinfor.get(2).equals('1');
				User user = new User(userID, hasRight, isKeeper);
				userList.put(userID, user);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("UserList init error!");;
		}
	}
	/**
	 * Check password using the ultra-user API
	 * Now, always return True.
	 * @param UserID
	 * @param password
	 * @return
	 */
	public boolean checkAccount(String userID, String password) {
		return true;
	}
	/**
     * Return True only if the superPassword is correct and IP is in special field.
     * Now, only check the superPassword
     * @param password
     * @param ip 
     * @return
     */
	public boolean checkSuperPassword(String superPassword, String IP) {
    	return superPassword.equals("nice_job");
    }
	
	public boolean checkHas(String userID) {
		return userList.containsKey(userID);
	}
	public User loginOrRegister(String userID) {
		User user = userList.getOrDefault(userID, null);
		if (user != null)
			return user;
		
		user = new User(userID);
		dataBase.InsertUser("'"+userID+"'");
		userList.put(userID, user);
		return user;
	}
	public User getUser(String userID) {
		return userList.getOrDefault(userID, null);
	}
	
	public boolean changeRight(String userID, boolean target) {
		User user = getUser(userID);
		if (user==null) 
			return false;
		if (user.isKeeper)
			return false;
		if (user.hasRight == target)
			return false;
		String value = target ? "1" : "0";
		user.changeRight();
		dataBase.updateField("user", "uniformID", "'"+userID+"'", "hasRight", "'"+value+"'");
		return true;
	}
	public boolean changKeeper(String userID, boolean target) {
		User user = getUser(userID);
		if (user==null) 
			return false;
		if (user.isKeeper == target)
			return false;
		String value = target ? "1" : "0";
		user.changeKeeper();
		dataBase.updateField("user", "uniformID", "'"+userID+"'", "isKeeper", "'"+value+"'");
		return true;
	}
}
