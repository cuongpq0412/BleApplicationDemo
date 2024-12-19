package com.example.bleapplicationdemo;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import kotlin.Pair;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

public class TemperatureMeasurements {
    public static final double TEMP_NONE_VALUE = 999999;
    public static String TAG = "cuongpq";
    public static double getPHValueZenTest(byte[] value) {
        String hexValue = bytesToHex(value);
        if (hexValue.length() < 14) {
            return 0;
        }
        String data1 = hexToBinary(hexValue.substring(2,4));
        String data1Bit1Bit0 = data1.substring(data1.length() - 2);
        int data1Bit1Bit0Value = Integer.parseInt(data1Bit1Bit0, 2);
        StringBuilder data2 = new StringBuilder(hexToBinary(hexValue.substring(4,8)));

        if (data2.length() < 16) {
            int missingValueCount = 16 - data2.length();
            for (int i = 0; i < missingValueCount; i++) {
                data2.insert(0, "0");
            }
        }
        int bit15Bit12 = Integer.parseInt(data2.substring(0, 4), 2);
        double phValue = ((double) Integer.parseInt(data2.substring(data2.length() - 12), 2)) - 2000;
        if (data1Bit1Bit0Value == 1) {
            phValue = phValue / 10;
        } else {
            phValue = phValue / 100;
        }
        double ph;
        if (bit15Bit12 == 1) {
            ph = phValue;
        } else {
            ph = 7 - phValue / 57.14;
        }
        return ph;
    }
    public static double toTpmDouble(byte[] value,ProbeType probeType) {
        try {
            if(probeType.equals(ProbeType.LUMITY)){
                //  System.out.println((value[4] & 0xFF) + ((value[5] & 0xFF)+((value[5] & 0xFF)+(value[5] & 0xFF))));

                String aString = new String(value);
              /*  int mantissa = (value[4] & 0xFF) + ((value[5] & 0xFF) * 256);

                if (value[3] == -1) {
                    mantissa -= (256 * 256);
                }


                double temperature = mantissa / 10.0;*/
                // final DecimalFormat df = new DecimalFormat("0.00");
                String hexString = bytesToHex(value);



                // double temperature = calculateTPM(hexString);

                double tpm = calculateTPM(hexString);

                // round to one decimal place
                return BigDecimal
                        .valueOf(tpm)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

            }


        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
        return 0;
    }
    public static double toDouble(byte[] value,ProbeType probeType) {
        try {
            if(probeType.equals(ProbeType.COMARK)){
                int mantissa = (value[1] & 0xFF) + ((value[2] & 0xFF) * 256);

                if (value[3] == -1) {
                    mantissa -= (256 * 256);
                }

                double temperature = mantissa / 10.0;

                // round to one decimal place
                return BigDecimal
                        .valueOf(temperature)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }
            else if(probeType.equals(ProbeType.BLUE2)){

                int mantissa = (value[0] & 0xFF) + ((value[1] & 0xFF) * 256);

                if (value[2] == -1) {
                    mantissa -= (256 * 256);
                }

                double temperature = mantissa / 10.0;

                // round to one decimal place
                return BigDecimal
                        .valueOf(temperature)
                        .doubleValue();
            }
            else if(probeType.equals(ProbeType.KWIKSWITCH)){
                System.out.println((value[4] & 0xFF) + ((value[5] & 0xFF)+((value[5] & 0xFF)+(value[5] & 0xFF))));

                String aString = new String(value);
              /*  int mantissa = (value[4] & 0xFF) + ((value[5] & 0xFF) * 256);

                if (value[3] == -1) {
                    mantissa -= (256 * 256);
                }


                double temperature = mantissa / 10.0;*/
                // final DecimalFormat df = new DecimalFormat("0.00");
                String[] data =  aString.split(",");
                double tempF = Double.parseDouble(data[1]);
                double tempValue = ((tempF - 32)*5)/9;
                // round to one decimal place
                return BigDecimal
                        .valueOf(tempF)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

            }else if(probeType.equals(ProbeType.CHEFSMART)){
                System.out.println((value[4] & 0xFF) + ((value[5] & 0xFF)+((value[5] & 0xFF)+(value[5] & 0xFF))));

                String aString = new String(value);
              /*  int mantissa = (value[4] & 0xFF) + ((value[5] & 0xFF) * 256);

                if (value[3] == -1) {
                    mantissa -= (256 * 256);
                }


                double temperature = mantissa / 10.0;*/
                // final DecimalFormat df = new DecimalFormat("0.00");
                String[] data =  aString.split(",");
                double tempF = Double.parseDouble(data[1]);
                if (data[0].equals("BAT")){
                    return TEMP_NONE_VALUE;
                }
                // round to one decimal place
                return BigDecimal
                        .valueOf(tempF)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

            }else if(probeType.equals(ProbeType.MFT)){
               /* String aString = new String(value);
                int mantissa = (value[0] & 0xFF) + ((value[1] & 0xFF) * 256);

                if (value[2] == -1) {
                    mantissa -= (256 * 256);
                }

                double temperature = mantissa / 10.0;*/

                byte[] tempByte = new byte[4];
                tempByte = Arrays.copyOf(value,4);


                ByteBuffer b = ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN);
                float tempFloat =  b.getFloat();
                double temperature = tempFloat;

                // round to one decimal place
                return BigDecimal
                        .valueOf(temperature)
                        .doubleValue();

            }
            else if(probeType.equals(ProbeType.LUMITY)){
                //  System.out.println((value[4] & 0xFF) + ((value[5] & 0xFF)+((value[5] & 0xFF)+(value[5] & 0xFF))));

                String aString = new String(value);
              /*  int mantissa = (value[4] & 0xFF) + ((value[5] & 0xFF) * 256);

                if (value[3] == -1) {
                    mantissa -= (256 * 256);
                }


                double temperature = mantissa / 10.0;*/
                // final DecimalFormat df = new DecimalFormat("0.00");
                String hexString = bytesToHex(value);
                Pair tempValue = calculateTemperature(hexString);
                double temperature = ((Number)tempValue.getFirst()).doubleValue();
                TemperatureUnit.Unit temperatureUnit = (TemperatureUnit.Unit)tempValue.getSecond();

                // round to one decimal place
                return BigDecimal
                        .valueOf(temperature)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

            } else if (probeType.equals(ProbeType.ZENTEST)) {
                String hexValue = bytesToHex(value);
                if (hexValue.length() < 14) {
                    return 0;
                }
                Log.i(TAG, "hexValue " + hexValue);
                String data1 = hexToBinary(hexValue.substring(2,4));
                String data2 = hexToBinary(hexValue.substring(4,8));
                String data3 = hexToBinary(hexValue.substring(8,10));
                StringBuilder data4 = new StringBuilder(hexToBinary(hexValue.substring(10)));
                if (data4.length() < 16) {
                    int missingValueCount = 16 - data4.length();
                    for (int i = 0; i < missingValueCount; i++) {
                        data4.insert(0, "0");
                    }
                }
                String bit12 = data4.substring(data4.length() - 13, data4.length() - 12);
                String bit11 = data4.substring(data4.length() - 12, data4.length() - 11);
                int bit14Bit13 = Integer.parseInt(data4.substring(data4.length() - 15, data4.length() - 13), 2);
                TemperatureType temperatureType = TemperatureType.NONE;
                if (bit12.equals("1")) {
                    temperatureType = TemperatureType.C;
                } else {
                    temperatureType = TemperatureType.F;
                }
                double temperatureValue = (double) Integer.parseInt(data4.substring(data4.length() - 11), 2);
                double tempResult;
                if (bit14Bit13 == 1) {
                    tempResult = temperatureValue / 10;
                } else {
                    tempResult = temperatureValue / 100;
                }
                if (temperatureType == TemperatureType.C) {
                    return tempResult;
                } else {
                    return convertFToC(tempResult);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
        return 0;
    }
    private static final Pair calculateTemperature(String hexPacketValue) {
        TemperatureUnit.Unit tempUnit = TemperatureUnit.Unit.CELSIUS;
        String tempHex;
        byte var5;
        byte var6;
        boolean var7;
        double resultTemp;
        String subHex;
        if (hexPacketValue.length() == 24) {
            byte var4 = 10;
            var5 = 12;
            boolean var12 = false;
            if (hexPacketValue == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                TemperatureUnit.Unit var13;
                label52: {
                    subHex = hexPacketValue.substring(var4, var5);
                    Intrinsics.checkExpressionValueIsNotNull(subHex, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                    tempHex = subHex;
                    switch(tempHex.hashCode()) {
                        case 1536:
                            if (tempHex.equals("00")) {
                                var13 = TemperatureUnit.Unit.FAHRENHEIT;
                                break label52;
                            }
                            break;
                        case 1537:
                            if (tempHex.equals("01")) {
                                var13 = TemperatureUnit.Unit.CELSIUS;
                                break label52;
                            }
                    }

                    var13 = TemperatureUnit.Unit.CELSIUS;
                }

                tempUnit = var13;
                var5 = 12;
                var6 = 16;
                var7 = false;
                if (hexPacketValue == null) {
                    throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
                } else {
                    subHex = hexPacketValue.substring(var5, var6);
                    Intrinsics.checkExpressionValueIsNotNull(subHex, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                    tempHex = subHex;

                    try {
                        resultTemp = (double)Integer.parseInt(tempHex, 16) / (double)10;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, e.toString());
                        resultTemp = 0.0D;
                    }

                    return new Pair(resultTemp, tempUnit);
                }
            }
        } else if (hexPacketValue.length() == 28) {
            var5 = 6;
            var6 = 10;
            var7 = false;
            if (hexPacketValue == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                subHex = hexPacketValue.substring(var5, var6);
                Intrinsics.checkExpressionValueIsNotNull(subHex, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                tempHex = subHex;

                try {
                    resultTemp = (double)Integer.parseInt(tempHex, 16) / (double)10;
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.toString());
                    resultTemp = 0.0D;
                }

                return new Pair(resultTemp, tempUnit);
            }
        } else {
            return new Pair(0.0D, tempUnit);
        }
    }
    public static double convertFToC(double fah) {
        return (5.0/9) * (fah -32);
    }
    private static final double calculateTPM(String hexPacketValue) {
        String subHex;
        String tpmHex;
        double temperature;
        byte byte4;
        byte byte5;
        boolean var6;
        if (hexPacketValue.length() == 24) {
            byte4 = 16;
            byte5 = 20;
            var6 = false;
            if (hexPacketValue == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                subHex = hexPacketValue.substring(byte4, byte5);
                Intrinsics.checkExpressionValueIsNotNull(subHex, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                tpmHex = subHex;

                try {
                    temperature = (double)Integer.parseInt(tpmHex, 16) / (double)10;
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.toString());
                    temperature = 0.0D;
                }

                return temperature;
            }
        } else if (hexPacketValue.length() == 28) {
            byte4 = 20;
            byte5 = 24;
            var6 = false;
            if (hexPacketValue == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                subHex = hexPacketValue.substring(byte4, byte5);
                Intrinsics.checkExpressionValueIsNotNull(subHex, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                tpmHex = subHex;

                try {
                    temperature = (double)Integer.parseInt(tpmHex, 16) / (double)10;
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.toString());
                    temperature = 0.0D;
                }

                return temperature;
            }
        } else {
            return 0.0D;
        }
    }
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
    private static String hexToBinary(String s) {
        return new BigInteger(s, 16).toString(2);
    }
    public enum ProbeType {
        BLUE2,
        COMARK,
        KWIKSWITCH,
        CHEFSMART,
        MFT,
        LUMITY,
        ZENTEST
    }
    public static enum HoldStatus {
        HOLD,
        MEASURING;
    }
    public static class TemperatureUnit {
        enum Unit {
            CELSIUS,
            FAHRENHEIT;
        }

        @NotNull
        private final String displayValue;



        @NotNull
        public final String getDisplayValue() {
            return this.displayValue;
        }

        private TemperatureUnit(String displayValue) {
            this.displayValue = displayValue;
        }
    }
    public enum TemperatureType {
        C, F, NONE
    }
}
