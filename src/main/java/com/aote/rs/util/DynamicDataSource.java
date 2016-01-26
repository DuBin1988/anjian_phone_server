package com.aote.rs.util;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource{

	static Logger log=Logger.getLogger("DynamicDataSource");

	protected Object determineCurrentLookupKey() {
		return DbContextHolder.getDbType();
	}
	
	static class DbContextHolder {
		private static final ThreadLocal contextHolder = new ThreadLocal();
		
		public static void setDbType(String dbType){
			contextHolder.set(dbType);
		}
		
		public static String getDbType(){
			return (String)contextHolder.get();
		}
		
		public static void clearDbType(){
			contextHolder.remove();
		}

	}
}