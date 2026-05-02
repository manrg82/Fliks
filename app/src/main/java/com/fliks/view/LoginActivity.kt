package com.fliks.view

import com.fliks.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliks.ui.theme.FliksTheme
import com.fliks.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FliksTheme {
                PantallaLogin()
            }
        }
    }

    @Composable
    fun PantallaLogin() {
        var correo by remember { mutableStateOf("") }
        var contrasena by remember { mutableStateOf("") }
        var contrasenaVisible by remember { mutableStateOf(false) }

        // Colores del diseño Glassmorphism
        val azulFondo = Color(0xFF001220)
        val azulBrillante = Color(0xFF007BFF)
        val azulBorde = Color(0xFF326691)

        LaunchedEffect(viewModel.exitoLogin) {
            if (viewModel.exitoLogin) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    putExtra("USUARIO_EMAIL", correo)
                }
                startActivity(intent)
                finish()
            }
        }

        LaunchedEffect(viewModel.mensajeError) {
            viewModel.mensajeError?.let { error ->
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF002B4D), azulFondo)
                    )
                )
        ) {
            // Brillo detrás del logo
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(y = (-100).dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(azulBrillante.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .border(3.dp, azulBorde, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logosvg),
                        contentDescription = "Logo Fliks",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Entrar a Fliks",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("Correo electrónico", color = Color.Gray) },
                    leadingIcon = { Icon(painterResource(R.drawable.email), contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = azulBrillante,
                        unfocusedBorderColor = azulBorde,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = azulBrillante
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    placeholder = { Text("Contraseña", color = Color.Gray) },
                    leadingIcon = { Icon(painterResource(R.drawable.lock), contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        val imagen = if (contrasenaVisible)
                            painterResource(id = R.drawable.visible)
                        else
                            painterResource(id = R.drawable.novisible)

                        IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                            Icon(
                                painter = imagen,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = azulBrillante,
                        unfocusedBorderColor = azulBorde,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (viewModel.cargando) {
                    CircularProgressIndicator(color = azulBrillante)
                } else {
                    Button(
                        onClick = { viewModel.iniciarSesion(correo, contrasena) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = azulBrillante)
                    ) {
                        Text("Iniciar Sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    TextButton(onClick = {
                        startActivity(Intent(this@LoginActivity, RegistroActivity::class.java))
                    }) {
                        Row {
                            Text("¿No tienes cuenta? ", color = Color.White.copy(alpha = 0.7f))
                            Text("Regístrate aquí", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}