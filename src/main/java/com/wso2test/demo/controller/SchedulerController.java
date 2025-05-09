package com.wso2test.demo.controller;

import com.wso2test.demo.dto.AggregatedScheduleOutput;
import com.wso2test.demo.dto.DailyNonSupportScheduleRow;
import com.wso2test.demo.dto.DailySupportScheduleRow;
import com.wso2test.demo.model.SupportSchedule;
import com.wso2test.demo.respository.SupportScheduleRepository;
import com.wso2test.demo.service.SupportSchedulerService;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class SchedulerController {

    // Use @Autowired on the constructor for constructor injection with @RequiredArgsConstructor
    // or directly on the fields if not using @RequiredArgsConstructor for final fields.
    // For final fields with @RequiredArgsConstructor, @Autowired on constructor is implicit.
    private final SupportSchedulerService supportSchedulerService;
    private final SupportScheduleRepository supportScheduleRepository;

    // Endpoint to generate the monthly schedule and return it as an Excel file
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateMonthlyScheduleExcel(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) List<String> excludeJuniorDates) {

        try {
            AggregatedScheduleOutput aggregatedOutput = supportSchedulerService.generateMonthlySchedule(
                    year, month,
                    excludeJuniorDates == null ? Collections.emptyList() : excludeJuniorDates
            );

            Workbook workbook = new XSSFWorkbook();

            // --- Sheet 1: Non-Support Schedule (System Monitoring & COB) ---
            Sheet nonSupportSheet = workbook.createSheet("System_Monitoring_COB");
            // Add a title row for the first sheet
            Row titleRowNonSupport = nonSupportSheet.createRow(0);
            Cell titleCellNonSupport = titleRowNonSupport.createCell(0);
            titleCellNonSupport.setCellValue("Staffs Assigned for System Monitoring, COB and Extended Support.");
            // You can merge cells here if you want:
            // nonSupportSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5)); // Merge across 6 columns
            // Apply styling to titleCellNonSupport (bold, larger font, etc.)

            createNonSupportSheetContent(nonSupportSheet, aggregatedOutput.getNonSupportSchedule(), workbook);


            // --- Sheet 2: Support Schedule ---
            Sheet supportSheet = workbook.createSheet("Support_Schedule");
             // Add a title row for the second sheet
            Row titleRowSupport = supportSheet.createRow(0);
            Cell titleCellSupport = titleRowSupport.createCell(0);
            titleCellSupport.setCellValue("Core Banking Support Division");
            // supportSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); // Merge across 5 columns
            // Apply styling

            Row subTitleRowSupport = supportSheet.createRow(1);
            Cell subTitleCellSupport = subTitleRowSupport.createCell(0);
            subTitleCellSupport.setCellValue("Daily support schedule (Schedule for Help desk of each day of the month of May. " + year +")"); // Example, adjust month
            // supportSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));


            createSupportSheetContent(supportSheet, aggregatedOutput.getSupportSchedule(), workbook);

            // Write workbook to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();
            byte[] excelBytes = bos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String fileName = String.format("schedules_%d_%02d.xlsx", year, month);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelBytes.length);

            return ResponseEntity.ok().headers(headers).body(excelBytes);

        } catch (IllegalStateException e) { // Catch specific exception for existing schedule
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage().getBytes()); // Or a JSON error response
        } 
        catch (IOException e) {
            // Log the IOException
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating Excel file.".getBytes());
        }
        catch (Exception e) {
            // Log general exception
            System.err.println("Error generating schedule Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error generating schedule: " + e.getMessage()).getBytes());
        }
    }

    private void createNonSupportSheetContent(Sheet sheet, List<DailyNonSupportScheduleRow> data, Workbook workbook) {
        // Define header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        // headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // Start headers from row 2 to accommodate title rows
        Row headerRow = sheet.createRow(2); // Assuming title rows are 0 and 1
        String[] nonSupportHeaders = {"No", "Date", "Day", "System Monitoring Performers", "Evening COB Performers", "Remark"};
        for (int i = 0; i < nonSupportHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(nonSupportHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 3; // Data starts from row 3
        CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setBorderBottom(BorderStyle.THIN);
        defaultCellStyle.setBorderTop(BorderStyle.THIN);
        defaultCellStyle.setBorderLeft(BorderStyle.THIN);
        defaultCellStyle.setBorderRight(BorderStyle.THIN);
        defaultCellStyle.setWrapText(true); // Allow text wrapping in cells

        for (DailyNonSupportScheduleRow rowData : data) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, rowNum - 3, defaultCellStyle); // Simple row number
            createCell(row, 1, rowData.getDate().toString(), defaultCellStyle); // Or format as needed
            createCell(row, 2, rowData.getDayOfWeek(), defaultCellStyle);
            createCell(row, 3, rowData.getSystemMonitoringPerformersDisplay(), defaultCellStyle);
            createCell(row, 4, rowData.getEveningCobPerformersDisplay(), defaultCellStyle);
            createCell(row, 5, rowData.getRemark() != null ? rowData.getRemark() : "", defaultCellStyle);
        }
        for (int i = 0; i < nonSupportHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(3, 25 * 256); // Approx 25 characters width for System Monitoring
        sheet.setColumnWidth(4, 35 * 256); // Approx 35 characters width for COB
        sheet.setColumnWidth(5, 30 * 256); // Approx 30 characters width for Remark
    }

    private void createSupportSheetContent(Sheet sheet, List<DailySupportScheduleRow> data, Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Start headers from row 3 to accommodate title rows
        Row headerRow = sheet.createRow(3); // Assuming title rows are 0, 1, 2
        String[] supportHeaders = {"Date", "Day", "Reserved staff if supporters in Group (A) are not available", "Assigned Group (A) supporter", "Remark"};
        for (int i = 0; i < supportHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(supportHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 4; // Data starts from row 4
        CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setBorderBottom(BorderStyle.THIN);
        defaultCellStyle.setBorderTop(BorderStyle.THIN);
        defaultCellStyle.setBorderLeft(BorderStyle.THIN);
        defaultCellStyle.setBorderRight(BorderStyle.THIN);
        defaultCellStyle.setWrapText(true);

        for (DailySupportScheduleRow rowData : data) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, rowData.getDate().toString(), defaultCellStyle);
            createCell(row, 1, rowData.getDayOfWeek(), defaultCellStyle);
            createCell(row, 2, rowData.getReservedStaffDisplay(), defaultCellStyle); // Logic for this needs to be implemented if needed
            createCell(row, 3, rowData.getAssignedGroupADisplay(), defaultCellStyle);
            createCell(row, 4, rowData.getRemark() != null ? rowData.getRemark() : "", defaultCellStyle);
        }
        for (int i = 0; i < supportHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(2, 40 * 256); // Reserved staff
        sheet.setColumnWidth(3, 40 * 256); // Assigned Group A
        sheet.setColumnWidth(4, 30 * 256); // Remark
    }

    private void createCell(Row row, int columnNumber, String value, CellStyle style) {
        Cell cell = row.createCell(columnNumber);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
    private void createCell(Row row, int columnNumber, Number value, CellStyle style) {
        Cell cell = row.createCell(columnNumber);
        if (value != null) cell.setCellValue(value.doubleValue()); // POI handles int/long via doubleValue
        if (style != null) {
            cell.setCellStyle(style);
        }
    }


    // Endpoint to retrieve all support schedules (raw data)
    @GetMapping
    public ResponseEntity<List<SupportSchedule>> getAllSchedules() {
        List<SupportSchedule> schedules = supportScheduleRepository.findAll();
        return ResponseEntity.ok(schedules);
    }

    // Endpoint to delete all schedules (used when resetting or testing)
    @DeleteMapping
    public ResponseEntity<Void> deleteAllSchedules() {
        supportScheduleRepository.deleteAll(); // This deletes all SupportSchedule entities
        // If you also need to reset other data (like auto-generated leaves), do it here.
        // e.g., leaveRepository.deleteByReason("Rest after COB shift");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}