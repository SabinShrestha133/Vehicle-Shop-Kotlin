package com.example.bikeshop.view


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.bikeshop.model.OrderModel

import com.example.bikeshop.repository.OrderRepositoryImpl
import com.example.bikeshop.view.ui.theme.ui.theme.BikeShopTheme
import com.example.bikeshop.viewmodel.OrderViewModel
import com.example.bikeshop.viewmodel.OrderViewModelFactory

class UserOrderActivity : ComponentActivity() {

    private lateinit var orderViewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get userId passed via Intent
        val userId = intent.getStringExtra("userId") ?: ""

        val orderRepo = OrderRepositoryImpl()
        val orderFactory = OrderViewModelFactory(orderRepo)
        orderViewModel = ViewModelProvider(this, orderFactory)[OrderViewModel::class.java]
        val userOrders: LiveData<List<OrderModel>>


        setContent {
            BikeShopTheme {
                UserOrderScreen(userId, orderViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOrderScreen(userId: String, orderViewModel: OrderViewModel) {
//    val orders by orderViewModel.userOrders.observeAsState(emptyList())

    val orders = orderViewModel.userOrders.observeAsState(initial = emptyList()).value ?: emptyList()

    val context = LocalContext.current

    LaunchedEffect(userId) {
        orderViewModel.loadOrdersByUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bike Orders", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No bike orders found.")
                }
            } else {




                LazyColumn {
                    items(orders.size) { order ->
                        OrderCardWithCancel(
                            order = orders[order],
                            onCancel = {
                                orderViewModel.cancelOrder(
                                    orders[order].orderId,
                                    userId
                                )
                                Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                                orderViewModel.loadOrdersByUser(userId)
                            }
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun OrderCardWithCancel(order: Int, onCancel: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.titleMedium)
//            Text("Status: ${order.orderStatus}")
//            Text("Total: Rs. ${order.totalAmount}")
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            if (order.orderStatus != "Cancelled") {
//                Button(
//                    onClick = onCancel,
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                ) {
//                    Text("Cancel Order")
//                }
//            }
//        }
//    }
//}

//@Composable
//fun OrderCardWithCancel(order: OrderModel, onCancel: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.titleMedium)
//            Text("Status: ${order.orderStatus}")
//            Text("Total: Rs. ${order.totalAmount}")
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            if (order.orderStatus != "Cancelled") {
//                Button(
//                    onClick = onCancel,
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                ) {
//                    Text("Cancel Order")
//                }
//            }
//        }
//    }
//}

@Composable
fun OrderCardWithCancel(order: OrderModel, onCancel: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.titleMedium)
            Text("Status: ${order.orderStatus}")
            Text("Total: Rs. ${order.totalAmount}")

            Spacer(modifier = Modifier.height(8.dp))

            if (order.orderStatus != "Cancelled") {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel Order")
                }
            }
        }
    }
}