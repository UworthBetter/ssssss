package com.qkyd.health.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 文件解析器接口
 * 
 * 定义Excel和CSV文件的通用解析接口，支持批量数据导入功能
 * 
 * @author QKYD Team
 * @version 1.0
 */
public interface IFileParser {
    
    /**
     * 支持的文件类型枚举
     */
    enum FileType {
        EXCEL_XLS("xls", "Excel 97-2003格式"),
        EXCEL_XLSX("xlsx", "Excel 2007+格式"),
        CSV("csv", "逗号分隔值文件");
        
        private final String extension;
        private final String description;
        
        FileType(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }
        
        public String getExtension() {
            return extension;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 根据文件扩展名获取文件类型
         * 
         * @param extension 文件扩展名（不含点号）
         * @return 文件类型，如果不支持则返回null
         */
        public static FileType fromExtension(String extension) {
            if (extension == null) {
                return null;
            }
            for (FileType type : values()) {
                if (type.extension.equalsIgnoreCase(extension)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * 解析文件并返回数据列表
     * 
     * @param filePath 文件路径
     * @return 解析结果列表，每行数据为一个Map，键为表头，值为字段值
     * @throws Exception 解析异常
     */
    List<Map<String, Object>> parse(String filePath) throws Exception;
    
    /**
     * 解析文件并返回数据列表
     * 
     * @param file 文件对象
     * @return 解析结果列表，每行数据为一个Map，键为表头，值为字段值
     * @throws Exception 解析异常
     */
    List<Map<String, Object>> parse(File file) throws Exception;
    
    /**
     * 解析文件并返回数据列表（带工作表名参数，用于Excel多Sheet）
     * 
     * @param filePath 文件路径
     * @param sheetName 工作表名称（仅Excel有效）
     * @return 解析结果列表，每行数据为一个Map，键为表头，值为字段值
     * @throws Exception 解析异常
     */
    List<Map<String, Object>> parse(String filePath, String sheetName) throws Exception;
    
    /**
     * 获取支持的工作表列表（仅Excel有效）
     * 
     * @param filePath 文件路径
     * @return 工作表名称列表，CSV文件返回空列表
     * @throws Exception 解析异常
     */
    List<String> getSheetNames(String filePath) throws Exception;
    
    /**
     * 获取支持的文件类型
     * 
     * @return 支持的文件类型数组
     */
    FileType[] getSupportedFileTypes();
    
    /**
     * 验证文件是否为支持的格式
     * 
     * @param filePath 文件路径
     * @return 是否支持
     */
    boolean isSupported(String filePath);
    
    /**
     * 获取文件行数（不含表头）
     * 
     * @param filePath 文件路径
     * @return 数据行数
     * @throws Exception 解析异常
     */
    int getRowCount(String filePath) throws Exception;
}
