package com.example.demo.utils;

import com.example.demo.annotation.Excel;
import com.example.demo.constpool.ExcelConst;
import com.example.demo.entity.ExcelDefinition;
import com.example.demo.entity.WorkBook;
import com.example.demo.factory.WorkBookFactory;
import com.example.demo.reg.RegExpStyle;
import com.example.demo.styles.ExcelCellStyle;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.util.CollectionUtils;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (C), 2020
 *
 * @author: liuhao
 * @date: 2020/11/21 19:25
 * @description:
 */

public class ExcelExportUtil {

    /**
     * @param clazz 类型
     * @return 类中属性注解
     */
    private static List<Excel> buildAnnotationsSortedByClass(Class<?> clazz) {
        List<Excel> annotations = new ArrayList<>();

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Excel excel = field.getAnnotation(Excel.class);
            if (excel != null) {
                annotations.add(excel);
            }
        }
        return annotations.stream().sorted(Comparator.comparing(Excel::column)).collect(Collectors.toList());
    }

    /**
     * 初始化excel
     *
     * @param clazz      数据pojo的class类型
     * @param sheetNames sheet名称
     */
    public static void initialExcel(Class<?> clazz, String[] sheetNames) {
        WorkBookFactory factory = WorkBookFactory.getSingletonFactory()
                .initial()
                .createSheets(sheetNames)
                .createCellColorStyles();
        final WorkBook workBook = factory.getWorkBook();
        HSSFSheet sheet = workBook.getDefaultSheet();

        List<Excel> sortedExcel = buildAnnotationsSortedByClass(clazz);
        List<String> tableRowNames = sortedExcel.stream().map(Excel::name).collect(Collectors.toList());
        factory.buildFirstRow(sheet, tableRowNames.toArray(new String[0]));
    }

    /**
     * 构建excel内部对象
     *
     * @param pojoList 数据pojo
     * @return excel内部对象
     * @throws Exception 异常
     */
    public static List<ExcelDefinition> buildExcelDefinition(List<?> pojoList) throws Exception {
        List<ExcelDefinition> excelDefinitionList = new ArrayList<>();

        final Class<?> pojoClazz = pojoList.get(0).getClass();
        final Field[] declaredFields = pojoClazz.getDeclaredFields();

        for (Object pojo : pojoList) {
            ExcelDefinition excelDefinition = new ExcelDefinition();
            Object[] tableRowValues = new Object[declaredFields.length];
            String[] tableRowTypes = new String[declaredFields.length];
            Excel[] annotationExcels = new Excel[declaredFields.length];

            for (Field field : declaredFields) {
                Excel excel = field.getAnnotation(Excel.class);
                if (excel != null) {
                    field.setAccessible(true);
                    tableRowValues[excel.column()] = field.get(pojo);
                    tableRowTypes[excel.column()] = excel.pattern();
                    annotationExcels[excel.column()] = excel;
                    excelDefinition.setTableRowCount(excelDefinition.getTableRowCount() + 1);
                }
            }
            excelDefinition.setTableRowValues(tableRowValues);
            excelDefinition.setTableRowTypes(tableRowTypes);
            excelDefinition.setAnnotationExcels(annotationExcels);
            excelDefinitionList.add(excelDefinition);
        }
        return excelDefinitionList;
    }

    /**
     * 填充内容
     *
     * @param pojoList 数据对象
     * @param sheetIndex 数据对象
     * @throws Exception 异常
     */
    public static void fillExcelRows(List<?> pojoList, int sheetIndex) throws Exception {
        final WorkBookFactory workBookFactory = WorkBookFactory.getSingletonFactory();
        final HSSFWorkbook wb = workBookFactory.getWorkBook().getWb();
        final HSSFSheet defaultSheet = workBookFactory.getWorkBook().getSheet(sheetIndex);
        final HSSFPatriarch patriarch = workBookFactory.getPatriarch(defaultSheet);

        if (CollectionUtils.isEmpty(pojoList)) {
            return;
        }
        final List<ExcelDefinition> excelDefinitionList = buildExcelDefinition(pojoList);
        for (ExcelDefinition excelDefinition : excelDefinitionList) {
            // 创建单元格并填充数据
            int i = 0;
            HSSFRow row = defaultSheet.createRow(++i);
            for (int j = 0; j < excelDefinition.getTableRowCount(); j++) {
                if (RegExpStyle.PICTURE.equals(excelDefinition.getTableRowTypes()[j])) {
                    defaultSheet.setColumnWidth(j, excelDefinition.getAnnotationExcels()[j].width());
                    row.setHeight(excelDefinition.getAnnotationExcels()[j].height());
                    final HSSFClientAnchor clientAnchor = workBookFactory.getClientAnchor((short) j, i, (short) (j + 1), i + 1);
                    final BASE64Decoder decoder = new BASE64Decoder();
                    final byte[] base64Bytes = decoder.decodeBuffer((excelDefinition.getTableRowValues()[j]).toString());
                    patriarch.createPicture(clientAnchor, wb.addPicture(base64Bytes, HSSFWorkbook.PICTURE_TYPE_JPEG));
                } else {
                    final HSSFCell hssfCell = row.createCell(j);
                    hssfCell.setCellStyle(ExcelCellStyle.getSingletonCellStyle().getAlignments().get(ExcelConst.VERTICAL_ALIGNMENT_CENTER));
                    hssfCell.setCellValue(String.valueOf(excelDefinition.getTableRowValues()[j]));
                }
            }
        }
    }

    /**
     * 填充内容
     *
     * @param pojoList  数据对象
     * @param failPos   问题位置
     * @param failCause 问题原因
     * @throws Exception 异常
     */
    public static void fillExcelRows(List<?> pojoList, List<List<Integer>> failPos, List<List<String>> failCause) throws Exception {

        final HSSFSheet defaultSheet = WorkBookFactory.getSingletonFactory().getWorkBook().getDefaultSheet();
        final CellStyle defaultCellStyle = WorkBookFactory.getSingletonFactory().getWorkBook().getDefaultCellStyle();

        if (CollectionUtils.isEmpty(pojoList)) {
            return;
        }
        final Class<?> pojoClazz = pojoList.get(0).getClass();
        final Field[] declaredFields = pojoClazz.getDeclaredFields();

        for (int i = 0; i < pojoList.size(); i++) {
            HSSFRow row = defaultSheet.createRow(i + 1);
            Object pojo = pojoList.get(i);

            Object[] tableRowValues = new Object[declaredFields.length];
            int totalRow = 0;
            for (Field field : declaredFields) {
                Excel excel = field.getAnnotation(Excel.class);
                if (excel != null) {
                    field.setAccessible(true);
                    Object val = field.get(pojo);
                    int column = excel.column();
                    tableRowValues[column] = val;
                    totalRow++;
                }
            }

            List<Integer> pos = failPos.get(i);
            List<String> cause = failCause.get(i);
            // 创建单元格并填充数据
            for (int j = 0; j < totalRow; j++) {
                HSSFCell cellJ = row.createCell(j);
                cellJ.setCellValue(String.valueOf(tableRowValues[j]));
                if (pos.contains(j)) {
                    cellJ.setCellStyle(defaultCellStyle);
                }
            }
            row.createCell(totalRow).setCellValue(String.join(",", cause));
        }

    }

    /**
     * 输出excel
     *
     * @param output 输出流
     * @throws IOException 异常
     */
    public static void outputExcel(FileOutputStream output) throws IOException {
        final WorkBook workBook = WorkBookFactory.getSingletonFactory().getWorkBook();
        workBook.getWb().write(output);
    }

    /**
     * 输出excel
     *
     * @param filePath 输出文件路径
     * @throws IOException 异常
     */
    public static void outputExcel(String filePath) throws IOException {
        File exportFile = new File(filePath);
        FileOutputStream output = new FileOutputStream(exportFile);
        outputExcel(output);
    }

}