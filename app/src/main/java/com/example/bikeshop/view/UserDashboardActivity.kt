package com.example.bikeshop.view


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.bikeshop.model.CartItemModel
import com.example.bikeshop.model.ProductModel
import com.example.bikeshop.model.UserModel
import com.example.bikeshop.model.WishlistItemModel
import com.example.bikeshop.repository.CartRepositoryImpl
import com.example.bikeshop.repository.OrderRepositoryImpl
import com.example.bikeshop.repository.ProductRepositoryImpl
import com.example.bikeshop.repository.UserRepositoryImplementation
import com.example.bikeshop.repository.WishlistRepositoryImpl
import com.example.bikeshop.viewmodel.CartViewModel
import com.example.bikeshop.viewmodel.CartViewModelFactory
import com.example.bikeshop.viewmodel.OrderViewModel
import com.example.bikeshop.viewmodel.OrderViewModelFactory
import com.example.bikeshop.viewmodel.ProductViewModel
import com.example.bikeshop.viewmodel.UserViewModel
import com.example.bikeshop.viewmodel.UserViewModelFactory
import com.example.bikeshop.viewmodel.WishlistViewModel
import com.example.bikeshop.viewmodel.WishlistViewModelFactory
import com.google.firebase.database.FirebaseDatabase


class UserDashboardActivity : ComponentActivity() {

    private lateinit var cartViewModel: CartViewModel
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var orderViewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cartViewModel = ViewModelProvider(this, CartViewModelFactory(CartRepositoryImpl()))[CartViewModel::class.java]
        wishlistViewModel = ViewModelProvider(this, WishlistViewModelFactory(WishlistRepositoryImpl))[WishlistViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory(UserRepositoryImplementation()))[UserViewModel::class.java]
        orderViewModel = ViewModelProvider(this, OrderViewModelFactory(OrderRepositoryImpl()))[OrderViewModel::class.java]

        setContent {
            UserDashboardBody(cartViewModel, wishlistViewModel, userViewModel, orderViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUserId = userViewModel.getCurrentUser()?.uid
        currentUserId?.let {
            userViewModel.getUserById(it)
            orderViewModel.loadOrdersByUser(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardBody(
    cartViewModel: CartViewModel,
    wishlistViewModel: WishlistViewModel,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel
) {
    val context = LocalContext.current
    val productRepo = remember { ProductRepositoryImpl(FirebaseDatabase.getInstance()) }
    val productViewModel = remember { ProductViewModel(productRepo) }

    val currentUserId = userViewModel.getCurrentUser()?.uid
    val user by userViewModel.users.observeAsState()
    val filteredProducts by productViewModel.filteredProducts.observeAsState(emptyList())
    val orders by orderViewModel.userOrders.observeAsState(emptyList())
    val loading by productViewModel.loading.observeAsState(true)

    var menuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            userViewModel.getUserById(it)
            orderViewModel.loadOrdersByUser(it)
        }
        productViewModel.getAllProducts()
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        productViewModel.filterByCategoryAndSearch(selectedCategory, searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bike Pasal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, EditProfileActivity::class.java))
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Edit Profile", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Address Book") },
                                onClick = {
                                    menuExpanded = false
                                    context.startActivity(Intent(context, AddressActivity::class.java))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    menuExpanded = false
                                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1976D2)) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
                    label = { Text("Home", color = Color.White) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { context.startActivity(Intent(context, CartActivity::class.java)) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White) },
                    label = { Text("Cart", color = Color.White) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { context.startActivity(Intent(context, WishlistActivity::class.java)) },
                    icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist", tint = Color.White) },
                    label = { Text("Wishlist", color = Color.White) }
                )
            }
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            UserHeader(user)

            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search bikes...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                val categories = listOf("All", "Mountain", "Road", "Electric", "Kids")

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, bottom = 8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                productViewModel.filterByCategoryAndSearch(category, searchQuery)
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1976D2),
                                selectedLabelColor = Color.White,
                                containerColor = Color.LightGray,
                                labelColor = Color.Black
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(filteredProducts) { product ->
                        product?.let {
                            ProductCard(it, cartViewModel, wishlistViewModel, context)
                        }
                    }
                }
            }

            if (orders.isNotEmpty()) {
                Text(
                    text = "Your Orders",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.4f)
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Order ID: ${order.orderId}")
                                Text("Total: Rs. ${order.totalAmount}")
                                Text("Status: ${order.orderStatus}")
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun UserHeader(user: UserModel?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Welcome, ${user?.firstName ?: "Guest"}")
    }
}

@Composable
fun ProductCard(
    product: ProductModel,
    cartViewModel: CartViewModel,
    wishlistViewModel: WishlistViewModel,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(product.productName, style = MaterialTheme.typography.titleMedium)
            Text("Rs. ${product.productPrice}", style = MaterialTheme.typography.bodyLarge)
            Text(product.productDescription, style = MaterialTheme.typography.bodySmall)

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = {
                    var model = CartItemModel(
                        productId = product.productId,
                        productName = product.productName,
                        productPrice = product.productPrice,
                        image = product.image

                    )
                    cartViewModel.addToCart(model) }) {
                    Text("Add to Cart")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    var model = WishlistItemModel(
                        productName = product.productName,
                        productPrice = product.productPrice ,
                        image = product.image
                    )
                    wishlistViewModel.addToWishlist(model) }) {
                    Text("Wishlist")
                }
            }
        }
    }
}
