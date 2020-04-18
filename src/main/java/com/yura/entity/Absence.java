package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(
	name = "absences",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "name", name = "a_1")
	}
)
public class Absence {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_absence")
	@JsonProperty("idAbsence")
	private int idAbsence;
	@Column(name = "name")
	@JsonProperty("name")
	private String name;
	@OneToMany(mappedBy = "absence", cascade = {CascadeType.ALL})
	@JsonProperty("listCategoryAbsence")
	private List<CategoryAbsence> listCategoryAbsence;
	
	public Absence() {
		//
	}
	
	public Absence(String name, List<CategoryAbsence> listCategoryAbsence) {
		this.name = name;
		this.listCategoryAbsence = listCategoryAbsence;
	}
	
	public int getIdAbsence() {
		return this.idAbsence;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setListCategoryAbsence(List<CategoryAbsence> listCategoryAbsence) {
		this.listCategoryAbsence = listCategoryAbsence;
	}
	
	public List<CategoryAbsence> getListCategoryAbsence() {
		return this.listCategoryAbsence;
	}

	public List<Category> getListCategory() {
		List<Category> listCategory = new ArrayList<Category>();
		for (CategoryAbsence categoryAbsence : this.listCategoryAbsence) {
			listCategory.add(categoryAbsence.getCategory());
		}
		return listCategory;
	}
}