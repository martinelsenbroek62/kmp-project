package com.nseindia.mc.util;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.nseindia.mc.controller.dto.LineValidationResult;

public class ValidationUtils {

    private static Pattern amountPattern = Pattern.compile("(\\d{1,15})?(\\.\\d{0,2})?");

    private static Pattern quantityPattern = Pattern.compile("\\d{1,9}");

    private static Pattern batchNumberPattern = Pattern.compile("\\d{1,2}");

    private static Pattern alphaPattern = Pattern.compile("^[a-zA-Z0-9\\. ]+$");
    
    public static boolean isSame(double a, double b) {
    	return String.format("%.2f", a).equals(String.format("%.2f", b));
    }

    public static boolean isSame(int a, int b) {
        return a == b;
    }

    public static boolean isSame(String a, Integer b) {
        if (!isInteger(a)) {
            return false;
        }

        return isSame(getInteger(a), b);
    }

    public static boolean isSame(String a, Double b) {
    	if (!isDouble(a)) {
    		return false;
    	}
    	
    	return isSame(getDouble(a), b);
    }

    public static boolean isSetNotContains(Set<String> set, String value) {
        return !set.contains(value);
    }

    public static boolean isDouble(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isInteger(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isPositive(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);

            return d > 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


    public static double getDouble(LineValidationResult line, int index) {
        if (index < line.getFields().size()) {
            try {
                return Double.parseDouble(line.getFields().get(index));
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
        return 0;
    }

    public static String getDoubleString(String strNum) {
        return String.format("%.2f", getDouble(strNum));
    }

    public static String getDoubleString(double num) {
        return String.format("%.2f", num);
    }

    public static double getDouble(String strNum) {
        if (strNum == null) {
            return 0;
        }
        try {
            return Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static int getInteger(String strNum) {
        if (strNum == null) {
            return 0;
        }
        try {
            return Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static double sumOfDouble(String num1, String num2) {
        return getDouble(num1) + getDouble(num2);
    }

    public static double sumOfDouble(String num1, String num2, String num3) {
        return getDouble(num1) + getDouble(num2) + getDouble(num3);
    }

    public static int sumOfInteger(String num1, String num2) {
        return getInteger(num1) + getInteger(num2);
    }

    public static int sumOfInteger(String num1, String num2, String num3) {
        return getInteger(num1) + getInteger(num2) + getInteger(num3);
    }

    public static double getDouble(String line, int index) {
        String[] parts = line.split(",");

        if (index < parts.length) {
            try {
                return Double.parseDouble(parts[index]);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
        return 0;
    }

    public static boolean isAmount(String value) {
        return amountPattern.matcher(value).matches();
    }

    public static boolean isNotAmount(String value) {
        return !isAmount(value);
    }

    public static boolean isQuantity(String value) {
        return quantityPattern.matcher(value).matches();
    }

    public static boolean isNotQuantity(String value) {
        return !isQuantity(value);
    }

    public static boolean isBatchNumber(String value) {
        return batchNumberPattern.matcher(value).matches();
    }

    public static boolean isNotBatchNumber(String value) {
        return !isBatchNumber(value);
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isNotValidNumber(String s) {
        return !isDouble(s);
    }

    public static boolean notInList(String s, List<String> list) {
        return !list.contains(s);
    }

    public static boolean isOutsideLimits(String s, int len) {
        return !(s != null && s.length() <= len);
    }

    public static boolean containsNonAlphabets(String value) {
        return !alphaPattern.matcher(value).matches();
    }
}
