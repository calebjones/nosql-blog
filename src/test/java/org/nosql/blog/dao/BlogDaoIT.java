package org.nosql.blog.dao;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nosql.blog.model.Comment;
import org.nosql.blog.model.Post;
import org.nosql.blog.model.User;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BlogDaoIT {
	
	protected static final String EMAIL = "test@test.com";
	protected static final String NAME = "Test E. Testington";
	
	@Autowired
	private BlogDao dao;
	
	/**
	 * Called before each test.
	 */
	@Before
	public abstract void before();
	
	/**
	 * Called after each test. <strong>MUST</strong> clear 
	 * all data from the test.
	 */
	@After
	public abstract void after();
	
	@Test
	public void testSaveUser() {
		User user = dao.generateNewUser(EMAIL, "asdf", 
										"Test E. Testington");
		User savedUser = dao.saveUser(user);
		User returnedUser = dao.findUser(user.getEmail());
		assertThat(savedUser, equalTo(user));
		assertThat(returnedUser, equalTo(user));
	}

	@Test
	public void testSavePost() {
		Post post = dao.generateNewPost(EMAIL, NAME, "My Post", new DateTime(), 
						"This is the test post body.");
		Post savedPost = dao.savePost(post);
		Post returnedPost = dao.findPost(savedPost.getId());
		assertThat(returnedPost.getVotes(), equalTo(0L));
		assertThat(returnedPost, equalTo(savedPost));
	}
	
	@Test
	public void testSaveComment() {
		Post post = dao.generateNewPost(EMAIL, NAME, "Test Post", new DateTime(), "Post body.");
		dao.savePost(post);
		Comment comment = dao.generateNewComment(EMAIL, NAME, post.getId(), 
				System.currentTimeMillis(), "Post comment.");
		dao.saveComment(comment);
		assertThat(comment.getVotes(), equalTo(0L));
		Comment returnedComment = dao.findComment(comment.getId());
		assertThat(returnedComment, equalTo(comment));
	}
	
	@Test
	public void testFindUserNull() {
		assertThat(dao.findUser("bademail"), nullValue());
	}
	
	@Test
	public void testFindPostNull() {
		assertThat(dao.findPost(UUID.randomUUID()), nullValue());
	}
	
	@Test
	public void testFindCommentNull() {
		assertThat(dao.findComment(UUID.randomUUID()), nullValue());
	}
	
	@Test
	public void testFindPostUUIDsAndPostsByUser() {
		User user = dao.saveUser(dao.generateNewUser(EMAIL, "asdf", NAME));
		List<Post> posts = new ArrayList<Post>();
		List<UUID> postIds = new ArrayList<UUID>();
		for (int i = 0; i < 5; i++) {
			Post post = dao.savePost(dao.generateNewPost(user.getEmail(), user.getName(), 
					"Post " + (i + 1), new DateTime(), "Post body for post " + (i + 1)));
			posts.add(post);
			postIds.add(post.getId());
		}
		
		List<Post> foundPosts = dao.findPostsByUser(user.getEmail());
		assertThat(foundPosts, equalTo(posts));
		List<UUID> postUUIDs = dao.findPostUUIDsByUser(user.getEmail());
		assertThat(postUUIDs, equalTo(postIds));
	}
	
	@Test
	public void testFindPostUUIDsAndPostsByUserEmpty() {
		User user = dao.saveUser(dao.generateNewUser(EMAIL, "asdf", NAME));
		List<Post> posts = new ArrayList<Post>();
		List<UUID> postIds = new ArrayList<UUID>();
		for (int i = 0; i < 5; i++) {
			Post post = dao.savePost(dao.generateNewPost(user.getEmail(), user.getName(), 
					"Post " + (i + 1), new DateTime(), "Post body for post " + (i + 1)));
			posts.add(post);
			postIds.add(post.getId());
		}
		
		List<Post> foundPosts = dao.findPostsByUser(user.getEmail());
		assertThat(foundPosts, equalTo(posts));
		List<UUID> postUUIDs = dao.findPostUUIDsByUser(user.getEmail());
		assertThat(postUUIDs, equalTo(postIds));
	}
	
	@Test
	public void testFindPostUUIDsAndPostsByTimeRange() {
		int startYear = 2005;
		int endYear = 2015;
		
		User user = dao.saveUser(dao.generateNewUser(EMAIL, "asdf", NAME));
		List<Post> postsInRange = new ArrayList<Post>();
		List<UUID> postIdsInRange = new ArrayList<UUID>();
		for (int i = 0; i < 20; i++) {
			int year = 2000 + i;
			Post post = dao.savePost(dao.generateNewPost(user.getEmail(), user.getName(), 
					"Post " + (i + 1), new DateTime(year, 1, 1, 0, 0), "Post body for post " + (i + 1)));
			if (year >= startYear && year <= endYear) {
				postsInRange.add(post);
				postIdsInRange.add(post.getId());
			}
		}
		
		List<Post> foundPosts = 
				dao.findPostsByTimeRange(new DateTime(startYear, 1, 1, 0, 0), 
										 new DateTime(endYear + 1, 1, 1, 0, 0));
		assertThat(foundPosts, equalTo(postsInRange));
		List<UUID> postUUIDs = 
				dao.findPostUUIDsByTimeRange(new DateTime(startYear, 1, 1, 0, 0), 
											 new DateTime(endYear + 1, 1, 1, 0, 0));
		assertThat(postUUIDs, equalTo(postIdsInRange));
	}
	
	
	/*
	 

    **
     * Find all Comment UUIDs for a given user.
     *
     * @param userEmail user's email
     * @return list of Comment IDs
     *
    public List<UUID> findCommentUUIDsByUser( String userEmail );

    **
     * Find a post's comment UUIDs sorted by time.  Uses the ColumnFamily, post_comments, as an index.
     *
     * @param postId post ID
     * @return list of Comment IDs
     *
    public List<UUID> findCommentUUIDsByPostSortedByTime(UUID postId);

    **
     * Find a post's comment UUIDs sorted by vote.  Uses the ColumnFamily, post_comments_sorted_by_vote, as an index.
     *
     * @param postId Post ID
     * @return list of Comment IDs
     *
    public List<UUID> findCommentUUIDsByPostSortedByVotes(UUID postId);

    **
     * Find Comments given a list of Comment IDs.  It will also do a lookup to get the votes count for each Comment.
     *
     * @param uuidList Find Comment records given the list of Comment IDs
     * @return list of Comment records
     *
    public List<Comment> findCommentsByUUIDList( List<UUID> uuidList );

    **
     * Find all Comments for the given user email.
     *
     * @param userEmail user's email
     * @return list of Comment records
     *
    public List<Comment> findCommentsByUser( String userEmail );

    **
     * Vote on a Post.
     *
     * @param userEmail User's email
     * @param postId Post ID
     *
    public void voteOnPost( String userEmail, UUID postId );

    **
     * Vote on a comment and signal that the Post needs its "comments sorted by vote" index updated.
     *
     * @param userEmail user's email
     * @param commentId Comment ID
     *
    public void voteOnComment( String userEmail, UUID commentId );

    **
     * Find the vote counts for the list of UUIDs.  Since UUIDs are unique it doesn't matter if the UUID
     * is for a Post or a Comment.
     *
     * @param uuidList list of Comment or Post IDs
     * @return Comment/Post ID mapping to number of votes
     *
    public Map<UUID, Long> findVotes( List<UUID> uuidList );

    **
     * Find the timestamp, if any, when a User voted on a Post or Comment.
     *
     * @param userEmail User's email
     * @param uuid Post/Comment ID
     * @return Timestamp of when the user voted if found, null otherwise
     *
    public DateTime findUserVote(String userEmail, UUID uuid);

    **
     * Find 'number' of Posts ordered by their votes.  Uses the ColumnFamily, posts_sorted_by_vote, as an
     * index to speed up search.
     *
     * @param number Number of Posts to return
     * @return List of Post records
     *
    public List<Post> findPostsByVote(int number);
    
	 */
}
