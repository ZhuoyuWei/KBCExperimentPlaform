import sys
import math

relnum=int(sys.argv[2])

readdata=[]
for i in range(relnum):
    readdata.append([])
    for j in range(6):
        readdata[i].append([])
        for k in range(7):
            readdata[i][j].append(0)

for i in range(relnum):
    f=open(sys.argv[1]+str(i)+'log','r')
    tmplines=[]
    for line in f:
        line=line.strip()
        tmplines.append(line)
    #print 'tmplist '+str(len(tmplines))+" "+str(i)
    if len(tmplines)<7:
        continue
    for j in range(1,6):
        ss=tmplines[j].split('\t')
        if len(ss) != 7:
            print 'error'
        for k in range(7):
            #print float(ss[k])
            a=float(ss[k])
            #print a
            if not math.isnan(a):
                readdata[i][j-1][k]=a
            
#print readdata
            
sum=[]
for i in range(6):
    sum.append([])
    for j in range(7):
        sum[i].append(0)

for r in range(relnum):
    for i in range(6):
        for j in range(6):
            sum[i][j]+=readdata[r][i][j]*readdata[r][i][6]
triplet_size=0
for r in range(relnum):
    triplet_size+=readdata[r][0][6]
      
for i in range(6):
    for j in range(6):
        sum[i][j]/=triplet_size
        sys.stdout.write(str(sum[i][j])+'\t')
    sys.stdout.write('\n');