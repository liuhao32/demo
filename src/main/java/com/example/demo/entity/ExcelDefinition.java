package com.example.demo.entity;

import com.example.demo.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YCKJ3275
 */
@Getter
@Setter
public class ExcelDefinition {

    Object[] tableRowValues;

    String[] tableRowTypes;

    Excel[] annotationExcels;

    Integer tableRowCount = 0;

}
