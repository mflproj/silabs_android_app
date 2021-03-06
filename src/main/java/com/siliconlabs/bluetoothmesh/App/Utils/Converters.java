/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

// Converters - converts value between different numeral system
public class Converters {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    // Gets value in hexadecimal system
    public static String getHexValue(byte value[]) {
        if (value == null) {
            return "";
        }

        char[] hexChars = new char[value.length * 3];
        int v;
        for (int j = 0; j < value.length; j++) {
            v = value[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    // Gets value in hexadecimal system for single byte
    public static String getHexValue(byte b) {
        char[] hexChars = new char[2];
        int v;
        v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    // Gets value in ascii system
    public static String getAsciiValue(byte value[]) {
        if (value == null) {
            return "";
        }

        return new String(value);
    }

    // Gets value in decimal system
    public static String getDecimalValue(byte value[]) {
        if (value == null) {
            return "";
        }

        String result = "";
        for (byte b : value) {
            result += ((int) b & 0xff) + " ";
        }
        return result;
    }

    // Gets value in decimal system for single byte
    public static String getDecimalValue(byte b) {
        String result = "";
        result += ((int) b & 0xff);

        return result;
    }

    // Gets value in decimal system for single byte
    public static String getDecimalValueFromTwosComplement(byte b) {
        // the first bit of the byte in twos complement
        String result = "" + b;

        if ((b & 0xa0) > 0) {
            int val = b;
            val = ~val & 0xff;
            val = val + 0x01;

            // the sign of the value
            int sign = (b >>> 7) & 0x01;
            sign = sign > 0 ? -1 : 1;

            result = "" + (sign * val);
        }
        return result;
    }

    public static String getDecimalValueFromTwosComplement(String binaryString) {
        // default to hex value

        if (binaryString.length() > 64) {
            String binAsHex = (new BigInteger(binaryString, 2)).toString(16);
            return "0x" + binAsHex;
        }

        // prepend the sign up to 64 bits
        String result = "";
        String stringPrependExtendSign = binaryString;
        for (int i = 0; i < 64 - binaryString.length(); i++) {
            stringPrependExtendSign = binaryString.substring(0, 1) + stringPrependExtendSign;
        }

        // flip the bits (needed for negative numbers)
        String flippedBits = "";
        for (int i = 0; i < 64; i++) {
            if (binaryString.subSequence(0, 1).equals("1")) {
                // flip bits if negative twos complement negative
                if (stringPrependExtendSign.substring(i, i + 1).equals("1")) {
                    flippedBits += 0;
                } else {
                    flippedBits += 1;
                }
            }
        }

        // if prepended sign extension is negative, add one to flipped bits and make long neg.
        if (binaryString.subSequence(0, 1).equals("1")) {
            // finish twos complement calculation if negative twos complement number
            long flippedBitsAsLong = Long.parseLong(flippedBits, 2);
            flippedBitsAsLong += 1;
            flippedBitsAsLong = -1 * flippedBitsAsLong;
            result = "" + flippedBitsAsLong;
        } else {
            result = "" + Long.parseLong(stringPrependExtendSign, 2);
        }

        return result;
    }

    public static byte[] convertStringTo(String input, String format) {
        if (TextUtils.isEmpty(input)) {
            return input.getBytes();
        }

        byte[] returnVal = null;

        // note that java uses big endian
        switch (format) {
            case "utf8s":
                returnVal = convertToUTF8(input);
                break;
            case "utf16s":
                returnVal = convertToUTF16(input);
                break;
            case "uint8":
                returnVal = convertToUint8(input);
                break;
            case "uint16":
                returnVal = convertToUint16(input);
                break;
            case "uint24":
                returnVal = convertToUint24(input);
                break;
            case "uint32":
                returnVal = convertToUint32(input);
                break;
            case "uint40":
                returnVal = convertToUint40(input);
                break;
            case "uint48":
                returnVal = convertToUint48(input);
                break;
            case "sint8":
                returnVal = convertToSint8(input);
                break;
            case "sint16":
                returnVal = convertToSint16(input);
                break;
            case "sint24":
                returnVal = convertToSint24(input);
                break;
            case "sint32":
                returnVal = convertToSint32(input);
                break;
            case "sint40":
                returnVal = convertToSint40(input);
                break;
            case "sint48":
                returnVal = convertToSint48(input);
                break;
            case "float32":
                returnVal = convertToFloat32(input);
                break;
            case "float64":
                returnVal = convertToFloat64(input);
                break;
            default:
                returnVal = input.getBytes();
        }

        return returnVal;
    }

    public static byte[] convertToUTF8(String input) {
        byte[] returnVal = null;
        try {
            returnVal = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            returnVal = input.getBytes();
            e.printStackTrace();
        }
        return returnVal;
    }

    public static byte[] convertToUTF16(String input) {
        byte[] returnVal = null;
        try {
            returnVal = input.getBytes("UTF-16");
        } catch (UnsupportedEncodingException e) {
            returnVal = input.getBytes();
            e.printStackTrace();
        }
        return returnVal;
    }

    public static byte[] convertToUint8(String input) {
        try {
            int val = Integer.parseInt(input);

            byte[] returnVal = new byte[1];
            returnVal[0] = (byte) (val & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToUint16(String input) {
        try {
            int val = Integer.parseInt(input);

            byte[] returnVal = new byte[2];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToUint24(String input) {
        try {
            int val = Integer.parseInt(input);

            byte[] returnVal = new byte[3];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToUint32(String input) {
        try {
            long val = Long.parseLong(input);

            byte[] returnVal = new byte[4];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);
            returnVal[3] = (byte) ((val >>> 24) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToUint40(String input) {
        try {
            long val = Long.parseLong(input);

            byte[] returnVal = new byte[5];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);
            returnVal[3] = (byte) ((val >>> 24) & 0xFF);
            returnVal[4] = (byte) ((val >>> 32) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToUint48(String input) {
        try {
            long val = Long.parseLong(input);

            byte[] returnVal = new byte[6];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);
            returnVal[3] = (byte) ((val >>> 24) & 0xFF);
            returnVal[4] = (byte) ((val >>> 32) & 0xFF);
            returnVal[5] = (byte) ((val >>> 40) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToSint8(String input) {
        try {
            int val = Integer.parseInt(input);

            byte[] returnVal = new byte[1];
            returnVal[0] = (byte) (val & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToSint16(String input) {
        try {
            int val = Integer.parseInt(input);

            byte[] returnVal = new byte[2];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToSint24(String input) {
        try {
            int val = Integer.parseInt(input);

            byte[] returnVal = new byte[3];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToSint32(String input) {
        try {
            long val = Long.parseLong(input);

            byte[] returnVal = new byte[4];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);
            returnVal[3] = (byte) ((val >>> 24) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToSint40(String input) {
        try {
            long val = Long.parseLong(input);

            byte[] returnVal = new byte[5];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);
            returnVal[3] = (byte) ((val >>> 24) & 0xFF);
            returnVal[4] = (byte) ((val >>> 32) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToSint48(String input) {
        try {
            long val = Long.parseLong(input);

            byte[] returnVal = new byte[6];
            returnVal[0] = (byte) (val & 0xFF);
            returnVal[1] = (byte) ((val >>> 8) & 0xFF);
            returnVal[2] = (byte) ((val >>> 16) & 0xFF);
            returnVal[3] = (byte) ((val >>> 24) & 0xFF);
            returnVal[4] = (byte) ((val >>> 32) & 0xFF);
            returnVal[5] = (byte) ((val >>> 40) & 0xFF);

            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToFloat32(String input) {
        try {
            float floatVal = Float.parseFloat(input);
            int intBits = Float.floatToIntBits(floatVal);
            byte[] returnVal = new byte[4];
            returnVal[0] = (byte) (intBits & 0xff);
            returnVal[1] = (byte) ((intBits >>> 8) & 0xff);
            returnVal[2] = (byte) ((intBits >>> 16) & 0xff);
            returnVal[3] = (byte) ((intBits >>> 24) & 0xff);
            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] convertToFloat64(String input) {
        try {
            double floatVal = Double.parseDouble(input);
            long longBits = Double.doubleToLongBits(floatVal);
            byte[] returnVal = new byte[8];
            returnVal[0] = (byte) (longBits & 0xff);
            returnVal[1] = (byte) ((longBits >>> 8) & 0xff);
            returnVal[2] = (byte) ((longBits >>> 16) & 0xff);
            returnVal[3] = (byte) ((longBits >>> 24) & 0xff);
            returnVal[4] = (byte) ((longBits >>> 32) & 0xff);
            returnVal[5] = (byte) ((longBits >>> 40) & 0xff);
            returnVal[6] = (byte) ((longBits >>> 48) & 0xff);
            returnVal[7] = (byte) ((longBits >>> 56) & 0xff);
            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return input.getBytes();
        }
    }

    public static byte[] reverse(byte[] array) {
        if (array == null) {
            return new byte[0];
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        return array;
    }

    /*******************************************
     * Transforms String Address to Byte Array *
     *******************************************/
    public static byte[] getMacBytes(String address) {
        String[] macAddressParts = address.split(":");
        // convert hex string to byte values
        final byte[] macAddressBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }
        return macAddressBytes;
    }

    public static byte[] hexToByteArray(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static Byte[] wrapByteArray(byte[] bytes) {
        Byte[] bytes_wrapper = new Byte[bytes.length];
        int index = 0;
        for (byte b : bytes) {
            bytes_wrapper[index] = b;
            index++;
        }
        return bytes_wrapper;
    }

    public static int[] unwrapIntArrayList(ArrayList<Integer> Ints) {
        int[] unboxed = new int[Ints.size()];
        int index = 0;
        for (Integer i : Ints) {
            unboxed[index] = i.intValue();
            index++;
        }
        return unboxed;
    }

    public static int atou16(byte[] bytes, int position) {
        return bytes[position] | bytes[position + 1];
    }

    public static int atou32(byte[] bytes, int position) {
        byte[] chunk = new byte[4];
        for (int i = 0; i < chunk.length; ++i) {
            chunk[i] = bytes[position + i];
        }
        return ByteBuffer.wrap(chunk).getInt();
    }

    public static byte[] inv_atou16(int value) {
        return new byte[]{
                (byte) (value >> 8), (byte) value
        };
    }

    public static byte[] inv_atou32(int value) {
        return new byte[]{
                (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value
        };
    }

    public static String shortString(int toString) {
        short value = (short) toString;
        byte[] b_value = {(byte) (value >> 8), (byte) (value & 0xff)};
        String ret = Converters.getHexValueNoSpace(b_value);
        return ret;
    }

    public static String getHexValueNoSpace(byte value[]) {
        if (value == null) {
            return "";
        }

        char[] hexChars = new char[value.length * 2];
        int v;
        for (int j = 0; j < value.length; j++) {
            v = value[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //

    public static int convertUint8ToInt(byte[] bytes, int offset) {
        return bytes[offset] & 0x000000FF;
    }

    public static int convertUint16ToInt(byte[] bytes, int offset) {
        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = bytes[1 + offset] << 8 & 0x0000FF00;

        return component1 | component2;
    }

    public static int convertUint24ToInt(byte[] bytes, int offset) {
        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = bytes[1 + offset] << 8 & 0x0000FF00;
        final int component3 = bytes[2 + offset] << 16 & 0x00FF0000;

        return component1 | component2 | component3;
    }

    public static int convertUint32ToInt(byte[] bytes, int offset) {
        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = bytes[1 + offset] << 8 & 0x0000FF00;
        final int component3 = bytes[2 + offset] << 16 & 0x00FF0000;
        final int component4 = bytes[3 + offset] << 24 & 0xFF000000;

        return component1 | component2 | component3 | component4;
    }

    public static float convertUint32ToFloat(byte[] bytes, int offset) {
        return Float.intBitsToFloat(convertInt32ToInt(bytes, offset));
    }

    public static int convertInt32ToInt(byte[] bytes, int offset) {
        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = bytes[1 + offset] << 8 & 0x0000FF00;
        final int component3 = bytes[2 + offset] << 16 & 0x00FF0000;
        final int component4 = bytes[3 + offset] << 24 & 0xFF000000;

        return component1 | component2 | component3 | component4;
    }

    public static int convertInt8ToInt(byte[] bytes, int offset) {
        int fillUp = bytes[offset] >= 0 ? 0 : -1;

        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = fillUp & 0x0000FF00;
        final int component3 = fillUp & 0x00FF0000;
        final int component4 = fillUp & 0xFF000000;

        return component1 | component2 | component3 | component4;
    }

    public static int convertInt16ToInt(byte[] bytes, int offset) {
        int fillUp = bytes[1 + offset] >= 0 ? 0 : -1;

        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = bytes[1 + offset] << 8 & 0x0000FF00;
        final int component3 = fillUp & 0x00FF0000;
        final int component4 = fillUp & 0xFF000000;

        return component1 | component2 | component3 | component4;
    }

    public static int convertInt24ToInt(byte[] bytes, int offset) {
        int fillUp = bytes[2 + offset] >= 0 ? 0 : -1;

        final int component1 = bytes[offset] & 0x000000FF;
        final int component2 = bytes[1 + offset] << 8 & 0x0000FF00;
        final int component3 = bytes[2 + offset] << 16 & 0x00FF0000;
        final int component4 = fillUp & 0xFF000000;

        return component1 | component2 | component3 | component4;
    }

    public static byte[] convertFloatToByteArray(float value) {
        final byte[] bytes = new byte[4];

        int intBits = Float.floatToIntBits(value);
        bytes[0] = (byte) (intBits);
        bytes[1] = (byte) (intBits >> 8);
        bytes[2] = (byte) (intBits >> 16);
        bytes[3] = (byte) (intBits >> 24);

        return bytes;
    }

    public static float convertByteArrayToFloat(byte[] bytes) {
        final int component4 = (bytes[0] & 0xFF);
        final int component3 = (bytes[1] & 0xFF) << 8;
        final int component2 = (bytes[2] & 0xFF) << 16;
        final int component1 = bytes[3] << 24;

        int intBits = component1 | component2 | component3 | component4;
        return Float.intBitsToFloat(intBits);
    }

    public static byte[] convertIntToUint8(int value) {
        return convertIntToInt8(value);
    }

    public static byte[] convertIntToInt8(int value) {
        final byte[] bytes = new byte[1];

        bytes[0] = (byte) (value);

        return bytes;
    }

    public static byte[] convertIntToUint16(int value) {
        return convertIntToInt16(value);
    }

    public static byte[] convertIntToInt16(int value) {
        final byte[] bytes = new byte[2];

        bytes[0] = (byte) (value);
        bytes[1] = (byte) (value >> 8);

        return bytes;
    }

    public static byte[] convertIntToUint24(int value) {
        return convertIntToInt24(value);
    }

    public static byte[] convertIntToInt24(int value) {
        final byte[] bytes = new byte[3];

        bytes[0] = (byte) (value);
        bytes[1] = (byte) (value >> 8);
        bytes[2] = (byte) (value >> 16);

        return bytes;
    }
}
