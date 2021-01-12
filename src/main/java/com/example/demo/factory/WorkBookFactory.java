package com.example.demo.factory;

import com.example.demo.entity.WorkBook;
import com.example.demo.styles.ExcelCellStyle;
import lombok.Data;
import org.apache.poi.hssf.usermodel.*;

import java.util.ArrayList;

/**
 * @author YCKJ3275
 */
@Data
public class WorkBookFactory {

    private WorkBook workBook = new WorkBook();
    private static volatile WorkBookFactory workBookFactory;

    // 私有的构造方法
    private WorkBookFactory() {
    }

    public static WorkBookFactory getSingletonFactory() {

        if (workBookFactory == null) {
            synchronized (WorkBookFactory.class) {
                if (workBookFactory == null) {
                    workBookFactory = new WorkBookFactory();
                }
            }
        }
        return workBookFactory;
    }

    /**
     * 初始化
     *
     * @return
     */
    public WorkBookFactory initial() {
        workBook.setWb(new HSSFWorkbook());
        workBook.setSheets(new ArrayList<>());
        workBook.setCellStyles(new ArrayList<>());
        return workBookFactory;
    }

    /**
     * 构建sheet
     *
     * @param sheetNames sheet名称
     */
    public WorkBookFactory createSheets(String... sheetNames) {
        if (sheetNames.length == 0) {
            HSSFSheet sheet = workBook.getWb().createSheet("默认Sheet");
            workBook.getSheets().add(sheet);
        } else {
            for (String sheetName : sheetNames) {
                HSSFSheet sheet = workBook.getWb().createSheet(sheetName);
                workBook.getSheets().add(sheet);
            }
        }
        return workBookFactory;
    }

    /**
     * 构建默认表格颜色类型
     */
    public WorkBookFactory createCellColorStyles() {
        ExcelCellStyle.getSingletonCellStyle().initialCellStyle(workBook.getWb()).initialDefault();
        return workBookFactory;
    }

    /**
     * 构建默认表格画图的顶级管理器
     */
    public synchronized HSSFPatriarch getPatriarch(HSSFSheet sheet) {
        final HSSFPatriarch drawingPatriarch = sheet.getDrawingPatriarch();
        if (drawingPatriarch == null) {
            return sheet.createDrawingPatriarch();
        }
        return drawingPatriarch;
    }

    /**
     * 构建图片位置
     */
    public HSSFClientAnchor getClientAnchor(short col1, int row1, short col2, int row2) {
        return new HSSFClientAnchor(0, 0, 0, 0, col1, row1, col2, row2);
    }

    /**
     * 初始化首行
     *
     * @param sheet    sheet
     * @param rowNames 首行列名
     */
    public void buildFirstRow(HSSFSheet sheet, String... rowNames) {

        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < rowNames.length; i++) {
            row.createCell(i).setCellValue(rowNames[i]);
            //列宽自适应
            sheet.autoSizeColumn(i);
        }
    }
}
