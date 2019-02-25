package com.cafe.crm.controllers.rest;

import com.cafe.crm.utils.EnumWeekDay;
import com.cafe.crm.controllers.report.ReportController;
import com.cafe.crm.dto.DateClientAmount;
import com.cafe.crm.dto.SaleProductOnDay;
import com.cafe.crm.services.interfaces.report.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rest/reports")
public class ReportRestController {
    private final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final String clientDataName = "clientsData.xlsx";
    private final ReportService reportService;

    @Autowired
    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(value = "/createClientsData")
    public ResponseEntity createClientsData(@RequestParam("startDate") String start,
                                            @RequestParam("endDate") String end,
                                            @RequestParam("weekDays[]") String[] weekDays) {
        List<Integer> weekDaysNumer = new ArrayList<>();
        for (String weekDay : weekDays) {
            weekDaysNumer.add(EnumWeekDay.valueOf(weekDay).ordinal());
        }
        reportService.createDateClientData(clientDataName, LocalDate.parse(start), LocalDate.parse(end), weekDaysNumer);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value = "/getClientsData")
    public ResponseEntity<InputStreamResource> getClientsData() {
        File file = new File(clientDataName);

        InputStreamResource resource = null;
        try {
            resource = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            logger.error("File not found! ", e);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentLength(file.length())
                .body(resource);
    }

    @GetMapping("/getDataForChart")
    public ResponseEntity getDataForChart(@RequestParam("startDate") String start, @RequestParam("endDate") String end,
                                           @RequestParam("weekDays[]") String[] weekDays) {
        List<Integer> weekDaysNumer = new ArrayList<>();
        for (String weekDay : weekDays) {
            weekDaysNumer.add(EnumWeekDay.valueOf(weekDay).ordinal());
        }
        List<DateClientAmount> clientsOnDays = reportService.getClientCount(LocalDate.parse(start), LocalDate.parse(end), weekDaysNumer);
        return ResponseEntity.ok(clientsOnDays);
    }

    @GetMapping("/getDataForChartProducts")
    public ResponseEntity getDataForChartProduct(@RequestParam("startDate") String start, @RequestParam("endDate") String end,
                                           @RequestParam("weekDays[]") String[] weekDays,
                                           @RequestParam("products[]") String[] products) {
        StringBuilder weekDaysTemplate = new StringBuilder();
        for (String weekDay : weekDays) {
            weekDaysTemplate.append((weekDaysTemplate.length() == 0) ? "" : ",").append(EnumWeekDay.valueOf(weekDay).ordinal());
        }
        weekDaysTemplate = new StringBuilder("(" + weekDaysTemplate + ")");
        StringBuilder productsTemplate = new StringBuilder();
        for (String product : products) {
            productsTemplate.append((productsTemplate.length() == 0) ? "" : ",").append(product);
        }
        productsTemplate = new StringBuilder("(" + productsTemplate + ")");
        List<SaleProductOnDay> salesProductsOnDays = reportService.getProductOnDays(LocalDate.parse(start), LocalDate.parse(end), weekDaysTemplate.toString(), productsTemplate.toString());
        return ResponseEntity.ok(salesProductsOnDays);
    }
}