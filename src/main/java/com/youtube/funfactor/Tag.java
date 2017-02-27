package com.youtube.funfactor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.SQLInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@EqualsAndHashCode
@Table(name = "tags", catalog = "youtube",
uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@SQLInsert(sql="INSERT IGNORE INTO tags(score, name) VALUES(?,? )")
public class Tag implements Serializable {

	private static final long serialVersionUID = 3938095696942246119L;
	
	@Id
	@Column(name = "name", unique = true, nullable = false)
	private String name;
	private int score;
	
}
