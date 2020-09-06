package api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ImageServlet extends HttpServlet {//前后端交互的代码
//Demo代码
    // @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setStatus(200);
//        resp.getWriter().write("hello");//和body密切相关
//    }
//
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setStatus(200);
//        resp.setContentType("text/html;charset= utf-8");
//        resp.getWriter().write("郑超妮");//和body密切相关
//    }
//}


    //查看图片
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //考虑到是查找指定图片属性还是所有图片属性，根据url中是否带有imageId参数来区分，存在即查看指定图片属性
        //如果url中imageId不存在，那么返回null
        String imageId = req.getParameter("imageId");

        if(imageId==null||imageId.equals("")){
            selectAll(req, resp);
        }else {
            selectOne(imageId, resp);
        }
    }

    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        //创建imageDao对象，并查找数据库
        ImageDao imageDao=new ImageDao();
        List<Image> images=imageDao.selectAll();
        //把查找到的结果转成json格式的字符串，并写回resp对象中
        Gson gson=new GsonBuilder().create();
        // jsonData就是一个json格式的字符串，与之前前后端交互API的设计一样
        //Gson帮我们做了大量的格式转换工作，只要把之前相关的字段（Image类中的属性）约定成统一的命名，下面的操作就可以一部到位完成整个转换
        String jsonData=gson.toJson(images);
        resp.getWriter().write(jsonData);

    }

    private void selectOne(String imageId, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        //创建imageDao对象，并查找数据库
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));
        //把查找到的结果转成json格式的字符串，并写回resp对象中
        Gson gson=new GsonBuilder().create();
        // jsonData就是一个json格式的字符串，与之前前后端交互API的设计一样
        String jsonData=gson.toJson(image);
        resp.getWriter().write(jsonData);
    }


    //上传图片
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { //上传图片
        //1，获取图片的属性信息，并且从入数据库中
        //a.需要创建一个factory对象和upload对象，这是为了获取到图片属性做的准备工作   一个固定逻辑
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        //b.通过upload对象进一步解析请求（解析HTTP请求中奇怪的body中的内容（是关于图片的一些属性信息））
        //FileItem就代表一个上传的文件对象
        //理论上来说，HTTP支持一个请求中同时上传多个文件，所以用List
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(req);
        } catch (FileUploadException e) {
            //出现异常说明解析出错
            e.printStackTrace();
            //告诉客户端出现的具体的错误是啥
            resp.setContentType("application/json;charset=utf-8");
            resp.getWriter().write("{\"ok\":false,\"reason\":\"请求解析失败\"}");//json格式的响应
            return;
        }
        //c.把FileItem中的属性提取出来，转换成Image对象，才能存到数据库中
        //当前只考虑一张图片
        FileItem fileItem = items.get(0);
        Image image = new Image();
        image.setImageName(fileItem.getName());
        image.setSize((int) fileItem.getSize());
        //手动获取一下当前日期，并转成格式化日期，yymmdd=>20200218
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        image.setUploadTime(simpleDateFormat.format(new Date()));
        image.setContentType(fileItem.getContentType());
        //MD5     依赖下common=codec的类下的方法   FileItem就代表一个上传的文件对象
        image.setMd5(DigestUtils.md5Hex(fileItem.get()));//返回的是byte[]  二进制内容
        //image.setMd5("11223344");
        //自己构造一个路径来保存（在磁盘上的路径）
        image.setPath("./image/" + image.getMd5());//指当前路径

        //存到数据库中
        ImageDao imageDao = new ImageDao();
        //看看数据库是否存在相同的md5值的图片，不存在返回null,
        Image existImage = imageDao.selectByMd5(image.getMd5());
        imageDao.insert(image);//图片属性插入数据库

        //2.获取图片的内容信息，并且写入磁盘中
        if (existImage == null) {//数据库中不存在相同md5值图片，则没有存入过磁盘文件，所以可写入磁盘

            File file = new File(image.getPath());
            try {
                fileItem.write(file);//指定你要写入的路径
            } catch (Exception e) {
                e.printStackTrace();
                resp.setContentType("application/json;charset=utf-8");
                resp.getWriter().write("{ \"ok\" : false, \"reason\" : \"文件写入磁盘失败\"}");
                // 注意, 此处可能出现脏数据的情况, 比如数据库插入成功, 但是这里文件写入失败
                return;
            }

            //3.给客户端返回一个结果数据
            //resp.setContentType("application/json;charset=utf-8");
            //  resp.getWriter().write("{\"ok\":true}");
            //上传图片后跳转至展示图片页
            resp.sendRedirect("index.html");
        }
    }

    //删除指定图片
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        //先获取到图片的imageId
        String imageId=req.getParameter("imageId");
        if(imageId==null||imageId.equals("")){//没有指定具体id
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\" : false, \"reason\" : \"解析请求失败\"}");
            return;
        }
        //创建imageDao对象，根据id查看指定图片的相关属性（为了知道图片的文件路径path）

        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));
        if(image==null){//数据库中不存在此id
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\" : false, \"reason\" : \"imageId在数据库中不存在\"}");
            return;
        }
        //删除数据库中的指定图片属性
        imageDao.delete(Integer.parseInt(imageId));
        //删除本地磁盘文件中的指定图片内容
        File file=new File(image.getPath());
        file.delete();
        resp.getWriter().write("{ \"ok\" : true}");
    }

}
