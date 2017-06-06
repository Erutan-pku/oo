package library;

public class User {
	public String userID;
	public boolean hasRight;
	public boolean isKeeper;
	
	public User(String userID_) {
		userID = userID_;
		hasRight = true;
		isKeeper = false;
	}
	public User(String userID_, boolean hasRight_, boolean isKeeper_){
		userID = userID_;
		hasRight = hasRight_;
		isKeeper = isKeeper_;
	}
	
	public void changeRight() {
		hasRight = !hasRight;
	}
	public void changeKeeper() {
		isKeeper = !isKeeper;
	}
}
