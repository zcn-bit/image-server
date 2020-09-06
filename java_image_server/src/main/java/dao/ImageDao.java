package dao;


import common.JavaImageServerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImageDao {

    ///上传图片
    public void insert(Image image) {
        Connection connection=DBUtil.getConnection();//获取数据库连接

        String sql="insert into image_table values(null,?,?,?,?,?,?)";//拼装SQL语句 占位符
        PreparedStatement statement=null;
        try {
            assert connection != null;
            statement=connection.prepareStatement(sql);//拼装SQL语句
            statement.setString(1,image.getImageName());//下标从1 开始   第一个参数
            statement.setInt(2,image.getSize());
            statement.setString(3,image.getUploadTime());
            statement.setString(4,image.getContentType());
            statement.setString(5,image.getPath());
            statement.setString(6,image.getMd5());
            int ret=statement.executeUpdate();//执行SQL语句
            if(ret!=1){//应该是正确插入后，影响了一行
                throw new JavaImageServerException("插入数据库出错！");
            }
        } catch (SQLException e ) { //一旦出错即到catch         未关闭连接造成资源泄露
            e.printStackTrace();
        } catch (JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            //无论出错与否都要关闭连接
            DBUtil.close(connection,statement,null);//关闭连接和statement对象  有一定概率执行不到 比如出错直接跳转到catch语句
        }

    }



    public List<Image> selectAll() {   //几百几千个数据可以，上亿数据要加筛选条件（分页功能）
        // 1. 获取数据库链接
        Connection connection = DBUtil.getConnection();
        List<Image> images=new ArrayList<>();//存放查到的image属性
        String sql = "select * from image_table";//如需按条件筛选加where
        PreparedStatement statement = null;

        ResultSet resultSet=null;

        try {
            assert connection != null;
            statement=connection.prepareStatement(sql);//拼装sql语句
            resultSet=statement.executeQuery();
            //处理结果集
            while (resultSet.next()){
                //一个图片的属性
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setMd5(resultSet.getString("md5"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                images.add(image);//从结果集中提取信息组织成image对象添加在返回链表images中

            }
            return images;//正确执行
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return null;//中间失败出现异常

    }


    public Image selectOne(int imageId) {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "select * from image_table where imageId = ?";
        try {
            // 2. 执行 SQL 语句
            assert connection != null;
            statement = connection.prepareStatement(sql);
            statement.setInt(1, imageId);//拼装SQL语句
            resultSet = statement.executeQuery();

            // 3. 遍历结果集合(这个结果中应该只有一个)     ID是唯一的
            if (resultSet.next()) {
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setMd5(resultSet.getString("md5"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                return image;
            }

        }catch (SQLException e){
            e.printStackTrace();
        }finally {//无论出错与否。都要关闭连接
            DBUtil.close(connection,statement,resultSet);
        }
        return null;//如果出错返回null
    }


    public void delete(int imageId) {//唯一主键，不会重复
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "delete from image_table where imageId = ?";
        try {
            // 2. 执行 SQL 语句
            assert connection != null;
            statement = connection.prepareStatement(sql);
            statement.setInt(1, imageId);//拼装sql语句
            int ret = statement.executeUpdate();
            if(ret!=1){
                throw new JavaImageServerException("删除数据库操作失败!");
            }

        } catch (SQLException e ) {
            e.printStackTrace();
        } catch (JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    public Image selectByMd5(String md5){
        Connection connection = DBUtil.getConnection();

        String sql = "select * from image_table where md5 = ?";//如需按条件筛选加where
        PreparedStatement statement = null;
        ResultSet resultSet=null;

        try {
            //执行SQL 语句
            assert connection != null;
            statement=connection.prepareStatement(sql);//拼装sql语句
            statement.setString(1,md5);//补充占位符
            resultSet=statement.executeQuery();
            //处理结果集
            if (resultSet.next()){
                //一个图片的属性
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setMd5(resultSet.getString("md5"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                return  image;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return null;//中间失败出现异常

    }


    public static void main(String[] args) {//用于进行简单的测试
        // 测试 ImageDao 的代码   打一个war包部署到云服务器


        //1.测试插入
//        Image image = new Image();
//        //"测试.png", 100, "2019/12/03", "AABBCCDD", "image/png", "./data/测试.png");
//        image.setImageName("1.png");
//        image.setSize(100);
//        image.setContentType("image/png");
//        image.setUploadTime("20200603");
//        image.setPath("./data/12.jpeg");
//        image.setMd5("11223344");
//        ImageDao imageDao = new ImageDao();
//        imageDao.insert(image);

        //2.测试查找所有图片信息
//        ImageDao imageDao = new ImageDao();
//        List<Image> images = imageDao.selectAll();
//        System.out.println(images);

        //3.测试查找指定单个图片信息
//        ImageDao imageDao = new ImageDao();
//        Image image = imageDao.selectOne(41);
//        System.out.println(image);
//
//         //4.测试删除指定图片
//        ImageDao imageDao = new ImageDao();
//        imageDao.delete(41);


    }


}
