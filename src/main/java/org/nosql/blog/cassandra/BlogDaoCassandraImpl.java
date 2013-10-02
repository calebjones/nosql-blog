package org.nosql.blog.cassandra;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.CounterRow;
import me.prettyprint.hector.api.beans.CounterRows;
import me.prettyprint.hector.api.beans.CounterSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.MultigetSliceCounterQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hom.EntityManagerImpl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.nosql.blog.dao.BlogDao;
import org.nosql.blog.model.Comment;
import org.nosql.blog.model.Post;
import org.nosql.blog.model.User;

/**
 *
 *
 */
public class BlogDaoCassandraImpl implements BlogDao {
	
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("YYYYMMdd:HH");
    private static final byte[] POSTS_BY_VOTE_KEY = "posts-sorted".getBytes();

    private Keyspace keyspace;
    private EntityManagerImpl entityManager;

    private static final String CF_USERS = "users";
    private static final String USER_COL_PASS = "password";
    private static final String USER_COL_NAME = "name";

    private static final String CF_COMMENTS = "comments";

    private static final String CF_POSTS = "posts";

    private static final String CF_USER_POSTS = "user_posts";

    private static final String CF_USER_VOTES = "user_votes";

    private static final String CF_POSTS_BY_TIME = "posts_by_time";

    private static final String CF_POSTS_BY_VOTE = "posts_sorted_by_vote";

    private static final String CF_USER_COMMENTS = "user_comments";

    private static final String CF_POST_COMMENTS = "post_comments";

    private static final String CF_VOTES = "votes";

    private static final String CF_POST_COMMENT_VOTE_CHANGE = "post_comment_votes_changed";

    private static final String CF_POST_COMMENTS_SORTED_BY_VOTE = "post_comments_sorted_by_vote";

    private String host;
    
    private String keyspaceName;

    public BlogDaoCassandraImpl(String host, String keyspace) {
    	this.host = host;
    	this.keyspaceName = keyspace;
    }
    
    /**
     * Must call once (and only once) prior to using the DAO.
     *
     */
    public void init() {
		initHector();
	}

    private void initHector() {
        Cluster cluster = HFactory.getOrCreateCluster("training-cluster", host);
        keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        entityManager = new EntityManagerImpl(keyspace, "org.nosql.blog.model" );
    }

    @Override
    public User saveUser( User user ) {
        // this simple save could easily be done with HOM (Hector Object Mapper)
        // but we'll do it this way once for illustration
        Mutator<String> m = HFactory.createMutator(keyspace, StringSerializer.get());
        m.addInsertion(user.getEmail(), CF_USERS, HFactory.createColumn(USER_COL_PASS, user.getPassword(), StringSerializer.get(), StringSerializer.get()));
        m.addInsertion(user.getEmail(), CF_USERS, HFactory.createColumn(USER_COL_NAME, user.getName(), StringSerializer.get(), StringSerializer.get()));
        m.execute();
        return user;
    }

    @Override
    public Post savePost( Post post ) {
        Mutator<byte[]> m = HFactory.createMutator(keyspace, BytesArraySerializer.get());

        // insert row for Post - EntityManager handles mapping POJO to Cassandra row
        entityManager.persist(Collections.singleton(post), m);

        // insert one-to-many for user->post : these are sorted by TimeUUID (chrono + unique)
        // TODO - add params
//        m.addInsertion(key, CF, column);

        // insert TimeUUID post ID to track order the posts were entered
        DateTime dt = calculatePostTimeGranularity(post.getCreateTimestamp());
        // TODO - add params
//        m.addInsertion(key, CF, column);

        // add a zero to counter so we don't miss one when sorting by votes - this leaves the counter at zero
        m.addCounter(UUIDSerializer.get().toBytes(post.getId()), CF_VOTES, HFactory.createCounterColumn("v", 0));

        // send the batch
        m.execute();

        post.setVotes( 0L );
        return post;
    }

    @Override
    public Comment saveComment( Comment comment ) {
        Mutator<byte[]> m = HFactory.createMutator(keyspace, BytesArraySerializer.get());

        // insert row for comment - EntityManager handles mapping POJO to Cassandra row
        entityManager.persist(Collections.singleton(comment), m);

        // insert one-to-many for user->comments and post->comments : these are sorted by TimeUUID (chrono + unique)
        // TODO - add params
//        m.addInsertion(key, CF, column);
//        m.addInsertion(key, CF, column);

        // add a zero to counter so we don't miss one when sorting by votes - this leaves the counter at zero
        m.addCounter(UUIDSerializer.get().toBytes(comment.getId()), CF_VOTES, HFactory.createCounterColumn("v", 0));

        // this insert is to signal that this post needs its comments sorted
        m.addInsertion(UUIDSerializer.get().toBytes(comment.getPostId()), CF_POST_COMMENT_VOTE_CHANGE, HFactory.createColumn("v", EMPTY_BYTES));

        // send the batch
        m.execute();

        comment.setVotes( 0L );
        return comment;
    }

    @Override
    public User findUser( String email ) {
        // this simple query could easily be done with HOM (Hector Object Mapper)
        // but we'll do it this way once for illustration
        SliceQuery<String, String, String> q = HFactory.createSliceQuery(keyspace, StringSerializer.get(), StringSerializer.get(), StringSerializer.get());
        q.setColumnFamily(CF_USERS);
        q.setKey(email);
        q.setRange(null, null, false, 100);
        QueryResult<ColumnSlice<String, String>> qr = q.execute();

        ColumnSlice<String, String> slice = qr.get();

        if ( null == slice || slice.getColumns().isEmpty() ) {
            return null;
        }

        User user = new CassandraUser();
        user.setEmail(email);
        user.setPassword(slice.getColumnByName(USER_COL_PASS).getValue());
        user.setName(slice.getColumnByName(USER_COL_NAME).getValue());

        return user;
    }

    @Override
    public Post findPost( UUID postId ) {
        Post p = entityManager.find(Post.class, postId);
        Map<UUID, Long> voteMap = findVotes(Collections.singletonList(postId));
        p.setVotes(voteMap.get(postId));
        return p;
    }

    @Override
    public Comment findComment(UUID uuid) {
        Comment c = entityManager.find( Comment.class, uuid);
        if ( null == c ) {
            return null;
        }

        Map<UUID, Long> voteMap = findVotes(Collections.singletonList(uuid));
        if ( voteMap.isEmpty() ) {
            return c;
        }

        c.setVotes(voteMap.get(uuid));
        return c;
    }

    private List<Post> findPostsByUUIDList(List<UUID> uuidList, boolean includeVotes) {
        MultigetSliceQuery<UUID, String, byte[]> q = HFactory.createMultigetSliceQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_POSTS);
        q.setRange(null, null, false, 100);
        q.setKeys(uuidList);
        QueryResult<Rows<UUID, String, byte[]>> qr = q.execute();
        Rows<UUID, String, byte[]> rows = qr.get();
        if ( null == rows || 0 == rows.getCount()) {
            return null;
        }

        Map<UUID, Post> postMap = new HashMap<UUID, Post>();
        for ( Row<UUID, String, byte[]> row : rows) {
            postMap.put(row.getKey(), entityManager.find(Post.class, row.getKey(), row.getColumnSlice()));
        }

        // gotta do it this way to preserve ordering from the original UUID List
        List<Post> postList = new LinkedList<Post>();
        for ( UUID uuid : uuidList ) {
            postList.add(postMap.get(uuid));
        }

        if ( includeVotes ) {
            Map<UUID, Long> voteMap = findVotes(uuidList);
            for (Post post : postList ) {
                Long votes = voteMap.get(post.getId());
                if ( null != votes ) {
                    post.setVotes(votes);
                }
            }
        }

        return postList;
    }

    @Override
    public List<UUID> findPostUUIDsByUser( String userEmail ) {
        SliceQuery<String, UUID, byte[]> q = HFactory.createSliceQuery(keyspace, StringSerializer.get(), UUIDSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_USER_POSTS);
        q.setKey(userEmail);
        q.setRange(null, null, false, 10);

        ColumnSliceIterator<String, UUID, byte[]> iter = new ColumnSliceIterator<String, UUID, byte[]>(q, null, (UUID)null, false);
        List<UUID> uuidList = new LinkedList<UUID>();
        while ( iter.hasNext() ) {
            HColumn<UUID, byte[]> col = iter.next();
            uuidList.add(col.getName());
        }

        return uuidList;
    }

    @Override
    public List<Post> findPostsByUser( String userEmail ) {
        List<UUID> uuidList = findPostUUIDsByUser(userEmail);
        if ( uuidList.isEmpty() ) {
            return null;
        }

        return findPostsByUUIDList( uuidList, true );
    }

    @Override
    public List<UUID> findPostUUIDsByTimeRange( DateTime start, DateTime end ) {
        // this method assumes the number of keys for the range is "not too big" so as to blow
        // out thrift's frame buffer or cause cassandra to take too long and "time out"
        DateTime firstRow = calculatePostTimeGranularity(start);
        DateTime lastRow = calculatePostTimeGranularity(end);

        MultigetSliceQuery<String, UUID, byte[]> q = HFactory.createMultigetSliceQuery(keyspace, StringSerializer.get(), UUIDSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_POSTS_BY_TIME);

        // determine all the rows required to satisfy the time range and set as the 'row keys' for the query
        // each row key is "pre-decided" to be hours of the day
        DateTime current = firstRow;
        List<String> rowKeys = new LinkedList<String>();
        while ( current.isBefore(lastRow) || current.isEqual(lastRow) ) {
            rowKeys.add(hourFormatter.print(current));
            current = current.plusHours(1);
        }
        q.setKeys(rowKeys);
        q.setRange(null, null, false, 1000); // this is an assumption that there will not be more than 1000 posts in one hour

        QueryResult<Rows<String, UUID, byte[]>> qr = q.execute();
        Rows<String, UUID, byte[]> rows = qr.get();
        if ( null == rows || 0 == rows.getCount() ) {
            return null;
        }

        long startAsLong = start.getMillis();
        long endAsLong = end.getMillis();

        // loop over result rows, only adding to uuidList if Post time is between range
        List<UUID> uuidList = new LinkedList<UUID>();
        for ( Row<String, UUID, byte[]> row : rows ) {
            ColumnSlice<UUID, byte[]> slice = row.getColumnSlice();
            for ( HColumn<UUID, byte[]> col : slice.getColumns() ) {
                long t = TimeUUIDUtils.getTimeFromUUID(col.getName());
                if ( t > endAsLong ) {
                    break;
                }

                if ( t >= startAsLong ) {
                    uuidList.add(col.getName());
                }
            }
        }

        return uuidList;
    }

    @Override
    public List<Post> findPostsByTimeRange(DateTime start, DateTime end) {
        List<UUID> uuidList = findPostUUIDsByTimeRange(start, end);
        if ( uuidList.isEmpty() ) {
            return null;
        }

        return findPostsByUUIDList( uuidList, true );
    }

    @Override
    public List<UUID> findCommentUUIDsByUser( String userEmail ) {
        // TODO - do it all!

        // create a Slice Query

        // use a column slice iterator

        List<UUID> uuidList = new LinkedList<UUID>();

        // iterator over filling in this list

        return uuidList;
    }

    @Override
    public List<UUID> findCommentUUIDsByPostSortedByTime(UUID postId) {
        SliceQuery<UUID, UUID, byte[]> q = HFactory.createSliceQuery(keyspace, UUIDSerializer.get(), UUIDSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_POST_COMMENTS);
        q.setKey(postId);
        q.setRange(null, null, false, 10);

        ColumnSliceIterator<UUID, UUID, byte[]> iter = new ColumnSliceIterator<UUID, UUID, byte[]>(q, null, (UUID)null, false);
        List<UUID> uuidList = new LinkedList<UUID>();
        while ( iter.hasNext() ) {
            HColumn<UUID, byte[]> col = iter.next();
            uuidList.add(col.getName());
        }

        return uuidList;
    }

    @Override
    public List<UUID> findCommentUUIDsByPostSortedByVotes(UUID postId) {
        if (postCommentsNeedSorting(postId)) {
            sortCommentsByVotes(postId);
        }
        
        SliceQuery<UUID, Composite, byte[]> q = HFactory.createSliceQuery(keyspace, UUIDSerializer.get(), CompositeSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_POST_COMMENTS_SORTED_BY_VOTE);
        q.setKey(postId);
        q.setRange(null, null, false, 10);

        ColumnSliceIterator<UUID, Composite, byte[]> iter = new ColumnSliceIterator<UUID, Composite, byte[]>(q, null, (Composite)null, false);
        List<UUID> uuidList = new LinkedList<UUID>();
        while ( iter.hasNext() ) {
            HColumn<Composite, byte[]> col = iter.next();
            uuidList.add(UUIDSerializer.get().fromByteBuffer((ByteBuffer)col.getName().get(1)));
        }

        return uuidList;
    }

    @Override
    public List<Comment> findCommentsByUUIDList( List<UUID> uuidList ) {
        MultigetSliceQuery<UUID, String, byte[]> q = HFactory.createMultigetSliceQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_COMMENTS);
        q.setRange(null, null, false, 100);
        q.setKeys(uuidList);
        QueryResult<Rows<UUID, String, byte[]>> qr = q.execute();
        Rows<UUID, String, byte[]> rows = qr.get();
        if ( null == rows || 0 == rows.getCount()) {
            return null;
        }

        Map<UUID, Comment> commentMap = new HashMap<UUID, Comment>();
        for ( Row<UUID, String, byte[]> row : rows) {
            commentMap.put(row.getKey(), entityManager.find(Comment.class, row.getKey(), row.getColumnSlice()));
        }

        // gotta do it this way to preserve ordering from the original UUID List
        List<Comment> commentList = new LinkedList<Comment>();
        for ( UUID uuid : uuidList ) {
            commentList.add(commentMap.get(uuid));
        }

        Map<UUID, Long> voteMap = findVotes(uuidList);
        for (Comment comment : commentList ) {
            Long votes = voteMap.get(comment.getId());
            if ( null != votes ) {
                comment.setVotes(votes);
            }
        }

        return commentList;
    }

    @Override
    public List<Comment> findCommentsByUser( String userEmail ) {
        List<UUID> uuidList = findCommentUUIDsByUser(userEmail);
        if ( uuidList.isEmpty() ) {
            return null;
        }

        return findCommentsByUUIDList(uuidList);
    }

    private void vote(String userEmail, String type, UUID uuid) {
        Mutator<byte[]> m = HFactory.createMutator(keyspace, BytesArraySerializer.get());

        m.addCounter(UUIDSerializer.get().toBytes(uuid), CF_VOTES, HFactory.createCounterColumn("v", 1));
        m.addInsertion(StringSerializer.get().toBytes(userEmail), CF_USER_VOTES, HFactory.createColumn(uuid, System.currentTimeMillis()));

        // this inserts the fact that this post has comment votes that have been updated, so next time we
        // need the comments sorted, we will do so, otherwise, don't waste time sorting
        if ( "comment".equalsIgnoreCase(type) ) {
            Comment c = findComment(uuid);
            if ( null != c) {
                m.addInsertion( UUIDSerializer.get().toBytes(c.getPostId()), CF_POST_COMMENT_VOTE_CHANGE, HFactory.createColumn("v", EMPTY_BYTES) );
            }
        }

        m.execute();
    }

    @Override
    public void voteOnPost( String userEmail, UUID postId ) {
        if ( null == findUserVote(userEmail, postId) ) {
        	vote(userEmail, "POST", postId);
        }
    }

    @Override
    public void voteOnComment( String userEmail, UUID commentId ) {
        if ( null == findUserVote(userEmail, commentId) ) {
        	vote(userEmail, "COMMENT", commentId);
        }
    }

    @Override
    public Map<UUID, Long> findVotes( List<UUID> uuidList ) {
        if ( null == uuidList || uuidList.isEmpty() ) {
            return Collections.emptyMap();
        }

        MultigetSliceCounterQuery<UUID, String> q = HFactory.createMultigetSliceCounterQuery(keyspace, UUIDSerializer.get(), StringSerializer.get());
        q.setColumnFamily(CF_VOTES);
        q.setKeys(uuidList);
        q.setRange(null, null, false, 100);
        QueryResult<CounterRows<UUID, String>> qr = q.execute();
        CounterRows<UUID, String> rows = qr.get();
        if ( null == rows || 0 == rows.getCount()) {
            return null;
        }

        Map<UUID, Long> voteList = new HashMap<UUID, Long>();

        for ( CounterRow<UUID, String> row : rows ) {
            CounterSlice<String> slice = row.getColumnSlice();
            if ( null != slice && !slice.getColumns().isEmpty()) {
                voteList.put( row.getKey(), slice.getColumnByName("v").getValue());
            }
        }

        return voteList;
    }

    @Override
    public DateTime findUserVote(String userEmail, UUID uuid) {
        ColumnQuery<String, UUID, Long> q = HFactory.createColumnQuery(keyspace, StringSerializer.get(), UUIDSerializer.get(), LongSerializer.get());
        q.setColumnFamily(CF_USER_VOTES);
        q.setKey(userEmail);
        q.setName(uuid);

        QueryResult<HColumn<UUID, Long>> qr = q.execute();
        HColumn<UUID, Long> col = qr.get();
        if ( null != col ) {
            return new DateTime(col.getValue());
        }
        else {
            return null;
        }
    }

    @Override
    public List<Post> findPostsByVote(int number) {
    	sortPostsByVote(BlogDao.MAX_SORT_DAYS);
    	
        SliceQuery<byte[], Composite, byte[]> q = HFactory.createSliceQuery(keyspace, BytesArraySerializer.get(), CompositeSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_POSTS_BY_VOTE);
        q.setKey(POSTS_BY_VOTE_KEY);
        q.setRange(null, null, false, 10);

        ColumnSliceIterator<byte[], Composite, byte[]> iter = new ColumnSliceIterator<byte[], Composite, byte[]>(q, null, (Composite)null, false);
        List<UUID> uuidList = new LinkedList<UUID>();
        Map<UUID, Long> voteMap = new HashMap<UUID, Long>();
        int count = number;
        while ( iter.hasNext() && 0 < count--) {
            HColumn<Composite, byte[]> col = iter.next();
            ByteBuffer bb = (ByteBuffer)col.getName().get(1);
            UUID uuid = UUIDSerializer.get().fromByteBuffer(bb);
            uuidList.add( uuid );
            voteMap.put( uuid, LongSerializer.get().fromByteBuffer((ByteBuffer)col.getName().get(0)));
        }

        List<Post> postList = findPostsByUUIDList(uuidList, false);
        if ( null != postList && !postList.isEmpty() ) {
            for ( Post p : postList ) {
                p.setVotes(voteMap.get(p.getId()));
            }
        }

        return postList;
    }
    
	@Override
	public Comment generateNewComment(String userEmail,
			String userName, UUID postId, long createdTimestamp, String text) {
		return new CassandraComment(generateNewCommentUUID(), userEmail, userName, postId, createdTimestamp, text);
	}

	@Override
	public User generateNewUser(String email, String password, String fullName) {
		return new CassandraUser(email, password, fullName);
	}

	@Override
	public Post generateNewPost(String userEmail, String userName,
			String title, DateTime createdAt, String text) {
		return new CassandraPost(generateNewPostUUID(), userEmail, userName, title, createdAt, text);
	}

    protected boolean postCommentsNeedSorting(UUID postId) {
        ColumnQuery<UUID, String, byte[]> q = HFactory.createColumnQuery(keyspace, UUIDSerializer.get(), StringSerializer.get(), BytesArraySerializer.get());
        q.setColumnFamily(CF_POST_COMMENT_VOTE_CHANGE);
        q.setKey(postId);
        q.setName("v");

        QueryResult<HColumn<String, byte[]>> qr = q.execute();
        HColumn<String, byte[]> col = qr.get();
        return null != col;
    }
    
    protected void sortPostsByVote(int days) {
        Mutator<byte[]> m = HFactory.createMutator(keyspace, BytesArraySerializer.get());

        // delete the old row first, then we'll add the new
        m.addDeletion(POSTS_BY_VOTE_KEY, CF_POSTS_BY_VOTE);

        // calc date range, end with yesterday and start 'days' prior
        DateTime start = new DateTime().minusDays(days).hourOfDay().roundFloorCopy();
        DateTime end = new DateTime();
        List<UUID> uuidList = findPostUUIDsByTimeRange(start, end);

        // find votes, then save them to CF which will sort them using Composite col name
        if ( null != uuidList && !uuidList.isEmpty() ) {
            Map<UUID, Long> voteMap = findVotes(uuidList);
            // now write to CF
            for (Map.Entry<UUID, Long> entry : voteMap.entrySet() ) {
                Composite colName = new Composite(entry.getValue(), entry.getKey());
                m.addInsertion(POSTS_BY_VOTE_KEY, CF_POSTS_BY_VOTE, HFactory.createColumn(colName, EMPTY_BYTES));
            }
        }

        // send the batch
        m.execute();
    }
    
    protected void sortCommentsByVotes(UUID postId) {
        Mutator<byte[]> m = HFactory.createMutator(keyspace, BytesArraySerializer.get());

        byte[] postIdAsBytes = UUIDSerializer.get().toBytes(postId);
        // delete the old row first, then we'll add the new
        m.addDeletion(postIdAsBytes, CF_POST_COMMENTS_SORTED_BY_VOTE);

        List<UUID> uuidList = findCommentUUIDsByPostSortedByTime(postId);
        if ( null == uuidList || uuidList.isEmpty() ) {
            return;
        }

        Map<UUID, Long> voteMap = findVotes(uuidList);

        // now write to CF
        for (Map.Entry<UUID, Long> entry : voteMap.entrySet() ) {
            Composite colName = new Composite(entry.getValue(), entry.getKey());
            m.addInsertion(postIdAsBytes, CF_POST_COMMENTS_SORTED_BY_VOTE, HFactory.createColumn(colName, EMPTY_BYTES));
        }

        // delete the marker that said we needed to sort comments for this post
        m.addDeletion(postIdAsBytes, CF_POST_COMMENT_VOTE_CHANGE);

        m.execute();
    }
    
    private UUID generateNewPostUUID() {
    	return TimeUUIDUtils.getUniqueTimeUUIDinMillis();
    }
    
    private UUID generateNewCommentUUID() {
    	return TimeUUIDUtils.getUniqueTimeUUIDinMillis();
    }

    private DateTime calculatePostTimeGranularity(DateTime timestamp) {
        return timestamp.withZone(DateTimeZone.forOffsetHours(0)).hourOfDay().roundFloorCopy();
    }

}
