package com.adam;

import com.adam.service.UserService;
import com.spring.AdamApplicationContext;

public class Test {
    public static void main(String[] args) {
        AdamApplicationContext adamApplicationContext = new AdamApplicationContext(AppConfig.class);

        //map <beanName, beanObj>
//        System.out.println(adamApplicationContext.getBean("userService"));
//        System.out.println(adamApplicationContext.getBean("userService"));
//        System.out.println(adamApplicationContext.getBean("userService"));

        UserService userService = (UserService) adamApplicationContext.getBean("userService");
        userService.test();
    }
}
