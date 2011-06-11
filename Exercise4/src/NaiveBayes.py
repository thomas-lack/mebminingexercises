from FeatureClass import FeatureClass
if __name__ == '__main__':
    
    trainingsSet = {}
    trainingsSet["course"] = FeatureClass("data/tf/course/training")
    trainingsSet["faculty"] = FeatureClass("data/tf/faculty/training")
    trainingsSet["other"] = FeatureClass("data/tf/other/training")
    trainingsSet["project"] = FeatureClass("data/tf/project/training")
    trainingsSet["student"] = FeatureClass("data/tf/student/training")
        