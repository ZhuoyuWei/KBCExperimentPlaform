import sys

#def readdict(filename):

#def readformula(filename):

            

def onerel(filename,rel):
    try:
        f=open(filename,"r")
    except Exception as e:
        return
    count=0
    for line in f:
        if count == rel:
            ss=line.strip().split()
            i=0;
            maxval=-1
            sortlist=[]
            for j in range(len(ss)-1):
                sortlist.append([float(ss[j]),j])
            sortlist.sort(reverse=True)
            try:
                print str(rel)+"\t"+str(sortlist[0][1])
                print str(rel)+"\t"+str(sortlist[1][1])
                print str(rel)+"\t"+str(sortlist[2][1])            
            except Exception as e:
                break        
            break
        count+=1
    f.close()
    
if __name__=='__main__':
    dir=sys.argv[1]
    relnum=int(sys.argv[2])
    for i in range(relnum):
        onerel(dir+"/"+str(i)+"path.weight",i)
    
    