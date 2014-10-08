package com.rain.utils.android.robocop.clienttest;

import com.rain.utils.android.robocop.generator.ContentProviderWriter;

/**
 * Created with IntelliJ IDEA.
 * User: dustin
 * Date: 1/15/14
 * Time: 9:43 AM
 */
public class Main {
    public static void main(String[] args) {
        ContentProviderWriter writer = new ContentProviderWriter();
        writer.createContentProvider("ClientTest/resources/example_schema.json", "ClientTest/src-gen/");
    }
}
