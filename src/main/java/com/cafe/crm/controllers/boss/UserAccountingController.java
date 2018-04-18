package com.cafe.crm.controllers.boss;

import com.cafe.crm.dto.ExtraUserData;
import com.cafe.crm.exceptions.user.PositionDataException;
import com.cafe.crm.exceptions.user.UserDataException;
import com.cafe.crm.models.shift.Shift;
import com.cafe.crm.models.user.Position;
import com.cafe.crm.models.user.Role;
import com.cafe.crm.models.user.User;
import com.cafe.crm.services.interfaces.calculation.ShiftCalculationService;
import com.cafe.crm.services.interfaces.position.PositionService;
import com.cafe.crm.services.interfaces.role.RoleService;
import com.cafe.crm.services.interfaces.shift.ShiftService;
import com.cafe.crm.services.interfaces.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class UserAccountingController {

	private final UserService userService;
	private final PositionService positionService;
	private final RoleService roleService;
	private final ShiftService shiftService;
	private final ShiftCalculationService shiftCalculationService;

	@Autowired
	public UserAccountingController(UserService userService, PositionService positionService, RoleService roleService,
									ShiftService shiftService, ShiftCalculationService shiftCalculationService) {
		this.userService = userService;
		this.positionService = positionService;
		this.roleService = roleService;
		this.shiftService = shiftService;
		this.shiftCalculationService = shiftCalculationService;
	}

	@RequestMapping(value = {"/boss/user/accounting"}, method = RequestMethod.GET)
	public String showAllUser(Model model) {
		List<User> allUsers = userService.findAll();
		Map<Position, List<User>> usersByPositions = userService.findAndSortUserByPosition();
		List<Position> allPositions = positionService.findAll();
		List<Role> allRoles = roleService.findAll();

		model.addAttribute("allUsers", allUsers);
		model.addAttribute("usersByPositions", usersByPositions);
		model.addAttribute("allPositions", allPositions);
		model.addAttribute("allRoles", allRoles);

		return "userAccounting/userAccounting";
	}

	@RequestMapping(value = {"/boss/user/add"}, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> addUser(@ModelAttribute @Valid User user, BindingResult bindingResult,
									 @RequestParam(name = "positionsIds") String positionsIds,
									 @RequestParam(name = "rolesIds") String rolesIds,
									 @RequestParam(name = "isDefaultPassword", required = false) String isDefaultPassword) {
		if (bindingResult.hasErrors()) {
			String fieldError = bindingResult.getFieldError().getDefaultMessage();
			throw new UserDataException("Не удалось добавить пользователя!\n" + fieldError);
		}
		userService.save(user, positionsIds, rolesIds, isDefaultPassword);
		return ResponseEntity.ok("Пользователь успешно обновлен!");
	}

	@RequestMapping(value = {"/boss/user/edit"}, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> editUser(@ModelAttribute @Valid User user, BindingResult bindingResult,
									  ExtraUserData extraUserData) {
		if (bindingResult.hasErrors()) {
			String fieldError = bindingResult.getFieldError().getDefaultMessage();
			throw new UserDataException("Не удалось изменить данные пользователя!\n" + fieldError);
		}
		userService.update(user, extraUserData);
		return ResponseEntity.ok("Пользователь успешно обновлен!");
	}

	@RequestMapping(value = {"/boss/user/delete/{id}"}, method = RequestMethod.POST)
	public String deleteUser(@PathVariable("id") Long id) throws IOException {
		userService.delete(id);
		return "redirect:/boss/user/accounting";
	}

	@RequestMapping(value = {"/boss/user/position/add"}, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> addPosition(Position position) throws IOException {
		positionService.save(position);
		return ResponseEntity.ok("Должность успешно добавлена!");
	}

	@RequestMapping(value = {"/boss/user/position/edit"}, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> editPosition(Position position) throws IOException {
		positionService.update(position);
		return ResponseEntity.ok("Должность успешно обновлена!");
	}

	@RequestMapping(value = {"/boss/user/position/delete/{id}"}, method = RequestMethod.GET)
	public String deletePosition(@PathVariable("id") Long id) throws IOException {
		positionService.delete(id);
		return "redirect:/boss/user/accounting";
	}

	@RequestMapping(value = {"/boss/user/get-all"}, method = RequestMethod.POST)
	@ResponseBody
	public List<User> getAllUsers() {
		List<User> users = userService.findAll();
		if (users == null) {
			throw new UserDataException("В системе нет ни одного пользователя!");
		}
		return users;
	}

	@RequestMapping(value = {"/boss/user/get-salary-clients"}, method = RequestMethod.POST)
	@ResponseBody
	public List<User> outputClients(@RequestParam(name = "clientsId", required = false) long[] clientsId) {
		if (clientsId == null || clientsId.length == 0) {
			throw new UserDataException("Выберите работников для выдачи зарплаты!");
		}
		List<User> salaryUsers = userService.findByIdIn(clientsId);
		if (salaryUsers == null) {
			throw new UserDataException("Выбраны несуществующие работники!");
		}
		return salaryUsers;
	}

	@RequestMapping(value = {"/boss/user/pay-salaries"}, method = RequestMethod.POST)
	@ResponseBody
	public List<User> paySalary(@RequestParam(name = "clientsId", required = false) long[] clientsId) {

		if (clientsId == null || clientsId.length == 0) {
			throw new UserDataException("Выберите работников для выдачи зарплаты!");
		}
		List<User> allUsers = getAllUsers();
		List<User> salaryUsers = userService.findByIdIn(clientsId);

		if (salaryUsers == null) {
			throw new UserDataException("Выбраны несуществующие работники!");
		}

		shiftCalculationService.paySalary(salaryUsers);

		return allUsers;
	}

	@ExceptionHandler(value = UserDataException.class)
	public ResponseEntity<?> handleUserUpdateException(UserDataException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	@ExceptionHandler(value = PositionDataException.class)
	public ResponseEntity<?> handleUserUpdateException(PositionDataException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
}
