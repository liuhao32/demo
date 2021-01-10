package com.example.demo.styles;

import com.example.demo.constpool.ExcelConst;
import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExcelCellStyle {

    private final Map<String, CellStyle> VERTICAL_ALIGNMENT = new HashMap<>();
    private final Map<String, CellStyle> GROUND_COLOR = new HashMap<>();

    private HSSFWorkbook hssfWorkbook;

    private static volatile ExcelCellStyle excelCellStyle;

    // 私有的构造方法
    private ExcelCellStyle() {
    }

    public static ExcelCellStyle getSingletonCellStyle() {

        if (excelCellStyle == null) {
            synchronized (ExcelCellStyle.class) {
                if (excelCellStyle == null) {
                    excelCellStyle = new ExcelCellStyle();
                }
            }
        }
        return excelCellStyle;
    }

    public ExcelCellStyle initialCellStyle(HSSFWorkbook hssfWorkbook) {
        this.hssfWorkbook = hssfWorkbook;
        return excelCellStyle;
    }

    public void initialDefault() {
        initialDefaultAlignment();
        initialDefaultGroundColor();
    }

    public void initialDefaultAlignment() {
        CellStyle cellStyle = hssfWorkbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        VERTICAL_ALIGNMENT.put(ExcelConst.VERTICAL_ALIGNMENT_CENTER, cellStyle);
    }

    public void initialDefaultGroundColor() {
        CellStyle cellStyle = hssfWorkbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setFillForegroundColor(HSSFColor.RED.index);
        cellStyle.setFillBackgroundColor(HSSFColor.RED.index);
        cellStyle.setFillPattern(HSSFCellStyle.BIG_SPOTS);
        GROUND_COLOR.put(ExcelConst.GROUND_COLOR, cellStyle);
    }

    public Map<String, CellStyle> getAlignments() {
        return VERTICAL_ALIGNMENT;
    }

    public Map<String, CellStyle> getGroundColors() {
        return GROUND_COLOR;
    }

}
