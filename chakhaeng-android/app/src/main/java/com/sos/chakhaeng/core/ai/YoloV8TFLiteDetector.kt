package com.sos.chakhaeng.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection as TaskDet
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class YoloV8TFLiteDetector(
    private val context: Context
) : Detector {

    private val detector: ObjectDetector by lazy {
        val base = BaseOptions.builder()
            .setNumThreads(4)
            .build()
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(base)
            .setMaxResults(50)
            .setScoreThreshold(0.30f)
            .build()

        // tflite 모델 넣어잇
        ObjectDetector.createFromFileAndOptions(context, "models/yolov8s.tflite", options)
    }

    override fun warmup() {
        val w = 320; val h = 320
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        detect(bmp, 0)
    }

    override fun detect(
        bitmap: Bitmap,
        rotation: Int
    ): List<Detection> {
        val image = TensorImage.fromBitmap(bitmap)
        val rot = when((rotation % 360 + 360) % 360) {
            0 -> ImageProcessingOptions.Orientation.TOP_LEFT
            90 -> ImageProcessingOptions.Orientation.RIGHT_TOP
            180 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            270 -> ImageProcessingOptions.Orientation.LEFT_BOTTOM
            else -> ImageProcessingOptions.Orientation.TOP_LEFT
        }
        val impOpts = ImageProcessingOptions.builder()
            .setOrientation(rot)
            .build()

        val results = detector.detect(image, impOpts)
        return results.flatMap { taskDetToDetections(it, bitmap.width, bitmap.height) }
    }

    private fun taskDetToDetections(d: TaskDet, w: Int, h: Int): List<Detection> {
        val bb: RectF = d.boundingBox
        val norm = RectF(
            bb.left / w.toFloat(),
            bb.top / h.toFloat(),
            bb.right / w.toFloat(),
            bb.bottom / h.toFloat()
        )

        return d.categories.map { cat ->
            val label = cat.displayName.ifEmpty { cat.label }
            Detection(
                label = label,
                score = cat.score,
                box = norm
            )
        }
    }

    override fun close() {
        try { detector.close() } catch (_: Throwable) {}
    }

}