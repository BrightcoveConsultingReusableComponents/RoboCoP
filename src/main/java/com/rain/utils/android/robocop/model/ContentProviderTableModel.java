package com.rain.utils.android.robocop.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dustin
 * Date: 1/15/14
 * Time: 9:46 AM
 */
public class ContentProviderTableModel {

    public static final String STRING = "string";
    public static final String DOUBLE = "double";
    public static final String INT = "int";
    public static final String BOOLEAN = "boolean";
    public static final String LONG = "long";
    public static final String DATE = "date";
    public static final String ARRAY = "array";

    @SerializedName("name")
    private String mTableName;

    @SerializedName("members")
    private List<ContentProviderTableFieldModel> mFields = new ArrayList<ContentProviderTableFieldModel>();

    public ContentProviderTableModel(String tableName) {
        mTableName = tableName;
    }

    public List<ContentProviderTableFieldModel> getFields() {
        return mFields;
    }

    public String getTableName() {
        return mTableName;
    }

    public String getTableClassName() {
        return StringUtils.convertToTitleCase(mTableName);
    }

    public String getTableConstantName() {
        return StringUtils.getConstantString(mTableName);
    }

    public String getHasDateType() {
        return getHasType(DATE);
    }

    public String getHasArrayType() {
        return getHasType(ARRAY);
    }

    private String getHasType(String type) {
        for (ContentProviderTableFieldModel field : mFields) {
            if (field.mFieldType.toLowerCase().equals(type))
                return Boolean.TRUE.toString();
        }
        return null;
    }

    public String getHasSerializedNames() {
        for (ContentProviderTableFieldModel field : mFields) {
            if (field.mSerializedName != null && field.mSerializedName.length() > 0)
                return Boolean.TRUE.toString();
        }
        return null;
    }

    public static class ContentProviderTableFieldModel {

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

        public String getTypeString() {
            if (mFieldType.equals(INT) || mFieldType.equals(BOOLEAN)) {
                return "INTEGER";
            } else if (mFieldType.equals(LONG) || mFieldType.equals(DOUBLE)) {
                return "NUMERIC";
            } else {
                return "TEXT";
            }
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
            } else {
                return "String";
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

        public String getBooleanComparison() {
            if (mFieldType.equals(BOOLEAN)) {
                return " == 1";
            }
            return "";
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

    @Override
    public boolean equals(Object o) {
        return ((ContentProviderTableModel)o).getTableName().equals(getTableName());
    }
}
