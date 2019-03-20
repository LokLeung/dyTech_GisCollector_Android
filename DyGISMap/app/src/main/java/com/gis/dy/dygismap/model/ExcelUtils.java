package com.gis.dy.dygismap.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gis.dy.dygismap.repository.MyDataBase;
import com.gis.dy.dygismap.repository.room.MyRoomDataBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";


    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);

            arial12font = new WritableFont(WritableFont.ARIAL, 10);
            arial12format = new WritableCellFormat(arial12font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);//对齐格式
            arial12format.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN); //设置边框

        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Excel
     * @param fileName
     * @param colName
     */
    public void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            //if (!file.exists()) file.mkdirs();
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("sheet1", 0);
            //创建标题栏
            sheet.addCell((WritableCell) new Label(0, 0, fileName,arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            sheet.setRowView(0,340); //设置行高

            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void writeObjListToExcel(ArrayList<ArrayList<String>> objList, String fileName, Context c) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName),workbook);
                WritableSheet sheet = writebook.getSheet(0);

//              sheet.mergeCells(0,1,0,objList.size()); //合并单元格
//              sheet.mergeCells()

                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = objList.get(j);
                    Log.v("test",list.size()+"size");
                    for (int i = 0; i < list.size(); i++) {
                        String content = list.get(i);
                        if(content == null)
                            content = "";
                        sheet.addCell(new Label(i, j + 1, content,arial12format));
                        if (content.length() <= 5){
                            sheet.setColumnView(i,content.length()+8); //设置列宽
                        }else {
                            sheet.setColumnView(i,content.length()+5); //设置列宽
                        }
                    }
                    sheet.setRowView(j+1,350); //设置行高
                }

                writebook.write();
                Log.e("export","export excel sucess!"+fileName);
                Toast.makeText(c, "导出到手机存储中成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(c, "导出到手机存储中失败"+e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private List list = new ArrayList();

    public MyGISPoint[] getXlsData(String filePath) throws Exception {
        // 创建输入流
        InputStream stream = new FileInputStream(filePath);
        // 获取Excel文件对象
        Workbook rwb = Workbook.getWorkbook(stream);
        // 获取文件的指定工作表 默认的第一个
        Sheet sheet = rwb.getSheet(0);
        // 行数(表头的目录不需要，从1开始)
        for (int i = 1; i < sheet.getRows(); i++) {
            MyGISPoint gpoint = new MyGISPoint();//根据具体的生成对应的对象文件
            gpoint.pointType = sheet.getCell(0, i).getContents();
            gpoint.point_id = sheet.getCell(1, i).getContents();
            gpoint.LNG = sheet.getCell(2, i).getContents();
            gpoint.LAT = sheet.getCell(3, i).getContents();
            gpoint.jingwushi = sheet.getCell(4, i).getContents();
            gpoint.cunjuwei = sheet.getCell(5, i).getContents();
            gpoint.jieluxiang = sheet.getCell(6, i).getContents();
            gpoint.menpaihao = sheet.getCell(7, i).getContents();
            gpoint.xiaoquName = sheet.getCell(8, i).getContents();
            gpoint.gongcangName = sheet.getCell(9, i).getContents();
            gpoint.xuexiaoName = sheet.getCell(10, i).getContents();
            gpoint.yiyuanName = sheet.getCell(11, i).getContents();
            gpoint.shangpuName = sheet.getCell(12, i).getContents();
            gpoint.jianzhuwumingchen = sheet.getCell(13, i).getContents();
            gpoint.loupaihao = sheet.getCell(14, i).getContents();
            gpoint.danweiming = sheet.getCell(15, i).getContents();
            gpoint.loucengshu = sheet.getCell(16, i).getContents();
            gpoint.xiaoqudanyuan = sheet.getCell(17, i).getContents();
            gpoint.roomName = sheet.getCell(18, i).getContents();
            gpoint.plateType = sheet.getCell(19, i).getContents();
            gpoint.comments = sheet.getCell(20, i).getContents();
            gpoint.time = sheet.getCell(21, i).getContents();
            gpoint.picName = sheet.getCell(22, i).getContents();
            gpoint.workerID = sheet.getCell(23, i).getContents();
            // 把刚获取的列存入list，也可以存入到数据库
            list.add(gpoint);
        }
        return (MyGISPoint[]) list.toArray(new MyGISPoint[0]);
    }
}

