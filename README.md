# FTC HuskyLens Gen2

FTC Java driver and sample OpModes for the DFRobot HUSKYLENS 2 / SEN0638 camera on a REV Control Hub.

This repository keeps the HuskyLens Gen2 work intentionally small and FTC-friendly:

- One driver file: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/HuskyLens2.java`
- One full API teaching sample: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/HuskyLens2MasterSample.java`
- One practical tag tracker sample: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/HuskyLens2TagTracker.java`

## Hardware Setup

1. Connect HUSKYLENS 2 to a REV Control Hub I2C port.
2. In Driver Station, open `Configure Robot`.
3. Add an I2C device using type `HuskyLens Gen2` / `HuskyLens2`.
4. Name the device `huskylens` for the included samples.
5. Save and restart the Robot Controller if prompted.

The default Gen2 I2C address is `0x50` 7-bit.

For multiple HUSKYLENS 2 cameras, use separate REV I2C ports unless every camera has a unique I2C address. Do not put multiple default-address `0x50` cameras on the same bus.

## Files

### `HuskyLens2.java`

Production driver for FTC SDK:

- FTC `I2cDeviceSynchDevice<I2cDeviceSynch>` hardware device
- `@I2cDeviceType` / `@DeviceProperties` registration
- Gen2 packet format: `55 AA command algorithm length payload checksum`
- I2C default address `0x50`
- Algorithm switching
- Block and arrow result parsing
- Non-blocking result request / poll API
- Bounded compatibility read helpers
- Learning, forgetting, knowledge save/load
- Display overlays, text drawing, screenshots/photos
- Music and recording commands
- Algorithm parameter helpers

### `HuskyLens2MasterSample.java`

Interactive TeleOp that demonstrates all public driver calls and shows returned data through telemetry.

Controls:

- D-pad left/right: cycle algorithms
- `A`: `knock()`
- `X`: learn and save knowledge
- `B`: load knowledge
- `Y`: forget
- Left bumper: draw overlays
- Right bumper: photo/screenshot demo
- Left trigger: start recording
- Right trigger: stop recording
- Start: algorithm parameter demo
- Back: multi-algorithm demo
- Left stick button: play music
- Right stick button: set name by ID

### `HuskyLens2TagTracker.java`

Practical tag tracking example:

- Selects Tag Recognition
- Reads tag blocks using non-blocking polling
- Picks the tag closest to optical center
- Pans `turretServo`
- Runs `shooterMotor` only after the tag is centered for several frames

Configure these hardware names if you run it:

- `huskylens`
- `turretServo`
- `shooterMotor`

## Recommended Non-Blocking Read Pattern

Use this pattern in TeleOp and Autonomous loops:

```java
HuskyLens2 huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");

huskyLens.knock();
huskyLens.selectAlgorithm(HuskyLens2.Algorithm.ALGORITHM_TAG_RECOGNITION);
huskyLens.beginResultRequest(HuskyLens2.Algorithm.ALGORITHM_TAG_RECOGNITION);

while (opModeIsActive()) {
    boolean complete = huskyLens.pollResultRequest(2, 4);
    HuskyLens2.ReadStatus status = huskyLens.getLastReadStatus();

    for (HuskyLens2.Block tag : huskyLens.getCachedBlocks()) {
        telemetry.addData("Tag ID", tag.id);
        telemetry.addData("Center", "%d, %d", tag.xCenter, tag.yCenter);
        telemetry.addData("Size", "%d x %d", tag.width, tag.height);
        telemetry.addData("Name", tag.name);
        telemetry.addData("Content", tag.content);
    }

    if (complete || isFinishedStatus(status)) {
        huskyLens.beginResultRequest(HuskyLens2.Algorithm.ALGORITHM_TAG_RECOGNITION);
    }

    telemetry.addData("Husky Status", status);
    telemetry.update();
    idle();
}
```

Terminal read states:

- `TIMEOUT`
- `TRUNCATED`
- `CHECKSUM_ERROR`
- `MALFORMED_PACKET`
- `PACKET_TOO_LARGE`
- `I2C_ERROR`

## API Summary

Connection and configuration:

- `initialize(HuskyLens2.Parameters parameters)`
- `getParameters()`
- `setI2cAddress(I2cAddr address)`
- `getI2cAddress()`
- `knock()`
- `exit()`

Algorithms:

- `selectAlgorithm(Algorithm algorithm)`
- `selectAlgorithm(int algorithmId)`
- `switchAlgorithm(Algorithm algorithm)`
- `setMultiAlgorithm(Algorithm... algorithms)`
- `setMultiAlgorithmRatio(int... ratios)`

Results:

- `beginResultRequest(Algorithm algorithm)`
- `pollResultRequest(int maxPackets, long maxMillis)`
- `getCachedInfo()`
- `getCachedBlocks()`
- `getCachedArrows()`
- `getLastReadStatus()`

Compatibility helpers with bounded waits:

- `requestInfo(Algorithm algorithm)`
- `requestBlocks(Algorithm algorithm)`
- `requestArrows(Algorithm algorithm)`

Learning and knowledge:

- `learn(Algorithm algorithm)`
- `learnBlock(Algorithm algorithm, int x, int y, int width, int height)`
- `forget()`
- `forget(Algorithm algorithm)`
- `saveKnowledge(Algorithm algorithm, int knowledgeId)`
- `loadKnowledge(Algorithm algorithm, int knowledgeId)`

Display and media:

- `drawRect(int color, int lineWidth, int x, int y, int width, int height)`
- `drawUniqueRect(int color, int lineWidth, int x, int y, int width, int height)`
- `clearRect()`
- `drawText(int color, int fontSize, int x, int y, String text)`
- `clearText()`
- `takePhoto()`
- `takePhoto(int resolution)`
- `takeScreenshot()`
- `playMusic(String name, int volume)`
- `startRecording(int mediaType, int duration, String filename, int resolution)`
- `stopRecording(int mediaType)`

Algorithm parameters:

- `getAlgorithmParamBoolean(Algorithm algorithm, String key)`
- `getAlgorithmParamFloat(Algorithm algorithm, String key)`
- `getAlgorithmParamString(Algorithm algorithm, String key)`
- `getAlgorithmParam(Algorithm algorithm, String key)`
- `setAlgorithmParam(Algorithm algorithm, String key, boolean value)`
- `setAlgorithmParam(Algorithm algorithm, String key, float value)`
- `setAlgorithmParam(Algorithm algorithm, String key, String value)`
- `setAlgorithmParam(Algorithm algorithm, String key, Object value)`
- `updateAlgorithmParams(Algorithm algorithm)`

Miscellaneous:

- `setNameByID(Algorithm algorithm, int id, String name)`

## Result Data

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

`Info` fields:

- `maxID`
- `totalResults`
- `totalResultsLearned`
- `totalBlocks`
- `totalBlocksLearned`

## FTC / REV Notes

- The driver uses raw command/response I2C reads and writes, not FTC read windows.
- REV I2C calls still take finite time, so use the split `beginResultRequest()` / `pollResultRequest()` API in robot control loops.
- `pollResultRequest(maxPackets, maxMillis)` limits work per loop.
- Multiple cameras are supported as separate configured FTC hardware devices.
- Prefer one HUSKYLENS 2 per physical REV I2C port.
- Bus 0 may share traffic with internal hub devices; external cameras are usually happier on other I2C buses.

## Coordinates

The driver follows the DFRobot Gen2 reference coordinate system:

- Width: `640`
- Height: `480`
- Center: `320, 240`

Use:

```java
int centerX = HuskyLens2.FRAME_WIDTH / 2;
int centerY = HuskyLens2.FRAME_HEIGHT / 2;
```

## References

- DFRobot SEN0638 wiki: https://wiki.dfrobot.com/sen0638/
- DFRobot HUSKYLENS V2 library: https://github.com/DFRobot/DFRobot_HuskylensV2
- REV I2C documentation: https://docs.revrobotics.com/duo-control/sensors/i2c

## License

MIT License
