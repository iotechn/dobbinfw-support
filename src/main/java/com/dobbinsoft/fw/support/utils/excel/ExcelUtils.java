package com.dobbinsoft.fw.support.utils.excel;

import com.dobbinsoft.fw.support.utils.FieldUtils;
import com.dobbinsoft.fw.support.utils.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    private static final String TIMEF_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static <T> List<T> importExcel(MultipartFile file, Class<T> clazz) {
        checkFile(file);
        Workbook workbook = getWorkBook(file);
        return importExcel(clazz, workbook);
    }

    public static <T> List<T> importExcel(InputStream is, String fileName, Class<T> clazz) {
        Workbook workbook = getWorkBook(is, fileName);
        return importExcel(clazz, workbook);
    }

    @Nullable
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
                    obj = clazz.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    log.error("【excel导入】clazz映射地址：{},{}", clazz.getCanonicalName(), "excel导入异常！");
                    throw new RuntimeException("excel导入异常", e);
                }
                boolean setValue = false;
                for (Field field : fields) {
                    // TODO 导入图片适配
                    ExcelColumn excelColumn = field.getDeclaredAnnotation(ExcelColumn.class);
                    if (Objects.isNull(excelColumn)) {
                        return null;
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

    private static Workbook getWorkBook(MultipartFile file) {
        // 获得文件名
        String fileName = file.getOriginalFilename();
        // 创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        // 获取excel文件的io流
        try (InputStream is = file.getInputStream()){
            // 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            assert fileName != null;
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

    private static void checkFile(MultipartFile file) {
        // 判断文件是否存在
        if (null == file) {
            throw new RuntimeException("文件不存在!!");
        }
        // 获得文件名
        String fileName = file.getOriginalFilename();
        // 判断文件是否是excel文件
        assert fileName != null;
        if (!fileName.endsWith(XLS) && !fileName.endsWith(XLS_X)) {
            throw new RuntimeException(fileName + "不是excel文件");
        }
    }


    public static <T> void exportExcel(HttpServletResponse response, ExcelData<T> data, Class<T> clazz) {
        log.info("导出解析开始，fileName:{}", data.getFileName());
        try {
            //实例化XSSFWorkbook
            XSSFWorkbook workbook = new XSSFWorkbook();
            //创建一个Excel表单，参数为sheet的名字
            XSSFSheet sheet = setSheet(clazz, workbook);
            //设置单元格并赋值
            setData(workbook, sheet, data.getData(), setTitle(workbook, sheet, clazz));
            //设置浏览器下载
            setBrowser(response, workbook, data.getFileName() + XLS_X);
            log.info("导出解析成功!");
        } catch (Exception e) {
            log.error("导出解析失败!", e);
        }
    }

    public static <T> void exportExcel(OutputStream os, List<T> data, Class<T> clazz) {
        try {
            //实例化XSSFWorkbook
            XSSFWorkbook workbook = new XSSFWorkbook();
            //创建一个Excel表单，参数为sheet的名字
            XSSFSheet sheet = setSheet(clazz, workbook);
            //设置单元格并赋值
            setData(workbook, sheet, data, setTitle(workbook, sheet, clazz));
            workbook.write(os);
            log.info("导出解析成功!");
        } catch (Exception e) {
            log.error("导出解析失败!", e);
        }
    }

    private static <T> XSSFSheet setSheet(Class<T> clazz, XSSFWorkbook workbook) {
        Class<?> superClass = clazz;
        do {
            if (superClass.isAnnotationPresent(ExcelSheet.class)) {
                ExcelSheet excelSheet = superClass.getDeclaredAnnotation(ExcelSheet.class);
                XSSFSheet sheet = workbook.createSheet(excelSheet.title());
                sheet.setDefaultRowHeight((short) (excelSheet.rowHeight() * 20));
                return sheet;
            }
            superClass = superClass.getSuperclass();
        } while (superClass != null && superClass != Object.class);
        return workbook.createSheet("sheet");
    }

    private static Field[] setTitle(XSSFWorkbook workbook, XSSFSheet sheet, Class clazz) {
        Field[] fields = FieldUtils.getAllFields(clazz);
        try {
            XSSFCellStyle style = createXssfCellStyle(workbook);
            setHeaderTemplate(sheet, clazz, style);
            setColumnTemplate(sheet, fields, style);
            setColumnTitle(sheet, fields, style);
        } catch (Exception e) {
            log.info("导出时设置表头失败！", e);
        }
        return fields;
    }

    private static XSSFCellStyle createXssfCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();

        // 设置字体
        XSSFFont font = workbook.createFont();
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
        XSSFDataFormat fmt = workbook.createDataFormat();
        style.setDataFormat(fmt.getFormat("m/d/yy h:mm"));

        return style;
    }

    private static void setColumnTemplate(XSSFSheet sheet, Field[] fields, XSSFCellStyle style) {
        int nextRow = sheet.getLastRowNum() + 1;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ExcelTemplate.class)) {
                ExcelTemplate template = field.getDeclaredAnnotation(ExcelTemplate.class);
                CellRangeAddress region = new CellRangeAddress(nextRow, nextRow + template.rowspan(), template.colIndex(), template.colIndex() + template.colspan());
                XSSFRow row = sheet.getRow(nextRow);
                if (Objects.isNull(row)) {
                    row = sheet.createRow(nextRow);
                }
                sheet.addMergedRegion(region);
                XSSFCell cell = row.createCell(template.colIndex());
                cell.setCellValue(template.value());
                cell.setCellStyle(style);
                XSSFRow lastRow = sheet.getRow(nextRow + template.rowspan());
                if (Objects.isNull(lastRow)) {
                    sheet.createRow(nextRow + template.rowspan());
                }
            }
        }
    }

    private static void setColumnTitle(XSSFSheet sheet, Field[] fields, XSSFCellStyle style) {
        int nextRow = sheet.getLastRowNum() + 1;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                ExcelColumn excelColumn = field.getDeclaredAnnotation(ExcelColumn.class);
                sheet.setColumnWidth(excelColumn.index(), excelColumn.width() * 256);
                XSSFRow row = sheet.getRow(nextRow);
                if (Objects.isNull(row)) {
                    row = sheet.createRow(nextRow);
                }
                XSSFCell cell = row.createCell(excelColumn.index());
                if (StringUtils.isNotEmpty(excelColumn.subTitle())) {

                    String part1 = excelColumn.title() + "\n";
                    String part2 = excelColumn.subTitle();

                    XSSFRichTextString richTextString = new XSSFRichTextString(part1 + part2);
                    XSSFWorkbook workbook = sheet.getWorkbook();
                    XSSFFont font1 = workbook.createFont();
                    font1.setFontName("Arial");
                    font1.setFontHeightInPoints((short) 14);
                    font1.setColor(IndexedColors.BLACK.getIndex());
                    richTextString.applyFont(0, part1.length(), font1);

                    // 创建并设置第二种字体
                    XSSFFont font2 = workbook.createFont();
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

    private static void setValidation(XSSFSheet sheet, String[] enums, int firstRow, int lastRow, int firstCol, int lastCol) {
        DataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(enums);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private static void setHeaderTemplate(XSSFSheet sheet, Class clazz, XSSFCellStyle style) {
        if (clazz.isAnnotationPresent(ExcelTemplate.class)) {
            ExcelTemplate template = (ExcelTemplate) clazz.getDeclaredAnnotation(ExcelTemplate.class);
            CellRangeAddress region = new CellRangeAddress(FIRST_ROW, FIRST_ROW + template.rowspan(), template.colIndex(), template.colIndex() + template.colspan());
            XSSFRow row = sheet.createRow(FIRST_ROW);
            sheet.addMergedRegion(region);
            XSSFCell cell = row.createCell(FIRST_COL);
            cell.setCellValue(template.value());
            cell.setCellStyle(style);
        }
    }

    private static <T> void setData(XSSFWorkbook workbook, XSSFSheet sheet, List<T> data, Field[] fields) {
        try {
            int lastRow = sheet.getLastRowNum();
            for (int i = 0; i < data.size(); i++) {
                XSSFRow row = sheet.createRow(lastRow + i + 1);
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ExcelColumn.class)) {
                        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                        Object value = field.get(data.get(i));
                        if (Objects.isNull(value)) {
                            continue;
                        }
                        if (field.getType().equals(Double.class)) {
                            XSSFCell cell = row.createCell(excelColumn.index());
                            cell.setCellValue((Double) value);
                            setDataCellStyle(workbook, excelColumn, cell);
                        } else if (field.getType().equals(Date.class)) {
                            XSSFCell cell = row.createCell(excelColumn.index());
                            cell.setCellValue((Date) value);
                            setDataCellStyle(workbook, cell,
                                    StringUtils.isNoneBlank(excelColumn.format()) ? excelColumn.format() : TIMEF_FORMAT);
                        } else if (field.getType().equals(LocalDate.class)) {
                            XSSFCell cell = row.createCell(excelColumn.index());
                            cell.setCellValue((LocalDate) value);
                            setDataCellStyle(workbook, cell,
                                    StringUtils.isNoneBlank(excelColumn.format()) ? excelColumn.format() : DATE_FORMAT);
                        } else if (field.getType().equals(LocalDateTime.class)) {
                            XSSFCell cell = row.createCell(excelColumn.index());
                            cell.setCellValue((LocalDateTime) value);
                            setDataCellStyle(workbook, cell,
                                    StringUtils.isNoneBlank(excelColumn.format()) ? excelColumn.format() : TIMEF_FORMAT);
                        } else if (field.getType().equals(Integer.class)) {
                            XSSFCell cell = row.createCell(excelColumn.index());
                            cell.setCellValue((Integer) value);
                            setDataCellStyle(workbook, excelColumn, cell);
                        } else if (field.getType().equals(ExcelImage.class)) {
                            // 如果是字节数组，则尝试从单元格中读取图片
                            ExcelImage excelImage = (ExcelImage) value;
                            insertImage(sheet, excelImage.getBytes(), row.getRowNum(), excelColumn.index(), excelImage.getName());
                        } else {
                            XSSFCell cell = row.createCell(excelColumn.index());
                            cell.setCellValue((String) value);
                            setDataCellStyle(workbook, excelColumn, cell);
                        }
                    }
                }
            }
            log.info("表格赋值成功！");
        } catch (Exception e) {
            log.error("表格赋值失败！", e);
        }
    }

    private static void insertImage(XSSFSheet sheet, byte[] imageBytes, int row, int col, String imageName) throws IOException {
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


    private static void setDataCellStyle(XSSFWorkbook workbook, XSSFCell cell, String format) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFDataFormat fmt = workbook.createDataFormat();
        style.setDataFormat(fmt.getFormat(format));
        cell.setCellStyle(style);
    }

    private static void setDataCellStyle(XSSFWorkbook workbook, ExcelColumn excelColumn, XSSFCell cell) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFDataFormat fmt = workbook.createDataFormat();
        if (StringUtils.isNoneBlank(excelColumn.format())) {
            style.setDataFormat(fmt.getFormat(excelColumn.format()));
        }
        cell.setCellStyle(style);
    }

    private static void setBrowser(HttpServletResponse response, XSSFWorkbook workbook, String fileName) {
        try (OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
            //设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType("application/vnd.ms-excel;charset=gb2312");
            //将excel写入到输出流中
            workbook.write(os);
            os.flush();
            log.info("设置浏览器下载成功！");
        } catch (Exception e) {
            log.error("设置浏览器下载失败！", e);
        }
    }
}