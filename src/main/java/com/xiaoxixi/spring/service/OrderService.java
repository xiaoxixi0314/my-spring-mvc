package com.xiaoxixi.spring.service;

import com.xiaoxixi.spring.vo.OrderVO;

public interface OrderService {

    OrderVO queryOrder(String orderNo);
}
