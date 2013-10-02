package org.nosql.blog.mongo;

public class MongoVote {
	
	private String userEmail;
	
	private Long timestamp;
	
	public MongoVote() {
		
	}
	
	public MongoVote(String userEmail, Long timestamp) {
		this.userEmail = userEmail;
		this.timestamp = timestamp;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
