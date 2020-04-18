package com.yura.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;
import javax.persistence.ForeignKey;
import javax.persistence.GenerationType;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

import java.util.ArrayList;
import java.util.List;

import com.yura.entity.Expenditure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Transient;

@Entity
@Table(
    name = "subdivisions",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"name"}, name = "s_1")		
    }
)
public class Subdivision {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subdivision")
	@JsonProperty("idSubdivision")
    private int idSubdivision;
    @Column(name = "name")
	@JsonProperty("name")
    private String name;
    @ManyToOne()
    @JoinColumn(name = "fk_id_subdivision", referencedColumnName = "id_subdivision", foreignKey = @ForeignKey(name = "s_2"), nullable = true)
	@JsonProperty("parent")
    private Subdivision parent;
    @OneToMany(mappedBy = "parent")
	@JsonIgnore()
    private List<Subdivision> children = new ArrayList<Subdivision>();
    @OneToMany(mappedBy = "subdivision")
	@JsonIgnore()
    private List<UserSubdivision> listUserSubdivision;
    @Transient
	@JsonIgnore()
    private List<Expenditure> listExpenditure;
    
    public Subdivision() {
		//
    }

    public Subdivision(String name) {
        this.name = name;
        this.parent = null;
    }

    public Subdivision(String name, Subdivision parent) {
        if (parent == null) {
            this.parent = null;
        } else {
            this.parent = parent;
            parent.getChildren().add(this);
        }
        this.name = name;
    }

    public int getIdSubdivision() {
        return this.idSubdivision;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setParent(Subdivision parent) {
        if (this.parent == null && parent == null) {
            // нихуя не делать
        }
        if(this.parent != null && parent == null) {
            this.parent.getChildren().remove(this);
            this.parent = null;
        }
        if(this.parent == null && parent != null) {
            this.parent = parent;
			if (parent.getChildren() != null)
				parent.getChildren().add(this);
        }
        if(this.parent != null && parent != null) {
			this.parent = parent;
			if (parent.getChildren() != null) {
				parent.getChildren().add(this);
			} else {
				parent.children = new ArrayList<Subdivision>();
				parent.getChildren().add(this);
			}
        } else {
        
        }
    }

    public Subdivision getParent() {
        return this.parent;
    }

    public void setChildren(List<Subdivision> children) {
        this.children = children;
    }

    public List<Subdivision> getChildren() {
        return this.children;
    }

    public void addChild(Subdivision child) {
        this.children.add(child);
        child.parent.setParent(this);
    }   
    
    public void setListUserSubdivision(List<UserSubdivision> listUserSubdivision) {
    	this.listUserSubdivision = listUserSubdivision;
    } 
    
    public List<UserSubdivision> getListUserSubdivision() {
    	return this.listUserSubdivision;
    }
    
	@JsonIgnore()
    public List<User> getUsers() {
    	List<User> users = new ArrayList<User>();
    	for(UserSubdivision userSubdivision : this.listUserSubdivision) {
    		users.add(userSubdivision.getUser());
    	}
    	return users;
    }
    
    public List<Expenditure> getListExpenditure() {
        return this.listExpenditure;
    }
    
    public void setListExpenditure(List<Expenditure> listExpenditure) {
        this.listExpenditure = listExpenditure;
    }
}