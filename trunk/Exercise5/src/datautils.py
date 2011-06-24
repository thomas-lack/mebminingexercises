'''
Created on 22.06.2011

@author: Florian Mueller
'''
from node import Node

def readFile(path):
    fd = open(path)
    data = fd.read()
    fd.close()
    return data

def readNodeFile(path):
    fd = open(path)
    nodeList = {}
    for line in fd:
        if 0 < len(line):
            split = line.split("->")
            id = int(split[0].strip())
            link = split[1].strip()
            node = Node(id, link, readFile("data/%d.txt" % (id)))
            nodeList[node.getId()] = node
    fd.close()
    return nodeList

def readGraphFile(path, nodes):
    fd = open(path)
    for line in fd:
        if 0 < len(line):
            split = line.split(" -> ")
            if 2 == len(split):
                outNode = int(split[0].strip())
                inNode = int(split[1].strip())
                nodes[outNode].addOutgoingLink(inNode)
                nodes[inNode].addIncomingLink(outNode)
    fd.close()
    return nodes

def readGraph(dir):
    nodeList = readNodeFile("%s/%s" % (dir, "node.dict"))
    return readGraphFile("%s/%s" % (dir, "graph.dot"), nodeList)
