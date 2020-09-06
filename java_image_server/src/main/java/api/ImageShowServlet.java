package api;

import dao.Image;
import dao.ImageDao;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;


public class ImageShowServlet extends HttpServlet  {
    static private HashSet<String>  whiteList=new HashSet<>();
    static {
        whiteList.add("http://106.54.87.76:8080/java_image_server/index.html");
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException{
        String referer=req.getHeader("referer");
        if(!whiteList.contains(referer)){
            resp.setContentType("application/json; charset:utf-8");
            resp.getWriter().write("{\"ok\":false,\"reason\":\"未授权的访问\"}");
            return;
        }
        //解析图片id
        String imageId = req.getParameter("imageId");
        //错误处理
        if(imageId==null||imageId.equals("")) {
            resp.setContentType("application/json;charset=utf-8");
            resp.setStatus(404);
            resp.getWriter().write("{\"ok\":false,\"reason\":\"imageId解析失败\"}");
            return;
        }
        //创建imageDao对象，   根据imageId查找数据库（得到path,磁盘文件）
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));
        if (image == null) {
            resp.setContentType("application/json; charset=utf8");
            resp.setStatus(404);
            resp.getWriter().write("{ \"ok\": false, \"reason\": \"imageId 不存在\" }");
            return;
        }
        resp.setContentType(image.getContentType());
        //根据路径打开文件，读取其中的内容并存储到resp对象中
        File file=new File(image.getPath());
        //由于图片是二进制文件，所以采用字节流的方式读取文件
        OutputStream outputStream=resp.getOutputStream();
        FileInputStream fileInputStream=new FileInputStream(file);
        byte[] buffer=new byte[1024];
        while (true){
            int len=fileInputStream.read(buffer);//把数据读到buffer里去
            if(len==-1){
                break;//文件读取结束

            }
            //此时已经读到了一部分内容，存储在buffer里，再把buffer中的内容写到resp对象中
            outputStream.write(buffer);
        }
        //使用流，及时关闭流
        fileInputStream.close();
        outputStream.close();

    }
}

