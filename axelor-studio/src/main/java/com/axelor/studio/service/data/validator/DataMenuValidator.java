package com.axelor.studio.service.data.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.DataCommon;
import com.axelor.studio.service.data.exporter.DataExportMenu;
import com.google.inject.Inject;

public class DataMenuValidator extends DataCommon {
	
	private DataValidatorService validatorService;
	
	private List<String> menus;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private ConfigurationService configService;
	
	public void validate(DataValidatorService validatorService, XSSFSheet sheet) throws IOException {
		if (validatorService == null) {
			return;
		}
		this.validatorService = validatorService;
		if (sheet == null) {
			return;
		}
		
		menus = new ArrayList<String>();
		
		Iterator<Row> rowIter = sheet.iterator();
		while(rowIter.hasNext()) {
			Row row = rowIter.next();
			if (row.getRowNum() == 0) {
				continue;
			}
			String module = getValue(row, DataExportMenu.MODULE);
			if (module == null || configService.getNonCustomizedModules().contains(module)) {
				continue;
			}
			validateMenu(row);
		}
		
	}
	
	private void validateMenu(Row row) throws IOException {
		
		String name = getValue(row, DataExportMenu.NAME);
		String title = getValue(row, DataExportMenu.TITLE);
		
		if (title == null) {
			title = getValue(row, DataExportMenu.TITLE_FR);
		}
		
		if (name == null && title == null) {
			validatorService.addLog(I18n.get("Name and title is empty" ), row);
		}
		
		String model = getValue(row, DataExportMenu.OBJECT);
		if (model != null && !validatorService.isValidModel(model)) {
			validatorService.addLog(I18n.get("Invalid model" ), row);
		}
		
		String order = getValue(row, DataExportMenu.ORDER);
		if (order != null) {
			try {
				Integer.parseInt(order.trim());
			} catch(Exception e) {
				validatorService.addLog(I18n.get("Invalid menu order"), row);
			}
		}
		
		if (name == null) {
			name = title;
		}
		
		String parent = getValue(row, DataExportMenu.PARENT);
		if (parent != null && !menus.contains(parent)) {
			MetaMenu menu = metaMenuRepo.all().filter("self.name = ?1" , parent).fetchOne();
			if(menu == null){
				validatorService.addLog(I18n.get("No parent menu defined"), row);
			}
		}
		
		menus.add(name);
		
	}
}
