'''
Created on 22.06.2011

@author: Florian Mueller
'''

class Node(object):
    def __init__(self, id, link, data):
        self.id = id
        self.link = link
        self.data = data
        self.outgoingLinks = []
        self.incomingLinks = []
        
    def getId(self):
        return self.id
        
    def getLink(self):
        return self.link

    def getData(self):
        return self.data
    
    def getOutgoingLinks(self):
        return self.outgoingLinks
    
    def setOutgoingLinks(self, ids):
        self.outgoingLinks = ids
    
    def addOutgoingLink(self, id):
        self.outgoingLinks.append(id)
        
    def getIncomingLinks(self):
        return self.incomingLinks
    
    def setIncomingLinks(self, ids):
        self.incomingLinks = ids
    
    def addIncomingLink(self, id):
        self.incomingLinks.append(id)
        