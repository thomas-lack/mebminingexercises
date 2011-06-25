'''
Created on 24.06.2011

@author: Florian Mueller
'''
from datautils import readGraph
from algorithms import calcPageRank, calcHubAndAuthorities, makeRootSet,\
    makeBaseSet, concatDicts


pages = readGraph("data")
pages = calcPageRank(pages, 10000, 0.85)
pages = calcHubAndAuthorities(pages, 10000)

rootSet = makeRootSet("web mining", pages)
baseSet = makeBaseSet(pages, rootSet)
baseRootSet = concatDicts(rootSet, baseSet)

print rootSet
print baseSet
print baseRootSet

#for p in pages.values():
#    print p.getId(), p.getPageRank(),p.getAuthority(),p.getHub()
    
