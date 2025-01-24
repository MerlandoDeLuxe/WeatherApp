package com.example.weatherapp.presentation.search


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.domain.entity.City


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    component: SearchComponent
) {
    val state by component.model.collectAsState()

    val focusRequester =
        remember { //Отвечает за фоукс в момент открытия SearchBar, чтобы клавиатура открывалась сразу
            FocusRequester()            //И это нужно сделать только при первой композиции, при первом открытии экрана
        }                               //Для этого нужно использовать функцию LaunchedEffect

    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    SearchBar(
        modifier = Modifier.focusRequester(focusRequester),
        placeholder = { Text(text = stringResource(R.string.search)) }, //До ввода текста в строку поиска здесь должен отображаться какой-то текст
        query = state.searchQuery,
        onQueryChange = { component.changeSearchQuery(it) },
        onSearch = { component.onClickSearch() },    //Метод вызывается при клике на лупу на клавиаутуре
        active = true, //видимый контент поиска
        onActiveChange = {},
        leadingIcon = { //Стрелка назад
            IconButton(onClick = { component.onClickBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.button_back)
                )
            }
        },
        trailingIcon = {//Иконка поиска в правой части
            IconButton(onClick = { component.onClickSearch() }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        }
    ) {
        when (val searchState = state.searchState) {
            SearchStore.SearchState.EmptyResult -> {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 18.sp),
                    text = stringResource(R.string.no_city_found)
                )
            }

            SearchStore.SearchState.Error -> {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 18.sp),
                    text = stringResource(R.string.something_went_wrong)
                )
            }

            SearchStore.SearchState.Initial -> {

            }

            SearchStore.SearchState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(100.dp),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }

            is SearchStore.SearchState.SuccessLoaded -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),   //Отступ между всеми элементами
                    contentPadding = PaddingValues(16.dp)   //Отступ от края экрана жо контента
                ) {
                    items(items = searchState.cities,
                        key = { it.id }
                    ) { city ->
                        CityCard(
                            city = city,
                            onCityClickListener = { component.onClickCity(city) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityCard(
    city: City,
    onCityClickListener: (City) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { onCityClickListener(city) }
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            )
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = city.country)
        }
    }
}