'''
Created on 26.06.2011

@author: Florian Mueller
'''
from algorithms import makeRootSet, makeBaseSet, makeSubGraph, calcPageRank,\
    calcHubAndAuthorities

def doQuery(query, pages):
    rootSet = makeRootSet(query, pages)
    baseSet = makeBaseSet(pages, rootSet)
    subGraph = makeSubGraph(rootSet, baseSet)
    
    pageRank = calcPageRank(pages, 10000, 0.85)
    hubAndAuthorities = calcHubAndAuthorities(subGraph, 10000)
    
    print "RootSet"
    for p in rootSet.values():
        print p.getId(), p.getLink()," out: ", p.getOutgoingLinks(), " in: ", p.getIncomingLinks()
    
    print "\nSubgraph"
    for p in subGraph.values():
        print p.getId(), p.getLink()," out: ", p.getOutgoingLinks(), " in: ", p.getIncomingLinks(), \
            " PR: ", pageRank[p.getId()], " hub: ", hubAndAuthorities[0][p.getId()], " auth: ", hubAndAuthorities[1][p.getId()]
    
    
    
    pageRankResult = []
    histResult = []
    
    for p in rootSet.values():
        id = p.getId()
        pageRankResult.append((id, pageRank[id]))
        histResult.append((id, hubAndAuthorities[0][id],hubAndAuthorities[1][id]))
        
    pageRankResult.sort(lambda x,y: cmp(y[1],x[1]))
    histResult.sort(lambda x,y: cmp(y[1]+y[2],x[1]+x[2]))
    
    return (pageRankResult, histResult)