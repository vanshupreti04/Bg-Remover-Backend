package com.vansh.removeBackground.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;

public interface OrderService {

    Order createOrder(String planId, String clerkId) throws RazorpayException;
}
