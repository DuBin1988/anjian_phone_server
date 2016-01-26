package com.aote.rs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.aote.rs.util.DynamicDataSource.DbContextHolder;

//通过URL调用TestDataSource方法，测试能否通过拦截URL转换数据源
@Path("test")
@Component
public class TestDynamicDataSource {
	static Logger log = Logger.getLogger(TestDynamicDataSource.class);
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@GET
	@Path("data")
	public String TestDataSource(){
		try {
			//通过hibernate查询数据源dataSource1的数据库
			DbContextHolder.setDbType("dataSource1");
			
			String UserSql = "from t_user where name = '超级管理员'";
			List<Object> UserList = new ArrayList<Object>();
			Map<String, Object> UserMap = new HashMap<String, Object>();
			
			UserList = this.hibernateTemplate.find(UserSql);
			String Name = "";
			String ParentName = "";
			
			if(UserList.size() == 0)
			{
				return "not ok. Because the result is null.";
			}
			UserMap = (Map<String, Object>) UserList.get(0);
			Name = UserMap.get("name") == null ? "" : UserMap.get("name").toString();
			ParentName = UserMap.get("f_parentname") == null ? "0" : UserMap.get("f_parentname").toString();
			System.out.println("dataSource1 : Name : " + Name + ", ParentName : " + ParentName);
			
			DbContextHolder.clearDbType();
			
			//通过JDBC查询数据源dataSource2的数据库
			DbContextHolder.setDbType("dataSource2");
			
			Map<String, Object> UserMap1 = new HashMap<String, Object>();
			UserMap1 = executeQuerySingle("select name, f_parentname from t_user where name = '安刚'");
			String Name1 = UserMap1.get("name") == null ? "" : UserMap1.get("name").toString();
			String ParentName1 = UserMap1.get("f_parentname") == null ? "0" : UserMap1.get("f_parentname").toString();
			System.out.println("dataSource2 : Name : " + Name1 + ", ParentName : " + ParentName1);
			
			DbContextHolder.clearDbType();
			
			return "ok";
		} catch (Exception e) {
			e.printStackTrace();
			return "not ok";
		}
	}
	
	public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String connectDB="jdbc:sqlserver://127.0.0.1:1433;DatabaseName=alashangas";
            String user="sa";
        	String password="000";
        	Connection conn=DriverManager.getConnection(connectDB,user,password);
            return conn;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
	
	public static Map<String, Object> executeQuerySingle(String sql) {
        try {
			List<Map<String, Object>> list = executeQuery(sql);
			return list.get(0);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
    }
	
	public static List<Map<String, Object>> executeQuery(String sql) {
        try {
            Connection conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet set = st.executeQuery(sql);
            ResultSetMetaData metaData = set.getMetaData();
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            int columnCount = metaData.getColumnCount();
            while (set.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    String name = metaData.getColumnName(i);
                    Object value = set.getObject(name);
                    map.put(name, value);
                }
                result.add(map);
            }
            set.close();
            st.close();
            conn.close();
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
	
}
