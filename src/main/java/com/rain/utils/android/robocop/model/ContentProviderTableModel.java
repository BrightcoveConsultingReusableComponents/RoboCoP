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
@SuppressWarnings("unused")
public class ContentProviderTableModel {

    public static final String STRING = "string";
    public static final String DOUBLE = "double";
    public static final String INT = "int";
    public static final String BOOLEAN = "boolean";
    public static final String LONG = "long";
    public static final String DATE = "date";
    public static final String ARRAY = "array";
    public static final String THROWABLE = "throwable";
    public static final String[] FIELD_TYPES = {
            STRING, DOUBLE, INT, BOOLEAN,
            LONG, DATE, ARRAY, THROWABLE
    };

    @SerializedName("name")
    private String mName;

    @SerializedName("serialize_all_names")
    private String mSerializeAllNames;

    @SerializedName("constrain_unique_multi_columns")
    private String mConstrainUniqueColumns;

    @SerializedName("full_text_index_table")
    private String mCreateFullTextIndex;

    @SerializedName("full_text_index_module")
    private String mFullTextModule;

    @SerializedName("members")
    private List<ContentProviderTableFieldModel> mFields = new ArrayList<>();

    public ContentProviderTableModel(String tableName) {
        mName = tableName;
    }

    public List<ContentProviderTableFieldModel> getFields() {
        return mFields;
    }

    public String getName() {
        return mName;
    }

    public String getTableClassName() {
        return StringUtils.convertToTitleCase(mName);
    }

    public String getTableConstantName() {
        return StringUtils.getConstantString(mName);
    }

    public String getHasDateType() {
        return getHasType(DATE);
    }

    public String getHasArrayType() {
        return getHasType(ARRAY);
    }

    public String getSerializeAllNames() {
        // Set to TRUE by default
        if (mSerializeAllNames == null) {
            return Boolean.TRUE.toString();
        }

        if (mSerializeAllNames.equalsIgnoreCase(Boolean.TRUE.toString()))
            return Boolean.TRUE.toString();
        return null;
    }

    public String getConstrainUniqueColumns() {
        if (mConstrainUniqueColumns != null) return mConstrainUniqueColumns;
        return null;
    }

    public String getCreateFullTextIndex() {
        if (mCreateFullTextIndex != null) return mCreateFullTextIndex;
        return null;
    }

    public String getFullTextModule() {
        if (mFullTextModule != null) return mFullTextModule;
        return "fts3";  // Default to fts3
    }

    private String getHasType(String type) {
        for (ContentProviderTableFieldModel field : mFields) {
            if (field.mFieldType.toLowerCase().equals(type))
                return Boolean.TRUE.toString();
        }
        return null;
    }

    public String getHasSerializedNames() {
        // If the we've been told to serialize all names, then yes, we have serialized names
        if (Boolean.TRUE.toString().equals(getSerializeAllNames()))
            return Boolean.TRUE.toString();

        // Iterate through all declared fields and see if any are serialized.
        for (ContentProviderTableFieldModel field : mFields) {
            if (field.mSerializedName != null && field.mSerializedName.length() > 0)
                return Boolean.TRUE.toString();
        }
        return null;
    }

    public String getIsFtiAndHasConstraints() {
        // If Full Text is disabled, return null
        if (mFullTextModule == null) return null;

        // This table is full-text indexed.  Iterate through all fields and see if any have a UNIQUE constraint.
        for (ContentProviderTableFieldModel field : mFields) {
            if ("".equals(field.getUniqueConstraint())) {
                return Boolean.TRUE.toString();
            }
        }
        return null;
    }

    public List<String> getAllUniqueFieldNames() {
        // Returns a List of all fields that have the UNIQUE constraint

        List<String> fields = new ArrayList<>();

        for (ContentProviderTableFieldModel field: mFields) {
            if ("".equals(field.getUniqueConstraint())) {
                fields.add(field.getFieldName());
            }
        }

        return fields;
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

        @SerializedName("constraint_unique")
        private String mConstraintUnique;

        @SerializedName("constraint_not_null")
        private String mConstraintNotNull;

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
            switch (mFieldType) {
                case INT:
                case BOOLEAN:
                    return "INTEGER";
                case LONG:
                case DOUBLE:
                case DATE:
                    return "NUMERIC";
                default:
                    return "TEXT";
            }
        }

        public String getJavaTypeString() {
            String typeLower = mFieldType.toLowerCase();
            switch (typeLower) {
                case BOOLEAN:
                    return "boolean";
                case INT:
                    return "int";
                case LONG:
                case DOUBLE:
                    return "double";
                case ARRAY:
                    return "List<" + mArrayType + ">";
                case STRING:
                case DATE:
                    return "String";
                case THROWABLE:
                    return "Throwable";
                default:
                    // Assume type is a generated class
                    return mFieldType;
            }
        }

        public String getJavaTypeStringGetter() {
            switch (mFieldType) {
                case INT:
                case BOOLEAN:
                    return "getInt";
                case LONG:
                case DATE:
                    return "getLong";
                case DOUBLE:
                    return "getDouble";
                default:
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

        public String getIsThrowableType() {
            return mFieldType.toLowerCase().equals(THROWABLE) ? Boolean.TRUE.toString() : null;
        }

        public String getUniqueConstraint() {
            return Boolean.TRUE.toString().equalsIgnoreCase(mConstraintUnique) ? " UNIQUE" : "";
        }

        public String getNotNullConstraint() {
            return Boolean.TRUE.toString().equalsIgnoreCase(mConstraintNotNull) ? " NOT NULL" : "";
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

        public String getIsClass() {

            for (String type : FIELD_TYPES) {
                if (mFieldType.equalsIgnoreCase(type)) return null;
            }
            return Boolean.TRUE.toString();
        }

        public String getParcelReadMethod() {
            String typeLower = mFieldType.toLowerCase();
            switch (typeLower) {
                case BOOLEAN:
                    return "this." + getPrivateVariableName() + " = (parcel.readInt() == 1)";
                case INT:
                    return "this." + getPrivateVariableName() + " = parcel.readInt()";
                case LONG:
                case DOUBLE:
                    return "this." + getPrivateVariableName() + " = parcel.readDouble()";
                case ARRAY:
                    return "parcel.readList(" + getPrivateVariableName() + ", null)";
                case STRING:
                case DATE:
                    return "this." + getPrivateVariableName() + " = parcel.readString()";
                case THROWABLE:
                    return "this." + getPrivateVariableName() + " = (Throwable) parcel.readSerializable()";
                default:
                    // This is a generated class
                    return "this." + getPrivateVariableName() + " = parcel.readParcelable(" + mFieldType + ".class.getClassLoader())";
            }
        }

        public String getParcelWriteMethod() {
            String typeLower = mFieldType.toLowerCase();
            switch (typeLower) {
                case BOOLEAN:
                    return "dest.writeInt(" + getPrivateVariableName() + " ? 1 : 0)";
                case INT:
                    return "dest.writeInt(" + getPrivateVariableName() + ")";
                case LONG:
                case DOUBLE:
                    return "dest.writeDouble(" + getPrivateVariableName() + ")";
                case ARRAY:
                    return "dest.writeList(" + getPrivateVariableName() + ")";
                case STRING:
                case DATE:
                    return "dest.writeString(" + getPrivateVariableName() + ")";
                case THROWABLE:
                    return "dest.writeSerializable(" + getPrivateVariableName() + ")";
                default:
                    // This is a generated class
                    return "dest.writeParcelable(" + getPrivateVariableName() + ", PARCELABLE_WRITE_RETURN_VALUE)";
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ContentProviderTableModel && ((ContentProviderTableModel) o).getName().equals(getName());
    }
}
