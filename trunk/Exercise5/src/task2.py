'''
Created on 24.06.2011

@author: Florian Mueller
'''
from datautils import readGraph
from pagerank import calcPageRank
from hits import calcHubAndAuthorities

pages = readGraph("data")
pages = calcPageRank(pages, 10000, 0.85)
pages = calcHubAndAuthorities(pages, 10000)

for p in pages.values():
    print p.getId(), p.getPageRank(),p.getAuthority(),p.getHub()