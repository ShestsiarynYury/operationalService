package com.yura.entity;

import java.util.List;
import java.util.Map;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(
	name = "titles",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "name", name = "t_1")
	}
)
public class Title {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @Column(name = "id_title")
    @JsonProperty("idTitle")
    private int id;
    @Column(name = "name")
    @JsonProperty("name")
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "title", cascade = CascadeType.ALL)
    private List<Category> listCategory;
    @Transient
    private Map<Category, int[]> mapCategoryAndCountByExpenditure;
    
	public Title() {
		//
	}
	
	public Title(String name) {
		this.name = name;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setListCategory(List<Category> listCategory) {
		this.listCategory = listCategory;
	}
	
	public List<Category> getListCategory() {
		return this.listCategory;
	}

    public void setMapCategoryAndCountByExpenditure(Map<Category, int[]> mapCategoryAndCountByExpenditure) {
        this.mapCategoryAndCountByExpenditure = mapCategoryAndCountByExpenditure;
    }
    
    public Map<Category, int[]> getMapCategoryAndCountByExpenditure() {
        return this.mapCategoryAndCountByExpenditure;
    }
    
}