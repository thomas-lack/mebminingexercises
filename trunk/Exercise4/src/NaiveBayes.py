import os
import math

def readTFFile(path):
    result = {}
    fd = open(path)
    for line in fd:
        split = line.split(",")
        term = str(split[0])
        freq = int(split[1])
                
        if result.__contains__(term):
            result[term] += freq
        else:
            result[term] = freq
    return result

def readTestFile(path):
    result = []
    fd = open(path)
    for line in fd:
        split = line.split(",")
        result.append(str(split[0]))
                      
    return result
            
def readClassFolder(folder):
    result = []
    files = os.listdir(folder)
    for file in files:
        result.append(readTFFile("%s/%s" % (folder,file)))
    
    return result

def readTrainingClasses():
    result = {}
    result["course"] = readClassFolder("data/tf/course/training")
    result["faculty"] = readClassFolder("data/tf/faculty/training")
    result["other"] = readClassFolder("data/tf/other/training")
    result["project"] = readClassFolder("data/tf/project/training")
    result["student"] = readClassFolder("data/tf/student/training")
    return result
                
def _total_n_wc(wordset, clazz):
    sum = 0;
    for w in wordset.keys():
        sum += _n_wc(w, clazz)
    return sum
                
def _n_wc(word, clazz):
    result = 0
    for d in clazz:
        if d.keys().__contains__(word):
            result += d[word]
    
    return result

def _p_wc(word, total_n_wc, clazz):    
    return (_n_wc(word, clazz) + 1) / float(total_n_wc)

def naiveBayes(trainingsSet, words):
    value = None
    result = "unknown"
    
    for clazz in trainingsSet.keys():
        print clazz
        sum = 0
        total_n_wc = _total_n_wc(words, trainingsSet[clazz])
        for word in words.keys():
            sum += math.log10(_p_wc(word, total_n_wc, trainingsSet[clazz]))
        
        p = math.log10(0.2) + sum
        print p, clazz
        if value < p or value == None:
            value = p
            result = clazz
    
    return result
        

if __name__ == '__main__':
    trainingsSet = readTrainingClasses()
    words = readTFFile("data/tf/faculty/test/http_^^robios8.me.wisc.edu^~lumelsky^lumelsky.html.txt.tfv")
    print naiveBayes(trainingsSet, words)
        