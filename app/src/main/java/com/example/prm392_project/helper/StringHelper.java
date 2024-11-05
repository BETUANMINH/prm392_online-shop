package com.example.prm392_project.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringHelper {
    public static List<String> parseCsv(String csvStr) {
        String[] arr = csvStr.split(",");
        return Arrays.asList(arr);
    }

    /**
     * Due to someone stupid design, selected size is in the format "Selected size: size".
     * Therefore I have to write this method to extract size number
     *
     * @param selectedSize
     * @return
     */
    public static int getSelectedSize(String selectedSize) {
        int colonIndex = selectedSize.indexOf(":");
        String numberPart = selectedSize.substring(colonIndex + 1).trim();
        int sizeNumber = Integer.parseInt(numberPart);
        return sizeNumber;
    }
}
