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
    return map(lambda x: x.lower(),filter(lambda x: not 0 == len(x), data.split(" ")))  

def readStopwordsFile(file):
    fd = open(file)
    data = str(fd.read())
    fd.close()
    return data.splitlines()

def filterStopWords(words, stopwords=[]):
    return filter(lambda x: not stopwords.__contains__(x.lower()) ,words)

def getWordFrequencies(words=[]):
    N = float(len(words))
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
        result[key] = reduce(lambda x, y: x + y, groupList[key]) / N 
    return result

def doStemming(words):
    stemmer = PorterStemmer()
    return map(lambda x: stemmer.stem(x, 0, len(x)-1), words)

def createFeatureVectors(fileFreq, docFreq):
    fileFeatVecDict = {}
    N = float(len(docFreq))
    
    # iterate over files
    for filekey in fileFreq:
        dict = {}
        #iterate over frequencies
        for wordkey in fileFreq[filekey].keys():
            value = fileFreq[filekey][wordkey]
            # calculate TF*IDF
            dict[wordkey] = value * math.log10(N / docFreq[wordkey])
            
        fileFeatVecDict[filekey] = dict
        
    return fileFeatVecDict
 
def getTopNWords(dict, N):
    words = docFreqDict.keys()
    words = sorted(words, lambda x,y: cmp(docFreqDict[y], docFreqDict[x]))
    return words[0:N]

def filterNFeaturesByFrequency(features, docFreqDict, N):
    keys = features.keys()
    keys= sorted(keys, lambda x,y:cmp(docFreqDict[y],docFreqDict[x]))
    select = keys[0:N]
    
    result = {}
    for key in features.keys():
        if select.__contains__(key):
            result[key] = features[key]
    
    return result    

def writeSVMFile(featureVectors, allowed, file):
    fd = open(file,"w")
    for fileKey in featureVectors.keys():
        counter = 0
        fd.write("+1 ")
        for featureKey in featureVectors[fileKey].keys():
            if allowed.__contains__(featureKey):
                value = featureVectors[fileKey][featureKey]
                if 0.0 != value:
                    fd.write("%d:%f "%(counter, value))
                    counter += 1
        fd.write("# %s\n"%(fileKey))
    fd.close()

def writeListToFile(list, file):
    fd = open(file, "w")
    for entry in list:
        fd.write(entry)
        fd.write("\n")
    fd.close()
    
def writeDictionaryToFile(dict, file):
    fd = open(file, "w")
    for key in dict.keys():
        fd.write(key)
        fd.write(" = ")
        fd.write(str(dict[key]))
        fd.write("\n")
    fd.close()

if __name__ == '__main__':  
    stopwords = readStopwordsFile("../data/stopwords_english.txt")
    trainingFiles = os.listdir("../data/training")
    fileFreqDict = {}
    docFreqDict = {}

    start = time.time()
    
    for file in trainingFiles:
        if file[0] == ".":
            continue
        
        print "transform %s ..."%(file)
        words = readWordsFromFile("../data/training/%s"%(file))
        writeListToFile(words, "../result/3.2.1/%s"%(file))
        words = filterStopWords(words, stopwords)
        stemms = doStemming(words)
        writeListToFile(stemms, "../result/3.2.2/%s"%(file))
        freq = getWordFrequencies(stemms)
        
        # create document frequency
        for key in freq.keys():
            if docFreqDict.__contains__(key):
                docFreqDict[key] = docFreqDict[key] + 1
            else:
                docFreqDict[key] = 1 
        
        fileFreqDict[file] = freq 
        
    
    print "Write document frequencies dictionary file..."
    writeDictionaryToFile(docFreqDict, "../result/documentfrequencies.txt")
    featVec = createFeatureVectors(fileFreqDict, docFreqDict)
    
    print "Write TF-IDF-Vector files..."
    for key in featVec.keys():
        writeDictionaryToFile(featVec[key], "../result/3.2.3/%s"%(key))
        
    N = 800
    topNWords = getTopNWords(docFreqDict, N)
    #writeListToFile(topNWords, "../result/top%dwords.txt"%(N))
    
    print "Write trainings file..."
    writeSVMFile(featVec, topNWords, "../result/training.svm")
    print "transfomation took %ds"%(time.time() - start)
    
    