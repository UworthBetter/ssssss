package com.qkyd.health.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV文件解析器
 * 
 * 功能特性:
 * - 支持逗号分隔
 * - 支持引号包裹字段(双引号)
 * - 支持字段内包含逗号(通过引号包裹)
 * - 支持字段内包含换行符
 * - 支持转义引号(双引号转义为"")
 * - 自动处理编码(支持UTF-8, GBK等)
 * - 空行自动跳过
 * 
 * @author QKYD Team
 * @version 1.0
 */
public class CsvParser implements IFileParser {
    
    /**
     * 默认字符编码
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    
    /**
     * CSV字段分隔符
     */
    private static final char DELIMITER = ',';
    
    /**
     * CSV引号字符
     */
    private static final char QUOTE = '"';
    
    /**
     * CSV换行符
     */
    private static final String LINE_SEPARATOR = "\n";
    
    /**
     * 读取CSV文件并解析为List<Map<String, Object>>
     * 第一行作为表头,后续行作为数据
     * 
     * @param filePath CSV文件路径
     * @return 解析结果列表,每行数据为一个Map,键为表头,值为字段值
     * @throws CsvParseException CSV解析异常
     */
    public List<Map<String, Object>> parse(String filePath) throws CsvParseException {
        return parseWithCharset(filePath, DEFAULT_CHARSET.name());
    }
    
    /**
     * 读取CSV文件并解析为List<Map<String, Object>>
     * 第一行作为表头,后续行作为数据
     *
     * @param filePath CSV文件路径
     * @param charsetName 字符编码名称(如UTF-8, GBK)
     * @return 解析结果列表,每行数据为一个Map,键为表头,值为字段值
     * @throws CsvParseException CSV解析异常
     */
    public List<Map<String, Object>> parseWithCharset(String filePath, String charsetName) throws CsvParseException {
        // 参数校验
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new CsvParseException("文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new CsvParseException("文件不存在: " + filePath);
        }
        if (!file.isFile()) {
            throw new CsvParseException("路径不是有效文件: " + filePath);
        }

        // 验证字符编码
        Charset charset;
        try {
            charset = Charset.forName(charsetName);
        } catch (Exception e) {
            throw new CsvParseException("不支持的字符编码: " + charsetName, e);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        List<String> headers = null;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), charset))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // 处理空行
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 解析CSV行
                List<String> fields = parseLine(line, lineNumber);

                // 第一行作为表头
                if (headers == null) {
                    headers = fields;
                    // 检查表头是否为空
                    if (headers.isEmpty()) {
                        throw new CsvParseException("第1行: 表头不能为空");
                    }
                    continue;
                }

                // 将数据行转换为Map
                Map<String, Object> row = mapRow(headers, fields, lineNumber);
                result.add(row);
            }

        } catch (UnsupportedEncodingException e) {
            throw new CsvParseException("编码异常: 不支持的编码 " + charsetName, e);
        } catch (IOException e) {
            throw new CsvParseException("IO异常: 读取文件失败 " + filePath, e);
        }

        // 检查是否有数据
        if (headers == null) {
            throw new CsvParseException("CSV文件为空或只包含空行");
        }

        return result;
    }

    // ==================== IFileParser 接口实现 ====================

    @Override
    public List<Map<String, Object>> parse(File file) throws Exception {
        return parse(file.getAbsolutePath());
    }

    @Override
    public List<Map<String, Object>> parse(String filePath, String sheetName) throws Exception {
        // CSV不支持多Sheet，忽略sheetName参数
        return parseWithCharset(filePath, DEFAULT_CHARSET.name());
    }

    @Override
    public List<String> getSheetNames(String filePath) throws Exception {
        // CSV不支持多Sheet，返回空列表
        return Collections.emptyList();
    }

    @Override
    public FileType[] getSupportedFileTypes() {
        return new FileType[]{FileType.CSV};
    }

    @Override
    public boolean isSupported(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        String extension = getFileExtension(filePath);
        return FileType.CSV.getExtension().equalsIgnoreCase(extension);
    }

    @Override
    public int getRowCount(String filePath) throws Exception {
        List<Map<String, Object>> data = parse(filePath);
        return data.size();
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
    
    /**
     * 解析单行CSV数据
     * 
     * @param line CSV行字符串
     * @param lineNumber 行号(用于错误提示)
     * @return 字段列表
     * @throws CsvParseException 解析异常
     */
    private List<String> parseLine(String line, int lineNumber) throws CsvParseException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (inQuotes) {
                // 在引号内的处理
                if (c == QUOTE) {
                    // 检查是否为转义的引号("")
                    if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                        // 转义引号,添加一个引号到字段
                        field.append(QUOTE);
                        i++; // 跳过下一个引号
                    } else {
                        // 结束引号
                        inQuotes = false;
                    }
                } else {
                    // 普通字符,添加到字段
                    field.append(c);
                }
            } else {
                // 不在引号内的处理
                if (c == QUOTE) {
                    // 开始引号(必须是字段开始或紧跟分隔符)
                    if (field.length() > 0) {
                        throw new CsvParseException("第" + lineNumber + "行: 引号位置错误,必须在字段开始");
                    }
                    inQuotes = true;
                } else if (c == DELIMITER) {
                    // 分隔符,结束当前字段
                    fields.add(field.toString());
                    field = new StringBuilder();
                } else {
                    // 普通字符,添加到字段
                    field.append(c);
                }
            }
        }
        
        // 添加最后一个字段
        fields.add(field.toString());
        
        // 检查引号是否闭合
        if (inQuotes) {
            throw new CsvParseException("第" + lineNumber + "行: 引号未闭合");
        }
        
        return fields;
    }
    
    /**
     * 将表头和数据字段映射为Map
     * 
     * @param headers 表头列表
     * @param fields 字段值列表
     * @param lineNumber 行号(用于错误提示)
     * @return 映射结果
     * @throws CsvParseException 字段数量不匹配时抛出异常
     */
    private Map<String, Object> mapRow(List<String> headers, List<String> fields, int lineNumber) 
            throws CsvParseException {
        
        if (fields.size() != headers.size()) {
            throw new CsvParseException(String.format(
                "第%d行: 字段数量不匹配,期望%d个,实际%d个", 
                lineNumber, headers.size(), fields.size()));
        }
        
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            String value = fields.get(i);
            
            // 移除字段值两端的空白字符
            value = value.trim();
            
            // 尝试转换数值类型
            Object convertedValue = convertValue(value);
            row.put(header, convertedValue);
        }
        
        return row;
    }
    
    /**
     * 尝试将字符串值转换为合适的类型
     * 
     * @param value 字符串值
     * @return 转换后的值(String, Integer, Long, Double, Boolean)
     */
    private Object convertValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // 尝试转换为布尔值
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }
        
        // 尝试转换为整数
        try {
            if (value.matches("-?\\d+")) {
                long num = Long.parseLong(value);
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
            if (value.matches("-?\\d+\\.\\d+([eE][+-]?\\d+)?")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // 不是浮点数
        }
        
        // 默认返回字符串
        return value;
    }
    
    /**
     * CSV解析异常
     */
    public static class CsvParseException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public CsvParseException(String message) {
            super(message);
        }
        
        public CsvParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
