package com.ayakashikitsune.fishifymanager.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ayakashikitsune.fishifymanager.R as localresource
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme

@Composable
fun AboutScreen() {
    val localContext = LocalContext.current
    val listcreator = listOf(
        "Estrada II, Jonas M.",
        "Magpulong, John Lloyd",
        "Marcial, Alvaro Y.",
        "Rolan, Lance Frazer O.",
        "Ruiz, Kurt Cobain V.",
        "Naval, Joana Jane Q."
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ) {
            Spacer(modifier = Modifier.padding(24.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                AsyncImage(
                    model = localresource.drawable.fishifylogo,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = stringResource(id = localresource.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Text(
                            text = "Version 1.0",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                listcreator.map {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun prevAboutscreen() {
    FishifyManagerTheme() {
        AboutScreen()
    }
}