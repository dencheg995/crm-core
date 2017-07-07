package com.cafe.crm.services.impl.menu;

import com.cafe.crm.models.menu.Menu;
import com.cafe.crm.repositories.menu.MenuRepository;
import com.cafe.crm.services.interfaces.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MenuServiceImpl implements MenuService {

	@Autowired
	private MenuRepository menuRepository;

	@Override
	public Menu getOne(Long id) {
		return menuRepository.findOne(id);
	}

}
