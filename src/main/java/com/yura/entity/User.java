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
import javax.persistence.FetchType;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"name"}, name = "u_1")		
	}
)
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_user")
	@JsonProperty("idUser")
	private int idUser;
	@Column(name = "name")
	@JsonProperty("name")
    private String name;
    @Column(name = "role_system")
	@JsonProperty("roleSystem")
    private String roleSystem;
    @Column(name = "password")
	@JsonProperty("password")
    private String password;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
	@JsonProperty("listUserSubdivision")
    private List<UserSubdivision> listUserSubdivision;
    
    public User() {
    	//
    }
    
    public User(String name, String roleSystem, String password) {
    	this.name = name;
    	this.roleSystem = roleSystem;
    	this.password = password;
    }
	
	public User(String name, String roleSystem, String password, List<UserSubdivision> listUserSubdivision) {
		this.name = name;
		this.roleSystem = roleSystem;
		this.password = password;
		this.listUserSubdivision = listUserSubdivision;
	}
    
    
    public int getIdUser() {
    	return this.idUser;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public void setRoleSystem(String roleSystem) {
    	this.roleSystem = roleSystem;
    }
    
    public String getRoleSystem() {
    	return this.roleSystem;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }
    
    public String getPassword() {
    	return this.password;
    }
    
    public void setListUserSubdivision(List<UserSubdivision> listUserSubdivision) {
    	this.listUserSubdivision = listUserSubdivision;
    }
    
    public List<UserSubdivision> getListUserSubdivision() {
    	return this.listUserSubdivision;
    }
    
    public List<Subdivision> getListSubdivision() {
    	List<Subdivision> listSubdivision = new ArrayList<Subdivision>();
    	for(UserSubdivision userSubdivision : this.listUserSubdivision) {
    		listSubdivision.add(userSubdivision.getSubdivision());
    	}
    	return listSubdivision;
    }
    
    // проверка может ли пользователь просматривать подразделение
    public boolean isMySubdivision(String nameSubdivision) {
    	int flag = 0;
    	List<Subdivision> listSubdivision = getListSubdivision();
    	for(Subdivision subdivision : listSubdivision) {
    		if (subdivision.getName().equals(nameSubdivision)) {
    			flag = 1;
    			break;
    		}	
    	}
    	if (flag == 1) {
    		return true;
    	} else {
    		return false;
    	}
    } 
    
    // проверка может ли пользователь редактировать подразделение
	public boolean isMySubdivisionIsEdit(String nameSubdivision) {
		int flag = 0;
		for(UserSubdivision userSubdivision : this.listUserSubdivision) {
			if (userSubdivision.getSubdivision().getName().equals(nameSubdivision) && userSubdivision.getIsEdit() == true) {
				flag = 1;
				break;
			}
		}
		if (flag == 1) { 
			return true;
		} else {
			return false;
		}			 
	}
	
	public List<String> getSubdivisionIsEditInformation() {
		List<String> information = new ArrayList<String>();
		for (UserSubdivision userSubdivision : this.listUserSubdivision) {
			if (userSubdivision.getIsEdit() == true) {
				String str = userSubdivision.getSubdivision().getName() + "->" + "может ред.";
				information.add(str);
			} else {
				String str = userSubdivision.getSubdivision().getName() + "->" + "нет";
				information.add(str);
			}
		}
		return information;
	}
}