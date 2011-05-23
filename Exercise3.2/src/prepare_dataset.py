'''
Created on 23.05.2011

@author: m0ep
'''
from porterstemer import PorterStemmer
import os, time, re
import math

def readWordsFromFile(file):
    fd = open(file)
    data = str(fd.read())
    data = re.sub("[-_!?.,;:'#+*~\"$%&/()=\}\]\[{}\'\`]", "", data)
    data = re.sub("[\s]+", " ", data)
    return filter(lambda x: not 0 == len(x), data.split(" "))  

def readStopwordsFile(file):
    fd = open(file)
    data = str(fd.read())
    fd.close()
    return data.splitlines()

def filterStopWords(words, stopwords=[]):
    return filter(lambda x: not stopwords.__contains__(x.lower()) ,words)

def getWordFrequencies(words=[]):
    # map stage of map-reduce-algo to count word frequencies
    mappedList = map(lambda x: (x.lower(),1), words)
    
    # make groups
    groupList = {}
    for entry in mappedList:
        if groupList.__contains__(entry[0]):
            groupList[entry[0]].append(entry[1])
        else:
            groupList[entry[0]] = [entry[1]]
    result = {}
    
    # do reduce on all groups
    for key in groupList.keys():
        result[key] = reduce(lambda x, y: x + y, groupList[key])
    return result

def doStemming(words):
    stemmer = PorterStemmer()
    return map(lambda x: stemmer.stem(x, 0, len(x)-1), words)


if __name__ == '__main__':
    stopwords = readStopwordsFile("../data/stopwords_english.txt")
    trainingFiles = os.listdir("../data/training")
    docFreqDict = {}
    totalFreqDict = {}

    start = time.time()
    
    for file in trainingFiles:
        words = readWordsFromFile("../data/training/%s"%(file))
        words = filterStopWords(words, stopwords)
        stemms = doStemming(words)
        freq = getWordFrequencies(stemms)
        
        for key in freq.keys():
            freq[key] = freq[key] / float(len(freq)) 
            if totalFreqDict.__contains__(key):
                totalFreqDict[key] = totalFreqDict[key] + 1
            else:
                totalFreqDict[key] = 1 
        
        docFreqDict[file] = freq 
        
    print time.time() - start
    
    t = docFreqDict[trainingFiles[50]]
    for key in t:
        print key,"=",t[key] * math.log10(len(totalFreqDict) / float(totalFreqDict[key]))
    