package org.nosql.blog.model;

public interface User {
	
    public String getEmail();
    public void setEmail(String email);

    public String getPassword();
    public void setPassword(String password);

    public String getName();
    public void setName(String name);
    
}
