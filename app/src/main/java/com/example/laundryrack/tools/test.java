package com.example.laundryrack.tools;

public class test {

    public static void main(String[] args)
    {
        String nowWeight="-13g";
        System.out.println(nowWeight);
        System.out.println(nowWeight.substring(nowWeight.indexOf("r")+1,nowWeight.indexOf("g")));
        //System.out.println(nowWeight.substring(nowWeight.indexOf("r")+1,nowWeight.indexOf("i")));
    }
}
