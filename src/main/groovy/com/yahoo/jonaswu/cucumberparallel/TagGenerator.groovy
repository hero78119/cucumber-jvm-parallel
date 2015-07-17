package com.yahoo.jonaswu.cucumberparallel

import cucumber.runtime.model.CucumberFeature
import cucumber.runtime.model.CucumberTagStatement
import gherkin.formatter.model.Tag

/**
 * Created by jonaswu on 2015/3/9.
 */
public class TagGenerator {
    public traverseHead = []
    private currentNode = null
    public rootNode = null

    public TagGenerator() {
    }

    //def tags = rawString.findAll(/(@[^\s\n]+)/)
    //      def tmp = it =~ /\@par_([\d\w\_]+)/
    public debug() {
        traverseHead.each { Node node ->
            println node.rawTag
        }
    }

    public parseFeature(TagGenerator tagGenerator, CucumberFeature cucumberFeature) {
        currentNode = tagGenerator.rootNode
        NodesFactory.resetForNewIteration()
        List<CucumberTagStatement> cucumberTagStatements = cucumberFeature.getFeatureElements()
        for (CucumberTagStatement cucumberTagStatement : cucumberTagStatements) {
            def rawTagLists = []
            def rawTagString = ""
            //cucumberTagStatement.run(formatter, reporter, runtime)
            cucumberTagStatement.getGherkinModel().tags.each { Tag tag ->
                rawTagString += tag.getName() + " "
                rawTagLists << tag.getName()
            }
            def tags = rawTagString.findAll(/(@[^\s\n]+)/)
            if (!tags) return
            def targetTag = null

            tags.each { tag ->
                def tmp = tag =~ /\@par_([\d\w\_]+)/
                if (tmp)
                    targetTag = tmp[0][1]
            }
            addNodeToGraph(targetTag, rawTagString, rawTagLists)
        }
    }

    private addNodeToGraph(targetTag, rawTagString, rawTagLists) {
        println 'addNodeToGraph'
        //        else if (targetTag == null && currentTag == null) {
//            //be the first one
//            currentTag = targetTag
//            def newNode = NodesFactory.creteNode(rawTagString, targetTag)
//            traverseHead << newNode
//            return
//        }
        if (targetTag == null)
            currentNode = NodesFactory.appendNext(currentNode, targetTag, rawTagString, rawTagLists)
        else if (targetTag && NodesFactory.tagExists(targetTag)) {
            currentNode = NodesFactory.appendToTagSets(targetTag, rawTagString, rawTagLists)
        } else {
            currentNode = NodesFactory.appendNext(currentNode, targetTag, rawTagString, rawTagLists)
        }
    }

}