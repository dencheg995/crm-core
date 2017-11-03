package com.cafe.crm.services.impl.calculation;


import com.cafe.crm.dto.*;
import com.cafe.crm.models.client.Calculate;
import com.cafe.crm.models.client.Client;
import com.cafe.crm.models.client.Debt;
import com.cafe.crm.models.client.LayerProduct;
import com.cafe.crm.models.cost.Cost;
import com.cafe.crm.models.menu.Product;
import com.cafe.crm.models.note.Note;
import com.cafe.crm.models.shift.Shift;
import com.cafe.crm.models.shift.UserSalaryDetail;
import com.cafe.crm.models.user.Position;
import com.cafe.crm.models.user.Receipt;
import com.cafe.crm.models.user.User;
import com.cafe.crm.services.interfaces.calculation.ShiftCalculationService;
import com.cafe.crm.services.interfaces.cost.CostService;
import com.cafe.crm.services.interfaces.menu.ProductService;
import com.cafe.crm.services.interfaces.note.NoteService;
import com.cafe.crm.services.interfaces.receipt.ReceiptService;
import com.cafe.crm.services.interfaces.salary.UserSalaryDetailService;
import com.cafe.crm.services.interfaces.shift.ShiftService;
import com.cafe.crm.utils.RoundUpper;
import com.yc.easytransformer.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShiftCalculationServiceImpl implements ShiftCalculationService {
	private final CostService costService;
	private final ShiftService shiftService;
	private final Transformer transformer;
	private final NoteService noteService;
	private final ProductService productService;
	private final UserSalaryDetailService userSalaryDetailService;
	@Autowired
	private ReceiptService receiptService;

	@Autowired
	public ShiftCalculationServiceImpl(CostService costService, ShiftService shiftService, Transformer transformer,
									   NoteService noteService, ProductService productService, UserSalaryDetailService userSalaryDetailService) {
		this.costService = costService;
		this.shiftService = shiftService;
		this.transformer = transformer;
		this.noteService = noteService;
		this.productService = productService;
		this.userSalaryDetailService = userSalaryDetailService;
	}

	@Override
	public double getTotalCashBox(Set<Shift> allShiftsBetween) {
		if (!allShiftsBetween.isEmpty()) {
			Shift lastShift = new Shift();

			long maxId = 0;
			for (Shift shift : allShiftsBetween) {
				long shiftId = shift.getId();
				if (maxId == 0) {
					maxId = shiftId;
				}
				if (shiftId >= maxId) {
					maxId = shiftId;
					lastShift = shift;
				}
			}
			return lastShift.getCashBox() + lastShift.getBankCashBox();
		} else {
			return 0D;
		}
	}

	@Override
	public UserSalaryDetail getUserSalaryDetail(User user, int percent, int bonus, Shift shift) {
		int salary = user.getShiftSalary() + bonus + percent;
		int shiftSalary = user.getShiftSalary();
		int shiftAmount = user.getShifts().size();
		return new UserSalaryDetail(user, salary, shiftSalary, shiftAmount, bonus, shift);
	}

	@Override
	public TotalStatisticView createTotalStatisticView(LocalDate from, LocalDate to) {
		Set<Shift> shifts = shiftService.findByDates(from, to);
		double profit = 0D;
		double totalShiftSalary = 0D;
		double otherCosts = 0D;
		List<UserDTO> users = getUserDTOList(shifts, from, to);
		Set<Calculate> allCalculate = new HashSet<>();
		Map<Client, ClientDetails> clientsOnDetails = new HashMap<>();
		List<Cost> otherCost = new ArrayList<>();
		List<Debt> givenDebts = new ArrayList<>();
		List<Debt> repaidDebt = new ArrayList<>();
		List<Receipt> receiptAmount = new ArrayList<>();
		if (shifts == null) {
			return new TotalStatisticView(profit, totalShiftSalary, otherCosts, users, clientsOnDetails, otherCost,
					givenDebts, repaidDebt);
		}
		for (Shift shift : shifts) {
			allCalculate.addAll(shift.getCalculates());
			otherCost.addAll(costService.findByShiftId(shift.getId()));
			givenDebts.addAll(shift.getGivenDebts());
			repaidDebt.addAll(shift.getRepaidDebts());
			receiptAmount.addAll(receiptService.findByShiftId(shift.getId()));
		}
		clientsOnDetails = getClientsOnDetails(allCalculate);
		for (UserDTO user : users) {
			totalShiftSalary += user.getSalary();
		}
		for (Cost cost : otherCost) {
			otherCosts += cost.getPrice() * cost.getQuantity();
		}
		for (Map.Entry<Client, ClientDetails> entry : clientsOnDetails.entrySet()) {
			profit += entry.getValue().getAllDirtyPrice();
		}
		for (Debt repDebt : repaidDebt) {
			profit += repDebt.getDebtAmount();
		}
		for (Debt givDebt : givenDebts) {
			profit -= givDebt.getDebtAmount();
		}
		for (Receipt receipt : receiptAmount){
			profit +=receipt.getReceiptAmount();
		}

		return new TotalStatisticView(profit, totalShiftSalary, otherCosts, users, clientsOnDetails, otherCost,
				givenDebts, repaidDebt);
	}

	private List<UserDTO> getUserDTOList(Set<Shift> shifts, LocalDate from, LocalDate to) {
		List<UserDTO> userDTOList = new ArrayList<>();
		Set<User> userSet = new HashSet<>();
		for (Shift shift : shifts) {
			userSet.addAll(shift.getUsers());
		}
		for (User user : userSet) {
			List<UserSalaryDetail> details = userSalaryDetailService.findByUserIdAndShiftDateBetween(user.getId(), from, to);
			UserSalaryDetail lastDetail = details.get(details.size() - 1);
			int salary = 0;
			int bonus = 0;
			int shiftAmount = lastDetail.getShiftAmount();
			int shiftSalary = lastDetail.getShiftSalary();

			for (UserSalaryDetail detail : details) {
				salary += detail.getSalary();
				bonus += detail.getBonus();
			}

			UserDTO userDTO = transformer.transform(user, UserDTO.class);
			userDTO.setSalary(salary);
			userDTO.setShiftSalary(shiftSalary);
			userDTO.setShiftAmount(shiftAmount);
			userDTO.setBonus(bonus);
			userDTOList.add(userDTO);
		}
		return userDTOList;
	}

	private Map<Client, ClientDetails> getClientsOnDetails (Set<Calculate> allCalculate) {
		Map<Client, ClientDetails> clientsOnDetails = new HashMap<>();
		Set<Client> roundedClients = new HashSet<>();
		Set<Client> notRoundedClients = new HashSet<>();
		for (Calculate calculate : allCalculate) {
			if (calculate.isRoundState()) {
				roundedClients.addAll(calculate.getClient());
			} else {
				notRoundedClients.addAll(calculate.getClient());
			}
		}
		for (Client roundClient : roundedClients) {
			ClientDetails details = getClientDetails(roundClient, true);
			clientsOnDetails.put(roundClient, details);
		}
		for (Client notRoundClient : notRoundedClients) {
			ClientDetails details = getClientDetails(notRoundClient, false);
			clientsOnDetails.put(notRoundClient, details);
		}
		return clientsOnDetails;
	}

	private ClientDetails getClientDetails(Client client, boolean isRounded) {
		Double allDirtyPrice;
		Double dirtyPriceMenu = 0D;
		Double otherPriceMenu = 0D;
		for (LayerProduct product : client.getLayerProducts()) {
			if (product.isDirtyProfit()) {
				dirtyPriceMenu += product.getCost() / product.getClients().size();
			} else {
				otherPriceMenu += product.getCost() / product.getClients().size();
			}
		}
		allDirtyPrice = client.getPriceTime() + Math.round(dirtyPriceMenu) - client.getPayWithCard();
		if (isRounded) {
			allDirtyPrice = RoundUpper.roundDouble(allDirtyPrice);
			dirtyPriceMenu = RoundUpper.roundDouble(dirtyPriceMenu);
			otherPriceMenu = RoundUpper.roundDouble(otherPriceMenu);
		}

		return new ClientDetails(allDirtyPrice, Math.round(otherPriceMenu), Math.round(dirtyPriceMenu));
	}

	@Override
	public DetailStatisticView createDetailStatisticView(Shift shift) {
		LocalDate shiftDate = shift.getShiftDate();
		Double cashBox = shift.getCashBox() + shift.getBankCashBox();
		Double allPrice = getAllPrice(shift);
		int clientsNumber = shift.getClients().size();
		List<UserDTO> usersOnShift = getUserDTOList(shift);
		List<UserSalaryDetail> salaryDetails = shift.getUserSalaryDetail();
		Set<Calculate> allCalculate = shift.getCalculates();
		List<Cost> otherCost = costService.findByDateAndVisibleTrue(shift.getShiftDate());
		Double allSalaryCost = 0D;
		Double allOtherCost = 0D;

		for (UserSalaryDetail detail : salaryDetails) {
			allSalaryCost += detail.getSalary();
		}
		for (Cost otherGood : otherCost) {
			allOtherCost = allOtherCost + otherGood.getPrice() * otherGood.getQuantity();
		}

		return new DetailStatisticView(shiftDate, cashBox, allPrice, clientsNumber,
				usersOnShift, salaryDetails, allCalculate, allSalaryCost, allOtherCost, otherCost);
	}

	private List<UserDTO> getUserDTOList(Shift shift) {
		List<UserDTO> userDTOList = new ArrayList<>();
		for (User user : shift.getUsers()) {
			UserSalaryDetail shiftUserDetails = userSalaryDetailService.findFirstByUserIdAndShiftId(user.getId(), shift.getId());
			UserDTO userDTO = transformer.transform(user, UserDTO.class);
			if (shiftUserDetails != null) {
				userDTO.setSalary(shiftUserDetails.getSalary());
				userDTO.setShiftSalary(shiftUserDetails.getShiftSalary());
				userDTO.setShiftAmount(shiftUserDetails.getShiftAmount());
				userDTO.setBonus(shiftUserDetails.getBonus());
			}
			userDTOList.add(userDTO);
		}
		return userDTOList;
	}

	@Override
	public ShiftView createShiftView(Shift shift) {
		List<UserDTO> usersOnShift = transformer.transform(shift.getUsers(), UserDTO.class);
		Set<Client> clients = new HashSet<>();
		List<Calculate> activeCalculate = new ArrayList<>();
		Set<Calculate> allCalculate = shift.getCalculates();
		List<Note> enabledNotes = noteService.findAllByEnableIsTrue();
		Double cashBox = shift.getCashBox();
		Double bankCashBox = shift.getBankCashBox();
		Double totalCashBox;
		int usersTotalShiftSalary = 0;
		Double card = 0D;
		Double allPrice = getAllPrice(shift);

		for (Calculate calculate : allCalculate) {
			if (!calculate.isState()) {
				clients.addAll(calculate.getClient());
			} else {
				activeCalculate.add(calculate);
			}
		}

		Set<LayerProduct> layerProducts = new HashSet<>();

		for (Client client : clients) {
			layerProducts.addAll(client.getLayerProducts());
		}

		Map<Long, Integer> staffPercentBonusesMap = calcStaffPercentBonusesAndGetMap(layerProducts, usersOnShift);


		LocalDate shiftDate = shift.getShiftDate();
		List<Cost> costWithoutUsersSalaries = costService.findByShiftId(shift.getId());
		double otherCosts = 0d;

		for (Cost cost : costWithoutUsersSalaries) {
			otherCosts += (cost.getPrice() * cost.getQuantity());
		}

		for (UserDTO user : usersOnShift) {
			int amountOfPositionsPercent = user.getPositions().stream().filter(PositionDTO::isPositionUsePercentOfSales).mapToInt(PositionDTO::getPercentageOfSales).sum();
			user.setShiftSalary((int) (user.getShiftSalary() + (allPrice * amountOfPositionsPercent) / 100));
			usersTotalShiftSalary += user.getShiftSalary();
		}

		totalCashBox = (cashBox + bankCashBox + allPrice) - (usersTotalShiftSalary + otherCosts);

		return new ShiftView(usersOnShift, clients, activeCalculate, allCalculate,
				cashBox, totalCashBox, usersTotalShiftSalary, card, allPrice, shiftDate, otherCosts, bankCashBox, enabledNotes, staffPercentBonusesMap);
	}

	private double getAllPrice(Shift shift) {
		Set<Calculate> allCalculate = shift.getCalculates();
		Set<Client> roundedClients = new HashSet<>();
		Set<Client> notRoundedClients = new HashSet<>();
		List<Receipt> receiptAmount = receiptService.findByShiftId(shift.getId());
		double allPrice = 0D;
		for (Calculate calculate : allCalculate) {
			if (!calculate.isState()) {
				if (calculate.isRoundState()) {
					roundedClients.addAll(calculate.getClient());
				} else {
					notRoundedClients.addAll(calculate.getClient());
				}
			}
		}
		for (Client client : roundedClients) {
			allPrice += RoundUpper.roundDouble(getAllDirtyPrice(client));
		}

		for (Client client : notRoundedClients) {
			allPrice += getAllDirtyPrice(client);
		}
		for (Debt debt : shift.getRepaidDebts()) {
			allPrice += debt.getDebtAmount();
		}
		for (Debt debt : shift.getGivenDebts()) {
			allPrice -= debt.getDebtAmount();
		}
		for (Receipt receipt : receiptAmount){
			allPrice +=receipt.getReceiptAmount();
		}
		return allPrice;
	}

	private Double getAllDirtyPrice(Client client) {
		Double dirtyPriceMenu = 0D;
		for (LayerProduct product : client.getLayerProducts()) {
			if (product.isDirtyProfit())
				dirtyPriceMenu += product.getCost() / product.getClients().size();
		}
		return client.getPriceTime() + Math.round(dirtyPriceMenu) - client.getPayWithCard();
	}

	private Map<Long, Integer> calcStaffPercentBonusesAndGetMap(Set<LayerProduct> layerProducts, List<UserDTO> staff) {
		Map<Long, Integer> staffPercentBonusesMap = new HashMap<>();

		for (LayerProduct layerProduct : layerProducts) {

			Long productId = layerProduct.getProductId();
			Product product = productService.findOne(productId);
			Map<Position, Integer> staffPercent = product.getStaffPercent();

			for (UserDTO user : staff) {
				List<PositionDTO> userPositions = user.getPositions();

				for (PositionDTO positionDTO : userPositions) {

					Integer percent = staffPercent.get(transformer.transform(positionDTO, Position.class));
					if (percent != null) {

						int bonus = (int) (layerProduct.getCost() * percent / 100);
						user.setShiftSalary(bonus + user.getShiftSalary());

						Integer saveBonus = staffPercentBonusesMap.get(user.getId());
						if (saveBonus == null) {
							staffPercentBonusesMap.put(user.getId(), bonus);
						} else {
							staffPercentBonusesMap.put(user.getId(), bonus + saveBonus);
						}
					}
				}

			}
		}
		return staffPercentBonusesMap;
	}

	@Override
	public void transferFromBankToCashBox(Double transfer) {
		Shift lastShift = shiftService.getLast();
		Double bankCashBox = lastShift.getBankCashBox() + transfer;
		Double cashBox = lastShift.getCashBox() - transfer;

		lastShift.setCashBox(cashBox);
		lastShift.setBankCashBox(bankCashBox);
		shiftService.saveAndFlush(lastShift);
	}

	@Override
	public void transferFromCashBoxToBank(Double transfer) {
		Shift lastShift = shiftService.getLast();
		Double bankCashBox = lastShift.getBankCashBox() - transfer;
		Double cashBox = lastShift.getCashBox() + transfer;

		lastShift.setCashBox(cashBox);
		lastShift.setBankCashBox(bankCashBox);
		shiftService.saveAndFlush(lastShift);
	}

}
