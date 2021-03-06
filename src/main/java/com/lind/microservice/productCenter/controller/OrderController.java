package com.lind.microservice.productCenter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lind.microservice.productCenter.dto.OrderList;
import com.lind.microservice.productCenter.enums.OrderStatus;
import com.lind.microservice.productCenter.model.OrderInfo;
import com.lind.microservice.productCenter.model.OrderItem;
import com.lind.microservice.productCenter.model.ProductDetail;
import com.lind.microservice.productCenter.model.UserInfo;
import com.lind.microservice.productCenter.mq.OrderPublisher;
import com.lind.microservice.productCenter.repository.OrderInfoRepository;
import com.lind.microservice.productCenter.repository.OrderItemRepository;
import com.lind.microservice.productCenter.repository.ProductDetailRepository;
import com.lind.microservice.productCenter.repository.UserInfoRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api("订单控制器")
@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

  @Autowired
  ProductDetailRepository productDetailRepository;
  @Autowired
  UserInfoRepository userInfoRepository;
  @Autowired
  OrderInfoRepository orderInfoRepository;
  @Autowired
  OrderItemRepository orderItemRepository;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  OrderPublisher orderPublisher;

  /**
   * 模拟一个用户，下一个订单
   */
  @GetMapping("/init")
  @ApiOperation(value = "初始化")
  public void init() throws Exception {
    UserInfo userInfo = UserInfo.builder()
        .email("bfyxzls@sina.com")
        .gander(UserInfo.Gander.male)
        .userName("lind")
        .phone("1352197xxxx")
        .build();
    userInfoRepository.save(userInfo);

    ProductDetail productDetail = ProductDetail.builder()
        .inventory(11)
        .productName("足球")
        .longDescription("体育用品")
        .salePrice(99)
        .discount(100)
        .shortDescription("体育用品")
        .build();
    productDetailRepository.save(productDetail);

    ProductDetail productDetail2 = ProductDetail.builder()
        .inventory(11)
        .productName("篮球")
        .longDescription("体育用品")
        .salePrice(129)
        .discount(100)
        .shortDescription("体育用品")
        .build();
    productDetailRepository.save(productDetail);

    OrderInfo orderInfo = OrderInfo.builder()
        .orderTime(new Date())
        .shippingName("zzl")
        .total(79)
        .userId(userInfo.getId())
        .userName(userInfo.getUserName())
        .orderStatus(OrderStatus.RECEIVED)
        .build();
    orderInfoRepository.save(orderInfo);

    List<OrderItem> orderItems = new ArrayList<>();
    orderItems.add(OrderItem.builder()
        .orderId(orderInfo.getId())
        .count(1)
        .salePrice(productDetail.getSalePrice())
        .productId(productDetail.getId())
        .productName(productDetail.getProductName())
        .build());
    orderItems.add(OrderItem.builder()
        .orderId(orderInfo.getId())
        .count(1)
        .salePrice(productDetail2.getSalePrice())
        .productId(productDetail2.getId())
        .productName(productDetail2.getProductName())
        .build());
    orderItemRepository.saveAll(orderItems);
    log.debug("订单初始化{}", orderInfo);
    orderPublisher.generateOrder(orderInfo);
  }

  @ApiOperation(value = "订单列表")
  @GetMapping("{id}")
  public List<OrderList> getOrderList(@PathVariable @ApiParam("编号") int id) {
    return orderInfoRepository.getOrderInfoByUser(id);
  }

  @ApiOperation(value = "订单详细")
  @GetMapping("{id}/items")
  public List<OrderItem> getOrderItem(@PathVariable @ApiParam("编号") int id) {
    return orderItemRepository.findByOrderId(id);
  }

  @ApiOperation(value = "订单汇总列表")
  @GetMapping()
  public List<OrderList> getOrderListAll() {
    return orderInfoRepository.getOrderInfos();
  }

  @ApiOperation(value = "删除订单")
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity del(@PathVariable @ApiParam("编号") int id) {
    log.info("删除订单 {}", id);
    orderInfoRepository.delOrder(id);
    orderItemRepository.delOrderItems(id);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

}
