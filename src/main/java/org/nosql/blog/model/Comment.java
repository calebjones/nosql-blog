package org.nosql.blog.model;

import java.util.UUID;

import org.nosql.blog.mongo.MongoUser;

public interface Comment {
	
    public UUID getId();
    public void setId(UUID id);

    public UUID getPostId();
    public void setPostId(UUID postId);

    public long getCreateTimestamp();
    public void setCreateTimestamp(long createTimestamp);

    public String getText();
    public void setText(String text);

    public String getUserEmail();
    public void setUserEmail(String userEmail);

    public String getUserDisplayName();
    public void setUserDisplayName(String userDisplayName);

    public Long getVotes();
    public void setVotes(Long votes);
    

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
    
}
