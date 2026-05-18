package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@I2cDeviceType
@DeviceProperties(
        name = "HuskyLens Gen2",
        description = "DFRobot HuskyLens Gen2 AI Camera",
        xmlTag = "HuskyLens2"
)
public class HuskyLens2 extends I2cDeviceSynchDevice<I2cDeviceSynch> implements I2cAddrConfig {

    public static final I2cAddr DEFAULT_ADDRESS = I2cAddr.create7bit(0x50);
    private static final String TAG = "HuskyLens2";

    public static final int FRAME_WIDTH = 640;
    public static final int FRAME_HEIGHT = 480;

    public static final int RESOLUTION_DEFAULT = 0;
    public static final int RESOLUTION_640x480 = 1;
    public static final int RESOLUTION_1280x720 = 2;
    public static final int RESOLUTION_1920x1080 = 3;

    public static final int MEDIA_TYPE_AUDIO = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_BOTH = 3;

    public static final int COLOR_WHITE = 0xFFFFFF;
    public static final int COLOR_RED = 0xFF0000;
    public static final int COLOR_ORANGE = 0xFFA500;
    public static final int COLOR_YELLOW = 0xFFFF00;
    public static final int COLOR_GREEN = 0x00FF00;
    public static final int COLOR_CYAN = 0x00FFFF;
    public static final int COLOR_BLUE = 0x0000FF;
    public static final int COLOR_PURPLE = 0x800080;
    public static final int COLOR_PINK = 0xFFC0CB;
    public static final int COLOR_GRAY = 0x808080;
    public static final int COLOR_BLACK = 0x000000;
    public static final int COLOR_BROWN = 0xA52A2A;
    public static final int COLOR_OLIVE = 0x808000;
    public static final int COLOR_TEAL = 0x008080;
    public static final int COLOR_INDIGO = 0x4B0082;
    public static final int COLOR_MAGENTA = 0xFF00FF;

    private static final int HEADER_0 = 0x55;
    private static final int HEADER_1 = 0xAA;
    private static final int FIXED_DATA_BYTES = 10;
    private static final int MAX_MULTI_ALGORITHMS = 3;
    private static final int LYNX_MAX_I2C_READ_BYTES = 100;
    private static final int DEFAULT_RETRY_COUNT = 2;
    private static final int PACKET_HEADER_BYTES = 5;
    private static final int PACKET_CHECKSUM_BYTES = 1;

    public static class Parameters {
        public I2cAddr i2cAddr = DEFAULT_ADDRESS;
        public int commandTimeoutMs = 120;
        public int initializationTimeoutMs = 300;
        public long defaultPollTimeBudgetMs = 6;
        public int maxCachedResults = 24;
        public int maxI2cReadBytes = LYNX_MAX_I2C_READ_BYTES;
        public int retryCount = DEFAULT_RETRY_COUNT;

        public Parameters() {
        }

        public Parameters(Parameters other) {
            if (other == null) {
                other = new Parameters();
            }
            this.i2cAddr = other.i2cAddr;
            this.commandTimeoutMs = other.commandTimeoutMs;
            this.initializationTimeoutMs = other.initializationTimeoutMs;
            this.defaultPollTimeBudgetMs = other.defaultPollTimeBudgetMs;
            this.maxCachedResults = other.maxCachedResults;
            this.maxI2cReadBytes = other.maxI2cReadBytes;
            this.retryCount = other.retryCount;
        }
    }

    public enum ReadStatus {
        IDLE,
        REQUEST_SENT,
        IN_PROGRESS,
        COMPLETE,
        TIMEOUT,
        TRUNCATED,
        CHECKSUM_ERROR,
        MALFORMED_PACKET,
        PACKET_TOO_LARGE,
        I2C_ERROR
    }

    public enum Algorithm {
        ALGORITHM_ANY(0),
        ALGORITHM_FACE_RECOGNITION(1),
        ALGORITHM_OBJECT_RECOGNITION(2),
        ALGORITHM_OBJECT_TRACKING(3),
        ALGORITHM_COLOR_RECOGNITION(4),
        ALGORITHM_OBJECT_CLASSIFICATION(5),
        ALGORITHM_SELF_LEARNING_CLASSIFICATION(6),
        ALGORITHM_SEGMENT(7),
        ALGORITHM_HAND_RECOGNITION(8),
        ALGORITHM_POSE_RECOGNITION(9),
        ALGORITHM_LICENSE_RECOGNITION(10),
        ALGORITHM_OCR_RECOGNITION(11),
        ALGORITHM_LINE_TRACKING(12),
        ALGORITHM_EMOTION_RECOGNITION(13),
        ALGORITHM_GAZE_RECOGNITION(14),
        ALGORITHM_FACE_ORIENTATION(15),
        ALGORITHM_TAG_RECOGNITION(16),
        ALGORITHM_BARCODE_RECOGNITION(17),
        ALGORITHM_QRCODE_RECOGNITION(18),
        ALGORITHM_FALLDOWN_RECOGNITION(19),
        ALGORITHM_DEPTH_CAMERA(20),
        ALGORITHM_DONKEYCAR(21),
        ALGORITHM_CAMERA(22),
        ALGORITHM_RFU3(23),
        ALGORITHM_RFU4(24),
        ALGORITHM_CUSTOM0(25),
        ALGORITHM_CUSTOM1(26),
        ALGORITHM_CUSTOM2(27),
        ALGORITHM_CUSTOM_BEGIN(128);

        public final int id;

        Algorithm(int id) {
            this.id = id;
        }

        public static Algorithm fromId(int id) {
            for (Algorithm algorithm : values()) {
                if (algorithm.id == id) {
                    return algorithm;
                }
            }
            return ALGORITHM_ANY;
        }
    }

    public enum Command {
        COMMAND_KNOCK(0x00),
        COMMAND_GET_RESULT(0x01),
        COMMAND_GET_ALGO_PARAM(0x02),
        COMMAND_SET_ALGORITHM(0x0A),
        COMMAND_SET_NAME_BY_ID(0x0B),
        COMMAND_SET_MULTI_ALGORITHM(0x0C),
        COMMAND_SET_MULTI_ALGORITHM_RATIO(0x0D),
        COMMAND_SET_ALGO_PARAMS(0x0E),
        COMMAND_UPDATE_ALGORITHM_PARAMS(0x0F),
        COMMAND_RETURN_ARGS(0x1A),
        COMMAND_RETURN_INFO(0x1B),
        COMMAND_RETURN_BLOCK(0x1C),
        COMMAND_RETURN_ARROW(0x1D),
        COMMAND_ACTION_TAKE_PHOTO(0x20),
        COMMAND_ACTION_TAKE_SCREENSHOT(0x21),
        COMMAND_ACTION_LEARN(0x22),
        COMMAND_ACTION_FORGET(0x23),
        COMMAND_ACTION_SAVE_KNOWLEDGES(0x24),
        COMMAND_ACTION_LOAD_KNOWLEDGES(0x25),
        COMMAND_ACTION_DRAW_RECT(0x26),
        COMMAND_ACTION_CLEAR_RECT(0x27),
        COMMAND_ACTION_DRAW_TEXT(0x28),
        COMMAND_ACTION_CLEAR_TEXT(0x29),
        COMMAND_ACTION_PLAY_MUSIC(0x2A),
        COMMAND_EXIT(0x2B),
        COMMAND_ACTION_LEARN_BLOCK(0x2C),
        COMMAND_ACTION_DRAW_UNIQUE_RECT(0x2D),
        COMMAND_ACTION_START_RECORDING(0x2E),
        COMMAND_ACTION_STOP_RECORDING(0x2F);

        public final int id;

        Command(int id) {
            this.id = id;
        }
    }

    public static class Block {
        public int id;
        public int algorithmId;
        public Algorithm algorithm = Algorithm.ALGORITHM_ANY;
        public int xCenter;
        public int yCenter;
        public int width;
        public int height;
        public String name = "";
        public String content = "";
        public byte[] privateData = new byte[0];

        @Override
        public String toString() {
            return String.format(
                    Locale.US,
                    "Block(id=%d, algo=%s, x=%d, y=%d, w=%d, h=%d, name=%s, content=%s)",
                    id, algorithm, xCenter, yCenter, width, height, name, content
            );
        }
    }

    public static class Arrow {
        public int id;
        public int level;
        public int xTarget;
        public int yTarget;
        public int angle;
        public int length;

        @Override
        public String toString() {
            return String.format(
                    Locale.US,
                    "Arrow(id=%d, level=%d, x=%d, y=%d, angle=%d, len=%d)",
                    id, level, xTarget, yTarget, angle, length
            );
        }
    }

    public static class Info {
        public int maxID;
        public int totalResults;
        public int totalResultsLearned;
        public int totalBlocks;
        public int totalBlocksLearned;

        @Override
        public String toString() {
            return String.format(
                    Locale.US,
                    "Info(maxID=%d, totalResults=%d, learned=%d, blocks=%d, blocksLearned=%d)",
                    maxID, totalResults, totalResultsLearned, totalBlocks, totalBlocksLearned
            );
        }
    }

    private static final class ArgsResponse {
        final int totalIntArgs;
        final boolean success;
        final int[] intArgs;
        final List<String> stringArgs;

        ArgsResponse(int totalIntArgs, boolean success, int[] intArgs, List<String> stringArgs) {
            this.totalIntArgs = totalIntArgs;
            this.success = success;
            this.intArgs = intArgs;
            this.stringArgs = stringArgs;
        }
    }

    private static final class Packet {
        final Command command;
        final int algorithmId;
        final byte[] payload;

        Packet(Command command, int algorithmId, byte[] payload) {
            this.command = command;
            this.algorithmId = algorithmId;
            this.payload = payload;
        }
    }

    private Parameters parameters = new Parameters();
    private Info cachedInfo;
    private final List<Block> cachedBlocks = new ArrayList<>();
    private final List<Arrow> cachedArrows = new ArrayList<>();
    private ReadStatus lastReadStatus = ReadStatus.IDLE;
    private Algorithm pendingResultAlgorithm = Algorithm.ALGORITHM_ANY;
    private boolean resultRequestActive = false;
    private boolean resultInfoReceived = false;
    private boolean resultWasTruncated = false;
    private int expectedBlocks = 0;
    private int expectedArrows = 0;
    private int receivedBlocks = 0;
    private int receivedArrows = 0;
    private byte[] receiveBuffer = new byte[0];

    public HuskyLens2(I2cDeviceSynch deviceSynch) {
        super(deviceSynch, true);
        this.deviceClient.setI2cAddress(parameters.i2cAddr);
        super.registerArmingStateCallback(false);
        this.deviceClient.engage();
    }

    @Override
    protected synchronized boolean doInitialize() {
        return internalInitialize(parameters, parameters.initializationTimeoutMs);
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.DFRobot;
    }

    @Override
    public String getDeviceName() {
        return "HuskyLens Gen2";
    }

    public synchronized Parameters getParameters() {
        return new Parameters(parameters);
    }

    public synchronized boolean initialize(Parameters parameters) {
        this.parameters = validateParameters(parameters);
        this.isInitialized = internalInitialize(this.parameters, this.parameters.initializationTimeoutMs);
        return this.isInitialized;
    }

    @Override
    public synchronized void setI2cAddress(I2cAddr newAddress) {
        parameters.i2cAddr = newAddress == null ? DEFAULT_ADDRESS : newAddress;
        deviceClient.setI2cAddress(parameters.i2cAddr);
        resetResultState(ReadStatus.IDLE);
    }

    @Override
    public synchronized I2cAddr getI2cAddress() {
        return deviceClient.getI2cAddress();
    }

    public boolean knock() {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = 0x01;
        return isSuccessful(sendCommand(Command.COMMAND_KNOCK, Algorithm.ALGORITHM_ANY, payload));
    }

    public boolean switchAlgorithm(Algorithm algorithm) {
        return selectAlgorithm(algorithm);
    }

    public boolean selectAlgorithm(Algorithm algorithm) {
        if (algorithm == null) {
            return false;
        }
        return selectAlgorithm(algorithm.id);
    }

    public boolean selectAlgorithm(int algorithmId) {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = (byte) algorithmId;
        return isSuccessful(sendCommand(Command.COMMAND_SET_ALGORITHM, Algorithm.ALGORITHM_ANY, payload));
    }

    public Info requestInfo(Algorithm algorithm) {
        beginResultRequest(algorithm);
        pollResultRequestUntilComplete(parameters.commandTimeoutMs, true);
        return getCachedInfo();
    }

    public List<Block> requestBlocks(Algorithm algorithm) {
        beginResultRequest(algorithm);
        pollResultRequestUntilComplete(parameters.commandTimeoutMs, true);
        return getCachedBlocks();
    }

    public List<Arrow> requestArrows(Algorithm algorithm) {
        beginResultRequest(algorithm);
        pollResultRequestUntilComplete(parameters.commandTimeoutMs, true);
        return getCachedArrows();
    }

    public synchronized boolean beginResultRequest(Algorithm algorithm) {
        resetResultState(ReadStatus.REQUEST_SENT);
        pendingResultAlgorithm = algorithm == null ? Algorithm.ALGORITHM_ANY : algorithm;
        resultRequestActive = true;
        try {
            deviceClient.write(buildPacket(Command.COMMAND_GET_RESULT, pendingResultAlgorithm, new byte[0]));
            return true;
        } catch (RuntimeException exception) {
            RobotLog.ee(TAG, exception, "Failed to send HuskyLens2 result request");
            resetResultState(ReadStatus.I2C_ERROR);
            return false;
        }
    }

    public synchronized boolean pollResultRequest(int maxPackets, long maxMillis) {
        if (!resultRequestActive) {
            return lastReadStatus == ReadStatus.COMPLETE || lastReadStatus == ReadStatus.TRUNCATED;
        }

        int packetBudget = maxPackets <= 0 ? 1 : maxPackets;
        long timeBudgetMs = maxMillis <= 0 ? parameters.defaultPollTimeBudgetMs : maxMillis;
        long deadlineNs = System.nanoTime() + (timeBudgetMs * 1000000L);
        int packetsProcessed = 0;

        while (resultRequestActive && packetsProcessed < packetBudget) {
            Packet packet = receivePacket();
            if (packet == null) {
                if (isTerminalReadStatus(lastReadStatus)) {
                    resultRequestActive = false;
                } else if (lastReadStatus == ReadStatus.REQUEST_SENT) {
                    lastReadStatus = ReadStatus.IN_PROGRESS;
                }
                break;
            }

            packetsProcessed++;
            handleResultPacket(packet);

            if (System.nanoTime() >= deadlineNs) {
                break;
            }
        }

        return !resultRequestActive
                && (lastReadStatus == ReadStatus.COMPLETE || lastReadStatus == ReadStatus.TRUNCATED);
    }

    public synchronized List<Block> getCachedBlocks() {
        return new ArrayList<>(cachedBlocks);
    }

    public synchronized List<Arrow> getCachedArrows() {
        return new ArrayList<>(cachedArrows);
    }

    public synchronized Info getCachedInfo() {
        return cachedInfo == null ? null : copyInfo(cachedInfo);
    }

    public synchronized ReadStatus getLastReadStatus() {
        return lastReadStatus;
    }

    public int learn(Algorithm algorithm) {
        ArgsResponse response = sendCommand(Command.COMMAND_ACTION_LEARN, algorithm, new byte[0]);
        return firstIntArg(response);
    }

    public int learnBlock(Algorithm algorithm, int x, int y, int width, int height) {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        writeShortLE(payload, 2, x);
        writeShortLE(payload, 4, y);
        writeShortLE(payload, 6, width);
        writeShortLE(payload, 8, height);
        ArgsResponse response = sendCommand(Command.COMMAND_ACTION_LEARN_BLOCK, algorithm, payload);
        return firstIntArg(response);
    }

    public boolean forget() {
        return forget(Algorithm.ALGORITHM_ANY);
    }

    public boolean forget(Algorithm algorithm) {
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_FORGET, algorithm, new byte[0]));
    }

    public String takePhoto() {
        return takePhoto(RESOLUTION_1280x720);
    }

    public String takePhoto(int resolution) {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = (byte) resolution;
        return firstStringArg(sendCommand(Command.COMMAND_ACTION_TAKE_PHOTO, Algorithm.ALGORITHM_ANY, payload));
    }

    public String takeScreenshot() {
        return firstStringArg(sendCommand(Command.COMMAND_ACTION_TAKE_SCREENSHOT, Algorithm.ALGORITHM_ANY, new byte[0]));
    }

    public boolean saveKnowledge(Algorithm algorithm, int knowledgeId) {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = (byte) knowledgeId;
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_SAVE_KNOWLEDGES, algorithm, payload));
    }

    public boolean loadKnowledge(Algorithm algorithm, int knowledgeId) {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = (byte) knowledgeId;
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_LOAD_KNOWLEDGES, algorithm, payload));
    }

    public boolean drawRect(int color, int lineWidth, int x, int y, int width, int height) {
        return drawRectBase(Command.COMMAND_ACTION_DRAW_RECT, color, lineWidth, x, y, width, height);
    }

    public boolean drawUniqueRect(int color, int lineWidth, int x, int y, int width, int height) {
        return drawRectBase(Command.COMMAND_ACTION_DRAW_UNIQUE_RECT, color, lineWidth, x, y, width, height);
    }

    public boolean clearRect() {
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_CLEAR_RECT, Algorithm.ALGORITHM_ANY, new byte[0]));
    }

    public boolean drawText(int color, int fontSize, int x, int y, String text) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[16 + textBytes.length];
        payload[0] = 0;
        payload[1] = (byte) fontSize;
        writeShortLE(payload, 2, x);
        writeShortLE(payload, 4, y);
        writeShortLE(payload, 6, 0);
        writeShortLE(payload, 8, 0);
        payload[10] = (byte) textBytes.length;
        System.arraycopy(textBytes, 0, payload, 11, textBytes.length);
        payload[11 + textBytes.length] = 0;
        writeInt32LE(payload, 12 + textBytes.length, color);
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_DRAW_TEXT, Algorithm.ALGORITHM_ANY, payload));
    }

    public boolean clearText() {
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_CLEAR_TEXT, Algorithm.ALGORITHM_ANY, new byte[0]));
    }

    public boolean playMusic(String name, int volume) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[11 + nameBytes.length];
        writeShortLE(payload, 2, volume);
        payload[10] = (byte) nameBytes.length;
        System.arraycopy(nameBytes, 0, payload, 11, nameBytes.length);
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_PLAY_MUSIC, Algorithm.ALGORITHM_ANY, payload));
    }

    public boolean setNameByID(Algorithm algorithm, int id, String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[11 + nameBytes.length];
        payload[0] = (byte) id;
        payload[10] = (byte) nameBytes.length;
        System.arraycopy(nameBytes, 0, payload, 11, nameBytes.length);
        return isSuccessful(sendCommand(Command.COMMAND_SET_NAME_BY_ID, algorithm, payload));
    }

    public boolean startRecording(int mediaType, int duration, String filename, int resolution) {
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[11 + filenameBytes.length];
        payload[0] = (byte) resolution;
        payload[1] = (byte) mediaType;
        writeShortLE(payload, 2, duration);
        payload[10] = (byte) filenameBytes.length;
        System.arraycopy(filenameBytes, 0, payload, 11, filenameBytes.length);
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_START_RECORDING, Algorithm.ALGORITHM_ANY, payload));
    }

    public boolean stopRecording(int mediaType) {
        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[1] = (byte) mediaType;
        return isSuccessful(sendCommand(Command.COMMAND_ACTION_STOP_RECORDING, Algorithm.ALGORITHM_ANY, payload));
    }

    public boolean updateAlgorithmParams(Algorithm algorithm) {
        return isSuccessful(sendCommand(Command.COMMAND_UPDATE_ALGORITHM_PARAMS, algorithm, new byte[0]));
    }

    public boolean setMultiAlgorithm(Algorithm... algorithms) {
        if (algorithms == null || algorithms.length == 0 || algorithms.length > MAX_MULTI_ALGORITHMS) {
            return false;
        }

        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = (byte) algorithms.length;
        for (int i = 0; i < algorithms.length; i++) {
            writeShortLE(payload, 2 + (i * 2), algorithms[i].id);
        }
        return isSuccessful(sendCommand(Command.COMMAND_SET_MULTI_ALGORITHM, Algorithm.ALGORITHM_ANY, payload));
    }

    public boolean setMultiAlgorithmRatio(int... ratios) {
        if (ratios == null || ratios.length == 0 || ratios.length > MAX_MULTI_ALGORITHMS) {
            return false;
        }

        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = (byte) ratios.length;
        for (int i = 0; i < ratios.length; i++) {
            writeShortLE(payload, 2 + (i * 2), ratios[i]);
        }
        return isSuccessful(sendCommand(Command.COMMAND_SET_MULTI_ALGORITHM_RATIO, Algorithm.ALGORITHM_ANY, payload));
    }

    public Boolean getAlgorithmParamBoolean(Algorithm algorithm, String key) {
        ArgsResponse response = sendGetAlgorithmParam(algorithm, key);
        if (!isSuccessful(response) || response.totalIntArgs < 1) {
            return null;
        }
        return response.intArgs[0] != 0;
    }

    public Float getAlgorithmParamFloat(Algorithm algorithm, String key) {
        ArgsResponse response = sendGetAlgorithmParam(algorithm, key);
        if (!isSuccessful(response) || response.totalIntArgs < 2) {
            return null;
        }
        int low = response.intArgs[0] & 0xFFFF;
        int high = response.intArgs[1] & 0xFFFF;
        return Float.intBitsToFloat((high << 16) | low);
    }

    public String getAlgorithmParamString(Algorithm algorithm, String key) {
        return firstStringArg(sendGetAlgorithmParam(algorithm, key));
    }

    public Object getAlgorithmParam(Algorithm algorithm, String key) {
        ArgsResponse response = sendGetAlgorithmParam(algorithm, key);
        if (!isSuccessful(response)) {
            return null;
        }
        if (!response.stringArgs.isEmpty()) {
            return response.stringArgs.get(0);
        }
        if (response.totalIntArgs >= 2) {
            int low = response.intArgs[0] & 0xFFFF;
            int high = response.intArgs[1] & 0xFFFF;
            return Float.intBitsToFloat((high << 16) | low);
        }
        if (response.totalIntArgs >= 1) {
            return response.intArgs[0] != 0;
        }
        return null;
    }

    public boolean setAlgorithmParam(Algorithm algorithm, String key, boolean value) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[12 + keyBytes.length];
        payload[0] = 1;
        writeShortLE(payload, 2, value ? 1 : 0);
        payload[10] = (byte) keyBytes.length;
        System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
        payload[11 + keyBytes.length] = 0;
        return isSuccessful(sendCommand(Command.COMMAND_SET_ALGO_PARAMS, algorithm, payload));
    }

    public boolean setAlgorithmParam(Algorithm algorithm, String key, float value) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[12 + keyBytes.length];
        int bits = Float.floatToIntBits(value);
        payload[0] = 2;
        writeShortLE(payload, 2, bits & 0xFFFF);
        writeShortLE(payload, 4, (bits >>> 16) & 0xFFFF);
        payload[10] = (byte) keyBytes.length;
        System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
        payload[11 + keyBytes.length] = 0;
        return isSuccessful(sendCommand(Command.COMMAND_SET_ALGO_PARAMS, algorithm, payload));
    }

    public boolean setAlgorithmParam(Algorithm algorithm, String key, String value) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[12 + keyBytes.length + valueBytes.length];
        payload[10] = (byte) keyBytes.length;
        System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
        payload[11 + keyBytes.length] = (byte) valueBytes.length;
        System.arraycopy(valueBytes, 0, payload, 12 + keyBytes.length, valueBytes.length);
        return isSuccessful(sendCommand(Command.COMMAND_SET_ALGO_PARAMS, algorithm, payload));
    }

    public boolean setAlgorithmParam(Algorithm algorithm, String key, Object value) {
        if (value instanceof Boolean) {
            return setAlgorithmParam(algorithm, key, ((Boolean) value).booleanValue());
        }
        if (value instanceof Float) {
            return setAlgorithmParam(algorithm, key, ((Float) value).floatValue());
        }
        if (value instanceof String) {
            return setAlgorithmParam(algorithm, key, (String) value);
        }
        return false;
    }

    public boolean exit() {
        return isSuccessful(sendCommand(Command.COMMAND_EXIT, Algorithm.ALGORITHM_ANY, new byte[0]));
    }

    private boolean drawRectBase(Command command, int color, int lineWidth, int x, int y, int width, int height) {
        byte[] payload = new byte[16];
        payload[0] = 0;
        payload[1] = (byte) lineWidth;
        writeShortLE(payload, 2, x);
        writeShortLE(payload, 4, y);
        writeShortLE(payload, 6, width);
        writeShortLE(payload, 8, height);
        writeShortLE(payload, 10, 0);
        writeInt32LE(payload, 12, color);
        return isSuccessful(sendCommand(command, Algorithm.ALGORITHM_ANY, payload));
    }

    private ArgsResponse sendGetAlgorithmParam(Algorithm algorithm, String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[11 + keyBytes.length];
        payload[10] = (byte) keyBytes.length;
        System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
        return sendCommand(Command.COMMAND_GET_ALGO_PARAM, algorithm, payload);
    }

    private boolean internalInitialize(Parameters parameters, int timeoutMs) {
        this.parameters = validateParameters(parameters);
        deviceClient.setI2cAddress(this.parameters.i2cAddr);
        resetResultState(ReadStatus.IDLE);

        byte[] payload = new byte[FIXED_DATA_BYTES];
        payload[0] = 0x01;
        return isSuccessful(sendCommand(
                Command.COMMAND_KNOCK,
                Algorithm.ALGORITHM_ANY,
                payload,
                timeoutMs
        ));
    }

    private Parameters validateParameters(Parameters candidate) {
        Parameters validated = new Parameters(candidate == null ? new Parameters() : candidate);
        if (validated.i2cAddr == null) {
            validated.i2cAddr = DEFAULT_ADDRESS;
        }
        validated.commandTimeoutMs = clipInt(validated.commandTimeoutMs, 20, 2000);
        validated.initializationTimeoutMs = clipInt(validated.initializationTimeoutMs, 20, 3000);
        validated.defaultPollTimeBudgetMs = clipLong(validated.defaultPollTimeBudgetMs, 1, 100);
        validated.maxCachedResults = clipInt(validated.maxCachedResults, 1, 128);
        validated.maxI2cReadBytes = clipInt(
                validated.maxI2cReadBytes,
                PACKET_HEADER_BYTES + PACKET_CHECKSUM_BYTES,
                LYNX_MAX_I2C_READ_BYTES
        );
        validated.retryCount = clipInt(validated.retryCount, 1, 5);
        return validated;
    }

    private void pollResultRequestUntilComplete(int timeoutMs, boolean requireAllResultPackets) {
        int packetBudget = Math.max(1, Math.min(parameters.maxCachedResults + 1, 32));
        long timeBudgetMs = Math.max(1, Math.min(timeoutMs, parameters.commandTimeoutMs));
        boolean complete = pollResultRequest(packetBudget, timeBudgetMs);
        if (!complete && requireAllResultPackets && resultRequestActive) {
            resultRequestActive = false;
            lastReadStatus = ReadStatus.TIMEOUT;
        }
    }

    private void resetResultState(ReadStatus status) {
        cachedInfo = null;
        cachedBlocks.clear();
        cachedArrows.clear();
        lastReadStatus = status;
        pendingResultAlgorithm = Algorithm.ALGORITHM_ANY;
        resultRequestActive = false;
        resultInfoReceived = false;
        resultWasTruncated = false;
        expectedBlocks = 0;
        expectedArrows = 0;
        receivedBlocks = 0;
        receivedArrows = 0;
        receiveBuffer = new byte[0];
    }

    private void handleResultPacket(Packet packet) {
        if (packet.command == Command.COMMAND_RETURN_INFO) {
            cachedInfo = parseInfo(packet.payload);
            if (cachedInfo == null) {
                resultRequestActive = false;
                lastReadStatus = ReadStatus.MALFORMED_PACKET;
                return;
            }
            resultInfoReceived = true;
            expectedBlocks = Math.max(0, cachedInfo.totalBlocks);
            expectedArrows = Math.max(0, cachedInfo.totalResults - cachedInfo.totalBlocks);
            lastReadStatus = ReadStatus.IN_PROGRESS;
            finishResultRequestIfDone();
            return;
        }

        if (!resultInfoReceived) {
            lastReadStatus = ReadStatus.IN_PROGRESS;
            return;
        }

        if (packet.command == Command.COMMAND_RETURN_BLOCK) {
            receivedBlocks++;
            if (canCacheAnotherResult()) {
                cachedBlocks.add(parseBlock(packet.payload));
            } else {
                resultWasTruncated = true;
            }
        } else if (packet.command == Command.COMMAND_RETURN_ARROW) {
            receivedArrows++;
            if (canCacheAnotherResult()) {
                cachedArrows.add(parseArrow(packet.payload));
            } else {
                resultWasTruncated = true;
            }
        }

        finishResultRequestIfDone();
    }

    private void finishResultRequestIfDone() {
        if (resultInfoReceived && receivedBlocks >= expectedBlocks && receivedArrows >= expectedArrows) {
            resultRequestActive = false;
            lastReadStatus = resultWasTruncated ? ReadStatus.TRUNCATED : ReadStatus.COMPLETE;
        }
    }

    private boolean canCacheAnotherResult() {
        return cachedBlocks.size() + cachedArrows.size() < parameters.maxCachedResults;
    }

    private Info parseInfo(byte[] response) {
        if (response == null || response.length < FIXED_DATA_BYTES) {
            return null;
        }

        Info info = new Info();
        info.maxID = unsignedByte(response[0]);
        info.totalResults = Math.max(0, readShortLE(response, 2));
        info.totalResultsLearned = Math.max(0, readShortLE(response, 4));
        info.totalBlocks = Math.max(0, readShortLE(response, 6));
        info.totalBlocksLearned = Math.max(0, readShortLE(response, 8));
        if (info.totalBlocks > info.totalResults) {
            info.totalBlocks = info.totalResults;
        }
        return info;
    }

    private Info copyInfo(Info source) {
        Info copy = new Info();
        copy.maxID = source.maxID;
        copy.totalResults = source.totalResults;
        copy.totalResultsLearned = source.totalResultsLearned;
        copy.totalBlocks = source.totalBlocks;
        copy.totalBlocksLearned = source.totalBlocksLearned;
        return copy;
    }

    private Block parseBlock(byte[] data) {
        Block block = new Block();
        if (data == null || data.length < FIXED_DATA_BYTES) {
            return block;
        }
        block.id = unsignedByte(data[0]);
        block.algorithmId = data.length > 1 ? unsignedByte(data[1]) : 0;
        block.algorithm = Algorithm.fromId(block.algorithmId);
        block.xCenter = readShortLE(data, 2);
        block.yCenter = readShortLE(data, 4);
        block.width = readShortLE(data, 6);
        block.height = readShortLE(data, 8);

        int offset = 10;
        StringResult name = readLengthPrefixedString(data, offset);
        block.name = name.value;
        offset = name.nextOffset;

        StringResult content = readLengthPrefixedString(data, offset);
        block.content = content.value;
        offset = content.nextOffset;

        if (offset < data.length) {
            block.privateData = new byte[data.length - offset];
            System.arraycopy(data, offset, block.privateData, 0, block.privateData.length);
        }
        return block;
    }

    private Arrow parseArrow(byte[] data) {
        Arrow arrow = new Arrow();
        if (data == null || data.length < FIXED_DATA_BYTES) {
            return arrow;
        }
        arrow.id = unsignedByte(data[0]);
        arrow.level = data.length > 1 ? unsignedByte(data[1]) : 0;
        arrow.xTarget = readShortLE(data, 2);
        arrow.yTarget = readShortLE(data, 4);
        arrow.angle = readShortLE(data, 6);
        arrow.length = readShortLE(data, 8);
        return arrow;
    }

    private ArgsResponse sendCommand(Command command, Algorithm algorithm, byte[] payload) {
        return sendCommand(command, algorithm, payload, parameters.commandTimeoutMs);
    }

    private ArgsResponse sendCommand(Command command, Algorithm algorithm, byte[] payload, int timeoutMs) {
        byte[] response = transact(command, algorithm, payload, Command.COMMAND_RETURN_ARGS, timeoutMs);
        return parseArgsResponse(response);
    }

    private byte[] transact(
            Command command,
            Algorithm algorithm,
            byte[] payload,
            Command expectedResponse,
            int timeoutMs
    ) {
        byte[] packet = buildPacket(command, algorithm, payload);
        for (int attempt = 0; attempt < parameters.retryCount; attempt++) {
            try {
                deviceClient.write(packet);
                Packet response = waitForPacket(expectedResponse, timeoutMs);
                if (response != null) {
                    return response.payload;
                }
            } catch (RuntimeException exception) {
                RobotLog.ee(TAG, exception, "HuskyLens2 I2C transaction failed for %s", command);
                lastReadStatus = ReadStatus.I2C_ERROR;
            }
        }
        return null;
    }

    private Packet waitForPacket(Command expectedCommand, int timeoutMs) {
        int readAttempts = Math.max(1, Math.min(32, (Math.max(1, timeoutMs) / 5) + 1));
        for (int attempt = 0; attempt < readAttempts; attempt++) {
            Packet response = receivePacket();
            if (response != null && response.command == expectedCommand) {
                return response;
            }
            if (isTerminalReadStatus(lastReadStatus)) {
                return null;
            }
        }
        lastReadStatus = ReadStatus.TIMEOUT;
        return null;
    }

    private ArgsResponse parseArgsResponse(byte[] response) {
        if (response == null || response.length < FIXED_DATA_BYTES) {
            return new ArgsResponse(0, false, new int[0], Collections.<String>emptyList());
        }

        int totalIntArgs = unsignedByte(response[0]);
        boolean success = response[1] == 0;

        int argCount = Math.min(totalIntArgs, 4);
        int[] intArgs = new int[argCount];
        for (int i = 0; i < argCount; i++) {
            intArgs[i] = readShortLE(response, 2 + (i * 2));
        }

        List<String> stringArgs = new ArrayList<>();
        int offset = 10;
        while (offset < response.length) {
            int length = unsignedByte(response[offset]);
            offset++;
            if (length == 0 || offset + length > response.length) {
                break;
            }
            stringArgs.add(new String(response, offset, length, StandardCharsets.UTF_8));
            offset += length;
        }

        return new ArgsResponse(totalIntArgs, success, intArgs, stringArgs);
    }

    private boolean isSuccessful(ArgsResponse response) {
        return response != null && response.success;
    }

    private int firstIntArg(ArgsResponse response) {
        if (!isSuccessful(response) || response.intArgs.length == 0) {
            return 0;
        }
        return response.intArgs[0];
    }

    private String firstStringArg(ArgsResponse response) {
        if (!isSuccessful(response) || response.stringArgs.isEmpty()) {
            return "";
        }
        return response.stringArgs.get(0);
    }

    private byte[] buildPacket(Command command, Algorithm algorithm, byte[] data) {
        Algorithm packetAlgorithm = algorithm == null ? Algorithm.ALGORITHM_ANY : algorithm;
        if (data == null) {
            data = new byte[0];
        }
        byte[] packet = new byte[5 + data.length + 1];
        packet[0] = (byte) HEADER_0;
        packet[1] = (byte) HEADER_1;
        packet[2] = (byte) command.id;
        packet[3] = (byte) packetAlgorithm.id;
        packet[4] = (byte) data.length;
        System.arraycopy(data, 0, packet, 5, data.length);

        int checksum = 0;
        for (int i = 0; i < packet.length - 1; i++) {
            checksum += unsignedByte(packet[i]);
        }
        packet[packet.length - 1] = (byte) (checksum & 0xFF);
        return packet;
    }

    private Packet receivePacket() {
        Packet bufferedPacket = parseBufferedPacket();
        if (bufferedPacket != null || isTerminalReadStatus(lastReadStatus)) {
            return bufferedPacket;
        }

        byte[] chunk = readChunkFromDevice();
        if (chunk == null || chunk.length == 0) {
            if (lastReadStatus != ReadStatus.I2C_ERROR) {
                lastReadStatus = ReadStatus.IN_PROGRESS;
            }
            return null;
        }

        if (!isBlank(chunk)) {
            appendToReceiveBuffer(chunk);
        }
        return parseBufferedPacket();
    }

    private Packet parseBufferedPacket() {
        if (!alignReceiveBufferToHeader()) {
            return null;
        }
        if (receiveBuffer.length < PACKET_HEADER_BYTES) {
            lastReadStatus = ReadStatus.IN_PROGRESS;
            return null;
        }

        int dataLength = unsignedByte(receiveBuffer[4]);
        int packetLength = PACKET_HEADER_BYTES + dataLength + PACKET_CHECKSUM_BYTES;
        if (packetLength > parameters.maxI2cReadBytes) {
            discardReceiveBuffer(receiveBuffer.length);
            lastReadStatus = ReadStatus.PACKET_TOO_LARGE;
            return null;
        }
        if (receiveBuffer.length < packetLength) {
            lastReadStatus = ReadStatus.IN_PROGRESS;
            return null;
        }

        Command command = commandFromId(unsignedByte(receiveBuffer[2]));
        if (command == null) {
            discardReceiveBuffer(packetLength);
            lastReadStatus = ReadStatus.MALFORMED_PACKET;
            return null;
        }

        int checksum = 0;
        for (int i = 0; i < packetLength - PACKET_CHECKSUM_BYTES; i++) {
            checksum += unsignedByte(receiveBuffer[i]);
        }
        if ((checksum & 0xFF) != unsignedByte(receiveBuffer[packetLength - 1])) {
            discardReceiveBuffer(packetLength);
            lastReadStatus = ReadStatus.CHECKSUM_ERROR;
            return null;
        }

        int algorithmId = unsignedByte(receiveBuffer[3]);
        byte[] payload = new byte[dataLength];
        System.arraycopy(receiveBuffer, PACKET_HEADER_BYTES, payload, 0, dataLength);
        discardReceiveBuffer(packetLength);
        return new Packet(command, algorithmId, payload);
    }

    private byte[] readChunkFromDevice() {
        int readLength = clipInt(parameters.maxI2cReadBytes, PACKET_HEADER_BYTES + PACKET_CHECKSUM_BYTES, LYNX_MAX_I2C_READ_BYTES);
        try {
            return deviceClient.read(readLength);
        } catch (RuntimeException exception) {
            RobotLog.ee(TAG, exception, "HuskyLens2 I2C read failed");
            lastReadStatus = ReadStatus.I2C_ERROR;
            return null;
        }
    }

    private void appendToReceiveBuffer(byte[] chunk) {
        int oldLength = receiveBuffer.length;
        byte[] combined = new byte[oldLength + chunk.length];
        System.arraycopy(receiveBuffer, 0, combined, 0, oldLength);
        System.arraycopy(chunk, 0, combined, oldLength, chunk.length);
        receiveBuffer = combined;
    }

    private boolean alignReceiveBufferToHeader() {
        if (receiveBuffer.length == 0) {
            lastReadStatus = ReadStatus.IN_PROGRESS;
            return false;
        }

        int headerIndex = findHeaderIndex();
        if (headerIndex < 0) {
            boolean keepTrailingHeaderStart = unsignedByte(receiveBuffer[receiveBuffer.length - 1]) == HEADER_0;
            byte[] nextBuffer = keepTrailingHeaderStart ? new byte[] { receiveBuffer[receiveBuffer.length - 1] } : new byte[0];
            receiveBuffer = nextBuffer;
            lastReadStatus = ReadStatus.IN_PROGRESS;
            return false;
        }

        if (headerIndex > 0) {
            discardReceiveBuffer(headerIndex);
        }
        return true;
    }

    private int findHeaderIndex() {
        for (int i = 0; i < receiveBuffer.length - 1; i++) {
            if (unsignedByte(receiveBuffer[i]) == HEADER_0 && unsignedByte(receiveBuffer[i + 1]) == HEADER_1) {
                return i;
            }
        }
        return -1;
    }

    private void discardReceiveBuffer(int byteCount) {
        if (byteCount <= 0) {
            return;
        }
        if (byteCount >= receiveBuffer.length) {
            receiveBuffer = new byte[0];
            return;
        }

        int remaining = receiveBuffer.length - byteCount;
        byte[] trimmed = new byte[remaining];
        System.arraycopy(receiveBuffer, byteCount, trimmed, 0, remaining);
        receiveBuffer = trimmed;
    }

    private boolean isTerminalReadStatus(ReadStatus status) {
        return status == ReadStatus.CHECKSUM_ERROR
                || status == ReadStatus.MALFORMED_PACKET
                || status == ReadStatus.PACKET_TOO_LARGE
                || status == ReadStatus.I2C_ERROR;
    }

    private Command commandFromId(int id) {
        for (Command command : Command.values()) {
            if (command.id == id) {
                return command;
            }
        }
        return null;
    }

    private boolean isBlank(byte[] data) {
        if (data == null) {
            return true;
        }
        for (byte value : data) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    private int unsignedByte(byte value) {
        return TypeConversion.unsignedByteToInt(value);
    }

    private int clipInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private long clipLong(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private int readShortLE(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8));
    }

    private void writeShortLE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private void writeInt32LE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private StringResult readLengthPrefixedString(byte[] data, int offset) {
        if (offset >= data.length) {
            return new StringResult("", offset);
        }
        int length = unsignedByte(data[offset]);
        offset++;
        if (length <= 0 || offset + length > data.length) {
            return new StringResult("", Math.min(offset, data.length));
        }
        String value = new String(data, offset, length, StandardCharsets.UTF_8);
        return new StringResult(value, offset + length);
    }

    private static final class StringResult {
        final String value;
        final int nextOffset;

        StringResult(String value, int nextOffset) {
            this.value = value;
            this.nextOffset = nextOffset;
        }
    }
}
