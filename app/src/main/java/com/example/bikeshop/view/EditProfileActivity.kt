package com.example.bikeshop.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bikeshop.R
import com.example.bikeshop.repository.ProductRepositoryImpl
import com.example.bikeshop.utils.ImageUtils
import com.example.bikeshop.viewmodel.ProductViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase

class EditProfileActivity : ComponentActivity() {
    private lateinit var imageUtils: ImageUtils
    private var selectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri -> selectedImageUri = uri }

        setContent {
            EditProfileScreen(
                selectedImageUri = selectedImageUri,
                onPickImage = { imageUtils.launchImagePicker() },
                onRemoveImage = { selectedImageUri = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showImageOptionsMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val currentUser = Firebase.auth.currentUser

    val repo = remember { ProductRepositoryImpl(FirebaseDatabase.getInstance()) }
    val viewModel = remember { ProductViewModel(repo) }

    LaunchedEffect(Unit) {
        currentUser?.let {
            name = it.displayName ?: ""
            email = it.email ?: ""
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showImageOptionsMenu = true }
                    .background(Color.LightGray, shape = CircleShape)
                    .align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val photoUrl = currentUser?.photoUrl
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.profilepicplaceholder),
                            contentDescription = "Default Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                DropdownMenu(
                    expanded = showImageOptionsMenu,
                    onDismissRequest = { showImageOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Choose from Gallery") },
                        onClick = {
                            showImageOptionsMenu = false
                            onPickImage()
                        }
                    )
                    if (selectedImageUri != null) {
                        DropdownMenuItem(
                            text = { Text("Remove Photo") },
                            onClick = {
                                showImageOptionsMenu = false
                                onRemoveImage()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.align(androidx.compose.ui.Alignment.End)
            ) {
                Text("Change Password")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentUser != null) {
                        val updateProfile: (String?) -> Unit = { photoUrl ->
                            val request = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .apply {
                                    if (photoUrl != null) setPhotoUri(Uri.parse(photoUrl))
                                }
                                .build()

                            currentUser.updateProfile(request)
                                .addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        currentUser.reload().addOnCompleteListener {
                                            currentUser.updateEmail(email)
                                                .addOnCompleteListener { emailTask ->
                                                    if (emailTask.isSuccessful) {
                                                        Toast.makeText(
                                                            context,
                                                            "Profile updated successfully",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        activity?.finish()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to update email",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to update profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                        if (selectedImageUri != null) {
                            viewModel.uploadImage(context, selectedImageUri) { imageUrl ->
                                if (imageUrl != null) {
                                    updateProfile(imageUrl)
                                } else {
                                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            updateProfile(null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }

        // Password Dialog
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Change Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it; passwordError = null },
                            placeholder = { Text("New Password") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = passwordError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; passwordError = null },
                            placeholder = { Text("Confirm Password") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = passwordError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (passwordError != null) {
                            Text(passwordError ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPassword.isBlank() || confirmPassword.isBlank()) {
                            passwordError = "Password fields cannot be empty"
                            return@TextButton
                        }
                        if (newPassword != confirmPassword) {
                            passwordError = "Passwords do not match"
                            return@TextButton
                        }
                        val user = Firebase.auth.currentUser
                        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                showPasswordDialog = false
                                newPassword = ""
                                confirmPassword = ""
                            } else {
                                Toast.makeText(context, "Failed to change password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Text("Change")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPasswordDialog = false
                        newPassword = ""
                        confirmPassword = ""
                        passwordError = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
