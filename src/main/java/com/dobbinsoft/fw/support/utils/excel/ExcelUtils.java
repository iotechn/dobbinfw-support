package com.dobbinsoft.fw.support.utils.excel;

import com.dobbinsoft.fw.support.model.Page;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.FieldUtils;
import com.dobbinsoft.fw.support.utils.StringUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExcelUtils {

    /**
     * xls 后缀
     */
    private static final String XLS = ".xls";
    /**
     * xlsx 后缀
     */
    private static final String XLS_X = ".xlsx";

    /**
     * sheet页的第一行
     */
    private static final int FIRST_ROW = 0;

    /**
     * 第一个工作簿
     */
    private static final int FIRST_SHEET = 0;

    /**
     * sheet页的第一列
     */
    private static final int FIRST_COL = 0;

    /**
     * 科学计数
     */
    private final static String E = "e";

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORMAT = "yyyy-MM-dd";


    public static <T> List<T> importExcel(InputStream is, String fileName, Class<T> clazz) {
        try (Workbook workbook = getWorkBook(is, fileName)) {
            return importExcel(clazz, workbook);
        } catch (IOException e) {
            log.error("导入解析失败!", e);
            return Collections.emptyList();
        }
    }

    private static <T> List<T> importExcel(Class<T> clazz, Workbook workbook) {
        List<T> list = new ArrayList<T>();
        Field[] fields = getFields(clazz);
        if (Objects.nonNull(workbook)) {
            Sheet sheet = getSheet(workbook, clazz);
            if (sheet == null || sheet.getLastRowNum() == 0) {
                return list;
            }
            // 获得当前sheet的开始行
            int firstRowNum = sheet.getFirstRowNum();
            // 获得当前sheet的结束行
            int lastRowNum = sheet.getLastRowNum();
            for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
                // 获得当前行
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                Object obj;
                try {
                    // 获取POJO无参构造器
                    Constructor<T> constructor = clazz.getConstructor();
                    obj = constructor.newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    log.error("【excel导入】clazz映射地址：{},{}", clazz.getCanonicalName(), "excel导入异常！");
                    throw new RuntimeException("excel导入异常", e);
                }
                boolean setValue = false;
                for (Field field : fields) {
                    // TODO 导入图片适配
                    ExcelColumn excelColumn = field.getDeclaredAnnotation(ExcelColumn.class);
                    if (Objects.isNull(excelColumn) || excelColumn.ignore()) {
                        continue;
                    }
                    Cell cell = row.getCell(excelColumn.index());
                    if (excelColumn.rowIndex() >= rowNum) {
                        break;
                    }
                    if (!setValue) {
                        setValue = true;
                    }
                    Object value = getCellValue(cell, field);
                    createBean(field, obj, value);
                }
                if (setValue) {
                    list.add((T) obj);
                }
            }
        }
        return list;
    }

    private static <T> void createBean(Field field, T newInstance, Object value) {
        field.setAccessible(true);
        try {
            if (value == null) {
                field.set(newInstance, null);
            } else if (Long.class.equals(field.getType())) {
                field.set(newInstance, Long.valueOf(String.valueOf(value).trim()));
            } else if (String.class.equals(field.getType())) {
                field.set(newInstance, String.valueOf(value).trim());
            } else if (Integer.class.equals(field.getType())) {
                field.set(newInstance, Integer.valueOf(String.valueOf(value).trim()));
            } else if (int.class.equals(field.getType())) {
                field.set(newInstance, Integer.parseInt(String.valueOf(value).trim()));
            } else if (Date.class.equals(field.getType())) {
                field.set(newInstance, value);
            } else if (Boolean.class.equals(field.getType())) {
                field.set(newInstance, value);
            } else if (Double.class.equals(field.getType())) {
                field.set(newInstance, Double.valueOf(String.valueOf(value).trim()));
            } else if (LocalDate.class.equals(field.getType())) {
                field.set(newInstance, ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            } else if (LocalDateTime.class.equals(field.getType())) {
                field.set(newInstance, ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (BigDecimal.class.equals(field.getType())){
                field.set(newInstance, new BigDecimal(String.valueOf(value).trim()));
            } else {
                field.set(newInstance, value);
            }
        } catch (IllegalAccessException e) {
            log.error("【excel导入】clazz映射地址：{},{}", newInstance, "excel实体赋值类型转换异常！", e);
            throw new RuntimeException("excel实体赋值类型转换异常", e);
        }
    }

    private static Object getCellValue(Cell cell, Field field) {
        Object cellValue = null;
        if (cell == null) {
            return cellValue;
        }
        // 把数字当成String来读，避免出现1读成1.0的情况
        // 判断数据的类型
        switch (cell.getCellType()) {
            case NUMERIC:

                if (cell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isValidExcelDate(cell.getNumericCellValue())) {
                        CellStyle style = cell.getCellStyle();
                        if (style == null) {
                            return false;
                        }
                        int i = style.getDataFormat();
                        String f = style.getDataFormatString();
                        boolean isDate = DateUtil.isADateFormat(i, f);
                        if (isDate) {
                            return cell.getDateCellValue();
                        }
                    }
                }
                // 防止科学计数进入
                if (String.valueOf(cell.getNumericCellValue()).toLowerCase().contains(E)) {
                    if (field.getType().equals(String.class)) {
                        return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
                    } else {
                        throw new RuntimeException("excel数据类型错误，请将数字转文本类型！！");
                    }
                }
                if ((int) cell.getNumericCellValue() != cell.getNumericCellValue()) {
                    // double 类型
                    cellValue = cell.getNumericCellValue();
                } else {
                    cellValue = (int) cell.getNumericCellValue();
                }
                break;
            // 字符串
            case STRING:
                cellValue = cell.getStringCellValue() == null ? "" : cell.getStringCellValue().trim();
                break;
            // Boolean
            case BOOLEAN:
                cellValue = cell.getStringCellValue() == null ? "" : String.valueOf(cell.getBooleanCellValue());
                break;
            // 公式
            case FORMULA:
                cellValue = cell.getStringCellValue() == null ? "" : String.valueOf(cell.getCellFormula());
                break;
            // 空值
            case BLANK:
                break;
            // 故障
            case ERROR:
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    private static <T> Sheet getSheet(Workbook workbook, Class<T> clazz) {
        Sheet sheet = null;
        if (clazz.isAnnotationPresent(ExcelSheet.class)) {
            ExcelSheet excelSheet = clazz.getDeclaredAnnotation(ExcelSheet.class);
            sheet = workbook.getSheetAt(excelSheet.index());
        } else {
            sheet = workbook.getSheetAt(FIRST_SHEET);
        }
        return sheet;
    }

    private static <T> Field[] getFields(Class<T> clazz) {
        //获取对象总数量
        Field[] fields = FieldUtils.getAllFields(clazz);
        if (fields.length == 0) {
            log.error("【excel导入】clazz映射地址：{},{}", clazz.getCanonicalName(), "实体空异常！");
            throw new RuntimeException("excel导入】clazz映射地址：" + clazz.getCanonicalName() + ",实体空异常！");
        }
        for (Field field : fields) {
            if (!field.isAnnotationPresent(ExcelColumn.class)) {
                log.error("【excel导入】clazz映射地址：{},{}", clazz.getCanonicalName(), "实体空Excel注解异常！");
                throw new RuntimeException("【excel导入】clazz映射地址：" + clazz.getCanonicalName() + ", 实体空Excel注解异常！");
            }
        }
        return fields;
    }

    private static Workbook getWorkBook(InputStream is, String fileName) {
        // 创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        // 获取excel文件的io流
        try {
            // 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith(XLS)) {
                // 2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(XLS_X)) {
                // 2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("excel 转换 HSSFWorkbook 异常！", e);
        }
        return workbook;
    }


    /**
     * 导出表格到流
     * @param os
     * @param data
     * @param clazz
     * @param <T>
     */
    public static <T> void exportExcel(OutputStream os, List<T> data, Class<T> clazz) {
        //实例化XSSFWorkbook
        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            //创建一个Excel表单，参数为sheet的名字
            Sheet sheet = setSheet(clazz, workbook);
            //设置单元格并赋值
            setData(workbook, sheet, data, setTitle(workbook, sheet, clazz));
            workbook.write(os);
            log.info("导出解析成功!");
        } catch (Exception e) {
            log.error("导出解析失败!", e);
        }
    }

    public static <T> byte[] exportExcel(List<T> data, Class<T> clazz) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exportExcel(os, data, clazz);
//        os.close(); ByteArrayOutputStream不需要关流
        return os.toByteArray();
    }

    /**
     * 导出表格到多个sheet
     * @param os
     * @param dataList  第0个数组，对应第0个class
     * @param clazzList 第0个class，对应第0个数组
     */
    public static void exportExcelMultiSheets(OutputStream os, List<List<?>> dataList, List<Class<?>> clazzList) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            //创建一个Excel表单，参数为sheet的名字
            for (int i = 0; i < clazzList.size(); i++) {
                Class<?> clazz = clazzList.get(i);
                List<?> data = dataList.get(i);
                Sheet sheet = setSheet(clazz, workbook);
                //设置单元格并赋值
                setData(workbook, sheet, data, setTitle(workbook, sheet, clazz));
            }


            workbook.write(os);
            log.info("导出解析成功!");
        } catch (Exception e) {
            log.error("导出解析失败!", e);
        }
    }

    /**
     * 导出大表格到流
     * @param os
     * @param adapter 大文件导出适配器
     * @param <T>
     */
    public static <T> void exportBigExcel(OutputStream os, ExcelBigExportAdapter<T> adapter) {
        Class<T> clazz = adapter.clazz();
        Sheet sheet = null;
        Field[] fields = FieldUtils.getAllFields(clazz);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200)) {
            Page<T> dataBuffer = null;
            do {
                dataBuffer = adapter.getData(adapter.getPageNo().getAndIncrement());
                if (!dataBuffer.hasPrevious()) {
                    // 首页，创建sheet， 设置表头
                    sheet = setSheet(clazz, workbook);
                    setTitle(workbook, sheet, clazz);
                }
                setData(workbook, sheet, dataBuffer.getItems(), fields);
            } while (dataBuffer.hasNext() || CollectionUtils.isNotEmpty(dataBuffer.getItems()));
            workbook.write(os);
            log.info("导出解析成功!");
        } catch (Exception e) {
            log.error("导出解析失败!", e);
        }
    }

    public static <T> byte[] exportBigExcel(ExcelBigExportAdapter<T> adapter) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exportBigExcel(os, adapter);
        return os.toByteArray();
    }

    /**
     * 设置工作簿
     * @param clazz
     * @param workbook
     * @return
     * @param <T>
     */
    private static <T> Sheet setSheet(Class<T> clazz, Workbook workbook) {
        Class<?> superClass = clazz;
        do {
            if (superClass.isAnnotationPresent(ExcelSheet.class)) {
                ExcelSheet excelSheet = superClass.getDeclaredAnnotation(ExcelSheet.class);
                Sheet sheet = workbook.createSheet(StringUtils.isEmpty(excelSheet.title()) ? "sheet" : excelSheet.title());
                sheet.setDefaultRowHeight((short) (excelSheet.rowHeight() * 20));
                return sheet;
            }
            superClass = superClass.getSuperclass();
        } while (superClass != null && superClass != Object.class);
        return workbook.createSheet("sheet");
    }

    /**
     * 设置表头
     * @param workbook
     * @param sheet
     * @param clazz
     * @return
     */
    private static Field[] setTitle(Workbook workbook, Sheet sheet, Class clazz) {
        Field[] fields = FieldUtils.getAllFields(clazz);
        try {
            CellStyle style = createXssfCellStyle(workbook);
            setHeaderTemplate(sheet, clazz, style);
            setColumnTemplate(sheet, fields, style);
            setColumnTitle(sheet, fields, style);
        } catch (Exception e) {
            log.info("导出时设置表头失败！", e);
        }
        return fields;
    }

    private static CellStyle createXssfCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置字体
        Font font = workbook.createFont();
//        font.setBold(true);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        // 设置背景颜色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置边框样式
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置日期格式
        DataFormat fmt = workbook.createDataFormat();
        style.setDataFormat(fmt.getFormat("m/d/yy h:mm"));

        return style;
    }

    private static void setColumnTemplate(Sheet sheet, Field[] fields, CellStyle style) {
        int nextRow = sheet.getLastRowNum() + 1;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ExcelTemplate.class)) {
                ExcelTemplate template = field.getDeclaredAnnotation(ExcelTemplate.class);
                CellRangeAddress region = new CellRangeAddress(nextRow, nextRow + template.rowspan(), template.colIndex(), template.colIndex() + template.colspan());
                Row row = sheet.getRow(nextRow);
                if (Objects.isNull(row)) {
                    row = sheet.createRow(nextRow);
                }
                sheet.addMergedRegion(region);
                Cell cell = row.createCell(template.colIndex());
                cell.setCellValue(template.value());
                cell.setCellStyle(style);
                Row lastRow = sheet.getRow(nextRow + template.rowspan());
                if (Objects.isNull(lastRow)) {
                    sheet.createRow(nextRow + template.rowspan());
                }
            }
        }
    }

    private static void setColumnTitle(Sheet sheet, Field[] fields, CellStyle style) {
        int nextRow = sheet.getLastRowNum() + 1;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                ExcelColumn excelColumn = field.getDeclaredAnnotation(ExcelColumn.class);
                sheet.setColumnWidth(excelColumn.index(), excelColumn.width() * 256);
                Row row = sheet.getRow(nextRow);
                if (Objects.isNull(row)) {
                    row = sheet.createRow(nextRow);
                }
                Cell cell = row.createCell(excelColumn.index());
                if (StringUtils.isNotEmpty(excelColumn.subTitle())) {

                    String part1 = excelColumn.title() + "\n";
                    String part2 = excelColumn.subTitle();

                    XSSFRichTextString richTextString = new XSSFRichTextString(part1 + part2);
                    Workbook workbook = sheet.getWorkbook();
                    Font font1 = workbook.createFont();
                    font1.setFontName("Arial");
                    font1.setFontHeightInPoints((short) 14);
                    font1.setColor(IndexedColors.BLACK.getIndex());
                    richTextString.applyFont(0, part1.length(), font1);

                    // 创建并设置第二种字体
                    Font font2 = workbook.createFont();
                    font2.setFontName("Arial");
                    font2.setFontHeightInPoints((short) 10);
                    font2.setColor(IndexedColors.BLACK.getIndex());

                    richTextString.applyFont(part1.length(), part1.length() + part2.length(), font2);
                    cell.setCellValue(richTextString);
                    style.setWrapText(true);
                } else {
                    cell.setCellValue(excelColumn.title());
                }
                cell.setCellStyle(style);
                String[] enums = excelColumn.enums();
                if (enums.length > 0) {
                    // 如果enums存在，则需要限定这一列，只能输入/选择枚举中的值
                    setValidation(sheet, enums, nextRow + 1, nextRow + 100, excelColumn.index(), excelColumn.index());
                }
            }
        }
    }

    private static void setValidation(Sheet sheet, String[] enums, int firstRow, int lastRow, int firstCol, int lastCol) {
        if (sheet instanceof XSSFSheet) {
            // 此处不抛出异常，如果不支持校验器的Sheet，则直接不校验
            DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(enums);
            CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            validation.setShowErrorBox(true);
            sheet.addValidationData(validation);
        }
    }

    private static void setHeaderTemplate(Sheet sheet, Class clazz, CellStyle style) {
        if (clazz.isAnnotationPresent(ExcelTemplate.class)) {
            ExcelTemplate template = (ExcelTemplate) clazz.getDeclaredAnnotation(ExcelTemplate.class);
            CellRangeAddress region = new CellRangeAddress(FIRST_ROW, FIRST_ROW + template.rowspan(), template.colIndex(), template.colIndex() + template.colspan());
            Row row = sheet.createRow(FIRST_ROW);
            sheet.addMergedRegion(region);
            Cell cell = row.createCell(FIRST_COL);
            cell.setCellValue(template.value());
            cell.setCellStyle(style);
        }
    }

    private static <T> void setData(Workbook workbook, Sheet sheet, List<T> data, Field[] fields) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        try {
            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(lastRow + i + 1);
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ExcelColumn.class)) {
                        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                        Object value = field.get(data.get(i));
                        if (Objects.isNull(value)) {
                            continue;
                        }
                        try {
                            if (field.getType().equals(Double.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((Double) value);
                                setDataCellStyle(workbook, excelColumn, cell);
                            } else if (field.getType().equals(Date.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((Date) value);
                                setDataCellStyle(workbook, cell,
                                        StringUtils.isNoneBlank(excelColumn.format()) ? excelColumn.format() : TIME_FORMAT);
                            } else if (field.getType().equals(LocalDate.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((LocalDate) value);
                                setDataCellStyle(workbook, cell,
                                        StringUtils.isNoneBlank(excelColumn.format()) ? excelColumn.format() : DATE_FORMAT);
                            } else if (field.getType().equals(LocalDateTime.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((LocalDateTime) value);
                                setDataCellStyle(workbook, cell,
                                        StringUtils.isNoneBlank(excelColumn.format()) ? excelColumn.format() : TIME_FORMAT);
                            } else if (field.getType().equals(Integer.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((Integer) value);
                                setDataCellStyle(workbook, excelColumn, cell);
                            } else if (field.getType().equals(Long.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((Long) value);
                                setDataCellStyle(workbook, excelColumn, cell);
                            } else if (field.getType().equals(ExcelImage.class)) {
                                // 如果是字节数组，则尝试从单元格中读取图片
                                ExcelImage excelImage = (ExcelImage) value;
                                insertImage(sheet, excelImage.getBytes(), row.getRowNum(), excelColumn.index(), excelImage.getName());
                            } else if (field.getType().equals(BigDecimal.class)) {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue(value.toString());
                                setDataCellStyle(workbook, excelColumn, cell);
                            } else {
                                Cell cell = row.createCell(excelColumn.index());
                                cell.setCellValue((String) value);
                                setDataCellStyle(workbook, excelColumn, cell);
                            }
                        } catch (Exception e) {
                            log.error("[Excel 导出] 单元格赋值失败 column:{}, value:{}", excelColumn.title(), value, e);
                        }
                    }
                }
            }
            log.info("[Excel 导出] 表格赋值 完成，总影响行数:{}", data.size());
        } catch (Exception e) {
            log.error("[Excel 导出] 表格赋值 异常", e);
        }
    }

    private static void insertImage(Sheet sheet, byte[] imageBytes, int row, int col, String imageName) throws IOException {
        String fileExtension = StringUtils.getFileExtension(imageName);
        int pictureTypePng = Workbook.PICTURE_TYPE_PNG;
        if ("jpg".equals(fileExtension) || "jpeg".equals(fileExtension)) {
            pictureTypePng = Workbook.PICTURE_TYPE_JPEG;
        }
        int pictureIdx = sheet.getWorkbook().addPicture(imageBytes, pictureTypePng);
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();

        // 设置图片插入位置
        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 1); // 图片贴合单元格，因此这里列+1
        anchor.setRow2(row + 1); // 图片贴合单元格，因此这里行+1
        Picture pict = drawing.createPicture(anchor, pictureIdx);

        // 获取单元格的宽度和高度
        double cellWidthInPoints = sheet.getColumnWidthInPixels(col);
        double cellHeightInPoints = sheet.getRow(row).getHeightInPoints();

        // 将图片调整为适应单元格
        pict.resize();

        // 设置图片的大小，使其贴合单元格
        pict.resize(cellWidthInPoints / pict.getImageDimension().getWidth(), cellHeightInPoints / pict.getImageDimension().getHeight());
    }

    // style 需要缓存,如果是同一个Workbook，

    private final static Cache<String, CellStyle> styleCache = Caffeine.newBuilder()
            .expireAfterWrite(120, TimeUnit.SECONDS)
            .build();


    private static void setDataCellStyle(Workbook workbook, Cell cell, String format) {
        CellStyle ifPresent = styleCache.getIfPresent(workbook.toString() + "___" + format);
        if (ifPresent != null) {
            cell.setCellStyle(ifPresent);
            return;
        }
        CellStyle style = workbook.createCellStyle();
        DataFormat fmt = workbook.createDataFormat();
        style.setDataFormat(fmt.getFormat(format));
        cell.setCellStyle(style);
        styleCache.put(workbook + "___" + format, style);
    }

    private static void setDataCellStyle(Workbook workbook, ExcelColumn excelColumn, Cell cell) {
        String cacheKey = workbook.toString() + "___" + workbook.getActiveSheetIndex() + "___" + excelColumn.format();
        CellStyle ifPresent = styleCache.getIfPresent(cacheKey);
        if (ifPresent != null) {
            cell.setCellStyle(ifPresent);
            return;
        }
        CellStyle style = workbook.createCellStyle();
        DataFormat fmt = workbook.createDataFormat();
        if (StringUtils.isNoneBlank(excelColumn.format())) {
            style.setDataFormat(fmt.getFormat(excelColumn.format()));
        }
        cell.setCellStyle(style);
        styleCache.put(cacheKey, style);
    }

//    private static void setBrowser(HttpServletResponse response, Workbook workbook, String fileName) {
//        try (OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
//            //设置response的Header
//            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
//            response.setContentType("application/vnd.ms-excel;charset=gb2312");
//            //将excel写入到输出流中
//            workbook.write(os);
//            os.flush();
//            log.info("设置浏览器下载成功！");
//        } catch (Exception e) {
//            log.error("设置浏览器下载失败！", e);
//        }
//    }
}