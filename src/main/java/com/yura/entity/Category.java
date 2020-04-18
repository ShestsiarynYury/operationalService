package com.yura.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(
	name = "categories",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"name"}, name = "c_1")
	}
)
public class Category {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_category")
	@JsonProperty("idCategory")
	private int idCategory;
	@Column(name = "name")
	@JsonProperty("name")
	private String name;
	@ManyToOne(cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
	@JoinColumn(name = "fk_id_title", referencedColumnName = "id_title", foreignKey = @ForeignKey(name = "c_2"), nullable = false)
	@JsonProperty("title")
	private Title title;
	@OneToMany(mappedBy = "category", cascade = {CascadeType.ALL})
	private List<CategoryAbsence> listCategoryAbsence;
	@OneToMany(mappedBy = "category")
	private List<ExpenditureCategoryCount> listExpenditureCategoryCount;
	
	public Category() {
		//
	}
	
	public Category(String name) {
		this.name = name;
	}
	
	public Category(String name, Title title) {
		this.name = name;
		this.title = title;
	}
	
	public int getIdCategory() {
		return this.idCategory;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setTitle(Title title) {
		this.title = title;
	}
	
	public Title getTitle() {
		return this.title;
	}
	
	public void setListCategoryAbsence(List<CategoryAbsence> listCategoryAbsence) {
		this.listCategoryAbsence = listCategoryAbsence;
	}
	
	public List<CategoryAbsence> getListCategoryAbsence() {
		return this.listCategoryAbsence;
	}
    
    public void setListExpenditureCategoryCount(List<ExpenditureCategoryCount> listExpenditureCategoryCount) {
        this.listExpenditureCategoryCount = listExpenditureCategoryCount;
    }
    
    public List<ExpenditureCategoryCount> getListExpenditureCategoryCount() {
        return this.listExpenditureCategoryCount;
    }
		
	public List<Absence> getListAbsence() {
		List<Absence> listAbsence = new ArrayList<Absence>();
		for (CategoryAbsence categoryAbsence : this.listCategoryAbsence) {
			listAbsence.add(categoryAbsence.getAbsence());
		}
		return listAbsence;
	}

	// testing *****************************************
	@Transient
	private Map<String, Integer> mapAbsenceAndCountAbsence;

	public void setMapAbsenceAndCountAbsence(Map<String, Integer> mapAbsenceAndCountAbsence) {
		this.mapAbsenceAndCountAbsence = mapAbsenceAndCountAbsence;
	}

	public Map<String, Integer> getMapAbsenceAndCountAbsence() {
		return this.mapAbsenceAndCountAbsence;
	}
	// *************************************************
}