package com.madao.disk.util;

import java.util.Random;

public class KeyUtil {

    public static final char[] letters = new char[52];
    public static final byte[] nums = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    static{
        letters[0] = 'a';
        letters[26] = 'A';
        for(int i=1; i<26; i++){
            letters[i] = ++letters[i-1];
        }
        for(int i=27; i<letters.length; i++){
            letters[i] = ++letters[i-1];
        }

        for(char c: letters){
            System.out.println(c);
        }

    }
    /**
     * 生成唯一的主键
     * 格式：时间+随机数
     * @return
     */
    public static synchronized String genUniquKey(){
        Random random = new Random();
        Integer number = random.nextInt(900000) + 100000;
        return System.currentTimeMillis() + String.valueOf(number);

    }

    public static synchronized Long genUniquKeyOnLong(){
        Random random = new Random();
        Integer number = random.nextInt(900000) + 100000;
        String key = System.currentTimeMillis() + String.valueOf(number);
        return Long.parseLong(key);

    }

    public static synchronized String genStringCode(int n){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<n; i++){
            int index = random.nextInt(51);
            sb.append(letters[index]);
        }
        return sb.toString();
    }

    public static String genNumCode(int n) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<n; i++){
            sb.append(nums[random.nextInt(9)]);
        }
        return sb.toString();
    }
}
