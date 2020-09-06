


import com.google.gson.Gson;
        import com.google.gson.GsonBuilder;

        import java.util.HashMap;
class Hero {
    public String name;
    public String skill1;
    public String skill2;
    public String skill3;
    public String skill4;
    //如果是private还要创建set() get()
}
public class TestGson {//测试代码

    public static void main(String[] args) {//json键值对结构对应Java中的hashmap
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("name", "曹操");
//        hashMap.put("skill1", "剑气");
//        hashMap.put("skill2", "三段跳");
//        hashMap.put("skill3", "加攻击并吸血");
//        hashMap.put("skill4", "加攻速");
        Hero hero = new Hero();
        hero.name = "曹操";//对象.属性
        hero.skill1 = "剑气";
        hero.skill2 = "三段跳";
        hero.skill3 = "加攻击并吸血";

        // 通过 map 转成 JSON 结构的字符串  无序
        // 1. 创建一个 gson 对象
        Gson gson = new GsonBuilder().create();
        // 2. 使用 toJson 方法把键值对结构转成 JSON 字符串        对象.方法()
        //String str = gson.toJson(hashMap);  //键值对 顺序没有要求
        String str = gson.toJson(hero);
        System.out.println(str);
    }
}
