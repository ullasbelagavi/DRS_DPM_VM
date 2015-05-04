package mongo;





import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.mysql.jdbc.PreparedStatement;

public class MongoToMySql {
	private static DB db;

	private static Connection conn;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://127.0.0.1:3306/logdb";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "";

	private static DB connectToMongoDb() throws UnknownHostException {
		if (db == null) {
			MongoClient client = new MongoClient();
			db = client.getDB("logdb");
		}
		return db;
	}

	public static Connection connectToMySql() {
		if (conn == null) {
			try {
				Class.forName(DRIVER);
				conn = DriverManager
						.getConnection(URL, USERNAME, PASSWORD);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}

	private static void archiveDataOfMongoDb() throws UnknownHostException {
		DBCollection tbl = connectToMongoDb().getCollection("data");
		Date today = new Date();
		String atblname = "archive"+today.getYear()+today.getMonth()+today.getDate();
		DBCollection atbl = connectToMongoDb().getCollection(atblname);
		DBCursor cur = tbl.find();
		while (cur.hasNext()) {
			atbl.insert(cur.next());
		}
		tbl.drop();
	}

	public static String getAggregateData() throws UnknownHostException {
		//DBCollection tbl = getConnection().getCollection("logs4");
		//tbl.rename("temp_logs4");
		DBCollection tbl = connectToMongoDb().getCollection("data");
		String grp = "{$group:{_id:'$vmname',avgcpu:{$avg:'$cpu'},avgmemory:{$avg:'$memory'},avgdisk:{$avg:'$disk'},avgnetwork:{$avg:'$network'},avgsystem:{$avg:'$system'}}}";
		
		DBObject group = (DBObject) JSON.parse(grp);
		AggregationOutput output = tbl.aggregate(group);
		ArrayList<DBObject> list = (ArrayList<DBObject>) output.results();
		for (DBObject dbObject : list) {
			System.out.println(dbObject);
			insertIntoMySql(dbObject);
		}
		archiveDataOfMongoDb();
		return "";
	}

	public static void insertIntoMySql(DBObject obj) {
		try {
			PreparedStatement st = (PreparedStatement) connectToMySql().prepareStatement("insert into logdb.log(id,host,time,vmname,cpu,memory,disk,network,system) values(?,?,?,now(),?,?,?,?,?)");
			st.setString(1, "1");
			st.setString(2, "team12-desktop");
			st.setString(3, obj.get("_id").toString());
			st.setDouble(4, Double.parseDouble( obj.get("avgcpu").toString()));
			st.setDouble(5, Double.parseDouble( obj.get("avgmemory").toString()));
			st.setDouble(6, Double.parseDouble( obj.get("avgdisk").toString()));
			st.setDouble(7, Double.parseDouble( obj.get("avgnetwork").toString()));
			st.setDouble(8, Double.parseDouble( obj.get("avgsystem").toString()));
			st.executeUpdate();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static Thread t1 = new Thread(){
		public void run(){
			while(true){
			try{
			getAggregateData();
			Thread.sleep(300000);
			}catch(UnknownHostException e){
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		}
	};

	public static void main(String[] args) throws UnknownHostException {
		t1.start();
	}
}
