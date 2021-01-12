package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.List;

/**
 * @author YCKJ3275
 */
@Getter
@Setter
public class WorkBook {

    private HSSFWorkbook wb;

    private List<HSSFSheet> sheets;

    private List<CellStyle> cellStyles;

    public HSSFSheet getDefaultSheet() {
        return sheets.get(0);
    }

    public HSSFSheet getSheet(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= sheets.size()) {
            index = sheets.size() - 1;
        }
        return sheets.get(index);
    }

    public CellStyle getDefaultCellStyle() {
        return cellStyles.get(0);
    }
}
