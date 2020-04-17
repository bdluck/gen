package top.jlpan;


import top.jlpan.config.GenConfig;
import top.jlpan.data.TableServiceImpl;
import top.jlpan.gen.ClassPathTemplateGen;
import top.jlpan.model.Table;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author panliang
 * @version 1.0
 * @ProjectName gen
 * @Description 代码生成管理配置
 * 使用时只需要自定义作者（author）和项目地址（root）即可
 * 选择需要的表执行即可
 * @Date 2020/4/15 11:19
 */
@SuppressWarnings("all")
public class GenMain {

    private static String origin = "/*gen_origin_sigin*/";

    public static void main(String[] args) throws IOException {

        /* 通用参数配置 */
        GenConfig.url = "jdbc:mysql://139.199.179.144:3306/ruoyi?usdeUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
        GenConfig.username = "ruoyi";
        GenConfig.password = "ruoyi";
        GenConfig.prefix = "";


        /* FIXME 本地项目地址  */
        String root = "E:\\project\\gen";
        /* FIXME 表名 */
        String tableName = "user";
        /* FIXME 表别名 t_user -> User  */
        String modelName = "user";

        /* 自定义数据声明 */
        HashMap<String, Object> fillMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fillMap.put("createDate", format.format(new Date()));
        /* FIXME 作者  */
        fillMap.put("author", "panliang");
        fillMap.put("version", "1.0");
        fillMap.put("namespace", "top.jlpan.project.upms");
        fillMap.put("origin", origin);

        /* 表数据声明 */
        Table table = new TableServiceImpl().selectTableByName(tableName);

        /* 声明模板路径 */
        String entityTemplate = "vm/entity.java.vm";
        String mapperTemplate = "vm/mapper.java.vm";
        String xmlTemplate = "vm/mapper.xml.vm";


        /* 声明生成路径 */
        String entityPath = root + "\\命名空间\\" + modelName + ".java";
        String mapperPath = root + "\\命名空间\\" + modelName + "Mapper.java";
        String xmlPath = root + "\\命名空间\\" + modelName + "Mapper.xml";

        /* 加载模板 */
        ClassPathTemplateGen entityGen = new ClassPathTemplateGen(entityTemplate);
        ClassPathTemplateGen mapperGen = new ClassPathTemplateGen(mapperTemplate);
        ClassPathTemplateGen xmlGen = new ClassPathTemplateGen(xmlTemplate);

        /* 注入参数 */
        entityGen.initData(fillMap, table);
        mapperGen.initData(fillMap, table);
        xmlGen.initData(fillMap, table);


        /* 写文件 */
        writeFile(entityPath, entityGen.gen());
        writeFile(mapperPath, mapperGen.gen());
        writeFile(xmlPath, xmlGen.gen());

    }


    /**
     * 写文件
     *
     * @param path
     * @param sw
     */
    static void writeFile(String path, StringWriter sw) throws IOException {

        String newString = sw.toString();
        System.out.println("路径:" + path + " 生成中....");
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("文件路径:" + path + " 不存在,正在新建...");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } else {
            while (true) {
                System.out.println("文件路径:" + path + "已存在,选择覆盖,取消,根据" + origin + "合并或重命名？");
                System.out.println("1:覆盖 2:取消 3:合并 4:重命名");
                Scanner scanner = new Scanner(System.in);
                String an = scanner.nextLine();
                if ("1".equals(an)) {
                    System.out.println("覆盖中...");
                    file.delete();
                    System.out.println("覆盖完毕！");
                    break;
                }
                if ("2".equals(an)) {
                    System.out.println("跳过执行！");
                    return;
                }
                if ("3".equals(an)) {
                    // 读取旧文件
                    FileInputStream stream = new FileInputStream(file);
                    int size = stream.available();
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    stream.close();
                    String oldString = new String(buffer);
                    int oldIndex = oldString.lastIndexOf(origin);
                    int newIndex = newString.lastIndexOf(origin);
                    if (oldIndex == -1 || newIndex == -1) {
                        System.out.println("不存在" + origin + ",跳过执行！");
                        return;
                    }

                    String oldNeed = oldString.substring(oldIndex + origin.length());
                    String newNeed = newString.substring(0, newIndex + origin.length() + 1);
                    newString = newNeed + oldNeed;
                    System.out.println("已合并！");
                    break;
                }
                if ("4".equals(an)) {
                    int index = path.lastIndexOf(".");
                    String fileName = path.substring(index);
                    String head = path.substring(0, index);
                    String copy = head + "Copy" + fileName;
                    file = new File(copy);
                    System.out.println("已重命名！");
                    break;
                }
                System.out.println("输入错误，请重新输入！");
            }
        }

        OutputStream out = new FileOutputStream(file);
        out.write(newString.getBytes());
        out.flush();
        out.close();

    }

}

