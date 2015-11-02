RoboCoP
=======

RoboCoP is a Java library that can generate a fully-functional ContentProvider from a simple JSON schema file. It can also generate POJOs which can easily be used by GSON. The generated POJOs also implement the [Parcelable](https://developer.android.com/reference/android/os/Parcelable.html) interface, meaning POJOs can be easily passed around in [Bundles](https://developer.android.com/reference/android/os/Bundle.html).


Application Setup
========
This library requires more work than the typical dependency addition in your app's ```build.gradle``` file. This is because the library runs at compile-time to generate classes. These set-up steps assuming you're configuring an app on Brightcove's network, and will therefore pull RoboCoP from our internal [Artifactory](https://www.jfrog.com/artifactory/) server.

Modify Your app's build.gradle file
--------
At the top of the app's ```build.gradle``` file, modify the buildscript section to look similar to the following:
```groovy
buildscript {
    repositories {
        jcenter()
        maven { url 'http://bamboo.vidmark.local:8081/artifactory/libs-release-local' }
    }
    dependencies {
        classpath 'org.apache.velocity:velocity:1.7'
        classpath 'com.google.code.gson:gson:2.3.1'
        classpath 'com.rain.util.android:RoboCoP:0.5.18'
    }
}
```
The RoboCoP library has dependencies on Apache Velocity and GSON. Ideally the RoboCoP library would pull in those dependencies, but it currently does not do so.

The next change needed to your ```build.gradle``` file is to add tasks to run.  The first task specifies inputs and outputs, runs the ContentProvider generator to build the code.
```groovy
import com.rain.utils.android.robocop.generator.ContentProviderGenerator;

task contentProviderGen(dependsOn: 'contentProviderClean') {
    description = 'Generate ContentProvider and required classes'
    doFirst {
        println "Generating ContentProvider..."
        String schemaFilename = "${rootProject.projectDir}/app/robocop/schema.json"
        String baseOutputDir = "${rootProject.projectDir}/app/build/generated/robocop/"
        ContentProviderGenerator.generateContentProvider(schemaFilename, baseOutputDir)
    }
}
```
Based on the location of your ```schema.json``` file, and the name of your app's subdirectory, you may need to change the lines that set ```schemaFilename``` and ```baseOutputDir```. ```schemaFilename``` specifies the location of the schema file RoboCoP reads from.  ```baseOutputDir``` specifies the location of the generated source code.


Next, a task needs to be added that cleans previously generated source:
```groovy
task contentProviderClean(type: Delete) {
    println "Deleting existing generated ContentProvider"
    delete "build/generated/robocop"
}
```

We also need to make sure the task that generates the content provider runs before a build. To do that, we specify the following:
```groovy
// Be sure to run our content provider generator before a build
tasks.whenTaskAdded { task ->
    if (task.name == 'preDebugBuild' || task.name == 'preReleaseBuild'
            || task.name == 'preProductionDebugBuild' || task.name == 'preProductionReleaseBuild'
            || task.name == 'preQaDebugBuild' || task.name == 'preQaReleaseBuild'
            || task.name == 'preDevDebugBuild' || task.name == 'preDevReleaseBuild') {
        task.dependsOn contentProviderGen
    }
}
```
Note that the if block will need to change, based on the build flavors you specify for your app.  In the above example, the app has three flavors (production, qa, and dev) and two build types (release and debug).

Lastly, we need to tell the Android application about this new source location.  In the ```android``` block, add the following:
```groovy
android {
    other stuff here...
    
    sourceSets {
        main {
            java.srcDir 'build/generated/robocop'
        }
    }
```

Create Your JSON Schema Definition
----------
Create a JSON schema definition and place it in the directory specified in the ```contentProviderGen``` task. Sample schema files can be found [here](https://github.com/BrightcoveConsultingReusableComponents/RoboCoP/tree/master/ClientTest/resources).

### Schema File Structure

```json
{
  "packageName" : "<the package name you want for your ContentProvider and associated classes/>",
  "providerName" : "<the base name for your provider. eg. 'Example' will yield 'ExampleProvider.java'/>",
  "databaseVersion" : "<the numeric value for the current version of your database. if you increment this, the database will upgrade/>",
  "classes": [], 
  "tables" : [],
  "relationships" : [] 
}
```
The ```classes``` array defines POJOs.  Any classes declared in here will not have a content provider created. The same rules mentioned in the table definition apply.

The ```tables``` array defines tables to generate.  See table definition below.

The ```relationships``` array defines table relationships.  See relationship definition section below.


#### Table Definition Structure

```json
{
  "name" : "<name of the table/>",
  "serialize_all_names": "either 'true' or 'false'.  If true will add an @SerializedName annotation with the field's name for every field in the table",
  "constrain_unique_multi_columns": "If set, will add a constraint on the table based on the fields specified.  Field names should be comma-seperated.",
  "full_text_index_table": "If true, will full-text index the table",
  "full_text_index_module": "The module to use when full-text indexing the table.  If full_text_index_table is true, and this field is not specified, it will default to fts3.",
  "members" : []
}
```

##### Table Field Definition Structure

```json
{
  "type" : "<the ~java data type for this field. Your options are: string, double, int, boolean, long, date, array (lower case). These will map to SQLite types.  In the case of date, it will be stored as a string in SQLite. In the case of array, the values will be stored as a JSON string. />",
  "name" : "<the name of the field (lower case, underscore separated)/>",
  "array_type": "If type is array, this is the type of the array.  For example, string, date, etc.",
  "format" : "<Only applicable for 'date' type.  Should be a format that java.text.SimpleDateFormat understands.  Will be used to convert date from a string to an actual java.util.Date object./>",
  "serialized_name" : "If specified, will add an @SerializedName notation for this member using the value specified.  Useful if using these model objects with GSON or something like Retrofit.",
  "constraint_not_null": "If true, will add a constraint that the field must not be null.",
  "constraint_unique": "If true, will add a constraint that the field must be unique"
}
```

#### Relationship Definition Structure

```json
{
  "name" : "<the name of this relationship (lower case, underscore separated)/>",
  "left_table" : "<the left side of the join (in a one-to-many this is the 'one' side)/>",
  "right_table" : "<the right side of the join (the 'many' side)/>",
  "type" : "<the type of relationship. Your current option is 'to_many'/>"
}
```

Remember that for the code generation to generate nice looking code, you need to write all of your schema values in lower case and underscore-separated.


Installing The Provider - Important!!!
---------
In order to use a ContentProvider in your Android app you must install it into your application's AndroidManifest file. The generator creates a special file in the root of your generated code called 'content-provider.xml' to assist with this step. Copy the contents of this file into your AndroidManifest file inside the <Application/> node. The generated code has the provider's exported property set to false. If this is true, then other applications may be able to access your data. Only set this to true if you know what you are doing.


Building and making changes to this library
---------
The following are some tips if you are going to make changes to this library (specific to this fork):
* The library is built with gradle.  On the command line, run ```./gradlew clean build```
* If deploying the library to our Artifactory server, make sure you have the [BuildTools](https://github.com/BrightcoveConsultingReusableComponents/BCC-Android-BuildTools) configured in your local environment, and run the ```artifactoryPublish``` gradle task. BuildTools will set the artifactory URL, user and password required to upload the new version.
* The build version number is updated in [build.gradle](https://github.com/BrightcoveConsultingReusableComponents/RoboCoP/blob/master/build.gradle) file, by updating the ```version``` number.
* IntelliJ Idea is used to develop this library.  When running the Main test to generate sources, you must first build the library on the command line.  I was unable to figure out how to get IntelliJ Idea to get the ClientTest module to have a dependency on the library and build it auto-magically.

License
-------

    Copyright 2014 Crossborders, LLC. Rain (DBA)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



