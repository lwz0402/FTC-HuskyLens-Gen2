# FTC HuskyLens Gen2

This repository is an FTC Android Studio project based on the official FTC SDK, extended with a Java driver and sample OpModes for **DFRobot HuskyLens Gen2**.

It now uses a single consolidated README as the primary project document. Content that previously lived in the project-level `TeamCode` and `huskylens` README files has been merged here.

## Development Information
- **Developer**: Arthur LIU from First Tech Challenge Team #25787 & #27570
- **Official Sponsor**: Proudly sponsored by **DFRobot**, the creators of HuskyLens.

## Project Summary

This project combines:

- The official FTC SDK project structure
- A custom FTC Java I2C driver for HuskyLens Gen2
- A practical AprilTag tracking sample
- A broader master sample for testing the public HuskyLens API
- Bundled official HuskyLens Gen2 protocol and reference materials

The HuskyLens Java driver in this repository is aligned with:

- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/HuskyLens2_Documents/HuskyLens2_Protocol.md`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/HuskyLens2_Documents/HuskyLens2_Wiki_compressed.pdf`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/HuskyLens2_Documents/DFRobot_HuskylensV2-master/`

## Requirements

- Android Studio Ladybug (2024.2) or later
- FTC Robot Controller / Driver Station environment compatible with this SDK branch
- A REV Control Hub or Expansion Hub
- HuskyLens Gen2 connected over I2C

## Repository Structure

- `FtcRobotController/`
  Official FTC SDK Robot Controller module and sample OpModes.
- `TeamCode/`
  Team module containing the HuskyLens driver, sample OpModes, and bundled reference docs.
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/HuskyLens2.java`
  Main FTC HuskyLens Gen2 Java driver.
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/HuskyLens2MasterSample.java`
  Interactive sample for switching algorithms and testing driver features.
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/HuskyLens2TagTracker.java`
  Practical AprilTag tracker example using a servo and shooter motor.
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/HuskyLens2_Documents/`
  Official protocol, wiki export, and DFRobot reference implementations kept as source material.

## FTC TeamCode Notes

`TeamCode` is the module where your team-specific OpModes live. In a normal FTC workflow:

1. Start from the `FtcRobotController` sample OpModes if you need a baseline.
2. Put your custom robot code in `TeamCode`.
3. Register OpModes with `@TeleOp` or `@Autonomous`.
4. Configure hardware names in the Robot Controller configuration to match your code.

## HuskyLens Gen2 FTC Support

The driver currently covers:

- I2C communication at address `0x50`
- Algorithm switching
- Result parsing for `Block` and `Arrow`
- AprilTag / Tag Recognition
- Learning, forgetting, and knowledge slot save / load
- Text and rectangle drawing on the HuskyLens display
- Photo / screenshot capture
- Music playback
- Audio / video recording
- Algorithm parameter read / write helpers for `boolean`, `float`, and `String`

## Hardware Configuration

### Minimum HuskyLens setup

1. Connect HuskyLens Gen2 to an I2C port.
2. Open the FTC Robot Controller configuration.
3. Add an I2C device with:
   - **Type**: `HuskyLens2`
   - **Name**: `huskylens`

### Additional hardware for the AprilTag tracker sample

If you want to run `HuskyLens2TagTracker`, also configure:

- `turretServo`
- `shooterMotor`

## Coordinate System

Following the official HuskyLens Gen2 V2 reference implementation, this project treats the HuskyLens image space as:

- Width: `640`
- Height: `480`
- Optical center: `320, 240`

Use:

```java
int centerX = HuskyLens2.FRAME_WIDTH / 2;
int centerY = HuskyLens2.FRAME_HEIGHT / 2;
```

## Included OpModes

### `HuskyLens2MasterSample`

Purpose:

- Test connectivity
- Switch between algorithms
- Inspect returned blocks and arrows
- Trigger photo, screenshot, learning, music, and recording features

### `HuskyLens2TagTracker`

Purpose:

- Switch HuskyLens to Tag Recognition
- Select the detected tag closest to the optical center
- Pan a servo until the tag is centered
- Only activate the shooter after lock is stable across multiple frames

## Quick Start

```java
HuskyLens2 huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");

if (huskyLens.knock()) {
    huskyLens.selectAlgorithm(HuskyLens2.Algorithm.ALGORITHM_TAG_RECOGNITION);
}
```

## AprilTag Example

```java
List<HuskyLens2.Block> tags =
        huskyLens.requestBlocks(HuskyLens2.Algorithm.ALGORITHM_TAG_RECOGNITION);

for (HuskyLens2.Block tag : tags) {
    telemetry.addData("Tag ID", tag.id);
    telemetry.addData("Center", "%d, %d", tag.xCenter, tag.yCenter);
    telemetry.addData("Size", "%d x %d", tag.width, tag.height);
    telemetry.addData("Name", tag.name);
    telemetry.addData("Content", tag.content);
}
```

## HuskyLens Java API Summary

### Connection and algorithm control

- `knock()`
- `switchAlgorithm(Algorithm algorithm)`
- `selectAlgorithm(Algorithm algorithm)`
- `requestInfo(Algorithm algorithm)`
- `exit()`

### Result retrieval

- `requestBlocks(Algorithm algorithm)`
- `requestArrows(Algorithm algorithm)`

`Block` fields:

- `id`
- `algorithmId`
- `algorithm`
- `xCenter`
- `yCenter`
- `width`
- `height`
- `name`
- `content`
- `privateData`

`Arrow` fields:

- `id`
- `level`
- `xTarget`
- `yTarget`
- `angle`
- `length`

### Learning and knowledge management

- `learn(Algorithm algorithm)`
- `learnBlock(Algorithm algorithm, int x, int y, int width, int height)`
- `forget()`
- `forget(Algorithm algorithm)`
- `saveKnowledge(Algorithm algorithm, int knowledgeId)`
- `loadKnowledge(Algorithm algorithm, int knowledgeId)`

### Drawing and UI overlay

- `drawRect(int color, int lineWidth, int x, int y, int width, int height)`
- `drawUniqueRect(int color, int lineWidth, int x, int y, int width, int height)`
- `clearRect()`
- `drawText(int color, int fontSize, int x, int y, String text)`
- `clearText()`

### Capture and media

- `takePhoto()`
- `takePhoto(int resolution)`
- `takeScreenshot()`
- `playMusic(String name, int volume)`
- `startRecording(int mediaType, int duration, String filename, int resolution)`
- `stopRecording(int mediaType)`

### Algorithm parameters

- `getAlgorithmParamBoolean(Algorithm algorithm, String key)`
- `getAlgorithmParamFloat(Algorithm algorithm, String key)`
- `getAlgorithmParamString(Algorithm algorithm, String key)`
- `getAlgorithmParam(Algorithm algorithm, String key)`
- `setAlgorithmParam(Algorithm algorithm, String key, boolean value)`
- `setAlgorithmParam(Algorithm algorithm, String key, float value)`
- `setAlgorithmParam(Algorithm algorithm, String key, String value)`
- `setAlgorithmParam(Algorithm algorithm, String key, Object value)`
- `updateAlgorithmParams(Algorithm algorithm)`

### Miscellaneous

- `setNameByID(Algorithm algorithm, int id, String name)`
- `setMultiAlgorithm(Algorithm... algorithms)`
- `setMultiAlgorithmRatio(int... ratios)`

## Notes on Protocol Behavior

- `RETURN_ARGS` is parsed using the same length-prefixed string layout used by DFRobot's current reference implementations.
- `takePhoto(int resolution)` is intentionally retained because DFRobot's official V2 reference libraries actively send a resolution payload, even though the protocol markdown is more minimal in that section.
- Drawing APIs use the official V2 implementation style with a 32-bit RGB color payload.
- Strings are encoded and decoded as UTF-8 so names and text overlays can safely include Chinese characters.

## Purchase
This project utilizes hardware and open-source resources provided by DFRobot.
- DFRobot Official Website: [https://www.dfrobot.com/](https://www.dfrobot.com/)(Applicable to countries and regions other than Chinese Mainland); [https://www.dfrobot.com.cn/](https://www.dfrobot.com.cn/)(Applicable to Chinese Mainland)
- DFRobot GitHub: [https://github.com/DFRobot/](https://github.com/DFRobot/)
- **HuskyLens Gen1**
- Product Page and Purchase Link (Official): [https://www.dfrobot.com/product-1922.html](https://www.dfrobot.com/product-1989.html)(Applicable to countries and regions other than Chinese Mainland); [https://www.dfrobot.com.cn/goods-2050.html](https://www.dfrobot.com.cn/goods-2050.html)(Applicable to Chinese Mainland)
- Reference Price: 34.9 USD; 249 CNY; 29.9 EUR; 25.9 GBP; 5490 JPY; 47.9 CAD; 48.9 AUD; 279 HKD
- Documentation: [https://wiki.dfrobot.com/sen0305/](https://wiki.dfrobot.com/sen0305/)(Applicable to countries and regions other than Chinese Mainland); [https://wiki.dfrobot.com.cn/_SKU_SEN0305_Gravity__HUSKYLENS_%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E6%91%84%E5%83%8F%E5%A4%B4](https://wiki.dfrobot.com.cn/_SKU_SEN0305_Gravity__HUSKYLENS_%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%E6%91%84%E5%83%8F%E5%A4%B4)(Applicable to Chinese Mainland)
> HuskyLens Gen1 is an easy-to-use artificial intelligence visual sensor with six major AI functions: facial recognition, object recognition, object tracking, line tracking, color recognition, and label recognition. Adopting high-performance Kendryte K210 AI processor, it runs fast and has high recognition accuracy. The biggest advantage lies in the ease of one click learning, which can complete AI model training without complex algorithm training or code writing. The onboard 2.0-inch IPS display allows for a WYSIWYG debugging process, while providing two communication interfaces: UART and I2C. It can easily connect to mainstream controllers such as Arduino, Raspberry Pi, LattePanda, micro: bit, etc., providing powerful offline visual recognition capabilities for projects such as robots, intelligent access control, and autonomous driving cars.

- **HuskyLens Gen2**
- Product Page and Purchase Link (Official): [https://www.dfrobot.com/product-2995.html](https://www.dfrobot.com/product-2995.html)(Applicable to countries and regions other than Chinese Mainland); [https://www.dfrobot.com.cn/goods-4198.html](https://www.dfrobot.com.cn/goods-4198.html)(Applicable to Chinese Mainland)
- Reference Price: 84.9 USD; 499 CNY; 72.9 EUR; 62.9 GBP; 13490 JPY; 115.9 CAD; 118.9 AUD; 669 HKD
- Documentation: [https://wiki.dfrobot.com/sen0305/](https://wiki.dfrobot.com/sen0305/)(Applicable to countries and regions other than Chinese Mainland); [https://wiki.dfrobot.com.cn/_SKU_SEN0638_Gravity_HUSKYLENS_2_AI_Camera_Vision_Sensor](https://wiki.dfrobot.com.cn/_SKU_SEN0638_Gravity_HUSKYLENS_2_AI_Camera_Vision_Sensor)(Applicable to Chinese Mainland)
> HuskyLens Gen2 is a simple and easy-to-use AI visual sensor with diverse gameplay. It uses a 6TOPS computing power dedicated AI chip and comes with over 20 pre installed AI models for face recognition, object detection, object classification, pose recognition, instance segmentation, and more. At the same time, users can also deploy self trained models to teach Erha how to recognize any target object through image recognition. Perfectly compatible with mainstream controllers such as Arduino, micro: bit, ESP32, UNIHIKER (M10, K10), control board, Raspberry Pi, etc., providing versatile visual solutions for diverse scenarios such as intelligent robots, industrial automation, education and research.

## Reference Docs Kept in Repository

The following files are intentionally preserved as upstream reference material:

- FTC SDK sample and project readmes shipped with the SDK
- DFRobot HuskyLens Gen2 protocol and reference implementation readmes under `HuskyLens2_Documents/`

This root README is the single maintained project overview for this repository.

## License

MIT License

Copyright © 2026 Arthur LIU
