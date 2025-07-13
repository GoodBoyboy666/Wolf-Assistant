package top.goodboyboy.hutassistant.ui.login

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
import top.goodboyboy.hutassistant.ScreenRoute

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
                    label = { Text("学号") },
                    leadingIcon = { Icon(Icons.Rounded.AssignmentInd, null) },
                    placeholder = { Text("请输入学号") },
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
                    label = { Text("密码") },
                    onValueChange = { passwd = it },
                    placeholder = { Text("请输入密码") },
                    leadingIcon = { Icon(Icons.Rounded.Password, null) },
                    trailingIcon = {
                        if (!passwdVisible) {
                            IconButton(onClick = {
                                passwdVisible = true
                            }) {
                                Icon(Icons.Rounded.VisibilityOff, "显示密码")
                            }
                        } else {
                            IconButton(onClick = {
                                passwdVisible = false
                            }) {
                                Icon(Icons.Rounded.Visibility, "隐藏密码")
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
                                snackbarHostState.showSnackbar("请确保账号密码不为空！")
                            }
                        } else {
                            scope.launch {
                                viewModel.login(userId, passwd)
                            }
                        }
                    },
                    enabled = enableLoginButton,
                ) {
                    Text("登录")
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
                                "登录失败！" + (loginState as LoginViewModel.LoginState.Failed).message,
                            )
                        }
                    }

                    is LoginViewModel.LoginState.Idle -> {}
                }
            }
        }
        Text(
            "软件不会存储您的账户密码，仅存储登录时返回的令牌。",
            modifier = Modifier.padding(20.dp, top = 25.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
