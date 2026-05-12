package com.fliks.view

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliks.R
import com.fliks.ui.theme.FliksTheme
import com.fliks.ui.theme.azulBorde
import com.fliks.ui.theme.azulBrillante
import com.fliks.ui.theme.azulFondo
import com.fliks.viewmodel.AuthViewModel

class RegistroActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FliksTheme {
                PantallaRegistro()
            }
        }
    }
    @Composable
    fun PantallaRegistro() {
        var correo by remember { mutableStateOf("") }
        var contrasena by remember { mutableStateOf("") }
        var contrasenaVisible by remember { mutableStateOf(false) }
        //si se crea la cuenta se manda a mainactivity
        LaunchedEffect(viewModel.exitoLogin) {
            if (viewModel.exitoLogin) {
                val intent = Intent(this@RegistroActivity, MainActivity::class.java).apply {
                    putExtra("USUARIO_EMAIL", viewModel.emailUsuario)
                }
                startActivity(intent)
                finishAffinity()
            }
        }
        LaunchedEffect(viewModel.mensajeError) {
            viewModel.mensajeError?.let { error ->
                Toast.makeText(this@RegistroActivity, error, Toast.LENGTH_LONG).show()
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
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = (-150).dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(azulBrillante.copy(alpha = 0.2f), Color.Transparent)
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
                        contentDescription = stringResource(R.string.logo_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.crear_cuenta),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(40.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text(stringResource(R.string.correo_electronico), color = Color.Gray) },
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
                    placeholder = { Text(stringResource(R.string.contrasena_minimo), color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.lock),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingIcon = {
                        val imagenOjo = if (contrasenaVisible)
                            painterResource(id = R.drawable.visible)
                        else
                            painterResource(id = R.drawable.novisible)
                        IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                            Icon(
                                painter = imagenOjo,
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
                Spacer(modifier = Modifier.height(48.dp))
                if (viewModel.cargando) {
                    CircularProgressIndicator(color = azulBrillante)
                } else {
                    Button(
                        onClick = { viewModel.registrarCuenta(correo, contrasena) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = azulBrillante)
                    ) {
                        Text(stringResource(R.string.registrarme), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    TextButton(onClick = { finish() }) {
                        Row {
                            Text(stringResource(R.string.ya_tienes_cuenta), color = Color.White.copy(alpha = 0.6f))
                            Text(stringResource(R.string.inicia_sesion), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}