package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field.Store;

@Comment("Many to many relationship between user and group")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID", "GROUP_ID" }) })
public class UserGroup extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "GROUP_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Grouping grouping;

	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	// Needed for JPA
	public UserGroup() {
	}

	public Grouping getGrouping() {
		return this.grouping;
	}

	public User getUser() {
		return this.user;
	}

	public void setGrouping(Grouping grouping) {
		this.grouping = grouping;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public Document getDoc() {
		Document doc = new Document();
		if (user.getFullName() != null) {
			doc.add(new TextField("text", user.getFullName(), Store.NO));
		}
		doc.add(new StringField("user", user.getName(), Store.NO));
		doc.add(new StringField("grouping", "Grouping:" + grouping.id, Store.YES));
		return doc;
	}

}
