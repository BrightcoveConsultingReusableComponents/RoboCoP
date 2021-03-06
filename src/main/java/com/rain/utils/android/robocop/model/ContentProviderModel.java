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
public class ContentProviderModel {

    @SerializedName("packageName")
    private String mPackage;

    @SerializedName("providerName")
    private String mProviderName;

    @SerializedName("databaseVersion")
    private int mDatabaseVersion;

    @SerializedName("use_sqlite_asset_helper")
    private String mUseSqliteAssetHelper;

    @SerializedName("application_id")
    private String mApplicationId;

    @SerializedName("tables")
    private List<ContentProviderTableModel> mTables;

    @SerializedName("classes")
    private List<ContentProviderTableModel> mModelClasses;

    @SerializedName("relationships")
    private List<ContentProviderRelationshipModel> mRelationships;

    @SerializedName("use_bc_logger")
    private String mUseBcLogger;

    public ContentProviderModel(String packageName, String providerName, int databaseVersion, List<ContentProviderTableModel> tables, List<ContentProviderRelationshipModel> relationships) {
        mPackage = packageName;
        mProviderName = providerName;
        mDatabaseVersion = databaseVersion;
        mTables = tables;
        mRelationships = relationships;
    }

    public String getProviderName() {

        return StringUtils.convertToTitleCase(mProviderName);
    }

    public List<ContentProviderTableModel> getTables() {
        return mTables;
    }

    public List<ContentProviderTableModel> getClasses() {
        return mModelClasses;
    }

    public String getPackage() {
        return mPackage;
    }

    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public String getApplicationId() {
        return mApplicationId;
    }

    public String getUseBcLogger() {
        return mUseBcLogger;
    }

    public String getUseSqliteAssetHelper() {
        if (mUseSqliteAssetHelper != null && mUseSqliteAssetHelper.equalsIgnoreCase(Boolean.TRUE.toString()))
            return Boolean.TRUE.toString();
        return null;
    }

    public List<ContentProviderRelationshipModel> getRelationships() {
        return mRelationships;
    }

    public List<ContentProviderRelationshipModel> getRelationshipsForTable(ContentProviderTableModel tableModel) {
        if (tableModel == null || mRelationships == null) return null;
        List<ContentProviderRelationshipModel> includedRelationships = new ArrayList<ContentProviderRelationshipModel>();
        for (ContentProviderRelationshipModel relationship : mRelationships) {
            if (relationship.getLeftTableModel() == tableModel || relationship.getRightTableModel() == tableModel) {
                includedRelationships.add(relationship);
            }
        }
        return includedRelationships;
    }

    public void inflateRelationships() {
        if (mRelationships != null) {
            for (ContentProviderRelationshipModel relationship : mRelationships) {
                String leftTableName = relationship.getLeftTableName();
                String rightTableName = relationship.getRightTableName();
                if (leftTableName == null || leftTableName.length() == 0 || rightTableName == null || rightTableName.length() == 0) {
                    //invalid relationship config, bail
                    System.out.println("invalid relationship config!!! one of the table names is missin or is blank");
                    return;
                }
                ContentProviderTableModel leftTable = null;
                ContentProviderTableModel rightTable = null;
                for (ContentProviderTableModel table : mTables) {
                    if (leftTable != null && rightTable != null) {
                        break;
                    }
                    if (table.getName().equals(leftTableName)) {
                        leftTable = table;
                    }
                    if (table.getName().equals(rightTableName)) {
                        rightTable = table;
                    }
                }
                if (leftTable == null || rightTable == null) {
                    // the referenced tables could not be found
                    System.out.println("one or both of the referenced tables in a relationship could not be found in the table definition. please check your spelling");
                    return;
                }
                relationship.setLeftTableModel(leftTable);
                relationship.setRightTableModel(rightTable);
            }
        }
    }
}
