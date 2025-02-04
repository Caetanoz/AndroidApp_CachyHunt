package pt.ipp.estg.cachyhunt.ui.screens.leaderboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import pt.ipp.estg.cachyhunt.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory
import androidx.compose.ui.res.stringResource

@Composable
fun LeaderboardScreen()

{
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context = LocalContext.current).userDao(), context), context)
    )
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    viewModel.getAllUsers(
        onSuccess = { fetchedUsers ->
            users = fetchedUsers
        },
        onError = {
            // Handle error
        }
    )
    val lastMonthText = stringResource(R.string.last_month)
    val yourCountryText = stringResource(R.string.your_country)
    val allTimeText = stringResource(R.string.all_time)

    var selectedButton by remember { mutableStateOf("Last Month") }


    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Beige)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.leaderboard_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                RoundedButtonGroup(
                    selectedButton = selectedButton,
                    onPodiumClick = {
                        selectedButton = lastMonthText
                    },
                    onStarClick = {
                        selectedButton = yourCountryText
                    },
                    onCrownClick = {
                        selectedButton = allTimeText
                        users = users.sortedByDescending { it.totalPoints }
                    }
                )

                LeaderboardBody(users)
            }
        }
    )
}

@Composable
fun LeaderboardBody(users: List<User>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(users) { index, user ->
            LeaderboardRow(user, position = index + 1)
            if (index < users.size - 1) {
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(user: User, position: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$position",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(30.dp)
        )

        Image(
            painter = rememberImagePainter(user.photo),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = user.nickName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${user.totalPoints}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_coin),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLeaderboardScreen() {
    val sampleGeocaches = listOf(
        GeocacheCaptured(id = 1, geocacheId = 1, userId = 1, capturedAt = "2023-01-01"),
        GeocacheCaptured(id = 2, geocacheId = 2, userId = 2, capturedAt = "2023-01-02")
    )

    val sampleUsers = listOf(
        User(id = 1, nickName = "John Doe", email = "john@example.com", password = "pass123",
            photo = "", userLevel = "Expert", currentPoints = 1000, totalPoints = 5000),
        User(id = 2, nickName = "Jane Smith", email = "jane@example.com", password = "pass123",
            photo = "", userLevel = "Pro", currentPoints = 950, totalPoints = 4700),
        User(id = 3, nickName = "Alice Johnson", email = "alice@example.com", password = "pass123",
            photo = "", userLevel = "Master", currentPoints = 1200, totalPoints = 6000)
    )

    /*val viewModel = object : LeaderboardViewModel() {
        override val leaderboardData = MutableStateFlow(sampleUsers)
    }*/
    LeaderboardScreen()
}


@Composable
fun RoundedButtonGroup(
    selectedButton: String,
    onPodiumClick: () -> Unit,
    onStarClick: () -> Unit,
    onCrownClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            .height(48.dp)
    ) {
        TextButton(
            onClick = onPodiumClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (selectedButton == stringResource(R.string.last_month)) Color.White else NatureGreen,
                    RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
        ) {
            Text(
                text = "Last Month",
                color = if (selectedButton == stringResource(R.string.last_month)) Color.Black else Color.White
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
        )

        TextButton(
            onClick = onStarClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (selectedButton == stringResource(R.string.your_country)) Color.White else NatureGreen
                )
        ) {
            Text(
                text = "Your Country",
                color = if (selectedButton == stringResource(R.string.your_country)) Color.Black else Color.White
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
        )
//
        TextButton(
            onClick = onCrownClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (selectedButton == stringResource(R.string.all_time)) Color.White else NatureGreen,
                    RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Text(
                text = stringResource(R.string.all_time),
                color = if (selectedButton == stringResource(R.string.all_time)) Color.Black else Color.White
            )
        }
    }
}