package com.yahoo.jonaswu.cucumberparallel

import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by jonaswu on 2015/3/10.
 */
class ReportAggregator {

    public static int aggregator(args) {
        return cucumberJsonAggregator(args)
    }

    public static int cucumberJsonAggregator(args) {
        CucumberArgumentMapper cucumberArgumentMapper = new CucumberArgumentMapper();
        def jsonFilePath = cucumberArgumentMapper.retriveJSONReportPath(args)
        def record = [:]
        def returnVal = 1
        if (jsonFilePath.size() > 0) {
            "rm $jsonFilePath".execute()
            def jsonSlurper = new JsonSlurper()
            println jsonFilePath
            def reportDirectory = new File(jsonFilePath).getParentFile()
            reportDirectory = reportDirectory ? reportDirectory : new File("./")
            reportDirectory.traverse(type: FileType.FILES, nameFilter: ~/.*\.json/, maxDepth: 0) {
                def f = it.getAbsoluteFile()
                def filePath = it.getAbsolutePath()
                try {
                    JSONObject jsonObj = jsonSlurper.parseText(f.getText("UTF-8"))[0]
                    def id = jsonObj.getString("id")
                    if (record.containsKey(id)) {
                        JSONObject ori = new JSONObject(record[id].toString())
                        JSONArray oriToMerge = ori.getJSONArray("elements")
                        JSONArray objToMerge = new JSONObject(jsonObj.toString()).getJSONArray("elements")
                        ori.put("elements", concatArray(oriToMerge, objToMerge))
                        record[id] = ori
                    } else {
                        record[id] = jsonObj
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                "rm $filePath".execute()

            }

            JSONArray res = new JSONArray();

            record.each { k, v ->
                res.put(new JSONObject(v.toString()))
            }

            if (!res.toString().contains('"status":"failed"'))
                returnVal = 0

            def outputFile = new File(jsonFilePath)
            outputFile.setText(res.toString(), "UTF-8")

        }
        return returnVal;
    }

    private static JSONArray concatArray(JSONArray arr1, JSONArray arr2) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }
}
