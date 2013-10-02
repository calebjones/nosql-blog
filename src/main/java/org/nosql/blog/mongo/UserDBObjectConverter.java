package org.nosql.blog.mongo;

import org.nosql.blog.model.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UserDBObjectConverter {
	
	public static final String NAME_KEY = "name";
	
	public static final String EMAIL_KEY = "email";
	
	public static final String PASSWORD_KEY = "password";
	
	public static User convertToUser(DBObject obj) {
		User user = new MongoUser(
				(String) obj.get(NAME_KEY), 
				(String) obj.get(EMAIL_KEY), 
				(String) obj.get(PASSWORD_KEY));
		
		return user;
	}
	
	public static DBObject convertToDBObject(User user) {
		DBObject obj = new BasicDBObject();
		obj.put(NAME_KEY, user.getName());
		obj.put(EMAIL_KEY, user.getEmail());
		obj.put(PASSWORD_KEY, user.getPassword());
		
		return obj;
	}

}
