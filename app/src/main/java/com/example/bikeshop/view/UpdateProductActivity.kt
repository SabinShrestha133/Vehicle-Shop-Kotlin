package com.example.bikeshop.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bikeshop.repository.ProductRepositoryImpl
import com.example.bikeshop.viewmodel.ProductViewModel
import com.google.firebase.database.FirebaseDatabase

class UpdateProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpdateBikeProductScreen()
        }
    }
}

@Composable
fun UpdateBikeProductScreen() {
    val repo = remember { ProductRepositoryImpl(FirebaseDatabase.getInstance()) }
    val viewModel = remember { ProductViewModel(repo) }
    val context = LocalContext.current
    val activity = context as? Activity

    val productId = activity?.intent?.getStringExtra("productId")
    val product by viewModel.product.observeAsState()

    var bikeName by remember { mutableStateOf("") }
    var bikePrice by remember { mutableStateOf("") }
    var bikeDescription by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        productId?.let { viewModel.getProductById(it) }
    }

    LaunchedEffect(product) {
        product?.let {
            bikeName = it.productName ?: ""
            bikePrice = it.productPrice?.toString() ?: ""
            bikeDescription = it.productDescription ?: ""
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(Color(0xFFE3F2FD)) // Light blue background
        ) {
            item {
                Text(
                    text = "Update Bike Details",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = bikeName,
                    onValueChange = { bikeName = it },
                    label = { Text("Bike Name") },
                    placeholder = { Text("e.g., Pulsar 220F") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bikePrice,
                    onValueChange = { bikePrice = it },
                    label = { Text("Price (Rs)") },
                    placeholder = { Text("e.g., 345000") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bikeDescription,
                    onValueChange = { bikeDescription = it },
                    label = { Text("Description") },
                    placeholder = { Text("Details about this bike...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val priceDouble = bikePrice.toDoubleOrNull() ?: 0.0
                        if (productId == null) {
                            Toast.makeText(context, "Invalid bike ID", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val updateData = mutableMapOf<String, Any?>(
                            "productName" to bikeName,
                            "productPrice" to priceDouble,
                            "productDescription" to bikeDescription
                        )
                        viewModel.updateProduct(productId, updateData) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) activity?.finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Update Bike")
                }
            }
        }
    }
}
