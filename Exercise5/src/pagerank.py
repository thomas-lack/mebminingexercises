'''
Created on 24.06.2011

@author: Florian Mueller
'''
from datautils import readGraph

def calcPageRank(webpages, iter, d):
    N = len(webpages.keys())
    for i in xrange(0,iter):
        for p in webpages.values():
            tmp = 0.0
            for incoming in p.getIncomingLinks():
                q = webpages[incoming]
                tmp += q.getPageRank() / float(len(q.getOutgoingLinks()))
            pr = (1.0 - d) * (1.0 / N) + d * tmp
            p.setPageRank(pr)
    return webpages