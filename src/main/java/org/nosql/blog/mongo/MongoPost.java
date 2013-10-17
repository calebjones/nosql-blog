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
	
	private Set<String> voters;

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
	
	public Set<String> getVoters() {
		return voters;
	}
	
	public void setVoters(Set<String> voters) {
		this.voters = voters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (createTimestamp ^ (createTimestamp >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result
				+ ((userDisplayName == null) ? 0 : userDisplayName.hashCode());
		result = prime * result
				+ ((userEmail == null) ? 0 : userEmail.hashCode());
		result = prime * result + ((voters == null) ? 0 : voters.hashCode());
		result = prime * result + ((votes == null) ? 0 : votes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MongoPost other = (MongoPost) obj;
		if (createTimestamp != other.createTimestamp)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (userDisplayName == null) {
			if (other.userDisplayName != null)
				return false;
		} else if (!userDisplayName.equals(other.userDisplayName))
			return false;
		if (userEmail == null) {
			if (other.userEmail != null)
				return false;
		} else if (!userEmail.equals(other.userEmail))
			return false;
		if (voters == null) {
			if (other.voters != null)
				return false;
		} else if (!voters.equals(other.voters))
			return false;
		if (votes == null) {
			if (other.votes != null)
				return false;
		} else if (!votes.equals(other.votes))
			return false;
		return true;
	}

}
