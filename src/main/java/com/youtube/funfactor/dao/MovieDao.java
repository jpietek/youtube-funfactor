package com.youtube.funfactor.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.youtube.funfactor.Movie;

public class MovieDao {

	private static Logger logger = Logger.getLogger(MovieDao.class);
	private Session session;
	
	public void open() {
		session = HibernateUtil.getSessionFactory().openSession();
	}

	public boolean checkMovie(String id) {
		Query q = session.createSQLQuery("SELECT COUNT(*) from youtube.movies"
				+ " WHERE youtube.movies.youtubeId = :id")
				.setParameter("id", id);
					
		if(((Number) q.uniqueResult()).intValue() == 0) {
			return false;
		}
		
		return true;
	}
	
	public void save(Movie movie) {
		if(checkMovie(movie.getYoutubeId())) {
			return;
		}
		Transaction tra = session.beginTransaction();
		session.save(movie);
	
		tra.commit();
	}
	
	public Set<String> getTags(String movieId) {
		List<String> tags = session.createSQLQuery(
				"SELECT youtube.movies_tags.tags_name " +
				"FROM youtube.movies_tags " + 
				"WHERE youtube.movies_tags.Movie_youtubeId = :id")
				.setParameter("id", movieId).list();
		
		Set<String> lowerTags = new HashSet<String>();
		for(String s: tags) {
			lowerTags.add(s.toLowerCase());
		}
		return lowerTags;
	}
	
	public Number getTagsScore(String movieId) {
		return (Number) session.createSQLQuery(
				"SELECT SUM(youtube.tags.score) " +
				"FROM youtube.movies_tags " +  
                "JOIN youtube.tags " +
                " ON youtube.movies_tags.tags_name = youtube.tags.name " +
				" WHERE youtube.movies_tags.Movie_youtubeId = :id")
				.setParameter("id", movieId).uniqueResult();
	}
	
	public void commitAndclose() {
		session.getTransaction().commit();
		session.close();
	}
	
	public void close() {
		session.close();
	}
	
	public List<String> getIds() {
		@SuppressWarnings("unchecked")
		List<String> ids = 
				session.createSQLQuery("SELECT youtube.movies.youtubeId "
						+ "FROM youtube.movies").list();

		return ids;
	}

	public void updateTagScore(String tag, int score) {
		Transaction tx = session.beginTransaction();
		Query q = session.createSQLQuery("UPDATE youtube.tags SET "
			   + "youtube.tags.score = :score"
			   + " WHERE youtube.tags.name = :tag")
			   .setParameter("tag", tag)
			   .setParameter("score", score);
		q.executeUpdate();
		tx.commit();
	}
	
	public void updateScore(String id, int score) {
		Query q = session.createSQLQuery("UPDATE youtube.movies SET "
				+ "youtube.movies.score = " + score
				+ " WHERE youtube.movies.youtubeId = " + "'" + id + "'");
		q.executeUpdate();
	}
}
