package com.example.demo.controller;

import com.example.demo.utils.ExcelExportUtil;
import com.example.demo.utils.ExcelImportUtil;
import com.example.demo.result.ImportResult;
import com.example.demo.entity.Device;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * Copyright (C), 2020
 *
 * @author: liuhao
 * @date: 2020/11/21 22:57
 * @description:
 */
@RestController
public class TestController {


    @PostMapping("/testImport")
    public void testImport(MultipartFile file) throws Exception {

        InputStream inputStream = file.getInputStream();
        ImportResult<?> result = ExcelImportUtil.importExcel(inputStream, Device.class);
        System.out.println(result);

        String filePath = System.currentTimeMillis() + ".xls";

        ExcelExportUtil.initialExcel(result.getFails().get(0).getClass(), new String[]{"错误数据导出表"});
        ExcelExportUtil.fillExcelRows(result.getFails());
        ExcelExportUtil.outputExcel(filePath);
    }

    @PostMapping("/testExport")
    public void testExport(@RequestBody List<Device> result) throws Exception {

        String filePath = System.currentTimeMillis() + ".xls";

        ExcelExportUtil.initialExcel(result.get(0).getClass(), new String[]{"错误数据导出表"});
        ExcelExportUtil.fillExcelRows(result);
        ExcelExportUtil.outputExcel(filePath);
    }
}
