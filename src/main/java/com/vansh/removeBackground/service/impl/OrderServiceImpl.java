package com.vansh.removeBackground.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.vansh.removeBackground.entity.OrderEntity;
import com.vansh.removeBackground.repository.OrderRepository;
import com.vansh.removeBackground.service.OrderService;
import com.vansh.removeBackground.service.RazorpayService;
import com.vansh.removeBackground.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final RazorpayService razorpayService;
    private final OrderRepository orderRepository;

    private static final Map<String, PlanDetails> PLAN_DETAILS = Map.of(
            "basic", new PlanDetails("Basic", 10, 99),
            "premium", new PlanDetails("Premium", 40, 299),
            "ultimate", new PlanDetails("Ultimate", 100, 699)

    );

    private record PlanDetails(String name, int credits, double amount){

    }

    @Override
    public Order createOrder(String planId, String clerkId) throws RazorpayException {
        PlanDetails details = PLAN_DETAILS.get(planId);
        if(details == null){
            throw new IllegalArgumentException("Invalid planId" + planId);
        }

        try{
            Order razorpayOrder = razorpayService.createOrder(details.amount(), "INR");
            OrderEntity newOrder = OrderEntity.builder().clerkId(clerkId).plan(details.name()).credits(details.credits()).amount(details.amount()).orderId(razorpayOrder.get("id")).build();
            orderRepository.save(newOrder);
            return razorpayOrder;
        }
        catch (RazorpayException e) {
            throw new RazorpayException("Error while creating order",e);
        }
    }
}
