'''
Created on 05.06.2011

@author: m0ep
'''
import os, sys, re, porterstemer

def createTFVectorsFromFolder(inputFolder, outputFolder):
    files = os.listdir(inputFolder);
    stopwords = readStopwordsFile("stopwords_en.txt")
    stemmer = porterstemer.PorterStemmer()
    
    for file in files:
        words = readWordsFromFile("%s/%s" % (inputFolder,file))
        words = filter(lambda x: not stopwords.__contains__(x.lower()) ,words)
        words = map(lambda x: stemmer.stem(x, 0, len(x)-1), words)
        tfVector = getWordFrequencies(words)
        writeTFVectorToFile("%s/%s.tfv" % (outputFolder,file), tfVector)
        
        
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

def writeTFVectorToFile(filename, tfVector):
    fd = open(filename,"w")
    for key in tfVector.keys():
        fd.write(key)
        fd.write(",")
        fd.write(str(tfVector[key]))
        fd.write("\n")
    fd.close()
    
def readFilesAndMakeText(destPath):
    files = os.listdir(destPath)
    result = {}
    
    for file in files:
        print file
        fd = open("%s/%s" % (destPath,file))
        html = fd.read()
        html = html[html.find("<HTML>"):]
        #html = re.sub("<!WA[0-9]+>", "", html)
        #text = DeHTMLParser.dehtml(html)
        result[file] = html
        fd.close()
    
    return result
    

if __name__ == '__main__':
    srcFolder = "data/original"
    destFolder = "data/tf"
    
    stopwords = readStopwordsFile("stopwords_en.txt")
    stemmer = porterstemer.PorterStemmer()
    
    clazzFolders = os.listdir(srcFolder)
    
    for clazz in clazzFolders:
        clazzSrcPath = "%s/%s" % (srcFolder,clazz)
        clazzDestPath = "%s/%s" % (destFolder,clazz)
        
        if not os.path.exists(clazzDestPath):
            os.mkdir(clazzDestPath)
            
        for subfolder in ("test","training"):
            srcPath = "%s/%s/%s" % (srcFolder,clazz,subfolder)
            destPath = "%s/%s/%s" % (destFolder,clazz,subfolder)
            
            if not os.path.exists(destPath):
                os.mkdir(destPath)
                
            files = os.listdir(srcPath)
            
            for file in files:
                words = readWordsFromFile("%s/%s" % (srcPath,file))
                words = filter(lambda x: not stopwords.__contains__(x.lower()) ,words)
                words = map(lambda x: stemmer.stem(x, 0, len(x)-1), words)
                tfVector = getWordFrequencies(words)
                writeTFVectorToFile("%s/%s.tfv" % (destPath,file), tfVector)