package com.youtube.funfactor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@EqualsAndHashCode
@Table(name = "movies", catalog = "youtube")
public class Movie implements Serializable {

	private static final long serialVersionUID = -6605313931957886584L;

	@Id
	@Column(name = "youtubeId", unique = true, nullable = false)
	private String youtubeId;

	private String title;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name="movies_tags")
	private Set<Tag> tags = new HashSet<Tag>();

	@Column(columnDefinition="TEXT")
	private String description;
	private long duration;
	private long viewCount;
	private int likeCount;
	private int dislikeCount;
	private int favoriteCount;
	private int commentCount;
	private String channelId;
	private int category;
	
	private int score;
	private boolean isTest;

}
