package DataScraping;

import DataScraping.RestaurantEntity.Category;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        Category c1 = new Category("chinese", "Chinese");
        Category c2 = new Category("seafood", "Sea Food");
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(c1);
        categoryList.add(c2);

        String categoryStr = categoryList.toString();

        List<String> list = List.of(categoryStr.substring(1, categoryStr.length() - 1));
        System.out.println(categoryList);
        System.out.println(list);
    }
}
