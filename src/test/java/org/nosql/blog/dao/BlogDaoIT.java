package org.nosql.blog.dao;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
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
	protected BlogDao dao;
	
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
	
	@Test
	public void testFindPostUUIDsAndPostsByTimeRangeEmpty() {
		List<Post> foundPosts = dao.findPostsByTimeRange(new DateTime(3000, 1, 1, 0, 0),
											new DateTime(4000, 1, 1, 0, 0));
		assertThat(foundPosts, hasSize(0));
		List<UUID> foundUUIDs = dao.findPostUUIDsByTimeRange(new DateTime(3000, 1, 1, 0, 0),
				new DateTime(4000, 1, 1, 0, 0));
		assertThat(foundUUIDs, hasSize(0));
	}
	
	@Test
	public void testFindCommentUUIDsByUser() {
		List<Post> posts = generatePosts(5);
		List<UUID> commentUUIDs = new ArrayList<UUID>();
		
		for (int i = 0; i < posts.size(); i++) {
			Comment comment = dao.generateNewComment(EMAIL, NAME, posts.get(i).getId(), 
					System.currentTimeMillis(), "This is comment #" + (i+1));
			Comment savedComment = dao.saveComment(comment);
			commentUUIDs.add(savedComment.getId());
		}
		
		List<UUID> returnedCommentUUIDs = dao.findCommentUUIDsByUser(EMAIL);
		assertThat(returnedCommentUUIDs, 
				IsIterableContainingInAnyOrder.containsInAnyOrder(commentUUIDs.toArray()));
	}
	
	@Test
	public void testFindCommentUUIDsByUserEmpty() {
		assertThat(dao.findCommentUUIDsByUser(EMAIL), hasSize(0));
	}
	
	@Test
	public void testFindCommentUUIDsByPostSortedByTime() throws InterruptedException {
		int numComments = 5;
		Post post = dao.generateNewPost(EMAIL, NAME, "Post Title", 
				new DateTime(), "This is a post.");
		dao.savePost(post);
		
		List<UUID> commentUUIDs = new ArrayList<UUID>();
		for (int i = 0; i < numComments; i++) {
			Comment comment = dao.generateNewComment(EMAIL, NAME, post.getId(), 
					System.currentTimeMillis(), "This is comment #" + (i+1));
			Comment savedComment = dao.saveComment(comment);
			commentUUIDs.add(savedComment.getId());
			Thread.sleep(10L);
		}
		
		List<UUID> returnedCommentUUIDs = dao.findCommentUUIDsByPostSortedByTime(post.getId());
		assertThat(returnedCommentUUIDs, 
				IsIterableContainingInOrder.contains(commentUUIDs.toArray()));
		
		long prevCreatedTimestamp = 0L;
		for (UUID uuid : returnedCommentUUIDs) {
			Comment comment = dao.findComment(uuid);
			assertThat(comment.getCreateTimestamp(), greaterThan(prevCreatedTimestamp));
			prevCreatedTimestamp = comment.getCreateTimestamp();
		}
	}

	@Test
	public void testFindCommentUUIDsByPostSortedByTimeEmpty() {
		assertThat(dao.findCommentUUIDsByPostSortedByTime(UUID.randomUUID()), hasSize(0));
	}

	@Test
	public void testFindCommentsByUser() {
		List<Post> posts = generatePosts(5);
		List<Comment> comments = new ArrayList<Comment>();
		
		for (int i = 0; i < posts.size(); i++) {
			Comment comment = dao.generateNewComment(EMAIL, NAME, posts.get(i).getId(), 
					System.currentTimeMillis(), "This is comment #" + (i+1));
			Comment savedComment = dao.saveComment(comment);
			comments.add(savedComment);
		}
		
		List<Comment> returnedComments = dao.findCommentsByUser(EMAIL);
		assertThat(returnedComments, 
				IsIterableContainingInAnyOrder.containsInAnyOrder(comments.toArray()));
	}
	
	@Test
	public void testFindCommentsByUserEmpty() {
		assertThat(dao.findCommentsByUser(EMAIL), hasSize(0));
	}


	@Test
	public void testFindCommentsByUUIDList() {
		List<Post> posts = generatePosts(5);
		List<UUID> commentUUIDs = new ArrayList<UUID>();
		List<Comment> comments = new ArrayList<Comment>();
		
		for (int i = 0; i < posts.size(); i++) {
			Comment comment = dao.generateNewComment(EMAIL, NAME, posts.get(i).getId(), 
					System.currentTimeMillis(), "This is comment #" + (i+1));
			Comment savedComment = dao.saveComment(comment);
			commentUUIDs.add(savedComment.getId());
			comments.add(savedComment);
		}
		
		List<Comment> returnedComments = dao.findCommentsByUUIDList(commentUUIDs);
		assertThat(returnedComments, 
				IsIterableContainingInAnyOrder.containsInAnyOrder(comments.toArray()));
	}
	
	@Test
	public void testFindCommentsByUUIDListEmptyParams() {
		assertThat(dao.findCommentsByUUIDList(new ArrayList<UUID>()), hasSize(0));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testVoteOnPost() {
		final Post post = generatePosts(1).get(0);
		
		Post returnedPost = dao.findPost(post.getId());
		assertThat(returnedPost.getVotes(), equalTo(0L));
		Map<UUID, Long> votes = dao.findVotes(new ArrayList<UUID>() {{add(post.getId());}});
		assertThat(votes.size(), equalTo(1));
		assertThat(votes.get(post.getId()), equalTo(post.getVotes()));
		
		dao.voteOnPost(EMAIL, returnedPost.getId());
		Post votedPost = dao.findPost(post.getId());
		assertThat(votedPost.getVotes(), equalTo(1L));
		votes = dao.findVotes(new ArrayList<UUID>() {{add(post.getId());}});
		assertThat(votes.size(), equalTo(1));
		assertThat(votes.get(post.getId()), equalTo(1L));
		
		dao.voteOnPost(EMAIL, returnedPost.getId());
		Post secondVotedPost = dao.findPost(post.getId());
		assertThat(secondVotedPost.getVotes(), equalTo(1L));
		Map<UUID, Long> secondVotes = dao.findVotes(new ArrayList<UUID>() {{add(post.getId());}});
		assertThat(secondVotes.size(), equalTo(1));
		assertThat(secondVotes.get(post.getId()), equalTo(1L));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testVoteOnPostNotExist() {
		final UUID uuid = UUID.randomUUID();
		dao.voteOnPost(EMAIL, uuid);
		Map<UUID, Long> votes = dao.findVotes(new ArrayList<UUID>() {{add(uuid);}});
		assertThat(votes.size(), equalTo(0));
	}

	
	@SuppressWarnings("serial")
	@Test
	public void testVoteOnComment() {
		final Post post = generatePosts(1).get(0);
		
		final Comment comment = dao.generateNewComment(EMAIL, NAME, post.getId(), 
				System.currentTimeMillis(), "This is a comment.");
		dao.saveComment(comment);
		
		Comment returnedComment = dao.findComment(comment.getId());
		assertThat(returnedComment.getVotes(), equalTo(0L));
		
		dao.voteOnComment(EMAIL, comment.getId());
		Comment votedComment = dao.findComment(comment.getId());
		assertThat(votedComment.getVotes(), equalTo(1L));
		Map<UUID, Long> votes = dao.findVotes(new ArrayList<UUID>() {{add(comment.getId());}});
		assertThat(votes.size(), equalTo(1));
		assertThat(votes.get(comment.getId()), equalTo(1L));
		
		dao.voteOnComment(EMAIL, comment.getId());
		Comment secondVotedComment = dao.findComment(comment.getId());
		assertThat(secondVotedComment.getVotes(), equalTo(1L));
		Map<UUID, Long> secondVotes = dao.findVotes(new ArrayList<UUID>() {{add(comment.getId());}});
		assertThat(secondVotes.size(), equalTo(1));
		assertThat(secondVotes.get(comment.getId()), equalTo(1L));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testVoteOnCommentNotExist() {
		final UUID uuid = UUID.randomUUID();
		dao.voteOnComment(EMAIL, uuid);
		Map<UUID, Long> votes = dao.findVotes(new ArrayList<UUID>() {{add(uuid);}});
		assertThat(votes.size(), equalTo(0));
	}
	
	@Test
	public void testFindUserVote() {
		DateTime start = new DateTime();
		
		Post post = generatePosts(1).get(0);
		
		Comment comment = dao.generateNewComment(EMAIL, NAME, post.getId(), 
				System.currentTimeMillis(), "This is a comment.");
		dao.saveComment(comment);
		
		DateTime time = dao.findUserVote(EMAIL, post.getId());
		assertThat(time, nullValue());
		time = dao.findUserVote(EMAIL, comment.getId());
		assertThat(time, nullValue());
		
		dao.voteOnPost(EMAIL, post.getId());
		time = dao.findUserVote(EMAIL, post.getId());
		assertThat(time, notNullValue());
		assertTrue("vote timestamp invalid", time.isAfter(start));
		
		dao.voteOnComment(EMAIL, comment.getId());
		time = dao.findUserVote(EMAIL, comment.getId());
		assertThat(time, notNullValue());
		assertTrue("vote timestamp invalid", time.isAfter(start));
	}
	
	@Test
	public void testFindUserVoteUUIDNotExist() {
		DateTime time = dao.findUserVote(EMAIL, UUID.randomUUID());
		assertThat(time, nullValue());
	}
	
	@Test
	public void testFindCommentUUIDsByPostSortedByVotes() {
		int numComments = 5;
		List<UUID> createdCommentUUIDs = new ArrayList<UUID>(numComments);
		
		Post post = generatePosts(1).get(0);
		for (int i = 0; i < numComments; i++) {
			Comment comment = dao.generateNewComment(EMAIL, NAME, post.getId(), 
					System.currentTimeMillis(), "Comment #" + (i+1));
			dao.saveComment(comment);
			createdCommentUUIDs.add(comment.getId());
			
			for (int j = i; j < numComments; j++) {
				String email = "user" + j + "@test.com";
				if (null == dao.findUser(email)) {
					dao.saveUser(dao.generateNewUser(email, "asdf", "Test User" + (j+1)));
				}
				dao.voteOnComment(email, comment.getId());
			}
		}
		
		List<UUID> commentUUIDs = dao.findCommentUUIDsByPostSortedByVotes(post.getId());
		assertThat(commentUUIDs, 
				IsIterableContainingInAnyOrder.containsInAnyOrder(createdCommentUUIDs.toArray()));
		Long prevVotes = numComments * 2L;
		for (UUID uuid : commentUUIDs) {
			Comment curComment = dao.findComment(uuid);
			assertThat(curComment.getVotes(), lessThan(prevVotes));
			prevVotes = curComment.getVotes();
		}
	}
	
	@Test
	public void testFindCommentUUIDsByPostSortedByVotesPostNotExist() {
		List<UUID> uuids = dao.findCommentUUIDsByPostSortedByVotes(UUID.randomUUID());
		assertThat(uuids.size(), equalTo(0));
	}
	
	@Test
	public void testFindCommentUUIDsByPostSortedByVotesPostNoComments() {
		Post post = generatePosts(1).get(0);
		List<UUID> uuids = dao.findCommentUUIDsByPostSortedByVotes(post.getId());
		assertThat(uuids.size(), equalTo(0));
	}
	
	@Test
	public void testFindPostsByVote() {
		int numPosts = 5;
		List<Post> createdPosts = new ArrayList<Post>(numPosts);
		
		for (int i = 0; i < numPosts; i++) {
			Post post = generatePosts(1).get(0);
			dao.savePost(post);
			createdPosts.add(post);
			
			for (int j = i; j < numPosts; j++) {
				String email = "user" + j + "@test.com";
				if (null == dao.findUser(email)) {
					dao.saveUser(dao.generateNewUser(email, "asdf", "Test User" + (j+1)));
				}
				dao.voteOnPost(email, post.getId());
			}
		}
		
		// extra posts that shouldn't be loaded from findPostsByVote() call
		generatePosts(5);
		
		// reload posts since they've now been voted on
		List<Post> votedPosts = new ArrayList<Post>(numPosts);
		for (Post curPost : createdPosts) {
			votedPosts.add(dao.findPost(curPost.getId()));
		}
		
		List<Post> postsByVote = dao.findPostsByVote(numPosts);
		assertThat(postsByVote, 
				IsIterableContainingInAnyOrder.containsInAnyOrder(votedPosts.toArray()));
		Long prevVotes = numPosts * 2L;
		for (Post curPost : postsByVote) {
			assertThat(curPost.getVotes(), lessThan(prevVotes));
			prevVotes = curPost.getVotes();
		}
	}
	
	@Test
	public void testFindPostsByVotePostNotExist() {
		List<Post> posts = dao.findPostsByVote(10);
		assertThat(posts.size(), equalTo(0));
	}
	
	protected List<Post> generatePosts(int num) {
		List<Post> posts = new ArrayList<Post>(num);
		for (int i = 0; i < num; i++) {
			Post post = dao.generateNewPost(EMAIL, NAME, "Post " + (i+1), 
					new DateTime(), "This is post #" + (i+1));
			dao.savePost(post);
			posts.add(post);
		}
		return posts;
	}
}
