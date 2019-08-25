package com.xiaoxixi.spring.controller;

import com.xiaoxixi.spring.annotation.Autowired;
import com.xiaoxixi.spring.annotation.RequestMapping;
import com.xiaoxixi.spring.annotation.RestController;
import com.xiaoxixi.spring.service.OrderService;
import com.xiaoxixi.spring.vo.OrderVO;

@RestController("orderController")
public class OrderController {

    @Autowired("orderService")
    private OrderService orderService;

    @RequestMapping("/order/detail")
    public OrderVO orderDetail(String orderNo) {
        return orderService.queryOrder(orderNo);
    }
}
