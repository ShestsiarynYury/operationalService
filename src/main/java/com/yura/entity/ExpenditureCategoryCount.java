package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ForeignKey;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.Transient;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.yura.entity.Expenditure;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "expenditure_category_counts")
public class ExpenditureCategoryCount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_expenditure_category_count")
    @JsonProperty("idExpenditureCategoryCount")
	private int idExpenditureCategoryCount;
	@ManyToOne()
	@JoinColumn(name = "fk_id_category", referencedColumnName = "id_category", foreignKey = @ForeignKey(name = "ecc_1"))
    @JsonProperty("category")
	private Category category;
	@Column(name = "count_category")
    @JsonProperty("countCategory")
	private int countCategory;
	@OneToMany(mappedBy = "expenditureCategory", cascade = CascadeType.PERSIST)
	private List<CategoryAbsenceCount> listCategoryAbsenceCount;
    @JsonProperty("id_expenditure")
	private int idExpenditure;
    @Transient
    //@JsonProperty("mapAbsenceCount")
    private Map<Absence, Integer> mapAbsenceCount = new HashMap<Absence, Integer>();

	public ExpenditureCategoryCount() {
		//
	}
	
	public ExpenditureCategoryCount(Category category,
										int countCategory,
											List<CategoryAbsenceCount> listCategoryAbsenceCount,
												int idExpenditure) 
	{
		this.category = category;
		this.countCategory = countCategory;
		this.listCategoryAbsenceCount = listCategoryAbsenceCount;
		this.idExpenditure = idExpenditure;
	}
	
	public int getIdExpenditureCategoryCount() {
		return this.idExpenditureCategoryCount;
	}
		
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public Category getCategory() {
		return this.category;
	}
	
	public void setCountCategory(int countCategory) {
		this.countCategory = countCategory;
	}
	
	public int getCountCategory() {
		return this.countCategory;
	}
	
	public void setListCategoryAbsenceCount(List<CategoryAbsenceCount> listCategoryAbsenceCount) {
		this.listCategoryAbsenceCount = listCategoryAbsenceCount;
	}
	
	public List<CategoryAbsenceCount> getListCategoryAbsenceCount() {
		return this.listCategoryAbsenceCount;
	}
	
	public void setIdExpenditure(int idExpenditure) {
		this.idExpenditure = idExpenditure;
	}
	
	public int getIdExpenditure() {
		return this.idExpenditure;
	}
    
    //public Map<Absence, Integer> getMapAbsenceCount() {
    //    return this.mapAbsenceCount;
    //}
    
    //public void setMapAbsenceCount(Map<Absence, Integer> mapAbsenceCount) {
    //    this.mapAbsenceCount = mapAbsenceCount;
    //}
}