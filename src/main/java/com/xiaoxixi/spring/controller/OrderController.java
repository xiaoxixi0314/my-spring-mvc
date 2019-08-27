package com.xiaoxixi.spring.controller;

import com.xiaoxixi.spring.annotation.MyAutowired;
import com.xiaoxixi.spring.annotation.MyRequestMapping;
import com.xiaoxixi.spring.annotation.MyRequestParam;
import com.xiaoxixi.spring.annotation.MyRestController;
import com.xiaoxixi.spring.service.OrderService;
import com.xiaoxixi.spring.vo.OrderVO;

@MyRestController("orderController")
public class OrderController {

    @MyAutowired("orderService")
    private OrderService orderService;

    @MyRequestMapping("/order/detail")
    public OrderVO orderDetail(@MyRequestParam(name = "orderNo")String orderNo) {
        return orderService.queryOrder(orderNo);
    }
}
