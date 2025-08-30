package top.goodboyboy.wolfassistant.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ScreenRoute

@Preview
@Composable
private fun LoginViewPreview() {
    LoginView(PaddingValues(), rememberNavController(), SnackbarHostState())
}

@Composable
fun LoginView(
    innerPadding: PaddingValues,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.AccountCircle,
            null,
            Modifier.size(72.dp),
        )

        var userId by remember { mutableStateOf("") }
        var passwd by remember { mutableStateOf("") }
        var passwdVisible by remember { mutableStateOf(false) }
        var enableUserTextField by remember { mutableStateOf(true) }
        var enablePasswdTextField by remember { mutableStateOf(true) }
        var enableLoginButton by remember { mutableStateOf(true) }

        OutlinedCard(
            modifier = Modifier.padding(top = 64.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                OutlinedTextField(
                    modifier =
                        Modifier
                            .padding(bottom = 10.dp)
                            .widthIn(min = 280.dp, max = 280.dp),
                    value = userId,
                    singleLine = true,
                    onValueChange = { userId = it.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.academic_number)) },
                    leadingIcon = { Icon(Icons.Rounded.AssignmentInd, null) },
                    placeholder = { Text(stringResource(R.string.enter_num)) },
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number,
                        ),
                    enabled = enableUserTextField,
                )
                OutlinedTextField(
                    modifier =
                        Modifier
                            .padding(bottom = 10.dp)
                            .widthIn(min = 280.dp, max = 280.dp),
                    value = passwd,
                    singleLine = true,
                    label = { Text(stringResource(R.string.password)) },
                    onValueChange = { passwd = it },
                    placeholder = { Text(stringResource(R.string.enter_passwd)) },
                    leadingIcon = { Icon(Icons.Rounded.Password, null) },
                    trailingIcon = {
                        if (!passwdVisible) {
                            IconButton(onClick = {
                                passwdVisible = true
                            }) {
                                Icon(Icons.Rounded.VisibilityOff, stringResource(R.string.show_passwd))
                            }
                        } else {
                            IconButton(onClick = {
                                passwdVisible = false
                            }) {
                                Icon(Icons.Rounded.Visibility, stringResource(R.string.hide_passwd))
                            }
                        }
                    },
                    visualTransformation =
                        if (passwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password,
                        ),
                    enabled = enablePasswdTextField,
                )
                ElevatedButton(
                    onClick = {
                        if (userId == "" || passwd == "") {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.id_passwd_note_empty))
                            }
                        } else {
                            scope.launch {
                                viewModel.login(userId, passwd)
                            }
                        }
                    },
                    enabled = enableLoginButton,
                ) {
                    Text(stringResource(R.string.login))
                }

                when (loginState) {
                    is LoginViewModel.LoginState.Loading -> {
                        enableUserTextField = false
                        enablePasswdTextField = false
                        enableLoginButton = false
                    }

                    is LoginViewModel.LoginState.Success -> {
                        enableUserTextField = true
                        enablePasswdTextField = true
                        enableLoginButton = true
                        navController.navigate(ScreenRoute.Home.route) {
                            popUpTo(0)
                        }
                    }

                    is LoginViewModel.LoginState.Failed -> {
                        enableUserTextField = true
                        enablePasswdTextField = true
                        enableLoginButton = true
                        LaunchedEffect(loginState) {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.login_failed) +
                                    (loginState as LoginViewModel.LoginState.Failed).message,
                            )
                        }
                    }

                    is LoginViewModel.LoginState.Idle -> {}
                }
            }
        }
        Text(
            stringResource(R.string.login_warning),
            modifier = Modifier.padding(20.dp, top = 25.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
