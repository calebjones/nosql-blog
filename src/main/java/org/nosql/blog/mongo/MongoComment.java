package org.nosql.blog.mongo;

import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.nosql.blog.model.Comment;

public class MongoComment implements Comment {
	
	private UUID id;
	
	private UUID postId;
	
	private String text;
	
	private String userEmail;
	
	private String userDisplayName;
	
	private Long votes;
	
	private long createTimestamp;
	
	private Set<MongoVote> voters;
	
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getPostId() {
		return postId;
	}

	public void setPostId(UUID postId) {
		this.postId = postId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	public Long getVotes() {
		return votes;
	}

	public void setVotes(Long votes) {
		this.votes = votes;
	}

	public long getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	public Set<MongoVote> getVoters() {
		return voters;
	}

	public void setVoters(Set<MongoVote> voters) {
		this.voters = voters;
	}

}
