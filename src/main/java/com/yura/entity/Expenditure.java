package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ForeignKey;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;

import java.sql.Timestamp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.yura.entity.Subdivision;
import java.util.ArrayList;
import javax.persistence.Transient;

@Entity
@Table(
	name = "expenditures"
)
public class Expenditure {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_expenditure")
    @JsonProperty("idExpenditure")
	private int idExpenditure;
	@Column(name = "date_and_time")
    @JsonProperty("dateAndTime")
	private Timestamp dateAndTime;
	@Column(name = "type_expenditure")
    @JsonProperty("typeExpenditure")
	private String typeExpenditure;
    @Column(name = "name_subdivision")
    @JsonProperty("nameSubdivision")
	private String nameSubdivision;
    @Transient
    @JsonProperty("listExpenditureCategoryCount")
	private List<ExpenditureCategoryCount> listExpenditureCategoryCount = new ArrayList<ExpenditureCategoryCount>();
	
	public Expenditure() {
	
	}
	
	public Expenditure(Timestamp dateAndTime, 
                           String typeExpenditure, 
                               String nameSubdivision, 
                                   List<ExpenditureCategoryCount> listExpenditureCategory) {
		this.dateAndTime = dateAndTime;
		this.typeExpenditure = typeExpenditure;
		this.nameSubdivision = nameSubdivision;
		this.listExpenditureCategoryCount = listExpenditureCategoryCount;
	}
	
	public int getIdExpenditure() {
		return this.idExpenditure;
	}
	
	public void setDateAndTime(Timestamp dateAndTime) {
		this.dateAndTime = dateAndTime;
	}
	
	public Timestamp getDateAndTime() {
		return this.dateAndTime;
	}
	
	public void setTypeExpenditure(String typeExpenditure) {
		this.typeExpenditure = typeExpenditure;
	}
	
	public String getTypeExpenditure() {
		return this.typeExpenditure;
	}
	
	public void setNameSubdivision(String nameSubdivision) {
		this.nameSubdivision = nameSubdivision;
	}
	
	public String getNameSubdivision() {
		return this.nameSubdivision;
	}
	
	public void setListExpenditureCategoryCount(List<ExpenditureCategoryCount> listExpenditureCategoryCount) {
		this.listExpenditureCategoryCount = listExpenditureCategoryCount;
	}
	
	public List<ExpenditureCategoryCount> getListExpenditureCategoryCount() {
		return this.listExpenditureCategoryCount;
	}
}