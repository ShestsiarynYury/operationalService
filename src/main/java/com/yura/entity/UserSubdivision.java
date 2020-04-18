package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.UniqueConstraint;
import javax.persistence.GenerationType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(
	name = "users_subdivisions",
	uniqueConstraints = {
		@UniqueConstraint( columnNames = {"fk_id_user", "fk_id_subdivision"}, name = "us_1")
	}
)
public class UserSubdivision {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_user_subdivision")
	@JsonProperty("idUserSubdivision")
	private int idUserSubdivision;
	@ManyToOne()
    @JoinColumn(name = "fk_id_user", referencedColumnName = "id_user", foreignKey = @ForeignKey(name = "us_2"))
	@JsonProperty("user")
	private User user;
	@ManyToOne()
    @JoinColumn(name = "fk_id_subdivision", referencedColumnName = "id_subdivision", foreignKey = @ForeignKey(name = "us_3"))
	@JsonProperty("subdivision")
	private Subdivision subdivision;
	@Column(name = "is_edit")
	@JsonProperty("isEdit")
	private boolean isEdit;
	
	public UserSubdivision() {
		//
	}
	
	public int getIdUserSubdivision() {
		return this.idUserSubdivision;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setSubdivision(Subdivision subdivision) {
		this.subdivision = subdivision;
	}
	
	public Subdivision getSubdivision() {
		return this.subdivision;
	}
	
	public void setIsEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}
	
	public boolean getIsEdit() {
		return this.isEdit;
	}
}