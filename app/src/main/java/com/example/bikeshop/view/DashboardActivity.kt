package com.example.bikeshop.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bikeshop.repository.CartRepositoryImpl
import com.example.bikeshop.repository.ProductRepositoryImpl
import com.example.bikeshop.viewmodel.CartViewModel
import com.example.bikeshop.viewmodel.CartViewModelFactory
import com.example.bikeshop.viewmodel.ProductViewModel
import com.google.firebase.database.FirebaseDatabase

class DashboardActivity : ComponentActivity() {
    private lateinit var cartViewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cartRepo = CartRepositoryImpl()
        val cartFactory = CartViewModelFactory(cartRepo)
        cartViewModel = androidx.lifecycle.ViewModelProvider(this, cartFactory)[CartViewModel::class.java]

        setContent {
            DashboardBody(cartViewModel = cartViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(cartViewModel: CartViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val repo = remember { ProductRepositoryImpl(FirebaseDatabase.getInstance()) }
    val viewModel = remember { ProductViewModel(repo) }

    val products = viewModel.allProducts.observeAsState(initial = emptyList())
    val loading = viewModel.loading.observeAsState(initial = true)

    LaunchedEffect(Unit) {
        viewModel.getAllProducts()
    }

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bike Shop Dashboard") },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        // Redirect to login screen or finish the activity
                        activity?.finish()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2), // Green color
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context, AddProductActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Bike")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1976D2)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        context.startActivity(Intent(context, OrderActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(color = Color(0xFFF1F8E9)) // light green background
            ) {
                if (loading.value) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(products.value.size) { index ->
                        val bike = products.value[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        ) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                Text(text = bike?.productName ?: "Unnamed Bike", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Price: Rs. ${bike?.productPrice ?: 0}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = bike?.productDescription ?: "", style = MaterialTheme.typography.bodySmall)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(context, UpdateProductActivity::class.java)
                                            intent.putExtra("productId", bike?.productId ?: "")
                                            context.startActivity(intent)
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = Color(0xFF1976D2)
                                        )
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Bike")
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteProduct(bike?.productId.toString()) { success, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = Color.Red
                                        )
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Bike")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
