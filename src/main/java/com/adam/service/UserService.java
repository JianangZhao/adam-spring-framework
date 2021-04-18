package com.adam.service;


import com.spring.Autowired;
import com.spring.Component;
import com.spring.Scope;

@Component(Value = "userService")
@Scope(Value = "prototype")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
    }

}
