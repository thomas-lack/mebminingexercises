'''
Created on 22.06.2011

@author: Florian Mueller
'''

class Node(object):
    def __init__(self, id, link, data):
        self.id = id
        self.link = link
        self.data = data
        self.pageRank = 0.0;
        self.hub = 1.0;
        self.authority = 1.0;
        self.outgoingLinks = []
        self.incomingLinks = []
        
    def getId(self):
        return self.id
        
    def getLink(self):
        return self.link

    def getData(self):
        return self.data
        
    def getPageRank(self):
        return self.pageRank
    
    def setPageRank(self, _rank):
        self.pageRank = _rank
        
    def getHub(self):
        return self.hub
    
    def setHub(self, _hub):
        self.hub = _hub
        
    def getAuthority(self):
        return self.authority
    
    def setAuthority(self, _authority):
        self.authority = _authority
    
    def getOutgoingLinks(self):
        return self.outgoingLinks
    
    def addOutgoingLink(self, id):
        self.outgoingLinks.append(id)
        
    def getIncomingLinks(self):
        return self.incomingLinks
    
    def addIncomingLink(self, id):
        self.incomingLinks.append(id)