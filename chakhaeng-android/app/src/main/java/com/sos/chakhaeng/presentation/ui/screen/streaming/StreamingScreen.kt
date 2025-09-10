import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.sos.chakhaeng.presentation.ui.screen.streaming.component.VideoPlayer


@OptIn(UnstableApi::class)
@Composable
fun StreamingScreen() {
    val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

    VideoPlayer(
        url = hlsUrl,
        mimeType = MimeTypes.APPLICATION_M3U8,
        autoPlay = false,
        useController = false,
        initialMute = false,
        onBackFromFullscreen = { /* 전체화면에서 복귀 시 UI 처리 */ }
    )
}