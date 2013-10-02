package org.nosql.blog.mongo;

public class MongoBlogConfig {
	
	private String dbName;
	
	private String postCollection;
	
	private String commentCollection;
	
	private String userCollection;
	
	private String votesCollection;
	
	public MongoBlogConfig(String dbName, String postCollection, 
			String commentCollection, String userCollection, String votesCollection) {
		this.dbName = dbName;
		this.postCollection = postCollection;
		this.commentCollection = commentCollection;
		this.userCollection = userCollection;
		this.votesCollection = votesCollection;
	}

	public String getPostCollection() {
		return postCollection;
	}

	public String getCommentCollection() {
		return commentCollection;
	}

	public String getUserCollection() {
		return userCollection;
	}
	
	public String getVotesCollection() {
		return votesCollection;
	}
	
	public String getDbName() {
		return dbName;
	}

}
