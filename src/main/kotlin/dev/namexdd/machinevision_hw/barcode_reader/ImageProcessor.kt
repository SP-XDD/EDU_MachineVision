/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Barcode image processing class

    This class processes an image to detect and read barcodes using ZXing. The process works as follows:

    1. Load the input image using OpenCV.
    2. Convert the image from OpenCV Mat to BufferedImage format (required for ZXing).
    3. Use ZXing’s MultiFormatReader to attempt to decode the barcode from the image.
    4. If a barcode is detected, extract the data from the barcode and categorize it.
    5. Annotate the image with the barcode data and its category.
    6. Return the annotated image, processing time, image size, barcode data, and category.
 */

package dev.namexdd.machinevision_hw.barcode_reader

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

// Data class to hold the results of the image analysis
data class AnalysisResult(
    val annotatedImage: Mat, // Image with barcode annotations
    val processingTimeMs: Long, // Time taken for processing in milliseconds
    val imageSize: Size, // Size of the image (width and height)
    val barcodeData: String?, // Extracted barcode data (if available)
    val category: String // Category of the barcode based on predefined mapping
)

object ImageProcessor {

    // Predefined categories for known barcodes
    private val categories = mapOf(
        "AAA-bbb-0000" to "Sensors", // Example category
        "BBB-ccc-0001" to "Cameras", // Example category
        "CCC-ddd-1000" to "Batteries", // Example category
        "EEE-fff-9999" to "Defective Parts" // Example category
    )

    // Main method to analyze the image and read the barcode
    fun analyzeImage(imagePath: String, fileName: String): AnalysisResult {
        val startTime = System.currentTimeMillis() // Start time for processing

        // Step 1: Load the image from the given path
        val inputImage = Imgcodecs.imread(imagePath)
        if (inputImage.empty()) {
            throw IllegalArgumentException("Image could not be loaded.") // Throw an error if the image cannot be loaded
        }

        val imageSize = inputImage.size() // Get the size of the input image
        val annotatedImage = inputImage.clone() // Clone the input image to annotate it later

        // Step 2: Convert OpenCV Mat to BufferedImage format (required by ZXing for barcode detection)
        val bufferedImage = toBufferedImage(inputImage)

        // Initialize ZXing Barcode Reader
        val reader = MultiFormatReader()
        val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(bufferedImage)))

        var barcodeData: String? = null
        var category = "Unknown"

        try {
            // Step 3: Try to decode the barcode from the image
            val result = reader.decode(binaryBitmap)
            barcodeData = result.text // Get the barcode data (string)

            // Step 4: Determine the category based on barcode data
            val barcodeString = barcodeData ?: ""
            category = categories[barcodeString] ?: "Unknown" // Find the category based on predefined mapping

            // Step 5: Annotate the image with the barcode data and category
            Imgproc.putText(
                annotatedImage,
                "Barcode: $barcodeData", // Display barcode data on the image
                Point(10.0, annotatedImage.rows() - 30.0),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                1.0,
                Scalar(0.0, 255.0, 0.0),
                2
            )

            // Annotate with the category
            if (category == "Defective Parts") {
                Imgproc.putText(
                    annotatedImage,
                    "DEFECTIVE", // Special annotation for defective parts
                    Point(10.0, annotatedImage.rows() - 60.0),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    Scalar(0.0, 0.0, 255.0), // Red color for defective parts
                    2
                )
            } else {
                Imgproc.putText(
                    annotatedImage,
                    "Category: $category", // Display category
                    Point(10.0, annotatedImage.rows() - 60.0),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    Scalar(0.0, 255.0, 0.0),
                    2
                )
            }

        } catch (e: NotFoundException) {
            println("No barcode found in the image.") // No barcode found
        } catch (e: Exception) {
            println("Error decoding barcode: ${e.message}") // Error decoding barcode
        }

        val endTime = System.currentTimeMillis() // End time for processing
        return AnalysisResult(
            annotatedImage = annotatedImage, // Return the annotated image
            processingTimeMs = endTime - startTime, // Calculate and return processing time
            imageSize = imageSize, // Return the image size
            barcodeData = barcodeData, // Return the barcode data
            category = category // Return the category of the barcode
        )
    }

    // Convert OpenCV Mat to BufferedImage for ZXing
    private fun toBufferedImage(mat: Mat): BufferedImage {
        val data = ByteArray((mat.total() * mat.channels()).toInt()) // Create a byte array to hold image data
        mat.get(0, 0, data) // Copy the image data into the byte array
        val type = if (mat.channels() == 1) BufferedImage.TYPE_BYTE_GRAY else BufferedImage.TYPE_3BYTE_BGR // Set the image type based on channels
        val image = BufferedImage(mat.cols(), mat.rows(), type) // Create a BufferedImage
        image.raster.setDataElements(0, 0, mat.cols(), mat.rows(), data) // Set the image data in the BufferedImage
        return image // Return the BufferedImage
    }
}