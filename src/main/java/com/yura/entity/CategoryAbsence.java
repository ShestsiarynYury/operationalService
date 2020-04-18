package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ForeignKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.CascadeType;

@Entity
@Table(name = "categories_absences")
public class CategoryAbsence {
 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_category_absence")
	private int idCategoryAbsence;
	@ManyToOne(cascade = {CascadeType.PERSIST})
	@JoinColumn(name = "fk_id_category", referencedColumnName = "id_category", foreignKey = @ForeignKey(name = "ca_1"))
	private Category category;
	@ManyToOne()
	@JoinColumn(name = "fk_id_absence", referencedColumnName = "id_absence", foreignKey = @ForeignKey(name = "ca_2"))
	private Absence absence;
 
	public CategoryAbsence() {
	 //
	}
	
	public CategoryAbsence(Category category, Absence absence) {
		this.category = category;
		this.absence = absence;
	}

	public int getIdCategoryAbsence() {
	 return this.idCategoryAbsence;
	}

	public void setCategory(Category category) {
	 this.category = category;
	}

	public Category getCategory() {
	 return this.category;
	}

	public void setAbsence(Absence absence) {
	 this.absence = absence;
	}

	public Absence getAbsence() {
	 return this.absence;
	}
}