package com.example.demo.utils;

import com.example.demo.annotation.Excel;
import com.example.demo.handler.Handler;
import com.example.demo.reg.RegExpStyle;
import com.example.demo.result.ImportResult;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C), 2020
 *
 * @author: liuhao
 * @date: 2020/11/21 19:25
 * @description:
 */

public class ExcelImportUtil {


    /**
     *
     */
    public static ImportResult<?> importExcel(InputStream inputStream, Class<?> pojoClass) throws Exception {

        List<Object> fails = new ArrayList<>();
        List<Object> succs = new ArrayList<>();

        List<String> failPos = new ArrayList<>();
        List<String> failCause = new ArrayList<>();

        HSSFWorkbook book = new HSSFWorkbook(inputStream);
        HSSFSheet sheet = book.getSheetAt(0);

        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {

            HSSFRow row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Object clazz = pojoClass.newInstance();
            List<String> pos = new ArrayList<>();
            List<String> cause = new ArrayList<>();

            boolean isSucc = true;
            Field[] declaredFields = pojoClass.getDeclaredFields();
            for (Field field : declaredFields) {
                Boolean succ = handleField(field, row, clazz);
                if (succ != null && succ.equals(false)) {
                    Excel excel = field.getAnnotation(Excel.class);
                    pos.add(String.valueOf(excel.column()));
                    cause.add(excel.cause());
                    isSucc = false;
                }
            }
            if (pos.size() > 0) {
                failPos.add(String.join(",", pos));
                failCause.add(String.join(",", cause));
            }
            Object o = isSucc ? succs.add(clazz) : fails.add(clazz);
        }
        return ImportResult.builder().fails(fails).succs(succs).failCause(failCause).failPos(failPos).build();
    }

    private static Boolean handleField(Field field, HSSFRow row, Object clazz) throws Exception {

        Excel excel = field.getAnnotation(Excel.class);
        if (null == excel) {
            return null;
        }
        HSSFCell cell = row.getCell(excel.column());
        Object cellValue = null;
        if (null != cell) {
            cellValue = Handler.getCellValue(cell);
        }
        if (cellValue != null) {
            field.setAccessible(true);
            field.set(clazz, cellValue.toString());
        }

        // 必要 且 为空
        if (excel.isRequire() && (cellValue == null || cellValue.toString().length() == 0)) {
            return false;
        }
        if (!RegExpStyle.NONE.equals(excel.pattern()) && !RegExpStyle.PICTURE.equals(excel.pattern())) {
            if (cellValue == null || cellValue.toString().length() == 0) {
                return true;
            }
            return match(excel.pattern(), cellValue.toString());
        }
        return true;
    }

    private static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    private static boolean isRowEmpty(HSSFRow row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            HSSFCell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                return false;
            }
        }
        return true;
    }
}