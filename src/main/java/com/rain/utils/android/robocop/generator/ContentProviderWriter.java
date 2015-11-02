package com.rain.utils.android.robocop.generator;

import com.google.gson.Gson;
import com.rain.utils.android.robocop.model.ContentProviderModel;
import com.rain.utils.android.robocop.model.ContentProviderTableModel;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: dustin
 * Date: 1/15/14
 * Time: 9:44 AM
 */
public class ContentProviderWriter {

    public void createContentProvider(String schemaPath, String sourcePath) {
        System.out.println("Current Directory: " + System.getProperty("user.dir"));
        System.out.println("Schema Path: " + schemaPath);
        System.out.println("Source Path: " + sourcePath);

        Gson gson = new Gson();
        try {
            ContentProviderModel model = gson.fromJson(readFile(schemaPath), ContentProviderModel.class);

            model.inflateRelationships();
            createContentProvider(model, sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createContentProvider(ContentProviderModel contentProviderModel, String sourcePath) {
        // Clear out any generated source folders
        final String providerPath = getFilePath(sourcePath, contentProviderModel.getPackage(), "provider");
        removeDirectoryContents(providerPath);
        final String providerXMLPath = getFilePath(sourcePath, contentProviderModel.getPackage(), null);
        removeDirectoryContents(providerXMLPath);
        final String databasePath = getFilePath(sourcePath, contentProviderModel.getPackage(), "database");
        removeDirectoryContents(databasePath);
        final String tablePath = getFilePath(sourcePath, contentProviderModel.getPackage(), "database/table");
        removeDirectoryContents(tablePath);
        final String modelPath = getFilePath(sourcePath, contentProviderModel.getPackage(), "model");
        removeDirectoryContents(modelPath);

        Properties props = new Properties();
        URL url = this.getClass().getClassLoader().getResource("velocity.properties");
        if (url == null) {
            System.err.println("Unable to load velocity.properties file, quitting");
            return;
        }
        try {
            props.load(url.openStream());
            VelocityEngine engine = new VelocityEngine(props);
            engine.init();

            VelocityContext baseContext = new VelocityContext();
            baseContext.put("packageName", contentProviderModel.getPackage());
            baseContext.put("applicationId", contentProviderModel.getApplicationId());
            baseContext.put("providerModel", contentProviderModel);

            // Create Content Provider class, AndroidManifest XML definition, and DB class (as long as there are tables defined)
            if (contentProviderModel.getTables() != null && !contentProviderModel.getTables().isEmpty()) {
                VelocityContext providerContext = new VelocityContext(baseContext);
                providerContext.put("providerName", contentProviderModel.getProviderName());
                providerContext.put("tables", contentProviderModel.getTables());
                providerContext.put("relationships", contentProviderModel.getRelationships());
                writeFile(engine, providerContext, "ContentProvider.vm", providerPath, "/" + contentProviderModel.getProviderName() + "Provider.java");
                writeFile(engine, providerContext, "ProviderXML.vm", providerXMLPath, "/content-provider.xml");

                VelocityContext databaseContext = new VelocityContext(providerContext);
                databaseContext.put("databaseVersion", contentProviderModel.getDatabaseVersion());
                databaseContext.put("useSqliteAssetHelper", contentProviderModel.getUseSqliteAssetHelper());
                writeFile(engine, databaseContext, "Database.vm", databasePath, "/" + contentProviderModel.getProviderName() + "Database.java");
            }

            // Create all simple class models
            if (contentProviderModel.getClasses() != null && !contentProviderModel.getClasses().isEmpty()) {
                for (ContentProviderTableModel classModel : contentProviderModel.getClasses()) {
                    VelocityContext classContext = new VelocityContext(baseContext);
                    classContext.put("class", classModel);
                    classContext.put("isClass", Boolean.toString(true));
                    classContext.put("className", classModel.getName());
                    classContext.put("fields", classModel.getFields());
                    classContext.put("hasDateType", classModel.getHasDateType());
                    classContext.put("hasArrayType", classModel.getHasArrayType());
                    classContext.put("hasSerializedNames", classModel.getHasSerializedNames());
                    classContext.put("serializeAllNames", classModel.getSerializeAllNames());
                    writeFile(engine, classContext, "Model.vm", modelPath, "/" + classModel.getName() + ".java");
                }
            }

            // Create all tables and associated model objects
            if (contentProviderModel.getTables() != null && !contentProviderModel.getTables().isEmpty()) {
                for (ContentProviderTableModel table : contentProviderModel.getTables()) {
                    VelocityContext tableContext = new VelocityContext(baseContext);
                    tableContext.put("table", table);
                    tableContext.put("isTable", Boolean.toString(true));
                    tableContext.put("participatingRelationships", contentProviderModel.getRelationshipsForTable(table));
                    tableContext.put("className", table.getTableClassName());
                    tableContext.put("fields", table.getFields());
                    tableContext.put("hasDateType", table.getHasDateType());
                    tableContext.put("hasSerializedNames", table.getHasSerializedNames());
                    tableContext.put("serializeAllNames", table.getSerializeAllNames());
                    tableContext.put("constrainUniqueCols", table.getConstrainUniqueColumns());
                    tableContext.put("createFullTextIndex", table.getCreateFullTextIndex());
                    tableContext.put("fullTextModule", table.getFullTextModule());
                    tableContext.put("hasArrayType", table.getHasArrayType());
                    writeFile(engine, tableContext, "Table.vm", tablePath, "/" + table.getTableClassName() + "Table.java");
                    writeFile(engine, tableContext, "Model.vm", modelPath, "/" + table.getTableClassName() + ".java");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    private void removeDirectoryContents(String rootFolderPath) {
        File directory = new File(rootFolderPath);
        removeFilesAndFoldersBelow(directory);
    }

    private void removeFilesAndFoldersBelow(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        removeFilesAndFoldersBelow(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
    }

    private String getFilePath(String sourcePath, String packageName, String lastFolderName) {
        return sourcePath + packageName.replace(".","/") + (lastFolderName != null ?"/" + lastFolderName : "");
    }

    private void writeFile(VelocityEngine engine, VelocityContext context, String templateName, String outputFilePath, String outputFileName) {
        File temp = new File(outputFilePath);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        Template providerTemplate = engine.getTemplate(templateName);

        FileWriter w = null;
        try {
            w = new FileWriter(outputFilePath + outputFileName);
            providerTemplate.merge(context, w);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
