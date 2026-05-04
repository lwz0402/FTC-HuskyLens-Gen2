package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.TypeConversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@I2cDeviceType
@DeviceProperties(name = "HuskyLens Gen2", description = "DFRobot HuskyLens Gen2 AI Camera", xmlTag = "HuskyLens2")
public class HuskyLens2 extends I2cDeviceSynchDevice<I2cDeviceSynch> {

    public static final I2cAddr DEFAULT_ADDRESS = I2cAddr.create7bit(0x50);

    private static final int HEADER_0 = 0x55;
    private static final int HEADER_1 = 0xAA;

    // --- Constants ---

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

    // --- Data Models and Enums ---

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
        ALGORITHM_FALLDOWN_RECOGNITION(19);

        public final int id;
        Algorithm(int id) { this.id = id; }
        public static Algorithm fromId(int id) {
            for (Algorithm a : values()) { if (a.id == id) return a; }
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
        COMMAND_ACTION_CLEAN_RECT(0x27),
        COMMAND_ACTION_DRAW_TEXT(0x28),
        COMMAND_ACTION_CLEAR_TEXT(0x29),
        COMMAND_ACTION_PLAY_MUSIC(0x2A),
        COMMAND_EXIT(0x2B),
        COMMAND_ACTION_LEARN_BLOCK(0x2C),
        COMMAND_ACTION_DRAW_UNIQUE_RECT(0x2D),
        COMMAND_ACTION_START_RECORDING(0x2E),
        COMMAND_ACTION_STOP_RECORDING(0x2F);

        public final int id;
        Command(int id) { this.id = id; }
        public static Command fromId(int id) {
            for (Command c : values()) { if (c.id == id) return c; }
            return null;
        }
    }

    public static class Block {
        public int id;
        public int xCenter;
        public int yCenter;
        public int width;
        public int height;
        public String name;
        public String content;
        public byte[] privateData;

        @Override
        public String toString() {
            return String.format(Locale.US, "Block(id=%d, x=%d, y=%d, w=%d, h=%d, name=%s, content=%s)",
                    id, xCenter, yCenter, width, height, name, content);
        }
    }

    public static class Arrow {
        public int id;
        public int xTarget;
        public int yTarget;
        public int angle;
        public int length;

        @Override
        public String toString() {
            return String.format(Locale.US, "Arrow(id=%d, x=%d, y=%d, angle=%d, len=%d)",
                    id, xTarget, yTarget, angle, length);
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
            return String.format(Locale.US, "Info(maxID=%d, totalResults=%d, learned=%d, blocks=%d, blocksLearned=%d)",
                    maxID, totalResults, totalResultsLearned, totalBlocks, totalBlocksLearned);
        }
    }

    // --- Core Functionality ---

    public HuskyLens2(I2cDeviceSynch deviceSynch) {
        super(deviceSynch, true);
        this.deviceClient.setI2cAddress(DEFAULT_ADDRESS);
        super.registerArmingStateCallback(false);
        this.deviceClient.engage();
    }

    @Override
    protected boolean doInitialize() {
        return knock();
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "HuskyLens Gen2";
    }

    public boolean knock() {
        byte[] payload = new byte[10];
        payload[0] = 0x01; // Large RAM
        byte[] command = buildPacket(Command.COMMAND_KNOCK, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        if (response != null && response.length >= 2) {
            return response[1] == 0; // Success
        }
        return false;
    }

    public boolean selectAlgorithm(Algorithm algo) {
        return selectAlgorithm(algo.id);
    }

    public boolean selectAlgorithm(int algoId) {
        byte[] payload = new byte[10];
        payload[0] = (byte) algoId;
        byte[] command = buildPacket(Command.COMMAND_SET_ALGORITHM, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);

        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        if (response != null && response.length >= 2) {
            return response[1] == 0;
        }
        return false;
    }

    public List<Block> requestBlocks(Algorithm algo) {
        Info info = getInfo(algo);
        List<Block> blocks = new ArrayList<>();
        if (info == null) return blocks;

        for (int i = 0; i < info.totalBlocks; i++) {
            byte[] blockData = receivePacket(Command.COMMAND_RETURN_BLOCK);
            if (blockData != null) {
                blocks.add(parseBlock(blockData));
            }
        }
        return blocks;
    }

    public List<Arrow> requestArrows(Algorithm algo) {
        Info info = getInfo(algo);
        List<Arrow> arrows = new ArrayList<>();
        if (info == null) return arrows;

        for (int i = 0; i < (info.totalResults - info.totalBlocks); i++) {
            byte[] arrowData = receivePacket(Command.COMMAND_RETURN_ARROW);
            if (arrowData != null) {
                arrows.add(parseArrow(arrowData));
            }
        }
        return arrows;
    }

    public boolean forget() {
        byte[] command = buildPacket(Command.COMMAND_ACTION_FORGET, Algorithm.ALGORITHM_ANY, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public String takePhoto(int resolution) {
        byte[] payload = new byte[10];
        payload[0] = (byte) resolution;
        byte[] command = buildPacket(Command.COMMAND_ACTION_TAKE_PHOTO, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return parseStringResponse(response);
    }

    public String takeScreenshot() {
        byte[] command = buildPacket(Command.COMMAND_ACTION_TAKE_SCREENSHOT, Algorithm.ALGORITHM_ANY, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return parseStringResponse(response);
    }

    public int learn(Algorithm algo) {
        byte[] command = buildPacket(Command.COMMAND_ACTION_LEARN, algo, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        if (response != null && response.length >= 4) {
            return readShortLE(response, 2); // arg0_int
        }
        return 0;
    }

    public int learnBlock(Algorithm algo, int x, int y, int width, int height) {
        byte[] payload = new byte[10];
        // payload[0], [1] RFU
        writeShortLE(payload, 2, (short)x);
        writeShortLE(payload, 4, (short)y);
        writeShortLE(payload, 6, (short)width);
        writeShortLE(payload, 8, (short)height);
        byte[] command = buildPacket(Command.COMMAND_ACTION_LEARN_BLOCK, algo, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        if (response != null && response.length >= 4) {
            return readShortLE(response, 2);
        }
        return 0;
    }

    public boolean saveKnowledge(Algorithm algo, int knowledgeId) {
        byte[] payload = new byte[10];
        payload[0] = (byte) knowledgeId;
        byte[] command = buildPacket(Command.COMMAND_ACTION_SAVE_KNOWLEDGES, algo, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean loadKnowledge(Algorithm algo, int knowledgeId) {
        byte[] payload = new byte[10];
        payload[0] = (byte) knowledgeId;
        byte[] command = buildPacket(Command.COMMAND_ACTION_LOAD_KNOWLEDGES, algo, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean drawRect(int color, int lineWidth, int x, int y, int width, int height) {
        return drawRectBase(Command.COMMAND_ACTION_DRAW_RECT, color, lineWidth, x, y, width, height);
    }

    public boolean drawUniqueRect(int color, int lineWidth, int x, int y, int width, int height) {
        return drawRectBase(Command.COMMAND_ACTION_DRAW_UNIQUE_RECT, color, lineWidth, x, y, width, height);
    }

    private boolean drawRectBase(Command cmd, int color, int lineWidth, int x, int y, int width, int height) {
        byte[] payload = new byte[14];
        payload[0] = 0; // colorID (legacy/ignored?)
        payload[1] = (byte) lineWidth;
        writeShortLE(payload, 2, (short)x);
        writeShortLE(payload, 4, (short)y);
        writeShortLE(payload, 6, (short)width);
        writeShortLE(payload, 8, (short)height);
        writeShortLE(payload, 10, (short)0); // RFU
        writeInt32LE(payload, 12, color);
        byte[] command = buildPacket(cmd, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean clearRect() {
        byte[] command = buildPacket(Command.COMMAND_ACTION_CLEAN_RECT, Algorithm.ALGORITHM_ANY, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean drawText(int color, int fontSize, int x, int y, String text) {
        byte[] textBytes = text.getBytes();
        byte[] payload = new byte[15 + textBytes.length];
        payload[0] = 0;
        payload[1] = (byte) fontSize;
        writeShortLE(payload, 2, (short)x);
        writeShortLE(payload, 4, (short)y);
        // payload 6-9 RFU
        payload[10] = (byte) textBytes.length;
        System.arraycopy(textBytes, 0, payload, 11, textBytes.length);
        writeInt32LE(payload, 11 + textBytes.length, color);
        
        byte[] command = buildPacket(Command.COMMAND_ACTION_DRAW_TEXT, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean clearText() {
        byte[] command = buildPacket(Command.COMMAND_ACTION_CLEAR_TEXT, Algorithm.ALGORITHM_ANY, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean playMusic(String name, int volume) {
        byte[] nameBytes = name.getBytes();
        byte[] payload = new byte[11 + nameBytes.length];
        writeShortLE(payload, 2, (short)volume);
        payload[10] = (byte) nameBytes.length;
        System.arraycopy(nameBytes, 0, payload, 11, nameBytes.length);
        
        byte[] command = buildPacket(Command.COMMAND_ACTION_PLAY_MUSIC, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean setNameByID(Algorithm algo, int id, String name) {
        byte[] nameBytes = name.getBytes();
        byte[] payload = new byte[11 + nameBytes.length];
        payload[0] = (byte) id;
        payload[10] = (byte) nameBytes.length;
        System.arraycopy(nameBytes, 0, payload, 11, nameBytes.length);
        
        byte[] command = buildPacket(Command.COMMAND_SET_NAME_BY_ID, algo, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean startRecording(int mediaType, int duration, String filename, int resolution) {
        byte[] fileBytes = filename.getBytes();
        byte[] payload = new byte[11 + fileBytes.length];
        payload[0] = (byte) resolution;
        payload[1] = (byte) mediaType;
        writeShortLE(payload, 2, (short)duration);
        payload[10] = (byte) fileBytes.length;
        System.arraycopy(fileBytes, 0, payload, 11, fileBytes.length);
        
        byte[] command = buildPacket(Command.COMMAND_ACTION_START_RECORDING, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean stopRecording(int mediaType) {
        byte[] payload = new byte[10];
        payload[1] = (byte) mediaType;
        byte[] command = buildPacket(Command.COMMAND_ACTION_STOP_RECORDING, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean updateAlgorithmParams(Algorithm algo) {
        byte[] command = buildPacket(Command.COMMAND_UPDATE_ALGORITHM_PARAMS, algo, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean setMultiAlgorithm(Algorithm... algos) {
        if (algos.length > 3) return false;
        byte[] payload = new byte[10];
        payload[0] = (byte) algos.length;
        // payload[1] RFU
        for (int i = 0; i < algos.length; i++) {
            writeShortLE(payload, 2 + i * 2, (short) algos[i].id);
        }
        byte[] command = buildPacket(Command.COMMAND_SET_MULTI_ALGORITHM, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean setMultiAlgorithmRatio(int... ratios) {
        if (ratios.length > 3) return false;
        byte[] payload = new byte[10];
        payload[0] = (byte) ratios.length;
        for (int i = 0; i < ratios.length; i++) {
            writeShortLE(payload, 2 + i * 2, (short) ratios[i]);
        }
        byte[] command = buildPacket(Command.COMMAND_SET_MULTI_ALGORITHM_RATIO, Algorithm.ALGORITHM_ANY, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public Object getAlgorithmParam(Algorithm algo, String key) {
        byte[] keyBytes = key.getBytes();
        byte[] payload = new byte[11 + keyBytes.length];
        payload[10] = (byte) keyBytes.length;
        System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
        
        byte[] command = buildPacket(Command.COMMAND_GET_ALGO_PARAM, algo, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return parseGetParamResponse(response);
    }

    private Object parseGetParamResponse(byte[] response) {
        if (response == null || response.length < 2 || response[1] != 0) return null;
        int totalIntArgs = TypeConversion.unsignedByteToInt(response[0]);
        if (totalIntArgs == 1) {
            return readShortLE(response, 2) != 0;
        } else if (totalIntArgs == 2) {
            int v0 = readShortLE(response, 2);
            int v1 = readShortLE(response, 4);
            int bits = ((v1 & 0xFFFF) << 16) | (v0 & 0xFFFF);
            return Float.intBitsToFloat(bits);
        } else if (response.length > 10) {
            return parseStringResponse(response);
        }
        return null;
    }

    public boolean setAlgorithmParam(Algorithm algo, String key, Object value) {
        byte[] keyBytes = key.getBytes();
        byte[] payload;
        
        if (value instanceof Boolean) {
            payload = new byte[11 + keyBytes.length + 1];
            payload[0] = 1; // totalIntArgs
            writeShortLE(payload, 2, (short)((Boolean)value ? 1 : 0));
            payload[10] = (byte) keyBytes.length;
            System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
            payload[11 + keyBytes.length] = 0; // arg0_str len
        } else if (value instanceof Float) {
            payload = new byte[11 + keyBytes.length + 1];
            payload[0] = 2; // totalIntArgs
            float f = (Float)value;
            int bits = Float.floatToIntBits(f);
            writeShortLE(payload, 2, (short)(bits & 0xFFFF));
            writeShortLE(payload, 4, (short)((bits >> 16) & 0xFFFF));
            payload[10] = (byte) keyBytes.length;
            System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
            payload[11 + keyBytes.length] = 0; // arg0_str len
        } else if (value instanceof String) {
            byte[] valBytes = ((String)value).getBytes();
            payload = new byte[11 + keyBytes.length + 1 + valBytes.length];
            payload[10] = (byte) keyBytes.length;
            System.arraycopy(keyBytes, 0, payload, 11, keyBytes.length);
            payload[11 + keyBytes.length] = (byte) valBytes.length;
            System.arraycopy(valBytes, 0, payload, 12 + keyBytes.length, valBytes.length);
        } else {
            return false;
        }

        byte[] command = buildPacket(Command.COMMAND_SET_ALGO_PARAMS, algo, payload);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    public boolean exit() {
        byte[] command = buildPacket(Command.COMMAND_EXIT, Algorithm.ALGORITHM_ANY, new byte[0]);
        deviceClient.write(command);
        byte[] response = receivePacket(Command.COMMAND_RETURN_ARGS);
        return response != null && response.length >= 2 && response[1] == 0;
    }

    private String parseStringResponse(byte[] response) {
        if (response != null && response.length > 10) {
            int nameLen = TypeConversion.unsignedByteToInt(response[10]);
            if (response.length >= 11 + nameLen) {
                return new String(response, 11, nameLen);
            }
        }
        return null;
    }

    private void writeInt32LE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private void writeShortLE(byte[] data, int offset, short value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private Info getInfo(Algorithm algo) {
        byte[] command = buildPacket(Command.COMMAND_GET_RESULT, algo, new byte[0]);
        deviceClient.write(command);

        byte[] infoData = receivePacket(Command.COMMAND_RETURN_INFO);
        if (infoData != null) {
            Info info = new Info();
            info.maxID = TypeConversion.unsignedByteToInt(infoData[0]);
            info.totalResults = readShortLE(infoData, 2);
            info.totalResultsLearned = readShortLE(infoData, 4);
            info.totalBlocks = readShortLE(infoData, 6);
            info.totalBlocksLearned = readShortLE(infoData, 8);
            return info;
        }
        return null;
    }

    private Block parseBlock(byte[] data) {
        Block block = new Block();
        block.id = TypeConversion.unsignedByteToInt(data[0]);
        block.xCenter = readShortLE(data, 2);
        block.yCenter = readShortLE(data, 4);
        block.width = readShortLE(data, 6);
        block.height = readShortLE(data, 8);
        
        int offset = 10;
        if (data.length > offset) {
            int nameLen = TypeConversion.unsignedByteToInt(data[offset]);
            offset++;
            if (nameLen > 0 && data.length >= offset + nameLen) {
                block.name = new String(data, offset, nameLen);
                offset += nameLen;
            }
            if (data.length > offset) {
                int contentLen = TypeConversion.unsignedByteToInt(data[offset]);
                offset++;
                if (contentLen > 0 && data.length >= offset + contentLen) {
                    block.content = new String(data, offset, contentLen);
                    offset += contentLen;
                }
            }
            if (data.length > offset) {
                block.privateData = new byte[data.length - offset];
                System.arraycopy(data, offset, block.privateData, 0, block.privateData.length);
            }
        }
        return block;
    }

    private Arrow parseArrow(byte[] data) {
        Arrow arrow = new Arrow();
        arrow.id = TypeConversion.unsignedByteToInt(data[0]);
        arrow.xTarget = readShortLE(data, 2);
        arrow.yTarget = readShortLE(data, 4);
        arrow.angle = readShortLE(data, 6);
        arrow.length = readShortLE(data, 8);
        return arrow;
    }

    private short readShortLE(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8));
    }

    private byte[] buildPacket(Command cmd, Algorithm algo, byte[] data) {
        byte[] packet = new byte[5 + data.length + 1];
        packet[0] = (byte) HEADER_0;
        packet[1] = (byte) HEADER_1;
        packet[2] = (byte) cmd.id;
        packet[3] = (byte) algo.id;
        packet[4] = (byte) data.length;
        System.arraycopy(data, 0, packet, 5, data.length);
        
        int checksum = 0;
        for (int i = 0; i < packet.length - 1; i++) {
            checksum += TypeConversion.unsignedByteToInt(packet[i]);
        }
        packet[packet.length - 1] = (byte) (checksum & 0xFF);
        return packet;
    }

    private byte[] receivePacket(Command expectedCmd) {
        byte[] header = deviceClient.read(5);
        if (header.length < 5) return null;
        if (TypeConversion.unsignedByteToInt(header[0]) != HEADER_0 || 
            TypeConversion.unsignedByteToInt(header[1]) != HEADER_1) {
            return null;
        }

        int cmdId = TypeConversion.unsignedByteToInt(header[2]);
        if (cmdId != expectedCmd.id) return null;

        int dataLen = TypeConversion.unsignedByteToInt(header[4]);
        byte[] data = deviceClient.read(dataLen + 1); // data + checksum
        if (data.length < dataLen + 1) return null;

        int checksum = 0;
        for (byte b : header) checksum += TypeConversion.unsignedByteToInt(b);
        for (int i = 0; i < dataLen; i++) checksum += TypeConversion.unsignedByteToInt(data[i]);
        
        if ((checksum & 0xFF) != TypeConversion.unsignedByteToInt(data[dataLen])) {
            return null;
        }

        byte[] result = new byte[dataLen];
        System.arraycopy(data, 0, result, 0, dataLen);
        return result;
    }
}
