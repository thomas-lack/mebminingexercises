'''
Created on 24.06.2011

@author: Florian Mueller
'''
import math
import re

def calcPageRank(pages, iter, d):
    print "calcPageRank"
    N = len(pages.keys())
    pr = {}
    
    for p in pages.values():
        pr[p.getId()] = 0
    
    for i in xrange(0,iter):
        change = 0.0
        for p in pages.values():
            tmp = 0.0
            for incoming in set(p.getIncomingLinks()):
                q = pages[incoming]
                tmp += pr[q.getId()] / float(len(set(q.getOutgoingLinks())))
            old = pr[p.getId()]
            new = (1.0 - d) * (1.0 / N) + d * tmp
            change += math.fabs(new - old)
            pr[p.getId()] = new
        if (change / N) < 0.0001:
            break
        
    print "i = ", i
    return pr

def calcHubAndAuthorities(pages, iter):
    print "calcHubAndAuthorities"
    N = len(pages.keys())
    hubs = {}
    authoritys = {}
    
    for p in pages.values():
        hubs[p.getId()] = 1.0
        authoritys[p.getId()] = 1.0
    
    for i in xrange(0,iter):
        norm = 0.0
        change = 0.0
        for p in pages.values():
            for id in set(p.getIncomingLinks()):
                q = pages[id]
                authoritys[p.getId()] += hubs[q.getId()] 
            norm += (authoritys[p.getId()] * authoritys[p.getId()])
        norm = math.sqrt(norm)
        
        for p in pages.values():
            old = authoritys[p.getId()]
            new = old / norm
            change += math.fabs(new - old)
            authoritys[p.getId()] = new
        norm = 0.0
        
        for p in pages.values():
            for id in set(p.getOutgoingLinks()):
                q = pages[id]
                hubs[p.getId()] += authoritys[q.getId()]
            norm += (hubs[p.getId()] * hubs[p.getId()])
        norm = math.sqrt(norm)
        
        for p in pages.values():
            old = hubs[p.getId()]
            new = old / norm
            change += math.fabs(new - old)
            hubs[p.getId()] = new
        
        if (change / (2*N)) < 0.0001:
            break
            
    print "i = ", i
    return (hubs, authoritys)

def readWordsFromString(data):
    data = re.sub("[-_!?.,;:'#+*~\"$%&/()=\}\]\[{}\'\`]", "", data)
    data = re.sub("[\s]+", " ", data)
    return map(lambda x: x.lower(),filter(lambda x: not 0 == len(x), data.split(" ")))

def makeRootSet(query, pages):
    keys = query.split(" ");
    result = {}
    
    for p in pages.values():
        data = str(p.getData())
        hits = 0
        words = readWordsFromString(data)
        
        for key in keys:
            if key.lower() in words:
                hits += 1
                
        if len(keys) == hits:
            result[p.getId()] = p
            
    return result
                 
def makeBaseSet(pages, rootset):
    result = {}
    for p in rootset.values():
        for q in p.getIncomingLinks():
            result[q] = pages[q]
        for r in p.getOutgoingLinks():
            result[r] = pages[r]
            
    return result
            
def makeSubGraph(rootSet, baseSet):
    graph = dict(rootSet.items() + baseSet.items())
    keys = graph.keys()
    for p in graph.values():
        tmp = []
        for id in p.getOutgoingLinks():
            if id in keys:
                tmp.append(id)
        p.setOutgoingLinks(sorted(set(tmp)))
        
        tmp = []
        for id in p.getIncomingLinks():
            if id in keys:
                tmp.append(id)
        p.setIncomingLinks(sorted(set(tmp)))
                
    return graph
    
        