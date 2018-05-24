package com.cafe.crm.services.impl.debt;


import com.cafe.crm.dto.DebtDTO;
import com.cafe.crm.exceptions.debt.DebtDataException;
import com.cafe.crm.models.client.Debt;
import com.cafe.crm.models.company.Company;
import com.cafe.crm.models.shift.Shift;
import com.cafe.crm.repositories.debt.DebtRepository;
import com.cafe.crm.services.interfaces.company.CompanyService;
import com.cafe.crm.services.interfaces.debt.DebtService;
import com.cafe.crm.services.interfaces.shift.ShiftService;
import com.cafe.crm.utils.CompanyIdCache;
import com.yc.easytransformer.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DebtServiceImpl implements DebtService {

	private final DebtRepository repository;
	private final ShiftService shiftService;
	private final CompanyService companyService;
	private final CompanyIdCache companyIdCache;
	private final Transformer transformer;

	@Autowired
	public DebtServiceImpl(DebtRepository repository, ShiftService shiftService, CompanyService companyService,
						   CompanyIdCache companyIdCache, Transformer transformer) {
		this.repository = repository;
		this.shiftService = shiftService;
		this.companyService = companyService;
		this.companyIdCache = companyIdCache;
		this.transformer = transformer;
	}

	private void setCompany(Debt debt) {
		Long companyId = companyIdCache.getCompanyId();
		Company company = companyService.findOne(companyId);
		debt.setCompany(company);
	}

	@Override
	public Debt save(Debt debt) {
		if (debt.getDebtAmount() > 0) {
			setCompany(debt);
			return repository.save(debt);
		} else {
			throw new DebtDataException("Введена недопустимая сумма долга");
		}
	}

	@Override
	public void saveAll(List<Debt> debts) {
		repository.save(debts);
	}


	@Override
	public void delete(Debt debt) {
		repository.delete(debt);
	}

	@Override
	public void delete(Long id) {
		repository.delete(id);
	}

	@Override
	public Debt get(Long id) {
		return repository.findOne(id);
	}

	@Override
	public List<Debt> getAll() {
		return repository.findByCompanyId(companyIdCache.getCompanyId());
	}

	@Override
	public List<Debt> findByVisibleIsTrueAndDateBetween(LocalDate from, LocalDate to) {
		return repository.findByVisibleIsTrueAndDateBetweenAndCompanyId(from, to, companyIdCache.getCompanyId());
	}

	@Override
	public List<Debt> findOtherDebtByVisibleIsTrueAndDateBetween(LocalDate from, LocalDate to) {
		return repository.findByVisibleIsTrueAndDateBetweenAndCompanyIdAndCashBoxDebtIsFalse(from, to, companyIdCache.getCompanyId());
	}

	@Override
	public List<Debt> findCashBoxDebtByVisibleIsTrueAndDateBetween(LocalDate from, LocalDate to) {
		return repository.findByVisibleIsTrueAndDateBetweenAndCompanyIdAndCashBoxDebtIsTrue(from, to, companyIdCache.getCompanyId());
	}

	@Override
	public void offVisibleStatus(Debt debt) {
		debt.setVisible(false);
		repository.save(debt);
	}

	@Override
	public List<Debt> findByDebtorAndDateBetween(String debtor, LocalDate from, LocalDate to) {
		return repository.findByDebtorAndDateBetweenAndCompanyId(debtor, from, to, companyIdCache.getCompanyId());
	}

	@Override
	public List<Debt> findOtherDebtByDebtorAndDateBetween(String debtor, LocalDate from, LocalDate to) {
		return repository.findByDebtorAndDateBetweenAndCompanyIdAndCashBoxDebtIsFalse(debtor, from, to, companyIdCache.getCompanyId());
	}

	@Override
	public List<Debt> findCashBoxDebtByDebtorAndDateBetween(String debtor, LocalDate from, LocalDate to) {
		return repository.findByDebtorAndDateBetweenAndCompanyIdAndCashBoxDebtIsTrue(debtor, from, to, companyIdCache.getCompanyId());
	}

	@Override
	public Debt repayDebt(Long id) {
		Shift lastShift = shiftService.getLast();
		Debt debt = repository.findOne(id);
		//todo repaired debts
		lastShift.addRepaidDebtToList(debt);
		Shift shiftWithDebt = debt.getShift();
		if (lastShift.equals(shiftWithDebt)) {
			//todo given debts
			shiftWithDebt.getGivenDebts().remove(debt);
		}
		shiftService.saveAndFlush(lastShift);
		offVisibleStatus(debt);
		return debt;
	}

	@Override
	public List<Debt> findByCalculateId(Long calculateId) {
		return repository.findByCalculateId(calculateId);
	}

	@Override
	public List<DebtDTO> transformDebtsWithOutShiftAndCalc(List<Debt> debts) {
		List<DebtDTO> debtsDTOS = new ArrayList<>(debts.size());

		for (Debt debt : debts) {
			DebtDTO dto = transformer.transform(debt, DebtDTO.class);
			dto.setDate(debt.getDate());
			debtsDTOS.add(dto);
		}

		return debtsDTOS;
	}

	@Override
	public Debt addDebtOnLastShift(Debt debt) {
		Shift lastShift = shiftService.getLast();

		debt.setShift(lastShift);
		//todo given debts
		lastShift.addGivenDebtToList(debt);

		return save(debt);
	}
}
