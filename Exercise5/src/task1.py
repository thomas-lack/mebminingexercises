'''
Created on 24.06.2011

@author: Florian Mueller
'''
from datautils import readGraph
from query import doQuery

pages = readGraph("data")
result = doQuery("machine learning", pages)


print "\nPageRank"
for page in result[0]:
    id = page[0]
    rank = page[1]
    print id, rank, pages[id].getLink()
    
print "\nHIST"
for hist in result[1]:
    id = hist[0]
    hub = hist[1]
    auth = hist[2]
    print id, hub + auth, hub, auth, pages[id].getLink()  
