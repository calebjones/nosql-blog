package org.nosql.blog.mongo;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Id;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.nosql.blog.model.Comment;


public class MongoComment implements Comment {
	
	protected class UUIDToStringSerializer extends JsonSerializer<UUID> {
		@Override
		public void serialize(UUID value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeObject(value.toString());
		}
	}
	
	protected class StringToUUIDDeserializer extends JsonDeserializer<UUID> {
		@Override
		public UUID deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			return UUID.fromString(jp.getText());
		}
	}
	
	@Id
	@JsonSerialize(using = UUIDToStringSerializer.class)
	@JsonDeserialize(using = StringToUUIDDeserializer.class)
	private UUID id;
	
	private UUID postId;
	
	private String text;
	
	private String userEmail;
	
	private String userDisplayName;
	
	private Long votes;
	
	private long createTimestamp;
	
	private Set<String> voters;
	
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
		result = prime * result + ((postId == null) ? 0 : postId.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		MongoComment other = (MongoComment) obj;
		if (createTimestamp != other.createTimestamp)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (postId == null) {
			if (other.postId != null)
				return false;
		} else if (!postId.equals(other.postId))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
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
