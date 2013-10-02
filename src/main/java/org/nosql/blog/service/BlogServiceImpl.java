package org.nosql.blog.service;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.nosql.blog.dao.BlogDao;
import org.nosql.blog.model.Comment;
import org.nosql.blog.model.Post;
import org.nosql.blog.model.User;

/**
 * Service layer responsible for pre/post processing of data to/from DAO.
 *
 */
public class BlogServiceImpl implements BlogService {

    private final BlogDao dao;

    public BlogServiceImpl(BlogDao dao) {
        this.dao = dao;
    }

    @Override
    public Comment createComment(String userEmail, UUID postId, String commentText) {
        User user = dao.findUser(userEmail);
        if ( null == user ) {
            throw new RuntimeException( "user with email, " + userEmail + ", does not exist!  Cannot create comment");
        }

        Comment comment = dao.generateNewComment(user.getEmail(), user.getName(),
                postId, System.currentTimeMillis(), commentText);
        return dao.saveComment(comment);
    }

    @Override
    public User createUser(String email, String password, String fullName) {
    	User user = dao.generateNewUser(email, password, fullName);
        return dao.saveUser(user);
    }

    @Override
    public Post createPost(String userEmail, String title, String text) {
        User user = dao.findUser(userEmail);
        Post post = dao.generateNewPost(user.getEmail(), user.getName(), title, new DateTime(), text);
        return dao.savePost(post);
    }

    @Override
    public List<Post> findPostsByTimeRange(DateTime start, DateTime end) {
        return dao.findPostsByTimeRange(start, end);
    }

    @Override
    public List<Post> findRecentPosts(int minutes) {
        DateTime end = new DateTime().withZone(DateTimeZone.forOffsetHours(0));
        DateTime start = end.minusMinutes(minutes);
        return findPostsByTimeRange(start, end);
    }

    @Override
    public List<Post> findTopPosts(int number) {
        return dao.findPostsByVote(number);
    }

    @Override
    public void voteOnPost(String userEmail, UUID uuid) {
        dao.voteOnPost(userEmail, uuid);
    }

    @Override
    public void voteOnComment(String userEmail, UUID uuid) {
        dao.voteOnComment(userEmail, uuid);
    }

    @Override
    public User findUser(String userEmail) {
        return dao.findUser(userEmail);
    }

    @Override
    public List<Comment> findCommentsByUser(String userEmail) {
        return dao.findCommentsByUser(userEmail);
    }

    @Override
    public Post findPost(UUID postId) {
        return dao.findPost(postId);
    }

    @Override
    public Comment findComment(UUID commentId) {
        return dao.findComment(commentId);
    }
    
}
