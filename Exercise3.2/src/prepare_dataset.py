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

def filterTopNFeaturesByDocumentFrequency(features, docFreqDict, N):
    keys = features.keys()
    keys= sorted(keys, lambda x,y:cmp(docFreqDict[y],docFreqDict[x]))
    select = keys[0:N]
    
    result = {}
    for key in features.keys():
        if select.__contains__(key):
            result[key] = features[key]
    
    return result    

def writeSVMFile(featureVectors, target, file):
    fd = open(file,"w")
    for fileKey in featureVectors.keys():
        counter = 1
        fd.write("%s "%(target))
        for featureKey in featureVectors[fileKey].keys():
            value = featureVectors[fileKey][featureKey]
            if 0.0 != value:
                fd.write("%d:%.13f "%(counter, value))
                counter += 1
        fd.write("\n")
        #fd.write("# %s\n"%(fileKey))
    fd.close()

if __name__ == '__main__':  
    folderPaths = ("../data/non-course/training","../data/non-course/test","../data/course/training","../data/course/test")
    svmPatters = ("../result/training_positive_N%d.svm","../result/test_positive_N%d.svm","../result/training_negative_N%d.svm","../result/test_negative_N%d.svm")
    targets = ("+1","+1","-1","-1")
    
    stopwords = readStopwordsFile("../data/stopwords_english.txt")
    
    for i in (0,1,2,3):
        fileFreqDict = {}
        docFreqDict = {}
        filesInFolder = os.listdir(folderPaths[i])
        for file in filesInFolder:
            if file[0] == ".":
                continue
                
            words = readWordsFromFile("%s/%s"%(folderPaths[i],file))
            stWo = filterStopWords(words, stopwords)
            stemms = doStemming(stWo)
            freq = getWordFrequencies(stemms)
            #freq = getWordFrequencies(stWo)
            #freq = getWordFrequencies(words)
             
            # create document frequency
            for key in freq.keys():
                if docFreqDict.__contains__(key):
                    docFreqDict[key] = docFreqDict[key] + 1
                else:
                    docFreqDict[key] = 1 
                                        
            fileFreqDict[file] = freq
        
        for N in (50,100,200,400,800,1000):  
            filteredTopN = {}
            for key in fileFreqDict:
                filteredTopN[key] = filterTopNFeaturesByDocumentFrequency(fileFreqDict[key], docFreqDict, N)
            
            featureVector = createFeatureVectors(filteredTopN, docFreqDict)
            print "Write %s" % (svmPatters[i]%(N))
            writeSVMFile(featureVector, targets[i], svmPatters[i]%(N))
    