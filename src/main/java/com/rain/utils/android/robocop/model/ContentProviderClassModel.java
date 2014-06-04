package com.rain.utils.android.robocop.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContentProviderClassModel {

    public static final String STRING = "string";
    public static final String DOUBLE = "double";
    public static final String INT = "int";
    public static final String BOOLEAN = "boolean";
    public static final String LONG = "long";
    public static final String DATE = "date";
    public static final String ARRAY = "array";

    @SerializedName("name")
    private String mClassName;

    @SerializedName("members")
    private List<ContentProviderClassFieldModel> mFields;

    public String getClassName() {
        return mClassName;
    }

    public List<ContentProviderClassFieldModel> getFields() {
        return mFields;
    }

    public String getHasDateType() {
        return getHasType(DATE);
    }

    public String getHasArrayType() {
        return getHasType(ARRAY);
    }

    private String getHasType(String type) {
        for (ContentProviderClassFieldModel field : mFields) {
            if (field.mFieldType.equalsIgnoreCase(type))
                return Boolean.TRUE.toString();
        }
        return null;
    }

    public String getHasSerializedNames() {
        for (ContentProviderClassFieldModel field : mFields) {
            if (field.mSerializedName != null && field.mSerializedName.length() > 0)
                return Boolean.TRUE.toString();
        }
        return null;
    }

    public static class ContentProviderClassFieldModel {

        @SerializedName("type")
        private String mFieldType;

        @SerializedName("name")
        private String mFieldName;

        @SerializedName("format")
        private String mFieldFormat;

        @SerializedName("serialized_name")
        private String mSerializedName;

        @SerializedName("array_type")
        private String mArrayType;

        public String getFieldType() {
            return mFieldType;
        }

        public String getFieldArrayType() {
            return mArrayType;
        }

        public String getFieldName() {
            return mFieldName;
        }

        public String getConstantString() {
            return StringUtils.getConstantString(mFieldName);
        }

        public String getJavaTypeString() {
            String typeLower = mFieldType.toLowerCase();
            if (typeLower.equals(BOOLEAN)) {
                return "boolean";
            } else if (typeLower.equals(INT)) {
                return "int";
            } else if (typeLower.equals(LONG) || typeLower.equals(DOUBLE)) {
                return "double";
            } else if (typeLower.equals(ARRAY)) {
                return "List<" + mArrayType + ">";
            } else if (typeLower.equals(STRING) || typeLower.equals(DATE)) {
                return "String";
            } else {
                // Assume type is a generated class
                return mFieldType;
            }
        }

        public String getJavaTypeStringGetter() {
            if (mFieldType.equals(INT) || mFieldType.equals(BOOLEAN)) {
                return "getInt";
            } else if (mFieldType.equals(LONG)) {
                return "getLong";
            } else if(mFieldType.equals(DOUBLE)) {
                return "getDouble";
            } else {
                return "getString";
            }
        }

        public String getPrivateVariableName() {
            return StringUtils.getPrivateVariableName(mFieldName);
        }

        public String getNameAsTitleCase() {
            return StringUtils.convertToTitleCase(mFieldName);
        }

        public String getIsDateType() {
            return mFieldType.toLowerCase().equals(DATE) ? Boolean.TRUE.toString() : null;
        }

        public String getIsArrayType() {
            return mFieldType.toLowerCase().equals(ARRAY) ? Boolean.TRUE.toString() : null;
        }

        public String getStaticTimeFormatName() {
            return mFieldName.toUpperCase() + "_TIME_FORMAT";
        }

        public String getTimeFormat() {
            return mFieldFormat;
        }

        public String getSerializedName() {
            return mSerializedName;
        }
    }
}
