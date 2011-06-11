'''
Created on 11.06.2011

@author: m0ep
'''

import os

class FeatureClass(object):
    termSet = {}
    totalTermCount = 0

    def __init__(self, folder):
        files = os.listdir(folder) 
        for file in files:
            fd = open(("%s/%s") % (folder,file),"r")
            for line in fd:
                split = line.split(",")
                term = str(split[0])
                freq = int(split[1])
                
                if self.termSet.__contains__(term):
                    self.termSet[term] += freq
                else:
                    self.termSet[term] = freq
                
                self.totalTermCount += freq
                
    def getTermSet(self):
        return self.termSet
    
    def getTotalTermCount(self):
        return self.totalTermCount
            