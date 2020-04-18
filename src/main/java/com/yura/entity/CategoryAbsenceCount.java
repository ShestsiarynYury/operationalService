package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ForeignKey;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "categories_absences_counts")
public class CategoryAbsenceCount {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_category_absence_count")
	private int idCategoryAbsenceCount;
	@ManyToOne()
	@JoinColumn(name = "fk_id_category_absence", referencedColumnName = "id_category_absence", foreignKey = @ForeignKey(name = "cac_1"))
	private CategoryAbsence categoryAbsence;
	@Column(name = "count_absence")
	private int countAbsence;
	@ManyToOne()
	@JoinColumn(name = "fk_id_expenditure_category_count", referencedColumnName = "id_expenditure_category_count", foreignKey = @ForeignKey(name = "cac_2"))
	private ExpenditureCategoryCount expenditureCategory;
	
	public CategoryAbsenceCount() {
		//
	}
	
	public int getIdCategoryAbsenceCount() {
		return this.idCategoryAbsenceCount;
	}
	
	public void setCategoryAbsence(CategoryAbsence categoryAbsence) {
		this.categoryAbsence = categoryAbsence;
	}
	
	public CategoryAbsence getCategoryAbsence() {
		return this.categoryAbsence;
	}
	
	public void setCountAbsence(int countAbsence) {
		this.countAbsence = countAbsence;
	}
	
	public int getCountAbsence() {
		return this.countAbsence;
	}
	
	public void setExpenditureCategory(ExpenditureCategoryCount expenditureCategory) {
		this.expenditureCategory = expenditureCategory;
	}
	
	public ExpenditureCategoryCount getExpenditureCatregory() {
		return this.expenditureCategory;
	}
}	