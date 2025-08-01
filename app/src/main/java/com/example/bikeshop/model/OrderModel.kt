package com.example.bikeshop.model

data class OrderModel(
    var orderId: String = "",      // Changed from val to var
    var userId: String = "",
    var items: List<CartItemModel> = emptyList(),
    var totalAmount: Double = 0.0,
    var orderStatus: String = "Pending",
    var orderDate: Long = System.currentTimeMillis()
)
