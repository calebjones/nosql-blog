package org.nosql.blog.service;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.nosql.blog.model.Comment;
import org.nosql.blog.model.Post;
import org.nosql.blog.model.User;

public interface BlogService {
	

    /**
     * Create a new Comment.  Reads the User record before saving to get the user's name.
     *
     * @param userEmail User's email
     * @param postId Post ID
     * @param commentText Complete comment text
     * @return Comment record just saved
     */
    public Comment createComment(String userEmail, UUID postId, String commentText);

    /**
     * Create a new User.
     *
     * @param email User's email
     * @param password User's password
     * @param fullName User's full name
     * @return User record just saved
     */
    public User createUser(String email, String password, String fullName);

    /**
     * Create a new Post.  Reads the User record before saving to get the user's name.
     *
     * @param userEmail User's email
     * @param title Post Title
     * @param text complete Post text
     * @return Post record just created
     */
    public Post createPost(String userEmail, String title, String text);

    /**
     * Retrieve Posts for the given time range.
     *
     * @param start Start time in GMT
     * @param end End time in GMT
     * @return List of Post records
     */
    public List<Post> findPostsByTimeRange(DateTime start, DateTime end);

    /**
     * Find recent Posts over the last 'minutes'
     *
     * @param minutes number of minutes
     * @return 
     */
    public List<Post> findRecentPosts(int minutes);

    /**
     * Find the top 'number' of Posts, sorted by vote.
     *
     * @param number Number of Posts to retrieve
     * @return list of Post records
     */
    public List<Post> findTopPosts(int number);

    /**
     * Vote on a Post.  A User can only vote once per Comment or Post.
     *
     * @param userEmail User's email
     * @param uuid Post ID
     */
    public void voteOnPost(String userEmail, UUID uuid);

    /**
     * Vote on a Comment.  A User can only vote once per Comment or Post.
     *
     * @param userEmail User's email
     * @param uuid Comment ID
     */
    public void voteOnComment(String userEmail, UUID uuid);

    /**
     * Retrieve User record by User Email.
     *
     * @param userEmail User's email
     * @return User record
     */
    public User findUser(String userEmail);

    /**
     * Retrieve Comments by User Email.
     *
     * @param userEmail User's email
     * @return List of Comment records
     */
    public List<Comment> findCommentsByUser(String userEmail);

    /**
     * Retreive Post by ID.
     *
     * @param postId Post ID
     * @return Post record
     */
    public Post findPost(UUID postId);

    /**
     * Retrieve Comment by ID.
     *
     * @param commentId Comment ID
     * @return Comment record
     */
    public Comment findComment(UUID commentId);

}
