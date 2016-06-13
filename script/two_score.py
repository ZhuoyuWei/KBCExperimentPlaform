import sys
import math

relnum=int(sys.argv[3])
bias1=int(sys.argv[4])
bias2=int(sys.argv[5])

readdata=[]
for i in range(relnum):
    readdata.append([])
    for j in range(6):
        readdata[i].append([])
        for k in range(7):
            readdata[i][j].append(0)
readdata2=[]
for i in range(relnum):
    readdata2.append([])
    for j in range(6):
        readdata2[i].append([])
        for k in range(7):
            readdata2[i][j].append(0)            
            

for i in range(relnum):
    try:
        f=open(sys.argv[1]+str(i)+'log','r')
    except Exception as e:
        continue
    tmplines=[]
    for line in f:
        line=line.strip()
        tmplines.append(line)
    #print 'tmplist '+str(len(tmplines))+" "+str(i)
    if len(tmplines)<7+bias1:
        continue
    for j in range(1,6):
        ss=tmplines[j+bias1].split('\t')
        if len(ss) != 7:
            print 'error'
        for k in range(7):
            #print float(ss[k])
            a=float(ss[k])
            #print a
            if not math.isnan(a):
                readdata[i][j-1][k]=a
                
for i in range(relnum):
    try:
        f=open(sys.argv[2]+str(i)+'log','r')
    except Exception as e:
        continue
    tmplines=[]
    for line in f:
        line=line.strip()
        tmplines.append(line)
    #print 'tmplist '+str(len(tmplines))+" "+str(i)
    if len(tmplines)<7+bias2:
        continue
    for j in range(1,6):
        ss=tmplines[j+bias2].split('\t')
        if len(ss) != 7:
            print 'error'
        for k in range(7):
            #print float(ss[k])
            a=float(ss[k])
            #print a
            if not math.isnan(a):
                readdata2[i][j-1][k]=a
            
#print readdata
            
sum=[]
for i in range(6):
    sum.append([])
    for j in range(7):
        sum[i].append(0)

for r in range(relnum):
    if readdata[r][0][0] > readdata2[r][0][0]:
        for i in range(6):
            for j in range(6):
                sum[i][j]+=readdata[r][i][j]*readdata[r][i][6]
    else:
        for i in range(6):
            for j in range(6):
                sum[i][j]+=readdata2[r][i][j]*readdata2[r][i][6]
                
                
triplet_size=0
for r in range(relnum):
    triplet_size+=readdata[r][0][6]
      
for i in range(6):
    for j in range(6):
        sum[i][j]/=triplet_size
        sys.stdout.write(str(sum[i][j])+'\t')
    sys.stdout.write('\n');