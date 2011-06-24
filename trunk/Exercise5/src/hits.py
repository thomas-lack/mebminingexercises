'''
Created on 24.06.2011

@author: Florian Mueller
'''
import math

def calcHubAndAuthorities(webpages, iter):
    for i in xrange(0,iter):
        norm = 0.0
        for p in webpages.values():
            for id in p.getIncomingLinks():
                q = webpages[id]
                p.setAuthority(p.getAuthority() + q.getHub())
            norm += math.sqrt(p.getAuthority())
        norm = math.sqrt(norm)
        
        for p in webpages.values():
            p.setAuthority(p.getAuthority() / norm)
        norm = 0.0
        
        for p in webpages.values():
            for id in p.getOutgoingLinks():
                q = webpages[id]
                p.setHub(p.getHub() + q.getAuthority())
            norm += math.sqrt(p.getHub())
        norm = math.sqrt(norm)
        
        for p in webpages.values():
            p.setHub(p.getHub() / norm)
    return webpages
        