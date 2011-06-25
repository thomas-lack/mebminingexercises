'''
Created on 24.06.2011

@author: Florian Mueller
'''
import math
import re

def calcPageRank(pages, iter, d):
    N = len(pages.keys())
    for i in xrange(0,iter):
        for p in pages.values():
            tmp = 0.0
            for incoming in p.getIncomingLinks():
                q = pages[incoming]
                tmp += q.getPageRank() / float(len(q.getOutgoingLinks()))
            old = p.getPageRank()
            new = (1.0 - d) * (1.0 / N) + d * tmp
            
            if math.fabs(new - old) < 0.0001:
                return pages
            
            p.setPageRank(new)
    return pages

def calcHubAndAuthorities(pages, iter):
    for i in xrange(0,iter):
        norm = 0.0
        for p in pages.values():
            for id in p.getIncomingLinks():
                q = pages[id]
                p.setAuthority(p.getAuthority() + q.getHub())
            norm += math.sqrt(p.getAuthority())
        norm = math.sqrt(norm)
        
        for p in pages.values():
            old = p.getAuthority()
            new = p.getAuthority() / norm
            if math.fabs(new - old) < 0.0001:
                return pages
            
            p.setAuthority(p.getAuthority() / norm)
        norm = 0.0
        
        for p in pages.values():
            for id in p.getOutgoingLinks():
                q = pages[id]
                p.setHub(p.getHub() + q.getAuthority())
            norm += math.sqrt(p.getHub())
        norm = math.sqrt(norm)
        
        for p in pages.values():
            old = p.getHub()
            new = p.getHub() / norm
            if math.fabs(new - old) < 0.0001:
                return pages
            
            p.setHub(new)
    return pages

def makeRootSet(query, pages):
    keys = query.split(" ");
    result = {}
    
    for p in pages.values():
        data = str(p.getData())
        hits = 0
        for key in keys:
            if None != re.search(" " + key + " ", data, re.I):
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
            
def concatDicts(d1, d2):
    return dict(d1.items() + d2.items())
        
    
        