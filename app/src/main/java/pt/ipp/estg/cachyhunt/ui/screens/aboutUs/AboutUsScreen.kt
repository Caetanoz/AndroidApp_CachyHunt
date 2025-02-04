package pt.ipp.estg.cachyhunt.ui.screens.aboutus

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import androidx.compose.ui.res.stringResource

@Composable
fun AboutUsScreen() {

    val intro2 = stringResource(R.string.about_us_intro_2)
    val intro3 = stringResource(R.string.about_us_intro_3)
    val intro4 = stringResource(R.string.about_us_intro_4)
    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Beige)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.banner_cachyhunt),
                        contentDescription = stringResource(R.string.enterprise_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    CircularLogoImage(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 60.dp)
                    )
                }

                Text(
                    stringResource(R.string.about_us_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = NatureGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val context = LocalContext.current

                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.about_us_intro_1))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.cachyhunt_name))
                            }
                            append(stringResource(R.string.about_us_intro_2))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.cachyhunt_name))
                            }
                            append(stringResource(R.string.about_us_intro_3))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.cachyhunt_name))
                            }
                            append(stringResource(R.string.about_us_intro_4))
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = stringResource(R.string.about_us_copyright),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Interaction buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+123456789")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.call_us))
                        }

                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:info@cachyhunt.com")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Email, contentDescription = stringResource(R.string.email_us))
                        }

                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("smsto:+123456789")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Message, contentDescription = stringResource(R.string.message_us))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CircularLogoImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cachyhunt),
            contentDescription = stringResource(R.string.logo_description),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutUsScreen() {
    AboutUsScreen()
}