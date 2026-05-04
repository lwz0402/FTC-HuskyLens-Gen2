# HuskyLens Gen2 FTC Java Library

Java driver for **DFRobot HuskyLens Gen2** camera designed specifically for the FIRST Technology Challenge (FTC) platform and REV robot control.

## Development Information
- **Developer**: Arthur LIU from First Tech Challenge Team #25787 & #27570
- **Official Sponsor**: Proudly sponsored by **DFRobot**, the creators of HuskyLens.

## Features
- **Full Protocol Support**: Implements all commands from the official HuskyLens Protocol (v0.2).
- **All Algorithms**: Native support for Face Recognition, Object Tracking, Line Tracking, Color Recognition, Tag/AprilTag Recognition, and more.
- **Robust Communication**: Built-in I2C checksum validation to ensure data integrity during competition.
- **Custom UI**: Overlay custom text and shapes (rectangles) on the HuskyLens built-in LCD screen directly from your OpMode.
- **Multimedia Support**: Programmatic control over music playback and video/audio recording.
- **Private Data Access**: Retrieve specialized landmarks for faces (eyes, nose, mouth), hands, and poses.
- **Knowledge Base Management**: Save and load AI models (Knowledge Bases) across multiple slots.

## Installation
1. Ensure the `HuskyLens2.java` file is in your `TeamCode` folder (suggested package: `org.firstinspires.ftc.teamcode.huskylens`).
2. In the FTC Driver Station app, configure your I2C device:
   - **Type**: `HuskyLens2` (Matches the `@DeviceProperties` annotation in the code).
   - **Name**: "huskylens" (or your preferred name in your code).

## Project Structure
- **Essential Files**
  - **huskylens** (`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens`): The primary folder containing the driver and samples.
  - **HuskyLens2.java**: The core driver code containing all protocol logic and data models.
- **Optional Files**
  - **HuskyLensMasterSample.java**: A comprehensive OpMode demonstrating the use of every public function in the library.
  - **README.md**: This documentation.
  - **HuskyLensTagTracker.java**: A practical example of using AprilTag recognition to track and "shoot" a target.

---

## Full API Reference

### 1. Initialization & Configuration
- `knock()`: Check if the HuskyLens is connected and responding.
- `selectAlgorithm(Algorithm algorithm)`: Switch between AI modes (e.g., `ALGORITHM_TAG_RECOGNITION`).
- `setAlgorithmParam(Algorithm algo, String key, Object value)`: Set settings like `show_name` or `threshold`.
- `getAlgorithmParam(Algorithm algo, String key)`: Retrieve current settings from the sensor.
- `updateAlgorithmParams(Algorithm algo)`: Commit changes made to parameters.

### 2. Object Detection
- `requestBlocks(Algorithm algo)`: Get all detected blocks (centers, width, height, and private data).
- `requestArrows(Algorithm algo)`: Get all detected arrows (used for Line Tracking).

### 3. Custom UI & Drawing
- `drawRect(int color, int lineWidth, int x, int y, int w, int h)`: Draw a persistent rectangle.
- `drawUniqueRect(...)`: Draw a rectangle and clear all previous protocol-drawn shapes.
- `clearRect()`: Remove all protocol-drawn rectangles.
- `drawText(int color, int size, int x, int y, String text)`: Draw custom text.
- `clearText()`: Remove all custom text overlays.

### 4. Learning & Memory
- `learn(Algorithm algo)`: Trigger the "Learn" function for the current central object.
- `learnBlock(Algorithm algo, int x, int y, int w, int h)`: Learn a specific screen region.
- `forget()`: Clear learned data for the current algorithm.
- `saveKnowledge(Algorithm algo, int slot)`: Save the current model to a slot (0-4).
- `loadKnowledge(Algorithm algo, int slot)`: Load a model from a slot (0-4).

### 5. Multimedia & Capture
- `takePhoto(int resolution)`: Take a photo and save it to the SD card.
- `takeScreenshot()`: Capture the entire UI to the SD card.
- `playMusic(String name, int volume)`: Play music files stored on the device.
- `startRecording(int type, int duration, String file, int res)`: Start video/audio recording.
- `stopRecording(int type)`: Stop active recording.

### 6. System
- `setNameByID(Algorithm algo, int id, String name)`: Label a specific ID.
- `exit()`: Return to the main HuskyLens menu.

---

## Basic Usage Example

```java
HuskyLens2 huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");
huskyLens.selectAlgorithm(Algorithm.ALGORITHM_TAG_RECOGNITION);

// In your loop
List<Block> tags = huskyLens.requestBlocks(Algorithm.ALGORITHM_TAG_RECOGNITION);
for (Block tag : tags) {
    telemetry.addData("Tag ID", tag.id);
    telemetry.addData("Pos", "X: %d, Y: %d", tag.xCenter, tag.yCenter);
}
```

## License
MIT License

Copyright © 2026 Arthur LIU

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
