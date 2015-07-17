package com.yahoo.jonaswu.cucumberparallel

/**
 * Created by jonaswu on 2015/3/9.
 */
public class NodesFactory {
    private static idList = []
    public static tags = [:]
    private static nodeCount = 0

    public static class Node {
        private rawTag
        private targetTag
        private parents
        private id
        private childs
        private rawTagLists

        public Node(nodeId, rawTag, targetTag, rawTagLists) {
            this.rawTagLists = rawTagLists
            this.rawTag = rawTag
            this.targetTag = targetTag
            parents = []
            childs = []
            id = nodeId
        }

        public getTargetTag() {
            return targetTag
        }

        public getChildsId() {
            return childs
        }

        public getParentsId() {
            return parents
        }


        public addParent(parentId) {
            parents << parentId
        }

        public getId() {
            return id;
        }

        public getRawTag() {
            return this.rawTag
        }

        public getRawTagList() {
            return this.rawTagLists
        }

        public dependOn(Node childNode) {
            def childId = childNode.getId()
            childs << childId
            childNode.addParent(id)
        }

        public removeChild(Node childNode) {
            def childId = childNode.getId()
            childs = childs - childId
        }

        public isNoChild() {
            return getChildsId().size() == 0
        }

    }

    public static creteNode(rawTagString, targetTag, rawTagLists) {
        def newNode = new Node(nodeCount, rawTagString, targetTag, rawTagLists)
        idList << newNode
        nodeCount += 1
        return newNode
    }

    public static resetForNewIteration() {
        //idList = []
        tags = [:]
    }

    public static int getNodeCount() {
        return nodeCount
    }

    public static getSiblingNode(Node node) {
        println 'getSiblingNode'
        def tag = node.getTargetTag()
        print 'tag: ' + tag
        if (tag == null)
            return [node] // no sibling
        def res = getNodesFromTag(tag)
        println 'res size:' + res.size()
        if (!res)
            return [node] // no sibling
        return res
    }

    public static getNodesFromTag(tag, count = null) {
        if (tag == null)
            return []
        createTagIfNotExist(tag)
        if (count == null || count > this.tags[tag].size())
            count = this.tags[tag].size() //get all

        def res = []
        for (int i = 0; i < count; i++) {
            res << this.tags[tag][i]
        }
        return res
    }

    public static getChildsNode(Node node, count = null) {
        println 'getChildsNode'
        def List childIds = node.getChildsId()
        println 'childIds: ' + childIds
        if (count == null || count > childIds.size())
            count = childIds.size() //get all
        def res = []
        for (int i = 0; i < count; i++) {
            def childId = childIds[i]
            res << idList[childId]
        }
        return res
    }

    public static getParentsNode(Node node, count = null) {
        def List parentIds = node.getParentsId()
        if (count == null || count > parentIds.size())
            count = parentIds.size() //get all
        def res = []
        for (int i = 0; i < count; i++) {
            def parentId = parentIds[i]
            res << idList[parentId]
        }
        return res
    }

    public static register(node, String tag) { //tag -> nodes is one to many mapping
        if (tag == null)
            return null
        createTagIfNotExist(tag)
        this.tags[tag] << node
    }

    private static createTagIfNotExist(tag) {
        if (!this.tags[tag])
            this.tags[tag] = []
    }

    public static boolean tagExists(tag) {
        if (this.tags[tag] && this.tags[tag].size() > 0)
            return true
        return false
    }

    public static debug() {
        tags.each { tag, nodes ->
            print tag
            nodes.each { Node node ->
                println node.getRawTag()
            }

        }
    }

    public static synchronized nofifyVisitToParent(Node child) {
        List<Node> parentNodes = getParentsNode(child)
        def nextVisit = []
        parentNodes.each { Node parentNode ->
            parentNode.removeChild(child)
            if (parentNode.isNoChild())
                nextVisit << parentNode

        }
        return nextVisit
    }

    public static appendNext(currentNode, targetTag, rawTagString, rawTagLists) {
        println 'appendNext'
        println 'targetTag: ' + targetTag
        def newNode = creteNode(rawTagString, targetTag, rawTagLists)
        def nodes = getSiblingNode(currentNode)
        println 'begin to print SiblingNode'
        nodes.each { Node node ->
            println node.getRawTag()
            newNode.dependOn(node)
        }
        println 'end of  to print SiblingNode'
        register(newNode, targetTag)
        println 'tag node Size:' + NodesFactory.getNodesFromTag(targetTag).size()
        return newNode
    }

    public static appendToTagSets(targetTag, rawTagString, rawTagLists) {
        println 'appendToTagSets'
        println 'targetTag: ' + targetTag
        def newNode = creteNode(rawTagString, targetTag, rawTagLists)
        def siblingNode = getNodesFromTag(targetTag, 1)
        if (siblingNode) {
            siblingNode = siblingNode[0]
            def List<Node> childNodes = getChildsNode(siblingNode)
            childNodes.each { childNode ->
                newNode.dependOn(childNode)
            }
        }
        register(newNode, targetTag)
        println 'tag node Size:' + NodesFactory.getNodesFromTag(targetTag).size()
        return newNode
    }
}