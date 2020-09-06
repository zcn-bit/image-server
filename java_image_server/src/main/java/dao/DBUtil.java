package dao;



import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class DBUtil { //封装获取数据库连接的过程

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/java_image_server?characterEncoding=utf8&useSSL=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";

    private static volatile DataSource dataSource = null;//多个线程调用一个dataSource对象
    //volatile解决代码过度优化的情况
    public static DataSource getDataSource() { //单例，只创造一份实例   懒加载技术
        // 通过这个方法来创建 DataSource 的实例
        if (dataSource == null) {//大多数不为null 不需要枷锁，事实上只在第一次调用时候加锁，大大降低了成本
            synchronized (DBUtil.class) {//给整个类加锁。  枷锁是个低效耗时的操作，每次调用get()方法都枷锁成本很大，所以先判断
                if (dataSource == null) {//加锁-》加锁成功是漫长过程 需二次判断
                    dataSource = new MysqlDataSource();//不加锁时候可能创造2个实例
                    MysqlDataSource tmpDatasource=(MysqlDataSource) dataSource;//主要为了用该类下的方法 向下强转
                    tmpDatasource.setURL(URL);
                    tmpDatasource.setUser(USERNAME);
                    tmpDatasource.setPassword(PASSWORD);
                }
            }
        }
        return dataSource;
    }



    public static Connection getConnection() {//获取connection对象
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;//如果抛出异常就返回它
        }

    }



    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {//顺着开，倒序关闭
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

