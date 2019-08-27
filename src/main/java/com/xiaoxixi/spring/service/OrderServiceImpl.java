package com.xiaoxixi.spring.service;

import com.xiaoxixi.spring.annotation.MyService;
import com.xiaoxixi.spring.vo.OrderVO;

@MyService("orderService")
public class OrderServiceImpl implements OrderService {

    public OrderVO queryOrder(String orderNo) {
        OrderVO order = new OrderVO();
        order.setOrderNo(orderNo);
        order.setSubject("这是测试订单");
        order.setTotal(100L);
        return order;
    }
}
