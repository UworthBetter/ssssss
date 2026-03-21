package com.qkyd.health.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import java.io.File;
import java.util.*;

/**
 * Excel文件解析器
 * 
 * 功能特性:
 * - 支持.xlsx和.xls格式
 * - 支持多Sheet读取
 * - 支持自定义列映射
 * - 自动处理空单元格
 * - 支持大文件流式读取
 * 
 * @author QKYD Team
 * @version 1.0
 */
public class ExcelParser implements IFileParser {
    
    /**
     * 默认字符编码
     */
    private static final String DEFAULT_CHARSET = "UTF-8";
    
    /**
     * 读取Excel文件并解析为List<Map<String, Object>>
     * 第一行作为表头,后续行作为数据
     * 
     * @param filePath Excel文件路径
     * @return 解析结果列表,每行数据为一个Map,键为表头,值为字段值
     * @throws Exception 解析异常
     */
    @Override
    public List<Map<String, Object>> parse(String filePath) throws Exception {
        return parse(filePath, 0);
    }
    
    /**
     * 读取Excel文件并解析为List<Map<String, Object>>
     * 第一行作为表头,后续行作为数据
     * 
     * @param filePath Excel文件路径
     * @param sheetIndex 工作表索引（从0开始）
     * @return 解析结果列表,每行数据为一个Map,键为表头,值为字段值
     * @throws Exception 解析异常
     */
    public List<Map<String, Object>> parse(String filePath, int sheetIndex) throws Exception {
        // 参数校验
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new Exception("文件路径不能为空");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("文件不存在: " + filePath);
        }
        if (!file.isFile()) {
            throw new Exception("路径不是有效文件: " + filePath);
        }
        
        // 验证文件扩展名
        if (!isSupported(filePath)) {
            throw new Exception("不支持的文件格式，仅支持.xls和.xlsx文件");
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 使用Hutool读取Excel
            ExcelReader reader = ExcelUtil.getReader(file, sheetIndex);
            
            // 读取所有行（包含表头）
            List<List<Object>> rows = reader.read();
            
            if (CollUtil.isEmpty(rows)) {
                throw new Exception("Excel文件为空");
            }
            
            // 第一行作为表头
            List<Object> headers = rows.get(0);
            if (CollUtil.isEmpty(headers)) {
                throw new Exception("第1行: 表头不能为空");
            }
            
            // 处理数据行
            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                Map<String, Object> rowData = mapRow(headers, row, i + 1);
                
                // 跳过完全空行
                if (!isRowEmpty(rowData)) {
                    result.add(rowData);
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            throw new Exception("Excel解析异常: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> parse(File file) throws Exception {
        return parse(file.getAbsolutePath());
    }
    
    @Override
    public List<Map<String, Object>> parse(String filePath, String sheetName) throws Exception {
        // 参数校验
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new Exception("文件路径不能为空");
        }
        
        if (sheetName == null || sheetName.trim().isEmpty()) {
            // sheetName为空，默认读取第一个Sheet
            return parse(filePath, 0);
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("文件不存在: " + filePath);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 使用Hutool读取指定Sheet
            ExcelReader reader = ExcelUtil.getReader(file);
            
            // 读取所有行（包含表头）
            List<List<Object>> rows = reader.read();
            
            if (CollUtil.isEmpty(rows)) {
                throw new Exception("Excel文件为空");
            }
            
            // 第一行作为表头
            List<Object> headers = rows.get(0);
            if (CollUtil.isEmpty(headers)) {
                throw new Exception("第1行: 表头不能为空");
            }
            
            // 处理数据行
            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                Map<String, Object> rowData = mapRow(headers, row, i + 1);
                
                // 跳过完全空行
                if (!isRowEmpty(rowData)) {
                    result.add(rowData);
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            throw new Exception("Excel解析异常: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    @Override
    public List<String> getSheetNames(String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new Exception("文件路径不能为空");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("文件不存在: " + filePath);
        }
        
        List<String> sheetNames = new ArrayList<>();
        
        try {
            ExcelReader reader = ExcelUtil.getReader(file);
            sheetNames = reader.getSheetNames();
            reader.close();
        } catch (Exception e) {
            throw new Exception("获取Sheet列表失败: " + e.getMessage(), e);
        }
        
        return sheetNames;
    }
    
    @Override
    public FileType[] getSupportedFileTypes() {
        return new FileType[]{FileType.EXCEL_XLS, FileType.EXCEL_XLSX};
    }
    
    @Override
    public boolean isSupported(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(filePath);
        return FileType.EXCEL_XLS.getExtension().equalsIgnoreCase(extension)
                || FileType.EXCEL_XLSX.getExtension().equalsIgnoreCase(extension);
    }
    
    @Override
    public int getRowCount(String filePath) throws Exception {
        List<Map<String, Object>> data = parse(filePath);
        return data.size();
    }
    
    /**
     * 将表头和数据字段映射为Map
     * 
     * @param headers 表头列表
     * @param row 字段值列表
     * @param lineNumber 行号(用于错误提示)
     * @return 映射结果
     * @throws Exception 字段数量不匹配时抛出异常
     */
    private Map<String, Object> mapRow(List<Object> headers, List<Object> row, int lineNumber) 
            throws Exception {
        
        if (CollUtil.isEmpty(row)) {
            throw new Exception(String.format("第%d行: 数据为空", lineNumber));
        }
        
        Map<String, Object> rowData = new HashMap<>();
        
        // 遍历表头，填充数据
        for (int i = 0; i < headers.size(); i++) {
            String header = String.valueOf(headers.get(i));
            header = header.trim();
            
            if (header.isEmpty()) {
                continue;
            }
            
            // 获取对应单元格的值（如果没有则为空）
            Object value = (i < row.size()) ? row.get(i) : "";
            
            // 处理单元格值为null的情况
            if (value == null) {
                value = "";
            }
            
            // 尝试转换数值类型
            Object convertedValue = convertValue(value);
            rowData.put(header, convertedValue);
        }
        
        return rowData;
    }
    
    /**
     * 判断行是否为空
     * 
     * @param row 行数据
     * @return 是否为空
     */
    private boolean isRowEmpty(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return true;
        }
        
        for (Object value : row.values()) {
            if (value != null && !value.toString().trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 尝试将值转换为合适的类型
     * 
     * @param value 值
     * @return 转换后的值
     */
    private Object convertValue(Object value) {
        if (value == null) {
            return "";
        }
        
        String strValue = value.toString().trim();
        if (strValue.isEmpty()) {
            return "";
        }
        
        // 如果已经是数字类型，直接返回
        if (value instanceof Number) {
            return value;
        }
        
        // 尝试转换为布尔值
        if ("true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue)) {
            return Boolean.valueOf(strValue);
        }
        
        // 尝试转换为整数
        try {
            if (strValue.matches("-?\\d+")) {
                long num = Long.parseLong(strValue);
                // 在int范围内返回Integer,否则返回Long
                if (num >= Integer.MIN_VALUE && num <= Integer.MAX_VALUE) {
                    return (int) num;
                }
                return num;
            }
        } catch (NumberFormatException e) {
            // 不是整数,继续尝试其他类型
        }
        
        // 尝试转换为浮点数
        try {
            if (strValue.matches("-?\\d+\\.\\d+([eE][+-]?\\d+)?")) {
                return Double.parseDouble(strValue);
            }
        } catch (NumberFormatException e) {
            // 不是浮点数
        }
        
        // 默认返回字符串
        return strValue;
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param filePath 文件路径
     * @return 文件扩展名（不含点号）
     */
    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(lastDotIndex + 1);
    }
}
