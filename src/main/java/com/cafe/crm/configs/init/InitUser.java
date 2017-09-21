package com.cafe.crm.configs.init;

import com.cafe.crm.models.user.Position;
import com.cafe.crm.models.user.Role;
import com.cafe.crm.models.user.User;
import com.cafe.crm.services.interfaces.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class InitUser {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public InitUser(PasswordEncoder passwordEncoder, UserService userService) {
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}

	private void forTest() {
		Position adminPosition = new Position("Администратор");
		Position bossPosition = new Position("Владелец");
		Position workerPosition = new Position("Чайник");

		List<Position> adminPositionsList = new ArrayList<>();
		List<Position> bossPositionsList = new ArrayList<>();
		List<Position> workerPositionsList = new ArrayList<>();
		adminPositionsList.add(adminPosition);
		bossPositionsList.add(bossPosition);
		workerPositionsList.add(workerPosition);

		Role managerRole = new Role("MANAGER");
		Role bossRole = new Role("BOSS");
		Role workerRole = new Role("WORKER");
		List<Role> managerRoles = new ArrayList<>();
		List<Role> bossRoles = new ArrayList<>();
		List<Role> workerRoles = new ArrayList<>();
		managerRoles.add(managerRole);
		bossRoles.add(bossRole);
		workerRoles.add(workerRole);

		User manager = new User();
		manager.setPassword(passwordEncoder.encode("manager"));
		manager.setActivated(true);
		manager.setFirstName("Anna");
		manager.setLastName("Jons");
		manager.setShiftSalary(1500);
		manager.setEmail("manager@mail.ru");
		manager.setPhone("89233456789");
		manager.setPositions(adminPositionsList);
		manager.setRoles(managerRoles);

		User boss = new User();
		boss.setPassword(passwordEncoder.encode("boss"));
		boss.setActivated(true);
		boss.setFirstName("Герман");
		boss.setLastName("Севостьянов");
		boss.setShiftSalary(2000);
		boss.setEmail("boss@mail.ru");
		boss.setPhone("89123456789");
		boss.setPositions(bossPositionsList);
		boss.setRoles(bossRoles);

		User worker = new User();
		worker.setPassword(passwordEncoder.encode("worker"));
		worker.setActivated(true);
		worker.setFirstName("Ахмад");
		worker.setLastName("Чай");
		worker.setEmail("worker@mail.ru");
		worker.setPhone("89111111111");
		worker.setPositions(workerPositionsList);
		worker.setRoles(workerRoles);

		userService.save(boss);
		userService.save(manager);
		userService.save(worker);
	}

	private void forGerman() {

		User admin = new User();
		admin.setPassword(passwordEncoder.encode("admin"));
		admin.setActivated(true);
		admin.setFirstName("Admin");
		admin.setLastName("admin");
		admin.setEmail("admin@mail.ru");
		admin.setPhone("89111111111");

		List<Role> bossRoles = new ArrayList<>();
		bossRoles.add(new Role("BOSS"));
		admin.setRoles(bossRoles);

		List<Position> bossPositionsList = new ArrayList<>();
		bossPositionsList.add(new Position("Владелец"));
		admin.setPositions(bossPositionsList);

		userService.save(admin);

	}

	//@PostConstruct
	public void init() {
		forTest();
	}


}
