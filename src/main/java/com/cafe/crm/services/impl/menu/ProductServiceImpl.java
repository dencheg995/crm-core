package com.cafe.crm.services.impl.menu;

import com.cafe.crm.dto.WrapperOfProduct;
import com.cafe.crm.models.company.Company;
import com.cafe.crm.models.menu.Ingredients;
import com.cafe.crm.models.menu.Product;
import com.cafe.crm.models.user.Position;
import com.cafe.crm.repositories.menu.ProductRepository;
import com.cafe.crm.services.interfaces.company.CompanyService;
import com.cafe.crm.services.interfaces.menu.IngredientsService;
import com.cafe.crm.services.interfaces.menu.ProductService;
import com.cafe.crm.services.interfaces.position.PositionService;
import com.cafe.crm.utils.CompanyIdCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final PositionService positionService;
	private final CompanyService companyService;
	private final CompanyIdCache companyIdCache;
	@Autowired
	private IngredientsService ingredientsService;

	@Autowired
	public ProductServiceImpl(ProductRepository productRepository, PositionService positionService, CompanyService companyService, CompanyIdCache companyIdCache) {
		this.productRepository = productRepository;
		this.positionService = positionService;
		this.companyService = companyService;
		this.companyIdCache = companyIdCache;
	}

	@Override
	public List<Product> findAll() {
		return productRepository.findByDeletedIsFalseAndCompanyId(companyIdCache.getCompanyId());
	}

	@Override
	public List<Product> saveAll(List<Product> products) {
		return productRepository.save(products);
	}

	private void setCompany(Product product){
		Long companyId = companyIdCache.getCompanyId();
		Company company = companyService.findOne(companyId);
		product.setCompany(company);
	}

	@Override
	public Product saveAndFlush(Product product) {
		setCompany(product);
		return productRepository.saveAndFlush(product);
	}

	@Override
	public Product findOne(Long id) {
		return productRepository.findOne(id);
	}

	@Override
	public void delete(Long id) {
		Product product = findOne(id);
		if (product != null) {
			product.setDeleted(true);
			saveAndFlush(product);
		}
	}

	@Override
	public void deleteIngredientFromProducts(List<Product> products, Ingredients ingredients) {
		for (Product product : products) {
			Map<Ingredients, Double> receiptMap = product.getRecipe();
			receiptMap.remove(ingredients);
			product.setRecipe(receiptMap);
		}
		saveAll(products);
	}

	@Override
	public Product findByNameAndDescriptionAndCost(String name, String description, Double cost) {
		return productRepository.findByDeletedIsFalseAndNameAndDescriptionAndCostAndCompanyId(name, description, cost, companyIdCache.getCompanyId());
	}

	@Override
	public List<Product> findAllOrderByRatingDesc() {
		return productRepository.findByDeletedIsFalseAndCompanyIdOrderByRatingDescNameAsc(companyIdCache.getCompanyId());
	}

	@Override
	public void reduceIngredientAmount(Product product) {
		ingredientsService.reduceIngredientAmount(product.getRecipe());
	}

	@Override
	public Map<Position, Integer> createStaffPercent(WrapperOfProduct wrapper) {
		Map<Position,Integer> staffPercent = new HashMap<>();

		List<Long> positionsId = wrapper.getStaffPercentPosition();
		List<Integer> percents = wrapper.getStaffPercentPercent();

		for (int i = 0; i < positionsId.size(); i++) {
			Position position = positionService.findById(positionsId.get(i));
			staffPercent.put(position,percents.get(i));
		}

		return staffPercent;
	}

	@Override
	public List<Product> findByIds(long[] ids) {
		return productRepository.findByIdIn(ids);
	}

	@Override
	public List<Product> findAllReceiptProducts(Ingredients ingredient) {
		return productRepository.findAllReceiptProducts(ingredient);
	}

}
