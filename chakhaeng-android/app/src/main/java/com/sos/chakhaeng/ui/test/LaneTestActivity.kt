package com.sos.chakhaeng.ui.test

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.ai.LaneDetector_old
import com.sos.chakhaeng.core.ai.LaneModelSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class LaneTestActivity : ComponentActivity() {

    private var detector: LaneDetector_old? = null
    private var interpreter: Interpreter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 테스트 전용 레이아웃 (ImageView 하나만 있음)
        val imageView = ImageView(this)
        setContentView(imageView)

        lifecycleScope.launch {
            // 1) LaneModelSpec 생성
            val spec = LaneModelSpec(
                key = "lane_v2",
                assetPath = "models/culane_res18_dynamic.tflite",
                preferInputW = 1600,
                preferInputH = 320,
                numRow = 72,
                numCol = 81,
                numCellRow = 200,
                numCellCol = 100,
                numLanes = 4,
                cropRatio = 0.6f
            )
            // 2) Interpreter 초기화 (assets에서 모델 로드) → I/O는 백그라운드에서
            val modelBuffer = withContext(Dispatchers.IO) {
                FileUtil.loadMappedFile(this@LaneTestActivity, spec.assetPath)
            }
            interpreter = Interpreter(modelBuffer)
            detector = LaneDetector_old(interpreter!!, spec)
            // 3) 테스트용 이미지 로드 (리소스에서) → I/O는 백그라운드
            val bitmap: Bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeResource(resources, R.drawable.test5)
            }
            // 4) 추론 실행 (CPU 연산 → Default dispatcher)
            val lanes = withContext(Dispatchers.Default) {
                detector?.detect(bitmap)
            }
            // 5) 시각화 (결과 그리기)
            val drawn = withContext(Dispatchers.Default) {
                detector?.drawLanes(bitmap.copy(Bitmap.Config.ARGB_8888, true), lanes!!)
            }
            // 6) 결과 UI에 반영
            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(drawn)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { interpreter?.close() }
    }
}
