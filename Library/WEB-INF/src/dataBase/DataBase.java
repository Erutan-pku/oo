package dataBase;
import java.sql.*;
import java.util.Vector;


public class DataBase {
	public static Connection getConnection(String password) throws Exception {
        String driverName="com.mysql.jdbc.Driver"; //加载JDBC驱动
        String dbUrl="jdbc:mysql://localhost:3306/oolibrary?useUnicode=true&characterEncoding=utf-8&useSSL=false";
        String userName="root";
        Class.forName(driverName);
        return DriverManager.getConnection(dbUrl,userName,password);
    }
	private Connection connection;
	public DataBase(String password) {
		try {
			if (password==null)
				password="19920326";
			connection = getConnection(password);
		} catch (Exception e) {
			System.err.println("connection error");
		}
	}
	// 特殊查询
	public boolean checkElement(String table, String name, String value) {
		try {
			String sql="select *from " + table + " where " + name + "=" + value;
			PreparedStatement ps=connection.prepareStatement(sql);
	        ResultSet rs = ps.executeQuery();
	        
	        int count = 0;
	        while (rs.next())
	        	count++;
	        return count > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	// 增
	public void InsertUser(String ID) {
		try {
			String sql="insert into user (uniformID,hasRight,isKeeper) " + 
					   "values("+ID+",true,false)";
			PreparedStatement ps=connection.prepareStatement(sql);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void InsertBook(String BookID,String name,String information,String state,String returnTime,String borrowedUserID) {
		try {
			String sql="insert into book (BookID,name,information,state,returnTime,borrowedUserID) " + 
					   "values("+BookID+","+name+","+information+","+state+","+returnTime+","+borrowedUserID+")";
			PreparedStatement ps=connection.prepareStatement(sql);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// 删
	public void removeData(String table, String field, String value) {
		try {
			String sql = "delete from " + table + " where "+field+"=" + value;
			Statement ps=connection.createStatement();
			ps.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// 查
	public Vector<String> getUserBorrowedList(String UserID) {
		Vector<String> ret = new Vector<>();
		try {
			String sql="select BookID from book where borrowedUserID= "+UserID+" and state!=0";
			PreparedStatement ps=connection.prepareStatement(sql);
	        ResultSet rs = ps.executeQuery();
	        int col = rs.getMetaData().getColumnCount();
	        while (rs.next()) {
	        	for (int i = 1; i <= col; i++)
                    ret.add(rs.getString(i));
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	public Vector<Vector<String>> getTable(String table) {
		Vector<Vector<String>> ret = new Vector<Vector<String>>();
		try {
			String sql="select * from "+table;
			PreparedStatement ps=connection.prepareStatement(sql);
	        ResultSet rs = ps.executeQuery();
	        int col = rs.getMetaData().getColumnCount();
	        while (rs.next()) {
	        	Vector<String> ret0 = new Vector<String>();
	        	for (int i = 1; i <= col; i++)
                    ret0.add(rs.getString(i));
	        	ret.add(ret0);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	public Vector<String> getData(String table, String name, String value) {
		Vector<String> ret = new Vector<String>();
		try {
			String sql="select *from "+table+" where "+name+"="+value;
			PreparedStatement ps=connection.prepareStatement(sql);
	        ResultSet rs = ps.executeQuery();
	        int col = rs.getMetaData().getColumnCount();
	        while (rs.next()) {
	        	for (int i = 1; i <= col; i++)
                    ret.add(rs.getString(i));
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	public String getElementField(String table, String field, String name, String value) {
		Vector<String> ret = new Vector<String>();
		try {
			String sql="select "+field+" from " + table + " where " + name + "=" + value + "";
			PreparedStatement ps=connection.prepareStatement(sql);
	        ResultSet rs = ps.executeQuery();
	        
	        int col = rs.getMetaData().getColumnCount();
	        while (rs.next())
	        	for (int i = 1; i <= col; i++)
	        		ret.add(rs.getString(i));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret.get(0);
	}
	// 改
	public void updateField(String table,String indexField, String indexValue, String field, String value) {
		try {
			Statement st = connection.createStatement();
			//System.out.println("update "+table+" set "+field+"="+value+" where "+indexField+"="+indexValue+"");
			st.executeUpdate("update "+table+" set "+field+"="+value+" where "+indexField+"="+indexValue+"");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
