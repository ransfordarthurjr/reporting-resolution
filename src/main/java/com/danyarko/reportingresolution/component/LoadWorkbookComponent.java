package com.danyarko.reportingresolution.component;

import com.danyarko.reportingresolution.configuration.CellReferenceProperty;
import com.danyarko.reportingresolution.configuration.DatasourceProperty;
import com.danyarko.reportingresolution.configuration.DirectoryProperty;
import com.danyarko.reportingresolution.hibernate.HibernateUtil;
import com.danyarko.reportingresolution.model.datasource.BankLoadingModel;
import com.danyarko.reportingresolution.model.datasource.BankReferenceModel;
import com.danyarko.reportingresolution.model.datasource.BankResolutionModel;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component("LoadWorkbookComponent")
public class LoadWorkbookComponent {
    @Autowired
    public LoadWorkbookComponent(DatasourceProperty datasourceProperty, DirectoryProperty directoryProperty, CellReferenceProperty cellReferenceProperty) {
        this.datasourceProperty = datasourceProperty;
        this.directoryProperty = directoryProperty;
        this.cellReferenceProperty = cellReferenceProperty;
    }

    private final DatasourceProperty datasourceProperty;
    private final DirectoryProperty directoryProperty;
    private final CellReferenceProperty cellReferenceProperty;

    private FormulaEvaluator evaluator;

    public void loadWorkbook() {
        BankLoadingModel bankLoading = this.getLoaderStatus();

        if(bankLoading != null && bankLoading.getLoading() != 1) {
            this.setLoaderRunningStatus(bankLoading, 1);
            String [] inputFiles = this.getPendingFiles();

            if (inputFiles != null && inputFiles.length > 0) {
                List<BankReferenceModel> bankReferences = this.getBankReferences();

                XSSFWorkbook workbook;
                String month, name;
                Sheet sheet;
                Row row;
                Cell cell;
                boolean isLoadableSheet;
                int rowIndex, colIndex;

                List<BankResolutionModel> bankResolutions;
                for (String fileName : inputFiles) {
                    if (FilenameUtils.getExtension(fileName).equalsIgnoreCase("xlsx")) {
                        System.out.println("loading workbook: " + fileName);
                        try {
                            bankResolutions = new ArrayList<>();
                            workbook = new XSSFWorkbook(new File(this.directoryProperty.getInput() + "/" + fileName));
                            this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();

                            month = this.getMonthFromSheet(workbook.getSheetAt(0)).trim();
                            name = this.getNameFromSheet(workbook.getSheetAt(0)).trim();
                            // System.out.println(month + "|" + name);

                            // dont include sheet 0 (COVER_SHEET)
                            for(int i=1; i<workbook.getNumberOfSheets(); i++) {
                                sheet = workbook.getSheetAt(i);
                                isLoadableSheet = this.isSheetLoadable(sheet.getSheetName().trim(), bankReferences);

                                if(isLoadableSheet) {
                                    System.out.println("loading sheet: " + sheet.getSheetName());

                                    BankResolutionModel bankResolution;
                                    for(int r: this.cellReferenceProperty.getRowsToLoad()) {
                                        rowIndex = r-1;
                                        row = sheet.getRow(rowIndex);
                                        bankResolution = new BankResolutionModel(month, name, fileName, sheet.getSheetName());

                                        for (int c: this.cellReferenceProperty.getColsToLoad()) {
                                            colIndex = c-1;
                                            cell = row.getCell(colIndex);

                                            // System.out.println("row: " + r + ", col: " + c);
                                            this.setBankResolutionField(c, cell, bankResolution);
                                        }

                                        // System.out.println(bankResolution);
                                        bankResolutions.add(bankResolution);
                                    }
                                }
                            }

                            workbook.close();
                            this.saveResolutions(bankResolutions);
                            this.moveProcessedFile(fileName);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }// end if xlsx file
                }// end for input files
            }
            else {
                System.out.println("no input files available");
            }

            this.setLoaderRunningStatus(bankLoading, 0);
        }
        else {
            System.out.println("loader is still running...");
        }
    }

    private BankLoadingModel getLoaderStatus() {
        Session session = HibernateUtil.beginTransaction(this.datasourceProperty);
        try {
            String hql = "FROM " + BankLoadingModel.class.getSimpleName() + " ORDER BY id DESC";
            List<BankLoadingModel> loadingModels = session.createQuery(hql, BankLoadingModel.class).setMaxResults(1).list();
            HibernateUtil.commitTransaction(session);

            return loadingModels != null && loadingModels.size() > 0 ? loadingModels.get(0) : null;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            HibernateUtil.closeTransaction(session);
        }

        return null;
    }

    private void setLoaderRunningStatus(BankLoadingModel bankLoadingModel, int status) {
        Session session = HibernateUtil.beginTransaction(this.datasourceProperty);
        try {
            bankLoadingModel.setLoading(status);
            session.update(bankLoadingModel);
            HibernateUtil.commitTransaction(session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            HibernateUtil.closeTransaction(session);
        }
    }

    private String [] getPendingFiles() {
        File inputDirectory = new File(directoryProperty.getInput());
        if(inputDirectory.exists() && inputDirectory.isDirectory()) {
            return  inputDirectory.list();
        }

        return null;
    }

    private String getMonthFromSheet(Sheet sheet) {
        CellReference cellReference = new CellReference(this.cellReferenceProperty.getMonth().toUpperCase());
        Cell cell = sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol());

        return this.getCellValue(cell);
    }

    private String getNameFromSheet(Sheet sheet) {
        CellReference cellReference = new CellReference(this.cellReferenceProperty.getName().toUpperCase());
        Cell cell = sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol());

        return this.getCellValue(cell);
    }

    private List<BankReferenceModel> getBankReferences() {
        List<BankReferenceModel> bankReferences = new ArrayList<>();

        Session session = HibernateUtil.beginTransaction(this.datasourceProperty);
        try {
            String hql = "FROM " + BankReferenceModel.class.getSimpleName() + " ORDER BY id";
            bankReferences = session.createQuery(hql, BankReferenceModel.class).list();
            HibernateUtil.commitTransaction(session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            HibernateUtil.closeTransaction(session);
        }

        return bankReferences;
    }

    private boolean isSheetLoadable(String sheetName, List<BankReferenceModel> bankReferences) {
        for(BankReferenceModel br: bankReferences) {
            if(br.getId().equalsIgnoreCase(sheetName)) {
                return true;
            }
        }
        return false;
    }

    private String getCellValue(Cell cell) {
        if(cell == null) {
            return "";
        }

        if(cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);

            switch (cellValue.getCellType()) {
                case BLANK:
                case _NONE:
                case ERROR:
                    return "";
                case NUMERIC:
                    return String.valueOf(BigDecimal.valueOf(cellValue.getNumberValue()));
                case BOOLEAN:
                    return String.valueOf(cellValue.getBooleanValue());
                default:
                    return cellValue.getStringValue();
            }
        }

        switch (cell.getCellType()) {
            case BLANK:
            case _NONE:
            case ERROR:
                return "";
            case NUMERIC:
                return String.valueOf(BigDecimal.valueOf(cell.getNumericCellValue()));
            case BOOLEAN:
                return String.valueOf((cell.getBooleanCellValue()));
            default:
                return cell.getStringCellValue();
        }
    }

    private void setBankResolutionField(int column, Cell cell, BankResolutionModel bankResolution) {
        switch (column) {
            case 2:
                bankResolution.setDescription(this.getCellValue(cell).trim());
                break;

            case 3:
                bankResolution.setBalanceAsAt2018(this.getCellValue(cell));
                break;

            case 4:
                bankResolution.setReceiversAdjustmentAtOpening(this.getCellValue(cell));
                break;

            case 5:
                bankResolution.setBalancesTransferredToCbg(this.getCellValue(cell));

                break;

            case 6:
                bankResolution.setReceiversOpeningBalances(this.getCellValue(cell));
                break;

            case 7:
                bankResolution.setOtherAdjustments(this.getCellValue(cell));
                break;

            case 8:
                bankResolution.setTotalRecoveriesAsAtMonthOfReporting(this.getCellValue(cell));
                break;

            case 9:
                bankResolution.setEstimatedRecoverableAssetsByTheReceiver(this.getCellValue(cell));
                break;

            case 10:
                bankResolution.setBalanceAsAtMonthOfReporting(this.getCellValue(cell));
                break;
        }
    }

    private void saveResolutions(List<BankResolutionModel> bankResolutions) {
        Session session = HibernateUtil.beginTransaction(this.datasourceProperty);

        int count = 0;
        for (BankResolutionModel resolution : bankResolutions) {
            session.save(resolution);

            //flush on every 20th save
            if (++count % 10 == 0)
                session.flush();
        }
        session.flush();

        HibernateUtil.commitTransaction(session);
        HibernateUtil.closeTransaction(session);
    }

    private void moveProcessedFile(String fileName) {
        Path sourcePath, destinationPath;
        try {
                sourcePath = Paths.get(this.directoryProperty.getInput() + "/" + fileName);
                destinationPath = Paths.get(this.directoryProperty.getOutput() + "/" + fileName);
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void moveProcessedFiles(List<String> fileNames) {
        Path sourcePath, destinationPath;
        try {
            for(String name : fileNames) {
                sourcePath = Paths.get(this.directoryProperty.getInput() + "/" +name);
                destinationPath = Paths.get(this.directoryProperty.getOutput() + "/" + name);
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
