package org.nosql.blog.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.mongojack.JacksonDBCollection;
import org.nosql.blog.dao.BlogDao;
import org.nosql.blog.model.Comment;
import org.nosql.blog.model.Post;
import org.nosql.blog.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

public class BlogDaoMongoImpl implements BlogDao {
	
	private static enum VoteType {
		POST, COMMENT;
	}
	
	private Mongo mongo;
	
	private MongoBlogConfig config;
	
	private MongoOperations mongoOps;
	
	public BlogDaoMongoImpl(Mongo mongo, MongoOperations mongoOps, MongoBlogConfig config) {
		this.mongo = mongo;
		this.mongoOps = mongoOps;
		this.config = config;
	}

	@Override
	public User saveUser(User user) {
		DBCollection coll = getUserCollection();
		WriteResult result = 
				coll.save(UserDBObjectConverter.convertToDBObject(user));
		
		if (result.getN() != 1) {
			throw new RuntimeException("no user saved");
		}
		
		return user;
	}

	@Override
	public Post savePost(Post post) {
		post.setVotes(0L);
		mongoOps.save(post, config.getPostCollection());
		return post;
	}

	@Override
	public Comment saveComment(Comment comment) {
		comment.setVotes(0L);
		JacksonDBCollection<MongoComment, UUID> collection = 
				JacksonDBCollection.wrap(getCommentCollection(), MongoComment.class, UUID.class);
		org.mongojack.WriteResult<MongoComment, UUID> result = collection.save((MongoComment)comment);
		
		if (result.getN() != 1) {
			throw new RuntimeException("no comment saved");
		}
		
		return comment;
	}

	@Override
	public User findUser(String email) {
		DBObject query = new BasicDBObject(UserDBObjectConverter.EMAIL_KEY, email);
		DBCollection userColleciton = getUserCollection();
		DBObject result = userColleciton.findOne(query);
		
		if (null == result) {
			return null;			
		}
		else {
			return UserDBObjectConverter.convertToUser(result);
		}
	}

	@Override
	public Post findPost(UUID postId) {
		Post post = mongoOps.findOne(new Query(Criteria.where("_id").is(postId)), 
						MongoPost.class, config.getPostCollection());
		return post;
	}

	@Override
	public Comment findComment(UUID uuid) {
		JacksonDBCollection<MongoComment, UUID> collection = 
				JacksonDBCollection.wrap(getCommentCollection(), MongoComment.class, UUID.class);
		Comment comment = collection.findOneById(uuid);
		return comment;
	}

	@Override
	public List<UUID> findPostUUIDsByUser(String userEmail) {
		List<UUID> postUUIDs = new ArrayList<UUID>();
		Query query = new Query(Criteria.where("userEmail").is(userEmail));
		query.fields().include("_id");
		
		mongoOps.executeQuery(query, config.getPostCollection(), 
				new UUIDCollectingHandler(postUUIDs));
		
		return postUUIDs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Post> findPostsByUser(String userEmail) {
		// yay java type erasure!
		List<?> posts = mongoOps.find(new Query(Criteria.where("userEmail").is(userEmail)), 
				MongoPost.class, config.getPostCollection());
		return (List<Post>) posts;
	}

	@Override
	public List<UUID> findPostUUIDsByTimeRange(DateTime start, DateTime end) {
		List<UUID> postUUIDs = new ArrayList<UUID>();
		Query query = getTimeRangeQuery("createTimestamp", start, end);
		query.fields().include("_id");
		mongoOps.executeQuery(query, config.getPostCollection(), new UUIDCollectingHandler(postUUIDs));
		return postUUIDs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Post> findPostsByTimeRange(DateTime start, DateTime end) {
		Query query = getTimeRangeQuery("createTimestamp", start, end);
		// yay java type erasure!
		List<?> posts = mongoOps.find(query, MongoPost.class, config.getPostCollection());
		return (List<Post>)posts;
	}

	@Override
	public List<UUID> findCommentUUIDsByUser(String userEmail) {
		List<UUID> commentUUIDs = new ArrayList<UUID>();
		
		DBCollection commentCollection = getCommentCollection();
		DBCursor cursor = commentCollection.find(new BasicDBObject("userEmail", userEmail),
							   		   			 new BasicDBObject("_id", 1));
		
		while (cursor.hasNext()) {
			commentUUIDs.add(UUID.fromString((String)cursor.next().get("_id")));
		}
		
		return commentUUIDs;
	}

	@Override
	public List<UUID> findCommentUUIDsByPostSortedByTime(UUID postId) {
		List<UUID> commentUUIDs = new ArrayList<UUID>();
		
		DBCollection commentCollection = getCommentCollection();
		DBCursor cursor = commentCollection.find(new BasicDBObject("postId", postId.toString()),
							   		   			 new BasicDBObject("_id", 1))
						   		   		   .sort(new BasicDBObject("createTimestamp", 1));
		
		while (cursor.hasNext()) {
			commentUUIDs.add(UUID.fromString((String)cursor.next().get("_id")));
		}
		
		return commentUUIDs;
	}

	@Override
	public List<UUID> findCommentUUIDsByPostSortedByVotes(UUID postId) {
		List<UUID> commentUUIDs = new ArrayList<UUID>();
		
		DBCollection commentCollection = getCommentCollection();
		DBCursor cursor = commentCollection.find(new BasicDBObject("postId", postId.toString()),
							   		   			 new BasicDBObject("_id", 1))
						   		   		   .sort(new BasicDBObject("votes", -1));
		
		while (cursor.hasNext()) {
			commentUUIDs.add(UUID.fromString((String)cursor.next().get("_id")));
		}
		
		return commentUUIDs;
	}

	@Override
	public List<Comment> findCommentsByUUIDList(List<UUID> uuidList) {
		List<Comment> comments = new ArrayList<Comment>();
		
		JacksonDBCollection<MongoComment, UUID> collection = 
				JacksonDBCollection.wrap(getCommentCollection(), MongoComment.class, UUID.class);
		org.mongojack.DBCursor<MongoComment> cursor = 
				collection.find(new BasicDBObject("_id", new BasicDBObject("$in", uuidList)));
		
		while(cursor.hasNext()) {
			comments.add(cursor.next());
		}
		
		return comments;
	}

	@Override
	public List<Comment> findCommentsByUser(String userEmail) {
		List<Comment> comments = new ArrayList<Comment>();
		
		JacksonDBCollection<MongoComment, UUID> collection = 
				JacksonDBCollection.wrap(getCommentCollection(), MongoComment.class, UUID.class);
		org.mongojack.DBCursor<MongoComment> cursor = 
				collection.find(new BasicDBObject("userEmail", userEmail));
		
		while(cursor.hasNext()) {
			comments.add(cursor.next());
		}
		
		return comments;
	}

	@Override
	public void voteOnPost(String userEmail, UUID postId) {
		Query query = new Query(Criteria.where("_id").is(postId).and("voters").ne(userEmail));
		Update update = new Update().addToSet("voters", userEmail).inc("votes", 1);
		
		WriteResult result = mongoOps.updateFirst(query, update, config.getPostCollection());
		
		if (result.getN() == 1) {
			recordVote(userEmail, postId, VoteType.POST);
		}
	}

	@Override
	public void voteOnComment(String userEmail, UUID commentId) {;;
		DBObject query = QueryBuilder
				.start("_id").is(commentId.toString())
				.and("voters").notEquals(userEmail).get();
		DBObject update = new BasicDBObject("$inc", new BasicDBObject("votes", 1));
		update.put("$addToSet", new BasicDBObject("voters", userEmail));
		
		WriteResult result = getCommentCollection().update(query, update);
		
		if (result.getN() == 1) {
			recordVote(userEmail, commentId, VoteType.COMMENT);
		}
	}

	@Override
	public Map<UUID, Long> findVotes(final List<UUID> uuidList) {
		final Map<UUID, Long> votesMap = new ConcurrentHashMap<UUID, Long>();
		
		Thread[] threads = new Thread[2];
		
		Thread commentsThread = new Thread() {
			public void run() {
				List<String> uuidStrings = new ArrayList<String>(uuidList.size());
				for(UUID uuid : uuidList) {
					uuidStrings.add(uuid.toString());
				}
				
				BasicDBObject query = new BasicDBObject("_id", new BasicDBObject("$in", uuidStrings));
				BasicDBObject fields = new BasicDBObject("_id", 1);
				fields.put("votes", 1);
				DBCursor cursor = getCommentCollection().find(query, fields);
				while (cursor.hasNext()) {
					DBObject obj = cursor.next();
					votesMap.put(UUID.fromString((String)obj.get("_id")), 
							     (Long) obj.get("votes"));
				}
			}
		};
		threads[0] = commentsThread;
		
		Thread postsThread = new Thread() {
			public void run() {
				final Query votesQuery = new Query(Criteria.where("_id").in(uuidList));
				mongoOps.executeQuery(votesQuery, config.getPostCollection(), 
						new VotesCollectingHandler(votesMap));
			}
		};
		threads[1] = postsThread;
		
		commentsThread.start();
		postsThread.start();
		
		for (Thread thread : threads) {
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		return votesMap;
	}

	@Override
	public DateTime findUserVote(String userEmail, UUID uuid) {
		BasicDBObject query = new BasicDBObject();
		query.put("email", userEmail);
		query.put("typeId", uuid);
		
		DBObject result = getVotesCollection().findOne(query, new BasicDBObject("ts", 1));
		
		return result != null ? new DateTime((Long)result.get("ts")) : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Post> findPostsByVote(int number) {
		Query query = new Query().with(new Sort(new Order(Direction.DESC, "votes"))).limit(number);
		List<? extends Post> posts = mongoOps.find(query, MongoPost.class, config.getPostCollection());
		return (List<Post>) posts;
	}

	@Override
	public Post generateNewPost(String userEmail, String userName,
			String title, DateTime createdAt, String text) {
		MongoPost post = new MongoPost();
		post.setId(UUID.randomUUID());
		post.setTitle(title);
		post.setText(text);
		post.setUserDisplayName(userName);
		post.setUserEmail(userEmail);
		post.setCreateTimestamp(createdAt);
		
		return post;
	}

	@Override
	public Comment generateNewComment(String userEmail, String userName,
			UUID postId, long createdTimestamp, String text) {
		MongoComment comment = new MongoComment();
		comment.setId(UUID.randomUUID());
		comment.setPostId(postId);
		comment.setText(text);
		comment.setUserEmail(userEmail);
		comment.setUserDisplayName(userName);
		comment.setCreateTimestamp(createdTimestamp);
		
		return comment;
	}

	@Override
	public User generateNewUser(String email, String password, String fullName) {
		MongoUser user = new MongoUser(fullName, email, password);
		return user;
	}
	
	protected WriteResult recordVote(String email, UUID id, VoteType type) {
		BasicDBObject voteObj = new BasicDBObject();
		voteObj.put("typeId", id);
		voteObj.put("email", email);
		voteObj.put("type", type.toString());
		voteObj.put("ts", System.currentTimeMillis());
		
		return getVotesCollection().insert(voteObj);
	}
	
	protected DBCollection getUserCollection() {
		return mongo.getDB(config.getDbName())
				.getCollection(config.getUserCollection());
	}
	
	protected DBCollection getPostCollection() {
		return mongo.getDB(config.getDbName())
				.getCollection(config.getPostCollection());
	}

	protected DBCollection getCommentCollection() {
		return mongo.getDB(config.getDbName())
				.getCollection(config.getCommentCollection());
	}
	
	protected DBCollection getVotesCollection() {
		return mongo.getDB(config.getDbName())
				.getCollection(config.getVotesCollection());
	}
	
	protected Query getTimeRangeQuery(String field, DateTime start, DateTime end) {
		Query query = new Query(Criteria.where(field).gte(start.getMillis())
									.andOperator(Criteria.where(field).lt(end.getMillis())));
		return query;
	}
	
	protected class VotesCollectingHandler implements DocumentCallbackHandler {
		
		private Map<UUID, Long> votesMap;
		
		public VotesCollectingHandler(Map<UUID, Long> votesMap) {
			this.votesMap = votesMap;
		}
		
		@Override
		public void processDocument(DBObject dbObject) throws MongoException,
				DataAccessException {
			Object id = dbObject.get("_id");
			if (id instanceof UUID) {
				votesMap.put((UUID) dbObject.get("_id"), (Long) dbObject.get("votes"));
			}
			else {
				votesMap.put(UUID.fromString((String)dbObject.get("_id")), (Long) dbObject.get("votes"));
			}
		}
		
	}
	
	protected class UUIDCollectingHandler implements DocumentCallbackHandler {
		
		private List<UUID> list;
		
		public UUIDCollectingHandler(List<UUID> list) {
			this.list = list;
		}

		@Override
		public void processDocument(DBObject dbObject) throws MongoException,
				DataAccessException {
			list.add((UUID) dbObject.get("_id"));
		}
		
	}

}
