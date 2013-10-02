package org.nosql.blog.model;

import java.util.UUID;

import org.joda.time.DateTime;

public interface Post {

    public UUID getId();
    public void setId(UUID id);

    public DateTime getCreateTimestamp();
    public void setCreateTimestamp(DateTime createTimestamp);

    public String getText();
    public void setText(String text);

    public String getUserEmail();
    public void setUserEmail(String userEmail);

    public String getUserDisplayName();
    public void setUserDisplayName(String userDisplayName);

    public String getTitle();
    public void setTitle(String title);

    public Long getVotes();
    public void setVotes(Long votes);
    
}
