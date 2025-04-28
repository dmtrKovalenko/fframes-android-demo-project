package com.fframes.hellorust

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.fframes.hellorust.ui.theme.HelloRustTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Native function declaration
    private external fun render(slug: String, output: String, tmpDir: String): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            HelloRustTheme {
                RenderScreen(
                    onRenderClick = { slug ->
                        val outputPath = getOutputVideoPath()
                        val tmpDir = getTemporaryDirectoryPath()
                        render(slug, outputPath, tmpDir)
                    },
                    getVideoPath = { getOutputVideoPath() }
                )
            }
        }
    }

    private fun getOutputVideoPath(): String {
        val outputDir = getExternalFilesDir(null)
        val outputFile = File(outputDir, "output_video.mp4")
        return outputFile.absolutePath
    }

    private fun getTemporaryDirectoryPath(): String {
        return cacheDir.absolutePath
    }
}

@Composable
fun RenderScreen(
    onRenderClick: (String) -> Boolean,
    getVideoPath: () -> String
) {
    val coroutineScope = rememberCoroutineScope()
    var renderState by remember { mutableStateOf(RenderState.IDLE) }
    var videoPath by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple non-experimental header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "FFrames ❤\uFE0F Android",
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),

                textAlign = TextAlign.Center
            )
        }

        // Content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (renderState) {
                RenderState.IDLE -> {
                    Text("Click the button below to render a video")
                    RenderButton(
                        onClick = {
                            renderState = RenderState.RENDERING
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = onRenderClick("android")
                                renderState = if (success) {
                                    videoPath = getVideoPath()
                                    Log.d("RenderScreen", "Render complete: $videoPath")
                                    RenderState.COMPLETED
                                } else {
                                    RenderState.FAILED
                                }
                            }
                        },
                        enabled = renderState == RenderState.IDLE
                    )
                }

                RenderState.RENDERING -> {
                    Text("Rendering video, please wait...")
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp)
                    )
                }

                RenderState.COMPLETED -> {
                    Text("Rendering complete!")
                    videoPath?.let { path ->
                        // Use Android's built-in VideoView
                        AndroidVideoPlayer(
                            videoPath = path,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                }

                RenderState.FAILED -> {
                    Text(
                        "Rendering failed. Please try again.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { renderState = RenderState.IDLE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@Composable
fun RenderButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text("Render Video")
    }
}

@Composable
fun AndroidVideoPlayer(
    videoPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                // Create media controller with play, pause, seek buttons
                val mediaController = MediaController(ctx)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)

                // Set video URI from file
                setVideoURI(Uri.fromFile(File(videoPath)))

                // Start playing when ready
                setOnPreparedListener { it.start() }
            }
        },
        modifier = modifier
    )
}

enum class RenderState {
    IDLE, RENDERING, COMPLETED, FAILED
}

@Preview(showBackground = true)
@Composable
fun RenderScreenPreview() {
    HelloRustTheme {
        RenderScreen(
            onRenderClick = { true },
            getVideoPath = { "" }
        )
    }
}
