package org.nosql.blog.dao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.nosql.blog.model.Comment;
import org.nosql.blog.model.Post;
import org.nosql.blog.model.User;

public interface BlogDao {

	
    public static final int MAX_SORT_DAYS = 30;

	/**
     * Save User record.
     *
     * @param user user record to save
     * @return User record after saved
     */
    public User saveUser( User user );

    /**
     * Save Post record.
     * <ul>
     *     <li>Save Post</li>
     *     <li>Save User/Post connection</li>
     *     <li>Initialize votes to zero for sorting</li>
     * </ul>
     *
     * @param post Post record to save
     * @return Post record after saved
     */
    public Post savePost( Post post );

    /**
     * Save Comment.
     *
     * <ul>
     *     <li>Save Comment</li>
     *     <li>Save User/Comment connection</li>
     *     <li>Save Post/Comment connection</li>
     *     <li>Initialize votes to zero for sorting</li>
     *     <li>Save flag indicating Post's comments need sorting</li>
     * </ul>
     *
     * @param comment Comment record to save
     * @return Comment record after saved
     */
    public Comment saveComment( Comment comment );

    /**
     * Find User by user email.
     *
     * @param email user's email
     * @return User record associated with email if found, null otherwise
     */
    public User findUser( String email );

    /**
     * Find Post by Post ID.
     *
     * @param postId UUID of Post to find
     * @return Post record if found, null otherwise
     */
    public Post findPost( UUID postId );

    /**
     * Find Comment by ID.  Also does a lookup to get the Comment's vote count.
     *
     * @param uuid UUID of Comment to find
     * @return Comment record if found, null otherwise
     */
    public Comment findComment(UUID uuid);

    /**
     * Find all Post UUIDs for the given User using the User/Post connection.
     *
     * @param userEmail user's email
     * @return list of Post IDs
     */
    public List<UUID> findPostUUIDsByUser( String userEmail );

    /**
     * Find all Posts for the given User.
     *
     * @param userEmail user's email
     * @return list of Post records
     */
    public List<Post> findPostsByUser( String userEmail );

    /**
     * Find Post UUIDs by time range (GMT).
     *
     * @param start Start time in GMT
     * @param end End time in GMT
     * @return list of Post IDs
     */
    public List<UUID> findPostUUIDsByTimeRange( DateTime start, DateTime end );

    /**
     * Find Posts by time range (GMT).
     *
     * @param start Start time in GMT
     * @param end End time in GMT
     * @return list of Post records
     */
    public List<Post> findPostsByTimeRange(DateTime start, DateTime end);

    /**
     * Find all Comment UUIDs for a given user.
     *
     * @param userEmail user's email
     * @return list of Comment IDs
     */
    public List<UUID> findCommentUUIDsByUser( String userEmail );

    /**
     * Find a post's comment UUIDs sorted by time.  Uses the ColumnFamily, post_comments, as an index.
     *
     * @param postId post ID
     * @return list of Comment IDs
     */
    public List<UUID> findCommentUUIDsByPostSortedByTime(UUID postId);

    /**
     * Find a post's comment UUIDs sorted by vote.  Uses the ColumnFamily, post_comments_sorted_by_vote, as an index.
     *
     * @param postId Post ID
     * @return list of Comment IDs
     */
    public List<UUID> findCommentUUIDsByPostSortedByVotes(UUID postId);

    /**
     * Find Comments given a list of Comment IDs.  It will also do a lookup to get the votes count for each Comment.
     *
     * @param uuidList Find Comment records given the list of Comment IDs
     * @return list of Comment records
     */
    public List<Comment> findCommentsByUUIDList( List<UUID> uuidList );

    /**
     * Find all Comments for the given user email.
     *
     * @param userEmail user's email
     * @return list of Comment records
     */
    public List<Comment> findCommentsByUser( String userEmail );

    /**
     * Vote on a Post.
     *
     * @param userEmail User's email
     * @param postId Post ID
     */
    public void voteOnPost( String userEmail, UUID postId );

    /**
     * Vote on a comment and signal that the Post needs its "comments sorted by vote" index updated.
     *
     * @param userEmail user's email
     * @param commentId Comment ID
     */
    public void voteOnComment( String userEmail, UUID commentId );

    /**
     * Find the vote counts for the list of UUIDs.  Since UUIDs are unique it doesn't matter if the UUID
     * is for a Post or a Comment.
     *
     * @param uuidList list of Comment or Post IDs
     * @return Comment/Post ID mapping to number of votes
     */
    public Map<UUID, Long> findVotes( List<UUID> uuidList );

    /**
     * Find the timestamp, if any, when a User voted on a Post or Comment.
     *
     * @param userEmail User's email
     * @param uuid Post/Comment ID
     * @return Timestamp of when the user voted if found, null otherwise
     */
    public DateTime findUserVote(String userEmail, UUID uuid);

    /**
     * Find 'number' of Posts ordered by their votes.  Uses the ColumnFamily, posts_sorted_by_vote, as an
     * index to speed up search.
     *
     * @param number Number of Posts to return
     * @return List of Post records
     */
    public List<Post> findPostsByVote(int number);
    
    public Post generateNewPost(String userEmail, String userName, String title, DateTime createdAt, String text);
    
    public Comment generateNewComment(String userEmail, String userName,
            						  UUID postId, long createdTimestamp, String text);
    
    public User generateNewUser(String email, String password, String fullName);

	
}
