package com.yahoo.jonaswu.cucumberparallel

import cucumber.runtime.RuntimeOptions
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.io.ResourceLoader
import cucumber.runtime.model.CucumberFeature

/**
 * Created by jonaswu on 2015/3/7.
 */

public class Main {
    public static void main(String[] args) {
        def argsList = Config.parseAndFilter(args)
        def tagGenerator = new TagGenerator()
        def classLoader = Thread.currentThread().getContextClassLoader()
        RuntimeOptions runtimeOptions = new RuntimeOptions(argsList)
        ResourceLoader resourceLoader = new MultiLoader(classLoader)
        List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader)
        int res = 1
        if (cucumberFeatures.size() > 0) {
            tagGenerator.rootNode = NodesFactory.creteNode(null, null, [])
            for (CucumberFeature cucumberFeature : cucumberFeatures) {
                tagGenerator.parseFeature(tagGenerator, cucumberFeature)
            }
            tagGenerator.debug()
            Traversor.pureTraverse(tagGenerator.rootNode, argsList)
            res = ReportAggregator.aggregator(argsList)
        }
        System.exit(res)
    }
}
