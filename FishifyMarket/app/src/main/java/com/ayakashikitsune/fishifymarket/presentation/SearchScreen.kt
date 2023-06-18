package com.ayakashikitsune.fishifymarket.presentation

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.domain.NavigationItemList
import com.ayakashikitsune.fishifymarket.domain.NavigationItemList.AboutNav.addToRoute
import com.ayakashikitsune.fishifymarket.domain.ViewItemAs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import com.ayakashikitsune.fishifymarket.R as localresource

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SearchScreen(
    vm: FishifyViewModel,
    navHostController: NavHostController
) {
    val focus = LocalFocusManager.current
    val config = LocalConfiguration.current
    val listRandom = vm.shopItemInfoFlow
    var isfocused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val toppadd = animateDpAsState(
        targetValue = if (isfocused) 4.dp else config.screenHeightDp.dp / 3
    )

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.height(toppadd.value)
        ) {}
        OutlinedTextField(
            value = "",
            onValueChange = {
                it
            },
            label = { Text("Search") },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .onFocusChanged { focusState ->
                    when {
                        focusState.isFocused -> {
                            isfocused = true
                            println("focused")
                        }

                        else -> {
                            isfocused = false
                            println("unfocused")
                        }
                    }

                }
                .fillMaxWidth()
                .padding(start = 4.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focus.clearFocus()
                }
            )
        )
        AnimatedVisibility(visible = isfocused) {
            AnimatedContent(
                targetState = true,
                transitionSpec = {
                    fadeIn() with fadeOut()
                }
            ) {
                LazyColumn() {
                    listRandom.take(3).map {
                        item {
                            ShopItemUI(
                                shopItem = it,
                                viewItemAs = ViewItemAs.ListView,
                                context = context,
                                onclick = {
                                    navHostController.navigate(
                                        NavigationItemList.DetailsNav.title.addToRoute(
                                            it
                                        )
                                    )
                                })
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = !isfocused,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            val message = remember {
                mutableStateOf("")
            }
            val dimensions = LocalConfiguration.current
            val routine = rememberCoroutineScope()
            val pagerState = rememberPagerState()
            LaunchedEffect(key1 = true){
                var i = pagerState.currentPage
                routine.launch{
                    while(true){
                        i++
                        if(i >= 5){
                            i = 0
                        }
                        Log.d("movinghp", "$i")
                        pagerState.animateScrollToPage(i)
                        delay(400)
                    }
                }
            }
            val itempager = listRandom.apply {
                if(size >= 5){
                    take(5)
                }else if(size < 5){
                    take(3)
                }
            }.shuffled()

            if(itempager.isNotEmpty()){
                HorizontalPager(
                    pageCount = itempager.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSize = PageSize.Fixed(pageSize = dimensions.screenWidthDp.dp * 0.70f),
                    pageSpacing = 32.dp,
                ) {
                    val item = itempager[it]
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .size(
                                dimensions.screenWidthDp.dp * 0.70f,
                                dimensions.screenHeightDp.dp / 3
                            )
                            .padding(horizontal = 32.dp)
                            .graphicsLayer {
                                // Calculate the absolute offset for the current page from the
                                // scroll position. We use the absolute value which allows us to mirror
                                // any effects for both directions
                                val pageOffset = (
                                        (pagerState.currentPage - it) + pagerState
                                            .currentPageOffsetFraction
                                        ).absoluteValue

                                // We animate the alpha, between 50% and 100%
                                alpha = lerp(
                                    start = 0.5f.toDp(),
                                    stop = 1f.toDp(),
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                ).toPx()
                            },
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(item.imageLink)
                                .placeholder(localresource.drawable.placeholderloadingimage)
                                .error(localresource.drawable.brokenimage)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.8f)),
                                        ),
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${item.name}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "${item.price} php",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun searchpreview() {
    SearchScreen(viewModel<FishifyViewModel>(), rememberNavController())
}