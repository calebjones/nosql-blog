package org.nosql.blog.mongo;

import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.nosql.blog.model.Post;

public class MongoPost implements Post {
	
	private UUID id;
	
	private String title;
	
	private String userEmail;
	
	private String userDisplayName;
	
	private String text;
	
	private Long votes;
	
	private long createTimestamp;
	
	private Set<MongoVote> voters;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public DateTime getCreateTimestamp() {
		return new DateTime(createTimestamp);
	}

	@Override
	public void setCreateTimestamp(DateTime createTimestamp) {
		this.createTimestamp = createTimestamp.getMillis();
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getUserEmail() {
		return userEmail;
	}

	@Override
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	@Override
	public String getUserDisplayName() {
		return userDisplayName;
	}

	@Override
	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public Long getVotes() {
		return votes;
	}

	@Override
	public void setVotes(Long votes) {
		this.votes = votes;
	}
	
	public Set<MongoVote> getVoters() {
		return voters;
	}
	
	public void setVoters(Set<MongoVote> voters) {
		this.voters = voters;
	}

}
