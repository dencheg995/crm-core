package com.cafe.crm.models.client;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "cards")
public class Card {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	private String token;

	private String name;

	private String surname;

	private String photo;

	private Long discount = (long)0;

	private Double balance = 0.0;

	private Double spend = 0.0;

	private LocalDate visitDate;

	@OneToMany(fetch = FetchType.EAGER, targetEntity = Calculate.class)
	@JoinTable(name = "cards_calculations",
			joinColumns = {@JoinColumn(name = "card_id")},
			inverseJoinColumns = {@JoinColumn(name = "calculate_id")})
	private List<Calculate> calculates;

	public Card() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Card card = (Card) o;

		if (id != null ? !id.equals(card.id) : card.id != null) return false;
		return name != null ? name.equals(card.name) : card.name == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public String getPhoto() {
		return photo;
	}

	public Long getDiscount() {
		return discount;
	}

	public Double getBalance() {
		return balance;
	}

	public Double getSpend() {
		return spend;
	}

	public LocalDate getVisitDate() {
		return visitDate;
	}

	public List<Calculate> getCalculates() {
		return calculates;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public void setDiscount(Long discount) {
		this.discount = discount;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public void setSpend(Double spend) {
		this.spend = spend;
	}

	public void setVisitDate(LocalDate visitDate) {
		this.visitDate = visitDate;
	}

	public void setCalculates(List<Calculate> calculates) {
		this.calculates = calculates;
	}
}
