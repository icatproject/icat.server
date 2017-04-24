package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "PARENT_ID", "NAME" }) })
public class Path implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected Long id;

	@JoinColumn(name = "PARENT_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Path parent;

	@Column(name = "NAME")
	private String name;

	private boolean file;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
	private List<Path> children = new ArrayList<Path>();

	public Path() {
	}

	public Path(Path parent, java.nio.file.Path path) {
		this.parent = parent;
		this.name = path.toString();
	}

	public List<Path> getChildren() {
		return children;
	}

	public String getName() {
		return name;
	}

	public Path getParent() {
		return parent;
	}

	public boolean isFile() {
		return file;
	}

	public void setChildren(List<Path> children) {
		this.children = children;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public void setParent(Path parent) {
		this.parent = parent;
	}

	public long getId() {
		return id;
	}

}
