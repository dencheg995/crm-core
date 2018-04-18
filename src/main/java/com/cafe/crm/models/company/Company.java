package com.cafe.crm.models.company;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "companies")
public class Company {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	private boolean isConfigured = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

	public void setConfigured(boolean configured) {
		isConfigured = configured;
	}
}