/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Colors thresholding image processor class

    This class processes an image to detect regions of specific colors (Red, Yellow, Green, Blue)
    based on defined color ranges in the HSV color space. It then annotates the image with bounding
    boxes and percentages showing the proportion of each detected color in each region. The image is
    processed in the following steps:

    1. Load the input image.
    2. Convert the image from BGR to HSV color space.
    3. Define color ranges for the colors Red, Yellow, Green, and Blue.
    4. For each color range:
        - Create a binary mask for the pixels within the color range.
        - Apply morphological operations to clean up the mask.
        - Find contours in the mask.
        - Filter contours based on size and merge nearby contours.
        - Draw bounding boxes and annotate the image with the color name and percentage of the color in the region.
    5. Return an `AnalysisResult` containing the annotated image, processing time, and color counts.
 */

package dev.namexdd.machinevision_hw.colors_thresholding

import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.sqrt

// Data class to hold the results of the image analysis
data class AnalysisResult(
    val fileName: String, // Name of the processed file
    val imageWithAnnotations: Mat, // Annotated image with bounding boxes and text
    val processingTimeMs: Long, // Time taken for processing in milliseconds
    val imageSize: Size, // Size of the image (width and height)
    val colorCounts: Map<String, Int> // A map of color names to the number of regions detected
)

object ImageProcessor {

    // Main method to analyze the image
    fun analyzeImage(imagePath: String, fileName: String, minPercent: Double): AnalysisResult {
        val startTime = System.currentTimeMillis() // Start time for processing

        // Step 1: Load the image from the given path
        val originalImage = Imgcodecs.imread(imagePath)
        if (originalImage.empty()) {
            throw IllegalArgumentException("Image could not be loaded: $imagePath") // Throw error if the image cannot be loaded
        }

        val imageSize = originalImage.size() // Get the size of the original image

        // Step 2: Convert the image from BGR to HSV color space
        val hsvImage = Mat()
        Imgproc.cvtColor(originalImage, hsvImage, Imgproc.COLOR_BGR2HSV)

        // Step 3: Define color ranges for the color detection (Red, Yellow, Green, Blue)
        val colorRanges = mapOf(
            "Red" to Pair(Scalar(0.0, 100.0, 100.0), Scalar(10.0, 255.0, 255.0)),
            "Yellow" to Pair(Scalar(20.0, 100.0, 100.0), Scalar(30.0, 255.0, 255.0)),
            "Green" to Pair(Scalar(35.0, 100.0, 100.0), Scalar(85.0, 255.0, 255.0)),
            "Blue" to Pair(Scalar(100.0, 100.0, 100.0), Scalar(140.0, 255.0, 255.0))
        )

        val colorCounts = mutableMapOf<String, Int>() // To store the number of regions detected for each color
        val annotatedImage = originalImage.clone() // Clone the original image to annotate it

        // Step 4: For each color, process the image to detect regions of that color
        for ((colorName, range) in colorRanges) {
            val mask = Mat() // Create a binary mask for the color
            Core.inRange(hsvImage, range.first, range.second, mask) // Create the mask based on color range

            // Step 4.1: Apply morphological operations to reduce noise in the mask
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0)) // Create a kernel for morphological operations
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel) // Close operation to fill small holes
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel) // Open operation to remove noise

            // Step 4.2: Find contours in the mask
            val contours = mutableListOf<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

            // Step 4.3: Filter and merge contours
            val filteredContours = contours.filter { Imgproc.contourArea(it) > 500.0 } // Filter out small contours
            val mergedContours = mergeCloseContours(filteredContours, 20.0) // Merge nearby contours

            // Step 4.4: Draw bounding boxes and annotate regions with sufficient color percentage
            mergedContours.forEach { contour ->
                val boundingRect = Imgproc.boundingRect(contour) // Get bounding box of the contour

                val maskROI = Mat(mask, boundingRect) // Get the region of interest (ROI) for the mask
                val nonZeroPixels = Core.countNonZero(maskROI) // Count the non-zero pixels in the mask
                val totalPixels = boundingRect.width * boundingRect.height // Total pixels in the bounding box
                val percentage = (nonZeroPixels.toDouble() / totalPixels) * 100 // Calculate percentage of color in the region

                // If the color percentage is above the minimum threshold, draw a bounding box and annotate
                if (percentage >= minPercent) {
                    // Draw the bounding box
                    Imgproc.rectangle(
                        annotatedImage,
                        Point(boundingRect.x.toDouble(), boundingRect.y.toDouble()),
                        Point((boundingRect.x + boundingRect.width).toDouble(), (boundingRect.y + boundingRect.height).toDouble()),
                        Scalar(0.0, 255.0, 0.0),
                        2
                    )

                    // Annotate the color and percentage on the image
                    val text = "$colorName: ${"%.2f".format(percentage)}%"
                    Imgproc.putText(
                        annotatedImage,
                        text,
                        Point(boundingRect.x.toDouble(), (boundingRect.y - 10).toDouble()),
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        0.5,
                        Scalar(0.0, 0.0, 0.0),
                        1
                    )
                }
            }

            colorCounts[colorName] = mergedContours.size // Store the number of detected regions for this color
        }

        val endTime = System.currentTimeMillis() // End time for processing
        return AnalysisResult(
            fileName = fileName,
            imageWithAnnotations = annotatedImage,
            processingTimeMs = endTime - startTime, // Calculate the processing time
            imageSize = imageSize,
            colorCounts = colorCounts // Return the color counts for each detected color
        )
    }

    // Helper function to merge nearby contours
    private fun mergeCloseContours(contours: List<MatOfPoint>, maxDistance: Double): List<MatOfPoint> {
        val mergedContours = mutableListOf<MatOfPoint>()
        val visited = BooleanArray(contours.size) { false }

        for (i in contours.indices) {
            if (visited[i]) continue

            val currentContour = contours[i]
            val boundingRect1 = Imgproc.boundingRect(currentContour)

            for (j in i + 1 until contours.size) {
                if (visited[j]) continue

                val boundingRect2 = Imgproc.boundingRect(contours[j])

                // Check if the two bounding boxes are close enough
                if (areBoundingBoxesClose(boundingRect1, boundingRect2, maxDistance)) {
                    visited[j] = true
                }
            }

            mergedContours.add(currentContour) // Add the contour to the merged list
        }
        return mergedContours
    }

    // Helper function to check if two bounding boxes are close enough based on their center distance
    private fun areBoundingBoxesClose(rect1: Rect, rect2: Rect, maxDistance: Double): Boolean {
        val center1 = Point((rect1.x + rect1.width / 2).toDouble(), (rect1.y + rect1.height / 2).toDouble())
        val center2 = Point((rect2.x + rect2.width / 2).toDouble(), (rect2.y + rect2.height / 2).toDouble())
        val distance = sqrt((center1.x - center2.x).pow(2.0) + (center1.y - center2.y).pow(2.0)) // Calculate distance between centers
        return distance < maxDistance // Return true if the distance is within the max allowed distance
    }
}