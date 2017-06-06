package com.cafe.crm.controllers.boss;

import com.cafe.crm.models.property.Property;
import com.cafe.crm.models.property.PropertyWrapper;
import com.cafe.crm.models.shift.Shift;
import com.cafe.crm.service_abstract.property.PropertyService;
import com.cafe.crm.service_abstract.shift_service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/boss/statistics")
public class ShiftStatistics {

	@Autowired
	private ShiftService shiftService;

	@Autowired
	private PropertyService propertyService;

	@ModelAttribute(value = "properties")
	public List<Property> addProperties() {

		return propertyService.findAll() ;
	}
	@ModelAttribute(value = "wrapper")
	public PropertyWrapper addClass() {
		PropertyWrapper PropertyWrapper = new PropertyWrapper();
		PropertyWrapper.setProperties(propertyService.findAll());
		return PropertyWrapper;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getAdminPage() {
		return "shiftStatistics";
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public ModelAndView search(@Param("start") String start, @Param("end") String end) {
		ModelAndView mv = new ModelAndView("shiftStatistics");
		Set<Shift> dates = shiftService.findByDates(LocalDate.parse(start), LocalDate.parse(end));
		mv.addObject("shifts", dates);
		return mv;
	}

	@RequestMapping(value = "/search/lastWeek", method = RequestMethod.GET)
	public ModelAndView searchLastWeek() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.minusDays(7);
		ModelAndView mv = new ModelAndView("shiftStatistics");
		Set<Shift> dates = shiftService.findByDates(end,start);
		mv.addObject("shifts", dates);
		return mv;
	}
	@RequestMapping(value = "/search/lastMonth", method = RequestMethod.GET)
	public ModelAndView searchMonth() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.minusMonths(1);
		ModelAndView mv = new ModelAndView("shiftStatistics");
		Set<Shift> dates = shiftService.findByDates(end,start);
		mv.addObject("shifts", dates);
		return mv;
	}

	@RequestMapping(value = "/search/allShifts", method = RequestMethod.GET)
	public ModelAndView searchLastMonth() {
		ModelAndView mv = new ModelAndView("shiftStatistics");
		List<Shift> dates = shiftService.findAll();
		mv.addObject("shifts", dates);
		return mv;
	}

	@RequestMapping(value = "/search/lastThreeMonth", method = RequestMethod.GET)
	public ModelAndView searchThreeMonth() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.minusMonths(2);
		ModelAndView mv = new ModelAndView("shiftStatistics");
		Set<Shift> dates = shiftService.findByDates(end,start);
		mv.addObject("shifts", dates);
		return mv;
	}

	@RequestMapping(value = "/search/lastHalfYear", method = RequestMethod.GET)
	public ModelAndView searchHalfYear() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.minusMonths(6);
		ModelAndView mv = new ModelAndView("shiftStatistics");
		Set<Shift> dates = shiftService.findByDates(end,start);
		mv.addObject("shifts", dates);
		return mv;
	}
}
