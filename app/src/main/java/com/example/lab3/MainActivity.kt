package com.example.lab3

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lab3.ui.theme.Lab3Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab3Theme  {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main()
                }
            }
        }
    }
}

@Preview
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Main() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val tokenValue = remember {
        mutableStateOf(TextFieldValue())
    }

    val userNameValue = remember {
        mutableStateOf(TextFieldValue())
    }

    val studentIdvalue = remember {
        mutableStateOf(TextFieldValue())
    }
    val store = UserStore(context)
    val tokenText = store.getAccessToken.collectAsState(initial = "")
    val userNameText = store.getUserName.collectAsState(initial = "")
    val studentIdText = store.getStudentID.collectAsState(initial = "")

    Column(
        modifier = Modifier.clickable { keyboardController?.hide() }.background(Color(
            255,
            255,
            255,
            134
        )
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(text = "MAPD721 - Lab3", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(15.dp))

        Text(text = tokenText.value)

        Spacer(modifier = Modifier.height(15.dp))

        TextField(
            value = tokenValue.value,
            onValueChange = { tokenValue.value = it },
            label = { Text("Token") }
        )

        Spacer(modifier = Modifier.height(30.dp))


        Spacer(modifier = Modifier.height(15.dp))
        Text(text = userNameText.value)
        TextField(
            value = userNameValue.value,
            onValueChange = { userNameValue.value = it },
            label = { Text("UserName") }
        )

        Spacer(modifier = Modifier.height(15.dp))
        Text(text = studentIdText.value)
        TextField(
            value = studentIdvalue.value,
            onValueChange = { studentIdvalue.value = it },
            label = { Text("Student ID") }
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    store.saveData(tokenValue.value.text, userNameValue.value.text,studentIdvalue.value.text)
                }
            }
        ) {
            Text(text = "Save Data")
        }
    }
}